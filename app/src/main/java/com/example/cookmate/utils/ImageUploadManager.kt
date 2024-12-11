package com.example.cookmate.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import android.util.Log
import com.google.firebase.storage.storageMetadata

/**
 * Manages image upload operations to Firebase Storage
 */
object ImageUploadManager {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Uploads a recipe image to Firebase Storage
     * @param userId The ID of the user uploading the image
     * @param imageUri The URI of the image to upload
     * @return Result containing the download URL if successful
     */
    suspend fun uploadRecipeImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            // Create a unique filename
            val filename = "recipes/${userId}/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(filename)

            // Log the upload attempt
            Log.d("ImageUploadManager", "Attempting to upload image to: $filename")
            
            // Verify the file exists
            val file = File(imageUri.path ?: "")
            if (!file.exists()) {
                Log.e("ImageUploadManager", "File does not exist at: ${imageUri.path}")
                return Result.failure(Exception("File does not exist"))
            }

            // Upload the file with metadata
            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }
            
            // Upload and await result
            val uploadTask = imageRef.putFile(imageUri, metadata).await()
            
            // Get download URL
            val downloadUrl = imageRef.downloadUrl.await().toString()
            Log.d("ImageUploadManager", "Successfully uploaded image. URL: $downloadUrl")
            
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e("ImageUploadManager", "Failed to upload image", e)
            Result.failure(e)
        }
    }
} 