package it.paoloinfante.rowerplus.navigation

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.AllWorkoutsRecyclerAdapter
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllWorkoutsFragment: Fragment(R.layout.fragment_all_workouts) {
    private lateinit var workoutsRecyclerView: RecyclerView
    private lateinit var workoutsRecyclerAdapter: AllWorkoutsRecyclerAdapter

    private val workoutsDataViewViewModel by activityViewModels<WorkoutDataViewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        workoutsRecyclerAdapter = AllWorkoutsRecyclerAdapter(requireContext(), listOf())

        workoutsRecyclerView = view.findViewById(R.id.workoutsRecyclerView)
        workoutsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        workoutsRecyclerView.adapter = workoutsRecyclerAdapter

        lifecycleScope.launch {
            workoutsDataViewViewModel.getAllWorkoutsWithStatuses().collectLatest {
                workoutsRecyclerAdapter.setWorkouts(it)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.workouts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.delete_all -> {
                askDeleteAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun askDeleteAll() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.workouts_delete_all_warning))
            .setNegativeButton("No") { _, _ ->

            }
            .setPositiveButton("Yes"
            ) { _, _ ->
                lifecycleScope.launch {
                    workoutsDataViewViewModel.deleteAll()
                }
            }.create().show()
    }
}