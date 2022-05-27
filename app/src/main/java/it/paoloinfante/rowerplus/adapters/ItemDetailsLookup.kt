package it.paoloinfante.rowerplus.adapters

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class ItemDetailsLookup<T : ItemDetailsRecyclerAdapter.ItemDetailsViewHolder>(private val recyclerView: RecyclerView) :
    ItemDetailsLookup<Long>() {
    override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
        val view = recyclerView.findChildViewUnder(e.x, e.y)
        return if (view != null) {
            (recyclerView.getChildViewHolder(view) as T).getItem()
        } else null
    }
}