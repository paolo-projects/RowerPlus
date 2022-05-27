package it.paoloinfante.rowerplus.dialogs

import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.BleScanRecyclerAdapter

class BleScanDialog(private val listener: Actions) : BottomSheetDialogFragment(),
    BleScanRecyclerAdapter.Actions {
    interface Actions {
        fun onDeviceSelected(device: BluetoothDevice)
    }

    private lateinit var bleScanRecyclerView: RecyclerView
    private lateinit var bleScanAdapter: BleScanRecyclerAdapter
    private lateinit var progress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_ble_scan, container, false)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = view.findViewById(R.id.progressBar)
        bleScanAdapter = BleScanRecyclerAdapter(requireContext(), this)
        bleScanRecyclerView = view.findViewById(R.id.bleScanRecyclerView)
        bleScanRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        bleScanRecyclerView.adapter = bleScanAdapter
    }

    fun setScanResults(scanResults: List<ScanResult>) {
        bleScanAdapter.setData(scanResults)
    }

    fun stopProgress() {
        progress.visibility = View.GONE
    }

    override fun onDeviceClick(device: BluetoothDevice) {
        listener.onDeviceSelected(device)
        dismiss()
    }
}