package com.example.dashboardapp.presentation.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class DashboardWebSocketClient(
    private val serverUrl: String,
    private val cookieHeader: String?,
    private val onConnectionChanged: (Boolean) -> Unit,
    private val onSleepStateUpdate: (SleepStateUpdate) -> Unit,
    private val onAllStatesReceived: (List<SleepStateUpdate>) -> Unit,
    private val onUserDisconnected: (String) -> Unit = {} // Callback para desconexión de usuario
) {
    
    private var webSocketClient: WebSocketClient? = null
    private val gson = Gson()
    private val tag = "DashboardWebSocket"
    
    fun connect() {
        try {
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.d(tag, "✅ Conectado al dashboard WebSocket")
                    onConnectionChanged(true)
                }
                
                override fun onMessage(message: String?) {
                    message?.let { handleMessage(it) }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(tag, "Dashboard WebSocket cerrado")
                    onConnectionChanged(false)
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(tag, "Error de conexión WebSocket")
                    onConnectionChanged(false)
                }
            }
            cookieHeader?.takeIf { it.isNotBlank() }?.let { webSocketClient?.addHeader("Cookie", it) }
            webSocketClient?.connect()
        } catch (e: Exception) {
            Log.e(tag, "No se pudo crear el cliente WebSocket")
            onConnectionChanged(false)
        }
    }
    
    fun disconnect() {
        webSocketClient?.close()
        webSocketClient = null
        onConnectionChanged(false)
    }
    
    private fun handleMessage(message: String) {
        try {
            val messageMap = gson.fromJson(message, Map::class.java) as Map<String, Any>
            
            when (messageMap["action"]) {
                "allStates" -> {
                    val statesArray = messageMap["states"] as? List<Map<String, Any>> ?: emptyList()
                    val sleepStates = statesArray.mapNotNull { parseState(it) }
                    onAllStatesReceived(sleepStates)
                    Log.d(tag, "Estados iniciales recibidos: ${sleepStates.size}")
                }
                
                "stateChange" -> {
                    val eventMap = messageMap["event"] as? Map<String, Any>
                    if (eventMap != null) {
                        parseState(eventMap)?.let { state ->
                            onSleepStateUpdate(state)
                            Log.d(tag, "Estado de sueño actualizado")
                        }
                    }
                }
                
                "userDisconnected" -> {
                    val userId = messageMap["userId"] as? String
                    val userName = messageMap["userName"] as? String
                    if (userId != null) {
                        onUserDisconnected(userId)
                        Log.d(tag, "Usuario desconectado del dashboard")
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            Log.e(tag, "No se pudo procesar el mensaje WebSocket")
        } catch (e: Exception) {
            Log.e(tag, "Error procesando el mensaje WebSocket")
        }
    }
    
    private fun parseState(stateMap: Map<String, Any>): SleepStateUpdate? {
        return try {
            SleepStateUpdate(
                userId = stateMap["userId"] as? String ?: return null,
                userName = stateMap["userName"] as? String ?: "",
                sleepState = stateMap["sleepState"] as? String ?: return null,
                sleepStateDisplay = stateMap["sleepStateDisplay"] as? String ?: "",
                colorCode = stateMap["colorCode"] as? String ?: "#000000",
                timestamp = stateMap["timestamp"] as? String ?: "",
                deviceId = stateMap["deviceId"] as? String
            )
        } catch (e: Exception) {
            Log.e(tag, "No se pudo procesar el estado de sueño")
            null
        }
    }
    
    fun isConnected(): Boolean = webSocketClient?.isOpen == true
}

data class SleepStateUpdate(
    val userId: String,
    val userName: String,
    val sleepState: String,
    val sleepStateDisplay: String,
    val colorCode: String,
    val timestamp: String,
    val deviceId: String? = null
)

enum class SleepStateEnum(val displayName: String, val colorCode: String, val icon: String) {
    REM("REM", "#FF6B6B", "🔴"),
    LIGHT("Sueño Ligero", "#4ECDC4", "🟢"),
    DEEP("Sueño Profundo", "#45B7D1", "🔵"),
    AWAKE("Despierto", "#96CEB4", "🟡");
    
    companion object {
        fun fromString(value: String): SleepStateEnum? {
            return values().find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
