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
    private inner class RulerView : View(this@MainActivity) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(panel)

            val cell = dp(65 * zoom).toFloat()
            p.strokeWidth = dp(1).toFloat()
            p.textSize = dp(11).toFloat()
            p.typeface = Typeface.DEFAULT_BOLD

            for (bar in 0..16) {
                val x = bar * cell
                p.color = border
                canvas.drawLine(x, 0f, x, height.toFloat(), p)

                if (bar > 0) {
                    p.color = mutedText
                    canvas.drawText(
                        bar.toString(),
                        x + dp(5),
                        dp(19).toFloat(),
                        p
                    )
                }
            }
        }
    }

    private inner class LaneView(
        private val track: Track
    ) : View(this@MainActivity) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val idx = tracks.indexOf(track)
            canvas.drawColor(
                if (idx == selectedTrack) {
                    Color.rgb(12, 43, 55)
                } else {
                    bg
                }
            )

            val cell = dp(65 * zoom).toFloat()

            for (bar in 0..16) {
                p.color = Color.rgb(24, 48, 69)
                p.strokeWidth = dp(1).toFloat()
                canvas.drawLine(
                    bar * cell, 0f,
                    bar * cell, height.toFloat(), p
                )
            }

            val clips = when (track.name) {
                "Vocal" -> listOf(
                    Clip(1, 6, "Vocal_01", false),
                    Clip(10, 14, "Vocal_02", false)
                )
                "Beat" -> listOf(
                    Clip(1, 11, "Beat_01", false)
                )
                "Keyboard" -> listOf(
                    Clip(1, 8, "Keyboard_01", true),
                    Clip(10, 16, "Keyboard_02", true)
                )
                "Guitar" -> listOf(
                    Clip(1, 7, "Guitar_01", false),
                    Clip(9, 14, "Guitar_02", false)
                )
                "Bass" -> listOf(
                    Clip(1, 10, "Bass_01", false)
                )
                else -> listOf(
                    Clip(1, 6, "${track.name}_01", false)
                )
            }

            clips.forEach { clip ->
                val l = clip.start * cell + dp(3)
                val r = clip.end * cell - dp(3)
                val top = dp(7).toFloat()
                val bottom = height - dp(7).toFloat()

                p.color = track.color
                p.alpha = 220

                canvas.drawRoundRect(
                    RectF(l, top, r, bottom),
                    dp(5).toFloat(),
                    dp(5).toFloat(),
                    p
                )

                p.alpha = 255
                p.color = Color.WHITE
                p.textSize = dp(10).toFloat()
                p.typeface = Typeface.DEFAULT_BOLD

                canvas.drawText(
                    clip.title,
                    l + dp(7),
                    top + dp(13),
                    p
                )

                if (clip.midi) {
                    drawNotes(canvas, l, top, r)
                } else {
                    drawWave(canvas, l, top, r, bottom)
                }
            }

            p.color = border
            canvas.drawLine(
                0f, height - 1f,
                width.toFloat(), height - 1f, p
            )
        }

        private fun drawWave(
            canvas: Canvas,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ) {
            val center = (top + bottom) / 2f
            p.color = Color.WHITE
            p.alpha = 185
            p.strokeWidth = dp(1).toFloat()

            var x = left + dp(5)
            var i = 0
            val step = dp(5).toFloat().coerceAtLeast(1f)

            while (x < right - dp(3)) {
                val amp = dp(
                    4 + ((i * 17 + track.name.length * 13) % 17)
                ).toFloat()

                canvas.drawLine(
                    x, center - amp,
                    x, center + amp, p
                )

                x += step
                i++
            }

            p.alpha = 255
        }

        private fun drawNotes(
            canvas: Canvas,
            left: Float,
            top: Float,
            right: Float
        ) {
            p.color = Color.rgb(213, 186, 255)

            var x = left + dp(8)
            var i = 0

            while (x + dp(15) < right - dp(4)) {
                val y = top + dp(18 + ((i * 7) % 17))

                canvas.drawRoundRect(
                    x, y,
                    x + dp(15), y + dp(3),
                    dp(1).toFloat(),
                    dp(1).toFloat(),
                    p
                )

                x += dp(22)
                i++
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                selectedTrack = tracks.indexOf(track).coerceAtLeast(0)
                statusText.text = "${track.name} selected"
                refreshTracks()
                invalidate()
                return true
            }

            return true
        }
    }

    private fun refreshTracks() {
        if (!::trackList.isInitialized) return

        trackList.removeAllViews()

        tracks.forEachIndexed { index, track ->
            val card = col().apply {
                setPadding(dp(3), dp(2), dp(3), dp(2))
                background = shape(
                    if (index == selectedTrack) {
                        Color.rgb(12, 43, 55)
                    } else {
                        panel
                    },
                    4,
                    track.color
                )
            }

            val top = row()

            top.addView(
                label("${index + 1}", 9f, track.color, true),
                LinearLayout.LayoutParams(dp(20), dp(22))
            )

            top.addView(
                label(track.name, 10f, white, true).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    setOnClickListener {
                        selectedTrack = index
                        statusText.text = "Selected: ${track.name}"
                        refreshTracks()
                    }
                },
                LinearLayout.LayoutParams(0, dp(22), 1f)
            )

            smallButton(top, "M", muteSet.contains(track.name)) {
                if (!muteSet.add(track.name)) {
                    muteSet.remove(track.name)
                }
                refreshTracks()
                refreshMixer()
            }

            smallButton(top, "S", soloSet.contains(track.name)) {
                if (!soloSet.add(track.name)) {
                    soloSet.remove(track.name)
                }
                refreshTracks()
                refreshMixer()
            }

            card.addView(top)

            val vr = row()

            val seek = SeekBar(this).apply {
                max = 100
                progress = track.volume

                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) track.volume = progress
                        }

                        override fun onStartTrackingTouch(
                            seekBar: SeekBar?
                        ) {}

                        override fun onStopTrackingTouch(
                            seekBar: SeekBar?
                        ) {
                            refreshMixer()
                        }
                    }
                )
            }

            vr.addView(
                seek,
                LinearLayout.LayoutParams(0, dp(22), 1f)
            )

            vr.addView(
                label("${track.volume}%", 8f, mutedText),
                LinearLayout.LayoutParams(dp(38), dp(22))
            )

            card.addView(vr)

            trackList.addView(
                card,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(58)
                ).apply {
                    setMargins(dp(1), dp(1), dp(1), dp(1))
                }
            )
        }
    }

    private fun smallButton(
        parent: LinearLayout,
        title: String,
        active: Boolean,
        action: () -> Unit
    ) {
        parent.addView(
            button(
                title,
                if (active) Color.rgb(0, 112, 106)
                else buttonColor,
                action
            ),
            LinearLayout.LayoutParams(dp(26), dp(24)).apply {
                setMargins(dp(1), 0, dp(1), 0)
            }
        )
    }

    private fun buildPiano() {
        pianoPanel = col().apply {
            background = shape(panel, 5)
        }

        val head = row().apply {
            setBackgroundColor(panel)
        }

        head.addView(
            label("♫ KEYBOARD / PIANO ROLL", 12f, cyan, true),
            LinearLayout.LayoutParams(0, dp(34), 1f)
        )

        weightedButton(head, "OCT −", buttonColor, 0.6f) {
            pianoView.octave =
                (pianoView.octave - 1).coerceAtLeast(1)
            pianoView.invalidate()
        }

        weightedButton(head, "OCT +", buttonColor, 0.6f) {
            pianoView.octave =
                (pianoView.octave + 1).coerceAtMost(7)
            pianoView.invalidate()
        }

        weightedButton(head, "CLEAR", buttonColor, 0.6f) {
            notes.clear()
            pianoView.invalidate()
        }

        pianoPanel.addView(head)

        pianoView = PianoView()

        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
        }

        scroll.addView(
            pianoView,
            ViewGroup.LayoutParams(dp(1300), dp(156))
        )

        pianoPanel.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        workspace.addView(
            pianoPanel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(194)
            )
        )
    }

    private inner class PianoView : View(this@MainActivity) {
        private val p = Paint(Paint.ANTI_ALIAS_FLAG)
        var octave = 4

        private val rowHeight get() = dp(13).toFloat()
        private val labelWidth get() = dp(38).toFloat()
        private val keyWidth get() = dp(48).toFloat()

        private val black = setOf(1, 3, 6, 8, 10)

        private val names = listOf(
            "B", "A#", "A", "G#", "G", "F#",
            "F", "E", "D#", "D", "C#", "C"
        )

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(bg)

            for (r in 0 until 12) {
                val top = r * rowHeight
                val bottom = top + rowHeight
                val isBlack = (11 - r) in black

                p.color = if (isBlack) {
                    Color.rgb(25, 38, 55)
                } else {
                    Color.rgb(219, 231, 242)
                }

                canvas.drawRect(
                    0f, top, labelWidth, bottom, p
                )

                p.color = if (isBlack) {
                    Color.rgb(15, 29, 46)
                } else {
                    Color.rgb(8, 23, 39)
                }

                canvas.drawRect(
                    labelWidth, top, width.toFloat(), bottom, p
                )

                p.color = border
                canvas.drawLine(
                    labelWidth, bottom,
                    width.toFloat(), bottom, p
                )

                p.color = if (isBlack) Color.BLACK else Color.DKGRAY
                p.textSize = dp(8).toFloat()

                canvas.drawText(
                    names[r],
                    dp(3).toFloat(),
                    bottom - dp(3),
                    p
                )
            }

            val count =
                ((width - labelWidth) / keyWidth).toInt() + 1

            for (c in 0..count) {
                val x = labelWidth + c * keyWidth
                p.color = if (c % 4 == 0) {
                    Color.rgb(42, 89, 121)
                } else {
                    Color.rgb(22, 49, 73)
                }

                canvas.drawLine(
                    x, 0f, x, height.toFloat(), p
                )
            }

            notes.forEach { (column, r) ->
                val l = labelWidth + column * keyWidth + dp(2)
                val top = r * rowHeight + dp(1)

                p.color = Color.rgb(165, 83, 255)

                canvas.drawRoundRect(
                    l, top,
                    l + keyWidth - dp(4),
                    top + rowHeight - dp(2),
                    dp(2).toFloat(),
                    dp(2).toFloat(),
                    p
                )
            }

            p.color = cyan
            p.strokeWidth = dp(2).toFloat()

            canvas.drawLine(
                labelWidth, 0f,
                labelWidth, height.toFloat(), p
            )
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (event.x < labelWidth) {
                    toast("Piano key — octave $octave")
                    return true
                }

                val c = ((event.x - labelWidth) / keyWidth)
                    .toInt().coerceAtLeast(0)

                val r = (event.y / rowHeight)
                    .toInt().coerceIn(0, 11)

                val note = c to r

                if (!notes.remove(note)) notes.add(note)

                invalidate()
                return true
            }

            return true
        }
    }
