package com.example.cookmate.data.firebase

import android.util.Log
import android.widget.Toast
import com.example.cookmate.data.model.Ingredient
import com.example.cookmate.data.model.LoggedInUser
import com.example.cookmate.data.model.Recipe
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object FirebaseMethods{
    var allRecipes: MutableMap<String, Recipe> =  mutableMapOf<String, Recipe>()
    var allUsers: MutableMap<String, LoggedInUser> = mutableMapOf<String, LoggedInUser>()
    lateinit var currentUser: LoggedInUser

    fun getAllUsers(onComplete: (Boolean) -> Unit) {
        val db = Firebase.firestore.collection("users")

        db.get().addOnSuccessListener {
            result ->
            for (document in result) {
                val data = document.data ?: emptyMap<String, Any>()
                val user = LoggedInUser(
                    userId = document.id,
                    displayName = data["username"] as? String ?: "",
                    authLevel = data["authLevel"] as? String ?: "",
                    createdRecipes = data["createdRecipes"] as? MutableList<String>,
                    favoriteRecipe = data["favoriteRecipes"] as? MutableList<String>
                )
                allUsers[document.id] = user
            }
            onComplete(true)
        } .addOnFailureListener {
            exception -> Log.e("Firestore", "Error getting user documents", exception)
            onComplete(false)
        }
    }

    fun loginUser(email: String, password: String, onComplete: (Boolean) -> Unit) {
        val auth = Firebase.auth

        if(auth.currentUser == null) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                task->
                if(task.isSuccessful) {
                    val logUser = auth.currentUser
                    Log.d("FirebaseAuth", "Logged in as: ${logUser?.uid}")

                    currentUser = allUsers[logUser?.uid]!!
                    onComplete(true)
                }
            } .addOnFailureListener {
                exception ->
                when(exception) {
                    is FirebaseAuthInvalidUserException -> {
                        Log.e("FirebaseAuth", "Error: Inv Email")
                    }
                    is FirebaseAuthInvalidCredentialsException -> {
                        Log.e("FirebaseAuth", "Error: Incorrect password")
                    }
                    else -> {
                        Log.e("FirebaseAuth", "Error occurred: ${exception.localizedMessage}")
                    }
                }
                onComplete(false)
            }
        } else {
            Log.e("FirebaseAuth", "User is already logged in")
            onComplete(false)
        }
    }

    fun logoutUser() {
        Firebase.auth.signOut()
        currentUser = LoggedInUser("0", "ERR", "nobody", null, null)
    }

    fun createUser(email: String, password: String, username: String, onComplete: () -> Unit) {
        val auth = Firebase.auth
        val db = Firebase.firestore.collection("users")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get the newly created user
                    val user = auth.currentUser

                    // Update the user's profile with the displayName
                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                    }

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Log.d("CreateUser", "Display name updated successfully.")
                                onComplete()
                            } else {
                                Log.e("CreateUser", "Failed to update display name", updateTask.exception)
                            }
                        }
                } else {
                    Log.e("CreateUser", "Failed to create user", task.exception)
                }
            }
    }

    fun addToFirestore(data: Any, database: String, onComplete: (String?) -> Unit){
        val db = Firebase.firestore.collection(database)

        db.add(data)
            .addOnSuccessListener {
                documentReference ->
                Log.d("Firestore", "added document to $database with ID: ${documentReference.id}")
                onComplete(documentReference.id)
            } .addOnFailureListener {
                exception ->
                Log.e("Firestore", "error adding to $database", exception)
                onComplete(null)
            }
    }

    fun storeRecipe(recipe: Recipe, callback: (Boolean) -> Unit){

        val firestore = FirebaseFirestore.getInstance()

        val recipeData = hashMapOf(
            "title" to recipe.title,
            "ingredients" to recipe.ingredients,
            "preparationSteps" to recipe.preparationSteps,
            "cookingTime" to recipe.cookingTime,
            "prepTime" to recipe.prepTime,
            "servingSize" to recipe.servingSize,
            "categories" to recipe.categories,
            "difficulty" to recipe.difficulty,
            "isDraft" to recipe.isDraft,
            "authorId" to recipe.authorId,
            "imageRes" to recipe.imageRes,
            "rating" to recipe.rating,
            "recipeDescription" to recipe.recipeDescription,
            "calories" to recipe.calories,
            "fat" to recipe.fat,
            "carbs" to recipe.carbs,
            "protein" to recipe.protein
        )

        firestore.collection("recipes")
            .add(recipeData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun updateAField(id: String, collection: String, fieldToUpdate: String, updateValue: Any ,onComplete:(Boolean) -> Unit){
        val ref = com.google.firebase.Firebase.firestore.collection(collection).document(id)

        ref.update(fieldToUpdate, updateValue)
            .addOnSuccessListener {
                Log.d("Firestore Update", "Updating ${ref.id} successful")
                onComplete(true)
            } .addOnFailureListener {
                    exception ->
                Log.e("Firestore Update", "Failure updating ${ref.id} ", exception)
                onComplete(false)
            }
    }

    fun deleteADocument(id: String, collection: String, onComplete: (Boolean) -> Unit) {
        val ref = com.google.firebase.Firebase.firestore.collection(collection).document(id)

        ref.delete()
            .addOnSuccessListener {
                Log.d("Firestore Delete", "Deleted $id from $collection")
                onComplete(true)
            } .addOnFailureListener {
                    exception ->
                Log.e("Firestore Delete", "Delete for $id failed", exception)
                onComplete(false)
            }
    }

    // Function to query recipes for the current user
    fun getRecipesForCurrentUser(currentUserId: String, onResult: (List<Map<String, Any>>) -> Unit) {

        val db = FirebaseFirestore.getInstance()

        db.collection("recipes")
            .whereEqualTo("authorId", currentUserId)
            .get()
            .addOnSuccessListener { documents ->
                val recipes = documents.map { it.data }
                onResult(recipes)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onResult(emptyList())
            }
    }

    //Function to get all ingredients from Firestore
    fun getAllIngredients(onResult: (List<Ingredient>) -> Unit) {

        val db = FirebaseFirestore.getInstance()

        db.collection("ingredients")
            .get()
            .addOnSuccessListener { documents ->
                val ingredients = documents.map { it.toObject(Ingredient::class.java) }
                onResult(ingredients)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onResult(emptyList())
            }
    }
}