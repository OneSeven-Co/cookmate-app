package com.example.cookmate.utils

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Manager class for handling image uploads to Firebase Storage
 */
class ImageUploadManager {
    private val storage = FirebaseStorage.getInstance()
    private val TAG = "ImageUploadManager"
    
    /**
     * Uploads an image to Firebase Storage
     * @param userId The ID of the user uploading the image
     * @param imageUri The URI of the image to upload
     * @return The download URL of the uploaded image
     */
    suspend fun uploadRecipeImage(userId: String, imageUri: Uri): Result<String> {
        return try {
            val filename = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            Log.d(TAG, "Starting upload for user: $userId")
            Log.d(TAG, "File name: $filename")
            Log.d(TAG, "Image URI: $imageUri")

            // Create the full path
            val imagePath = "users/$userId/recipes/images/$filename"
            Log.d(TAG, "Full storage path: $imagePath")

            val ref = storage.reference.child(imagePath)

            try {
                Log.d(TAG, "Starting file upload...")
                // Upload the file bytes
                val bytes = imageUri.toBytes()
                Log.d(TAG, "File size: ${bytes.size} bytes")
                
                val uploadTask = ref.putBytes(bytes).await()
                Log.d(TAG, "Upload successful, bytes transferred: ${uploadTask.bytesTransferred}")
                
                Log.d(TAG, "Getting download URL...")
                val downloadUrl = ref.downloadUrl.await()
                Log.d(TAG, "Download URL obtained: $downloadUrl")
                
                Result.success(downloadUrl.toString())
            } catch (e: Exception) {
                Log.e(TAG, "Inner upload error", e)
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Outer upload error", e)
            Result.failure(e)
        }
    }

    /**
     * Converts a Uri to a ByteArray
     */
    private suspend fun Uri.toBytes(): ByteArray {
        return storage.app.applicationContext.contentResolver
            .openInputStream(this)?.use { it.readBytes() }
            ?: throw IllegalStateException("Could not read file")
    }
} 