package com.san3a.app.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch

class UserRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val users = db.collection("users")

    fun observeProfile(uid: String): Flow<UserProfile?> = callbackFlow {
        val registration = users.document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(UserProfile::class.java))
        }
        awaitClose { registration.remove() }
    }.catch { emit(null) }

    fun observeCraftsmen(): Flow<List<UserProfile>> = callbackFlow {
        val registration = users
            .whereEqualTo("role", UserRole.CRAFTSMAN.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(UserProfile::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }.catch { emit(emptyList()) }

    fun observeReviews(craftsmanUid: String): Flow<List<Review>> = callbackFlow {
        val registration = users.document(craftsmanUid)
            .collection("reviews")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents
                    ?.mapNotNull { it.toObject(Review::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }.catch { emit(emptyList()) }

    suspend fun saveProfile(profile: UserProfile) {
        // Merge preserves server-side/extra fields (e.g. fcmToken) that are not
        // part of UserProfile.
        users.document(profile.uid).set(profile.toMap(), SetOptions.merge()).await()
    }

    suspend fun updateAvailability(uid: String, available: Boolean) {
        users.document(uid).update("available", available).await()
    }

    suspend fun updateFcmToken(uid: String, token: String) {
        users.document(uid).set(mapOf("fcmToken" to token), SetOptions.merge()).await()
    }

    suspend fun deleteProfile(uid: String) {
        users.document(uid).delete().await()
    }

    suspend fun submitReview(craftsmanUid: String, review: Review) {
        val craftsmanRef = users.document(craftsmanUid)
        val reviewRef = craftsmanRef.collection("reviews").document(review.userId)
        db.runTransaction { txn ->
            val existing = txn.get(reviewRef)
            val oldRating = existing.getLong("rating") ?: 0L
            val isNew = !existing.exists()
            txn.set(reviewRef, review.toMap())
            val delta = review.rating.toLong() - oldRating
            val countDelta = if (isNew) 1L else 0L
            txn.update(
                craftsmanRef,
                mapOf(
                    "ratingSum" to FieldValue.increment(delta),
                    "ratingCount" to FieldValue.increment(countDelta)
                )
            )
        }.await()
    }

    private fun UserProfile.toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "name" to name,
        "phone" to phone,
        "whatsapp" to whatsapp,
        "role" to role,
        "craft" to craft,
        "region" to region,
        "available" to available,
        "createdAt" to createdAt,
        "ratingSum" to ratingSum,
        "ratingCount" to ratingCount,
        "latitude" to latitude,
        "longitude" to longitude
    )

    private fun Review.toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "userName" to userName,
        "rating" to rating,
        "comment" to comment,
        "createdAt" to createdAt
    )
}
