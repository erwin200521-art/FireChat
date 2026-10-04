package com.example.firechat.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.example.firechat.R
import com.example.firechat.data.repository.UserRepository
import com.example.firechat.ui.chat.ActiveChat
import com.example.firechat.ui.chat.ChatActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Recibe los mensajes "data" enviados por la Cloud Function (ver backend/functions)
 * y construye la notificación tanto en primer como en segundo plano.
 */
class FireChatMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { runCatching { UserRepository().saveFcmToken(token) } }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val senderId = data["senderId"] ?: return
        val senderName = data["senderName"] ?: getString(R.string.app_name)
        val body = data["body"] ?: return

        // Si el usuario ya está dentro de esa conversación no se notifica
        if (ActiveChat.partnerId == senderId) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return

        // Al tocar la notificación: abre el chat y "atrás" regresa a la lista de usuarios
        val pendingIntent = TaskStackBuilder.create(this)
            .addNextIntentWithParentStack(ChatActivity.newIntent(this, senderId, senderName))
            .getPendingIntent(
                senderId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(senderName)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Un id por remitente: los mensajes nuevos de la misma persona reemplazan al anterior
        NotificationManagerCompat.from(this).notify(senderId.hashCode(), notification)
    }
}
