/*
 *    Andrew O'Shei
 *    22 Fevrier 2021
 *    ESP32 - Color Click
 */

#include <Wire.h>
#include <TCS3471.h>
#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

// Set 7-Seg Display Pins
#define SEG_DIN     19
#define SEG_CS      5
#define SEG_CLK     18

// Set LED GPIO pins
#define LED_RED     17
#define LED_GRN     16
#define LED_BLU     0

// Set button GPIO pins
#define BUTTON_RED  32
#define BUTTON_GRN  33
#define BUTTON_BLU  34
#define BUTTON_BLK  35

// Store current button state
int B_RED = 0;
int B_GRN = 0;
int B_BLU = 0;
int B_BLK = 0;

// Store old button state
int S_RED = 0;
int S_GRN = 0;
int S_BLU = 0;
int S_BLK = 0;

// Store button debounce delay
long D_RED = 0;
long D_GRN = 0;
long D_BLU = 0;
// Set debounce delay time in milliseconds
const long D_DELAY = 150;

// Stores current LED value
int L_RED = 0;
int L_GRN = 0;
int L_BLU = 0;

// TCS3471 Read walues
uint8_t CLR_VAL = 0;
uint8_t RED_VAL = 0;
uint8_t GRN_VAL = 0;
uint8_t BLU_VAL = 0;

// Set Read delay
long D_CLICK = 0;
long DELAY_CLICK = 500;

// All hex characters for display 0-F
uint8_t disp_char[16] = {0x7E, 0x30, 0x6D, 0x79, 0x33, 0x5B, 0x5F, 0x70, 0x7F, 0x7B, 0x77, 0x1F, 0x4E, 0x3D, 0x4F, 0x47};

// Error Message
uint8_t disp_error[8] = {0x4F, 0x05, 0x05, 0x1D, 0x05, 0x00, 0x7E, 0x00};

// Connect Message
uint8_t disp_conn[8] = {0x4E, 0x7E, 0x76, 0x76, 0x4F, 0x4E, 0x70, 0xC0};

// Disconnect Message
uint8_t disp_disc[8] = {0x3D, 0x30, 0x5B, 0x4E, 0x7E, 0x76, 0x76, 0x80};

// Serial Buffer
char ser_buffer[24];
bool ser_conn = false;
uint8_t disp_ser[8] = {0x5B, 0x4F, 0x05, 0x30, 0x77, 0x8E, 0x80, 0x80};
int ser_counter = 0;

BluetoothSerial SerialBT;

bool connect_status = false;

// BlueTooth I/O Buffer
char bufferBT[255];

char conn[13] =    { '<', 'C', 'O', 'N', '>', 'G', 'O', 'O', 'D', '_', 'C', 'O', 'N' };
char conDeja[13] = { '<', 'C', 'O', 'N', '>', 'D', 'E', 'J', 'A', '_', 'C', 'O', 'N' };
char disc[13] =    { '<', 'C', 'O', 'N', '>', 'G', 'O', 'O', 'D', '_', 'D', 'I', 'S' };
char disDeja[13] = { '<', 'C', 'O', 'N', '>', 'D', 'E', 'J', 'A', '_', 'D', 'I', 'S' };
char erro[13] =    { '<', 'M', 'S', 'G', '>', 'E', 'R', 'R', 'O', 'R', '(', '1', ')' };
char tcs[13] =     { '<', 'T', 'C', 'S', '>', '0', '0', '0', '0', '0', '0', '0', '0' };
char hex_vals[16] ={ '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' }; 

// i2C Read and Write functions
void i2cWrite(byte address, byte count, byte* buffer);
void i2cRead(byte address, byte count, byte* buffer);

// Create TCS3471 Object
TCS3471 TCS3471(i2cWrite, i2cRead);

// Writes data to the 7 segment display
void setDisplay(byte address, byte value) {
   digitalWrite(SEG_CS, LOW);
   shiftOut(SEG_DIN, SEG_CLK, MSBFIRST, address);
   shiftOut(SEG_DIN, SEG_CLK, MSBFIRST, value);
   digitalWrite(SEG_CS, HIGH);
}

