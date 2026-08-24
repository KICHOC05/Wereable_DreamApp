package com.example.dashboardapp

import com.example.dashboardapp.data.local.session.SessionCookieJar
import com.example.dashboardapp.data.local.session.SessionManager
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionSecurityTest {
    @Test
    fun sessionStateIsClearedOnLogout() {
        val session = SessionManager()
        session.setLoggedIn(true)

        assertTrue(session.isLoggedIn())
        session.clearSession()
        assertTrue(!session.isLoggedIn())
    }

    @Test
    fun websocketCookieIsInMemoryAndCanBeCleared() {
        val url = "https://example.test/ws".toHttpUrl()
        val jar = SessionCookieJar()
        val cookie = Cookie.Builder()
            .name("SESSION")
            .value("test-cookie")
            .domain(url.host)
            .path("/")
            .secure()
            .httpOnly()
            .build()

        jar.saveFromResponse(url, listOf(cookie))
        assertEquals("SESSION=test-cookie", jar.cookieHeader(url.toString()))
        jar.clear()
        assertNull(jar.cookieHeader(url.toString()))
    }
}
