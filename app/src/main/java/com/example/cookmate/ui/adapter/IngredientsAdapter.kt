package com.example.cookmate.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cookmate.R
import com.example.cookmate.data.model.Ingredient

/**
 * Adapter for displaying recipe ingredients in a RecyclerView
 * @property ingredients List of ingredients to display
 */
class IngredientsAdapter(private val ingredients: List<Ingredient>) :
    RecyclerView.Adapter<IngredientsAdapter.IngredientViewHolder>() {

    /**
     * ViewHolder for ingredient items
     * Displays ingredient name, amount, and substitutes if available
     */
    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        private val ingredientAmount: TextView = itemView.findViewById(R.id.ingredientAmount)

        /**
         * Binds ingredient data to the view
         * @param ingredient The ingredient to display
         */
        fun bind(ingredient: Ingredient) {
            // Format: "Name - Amount Unit [substitutes]"
            val substitutesText = if (!ingredient.substitutes.isNullOrEmpty()) {
                " [${ingredient.substitutes.joinToString(", ")}]"
            } else ""
            
            val amountText = "${ingredient.amount} ${ingredient.unit}"
            ingredientAmount.text = amountText
            ingredientName.text = "${ingredient.name}$substitutesText"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingredient_detail, parent, false)
        return IngredientViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        holder.bind(ingredients[position])
    }

    override fun getItemCount(): Int = ingredients.size
}
