package com.example.notificationlogger

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationService : NotificationListenerService() {

    companion object {
        const val LOG_FILE_NAME = "notif_logs.txt"
    }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val writeMutex = Mutex()

    override fun onListenerConnected() {
        super.onListenerConnected()
        appendLog("[SISTEMA] Listener conectado — monitoramento ativo.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        appendLog("[SISTEMA] Listener desconectado.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName ?: "Desconhecido"
        val extras = sbn.notification?.extras
        val title = extras?.getString("android.title") ?: "Sem Título"
        val text = extras?.getCharSequence("android.text")?.toString() ?: "Sem Conteúdo"
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(sbn.postTime))

        val logLine = "[$timestamp] $packageName\n  ↳ $title: $text\n"
        appendLog(logLine)
    }

    private fun appendLog(line: String) {
        scope.launch {
            writeMutex.withLock {
                try {
                    File(filesDir, LOG_FILE_NAME).appendText(line)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
