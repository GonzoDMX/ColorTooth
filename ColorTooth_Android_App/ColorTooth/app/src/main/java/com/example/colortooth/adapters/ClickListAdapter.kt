package com.example.colortooth.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.colortooth.MainActivity
import com.example.colortooth.R
import com.example.colortooth.connexion
import com.example.colortooth.data.ClickData


class ClickListAdapter(activity: MainActivity) :
        ListAdapter<ClickData, ClickListAdapter.ClickViewHolder>(STEP_COMPARATOR) {


    private var act = activity
    val selColor = ContextCompat.getColor(act, R.color.select_gray)
    var currentHolder: ClickViewHolder? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClickViewHolder {

        val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_item, parent, false)
        val holder = ClickViewHolder(v)
        setClickListeners(holder)
        return holder
    }

    override fun onBindViewHolder(holder: ClickViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    private fun setClickListeners(holder: ClickViewHolder){
        holder.itemView.setOnClickListener {
            if(!connexion) {
                act.updateColorView(holder.clickData!!)
                if (currentHolder != null) {
                    currentHolder!!.itemView.setBackgroundColor(Color.WHITE)
                }
                currentHolder = holder
                holder.itemView.setBackgroundColor(selColor)
            }
        }
    }

    class ClickViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var clickData: ClickData? = null
        private val stepItem: TextView = itemView.findViewById(R.id.text_step)
        private val nameItem: TextView = itemView.findViewById(R.id.text_name)
        private val valueItem: TextView = itemView.findViewById(R.id.text_value)
        private val timeItem: TextView = itemView.findViewById(R.id.text_time)

        @SuppressLint("SetTextI18nval", "SetTextI18n")
        fun bind(data: ClickData) {
            clickData = data
            stepItem.text = data.step.toString()
            nameItem.text = data.name
            valueItem.text = "( " + data.alpha.toString() + ", " +
                    data.red.toString() + ", " + data.green.toString() + ", " +
                    data.blue.toString() + " )"
            timeItem.text = data.time
        }

    }

    companion object {
        private val STEP_COMPARATOR = object : DiffUtil.ItemCallback<ClickData>() {
            override fun areItemsTheSame(oldItem: ClickData, newItem: ClickData): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: ClickData, newItem: ClickData): Boolean {
                return oldItem.step == newItem.step
            }
        }
    }
}