void setup() {

  // Start UART Serial interface
  Serial.begin(115200);

  // Start BlueTooth, set device name
  SerialBT.begin("ColorClick");

  // Start i2C
  Wire.begin();

  // Setup LED pin mode
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_GRN, OUTPUT);
  pinMode(LED_BLU, OUTPUT);

  // Set all LEDs to off
  digitalWrite(LED_RED, 0);
  digitalWrite(LED_GRN, 0);
  digitalWrite(LED_BLU, 0);

  // Setup 7 Seg Display Pins
  pinMode(SEG_DIN, OUTPUT);
  pinMode(SEG_CS, OUTPUT);
  pinMode(SEG_CLK, OUTPUT);
  digitalWrite(SEG_CLK, HIGH);
  delay(500);

  // Configure 7 Seg Display
  setDisplay(0x0F, 0x00); // Disable Test Mode
  setDisplay(0x0C, 0x01); // Set Normal Op
  setDisplay(0x0B, 0x07); // Set number of digits 0-7 = 8 in total
  setDisplay(0x0A, 0x0F); // Set Brightness 0F is Max
  setDisplay(0x09, 0x00); // Disable Decode Mode

  loadDisplay();

  // Check if TCS3471 is connected and functioning
  if(!TCS3471.detect()){
    //Serial.println("Error: Color Click not detected!");
    setMessage(disp_error);
    while(1);
  }

  // Setup TCS3471
  // Range is 2.4ms to 614.4ms, more time means slower but more precise
  TCS3471.setIntegrationTime(350.0);
  // Range is 2.4ms to 7400ms, longer waits mean slower reads but lower power consumption
  TCS3471.setWaitTime(150.0);
  // TCS3471_GAIN_1X, TCS3471_GAIN_4X, TCS3471_GAIN_16X and TCS3471_GAIN_60X
  TCS3471.setGain(TCS3471_GAIN_1X);
  // Enable the TCS3471 Chip
  TCS3471.enable();
  //Serial.println("TCS3471 Enabled!");

  // Setup push button pin mode
  pinMode(BUTTON_RED, INPUT);
  pinMode(BUTTON_GRN, INPUT);
  pinMode(BUTTON_BLU, INPUT);
  pinMode(BUTTON_BLK, INPUT);
}

void loadDisplay() {
    uint8_t roll = 0b00000010;
    for(int i = 0; i < 13; i++) {
      if(i == 6) {
        roll = 0b00000010;
      }
      if(i == 12) {
        roll = 0x00;
      }
      for(int i = 8; i > 0; i--){
        setDisplay(i, roll);
      }
      roll = roll << 1;
      delay(100);
    }
}

void serialInFlush(){
  int i;
  while(Serial.available()) {
    i = Serial.read();
  }
}

void loop() {

  // Read current button states
  B_RED = digitalRead(BUTTON_RED);
  B_GRN = digitalRead(BUTTON_GRN);
  B_BLU = digitalRead(BUTTON_BLU);
  B_BLK = digitalRead(BUTTON_BLK);

  // Check if buttons are pressed
  update_button(B_RED, &S_RED, &D_RED, &L_RED, LED_RED);
  update_button(B_GRN, &S_GRN, &D_GRN, &L_GRN, LED_GRN);
  update_button(B_BLU, &S_BLU, &D_BLU, &L_BLU, LED_BLU);
  update_blk();

  // Read incoming Bluetooth message and establish connection
  if (SerialBT.available()) {
    connect_status = connectBT();
    if(connect_status){
      setMessage(disp_conn);
    } else {
      setMessage(disp_disc);
    }
    delay(1000);
  }


  // Receive incoming Serial Message and Establish connection
  if (Serial.available() > 0) {
    ser_buffer[ser_counter] = Serial.read();
    if(ser_counter == 0 && ser_buffer[0] != '<'){
      ser_counter = 0;
    } else {
      ser_counter++;
    }
    
  }

  if(ser_counter == 9 && ser_buffer[8] == '>') {
      setMessage(disp_ser);
      delay(500);
      if(!ser_conn){
        if(checkConnect(ser_buffer)){
          Serial.print("<CONNECT>\r\n");
          ser_conn = true;
          setMessage(disp_conn);
        }  else {
          setMessage(disp_error);
        }
      } else { 
        if(checkDisconn(ser_buffer)){
          Serial.print("<DISCONN>\r\n");
          ser_conn = false;
          setMessage(disp_disc);
        } else {
          setMessage(disp_error);
        }
      }
      ser_counter = 0;
      delay(500);
    } else {
      ser_counter == 0;
    }

  // Read TCS3471 Data
  readColorValues();
}

// Write Status Messages to 7-Segment Display
void setMessage(byte* message){
  int digit = 8;
  for(int i = 0; i < 8; i++) {
    setDisplay(digit - i, message[i]);
  }
}

// Toggle LED Buttons
void update_button(int button, int *etat, long *bounce, int *led_etat, int led_pin) {
  if( (millis() - *bounce) > D_DELAY) {
    if(button == HIGH && *etat == 0) {
      if(*led_etat == 0) {
        *led_etat = 1;
      } else {
        *led_etat = 0;
      }
      *etat = 1;
      digitalWrite(led_pin, *led_etat);
      *bounce = millis();
    }
    else if(button == LOW && *etat == 1){
      *etat = 0;
      *bounce = millis();
    }
  }
}

// Toggle Black button
void update_blk() {
  if(B_BLK == HIGH && S_BLK == 0) {
    S_BLK = 1;
    L_RED = 0;
    L_GRN = 0;
    L_BLU = 0;
    digitalWrite(LED_RED, L_RED);
    digitalWrite(LED_GRN, L_GRN);
    digitalWrite(LED_BLU, L_BLU);
  }
  else if(B_BLK == LOW && S_BLK == 1){
    S_BLK = 0;
  }
}

