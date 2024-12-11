package com.example.cookmate.data.repository

import kotlinx.coroutines.flow.Flow

/**
 * Base interface for repositories defining common CRUD operations
 * @param T The type of model this repository handles
 */
interface BaseRepository<T> {
    /**
     * Updates a field in a document
     * @param id The document ID
     * @param field The field to update
     * @param value The new value
     * @return Flow containing Result indicating success or failure
     */
    fun updateField(id: String, field: String, value: Any): Flow<Result<Unit>>

    /**
     * Deletes a document
     * @param id The document ID
     * @return Flow containing Result indicating success or failure
     */
    fun deleteDocument(id: String): Flow<Result<Unit>>

    /**
     * Gets all documents
     * @return Flow containing Result with list of documents
     */
    fun getAll(): Flow<Result<List<T>>>
} 