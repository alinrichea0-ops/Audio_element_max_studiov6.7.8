package com.audioelements.maxstudio

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
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

    // ---------------------------------------------------------
    // MAIN UI
    // ---------------------------------------------------------

    private lateinit var root: LinearLayout
    private lateinit var header: LinearLayout
    private lateinit var transport: LinearLayout
    private lateinit var toolbar: LinearLayout

    private lateinit var workspace: LinearLayout
    private lateinit var trackPanel: LinearLayout
    private lateinit var timelinePanel: LinearLayout
    private lateinit var trackRows: LinearLayout

    private lateinit var pianoPanel: LinearLayout
    private lateinit var pianoRoll: PianoRollView

    private lateinit var mixerPanel: LinearLayout
    private lateinit var mixerChannels: LinearLayout

    private lateinit var statusLabel: TextView
    private lateinit var clockLabel: TextView

    // ---------------------------------------------------------
    // APP STATE
    // ---------------------------------------------------------

    private var mixerVisible = true
    private var pianoVisible = false

    private var selectedTrack = 0
    private var nextTrackNumber = 6

    private var zoomLevel = 1
    private var selectedTool = "SELECT"

    private val tracks = mutableListOf(
        Track("Vocal", Color.rgb(0, 224, 155), 70),
        Track("Beat", Color.rgb(0, 125, 255), 75),
        Track("Keyboard", Color.rgb(126, 45, 255), 65),
        Track("Guitar", Color.rgb(255, 150, 10), 68),
        Track("Bass", Color.rgb(255, 35, 95), 72)
    )

    private val bg = Color.rgb(5, 16, 29)
    private val panel = Color.rgb(11, 29, 48)
    private val panel2 = Color.rgb(15, 39, 63)
    private val buttonBg = Color.rgb(19, 43, 69)
    private val border = Color.rgb(31, 77, 112)
    private val accent = Color.rgb(0, 245, 211)
    private val white = Color.rgb(235, 246, 255)
    private val muted = Color.rgb(148, 179, 205)
    private val red = Color.rgb(231, 26, 69)
    private val green = Color.rgb(0, 190, 137)

    private val trackVolume = mutableMapOf<String, Int>()
    private val mutedTracks = mutableSetOf<String>()
    private val soloTracks = mutableSetOf<String>()

    private var masterVolume = 80

    private var chosenAudioUri: Uri? = null

    private val noteEvents = mutableListOf<NoteEvent>()

    private data class Track(
        val name: String,
        val color: Int,
        var volume: Int
    )

    private data class NoteEvent(
        val column: Int,
        val row: Int,
        val length: Int = 1
    )

    // ---------------------------------------------------------
    // ACTIVITY
    // ---------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.statusBarColor = bg
        window.navigationBarColor = bg

        for (track in tracks) {
            trackVolume[track.name] = track.volume
        }

        buildApplication()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    // ---------------------------------------------------------
    // DRAWABLES AND TEXT
    // ---------------------------------------------------------

    private fun rounded(
        color: Int,
        radius: Int = 8,
        strokeColor: Int = border
    ): GradientDrawable {

        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            setStroke(dp(1), strokeColor)
        }
    }

    private fun makeRow(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            isBaselineAligned = false
        }
    }

    private fun makeColumn(): LinearLayout {

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            isBaselineAligned = false
        }
    }

    private fun makeText(
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

            setPadding(
                dp(3),
                dp(2),
                dp(3),
                dp(2)
            )
        }
    }

    private fun makeButton(
        value: String,
        color: Int = buttonBg,
        action: () -> Unit
    ): Button {

        return Button(this).apply {
            text = value
            textSize = 10f
            isAllCaps = false

            setTextColor(white)
            background = rounded(color, 7)

            minWidth = 0
            minimumWidth = 0
            minHeight = dp(32)
            minimumHeight = dp(32)

            setPadding(dp(4), 0, dp(4), 0)

            setOnClickListener {
                action()
            }
        }
    }

    private fun addWeightedButton(
        parent: LinearLayout,
        title: String,
        color: Int = buttonBg,
        weight: Float = 1f,
        action: () -> Unit
    ) {

        parent.addView(
            makeButton(title, color, action),
            LinearLayout.LayoutParams(
                0,
                dp(36),
                weight
            ).apply {
                setMargins(
                    dp(2),
                    dp(2),
                    dp(2),
                    dp(2)
                )
            }
        )
    }

    // ---------------------------------------------------------
    // APPLICATION LAYOUT
    // ---------------------------------------------------------

    private fun buildApplication() {

        root = makeColumn().apply {
            setBackgroundColor(bg)
        }

        setContentView(root)

        buildHeader()
        buildTransport()
        buildToolbar()

        workspace = makeColumn()

        root.addView(
            workspace,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        buildArrangement()
        buildPianoPanel()
        buildMixer()

        refreshTrackRows()
        refreshMixer()
        updatePanels()
    }

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    private fun buildHeader() {

        header = makeRow().apply {
            setBackgroundColor(panel)
            setPadding(dp(5), dp(2), dp(5), dp(2))
        }

        val logo = makeText(
            "♫ AUDIO ELEMENTS MAX",
            15f,
            accent,
            true
        )

        logo.gravity = Gravity.CENTER_VERTICAL

        header.addView(
            logo,
            LinearLayout.LayoutParams(
                0,
                dp(42),
                2.8f
            )
        )

        addWeightedButton(
            header,
            "PROJECT",
            buttonBg,
            1f
        ) {
            toast("Audio Elements Max Studio")
        }

        addWeightedButton(
            header,
            "↶ UNDO",
            buttonBg,
            0.8f
        ) {
            toast("Undo mbola tsy ampifandraisina")
        }

        addWeightedButton(
            header,
            "↷ REDO",
            buttonBg,
            0.8f
        ) {
            toast("Redo mbola tsy ampifandraisina")
        }

        addWeightedButton(
            header,
            "⚙",
            buttonBg,
            0.4f
        ) {
            showSettings()
        }

        root.addView(
            header,
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

        android.app.AlertDialog.Builder(this)
            .setTitle("Audio Elements Max Settings")
            .setItems(options) { _, index ->

                when (index) {
                    0 -> {
                        pianoVisible = false
                        updatePanels()
                    }

                    1 -> {
                        pianoVisible = true
                        updatePanels()
                    }

                    2 -> {
                        mixerVisible = !mixerVisible
                        updatePanels()
                    }

                    3 -> openAudioPicker()
                }
            }
            .setNegativeButton("CLOSE", null)
            .show()
    }

    // ---------------------------------------------------------
    // TRANSPORT BAR
    // ---------------------------------------------------------

    private fun buildTransport() {

        transport = makeRow().apply {
            setPadding(dp(4), dp(2), dp(4), dp(2))
            setBackgroundColor(bg)
        }

        addWeightedButton(
            transport,
            "● REC",
            red,
            0.9f
        ) {
            toast(
                "REC mbola mila recorder sy fahazoan-dalana audio"
            )
        }

        addWeightedButton(
            transport,
            "▶ PLAY",
            Color.rgb(0, 145, 110),
            1f
        ) {
            toast(
                "PLAY mbola mila audio playback engine"
            )
        }

        addWeightedButton(
            transport,
            "Ⅱ PAUSE",
            buttonBg,
            1f
        ) {
            toast("Pause mbola tsy ampifandraisina")
        }

        addWeightedButton(
            transport,
            "■ STOP",
            buttonBg,
            0.9f
        ) {
            clockLabel.text = "00:00:00"
            toast("Stop")
        }

        clockLabel = makeText(
            "00:00:00",
            16f,
            accent,
            true
        ).apply {
            background = rounded(Color.rgb(2, 12, 23), 7)
        }

        transport.addView(
            clockLabel,
            LinearLayout.LayoutParams(
                0,
                dp(38),
                1.35f
            )
        )

        addWeightedButton(
            transport,
            "+ TRACK",
            accent,
            1f
        ) {
            addTrack()
        }

        addWeightedButton(
            transport,
            "☷ MIXER",
            buttonBg,
            0.95f
        ) {
            mixerVisible = !mixerVisible
            updatePanels()
        }

        addWeightedButton(
            transport,
            "PIANO",
            buttonBg,
            0.8f
        ) {
            pianoVisible = !pianoVisible
            updatePanels()
        }

        root.addView(
            transport,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(44)
            )
        )
    }

    // ---------------------------------------------------------
    // EDITING TOOLBAR
    // ---------------------------------------------------------

    private fun buildToolbar() {

        toolbar = makeRow().apply {
            setBackgroundColor(panel)
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }

        val tools = listOf(
            "SELECT",
            "CUT",
            "COPY",
            "PASTE",
            "MOVE",
            "DELETE",
            "ZOOM +",
            "ZOOM -"
        )

        for (tool in tools) {

            addWeightedButton(
                toolbar,
                tool,
                if (tool == selectedTool) {
                    Color.rgb(0, 125, 125)
                } else {
                    buttonBg
                },
                1f
            ) {
                handleTool(tool)
            }
        }

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(42)
            )
        )
    }

    // ---------------------------------------------------------
    // ARRANGEMENT
    // ---------------------------------------------------------

    private fun buildArrangement() {

        val arrangement = makeRow().apply {
            gravity = Gravity.TOP
            setBackgroundColor(bg)
        }

        // LEFT TRACK PANEL

        trackPanel = makeColumn().apply {
            setBackgroundColor(panel)
            background = rounded(panel, 5)
        }

        trackPanel.addView(
            makeText(
                "TRACKS",
                13f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(31)
            )
        )

        statusLabel = makeText(
            "Ready",
            9f,
            muted
        )

        trackPanel.addView(
            statusLabel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(20)
            )
        )

        val trackScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
        }

        trackRows = makeColumn()

        trackScroll.addView(
            trackRows,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        trackPanel.addView(
            trackScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val addTrackButton = makeButton(
            "+ ADD TRACK",
            buttonBg
        ) {
            addTrack()
        }

        trackPanel.addView(
            addTrackButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(34)
            ).apply {
                setMargins(dp(3), dp(2), dp(3), dp(3))
            }
        )

        arrangement.addView(
            trackPanel,
            LinearLayout.LayoutParams(
                dp(210),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // RIGHT TIMELINE PANEL

        timelinePanel = makeColumn().apply {
            setBackgroundColor(bg)
        }

        timelinePanel.addView(
            makeText(
                "ARRANGEMENT / MULTI-TRACK",
                12f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(30)
            )
        )

        val horizontalScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            setBackgroundColor(bg)
        }

        val timelineContent = makeColumn()

        val ruler = TimelineRulerView()

        timelineContent.addView(
            ruler,
            LinearLayout.LayoutParams(
                dp(1050),
                dp(30)
            )
        )

        val laneScroll = ScrollView(this).apply {
            isVerticalScrollBarEnabled = true
            isFillViewport = false
        }

        val laneColumn = makeColumn()

        for (track in tracks) {

            val lane = TimelineLaneView(
                track = track,
                selected = {
                    selectedTrack = tracks.indexOf(track)
                    statusLabel.text = "Selected: ${track.name}"
                    refreshTrackRows()
                }
            )

            laneColumn.addView(
                lane,
                LinearLayout.LayoutParams(
                    dp(1050),
                    dp(57)
                )
            )
        }

        timelineContent.addView(
            laneScroll,
            LinearLayout.LayoutParams(
                dp(1050),
                0,
                1f
            )
        )

        laneScroll.addView(
            laneColumn,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        horizontalScroll.addView(
            timelineContent,
            ViewGroup.LayoutParams(
                dp(1050),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        timelinePanel.addView(
            horizontalScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        arrangement.addView(
            timelinePanel,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        workspace.addView(
            arrangement,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
    }

    // ---------------------------------------------------------
    // TIMELINE RULER CUSTOM VIEW
    // ---------------------------------------------------------

    private inner class TimelineRulerView : View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            canvas.drawColor(panel)

            val cellWidth = dp(65 * zoomLevel).toFloat()

            paint.color = border
            paint.strokeWidth = dp(1).toFloat()

            for (bar in 0..16) {

                val x = bar * cellWidth

                canvas.drawLine(
                    x,
                    0f,
                    x,
                    height.toFloat(),
                    paint
                )

                paint.color = muted
                paint.textSize = dp(11).toFloat()
                paint.typeface = Typeface.DEFAULT_BOLD

                if (bar > 0) {
                    canvas.drawText(
                        bar.toString(),
                        x + dp(5),
                        dp(19).toFloat(),
                        paint
                    )
                }

                paint.color = border
            }
        }
    }

    // ---------------------------------------------------------
    // TIMELINE LANE
    // ---------------------------------------------------------

    private inner class TimelineLaneView(
        private val track: Track,
        private val selected: () -> Unit
    ) : View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            val selectedIndex = tracks.indexOf(track)

            canvas.drawColor(
                if (selectedIndex == selectedTrack) {
                    Color.rgb(12, 45, 58)
                } else {
                    bg
                }
            )

            val cellWidth = dp(65 * zoomLevel).toFloat()

            paint.color = Color.rgb(23, 48, 71)
            paint.strokeWidth = dp(1).toFloat()

            for (bar in 0..16) {
                val x = bar * cellWidth

                canvas.drawLine(
                    x,
                    0f,
                    x,
                    height.toFloat(),
                    paint
                )
            }

            paint.color = track.color

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
                    Clip(1, 5, "${track.name}_01", false)
                )
            }

            for (clip in clips) {

                val left =
                    (clip.start * cellWidth).toFloat() + dp(3)

                val right =
                    (clip.end * cellWidth).toFloat() - dp(3)

                val top = dp(7).toFloat()
                val bottom = height - dp(7).toFloat()

                paint.color = track.color
                paint.alpha = 220

                val rect = RectF(
                    left,
                    top,
                    right,
                    bottom
                )

                canvas.drawRoundRect(
                    rect,
                    dp(5).toFloat(),
                    dp(5).toFloat(),
                    paint
                )

                paint.alpha = 255

                paint.color = Color.WHITE
                paint.textSize = dp(10).toFloat()
                paint.typeface = Typeface.DEFAULT_BOLD

                canvas.drawText(
                    clip.title,
                    left + dp(8),
                    top + dp(13),
                    paint
                )

                if (clip.midi) {
                    drawMidiNotes(
                        canvas,
                        left,
                        top,
                        right,
                        bottom
                    )
                } else {
                    drawWaveform(
                        canvas,
                        left,
                        top,
                        right,
                        bottom,
                        track.color
                    )
                }
            }

            paint.color = Color.rgb(18, 45, 66)
            paint.strokeWidth = dp(1).toFloat()

            canvas.drawLine(
                0f,
                height - dp(1).toFloat(),
                width.toFloat(),
                height - dp(1).toFloat(),
                paint
            )
        }

        private fun drawWaveform(
            canvas: Canvas,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            color: Int
        ) {

            val center = (top + bottom) / 2f

            paint.color = Color.WHITE
            paint.alpha = 180
            paint.strokeWidth = dp(1).toFloat()

            val step = dp(5).toFloat().coerceAtLeast(1f)
            var x = left + dp(5)

            var index = 0

            while (x < right - dp(3)) {

                val wave = (
                    (index * 17 + track.name.length * 13) % 17
                )

                val amplitude =
                    dp(4 + wave).toFloat()

                canvas.drawLine(
                    x,
                    center - amplitude,
                    x,
                    center + amplitude,
                    paint
                )

                x += step
                index++
            }

            paint.alpha = 255
        }

        private fun drawMidiNotes(
            canvas: Canvas,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ) {

            paint.color = Color.rgb(207, 177, 255)

            val noteWidth = dp(15).toFloat()
            val noteHeight = dp(3).toFloat()

            var x = left + dp(8)
            var index = 0

            while (x + noteWidth < right - dp(4)) {

                val y = top + dp(
                    18 + ((index * 7) % 17)
                ).toFloat()

                canvas.drawRoundRect(
                    x,
                    y,
                    x + noteWidth,
                    y + noteHeight,
                    dp(1).toFloat(),
                    dp(1).toFloat(),
                    paint
                )

                x += dp(22)
                index++
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {

            if (event.action == MotionEvent.ACTION_DOWN) {

                selected()
                statusLabel.text = "${track.name} clip selected"

                if (selectedTrack >= 0) {
                    refreshTrackRows()
                }

                return true
            }

            return true
        }
    }

    private data class Clip(
        val start: Int,
        val end: Int,
        val title: String,
        val midi: Boolean
    )

    // ---------------------------------------------------------
    // TRACK LIST
    // ---------------------------------------------------------

    private fun refreshTrackRows() {

        if (!::trackRows.isInitialized) return

        trackRows.removeAllViews()

        tracks.forEachIndexed { index, track ->

            val lane = makeColumn().apply {
                setPadding(dp(3), dp(2), dp(3), dp(2))
                background = rounded(
                    if (index == selectedTrack) {
                        Color.rgb(14, 46, 60)
                    } else {
                        panel
                    },
                    4,
                    track.color
                )
            }

            val titleRow = makeRow()

            titleRow.addView(
                makeText(
                    "${index + 1}",
                    9f,
                    track.color,
                    true
                ),
                LinearLayout.LayoutParams(dp(19), dp(23))
            )

            val name = makeText(
                track.name,
                10f,
                white,
                true
            )

            name.gravity = Gravity.CENTER_VERTICAL

            name.setOnClickListener {
                selectedTrack = index
                statusLabel.text = "Selected: ${track.name}"
                refreshTrackRows()
            }

            titleRow.addView(
                name,
                LinearLayout.LayoutParams(
                    0,
                    dp(23),
                    1f
                )
            )

            addSmallButton(
                titleRow,
                "M",
                mutedTracks.contains(track.name)
            ) {
                if (mutedTracks.contains(track.name)) {
                    mutedTracks.remove(track.name)
                } else {
                    mutedTracks.add(track.name)
                }

                refreshTrackRows()
                refreshMixer()
            }

            addSmallButton(
                titleRow,
                "S",
                soloTracks.contains(track.name)
            ) {
                if (soloTracks.contains(track.name)) {
                    soloTracks.remove(track.name)
                } else {
                    soloTracks.add(track.name)
                }

                refreshTrackRows()
                refreshMixer()
            }

            lane.addView(titleRow)

            val volumeRow = makeRow()

            val seek = SeekBar(this).apply {
                max = 100
                progress = trackVolume[track.name] ?: track.volume

                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {

                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                trackVolume[track.name] = progress
                                track.volume = progress
                            }
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

            volumeRow.addView(
                seek,
                LinearLayout.LayoutParams(0, dp(22), 1f)
            )

            volumeRow.addView(
                makeText(
                    "${trackVolume[track.name] ?: track.volume}%",
                    8f,
                    muted
                ),
                LinearLayout.LayoutParams(dp(42), dp(22))
            )

            lane.addView(volumeRow)

            trackRows.addView(
                lane,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(57)
                ).apply {
                    setMargins(dp(2), dp(1), dp(2), dp(1))
                }
            )
        }
    }

    private fun addSmallButton(
        parent: LinearLayout,
        title: String,
        active: Boolean,
        action: () -> Unit
    ) {

        parent.addView(
            makeButton(
                title,
                if (active) Color.rgb(0, 112, 106)
                else buttonBg,
                action
            ),
            LinearLayout.LayoutParams(
                dp(25),
                dp(24)
            ).apply {
                setMargins(dp(1), 0, dp(1), 0)
            }
        )
    }

    // ---------------------------------------------------------
    // PIANO ROLL
    // ---------------------------------------------------------

    private fun buildPianoPanel() {

        pianoPanel = makeColumn().apply {
            background = rounded(panel, 5)
        }

        val pianoHeader = makeRow().apply {
            setBackgroundColor(panel)
        }

        pianoHeader.addView(
            makeText(
                "♫ KEYBOARD / PIANO ROLL",
                12f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                dp(33),
                1f
            )
        )

        addWeightedButton(
            pianoHeader,
            "OCT -",
            buttonBg,
            0.6f
        ) {
            pianoRoll.octave =
                (pianoRoll.octave - 1).coerceAtLeast(1)

            pianoRoll.invalidate()
        }

        addWeightedButton(
            pianoHeader,
            "OCT +",
            buttonBg,
            0.6f
        ) {
            pianoRoll.octave =
                (pianoRoll.octave + 1).coerceAtMost(7)

            pianoRoll.invalidate()
        }

        addWeightedButton(
            pianoHeader,
            "CLEAR",
            buttonBg,
            0.6f
        ) {
            noteEvents.clear()
            pianoRoll.invalidate()
            toast("Voafafa ny naoty rehetra")
        }

        pianoPanel.addView(pianoHeader)

        pianoRoll = PianoRollView()

        val pianoScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
        }

        pianoScroll.addView(
            pianoRoll,
            ViewGroup.LayoutParams(
                dp(1300),
                dp(160)
            )
        )

        pianoPanel.addView(
            pianoScroll,
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
                dp(195)
            )
        )
    }

    private inner class PianoRollView :
        View(this@MainActivity) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var octave = 4

        private val rowHeight = dp(13).toFloat()
        private val keyWidth = dp(48).toFloat()
        private val labelWidth = dp(38).toFloat()

        private val blackNotes =
            setOf(1, 3, 6, 8, 10)

        override fun onDraw(canvas: Canvas) {

            super.onDraw(canvas)

            canvas.drawColor(bg)

            val totalRows = 12
            val centerNotes = listOf(
                "B", "A#", "A", "G#", "G", "F#",
                "F", "E", "D#", "D", "C#", "C"
            )

            for (row in 0 until totalRows) {

                val top = row * rowHeight
                val bottom = top + rowHeight

                val semitone = 11 - row

                paint.color =
                    if (semitone in blackNotes) {
                        Color.rgb(25, 38, 55)
                    } else {
                        Color.rgb(219, 231, 242)
                    }

                canvas.drawRect(
                    0f,
                    top,
                    labelWidth,
                    bottom,
                    paint
                )

                paint.color =
                    if (semitone in blackNotes) {
                        Color.rgb(15, 29, 46)
                    } else {
                        Color.rgb(8, 23, 39)
                    }

                canvas.drawRect(
                    labelWidth,
                    top,
                    width.toFloat(),
                    bottom,
                    paint
                )

                paint.color = border
                paint.strokeWidth = dp(1).toFloat()

                canvas.drawLine(
                    labelWidth,
                    bottom,
                    width.toFloat(),
                    bottom,
                    paint
                )

                paint.color =
                    if (semitone in blackNotes) Color.BLACK
                    else Color.DKGRAY

                paint.textSize = dp(8).toFloat()

                canvas.drawText(
                    centerNotes[row],
                    dp(3).toFloat(),
                    top + rowHeight - dp(3),
                    paint
                )
            }

            val columns =
                ((width - labelWidth) / keyWidth).toInt() + 1

            for (column in 0..columns) {

                val x = labelWidth + column * keyWidth

                paint.color =
                    if (column % 4 == 0) {
                        Color.rgb(42, 89, 121)
                    } else {
                        Color.rgb(22, 49, 73)
                    }

                canvas.drawLine(
                    x,
                    0f,
                    x,
                    height.toFloat(),
                    paint
                )
            }

            for (note in noteEvents) {

                val left =
                    labelWidth + note.column * keyWidth + dp(2)

                val top =
                    note.row * rowHeight + dp(1)

                val right =
                    left + keyWidth * note.length - dp(4)

                val bottom =
                    top + rowHeight - dp(2)

                paint.color = Color.rgb(165, 83, 255)

                canvas.drawRoundRect(
                    left,
                    top,
                    right,
                    bottom,
                    dp(2).toFloat(),
                    dp(2).toFloat(),
                    paint
                )
            }

            paint.color = accent
            paint.strokeWidth = dp(2).toFloat()

            canvas.drawLine(
                labelWidth,
                0f,
                labelWidth,
                height.toFloat(),
                paint
            )
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {

            if (event.action == MotionEvent.ACTION_DOWN) {

                if (event.x < labelWidth) {
                    toast("Piano key - octave $octave")
                    return true
                }

                val column =
                    ((event.x - labelWidth) / keyWidth)
                        .toInt()
                        .coerceAtLeast(0)

                val row =
                    (event.y / rowHeight)
                        .toInt()
                        .coerceIn(0, 11)

                val existing = noteEvents.indexOfFirst {
                    it.column == column && it.row == row
                }

                if (existing >= 0) {
                    noteEvents.removeAt(existing)
                    toast("Naoty nesorina")
                } else {
                    noteEvents.add(
                        NoteEvent(column, row)
                    )

                    toast(
                        "Naoty nampidirina - octave $octave"
                    )
                }

                invalidate()
                return true
            }

            return true
        }
    }

    // ---------------------------------------------------------
    // MIXER
    // ---------------------------------------------------------

    private fun buildMixer() {

        mixerPanel = makeRow().apply {
            background = rounded(panel, 5)
            gravity = Gravity.CENTER_VERTICAL
        }

        val mixerMenu = makeColumn().apply {
            background = rounded(panel, 5)
        }

        mixerMenu.addView(
            makeText(
                "☷ MIXER",
                13f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(37)
            )
        )

        listOf(
            "♫ CHANNELS",
            "♬ EFFECTS",
            "⚙ SETTINGS"
        ).forEach { title ->

            mixerMenu.addView(
                makeButton(title, panel) {
                    toast(
                        when (title) {
                            "♫ CHANNELS" ->
                                "Mixer channels"

                            "♬ EFFECTS" ->
                                "Effects mbola tsy ampidirina"

                            else ->
                                "Mixer settings mbola hatsaraina"
                        }
                    )
                },
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(33)
                )
            )
        }

        mixerPanel.addView(
            mixerMenu,
            LinearLayout.LayoutParams(
                dp(130),
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        val mixerScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
        }

        mixerChannels = makeRow()

        mixerScroll.addView(
            mixerChannels,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        mixerPanel.addView(
            mixerScroll,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
            )
        )

        root.addView(
            mixerPanel,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(170)
            )
        )
    }

    private fun refreshMixer() {

        if (!::mixerChannels.isInitialized) return

        mixerChannels.removeAllViews()

        tracks.forEach { track ->

            val channel = makeColumn().apply {
                background = rounded(panel2, 5)
                gravity = Gravity.CENTER
                setPadding(dp(3), dp(2), dp(3), dp(2))
            }

            channel.addView(
                makeText(
                    track.name,
                    10f,
                    track.color,
                    true
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(23)
                )
            )

            val controlRow = makeRow()

            addWeightedButton(
                controlRow,
                "M",
                if (mutedTracks.contains(track.name)) red
                else buttonBg,
                1f
            ) {
                if (mutedTracks.contains(track.name)) {
                    mutedTracks.remove(track.name)
                } else {
                    mutedTracks.add(track.name)
                }

                refreshMixer()
            }

            addWeightedButton(
                controlRow,
                "S",
                if (soloTracks.contains(track.name)) {
                    Color.rgb(0, 112, 106)
                } else {
                    buttonBg
                },
                1f
            ) {
                if (soloTracks.contains(track.name)) {
                    soloTracks.remove(track.name)
                } else {
                    soloTracks.add(track.name)
                }

                refreshMixer()
            }

            channel.addView(controlRow)

            val fader = SeekBar(this).apply {
                max = 100
                progress = trackVolume[track.name] ?: track.volume

                setOnSeekBarChangeListener(
                    object : SeekBar.OnSeekBarChangeListener {

                        override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                trackVolume[track.name] = progress
                                track.volume = progress
                            }
                        }

                        override fun onStartTrackingTouch(
                            seekBar: SeekBar?
                        ) {}

                        override fun onStopTrackingTouch(
                            seekBar: SeekBar?
                        ) {
                            refreshTrackRows()
                        }
                    }
                )
            }

            channel.addView(
                fader,
                LinearLayout.LayoutParams(
                    dp(112),
                    dp(39)
                )
            )

            channel.addView(
                makeText(
                    "${trackVolume[track.name] ?: track.volume}%",
                    9f,
                    muted
                ),
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(19)
                )
            )

            mixerChannels.addView(
                channel,
                LinearLayout.LayoutParams(
                    dp(138),
                    ViewGroup.LayoutParams.MATCH_PARENT
                ).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                }
            )
        }

        val master = makeColumn().apply {
            gravity = Gravity.CENTER
            background = rounded(
                Color.rgb(3, 54, 57),
                5,
                accent
            )
        }

        master.addView(
            makeText(
                "MASTER",
                11f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(25)
            )
        )

        val masterFader = SeekBar(this).apply {
            max = 100
            progress = masterVolume

            setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {

                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        if (fromUser) {
                            masterVolume = progress
                        }
                    }

                    override fun onStartTrackingTouch(
                        seekBar: SeekBar?
                    ) {}

                    override fun onStopTrackingTouch(
                        seekBar: SeekBar?
                    ) {}
                }
            )
        }

        master.addView(
            masterFader,
            LinearLayout.LayoutParams(
                dp(100),
                dp(45)
            )
        )

        master.addView(
            makeText(
                "$masterVolume%",
                9f,
                muted
            ),
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(18)
            )
        )

        mixerChannels.addView(
            master,
            LinearLayout.LayoutParams(
                dp(125),
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(dp(3), dp(3), dp(3), dp(3))
            }
        )
    }

    // ---------------------------------------------------------
    // PANEL VISIBILITY
    // ---------------------------------------------------------

    private fun updatePanels() {

        if (::mixerPanel.isInitialized) {
            mixerPanel.visibility =
                if (mixerVisible) View.VISIBLE else View.GONE
        }

        if (::pianoPanel.isInitialized) {
            pianoPanel.visibility =
                if (pianoVisible) View.VISIBLE else View.GONE
        }

        if (::workspace.isInitialized) {
            workspace.requestLayout()
        }
    }

    // ---------------------------------------------------------
    // TRACK MANAGEMENT
    // ---------------------------------------------------------

    private fun addTrack() {

        val name = "Audio $nextTrackNumber"

        val colors = listOf(
            Color.rgb(0, 220, 185),
            Color.rgb(20, 135, 255),
            Color.rgb(153, 75, 255),
            Color.rgb(255, 145, 20),
            Color.rgb(255, 40, 90)
        )

        val track = Track(
            name,
            colors[nextTrackNumber % colors.size],
            70
        )

        tracks.add(track)
        trackVolume[name] = 70

        selectedTrack = tracks.lastIndex
        nextTrackNumber++

        refreshTrackRows()
        refreshMixer()

        statusLabel.text = "Track added: $name"

        toast("$name added")
    }

    // ---------------------------------------------------------
    // TOOLS
    // ---------------------------------------------------------

    private fun handleTool(tool: String) {

        selectedTool = tool

        when (tool) {

            "SELECT" -> {
                toast("Safidio ny track na clip")
            }

            "CUT" -> {
                toast("CUT mila clip editing engine")
            }

            "COPY" -> {
                toast("COPY mila clip editing engine")
            }

            "PASTE" -> {
                toast("PASTE mila clip editing engine")
            }

            "MOVE" -> {
                toast("MOVE mila clip editing engine")
            }

            "DELETE" -> {

                if (selectedTrack in tracks.indices) {

                    val removed = tracks.removeAt(selectedTrack)

                    trackVolume.remove(removed.name)
                    mutedTracks.remove(removed.name)
                    soloTracks.remove(removed.name)

                    selectedTrack =
                        if (tracks.isEmpty()) -1
                        else selectedTrack.coerceAtMost(
                            tracks.lastIndex
                        )

                    refreshTrackRows()
                    refreshMixer()

                    statusLabel.text =
                        "Deleted: ${removed.name}"

                } else {
                    toast("Safidio aloha ny track")
                }
            }

            "ZOOM +" -> {
                zoomLevel = (zoomLevel + 1).coerceAtMost(3)
                toast("Zoom: $zoomLevel")
            }

            "ZOOM -" -> {
                zoomLevel = (zoomLevel - 1).coerceAtLeast(1)
                toast("Zoom: $zoomLevel")
            }
        }
    }

    // ---------------------------------------------------------
    // AUDIO FILE PICKER
    // ---------------------------------------------------------

    private fun openAudioPicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }

        try {
            startActivityForResult(intent, 1001)
        } catch (_: Exception) {
            toast("Tsy afaka manokatra audio picker")
        }
    }

    @Deprecated("Uses the compatible Android activity result API")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 1001 &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {

            chosenAudioUri = data.data

            toast(
                "Audio voafidy. Mila ampifandraisina amin'ny audio engine izy."
            )
        }
    }
}
