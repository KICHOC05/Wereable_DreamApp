package com.example.appmobile

import android.security.NetworkSecurityPolicy
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSecurityInstrumentedTest {
    @Test
    fun cleartextTrafficIsDisabled() {
        assertFalse(NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted)
    }
}
