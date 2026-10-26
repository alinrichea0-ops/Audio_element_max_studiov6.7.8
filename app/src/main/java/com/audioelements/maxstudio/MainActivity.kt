package com.audioelements.maxstudio

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val bgColor = Color.rgb(18, 20, 30)
    private val accentColor = Color.rgb(0, 220, 180)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(bgColor)
        }

        val title = TextView(this).apply {
            text = "AUDIO ELEMENTS\nMAX STUDIO"
            textSize = 27f
            setTextColor(accentColor)
            gravity = Gravity.CENTER
        }

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val subtitle = TextView(this).apply {
            text = "Version 6.7.8 Beta • Audio Workstation"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 32)
        }
        root.addView(subtitle)

        addButton(root, "🎙  VOCAL RECORDING")
        addButton(root, "🎚  PARAMETRIC EQ")
        addButton(root, "🎛  COMPRESSOR")
        addButton(root, "🌊  REVERB & DELAY")
        addButton(root, "🎹  PIANO ROLL")
        addButton(root, "🎵  MULTI-TRACK")
        addButton(root, "📤  EXPORT AUDIO")

        val note = TextView(this).apply {
            text = "BETA VERSION • More audio tools coming soon"
            textSize = 11f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 0)
        }
        root.addView(note)

        setContentView(root)
    }

    private fun addButton(parent: LinearLayout, label: String) {
        val button = Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(42, 48, 65))
            isAllCaps = false
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            56
        ).apply {
            bottomMargin = 10
        }

        parent.addView(button, params)
    }
}