// Read values from TCS3471
void readColorValues(){
  if( (millis() - D_CLICK) > DELAY_CLICK) {
    if(TCS3471.rgbcValid()) {
      CLR_VAL = TCS3471.readCData() >> 8;
      RED_VAL = TCS3471.readRData() >> 8;
      GRN_VAL = TCS3471.readGData() >> 8;
      BLU_VAL = TCS3471.readBData() >> 8;
      if(ser_conn == true){
        updateSerial();
      }
      updateDisplay();
      updateBlueTooth();
      D_CLICK = millis();
    }
  }
}

// Write TCS3471 Values to Serial
void updateSerial() {
  if(CLR_VAL < 16) {
    Serial.print("0");
  }
  Serial.print(CLR_VAL, HEX);
  Serial.print(",");
  if(RED_VAL < 16) {
    Serial.print("0");
  }
  Serial.print(RED_VAL, HEX);
  Serial.print(",");
  if(GRN_VAL < 16) {
    Serial.print("0");
  }
  Serial.print(GRN_VAL, HEX);
  Serial.print(",");
  if(BLU_VAL < 16) {
    Serial.print("0");
  }
  Serial.print(BLU_VAL, HEX);
  Serial.print("\r\n");
}

// Write TCS3471 Values to the 7 Segment Display
void updateDisplay() {
  setDisplay(0x08, disp_char[CLR_VAL >> 4]);
  setDisplay(0x07, disp_char[lowHex(CLR_VAL)] + 128);
  setDisplay(0x06, disp_char[RED_VAL >> 4]);
  setDisplay(0x05, disp_char[lowHex(RED_VAL)] + 128);
  setDisplay(0x04, disp_char[GRN_VAL >> 4]);
  setDisplay(0x03, disp_char[lowHex(GRN_VAL)] + 128);
  setDisplay(0x02, disp_char[BLU_VAL >> 4]);
  setDisplay(0x01, disp_char[lowHex(BLU_VAL)] + 128);
}

// if connected write to bluetooth
void updateBlueTooth() {
  if(connect_status){
    tcs[5] = hex_vals[CLR_VAL >> 4];
    tcs[6] = hex_vals[lowHex(CLR_VAL)];
    tcs[7] = hex_vals[RED_VAL >> 4];
    tcs[8] = hex_vals[lowHex(RED_VAL)];
    tcs[9] = hex_vals[GRN_VAL >> 4];
    tcs[10] = hex_vals[lowHex(GRN_VAL)];
    tcs[11] = hex_vals[BLU_VAL >> 4];
    tcs[12] = hex_vals[lowHex(BLU_VAL)];
    writeBT(tcs);
  }
}

// Obtain Least significant hex value
int lowHex(uint8_t value) {
  return value % 16;
}

// TCS2471 chip attached to Arduino's two wire bus
void i2cWrite(byte address, byte count, byte* buffer) {
    Wire.beginTransmission(address);
    byte i;
    for (i = 0; i < count; i++) {
#if ARDUINO >= 100
        Wire.write(buffer[i]);
#else
        Wire.send(buffer[i]);
#endif
    }
    Wire.endTransmission();
}

void i2cRead(byte address, byte count, byte* buffer) {
    Wire.requestFrom(address, count);
    byte i;
    for (i = 0; i < count; i++) {
#if ARDUINO >= 100
        buffer[i] = Wire.read();
#else
        buffer[i] = Wire.receive();
#endif
    }
}

// Write data to bluetooth
void writeBT(char* msg){
    for(int i = 0; i < 13; i++){
      SerialBT.write(msg[i]);
    }
    SerialBT.write('\r');
    SerialBT.write('\n');
}

// Initiate bluetooth connection
bool connectBT(){
    bool curr = connect_status;
    char checkBuff[9];
    int i = 0;
    while(SerialBT.available()){
      bufferBT[i] = SerialBT.read();
      i += 1;
    }
    for(i = 0; i < 9; i++){
      checkBuff[i] = bufferBT[i];
    }
    //Serial.println("buffer: ");
    //Serial.println(bufferBT);
    //If valid connect message send confirmation
    if(checkConnect(checkBuff)) {
      //If already connected
      if(connect_status) {
        writeBT(conDeja);
      //Else confirm connection
      } else {
        writeBT(conn);
      }
      return true;
    }
    
    //Else if it is a disconnect message
    else if(checkDisconn(checkBuff)) {
      //If already disconnected
      if(!connect_status) {
        writeBT(disDeja);
      } else {
        writeBT(disc);
      }
      return false;
    } else {
      //Else send error message
      writeBT(erro);
      return curr;
    }
}

// Check if message is a valid connection message
bool checkConnect(char* buf) {
  char good[9] = { '<', 'C', 'O', 'N', 'N', 'E', 'C', 'T', '>' };
  for(int i = 0; i < 9; i++) {
      if(buf[i] != good[i]) {
        return false;
      }
  }
  return true;
}

// Check if message is a valie disconnect message
bool checkDisconn(char* buf) {
  char good[9] = { '<', 'D', 'I', 'S', 'C', 'O', 'N', 'N', '>' };
  for(int i = 0; i < 9; i++) {
      if(buf[i] != good[i]) {
        return false;
      }
  }
  return true;
}
