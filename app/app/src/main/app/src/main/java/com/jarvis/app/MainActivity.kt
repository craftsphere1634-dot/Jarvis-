package com.jarvis.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("jarvis", MODE_PRIVATE)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }

        fun label(t: String) = TextView(this).apply { text = t; textSize = 16f; setPadding(0, pad / 2, 0, pad / 2) }
        fun btn(t: String, f: () -> Unit) = Button(this).apply { text = t; setOnClickListener { f() } }

        val key = EditText(this).apply {
            hint = "Anthropic API key (sk-ant-...)"
            setText(prefs.getString("key", ""))
            setSingleLine()
        }

        root.addView(label("JARVIS"))
        root.addView(key)
        root.addView(btn("1. Mic & notification permission") {
            val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33) perms.add(Manifest.permission.POST_NOTIFICATIONS)
            requestPermissions(perms.toTypedArray(), 1)
        })
        root.addView(btn("2. Display over other apps allow") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        root.addView(btn("3. Start Jarvis") {
            prefs.edit().putString("key", key.text.toString().trim()).apply()
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Pehle step 1 karo", Toast.LENGTH_LONG).show()
            } else {
                startForegroundService(Intent(this, JarvisService::class.java))
                Toast.makeText(this, "Jarvis chalu. Ab \"Jarvis\" bolo", Toast.LENGTH_LONG).show()
            }
        })
        root.addView(btn("Stop Jarvis") { stopService(Intent(this, JarvisService::class.java)) })
        setContentView(root)
    }
}
