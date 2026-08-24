package com.example.dashboardapp.data.local.session

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {
    @Volatile
    private var loggedIn = false

    fun setLoggedIn(isLoggedIn: Boolean) {
        loggedIn = isLoggedIn
    }

    fun isLoggedIn(): Boolean = loggedIn

    fun clearSession() {
        loggedIn = false
    }
}
