package com.example.firechat.data.repository

import android.net.Uri
import com.example.firechat.data.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Mensajería: Firestore (mensajes en tiempo real) + Storage (imágenes).
 *
 * Estructura:
 *  chats/{chatId}                       -> participants, lastMessage, lastTimestamp
 *  chats/{chatId}/messages/{messageId}  -> senderId, senderName, text, imageUrl, timestamp
 * donde chatId = uidA + "_" + uidB (ordenados alfabéticamente).
 */
class ChatRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    val currentUserId: String get() = checkNotNull(auth.currentUser).uid
    val currentUserName: String get() = auth.currentUser?.displayName.orEmpty().ifBlank { "Usuario" }

    fun chatIdWith(partnerId: String): String =
        listOf(currentUserId, partnerId).sorted().joinToString("_")

    /** Emite la lista ordenada (ascendente por fecha) cada vez que cambia la conversación. */
    fun observeMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val registration = db.collection(CHATS).document(chatId).collection(MESSAGES)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                // ESTIMATE: los mensajes propios pendientes de confirmar usan la hora local estimada
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.copy(id = doc.id)
                }.orEmpty()
                trySend(messages)
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendText(chatId: String, partnerId: String, text: String) {
        push(chatId, partnerId, text = text, imageUrl = null)
    }

    suspend fun sendImage(chatId: String, partnerId: String, imageUri: Uri) {
        val ref = storage.reference.child("$IMAGES/$chatId/${UUID.randomUUID()}.jpg")
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        ref.putFile(imageUri, metadata).await()
        val url = ref.downloadUrl.await().toString()
        push(chatId, partnerId, text = "", imageUrl = url)
    }

    /** Escribe el mensaje y actualiza el resumen del chat en una sola operación atómica. */
    private suspend fun push(chatId: String, partnerId: String, text: String, imageUrl: String?) {
        val chatRef = db.collection(CHATS).document(chatId)
        val messageRef = chatRef.collection(MESSAGES).document()

        val message = hashMapOf(
            "senderId" to currentUserId,
            "senderName" to currentUserName,
            "text" to text,
            "imageUrl" to imageUrl,
            "timestamp" to FieldValue.serverTimestamp()
        )
        val chatSummary = hashMapOf(
            "participants" to listOf(currentUserId, partnerId),
            "lastMessage" to text.ifEmpty { "📷 Imagen" },
            "lastTimestamp" to FieldValue.serverTimestamp()
        )

        db.batch()
            .set(messageRef, message)
            .set(chatRef, chatSummary, SetOptions.merge())
            .commit()
            .await()
    }

    private companion object {
        const val CHATS = "chats"
        const val MESSAGES = "messages"
        const val IMAGES = "chat_images"
    }
}
