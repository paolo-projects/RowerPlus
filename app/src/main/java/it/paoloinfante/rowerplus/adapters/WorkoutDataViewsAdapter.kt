package it.paoloinfante.rowerplus.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.paoloinfante.rowerplus.fragments.WorkoutDataViewFragment
import it.paoloinfante.rowerplus.views.parameters.*
import kotlin.reflect.KClass

class WorkoutDataViewsAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val parameterViews = listOf<KClass<*>>(
        TimeParameter::class,
        DistanceParameter::class,
        CaloriesParameter::class,
        SpmParameter::class,
        TimeFor500Parameter::class,
        TotalStrokesParameter::class,
        BpmParameter::class
    )

    override fun getItemCount(): Int {
        return parameterViews.size
    }

    override fun createFragment(position: Int): Fragment {
        return WorkoutDataViewFragment(parameterViews[position] as KClass<ViewParameter>)
    }
}