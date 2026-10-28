package com.audioelements.maxstudio

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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

    private lateinit var root: LinearLayout
    private lateinit var trackContainer: LinearLayout
    private lateinit var mixerContainer: LinearLayout
    private lateinit var pianoContainer: LinearLayout
    private lateinit var pianoNotes: PianoRollView
    private lateinit var statusText: TextView
    private lateinit var timerText: TextView

    private var mixerVisible = true
    private var pianoVisible = true
    private var selectedTrack = 0
    private var trackCounter = 5
    private var zoom = 1

    private val tracks = mutableListOf(
        Track("Vocal", Color.rgb(0, 210, 150)),
        Track("Beat", Color.rgb(0, 135, 255)),
        Track("Keyboard", Color.rgb(126, 48, 255)),
        Track("Guitar", Color.rgb(255, 145, 20)),
        Track("Bass", Color.rgb(255, 35, 90))
    )

    private val bg = Color.rgb(7, 17, 30)
    private val panel = Color.rgb(12, 29, 48)
    private val panelLight = Color.rgb(22, 43, 66)
    private val border = Color.rgb(38, 66, 92)
    private val accent = Color.rgb(0, 240, 211)
    private val white = Color.rgb(235, 245, 255)
    private val muted = Color.rgb(144, 171, 196)
    private val red = Color.rgb(230, 34, 70)

    data class Track(
        val name: String,
        val color: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildInterface()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

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

    private fun row(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isBaselineAligned = false
        }

    private fun column(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isBaselineAligned = false
        }

    private fun text(
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
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }
    }

    private fun button(
        value: String,
        color: Int = panelLight,
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
            minHeight = dp(34)
            minimumHeight = dp(34)
            setPadding(dp(4), 0, dp(4), 0)
            setOnClickListener { action() }
        }
    }

    private fun addButton(
        parent: LinearLayout,
        value: String,
        color: Int = panelLight,
        weight: Float = 1f,
        action: () -> Unit
    ) {
        parent.addView(
            button(value, color, action),
            LinearLayout.LayoutParams(
                0, dp(37), weight
            ).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
        )
    }

    private fun toast(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
    }

    private fun buildInterface() {
        root = column().apply { setBackgroundColor(bg) }
        setContentView(root)

        buildHeader()
        buildTransport()
        buildTools()

        val main = column()
        root.addView(
            main,
            LinearLayout.LayoutParams( -1, 0, 1f )
        )

        buildArrangement(main)
        buildPianoRoll(main)
        buildMixer()
    }

    private fun buildHeader() {
        val header = row().apply {
            setPadding(dp(5), dp(2), dp(5), dp(2))
            setBackgroundColor(panel)
        }

        header.addView(
            text("♫ AUDIO ELEMENTS MAX", 15f, accent, true),
            LinearLayout.LayoutParams(0, dp(43), 2.5f)
        )

        addButton(header, "PROJECT", panelLight, 1f) {
            toast("Audio Elements Max Studio")
        }

        addButton(header, "UNDO", panelLight, 0.7f) {
            toast("Undo mbola tsy ampidirina")
        }

        addButton(header, "REDO", panelLight, 0.7f) {
            toast("Redo mbola tsy ampidirina")
        }

        addButton(header, "⚙", panelLight, 0.45f) {
            toast("Settings mbola hatsaraina")
        }

        root.addView(header, LinearLayout.LayoutParams(-1, dp(46)))
    }

    private fun buildTransport() {
        val bar = row().apply {
            setPadding(dp(5), dp(2), dp(5), dp(2))
        }

        addButton(bar, "● REC", red, 0.9f) {
            toast("REC: mila audio engine")
        }

        addButton(bar, "▶ PLAY", Color.rgb(0, 135, 105), 1f) {
            toast("PLAY: mila audio engine")
        }

        addButton(bar, "Ⅱ PAUSE", panelLight, 1f) {
            toast("PAUSE mbola tsy ampidirina")
        }

        addButton(bar, "■ STOP", panelLight, 0.9f) {
            timerText.text = "00:00:00"
            toast("STOP")
        }

        timerText = text("00:00:00", 17f, accent, true)
        timerText.background = shape(Color.rgb(3, 13, 24), 7)

        bar.addView(
            timerText,
            LinearLayout.LayoutParams(0, dp(39), 1.5f)
        )

        addButton(bar, "+ TRACK", accent, 1f) { addTrack() }

        addButton(bar, "MIXER", panelLight, 0.9f) {
            mixerVisible = !mixerVisible
            mixerContainer.visibility =
                if (mixerVisible) View.VISIBLE else View.GONE
        }

        root.addView(bar, LinearLayout.LayoutParams(-1, dp(45)))
    }

    private fun buildTools() {
        val tools = row().apply {
            setBackgroundColor(panel)
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }

        listOf(
            "SELECT", "CUT", "COPY", "PASTE",
            "MOVE", "DELETE", "ZOOM +", "ZOOM -"
        ).forEach { name ->
            addButton(
                tools,
                name,
                if (name == "SELECT") Color.rgb(0, 130, 125)
                else panelLight,
                1f
            ) {
                handleTool(name)
            }
        }

        root.addView(tools, LinearLayout.LayoutParams(-1, dp(43)))
    }

    private fun buildArrangement(parent: LinearLayout) {
        val area = row().apply {
            gravity = Gravity.TOP
        }

        val sidebar = column().apply {
            setBackgroundColor(panel)
        }

        sidebar.addView(
            text("TRACKS", 12f, accent, true),
            LinearLayout.LayoutParams(-1, dp(32))
        )

        statusText = text("Ready", 9f, muted)
        sidebar.addView(
            statusText,
            LinearLayout.LayoutParams(-1, dp(23))
        )

        sidebar.addView(
            button("+ ADD TRACK", panelLight) { addTrack() },
            LinearLayout.LayoutParams(-1, dp(35))
        )

        sidebar.addView(
            button("KEYBOARD / PIANO", panelLight) {
                pianoVisible = !pianoVisible
                pianoContainer.visibility =
                    if (pianoVisible) View.VISIBLE else View.GONE
            },
            LinearLayout.LayoutParams(-1, dp(35))
        )

        area.addView(
            sidebar,
            LinearLayout.LayoutParams(dp(150), -1)
        )

        val timelineArea = column().apply {
            setBackgroundColor(bg)
        }

        timelineArea.addView(
            text("ARRANGEMENT / MULTI-TRACK", 12f, accent, true),
            LinearLayout.LayoutParams(-1, dp(32))
        )

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            setBackgroundColor(panel)
        }

        val timeline = column()

        val ruler = row()
        for (bar in 1..32) {
            ruler.addView(
                text(bar.toString(), 10f, muted, true),
                LinearLayout.LayoutParams(dp(70 * zoom), dp(26))
            )
        }
        timeline.addView(ruler)

        trackContainer = column()

        val vertical = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = true
        }

        vertical.addView(
            trackContainer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        timeline.addView(
            vertical,
            LinearLayout.LayoutParams(-1, dp(255))
        )

        horizontal.addView(
            timeline,
            ViewGroup.LayoutParams(
                dp(70 * zoom * 32),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        timelineArea.addView(
            horizontal,
            LinearLayout.LayoutParams(-1, -1)
        )

        area.addView(
            timelineArea,
            LinearLayout.LayoutParams(0, -1, 1f)
        )

        parent.addView(
            area,
            LinearLayout.LayoutParams(-1, 0, 1.6f)
        )

        refreshTracks()
    }

    private fun refreshTracks() {
        if (!::trackContainer.isInitialized) return

        trackContainer.removeAllViews()

        tracks.forEachIndexed { index, track ->
            val lane = row().apply {
                setBackgroundColor(
                    if (index == selectedTrack) {
                        Color.rgb(19, 49, 61)
                    } else {
                        bg
                    }
                )
            }

            val name = text(
                track.name,
                10f,
                if (index == selectedTrack) accent else white,
                true
            )

            name.setOnClickListener {
                selectedTrack = index
                statusText.text = "Selected: ${track.name}"
                refreshTracks()
            }

            lane.addView(
                name,
                LinearLayout.LayoutParams(dp(82), dp(52))
            )

            val clip = TextView(this).apply {
                text = when (track.name) {
                    "Vocal" -> "Vocal_01     ∿ ∿ ∿ ∿ ∿ ∿ ∿"
                    "Beat" -> "Beat_01      ▥ ▥ ▥ ▥ ▥ ▥"
                    "Keyboard" -> "Keyboard_01   ▰ ▱ ▰ ▱ ▰ ▱"
                    "Guitar" -> "Guitar_01     ∿ ∿ ∿ ∿ ∿"
                    "Bass" -> "Bass_01       ∿ ∿ ∿ ∿ ∿ ∿"
                    else -> "${track.name}_01"
                }

                textSize = 10f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(7), dp(3), dp(7), dp(3))
                background = shape(track.color, 5, track.color)

                setOnClickListener {
                    selectedTrack = index
                    statusText.text = "${track.name} clip selected"
                    refreshTracks()
                }
            }

            lane.addView(
                clip,
                LinearLayout.LayoutParams(
                    dp(if (index == 1) 380 else 280),
                    dp(40)
                ).apply {
                    setMargins(dp(3), dp(4), dp(3), dp(4))
                }
            )

            trackContainer.addView(
                lane,
                LinearLayout.LayoutParams(-1, dp(53))
            )

            trackContainer.addView(
                View(this).apply {
                    setBackgroundColor(border)
                },
                LinearLayout.LayoutParams(-1, dp(1))
            )
        }
    }

    private fun buildPianoRoll(parent: LinearLayout) {
        pianoContainer = column().apply {
            setBackgroundColor(panel)
            background = shape(panel, 6)
        }

        val header = row()

        header.addView(
            text("♬ KEYBOARD / PIANO ROLL", 11f, accent, true),
            LinearLayout.LayoutParams(0, dp(29), 1f)
        )

        addButton(header, "OCT −", panelLight, 0.7f) {
            pianoNotes.octave = (pianoNotes.octave - 1).coerceAtLeast(1)
            pianoNotes.invalidate()
        }

        addButton(header, "OCT +", panelLight, 0.7f) {
            pianoNotes.octave = (pianoNotes.octave + 1).coerceAtMost(7)
            pianoNotes.invalidate()
        }

        pianoContainer.addView(header)

        pianoNotes = PianoRollView()
        pianoContainer.addView(
            pianoNotes,
            LinearLayout.LayoutParams(-1, dp(110))
        )

        parent.addView(
            pianoContainer,
            LinearLayout.LayoutParams(-1, 0, 0.85f)
        )
    }

    private inner class PianoRollView : View(this@MainActivity) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val notePositions = mutableListOf<Pair<Int, Int>>()

        var octave = 4

        private val blackKeys = setOf(1, 3, 6, 8, 10)

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val keyWidth = dp(43).toFloat()
            val labelWidth = dp(36).toFloat()
            val rowHeight = dp(13).toFloat()
            val availableWidth = width - labelWidth

            canvas.drawColor(Color.rgb(7, 18, 32))

            for (note in 0 until 12) {
                val y = note * rowHeight
                val pitch = (11 - note)

                paint.color = if (pitch % 12 in blackKeys) {
                    Color.rgb(25, 36, 51)
                } else {
                    Color.rgb(210, 222, 233)
                }

                canvas.drawRect(
                    0f, y, labelWidth, y + rowHeight, paint
                )

                paint.color = Color.rgb(20, 37, 56)
                canvas.drawRect(
                    labelWidth,
                    y,
                    width.toFloat(),
                    y + rowHeight,
                    paint
                )

                paint.color = Color.rgb(45, 67, 91)
                canvas.drawLine(
                    0f, y + rowHeight,
                    width.toFloat(), y + rowHeight,
                    paint
                )
            }

            val columns = (availableWidth / keyWidth).toInt() + 1

            for (column in 0..columns) {
                val x = labelWidth + column * keyWidth

                paint.color = if (column % 4 == 0) {
                    Color.rgb(48, 74, 100)
                } else {
                    Color.rgb(27, 46, 68)
                }

                canvas.drawLine(
                    x, 0f, x, height.toFloat(), paint
                )
            }

            paint.color = Color.rgb(161, 84, 255)

            notePositions.forEach { (column, row) ->
                val left = labelWidth + column * keyWidth + dp(2)
                val top = row * rowHeight + dp(1)

                canvas.drawRoundRect(
                    left,
                    top,
                    left + keyWidth - dp(4),
                    top + rowHeight - dp(2),
                    dp(2).toFloat(),
                    dp(2).toFloat(),
                    paint
                )
            }

            paint.color = accent
            paint.strokeWidth = dp(2).toFloat()
            canvas.drawLine(
                labelWidth, 0f,
                labelWidth, height.toFloat(), paint
            )
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val labelWidth = dp(36).toFloat()
                val keyWidth = dp(43).toFloat()
                val rowHeight = dp(13).toFloat()

                if (event.x >= labelWidth) {
                    val column =
                        ((event.x - labelWidth) / keyWidth).toInt()
                    val noteRow = (event.y / rowHeight).toInt()
                        .coerceIn(0, 11)

                    notePositions.add(column to noteRow)
                    invalidate()

                    toast(
                        "Naoty nampidirina: octave $octave"
                    )
                } else {
                    val noteRow = (event.y / rowHeight).toInt()
                        .coerceIn(0, 11)

                    val noteNames = listOf(
                        "B", "A#", "A", "G#", "G", "F#",
                        "F", "E", "D#", "D", "C#", "C"
                    )

                    toast(
                        "Lakile ${noteNames[noteRow]} - octave $octave"
                    )
                }

                return true
            }

            return true
        }
    }

    private fun buildMixer() {
        mixerContainer = row().apply {
            setBackgroundColor(panel)
            gravity = Gravity.CENTER_VERTICAL
        }

        root.addView(
            mixerContainer,
            LinearLayout.LayoutParams(-1, dp(103))
        )

        refreshMixer()
    }

    private fun refreshMixer() {
        if (!::mixerContainer.isInitialized) return

        mixerContainer.removeAllViews()

        mixerContainer.addView(
            text("♫ MIXER", 12f, accent, true),
            LinearLayout.LayoutParams(dp(75), -1)
        )

        tracks.forEachIndexed { index, track ->
            val channel = column().apply {
                gravity = Gravity.CENTER
                background = shape(
                    panelLight, 5, border
                )
                setPadding(dp(2), dp(2), dp(2), dp(2))
            }

            channel.addView(
                text(track.name, 10f, track.color, true),
                LinearLayout.LayoutParams(-1, dp(21))
            )

            val controls = row()

            addButton(controls, "M", panel, 1f) {
                toast("${track.name}: Mute mbola tsy ampidirina")
            }

            addButton(controls, "S", panel, 1f) {
                toast("${track.name}: Solo mbola tsy ampidirina")
            }

            channel.addView(controls)

            val volume = SeekBar(this).apply {
                max = 100
                progress = 75
                contentDescription = "${track.name} volume"
            }

            channel.addView(
                volume,
                LinearLayout.LayoutParams(
                    dp(85), dp(26)
                )
            )

            channel.addView(
                text("VOL", 8f, muted),
                LinearLayout.LayoutParams(-1, dp(15))
            )

            mixerContainer.addView(
                channel,
                LinearLayout.LayoutParams(0, -1, 1f).apply {
                    setMargins(dp(2), dp(2), dp(2), dp(2))
                }
            )
        }

        val master = column().apply {
            gravity = Gravity.CENTER
            background = shape(Color.rgb(5, 49, 56), 5, accent)
        }

        master.addView(
            text("MASTER", 10f, accent, true),
            LinearLayout.LayoutParams(-1, dp(22))
        )

        val masterVolume = SeekBar(this).apply {
            max = 100
            progress = 85
            contentDescription = "Master volume"
        }

        master.addView(
            masterVolume,
            LinearLayout.LayoutParams(dp(82), dp(30))
        )

        master.addView(
            text("OUTPUT", 8f, muted),
            LinearLayout.LayoutParams(-1, dp(15))
        )

        mixerContainer.addView(
            master,
            LinearLayout.LayoutParams(dp(100), -1).apply {
                setMargins(dp(2), dp(2), dp(3), dp(2))
            }
        )
    }

    private fun addTrack() {
        trackCounter++

        tracks.add(
            Track(
                "Audio $trackCounter",
                Color.rgb(
                    60 + (trackCounter * 17) % 180,
                    100 + (trackCounter * 23) % 130,
                    140 + (trackCounter * 11) % 100
                )
            )
        )

        selectedTrack = tracks.lastIndex

        refreshTracks()
        refreshMixer()

        statusText.text = "Track added: ${tracks.last().name}"
    }

    private fun handleTool(tool: String) {
        when (tool) {
            "SELECT" -> toast("Safidio ny Track na Clip")

            "CUT" -> toast("CUT mila audio editing engine")

            "COPY" -> toast("COPY mbola tsy ampidirina")

            "PASTE" -> toast("PASTE mbola tsy ampidirina")

            "MOVE" -> toast("MOVE mbola tsy ampidirina")

            "DELETE" -> {
                if (selectedTrack in tracks.indices) {
                    val removed = tracks.removeAt(selectedTrack)

                    selectedTrack =
                        if (tracks.isEmpty()) -1
                        else selectedTrack.coerceAtMost(tracks.lastIndex)

                    refreshTracks()
                    refreshMixer()

                    statusText.text = "Deleted: ${removed.name}"
                } else {
                    toast("Tsy misy piste hofafana")
                }
            }

            "ZOOM +" -> {
                zoom = (zoom + 1).coerceAtMost(3)
                toast("Zoom: $zoom")
            }

            "ZOOM -" -> {
                zoom = (zoom - 1).coerceAtLeast(1)
                toast("Zoom: $zoom")
            }
        }
    }
}
