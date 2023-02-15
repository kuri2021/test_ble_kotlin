package com.example.test_ble_kotlin

import android.bluetooth.BluetoothDevice
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(private val myDataset: ArrayList<BluetoothDevice>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    var mListener : OnItemClickListener? = null

    interface OnItemClickListener{
        fun onClick(view: View, position: Int)
    }


    class MyViewHolder(val linearView: LinearLayout) : RecyclerView.ViewHolder(linearView)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): RecyclerViewAdapter.MyViewHolder {
        // create a new view
        val linearView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_item, parent, false) as LinearLayout

        return MyViewHolder(linearView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressWarnings("MissingPermission")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemName:TextView = holder.linearView.findViewById(R.id.item_name)
        val itemAddress:TextView = holder.linearView.findViewById(R.id.item_address)
        itemName.text = myDataset[position].name
        itemAddress.text = myDataset[position].address
        if(mListener!=null){
            holder?.itemView?.setOnClickListener{v->
                mListener?.onClick(v, position)
            }
        }
    }

    override fun getItemCount() = myDataset.size
}
private fun Handler.postDelayed(function: () -> Unit?, scanPeriod: Int) {

}