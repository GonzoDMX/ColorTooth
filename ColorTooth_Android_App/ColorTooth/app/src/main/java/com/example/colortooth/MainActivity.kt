package com.example.colortooth


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.colortooth.adapters.ClickListAdapter
import com.example.colortooth.data.ClickApplication
import com.example.colortooth.data.ClickData
import com.example.colortooth.databinding.ActivityMainBinding
import com.example.colortooth.interfaceJS.HeightInterface
import com.example.colortooth.interfaceJS.WidthInterface
import com.example.colortooth.network.BluetoothClient
import com.example.colortooth.ui.MainViewModel
import com.example.colortooth.ui.MainViewModelFactory
import com.example.colortooth.utilities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt


// Declare string, pour passer des messages de MainActivity à BluetoothClient Class
var message = ""
// Déclarer l'indicateur d'état de la connexion
var connexion = false
// Indicateur de fin de connexion, indique quand l'utilisateur a fermé la connexion pour que le fil puisse se fermer
var terminateConn = false

var myWebViewWidth = 540
var myWebViewHeight = 540



class MainActivity : AppCompatActivity() {

    private var msgCount: Int = 0

    private var connectionStatus: String = "Device is not connected."

    private lateinit var binding: ActivityMainBinding

    private val mViewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as ClickApplication).repository)
    }

    // Déclarer la baie contient l'ID de périphérique Bluetooth
    private var devices = ArrayList<BluetoothDevice>()
    // Déclarer HashMap contient les adresses de l'appareil Bluetooth
    private var devicesMap = HashMap<String, BluetoothDevice>()
    // Déclarer l'adaptateur Array, contient des chaînes pour Bluetooth AlertDialog
    private var mArrayAdapter: ArrayAdapter<String>? = null

    private var mGraph = GraphJS

    private var myWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        // Bind activity to access buttons and whatnot
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerLog)
        val adapter = ClickListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Turn off recyclervier animation, otherwise it flashes each time the database is updated
        recyclerView.itemAnimator = null
        recyclerView.setHasFixedSize(true)

        mViewModel.allCLicks.observe(this) { clicks ->
            clicks.let { adapter.submitList(it) }
        }

        // Définir le layout de la liste des appareils AlertDialog
        mArrayAdapter = ArrayAdapter(this, R.layout.dialog_select_device)

        bluetoothPermissions()
        bindButtonListeners()

        /*
        try {
            // Get Android device screen size for correct webview scaling
            val display = windowManager.currentWindowMetrics.bounds
            myWebViewWidth = (display.right * 0.35).roundToInt()
            myWebViewHeight = (display.bottom * 0.14).roundToInt()
        }
        catch(ex: Exception) {
            Log.d("WINDOW_ERR", ex.toString())
        }
        finally{
            if(myWebViewWidth == 0) {
                myWebViewWidth = 500
                myWebViewHeight = 400
            }
        }
         */


        val webSettings = binding.webViewMain.settings
        myWebView = binding.webViewMain as WebView
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        binding.webViewMain
        // Test retrieving data from a kotlin JavaScript Interface
        myWebView!!.addJavascriptInterface(WidthInterface(this), "AndroidWidth")
        myWebView!!.addJavascriptInterface(HeightInterface(this), "AndroidHeight")
        myWebView!!.loadUrl("file:///android_asset/ClickChart.html")

        binding.webViewMain.isVisible = false
        binding.webViewHolder.isVisible = false

        // Disables web view scrolling by catching and overriding touch events
        binding.webViewMain.setOnTouchListener { _: View, motionEvent: MotionEvent -> motionEvent.action == MotionEvent.ACTION_MOVE }

        binding.switchWeb.setOnClickListener() {
            if(!binding.webViewMain.isVisible) {
                binding.webViewMain.isVisible = true
                binding.webViewHolder.isVisible = true
                myWebView!!.loadUrl("javascript:postChart(${mGraph.pullJSON()})")
            } else {
                binding.webViewMain.isVisible = false
                binding.webViewHolder.isVisible = false
            }
        }
    }


    fun refreshTester(v:WebView) {
        GlobalScope.launch(context = Dispatchers.Main) {
            for(i in 1..25) {
                mGraph.updateJSON(msgCount, msgCount / 2, msgCount * 2, 0, 0)
                v.loadUrl("javascript:postChart(${mGraph.pullJSON()})")
                delay(1000)
                msgCount += 1
                Log.d("HELLO_JACK", "print")
            }
        }
    }

    private fun bluetoothPermissions() {
        // Si les autorisations Bluetooth ne sont pas définies
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Demandez l'autorisation d'accéder au Bluetooth lors du premier lancement de l'application
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }


    private fun bindButtonListeners() {
        binding.buttonConn.setOnClickListener() { view ->
            // Si le Bluetooth n'existe pas sur l'appareil
            if (BluetoothAdapter.getDefaultAdapter() == null) {
                messageToast(this, "Bluetooth is disabled!")
            }
            // Vérifiez que le Bluetooth est activé
            else if(!BluetoothAdapter.getDefaultAdapter().isEnabled){
                // Afficher un message d'erreur
                errorDialog(this, "Bluetooth is not enabled,\nEnable bluetooth and retry.")
            } else {
                // Initialiser la variable d'appareil, stocke temporairement l'appareil de BluetoothAdapter
                devices = ArrayList()
                // Réinitialiser la liste des appareils
                mArrayAdapter!!.clear()
                // Si la connexion est active
                if(connexion) {
                    // Load outgoing message, set variables and UI ELements to close the connection
                    setDisconnect()
                    // Si la connexion n'est pas active
                } else {
                    // Load outgoing message, reset variables and UI Elements to initiate a connection
                    setConnect()
                    // Load paired devices into bluetooth adapter
                    loadBluetoothAdapter()
                    // Vérifiez qu'il existe des appareils couplés avant la découverte, sinon l'application se bloque
                    if(devicesMap.isNotEmpty()) {
                        // If devices are available prompt user to choose a device
                        discoverBluetoothDevices()
                    } else {
                        // Déclarer la connexion fermée
                        connexion = false
                        // Modifier le texte sur le bouton de connexion
                        binding.buttonConn.text = "Connect"
                        // Mettre à jour le journal des messages
                        errorDialog(this, "No paired devices detected!")
                        // Déverrouille le bouton de connexion
                        unlockButton()
                    }
                }
            }
        }

        binding.buttonClear.setOnClickListener() {
            clearDatabase()
            clearColorView()
        }

        binding.buttonOnline.setOnClickListener() {
            messageToast(this, connectionStatus)
        }
    }


    private fun setConnect() {
        clearDatabase()
        clearColorView()
        // Définir le message de connexion
        message = "<CONNECT>"
        // Déclarer la connexion ouverte
        connexion = true
        // Réinitialiser l'indicateur de fin de connexion
        terminateConn = false
        // Modifier le texte sur le bouton de connexion
        binding.buttonConn.text = "Disconnect"
        // Réinitialiser le compteur de messages
        msgCount = 0
        // Verrouiller le bouton de connexion, évite les erreurs de faux état
        binding.buttonConn.isEnabled = false
        // Réinitialiser l'aperçu des couleurs
        clearColorView()
        mGraph.reInitJSON()
    }


    private fun setDisconnect() {
        // Définir le message de déconnexion
        message = "<DISCONN>"
        // Déclarer la connexion fermée
        connexion = false
        // Modifier le texte sur le bouton de connexion
        binding.buttonConn.text = "Connect"
        // Définir l'indicateur pour fermer le thread client
        terminateConn = true
    }


    // réinitialiser l'etat de la connexion
    fun resetConnection(){
        // Déclarer la connexion fermée
        connexion = false
        // Déclarer la connexion fermée
        terminateConn = true
        // Modifier le texte sur le bouton de connexion
        binding.buttonConn.text = "Connect"
    }


    // Déverrouille le bouton de connexion
    fun unlockButton(){
        // Définir le bouton sur actif
        binding.buttonConn.isEnabled = true
    }


    private fun loadBluetoothAdapter() {
        // Effacer le hashmap, évite les erreurs si l'appareil n'est pas couplé pendant que l'application est active
        devicesMap = HashMap<String, BluetoothDevice>()
        // Pour tous les appareils couplés dans l'adaptateur Bluetooth
        for (device in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
            // Charger l'appareil avec l'adresse associée dans la carte des appareils
            devicesMap[device.address] = device
            // Ajouter un appareil à la liste des appareils
            devices.add(device)
            // Ajoutez le nom et l'adresse à un adaptateur de baie à afficher dans un ListView
            mArrayAdapter!!.add((if (device.name != null) device.name else "Unknown") + "\n" + device.address + "\nPaired")
        }
    }


    private fun discoverBluetoothDevices() {
        // Lancer le processus de découverte
        if (BluetoothAdapter.getDefaultAdapter().startDiscovery()) {
            // Configurer le générateur AlertDialog
            // Cette boîte de dialogue permet à l'utilisateur de sélectionner parmi les appareils Bluetooth couplés
            val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
            // Définir le titre AlertDialog
            dialog.setTitle("Connect to: ")
            // Ne pas autoriser l'annulation des dialogues, cela évite un bug de faux état
            dialog.setCancelable(false)
            // Créer la liste des appareils couplés à partir de l'adaptateur et de l'action de liaison
            dialog.setAdapter(mArrayAdapter) { _, which: Int ->
                // Terminer le processus de découverte Bluetooth
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                // Démarrer le thread client Bluetooth avec le périphérique sélectionné
                BluetoothClient(this, devices[which]).start()
            }
            // Rendre le AlertDialog
            dialog.show()
        }
    }


    // Internationalisation Seppress, nous voulons une sortie brute et non une traduction
    @SuppressLint("SetTextI18n")
    // La fonction reçoit un message Bluetooth et analyse le message
    fun parseMessage(text: String) {
        // Déclarer un tableau pour stocker les valeurs de couleur reçues
        var values = IntArray(4)
        // Déclarer l'en-tête du message
        val check = "<TCS>"
        // Extraire l'en-tête du message en tant que sous-chaîne
        var header: String = text.subSequence(0, 5) as String
        // Extraire les valeurs de couleur de la chaîne
        var extVals: String = text.subSequence(5, 13) as String
        // Si l'en-tête correspond à <TCS>
        if(header == check){
            // Itérer les valeurs de couleur dans le message reçu
            for(i in 0 until 7 step 2) {
                // Convertir la valeur hexadécimale reçue en Int
                values[i / 2] = hexToInt(extVals[i], extVals[i + 1])
            }
            // Évitez les bogues où le thread se ferme avant la fin de la fonction
            if(connexion) {
                val colorName = nameOfColor(values[0], values[1], values[2], values[3])
                msgCount += 1
                updateLineChart(msgCount, values[0], values[1], values[2], values[3])
                val data = ClickData(0, msgCount, colorName, values[0], values[1], values[2], values[3])
                // Write values to Database
                writeToDatabase(data)
                // Update la couleur de l'imageView
                updateColorView(data)
            }
        // Si l'en-tête ne correspond pas à <TCS>
        } else {
            parseConnectionStatus(text)
        }
    }

    //Updates JSON pushed to Line Chart in Web View
    private fun updateLineChart(t: Int, a: Int, r: Int, g: Int, b: Int) {
        mGraph.updateJSON(t, a, r, g, b)
        if(binding.webViewMain.isVisible) {
            myWebView!!.loadUrl("javascript:postChart(${mGraph.pullJSON()})")
        }
    }

    fun updateColorView(data: ClickData) {
        binding.imageView.setColorFilter(Color.argb(data.alpha, data.red, data.green, data.blue))
        binding.textAlpha.text = data.alpha.toString()
        binding.textRed.text = data.red.toString()
        binding.textGreen.text = data.green.toString()
        binding.textBlue.text = data.blue.toString()
    }


    private fun clearColorView() {
        binding.imageView.setColorFilter(Color.argb(0, 0, 0, 0))
        binding.textAlpha.text = "0"
        binding.textRed.text = "0"
        binding.textGreen.text = "0"
        binding.textBlue.text = "0"
    }


    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun parseConnectionStatus(msg: String) {
        // Si le message reçu correspond
        when (msg) {
            "<CON>GOOD_CON\r\n" -> {
                connectionStatus = "Device is connected."
                val colorInt = ContextCompat.getColor(this, R.color.online_green)
                binding.buttonOnline.compoundDrawableTintList = ColorStateList.valueOf(colorInt)
            }
            "<CON>GOOD_DIS\r\n" -> {
                connectionStatus = "Device is not connected."
                val colorInt = ContextCompat.getColor(this, R.color.offline_gray)
                binding.buttonOnline.compoundDrawableTintList = ColorStateList.valueOf(colorInt)
            }
            "<MSG>ERROR(1)" -> {
                connectionStatus = "There is a connection error."
                binding.buttonOnline.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
            }
            else -> {
                connectionStatus = "There is a connection error"
                binding.buttonOnline.compoundDrawableTintList = ColorStateList.valueOf(Color.RED)
            }
        }
    }


    private fun writeToDatabase(data: ClickData) {
        mViewModel.insert(data)
    }


    private fun clearDatabase() {
        msgCount = 0
        mViewModel.delete()
    }

    override fun onDestroy() {
        super.onDestroy()
        setDisconnect()
    }
}

