package com.example.wereableapp.presentation.data.communication

import android.util.Log
import com.example.wereableapp.presentation.data.repository.UserRepository
import com.example.wereableapp.presentation.domain.model.UserData
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// En el WEARABLE
class WearMessageReceiver : WearableListenerService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        if (messageEvent.path == "/user_data") {
            val payload = String(messageEvent.data, Charsets.UTF_8)
            serviceScope.launch {
                val trusted = Wearable.getNodeClient(applicationContext).connectedNodes.await()
                    .any { it.id == messageEvent.sourceNodeId }
                if (!trusted) {
                    Log.w("WearMessageReceiver", "Mensaje rechazado: origen no confiable")
                    return@launch
                }
                processUserData(payload)
            }
        }
    }

    private fun processUserData(payload: String) {

            val parts = payload.split("|")
            val edad = parts.getOrNull(0)?.toIntOrNull()
            val peso = parts.getOrNull(1)?.toFloatOrNull()
            val estatura = parts.getOrNull(2)?.toFloatOrNull()
            val sexo = parts.getOrNull(3)?.trim()

            if (edad != null && peso != null && estatura != null && !sexo.isNullOrEmpty()) {
                val userData = UserData(edad, peso, estatura, sexo)
                UserRepository.saveUserData(userData)

            } else {
                Log.e("WearMessageReceiver", "No se pudo procesar la información del usuario")
            }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
