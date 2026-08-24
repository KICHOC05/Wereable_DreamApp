package com.example.dashboardapp.domain.usecase.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportDatabaseUseCase @Inject constructor() {

    suspend fun execute(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Ruta de la base de datos interna
            val internalDbPath = context.getDatabasePath("dashboardapp_db").absolutePath
            val internalDbFile = File(internalDbPath)
            
            if (!internalDbFile.exists()) {
                return@withContext Result.failure(Exception("Base de datos no encontrada"))
            }

            // Keep the export inside the app sandbox. Sharing must go through a
            // controlled FileProvider instead of public external storage.
            val appDir = File(context.cacheDir, "exports")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            // Crear nombre de archivo con timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val exportedDbFile = File(appDir, "dashboardapp_db_$timestamp.db")

            // Copiar el archivo de base de datos
            FileInputStream(internalDbFile).use { input ->
                FileOutputStream(exportedDbFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(exportedDbFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
