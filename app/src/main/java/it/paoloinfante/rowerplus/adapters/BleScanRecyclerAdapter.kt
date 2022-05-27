package it.paoloinfante.rowerplus.adapters

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.paoloinfante.rowerplus.R

class BleScanRecyclerAdapter(private val mContext: Context, private val listener: Actions) :
    RecyclerView.Adapter<BleScanRecyclerAdapter.ViewHolder>() {
    private val scanResults = ArrayList<ScanResult>()

    interface Actions {
        fun onDeviceClick(device: BluetoothDevice)
    }

    fun setData(scanResults: List<ScanResult>) {
        val previousSize = scanResults.size
        this.scanResults.clear()
        this.scanResults.addAll(scanResults)
        notifyItemRangeRemoved(0, previousSize)
        notifyItemRangeInserted(0, scanResults.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ViewHolder(inflater.inflate(R.layout.itemview_ble_scan, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(scanResults[position])
    }

    override fun getItemCount(): Int {
        return scanResults.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(scanResult: ScanResult) {
            uuidTextView.text = scanResult.device.name

            itemView.setOnClickListener {
                listener.onDeviceClick(scanResults[adapterPosition].device)
            }
        }

        val uuidTextView = itemView.findViewById<TextView>(R.id.uuidTextView)
    }

}