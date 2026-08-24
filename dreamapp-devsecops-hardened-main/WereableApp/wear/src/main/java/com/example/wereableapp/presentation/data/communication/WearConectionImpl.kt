package com.example.wereableapp.presentation.data.communication

import android.content.Context
import android.util.Log
import com.example.wereableapp.presentation.domain.repository.WearConnectionRepository
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearConnectionImpl(
    private val context: Context
) : WearConnectionRepository {

    private val messageClient: MessageClient = Wearable.getMessageClient(context)

    override suspend fun sendHeartRate(bpm: Float) {
        sendMessage("/heart_rate", bpm.toString())
    }

    override suspend fun sendHRV(rmssd: Double, sdnn: Double) {
        val payload = "RMSSD:$rmssd;SDNN:$sdnn"
        sendMessage("/hrv", payload)
    }

    override suspend fun sendSleepPhase(phase: String) {
        sendMessage("/sleep_phase", phase)
    }

    override suspend fun sendSleepJson(json: String) {
        sendMessage("/sleep_full_data", json)
    }

    private suspend fun sendMessage(path: String, payload: String) {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()

            if (nodes.isEmpty()) {
                return
            }

            for (node in nodes) {
                messageClient.sendMessage(
                    node.id,
                    path,
                    payload.toByteArray()
                ).await()
            }
        } catch (e: Exception) {
        }
    }
}
