package com.example.cookmate.data.firebase

import android.util.Log
import com.example.cookmate.data.model.LoggedInUser
import com.example.cookmate.data.model.Recipe
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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

    fun getAllRecipes(onComplete: (Boolean) -> Unit) {
        val db = Firebase.firestore.collection("recipes")

        db.get().addOnSuccessListener {
                result ->
            for (document in result) {
                val data = document.data ?: emptyMap<String, Any>()

                /* TODO: Change Recipe data class to accommodate  this
                val ingredientsList = (data["ingredients"] as? List<Map<String, Any>>)?.map {
                    ingredientMap ->
                    Ingredient(
                        amount = ingredientMap["amount"] as? String ?: "",
                        name = ingredientMap["name"] as? String ?: ""
                    )
                } ?: emptyList()

                val alt_ingredientsList = (data["alternativeIngredients"] as? List<Map<String, Any>>)?.map {
                    ingredientMap ->
                    Ingredient(
                        amount = ingredientMap["amount"] as? String ?: "",
                        name = ingredientMap["name"] as? String ?: "",
                        originalIngredient = ingredientMap["originalIngredient"] as? String?: ""
                    )
                } ?: emptyList()
                */

                val recipe = Recipe(
                    name = data["recipeName"] as? String ?: "",
                    imageRes = 0,
                    category = data["category"] as? String ?: "",
                    difficulty = data["difficulty"] as? String ?: "",
                    time = data["prepTime"] as? String ?: "",
                    servings = data["servingSize"] as? String ?: "",
                    rating = data["rating"] as? Float ?: -1f,
                    authorId = data["recipeAuthorId"] as? String ?: ""

                    /* TODO: Change Recipe data class to accommodate  this
                    tags = data["tags"] as? MutableList<String> ?: null,
                    instructions = data["instructions"] as? String ?: "",
                    ingredients = ingredientsList,
                    alternateIngredients = alt_ingredientList,
                    cookingTime = data["cookingTime"] as? String ?: "",
                    creationDate = (data["creationDate"] as? Timestamp ?: null)?.toDate()
                     */
                )
                allRecipes[document.id] = recipe
            }
            onComplete(true)
        } .addOnFailureListener {
                exception -> Log.e("Firestore", "Error getting recipe documents", exception)
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
}