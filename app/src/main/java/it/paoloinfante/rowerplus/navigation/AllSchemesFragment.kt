package it.paoloinfante.rowerplus.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.paoloinfante.rowerplus.R
import it.paoloinfante.rowerplus.adapters.AllSchemesRecyclerAdapter
import it.paoloinfante.rowerplus.database.models.SchemeWithStepsAndVariables
import it.paoloinfante.rowerplus.utils.GridSpacingItemDecoration
import it.paoloinfante.rowerplus.utils.YamlSchemeParser
import it.paoloinfante.rowerplus.viewmodels.AllSchemesViewModel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

class AllSchemesFragment : Fragment(R.layout.fragment_all_schemes),
    AllSchemesRecyclerAdapter.Actions {
    private lateinit var schemesRecyclerView: RecyclerView
    private lateinit var schemesRecyclerAdapter: AllSchemesRecyclerAdapter

    private val schemesViewModel by activityViewModels<AllSchemesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        schemesRecyclerView = view.findViewById(R.id.schemesRecyclerView)

        schemesRecyclerAdapter = AllSchemesRecyclerAdapter(requireContext(), emptyList(), this)
        schemesRecyclerView.layoutManager =
            GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)
        schemesRecyclerView.addItemDecoration(
            GridSpacingItemDecoration(
                3,
                resources.getDimensionPixelSize(R.dimen.schemesGridSpacing),
                false,
                0
            )
        )
        schemesRecyclerView.adapter = schemesRecyclerAdapter

        lifecycleScope.launch {
            schemesViewModel.getAllSchemes().collect(schemesCollector)
        }
    }

    private val schemesCollector = FlowCollector<List<SchemeWithStepsAndVariables>> {
        schemesRecyclerAdapter.setData(it)
    }

    override fun onItemClick(id: Int?) {

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.schemes, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.scheme_load_file -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/x-yaml"
                }
                openDocumentResultLauncher.launch(intent)
                true
            }
            /*
            R.id.scheme_add -> {
                findNavController().navigate(AllSchemesFragmentDirections.actionAllSchemesFragmentToNewSchemeFragment())
                true
            }*/
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun loadSchemeFromDocument(uri: Uri) {
        val schemeParser = YamlSchemeParser(uri.path!!)
        val scheme = schemeParser.parse()

        if (scheme != null) {
            lifecycleScope.launch {
                schemesViewModel.insertSchemeWithStepsAndVariables(scheme)
            }
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_configuration_file_not_supported),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val openDocumentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data.also { uri ->
                    if (uri != null) {
                        loadSchemeFromDocument(uri)
                    }
                }
            }
        }
}