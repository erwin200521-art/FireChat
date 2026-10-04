const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * Se dispara cada vez que se crea un mensaje en chats/{chatId}/messages/{messageId}.
 * Envía una notificación (mensaje "data") a todos los dispositivos del destinatario.
 */
exports.sendMessageNotification = onDocumentCreated(
  "chats/{chatId}/messages/{messageId}",
  async (event) => {
    const message = event.data.data();
    const db = admin.firestore();

    const chatSnap = await db.doc(`chats/${event.params.chatId}`).get();
    const participants = chatSnap.get("participants") || [];
    const receiverId = participants.find((uid) => uid !== message.senderId);
    if (!receiverId) return;

    const receiverRef = db.doc(`users/${receiverId}`);
    const tokens = (await receiverRef.get()).get("fcmTokens") || [];
    if (tokens.length === 0) return;

    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      data: {
        senderId: message.senderId,
        senderName: message.senderName || "Nuevo mensaje",
        body: message.text && message.text.length > 0 ? message.text : "📷 Imagen",
      },
      android: { priority: "high" },
    });

    // Limpia tokens inválidos/caducados
    const invalid = [];
    response.responses.forEach((res, i) => {
      const code = res.error && res.error.code;
      if (
        code === "messaging/registration-token-not-registered" ||
        code === "messaging/invalid-registration-token"
      ) {
        invalid.push(tokens[i]);
      }
    });
    if (invalid.length > 0) {
      await receiverRef.update({
        fcmTokens: admin.firestore.FieldValue.arrayRemove(...invalid),
      });
    }
  }
);
