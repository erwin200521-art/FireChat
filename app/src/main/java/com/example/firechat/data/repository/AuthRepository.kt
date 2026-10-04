package com.example.firechat.data.repository

import com.example.firechat.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Acceso a Firebase Authentication (y creación del perfil en Firestore al registrarse). */
class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    val currentUserId: String? get() = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun register(name: String, email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        val firebaseUser = checkNotNull(result.user)

        firebaseUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()
        ).await()

        val profile = User(uid = firebaseUser.uid, name = name.trim(), email = email.trim())
        db.collection(UserRepository.USERS).document(firebaseUser.uid).set(profile).await()
    }

    fun logout() = auth.signOut()
}
