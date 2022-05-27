package it.paoloinfante.rowerplus.navigation

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.AllWorkoutsRecyclerAdapter
import it.paoloinfante.rowerplus.adapters.ItemDetailsLookup
import it.paoloinfante.rowerplus.adapters.ItemsKeyProvider
import it.paoloinfante.rowerplus.fragments.viewmodels.WorkoutDataViewViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AllWorkoutsFragment : Fragment(R.layout.fragment_all_workouts), AllWorkoutsRecyclerAdapter.Actions {
    companion object {
        private const val TAG = "AllWorkoutsFragment"
    }

    private lateinit var rootView: ConstraintLayout
    private lateinit var workoutsRecyclerView: RecyclerView
    private lateinit var workoutsRecyclerAdapter: AllWorkoutsRecyclerAdapter
    private lateinit var selectionTracker: SelectionTracker<Long>
    private var actionMode: ActionMode? = null
    private val actionModeCallback = WorkoutsActionModeCallback()

    private val workoutsDataViewViewModel by activityViewModels<WorkoutDataViewViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView = view.findViewById(R.id.rootView)

        workoutsRecyclerAdapter = AllWorkoutsRecyclerAdapter(requireContext(), listOf(), this)

        workoutsRecyclerView = view.findViewById(R.id.workoutsRecyclerView)
        workoutsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        workoutsRecyclerView.adapter = workoutsRecyclerAdapter

        selectionTracker = SelectionTracker.Builder(
            "all_workouts_workout_selection",
            workoutsRecyclerView,
            ItemsKeyProvider(workoutsRecyclerAdapter),
            ItemDetailsLookup<AllWorkoutsRecyclerAdapter.ViewHolder>(workoutsRecyclerView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

        selectionTracker.addObserver(selectionObserver)

        workoutsRecyclerAdapter.tracker = selectionTracker

        lifecycleScope.launch {
            workoutsDataViewViewModel.getAllWorkoutsWithStatuses().collectLatest {
                workoutsRecyclerAdapter.setWorkouts(it)
            }
        }
    }

    private val selectionObserver = object : SelectionTracker.SelectionObserver<Long>() {
        override fun onSelectionChanged() {
            super.onSelectionChanged()

            if (selectionTracker.selection.size() > 0) {
                if (actionMode == null) {
                    rootView.startActionMode(actionModeCallback, ActionMode.TYPE_PRIMARY)
                }

                actionMode?.subtitle = getString(
                    R.string.all_workouts_selection_format,
                    selectionTracker.selection.size()
                )
            } else {
                actionMode?.finish()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }
/*
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.workouts, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_all -> {
                askDeleteAll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun askDeleteAll() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.workouts_delete_all_warning)
            .setNegativeButton(R.string.message_no) { _, _ ->

            }
            .setPositiveButton(
                R.string.message_yes
            ) { _, _ ->
                lifecycleScope.launch {
                    actionMode?.finish()
                    workoutsDataViewViewModel.deleteAll()
                }
            }.create().show()
    }

    private fun askDeleteSelection() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.workouts_delete_selected_warning, selectionTracker.selection.size()))
            .setNegativeButton(R.string.message_no) { _, _ ->

            }
            .setPositiveButton(
                R.string.message_yes
            ) { _, _ ->
                lifecycleScope.launch {
                    workoutsDataViewViewModel.deleteWorkoutsById(selectionTracker.selection.map { it.toInt() }.toList())
                    actionMode?.finish()
                }
            }.create().show()
    }

    override fun onItemClick(id: Int?) {
        if(id != null) {
            val action = AllWorkoutsFragmentDirections.actionAllWorkoutsFragmentToSingleWorkoutFragment(id)
            findNavController().navigate(action)
        }
    }

    inner class WorkoutsActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            actionMode = mode
            mode.menuInflater.inflate(R.menu.workouts, menu)
            mode.title = getString(R.string.all_workouts_actionmode_title)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.delete_all -> {
                    askDeleteAll()
                }
                R.id.delete_some -> {
                    askDeleteSelection()
                }
                android.R.id.home -> {
                    Log.d(TAG, "onActionItemClicked: Home pressed")
                }
            }

            return true
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            selectionTracker.clearSelection()
            actionMode = null
        }
    }
}