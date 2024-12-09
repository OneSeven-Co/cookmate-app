package com.example.cookmate.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient

class IngredientsAdapter(private val ingredients: List<Ingredient>) :
    RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        private val ingredientAmount: TextView = itemView.findViewById(R.id.ingredientAmount)
        private val ingredientSubstitute: TextView = itemView.findViewById(R.id.ingredientSubstitute)

        fun bind(ingredient: Ingredient) {
            "${ingredient.name} - ".also { ingredientName.text = it }
            "${ingredient.amount} - ".also { ingredientAmount.text = it }
            if (ingredient.substitute != null) {
                " (${ingredient.substitute})".also { ingredientSubstitute.text = it }
                ingredientSubstitute.visibility = View.VISIBLE
            } else {
                ingredientSubstitute.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.ingredient_item, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size
}
