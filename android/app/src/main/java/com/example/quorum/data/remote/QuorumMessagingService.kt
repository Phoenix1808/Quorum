package com.example.quorum.data.remote

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.quorum.MainActivity
import com.example.quorum.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// FCM ke do kaam yahan aate hain:
//   1) token mila / refresh hua  -> backend ko batao
//   2) message aaya              -> notification dikhao
class QuorumMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        android.util.Log.d("Quorum", "FCM token refreshed")
        // Token badalta rehta hai (reinstall, app data clear, Firebase ka apna rotation).
        // Har baar backend ko naya token dena zaroori hai warna push kahin nahi pahunchega.
        PushRegistration.onToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        // Backend "notification" payload bhejta hai to app background me hone par Android
        // khud notification dikha deta hai aur ye method call hi nahi hota. Foreground me
        // hamesha yahi call hota hai. Isliye dono jagah kaam kare, iske liye backend ko
        // "data" payload bhejna chahiye -- tab har haal me ye method chalta hai.
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Proposal closing soon"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        // Android 13+ pe bina permission ke notification chup-chaap gir jati hai
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            android.util.Log.d("Quorum", "Notification permission nahi hai — skip")
            return
        }

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel API 26+ pe zaroori hai. Dobara banane se kuch nahi bigadta.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Proposal deadlines",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Followed DAOs ke proposals band hone se pehle reminder"
                }
            )
        }

        // Tap karne par app khule
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        // id unique rakho warna har nayi notification purani ko replace kar degi
        NotificationManagerCompat.from(this)
            .notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "proposal_deadlines"
    }
}
