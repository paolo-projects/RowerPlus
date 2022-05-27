package it.paoloinfante.rowerplus.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.models.TimerData
import it.paoloinfante.rowerplus.viewmodels.BleViewModel
import it.paoloinfante.rowerplus.viewmodels.UsbConnectionViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkoutDataViewFragment(
    private val dataView: String
) : Fragment(R.layout.fragment_workoutdata_view) {
    companion object {
        const val TAG = "WorkoutDataViewFragment"
    }

    private lateinit var dataViews: Array<String>
    private lateinit var remainingDataViews: Array<String>

    private lateinit var mainTextView: TextView
    private lateinit var descriptionTextView: TextView

    private lateinit var view2TextView: TextView
    private lateinit var view2TitleTextView: TextView

    private lateinit var view3TextView: TextView
    private lateinit var view3TitleTextView: TextView

    private lateinit var view4TextView: TextView
    private lateinit var view4TitleTextView: TextView

    private lateinit var view5TextView: TextView
    private lateinit var view5TitleTextView: TextView

    private lateinit var view6TextView: TextView
    private lateinit var view6TitleTextView: TextView

    private lateinit var view7TextView: TextView
    private lateinit var view7TitleTextView: TextView

    private val usbConnectionViewModel by activityViewModels<UsbConnectionViewModel>()
    private val bleViewModel by activityViewModels<BleViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainTextView = view.findViewById(R.id.mainTextView)
        descriptionTextView = view.findViewById(R.id.trainingInfoScreenDescription)
        view2TextView = view.findViewById(R.id.workoutData2)
        view2TitleTextView = view.findViewById(R.id.workoutDataTitle2)
        view3TextView = view.findViewById(R.id.workoutData3)
        view3TitleTextView = view.findViewById(R.id.workoutDataTitle3)
        view4TextView = view.findViewById(R.id.workoutData4)
        view4TitleTextView = view.findViewById(R.id.workoutDataTitle4)
        view5TextView = view.findViewById(R.id.workoutData5)
        view5TitleTextView = view.findViewById(R.id.workoutDataTitle5)
        view6TextView = view.findViewById(R.id.workoutData6)
        view6TitleTextView = view.findViewById(R.id.workoutDataTitle6)
        view7TextView = view.findViewById(R.id.workoutData7)
        view7TitleTextView = view.findViewById(R.id.workoutDataTitle7)

        dataViews = requireContext().resources.getStringArray(R.array.workout_variables)
        remainingDataViews = dataViews.filter { it != dataView }.toTypedArray()

        setTitles()

        lifecycleScope.launch {
            async {
                usbConnectionViewModel.timerDataEvents.collect {
                    onNewStatus(it.timerData)
                }
            }
            async {
                usbConnectionViewModel.connectionStatusEvents.collect {
                    descriptionTextView.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        if (it.connected) 0 else R.drawable.ic_baseline_power_off_24,
                        0
                    )

                    mainTextView.isEnabled = it.connected
                    view2TextView.isEnabled = it.connected
                    view3TextView.isEnabled = it.connected
                    view4TextView.isEnabled = it.connected
                    view5TextView.isEnabled = it.connected
                    view6TextView.isEnabled = it.connected
                    view7TextView.isEnabled = it.connected
                }
            }
            async {
                bleViewModel.bleMeasurements.collect {
                    onNewBpm(it)
                }
            }
        }
    }

    private fun setTitles() {
        descriptionTextView.text = dataView

        view2TitleTextView.text = remainingDataViews[0]
        view3TitleTextView.text = remainingDataViews[1]
        view4TitleTextView.text = remainingDataViews[2]
        view5TitleTextView.text = remainingDataViews[3]
        view6TitleTextView.text = remainingDataViews[4]
        view7TitleTextView.text = remainingDataViews[5]
    }

    private fun onNewBpm(bpm: Int) {
        if (dataView == dataViews[6]) {
            mainTextView.text = getString(R.string.bpm_format, bpm)
        } else {
            view7TextView.text = getString(R.string.bpm_format, bpm)
        }
    }

    private fun onNewStatus(workoutStatus: TimerData) {
        //TODO: Could as well remove redundancy here

        Log.d(TAG, "workoutLiveDataUpdate: $workoutStatus")

        when (dataView) {
            dataViews[0] -> {
                mainTextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                view2TextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                view3TextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                view4TextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                view5TextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                view6TextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
            dataViews[1] -> {
                view2TextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                mainTextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                view3TextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                view4TextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                view5TextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                view6TextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
            dataViews[2] -> {
                view2TextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                view3TextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                mainTextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                view4TextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                view5TextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                view6TextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
            dataViews[3] -> {
                view2TextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                view3TextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                view4TextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                mainTextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                view5TextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                view6TextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
            dataViews[4] -> {
                view2TextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                view3TextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                view4TextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                view5TextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                mainTextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                view6TextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
            dataViews[5] -> {
                view2TextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                view3TextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                view4TextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                view5TextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                view6TextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                mainTextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
            dataViews[6] -> {
                view2TextView.text =
                    getString(R.string.timer_format).format(
                        workoutStatus.timeElapsed / 60,
                        workoutStatus.timeElapsed % 60
                    )
                view3TextView.text =
                    getString(R.string.distance_format).format(workoutStatus.distance)
                view4TextView.text =
                    getString(R.string.calories_format).format(workoutStatus.calories)
                view5TextView.text =
                    getString(R.string.current_rpm_format).format(workoutStatus.currentRPM)
                view6TextView.text =
                    getString(R.string.time_for_500m_format).format(
                        workoutStatus.currentSecsFor500M.toInt() / 60,
                        workoutStatus.currentSecsFor500M.toInt() % 60
                    )
                view7TextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
        }
    }
}