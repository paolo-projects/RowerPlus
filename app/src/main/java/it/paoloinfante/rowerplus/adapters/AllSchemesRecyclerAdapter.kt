package it.paoloinfante.rowerplus.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.SchemeWithStepsAndVariables

class AllSchemesRecyclerAdapter(
    private val mContext: Context,
    schemes: List<SchemeWithStepsAndVariables>,
    private val actionsListener: Actions
) : ItemDetailsRecyclerAdapter<AllSchemesRecyclerAdapter.ViewHolder>() {
    private val schemes = ArrayList<SchemeWithStepsAndVariables>()
    private var selectedColor: Int = 0
    private var normalColor: Int = 0

    var tracker: SelectionTracker<Long>? = null

    interface Actions {
        fun onItemClick(id: Int?)
    }

    init {
        val selectedColorAttr = intArrayOf(R.attr.selectionOverlayColor, R.attr.listItemBackground)
        val arr =
            mContext.theme.obtainStyledAttributes(selectedColorAttr)
        selectedColor = arr.getColor(0, mContext.getColor(R.color.moreLucidColor))
        normalColor = arr.getColor(1 as Int, mContext.getColor(R.color.lucidColor))
        arr.recycle()

        this.schemes.addAll(schemes)
    }

    fun setData(data: List<SchemeWithStepsAndVariables>) {
        val prevSize = schemes.size
        schemes.clear()
        schemes.addAll(data)

        notifyItemRangeRemoved(0, prevSize)
        notifyItemRangeInserted(0, schemes.size)
    }

    override fun getItemKey(position: Int): Long? {
        return schemes[position].scheme.id?.toLong()
    }

    override fun getItemKeyPosition(key: Long): Int {
        return schemes.indexOfFirst { it.scheme.id?.toLong() == key }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        return ViewHolder(layoutInflater.inflate(R.layout.itemview_scheme, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(schemes[position])
    }

    override fun getItemCount(): Int {
        return schemes.size
    }

    inner class ViewHolder(itemView: View) :
        ItemDetailsRecyclerAdapter.ItemDetailsViewHolder(itemView) {
        override fun getItem() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int {
                return adapterPosition
            }

            override fun getSelectionKey(): Long? {
                return schemes[adapterPosition].scheme.id?.toLong()
            }
        }

        fun bind(scheme: SchemeWithStepsAndVariables) {
            schemeName.text = scheme.scheme.name
            val durationSecs = scheme.schemeSteps.fold(0L) { acc, step ->
                acc + step.schemeStep.duration.seconds
            }
            schemeDuration.text =
                mContext.getString(R.string.timer_format, durationSecs / 60, durationSecs % 60)

            tracker?.let {
                if (it.isSelected(scheme.scheme.id?.toLong())) {
                    itemView.setBackgroundColor(selectedColor)
                } else {
                    itemView.setBackgroundColor(normalColor)
                }
            }

            itemView.setOnClickListener {
                if (tracker?.selection == null || tracker?.selection?.size() == 0) {
                    actionsListener.onItemClick(schemes[adapterPosition].scheme.id)
                }
            }
        }

        val schemeName = itemView.findViewById<TextView>(R.id.schemeName)
        val schemeDuration = itemView.findViewById<TextView>(R.id.schemeDuration)
    }
}