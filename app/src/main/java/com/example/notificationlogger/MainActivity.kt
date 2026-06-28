package com.example.notificationlogger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLogs: TextView
    private lateinit var statusCard: LinearLayout
    private val handler = Handler(Looper.getMainLooper())

    private val statusChecker = object : Runnable {
        override fun run() {
            updateStatus()
            handler.postDelayed(this, 1500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // Header
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF1976D2.toInt())
            setPadding(48, 56, 48, 32)
        }
        val tvTitle = TextView(this).apply {
            text = "🔔 Notification Logger"
            textSize = 22f
            setTextColor(0xFFFFFFFF.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val tvSubtitle = TextView(this).apply {
            text = "Captura e registra notificações do sistema"
            textSize = 13f
            setTextColor(0xCCFFFFFF.toInt())
        }
        header.addView(tvTitle)
        header.addView(tvSubtitle)

        // Status card
        statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(32, 24, 32, 24)
            gravity = Gravity.CENTER_VERTICAL
            elevation = 4f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(24, 24, 24, 0)
            layoutParams = params
        }
        val statusDot = TextView(this).apply {
            text = "●"
            textSize = 18f
            tag = "dot"
        }
        val statusTexts = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            p.setMargins(16, 0, 0, 0)
            layoutParams = p
        }
        val tvStatusLabel = TextView(this).apply {
            text = "STATUS DO LISTENER"
            textSize = 10f
            setTextColor(0xFF9E9E9E.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tvStatus = TextView(this).apply {
            text = "Verificando..."
            textSize = 15f
            setTextColor(0xFF212121.toInt())
        }
        statusTexts.addView(tvStatusLabel)
        statusTexts.addView(tvStatus)
        statusCard.addView(statusDot)
        statusCard.addView(statusTexts)

        // Botões
        val btnArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            p.setMargins(24, 16, 24, 0)
            layoutParams = p
        }

        val btnPermission = makeButton("⚙️  Ativar Listener de Notificações", 0xFF1976D2.toInt()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
        val btnBattery = makeButton("🔋  Ignorar Otimização de Bateria", 0xFF455A64.toInt()) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
            )
        }
        val btnRefresh = makeButton("🔄  Atualizar Logs", 0xFF388E3C.toInt()) { loadLogs() }
        val btnClear = makeButton("🗑️  Limpar Logs", 0xFFC62828.toInt()) {
            File(filesDir, NotificationService.LOG_FILE_NAME).writeText("")
            loadLogs()
        }

        btnArea.addView(btnPermission)
        btnArea.addView(space(12))
        btnArea.addView(btnBattery)
        btnArea.addView(space(12))
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val btnRefreshParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        btnRefreshParams.setMargins(0, 0, 8, 0)
        btnRefresh.layoutParams = btnRefreshParams
        val btnClearParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        btnClear.layoutParams = btnClearParams
        row.addView(btnRefresh)
        row.addView(btnClear)
        btnArea.addView(row)

        // Log card
        val logCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF212121.toInt())
            setPadding(24, 20, 24, 20)
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0, 1f
            )
            p.setMargins(24, 16, 24, 24)
            layoutParams = p
        }
        val tvLogLabel = TextView(this).apply {
            text = "REGISTROS"
            textSize = 10f
            setTextColor(0xFF64FFDA.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        tvLogs = TextView(this).apply {
            text = "Nenhum registro."
            textSize = 12f
            setTextColor(0xFFE0E0E0.toInt())
            typeface = android.graphics.Typeface.MONOSPACE
        }
        val scrollLogs = ScrollView(this).apply {
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            p.topMargin = 12
            layoutParams = p
        }
        scrollLogs.addView(tvLogs)
        logCard.addView(tvLogLabel)
        logCard.addView(scrollLogs)

        root.addView(header)
        root.addView(statusCard)
        root.addView(btnArea)
        root.addView(logCard)
        setContentView(root)

        loadLogs()
    }

    override fun onResume() {
        super.onResume()
        handler.post(statusChecker)
        loadLogs()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(statusChecker)
    }

    private fun updateStatus() {
        val enabled = isNotificationListenerEnabled()
        val dot = statusCard.findViewWithTag<TextView>("dot")
        if (enabled) {
            tvStatus.text = "Ativo — capturando notificações"
            tvStatus.setTextColor(0xFF388E3C.toInt())
            dot?.setTextColor(0xFF4CAF50.toInt())
        } else {
            tvStatus.text = "Inativo — permissão não concedida"
            tvStatus.setTextColor(0xFFC62828.toInt())
            dot?.setTextColor(0xFFF44336.toInt())
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            contentResolver, "enabled_notification_listeners"
        ) ?: return false
        return enabledListeners.split(":").any { it.contains(packageName, ignoreCase = true) }
    }

    private fun loadLogs() {
        val file = File(filesDir, NotificationService.LOG_FILE_NAME)
        tvLogs.text = when {
            !file.exists() -> "Nenhum registro ainda.\n\nActive o listener e aguarde notificações."
            file.readText().isBlank() -> "Arquivo de log vazio."
            else -> file.readText().trimEnd()
        }
    }

    private fun makeButton(label: String, color: Int, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setBackgroundColor(color)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(32, 20, 32, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            isAllCaps = false
            setOnClickListener { action() }
        }
    }

    private fun space(dp: Int): android.view.View {
        return android.view.View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp)
        }
    }
}
