package it.paoloinfante.rowerplus.adapters

import android.view.View
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

abstract class ItemDetailsRecyclerAdapter<T : ItemDetailsRecyclerAdapter.ItemDetailsViewHolder> :
    RecyclerView.Adapter<T>() {

    abstract fun getItemKey(position: Int): Long?
    abstract fun getItemKeyPosition(key: Long): Int

    abstract class ItemDetailsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun getItem(): ItemDetailsLookup.ItemDetails<Long>
    }
}