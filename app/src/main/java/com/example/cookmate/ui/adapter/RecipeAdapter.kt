package com.example.cookmate.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cookmate.R
import com.example.cookmate.data.model.Recipe

class RecipeAdapter(
    private val onRecipeClicked: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipeAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    // View holder for recipe items
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameView: TextView = itemView.findViewById(R.id.recipeName)
        private val imageView: ImageView = itemView.findViewById(R.id.recipeImage)
        private val difficultyView: TextView = itemView.findViewById(R.id.recipeDifficulty)
        private val timeView: TextView = itemView.findViewById(R.id.recipeTime)
        private val servingsView: TextView = itemView.findViewById(R.id.recipeServings)
        private val ratingView: TextView = itemView.findViewById(R.id.recipeRating)
        private val starIcon: ImageView = itemView.findViewById(R.id.starIcon)

        // Set up click listener
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onRecipeClicked(getItem(position))
                }
            }
        }

        // Bind recipe data to the view
        fun bind(recipe: Recipe) {
            nameView.text = recipe.name
            imageView.setImageResource(recipe.imageRes)
            difficultyView.text = recipe.difficulty ?: "N/A"
            timeView.text = recipe.time ?: "N/A"
            servingsView.text = recipe.servings ?: "N/A"
            ratingView.text = recipe.rating.toString()
            starIcon.setImageResource(R.drawable.ic_star)
        }
    }

    // Create view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe, parent, false)
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.name == newItem.name
        }
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}