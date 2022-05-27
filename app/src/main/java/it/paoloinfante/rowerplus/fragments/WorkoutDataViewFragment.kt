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
import it.paoloinfante.rowerplus.views.parameters.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

@AndroidEntryPoint
class WorkoutDataViewFragment(
    private val mainDataView: KClass<ViewParameter>
) : Fragment(R.layout.fragment_workoutdata_view) {
    companion object {
        const val TAG = "WorkoutDataViewFragment"
    }

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

    private lateinit var parametersClasses: List<KClass<*>>
    private lateinit var parameters: ArrayList<ViewParameter>

    private lateinit var paramViews: List<TextView>
    private lateinit var paramTitleViews: List<TextView>

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

        paramViews = listOf(
            view2TextView,
            view3TextView,
            view4TextView,
            view5TextView,
            view6TextView,
            view7TextView
        )
        paramTitleViews = listOf(
            view2TitleTextView,
            view3TitleTextView,
            view4TitleTextView,
            view5TitleTextView,
            view6TitleTextView,
            view7TitleTextView
        )

        parametersClasses = listOf(
            TimeParameter::class,
            DistanceParameter::class,
            CaloriesParameter::class,
            SpmParameter::class,
            TimeFor500Parameter::class,
            TotalStrokesParameter::class,
            BpmParameter::class,
        ).filter { it != mainDataView }

        parameters = ArrayList()
        parameters.add(
            mainDataView.constructors.first()
                .call(requireContext(), mainTextView, descriptionTextView)
        )
        parametersClasses.forEachIndexed { index, paramClass ->
            parameters.add(
                paramClass.constructors.first().call(
                    requireContext(),
                    paramViews[index],
                    paramTitleViews[index]
                ) as ViewParameter
            )
        }

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
                    paramViews.forEach { view -> view.isEnabled = it.connected }
                }
            }
            async {
                bleViewModel.bleMeasurements.collect {
                    onNewBpm(it)
                }
            }
        }
    }

    private fun onNewBpm(bpm: Int) {
        parameters.firstOrNull { it is BpmParameter }
            ?.update(null, bpm)
    }

    private fun onNewStatus(workoutStatus: TimerData) {
        Log.d(TAG, "onNewStatus: $workoutStatus")

        parameters.filter { it !is BpmParameter }.forEach {
            it.update(workoutStatus, null)
        }
    }
}