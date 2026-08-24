package com.example.appmobile.presentation.reciver

import android.content.Intent
import android.util.Log
import com.example.appmobile.data.database.SleepDatabase
import com.example.appmobile.data.database.entity.SleepCycleEntity
import com.example.appmobile.data.database.entity.SleepPhaseDataEntity
import com.example.appmobile.presentation.shared.PhoneDataHolder
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.gson.Gson
import com.google.android.gms.wearable.Wearable


/**
 * Escucha mensajes enviados desde el Wearable.
 */
data class SleepPhaseData(
    val id: Int,
    val phase: String,
    val datetime: String,
    val hr_bpm: Int,
    val hrv_rmssd: Double,
    val hrv_sdnn: Double
)

data class SleepCycle(
    val uidUser: String,
    val deviceId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val timezone: String,
    val totalDuration: Int,
    val sleepDuration: Int,
    val lightSleepMinutes: Int,
    val deepSleepMinutes: Int,
    val remSleepMinutes: Int,
    val awakeDuration: Int,
    val sleepEfficiency: Double,
    val awakeningsCount: Int,
    val quality: String,
    val avgHeartRate: Int,
    val minHeartRate: Int,
    val maxHeartRate: Int,
    val avgMovement: Int,
    val avgRmssd: Double,
    val avgSdnn: Double,
    val sleepPhaseData: List<SleepPhaseData>,
    val createdAt: Long,
    val dataVersion: String
)
class PhoneMessageReceiver : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
    }

    override fun onMessageReceived(event: MessageEvent) {
        super.onMessageReceived(event)
        val path = event.path
        val dataString = String(event.data, Charsets.UTF_8)
        val sourceNodeId = event.sourceNodeId
        serviceScope.launch {
            if (!isTrustedNode(sourceNodeId)) {
                Log.w("PhoneReceiver", "Mensaje rechazado: origen no confiable")
                return@launch
            }
            handleMessage(path, dataString)
        }
    }

    private suspend fun isTrustedNode(nodeId: String): Boolean =
        Wearable.getNodeClient(applicationContext).connectedNodes.await()
            .any { it.id == nodeId }

    private fun handleMessage(path: String, dataString: String) {
        when (path) {
            "/heart_rate" -> {
                val bpm = dataString.toFloatOrNull()
                bpm?.let {
                    runOnUiThread {
                        PhoneDataHolder.heartRate.value = it
                    }
                } ?: Log.w("PhoneReceiver", "No se pudo procesar la frecuencia cardíaca")
            }
            "/hrv" -> {
                runOnUiThread {
                    PhoneDataHolder.hrv.value = dataString
                }
            }
            "/sleep_phase" -> {
                runOnUiThread {
                    PhoneDataHolder.sleepPhase.value = dataString
                }
            }
            "/sleep_full_data" -> {
                try {
                    val cycle = Gson().fromJson(dataString, SleepCycle::class.java)

                    val cycleEntity = SleepCycleEntity(
                        createdAt = cycle.createdAt,
                        uidUser = cycle.uidUser,
                        deviceId = cycle.deviceId,
                        date = cycle.date,
                        startTime = cycle.startTime,
                        endTime = cycle.endTime,
                        timezone = cycle.timezone,
                        totalDuration = cycle.totalDuration,
                        sleepDuration = cycle.sleepDuration,
                        lightSleepMinutes = cycle.lightSleepMinutes,
                        deepSleepMinutes = cycle.deepSleepMinutes,
                        remSleepMinutes = cycle.remSleepMinutes,
                        awakeDuration = cycle.awakeDuration,
                        sleepEfficiency = cycle.sleepEfficiency,
                        awakeningsCount = cycle.awakeningsCount,
                        quality = cycle.quality,
                        avgHeartRate = cycle.avgHeartRate,
                        minHeartRate = cycle.minHeartRate,
                        maxHeartRate = cycle.maxHeartRate,
                        avgMovement = cycle.avgMovement,
                        avgRmssd = cycle.avgRmssd,
                        avgSdnn = cycle.avgSdnn,
                        dataVersion = cycle.dataVersion
                    )

                    val phasesEntity = cycle.sleepPhaseData.map {
                        SleepPhaseDataEntity(
                            id = it.id,
                            parentCreatedAt = cycle.createdAt,
                            phase = it.phase,
                            datetime = it.datetime,
                            hr_bpm = it.hr_bpm,
                            hrv_rmssd = it.hrv_rmssd,
                            hrv_sdnn = it.hrv_sdnn
                        )
                    }

                    CoroutineScope(Dispatchers.IO).launch {
                        val db = SleepDatabase.getDatabase(applicationContext)
                        db.sleepCycleDao().insertSleepCycle(cycleEntity)
                        db.sleepPhaseDataDao().insertSleepPhaseData(phasesEntity)
                    }
                } catch (e: Exception) {
                    Log.e("PhoneReceiver", "No se pudo guardar el ciclo de sueño")
                }
            }
            else -> {
                Log.w("PhoneReceiver", "Path de mensaje no reconocido")
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun runOnUiThread(action: () -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).post(action)
    }
}
