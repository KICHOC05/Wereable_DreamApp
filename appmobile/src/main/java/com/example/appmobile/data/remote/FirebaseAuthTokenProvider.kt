package com.example.appmobile.data.remote

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object FirebaseAuthTokenProvider {
    suspend fun getCurrentToken(): String? = withContext(Dispatchers.IO) {
        val user = FirebaseAuth.getInstance().currentUser ?: return@withContext null
        runCatching {
            Tasks.await(user.getIdToken(false), 10, TimeUnit.SECONDS).token
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }
}
