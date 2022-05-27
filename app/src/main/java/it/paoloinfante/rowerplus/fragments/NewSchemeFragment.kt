package it.paoloinfante.rowerplus.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import it.paoloinfante.rowerplus.R

class NewSchemeFragment : Fragment(R.layout.fragment_new_scheme) {

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
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.new_scheme, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_scheme -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}