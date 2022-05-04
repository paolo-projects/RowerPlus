package it.paoloinfante.rowerplus.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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

    private val workoutDataViewModel by activityViewModels<WorkoutDataViewViewModel>()

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

        dataViews = requireContext().resources.getStringArray(R.array.workout_variables)
        remainingDataViews = dataViews.filter { it != dataView }.toTypedArray()

        setTitles()

        lifecycleScope.launch {
            workoutDataViewModel.getLastStatus().collectLatest {
                onNewStatus(it)
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
    }

    private fun onNewStatus(workoutStatus: WorkoutStatus?) {
        Log.d(TAG, "workoutLiveDataUpdate: $workoutStatus")

        if(workoutStatus == null) {
            return
        }

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
                    getString(R.string.secs_for_500m_format).format(workoutStatus.currentSecsFor500M)
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
                    getString(R.string.secs_for_500m_format).format(workoutStatus.currentSecsFor500M)
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
                    getString(R.string.secs_for_500m_format).format(workoutStatus.currentSecsFor500M)
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
                    getString(R.string.secs_for_500m_format).format(workoutStatus.currentSecsFor500M)
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
                    getString(R.string.secs_for_500m_format).format(workoutStatus.currentSecsFor500M)
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
                    getString(R.string.secs_for_500m_format).format(workoutStatus.currentSecsFor500M)
                mainTextView.text =
                    getString(R.string.total_rows_format).format(workoutStatus.rowsCount)
            }
        }
    }
}