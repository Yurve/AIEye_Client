package com.example.AIEye


import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class MessagingService : com.google.firebase.messaging.FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {

        //앱이 실행 중이라면 알람 처리 X
        if (isForeground()) {
            return
        }

        //수신한 메시지를 처리
        if (message.data.isNotEmpty()) {
            Log.d("message!", "Message data payload: ${message.data}")
            //10초 이상은 비동기 처리를 권장
            //10초 이하는 바로 실행
            val title = message.data["title"]
            val body = message.data["body"]

            //테스트 알람
            notification(title, body)
        }
        super.onMessageReceived(message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("newToken", token)
        //token 을 서버로 전송
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun notification(title: String?, body: String?) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MainActivity.CHANNEL_ID,
                "Event",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        //문자열에서 cameraId 추출
        val jsonObject = JSONObject(body!!)
        Log.d("sdf", jsonObject.toString())
        val cameraId = jsonObject.get("cameraId") as Int
        val msg = jsonObject.get("task") as String

        WebViewActivity.CameraId = cameraId

        //알람을 누르면 앱을 실행
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getActivity(
                applicationContext, 0,
                intent, PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val builder = NotificationCompat.Builder(applicationContext, MainActivity.CHANNEL_ID)


        builder
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(title)
            .setContentText(msg)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_hkai)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }

    private fun isForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        //실행중인 앱 목록
        val appProcesses = activityManager.runningAppProcesses ?: return false
        // 목록 중 현재 이 앱이 foreground 상태라면 return true
        for (appProcess in appProcesses) {
            if (appProcess.processName == packageName && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }

}