package com.example.notificationlogger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var tvLogs: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val btnNotificationPermission = Button(this).apply {
            text = "1. Permitir Acesso às Notificações"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val btnBatteryPermission = Button(this).apply {
            text = "2. Ignorar Otimização de Bateria"
            setOnClickListener {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:" + packageName)
                }
                startActivity(intent)
            }
        }

        val btnRefresh = Button(this).apply {
            text = "Atualizar Visualização de Logs"
            setOnClickListener {
                loadLogs()
            }
        }

        val btnClear = Button(this).apply {
            text = "Limpar Histórico de Logs"
            setOnClickListener {
                val file = File(filesDir, "notif_logs.txt")
                if (file.exists()) {
                    file.writeText("")
                }
                loadLogs()
            }
        }

        tvLogs = TextView(this).apply {
            text = "Nenhum log carregado."
            textSize = 14f
        }

        val scrollView = ScrollView(this).apply {
            addView(tvLogs)
        }

        layout.addView(btnNotificationPermission)
        layout.addView(btnBatteryPermission)
        layout.addView(btnRefresh)
        layout.addView(btnClear)
        layout.addView(scrollView)

        setContentView(layout)
        loadLogs()
    }

    private fun loadLogs() {
        val file = File(filesDir, "notif_logs.txt")
        if (file.exists()) {
            val content = file.readText()
            tvLogs.text = if (content.isEmpty()) "O arquivo de log está vazio." else content
        } else {
            tvLogs.text = "Nenhum registro encontrado."
        }
    }
}
