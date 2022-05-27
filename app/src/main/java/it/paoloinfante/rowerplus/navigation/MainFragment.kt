package it.paoloinfante.rowerplus.navigation

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.WorkoutDataViewsAdapter
import it.paoloinfante.rowerplus.ble.HeartRateBLEManager
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.dialogs.BleScanDialog
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import it.paoloinfante.rowerplus.ui.PulseAnimation
import it.paoloinfante.rowerplus.utils.DepthPageTransformer
import it.paoloinfante.rowerplus.viewmodels.BleViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import java.lang.Integer.max
import kotlin.math.min

class MainFragment : Fragment(R.layout.fragment_main_content), BleViewModel.ScanResultListener,
    BleScanDialog.Actions {
    private lateinit var viewPager: ViewPager2
    private lateinit var fragmentAdapter: WorkoutDataViewsAdapter
    private lateinit var rightArrow: ImageView
    private lateinit var leftArrow: ImageView

    private lateinit var mainViewContainer: ConstraintLayout
    private lateinit var pulseAnimation: PulseAnimation

    private var bleScanDialog: BleScanDialog? = null

    private val workoutDataViewViewModel by activityViewModels<WorkoutDataViewViewModel>()
    private val bleViewModel by activityViewModels<BleViewModel>()

    private lateinit var permissionRequestLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewContainer = view.findViewById(R.id.mainViewContainer)

        viewPager = view.findViewById(R.id.pager)
        fragmentAdapter = WorkoutDataViewsAdapter(requireContext(), childFragmentManager, lifecycle)
        viewPager.adapter = fragmentAdapter
        viewPager.setPageTransformer(DepthPageTransformer())

        rightArrow = view.findViewById(R.id.rightArrow)
        leftArrow = view.findViewById(R.id.leftArrow)

        rightArrow.setOnClickListener {
            viewPager.setCurrentItem(
                min(viewPager.currentItem + 1, fragmentAdapter.itemCount - 1),
                true
            )
        }
        leftArrow.setOnClickListener {
            viewPager.setCurrentItem(max(viewPager.currentItem - 1, 0), true)
        }

        pulseAnimation = PulseAnimation(
            Color.TRANSPARENT,
            ContextCompat.getColor(requireContext(), R.color.redTranslucent),
            resources.getInteger(R.integer.pulse_animation_duration_ms)
        )

        bleViewModel.attach(requireContext(), this)

        lifecycleScope.launch {
            async {
                workoutDataViewViewModel.getLastWorkoutStatus().collect(lastWorkoutStatusCollector)
            }
            async {
                bleViewModel.bleStatus.collect(bleStatusCollector)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        permissionRequestLauncher = requireActivity().registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            permissionResultCallback
        )
    }

    private val lastWorkoutStatusCollector = FlowCollector<WorkoutStatus?> {
        if (it != null) {
            pulseAnimation.animate(mainViewContainer)
        }
    }

    private val bleStatusCollector = FlowCollector<Boolean> {
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.rower_data, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun connectToHr() {
        bleViewModel.requestBleScan()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        (menu.findItem(R.id.connect_hr) as MenuItem).isEnabled = !bleViewModel.bleStatus.value
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.connect_hr -> {
                connectToHr()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDeviceSelected(device: BluetoothDevice) {
        bleViewModel.requestDeviceConnect(device)
    }

    override fun onScanResult(result: List<ScanResult>) {
        bleScanDialog?.setScanResults(result)
    }

    override fun onScanStarted() {
        bleScanDialog?.dismiss()
        bleScanDialog = BleScanDialog(this)
        bleScanDialog!!.show(childFragmentManager, null)
    }

    override fun onScanEnded() {
        bleScanDialog?.stopProgress()
    }

    override fun onPermissionsRequired() {
        permissionRequestLauncher.launch(HeartRateBLEManager.PERMISSIONS)
    }

    private val permissionResultCallback = ActivityResultCallback<Map<String, Boolean>> {
        if (it.all { granted ->
                granted.value
            }) {
            connectToHr()
        }
    }
}