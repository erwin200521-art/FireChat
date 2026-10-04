package com.example.firechat.data.repository

import com.example.firechat.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Lista de usuarios y gestión de tokens FCM. */
class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
) {

    /** Emite en tiempo real todos los usuarios registrados excepto el actual. */
    fun observeUsers(): Flow<List<User>> = callbackFlow {
        val myId = auth.currentUser?.uid
        val registration = db.collection(USERS)
            .orderBy("name", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents
                    ?.mapNotNull { it.toObject(User::class.java)?.copy(uid = it.id) }
                    ?.filter { it.uid != myId }
                    .orEmpty()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }

    /** Guarda el token FCM del dispositivo en el perfil del usuario actual. */
    suspend fun saveFcmToken(token: String? = null) {
        val uid = auth.currentUser?.uid ?: return
        val fcmToken = token ?: messaging.token.await()
        db.collection(USERS).document(uid)
            .set(mapOf("fcmTokens" to FieldValue.arrayUnion(fcmToken)), SetOptions.merge())
            .await()
    }

    /** Elimina el token del dispositivo (se llama antes de cerrar sesión). */
    suspend fun removeFcmToken() {
        val uid = auth.currentUser?.uid ?: return
        val token = messaging.token.await()
        db.collection(USERS).document(uid)
            .update("fcmTokens", FieldValue.arrayRemove(token))
            .await()
        messaging.deleteToken().await()
    }

    companion object {
        const val USERS = "users"
    }
}
