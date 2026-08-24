package com.example.dashboardapp.data.remote.helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import com.example.dashboardapp.data.local.session.SessionCookieJar

class NotifyUpdateHelper(
    private val notifyUpdateUrl: String,
    private val sessionCookieJar: SessionCookieJar
) {
    suspend fun notifyUpdate() {
        withContext(Dispatchers.IO) {
            try {
                val url = notifyUpdateUrl.toHttpUrlOrNull() ?: return@withContext
                val requestBuilder = Request.Builder().url(url).post(okhttp3.RequestBody.create(null, ByteArray(0)))
                sessionCookieJar.cookieHeader(notifyUpdateUrl)?.let { requestBuilder.header("Cookie", it) }
                okhttp3.OkHttpClient().newCall(requestBuilder.build()).execute().use { }
            } catch (_: Exception) {
            }
        }
    }
}
