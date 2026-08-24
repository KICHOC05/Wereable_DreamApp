package com.example.dashboardapp.data.local.session

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionCookieJar @Inject constructor() : CookieJar {
    private val cookies = CopyOnWriteArrayList<Cookie>()

    override fun saveFromResponse(url: HttpUrl, newCookies: List<Cookie>) {
        newCookies.forEach { newCookie ->
            cookies.removeIf {
                it.name == newCookie.name &&
                    it.domain == newCookie.domain &&
                    it.path == newCookie.path
            }
            if (newCookie.expiresAt > System.currentTimeMillis()) {
                cookies.add(newCookie)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookies.filter { it.matches(url) }

    fun cookieHeader(url: String): String? {
        val httpUrl = url
            .replaceFirst("wss://", "https://")
            .replaceFirst("ws://", "http://")
            .toHttpUrlOrNull()

        return httpUrl
            ?.let { loadForRequest(it) }
            ?.takeIf { it.isNotEmpty() }
            ?.joinToString("; ") { "${it.name}=${it.value}" }
    }

    fun clear() {
        cookies.clear()
    }
}
