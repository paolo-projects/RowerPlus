package it.paoloinfante.rowerplus.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.WorkoutDataInterface
import it.paoloinfante.rowerplus.fragments.WorkoutDataViewFragment

class WorkoutDataViewsAdapter(
    private val mContext: Context,
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private val workoutDataViews = mContext.resources.getStringArray(R.array.workout_variables)

    override fun getItemCount(): Int {
        return workoutDataViews.size
    }

    override fun createFragment(position: Int): Fragment {
        return WorkoutDataViewFragment(workoutDataViews[position])
    }
}