/**
 * When a document is created at groups/{groupId}/announcements/{id},
 * sends a notification to FCM topic "hg_" + sanitized groupId (must match Android GroupNotificationTopics).
 *
 * Deploy: npm install in functions/, then firebase deploy --only functions
 */
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

function topicForGroupId(groupId) {
  const safe = String(groupId).replace(/[^a-zA-Z0-9\-_.~%]/g, "_").substring(0, 180);
  return `hg_${safe}`;
}

exports.onGroupAnnouncementCreated = functions.firestore
  .document("groups/{groupId}/announcements/{announcementId}")
  .onCreate(async (snap, context) => {
    const data = snap.data() || {};
    const groupId = context.params.groupId;
    const topic = topicForGroupId(groupId);
    const title = data.title || "Group announcement";
    const body = data.body || "";
    const notifTitle = title.length > 80 ? title.substring(0, 77) + "..." : title;
    const notifBody = body.length > 240 ? body.substring(0, 237) + "..." : body;

    const message = {
      topic,
      notification: {
        title: notifTitle,
        body: notifBody,
      },
      data: {
        type: "group_announcement",
        groupId: String(groupId),
        title: String(data.title || ""),
        body: String(data.body || ""),
      },
      android: {
        priority: "high",
      },
    };

    try {
      await admin.messaging().send(message);
      functions.logger.info("FCM sent to topic", { topic });
    } catch (e) {
      functions.logger.error("FCM send failed", e);
    }
  });
