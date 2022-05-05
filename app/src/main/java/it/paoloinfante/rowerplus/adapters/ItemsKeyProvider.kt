package it.paoloinfante.rowerplus.adapters

import androidx.recyclerview.selection.ItemKeyProvider

class ItemsKeyProvider(private val adapter: AllWorkoutsRecyclerAdapter) : ItemKeyProvider<Long>(
    SCOPE_CACHED
) {
    override fun getKey(position: Int): Long? {
        return adapter.workouts[position].workout.id?.toLong()
    }

    override fun getPosition(key: Long): Int {
        return adapter.workouts.indexOfFirst { it.workout.id?.toLong() == key }
    }
}