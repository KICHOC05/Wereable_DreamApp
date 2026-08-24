import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

val endpointProperties = Properties().apply {
    val file = rootProject.file("config/api-endpoints.properties")
    if (file.isFile) file.inputStream().use { load(it) }
}

fun endpoint(propertyName: String, environmentName: String): String =
    providers.gradleProperty(environmentName)
        .orElse(providers.environmentVariable(environmentName))
        .orElse(endpointProperties.getProperty(propertyName, "https://example.invalid/"))
        .get().trimEnd('/') + "/"

val dreamAppApiUrl = endpoint("appmobile.api.baseUrl", "DREAMAPP_API_URL")
val sleepApiBaseUrl = endpoint("appmobile.api.sleepBaseUrl", "DREAMAPP_SLEEP_API_URL")
val userLookupBaseUrl = endpoint("appmobile.api.userLookupBaseUrl", "DREAMAPP_USER_LOOKUP_API_URL")
val userSearchBaseUrl = endpoint("appmobile.api.userSearchBaseUrl", "DREAMAPP_USER_SEARCH_API_URL")
val userRegistrationBaseUrl = endpoint("appmobile.api.userRegistrationBaseUrl", "DREAMAPP_USER_REGISTRATION_API_URL")
val releaseStoreFile = providers.gradleProperty("RELEASE_STORE_FILE")
    .orElse(providers.environmentVariable("RELEASE_STORE_FILE")).orNull
val releaseStorePassword = providers.gradleProperty("RELEASE_STORE_PASSWORD")
    .orElse(providers.environmentVariable("RELEASE_STORE_PASSWORD")).orNull
val releaseKeyAlias = providers.gradleProperty("RELEASE_KEY_ALIAS")
    .orElse(providers.environmentVariable("RELEASE_KEY_ALIAS")).orNull
val releaseKeyPassword = providers.gradleProperty("RELEASE_KEY_PASSWORD")
    .orElse(providers.environmentVariable("RELEASE_KEY_PASSWORD")).orNull
val releaseSigningConfigured = listOf(
    releaseStoreFile,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.example.appmobile"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.appmobile"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Habilitar Database Inspector
        buildConfigField("boolean", "DEBUG_DATABASE", "false")
        buildConfigField("String", "API_BASE_URL", "\"$dreamAppApiUrl\"")
        buildConfigField("String", "WS_BASE_URL", "\"${dreamAppApiUrl.replace("https://", "wss://").replace("http://", "ws://").trimEnd('/')}\"")
        buildConfigField("String", "SLEEP_API_BASE_URL", "\"$sleepApiBaseUrl\"")
        buildConfigField("String", "USER_LOOKUP_API_BASE_URL", "\"$userLookupBaseUrl\"")
        buildConfigField("String", "USER_SEARCH_API_BASE_URL", "\"$userSearchBaseUrl\"")
        buildConfigField("String", "USER_REGISTRATION_API_BASE_URL", "\"$userRegistrationBaseUrl\"")
    }

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.play.services.wearable)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.material.icons.extended.v178)
    // implementation(libs.androidx.ui.desktop)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Debug Tools
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.coil.compose)
    implementation(libs.gson)
    // Room (Database)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    // Retrofit & Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp Logging
    implementation(libs.okhttp.logging.interceptor)

    // Retrofit para API calls

    // Lifecycle ViewModel (Compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // WebSocket support
    implementation(libs.java.websocket)

    // Conectar con el módulo wear
    wearApp(project(":wear"))
}

kapt {
    correctErrorTypes = true
}
