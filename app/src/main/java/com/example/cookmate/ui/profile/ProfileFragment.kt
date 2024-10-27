package com.example.cookmate.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cookmate.R
import com.example.cookmate.databinding.FragmentProfileBinding
import com.example.cookmate.ui.adapter.RecipeAdapter
import com.example.cookmate.data.model.Recipe


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        //set up recycler views for horizonal scrolling
        setupRecyclerView(binding.recipesRecyclerView)
        setupRecyclerView(binding.favoritesRecyclerView)

    }

    private fun setupRecyclerView(recyclerView: RecyclerView){
        //set up recycler view for user's recipes
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = RecipeAdapter(getSampleData()) //TODO: Replace this with the actual data
    }

    private fun getSampleData(): List<Recipe> {
        // TODO: Replace this with actual data retrieval logic
        return listOf(
            Recipe("Recipe 1", R.drawable.ic_recipe_placeholder),
            Recipe("Recipe 2", R.drawable.ic_recipe_placeholder),
            Recipe("Recipe 3", R.drawable.ic_recipe_placeholder)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}