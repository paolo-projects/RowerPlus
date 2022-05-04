package it.paoloinfante.rowerplus.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.database.models.WorkoutWithStatuses

class AllWorkoutsRecyclerAdapter(private val mContext: Context, workouts: List<WorkoutWithStatuses>): RecyclerView.Adapter<AllWorkoutsRecyclerAdapter.ViewHolder>() {
    private val workouts = ArrayList<WorkoutWithStatuses>()

    init {
        this.workouts.addAll(workouts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val rootView = layoutInflater.inflate(R.layout.itemview_workout, parent, false)

        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.workoutName.text = workout.workout.name
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

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val workoutName = itemView.findViewById<TextView>(R.id.workoutName)
    }
}