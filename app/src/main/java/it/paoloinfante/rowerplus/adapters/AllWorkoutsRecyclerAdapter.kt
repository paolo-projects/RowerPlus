package it.paoloinfante.rowerplus.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses
import it.paoloinfante.rowerplus.utils.DateDisplay
import kotlin.math.floor

class AllWorkoutsRecyclerAdapter(
    private val mContext: Context,
    workouts: List<WorkoutWithStatuses>,
    private val actionListener: Actions
) : ItemDetailsRecyclerAdapter<AllWorkoutsRecyclerAdapter.ViewHolder>() {
    val workouts = ArrayList<WorkoutWithStatuses>()
    var tracker: SelectionTracker<Long>? = null
    private var selectedColor: Int = 0
    private var normalColor: Int = 0

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

        this.workouts.addAll(workouts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =
            mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val rootView = layoutInflater.inflate(R.layout.itemview_workout, parent, false)

        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.bind(workout)
    }

    override fun getItemCount(): Int {
        return this.workouts.size
    }

    fun setWorkouts(workouts: List<WorkoutWithStatuses>) {
        val previousCount = this.workouts.size

        this.workouts.clear()
        this.workouts.addAll(workouts)

        notifyItemRangeRemoved(0, previousCount)
        notifyItemRangeInserted(0, this.workouts.size)
    }

    override fun getItemKey(position: Int): Long? {
        return workouts[position].workout.id?.toLong()
    }

    override fun getItemKeyPosition(key: Long): Int {
        return workouts.indexOfFirst { it.workout.id?.toLong() == key }
    }

    inner class ViewHolder(itemView: View) :
        ItemDetailsRecyclerAdapter.ItemDetailsViewHolder(itemView) {
        override fun getItem() = object : ItemDetailsLookup.ItemDetails<Long>() {
            override fun getPosition(): Int {
                return adapterPosition
            }

            override fun getSelectionKey(): Long? {
                return workouts[adapterPosition].workout.id?.toLong()
            }
        }

        fun bind(item: WorkoutWithStatuses) {
            workoutName.text = DateDisplay.dateToTimeAgo(mContext, item.workout.time).lowercase()

            if(item.workoutStatuses.isNotEmpty()) {
                val time = item.workoutStatuses.maxOf { it.timeElapsed }
                workoutTime.text = mContext.getString(
                    R.string.timer_format,
                    floor(time / 60f).toInt(), (time % 60)
                )
                workoutDistance.text = mContext.getString(
                    R.string.distance_format,
                    item.workoutStatuses.maxOf { it.distance })
                workoutCalories.text = mContext.getString(
                    R.string.calories_format,
                    item.workoutStatuses.maxOf { it.calories })
            } else {
                workoutTime.text = "-"
                workoutDistance.text = "-"
                workoutCalories.text = "-"
            }

            tracker?.let {
                if (it.isSelected(item.workout.id?.toLong())) {
                    itemView.setBackgroundColor(selectedColor)
                } else {
                    itemView.setBackgroundColor(normalColor)
                }
            }

            itemView.setOnClickListener {
                if (tracker?.selection == null || tracker?.selection?.size() == 0) {
                    actionListener.onItemClick(workouts[adapterPosition].workout.id)
                }
            }
        }

        val workoutName: TextView = itemView.findViewById(R.id.workoutName)
        val workoutTime: TextView = itemView.findViewById(R.id.workoutTime)
        val workoutDistance: TextView = itemView.findViewById(R.id.workoutDistance)
        val workoutCalories: TextView = itemView.findViewById(R.id.workoutCalories)
    }
}