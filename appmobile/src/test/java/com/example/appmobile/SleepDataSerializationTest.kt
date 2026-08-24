package com.example.appmobile

import com.example.appmobile.domain.model.SleepDataUpload
import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepDataSerializationTest {
    @Test
    fun sleepPayloadUsesExpectedContractWithoutCredentials() {
        val payload = SleepDataUpload(
            uidUser = "test-user",
            deviceId = "test-device",
            date = "2025-01-01",
            startTime = "2025-01-01T22:00:00.000Z",
            endTime = "2025-01-02T06:00:00.000Z",
            timezone = "UTC",
            totalDuration = 480,
            sleepDuration = 420,
            lightSleepMinutes = 200,
            deepSleepMinutes = 120,
            remSleepMinutes = 100,
            awakeDuration = 60,
            sleepEfficiency = 87.5,
            awakeningsCount = 3,
            quality = "GOOD",
            avgHeartRate = 62,
            minHeartRate = 48,
            maxHeartRate = 78,
            avgMovement = 15,
            avgRmssd = 45.2,
            avgSdnn = 52.1,
            sleepPhaseData = emptyList(),
            createdAt = 1L,
            dataVersion = "1.0"
        )

        val json = Gson().toJson(payload)

        assertTrue(json.contains("\"uidUser\":\"test-user\""))
        assertFalse(json.contains("password"))
    }
}
