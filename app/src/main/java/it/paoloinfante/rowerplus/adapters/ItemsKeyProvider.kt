package it.paoloinfante.rowerplus.adapters

import androidx.recyclerview.selection.ItemKeyProvider

class ItemsKeyProvider<T : ItemDetailsRecyclerAdapter.ItemDetailsViewHolder>(private val adapter: ItemDetailsRecyclerAdapter<T>) :
    ItemKeyProvider<Long>(
        SCOPE_CACHED
    ) {
    override fun getKey(position: Int): Long? {
        return adapter.getItemKey(position)
    }

    override fun getPosition(key: Long): Int {
        return adapter.getItemKeyPosition(key)
    }
}