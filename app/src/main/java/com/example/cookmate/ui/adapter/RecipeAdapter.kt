package com.example.cookmate.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
            nameView.text = recipe.title
            
            // Handle image loading
            when {
                recipe.localImagePath != null -> {
                    // Load from local URI
                    Glide.with(imageView.context)
                        .load(Uri.parse(recipe.localImagePath))
                        .placeholder(R.drawable.ic_recipe_placeholder)
                        .error(R.drawable.ic_recipe_placeholder)
                        .into(imageView)
                }
                recipe.imageRes != null && recipe.imageRes != 0 -> {
                    // Load from resource ID
                    Glide.with(imageView.context)
                        .load(recipe.imageRes)
                        .placeholder(R.drawable.ic_recipe_placeholder)
                        .error(R.drawable.ic_recipe_placeholder)
                        .into(imageView)
                }
                else -> {
                    // Fallback to placeholder
                    imageView.setImageResource(R.drawable.ic_recipe_placeholder)
                }
            }

            // Rest of the binding code
            difficultyView.text = recipe.difficulty ?: "N/A"
            timeView.text = recipe.cookingTime ?: "N/A"
            servingsView.text = recipe.servingSize ?: "N/A"
            recipe.rating.toString().also { ratingView.text = it }
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
            return oldItem.title == newItem.title
        }
        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}