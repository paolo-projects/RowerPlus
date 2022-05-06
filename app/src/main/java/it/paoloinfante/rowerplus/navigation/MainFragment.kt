package it.paoloinfante.rowerplus.navigation

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.WorkoutDataViewsAdapter
import it.paoloinfante.rowerplus.database.models.WorkoutStatus
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import it.paoloinfante.rowerplus.ui.PulseAnimation
import it.paoloinfante.rowerplus.utils.DepthPageTransformer
import it.paoloinfante.rowerplus.viewmodels.UsbConnectionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.Integer.max
import kotlin.math.min

class MainFragment : Fragment(R.layout.fragment_main_content) {
    private lateinit var viewPager: ViewPager2
    private lateinit var fragmentAdapter: WorkoutDataViewsAdapter
    private lateinit var rightArrow: ImageView
    private lateinit var leftArrow: ImageView

    private lateinit var mainViewContainer: ConstraintLayout
    private lateinit var pulseAnimation: PulseAnimation

    private val workoutDataViewViewModel by activityViewModels<WorkoutDataViewViewModel>()

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

        lifecycleScope.launch {
            workoutDataViewViewModel.getLastWorkoutStatus().collect(lastWorkoutStatusCollector)
        }
    }

    private val lastWorkoutStatusCollector = FlowCollector<WorkoutStatus?> {
        if (it != null) {
            pulseAnimation.animate(mainViewContainer)
        }
    }
}