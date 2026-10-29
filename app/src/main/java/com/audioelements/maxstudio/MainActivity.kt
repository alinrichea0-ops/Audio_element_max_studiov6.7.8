package com.audioelements.maxstudio

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val bg = Color.rgb(5, 15, 27)
    private val panel = Color.rgb(11, 27, 44)
    private val panel2 = Color.rgb(17, 39, 61)
    private val buttonColor = Color.rgb(22, 45, 69)
    private val border = Color.rgb(38, 70, 96)
    private val cyan = Color.rgb(0, 239, 204)
    private val white = Color.rgb(235, 245, 255)
    private val mutedText = Color.rgb(151, 177, 199)
    private val red = Color.rgb(213, 35, 69)

    private lateinit var root: LinearLayout
    private lateinit var workspace: LinearLayout
    private lateinit var trackList: LinearLayout
    private lateinit var mixerPanel: LinearLayout
    private lateinit var mixerChannels: LinearLayout
    private lateinit var pianoPanel: LinearLayout
    private lateinit var pianoView: PianoView
    private lateinit var statusText: TextView
    private lateinit var clockText: TextView

    private var mixerShown = true
    private var pianoShown = false
    private var zoom = 1
    private var selectedTrack = 0
    private var nextTrack = 6
    private var masterVolume = 80
    private var selectedTool = "SELECT"
    private var importedAudio: Uri? = null

    private val notes = mutableListOf<Pair<Int, Int>>()
    private val muteSet = mutableSetOf<String>()
    private val soloSet = mutableSetOf<String>()

    private data class Track(
        val name: String,
        val color: Int,
        var volume: Int = 75
    )

    private data class Clip(
        val start: Int,
        val end: Int,
        val title: String,
        val midi: Boolean
    )

    private val tracks = mutableListOf(
        Track("Vocal", Color.rgb(0, 218, 145), 72),
        Track("Beat", Color.rgb(0, 125, 255), 78),
        Track("Keyboard", Color.rgb(119, 49, 245), 66),
        Track("Guitar", Color.rgb(255, 145, 17), 70),
        Track("Bass", Color.rgb(244, 35, 91), 74)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildUi()
    }

    private fun dp(v: Int): Int =
        (v * resources.displayMetrics.density).toInt()

    private fun toast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun shape(
        color: Int,
        radius: Int = 8,
        stroke: Int = border
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), stroke)
        }
    }

    private fun row(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isBaselineAligned = false
        }
    }

    private fun col(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isBaselineAligned = false
        }
    }

    private fun label(
        value: String,
        size: Float = 11f,
        color: Int = white,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }

            setPadding(dp(3), dp(2), dp(3), dp(2))
        }
    }

    private fun button(
        value: String,
        color: Int = buttonColor,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = value
            textSize = 10f
            isAllCaps = false
            setTextColor(white)
            background = shape(color, 7)
            minWidth = 0
            minimumWidth = 0
            minHeight = dp(32)
            minimumHeight = dp(32)
            setPadding(dp(3), 0, dp(3), 0)
            setOnClickListener { action() }
        }
    }

    private fun weightedButton(
        parent: LinearLayout,
        title: String,
        color: Int = buttonColor,
        weight: Float = 1f,
        action: () -> Unit
    ) {
        parent.addView(
            button(title, color, action),
            LinearLayout.LayoutParams(0, dp(36), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
        )
    }

    private fun buildUi() {
        root = col().apply {
            setBackgroundColor(bg)
        }

        setContentView(root)

        buildHeader()
        buildTransport()
        buildToolbar()

        workspace = col()

        root.addView(
            workspace,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        buildWorkspace()
        buildPiano()
        buildMixer()

        refreshTracks()
        refreshMixer()
        refreshPanels()
    }

    private fun buildHeader() {
        val h = row().apply {
            setBackgroundColor(panel)
            setPadding(dp(5), dp(2), dp(5), dp(2))
        }

        h.addView(
            label("♫ AUDIO ELEMENTS MAX", 15f, cyan, true).apply {
                gravity = Gravity.CENTER_VERTICAL
            },
            LinearLayout.LayoutParams(0, dp(42), 2.5f)
        )

        weightedButton(h, "▣ PROJECT", buttonColor, 1f) {
            AlertDialog.Builder(this)
                .setTitle("AUDIO ELEMENTS MAX")
                .setMessage("Audio Elements Max Studio")
                .setPositiveButton("OK", null)
                .show()
        }

        weightedButton(h, "↶ UNDO", buttonColor, 0.75f) {
            toast("Tsy mbola misy hetsika azo averina")
        }

        weightedButton(h, "↷ REDO", buttonColor, 0.75f) {
            toast("Tsy mbola misy hetsika azo averina")
        }

        weightedButton(h, "⚙", buttonColor, 0.4f) {
            showSettings()
        }

        root.addView(
            h,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(46)
            )
        )
    }

    private fun showSettings() {
        val options = arrayOf(
            "Arrangement",
            "Piano Roll",
            "Mixer",
            "Import Audio"
        )

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setItems(options) { _, i ->
                when (i) {
                    0 -> {
                        pianoShown = false
                        refreshPanels()
                    }
                    1 -> {
                        pianoShown = true
                        refreshPanels()
                    }
                    2 -> {
                        mixerShown = !mixerShown
                        refreshPanels()
                    }
                    3 -> pickAudio()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    private fun buildTransport() {
        val bar = row().apply {
            setPadding(dp(4), dp(2), dp(4), dp(2))
            setBackgroundColor(bg)
        }

        weightedButton(bar, "● REC", red, 0.85f) {
            toast("REC mbola mila audio recording engine")
        }

        weightedButton(
            bar, "▶ PLAY", Color.rgb(0, 139, 104), 0.9f
        ) {
            toast("PLAY mbola mila audio playback engine")
        }

        weightedButton(bar, "Ⅱ PAUSE", buttonColor, 0.85f) {
            toast("Pause")
        }

        weightedButton(bar, "■ STOP", buttonColor, 0.8f) {
            clockText.text = "00:00:00"
        }

        clockText = label("00:00:00", 16f, cyan, true).apply {
            background = shape(Color.rgb(2, 10, 20), 7)
        }

        bar.addView(
            clockText,
            LinearLayout.LayoutParams(0, dp(38), 1.2f)
        )

        weightedButton(bar, "+ TRACK", cyan, 0.85f) {
            addTrack()
        }

        weightedButton(bar, "☷ MIXER", buttonColor, 0.8f) {
            mixerShown = !mixerShown
            refreshPanels()
        }

        weightedButton(bar, "♫ PIANO", buttonColor, 0.75f) {
            pianoShown = !pianoShown
            refreshPanels()
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(44)
            )
        )
    }

    private fun buildToolbar() {
        val bar = row().apply {
            setBackgroundColor(panel)
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }

        listOf(
            "SELECT", "CUT", "COPY", "PASTE",
            "MOVE", "DELETE", "ZOOM +", "ZOOM -"
        ).forEach { tool ->
            weightedButton(
                bar,
                tool,
                if (tool == selectedTool) {
                    Color.rgb(0, 112, 112)
                } else {
                    buttonColor
                },
                1f
            ) {
                useTool(tool)
            }
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )
    }

    private fun buildWorkspace() {
        val main = row().apply {
            gravity = Gravity.TOP
            setBackgroundColor(bg)
        }

        val left = col().apply {
            background = shape(panel, 5)
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }

        left.addView(
            label("TRACKS", 13f, cyan, true),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        statusText = label("Ready", 9f, mutedText)

        left.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(20)
            )
        )

        val trackScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
        }

        trackList = col()

        trackScroll.addView(
            trackList,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        left.addView(
            trackScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        left.addView(
            button("+ ADD TRACK", buttonColor) {
                addTrack()
            },
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(34)
            )
        )

        main.addView(
            left,
            LinearLayout.LayoutParams(
                dp(215),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val timeline = col().apply {
            setBackgroundColor(bg)
        }

        timeline.addView(
            label("ARRANGEMENT / MULTI-TRACK", 12f, cyan, true),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
        }

        val content = col()

        content.addView(
            RulerView(),
            LinearLayout.LayoutParams(dp(1050 * zoom), dp(30))
        )

        val lanesScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
        }

        val lanes = col()

        tracks.forEach { t ->
            lanes.addView(
                LaneView(t),
                LinearLayout.LayoutParams(dp(1050 * zoom), dp(58))
            )
        }

        lanesScroll.addView(
            lanes,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            lanesScroll,
            LinearLayout.LayoutParams(dp(1050 * zoom), 0, 1f)
        )

        horizontal.addView(
            content,
            ViewGroup.LayoutParams(
                dp(1050 * zoom),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        timeline.addView(
            horizontal,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        main.addView(
            timeline,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        workspace.addView(
            main,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }
