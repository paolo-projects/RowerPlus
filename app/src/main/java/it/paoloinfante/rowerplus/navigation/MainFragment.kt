package it.paoloinfante.rowerplus.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.WorkoutDataViewsAdapter
import it.paoloinfante.rowerplus.utils.DepthPageTransformer
import java.lang.Integer.max
import kotlin.math.min

class MainFragment: Fragment(R.layout.fragment_main_content) {
    private lateinit var viewPager: ViewPager2
    private lateinit var fragmentAdapter: WorkoutDataViewsAdapter
    private lateinit var rightArrow: ImageView
    private lateinit var leftArrow: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = view.findViewById(R.id.pager)
        fragmentAdapter = WorkoutDataViewsAdapter(requireContext(), childFragmentManager, lifecycle)
        viewPager.adapter = fragmentAdapter
        viewPager.setPageTransformer(DepthPageTransformer())

        rightArrow = view.findViewById(R.id.rightArrow)
        leftArrow = view.findViewById(R.id.leftArrow)

        rightArrow.setOnClickListener {
            viewPager.setCurrentItem(min(viewPager.currentItem + 1, fragmentAdapter.itemCount - 1), true)
        }
        leftArrow.setOnClickListener {
            viewPager.setCurrentItem(max(viewPager.currentItem - 1, 0), true)
        }
    }
}