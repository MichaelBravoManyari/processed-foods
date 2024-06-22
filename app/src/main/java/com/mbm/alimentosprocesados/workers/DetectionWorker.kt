package com.mbm.alimentosprocesados.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.mbm.alimentosprocesados.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val CHANNEL_ID = "low_battery_channel"

class DetectionWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val db = FirebaseFirestore.getInstance()
        val detectionsRef = db.collection("detections")
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        detectionsRef.get().addOnSuccessListener { snapshots ->
            val todayCount = snapshots.documents.count {
                it.getString("timestamp")?.startsWith(todayStr) == true
            }

            if (todayCount >= 300 && (todayCount - 300) % 20 == 0) {
                sendNotification(todayCount)
            }
        }.addOnFailureListener {

        }

        return Result.success()
    }

    private fun sendNotification(count: Int) {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Batería Baja")
            .setContentText("Se han realizado $count registros hoy. Ponga el prototipo a cargar.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(applicationContext)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permiso no otorgado, no se puede enviar la notificación
                    return
                }
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1
    }
}
