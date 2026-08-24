package com.example.appmobile.data.repository

import android.content.Context
import com.example.appmobile.domain.repository.SensorRepository
import com.example.appmobile.presentation.shared.PhoneDataHolder

class SensorRepositoryImpl(private val context: Context) : SensorRepository {

    override fun saveHeartRate(bpm: Float) {
        PhoneDataHolder.heartRate.value = bpm
        // Aquí puedes agregar lógica para guardar en BD o prefs usando context
    }

    override fun saveHRV(data: String) {
        PhoneDataHolder.hrv.value = data
    }

    override fun saveSleepPhase(phase: String) {
        PhoneDataHolder.sleepPhase.value = phase
    }
}
