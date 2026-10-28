package com.audioelements.maxstudio

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
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
    private lateinit var statusText: TextView
    private lateinit var timelineContent: LinearLayout
    private lateinit var timelineScroll: HorizontalScrollView

    private var mixerVisible = true
    private var selectedTrack = 0
    private var trackCounter = 3

    private val tracks = mutableListOf(
        "Vocal",
        "Beat",
        "Keyboard"
    )

    private val bg = Color.rgb(19, 22, 28)
    private val panel = Color.rgb(30, 34, 42)
    private val panelLight = Color.rgb(39, 44, 54)
    private val accent = Color.rgb(0, 205, 170)
    private val white = Color.rgb(240, 244, 248)
    private val muted = Color.rgb(145, 156, 171)
    private val line = Color.rgb(52, 59, 71)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        buildInterface()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun background(
        color: Int,
        radius: Int = 8
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun label(
        value: String,
        size: Float = 12f,
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
            setPadding(dp(5), dp(3), dp(5), dp(3))
        }
    }

    private fun row(): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        isBaselineAligned = false
        }
    }

    private fun column(): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        isBaselineAligned = false
        }
    }

    private fun makeButton(
        title: String,
        color: Int = panelLight,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = title
            textSize = 10f
            isAllCaps = false
            setTextColor(white)
            backgroundTintList =
                android.content.res.ColorStateList.valueOf(color)

            minHeight = dp(38)
            minimumHeight = dp(38)
            minWidth = dp(0)
            minimumWidth = dp(0)
            setPadding(dp(5), 0, dp(5), 0)

            setOnClickListener {
                action()
            }
        }
    }

    private fun addButton(
        parent: LinearLayout,
        title: String,
        color: Int = panelLight,
        weight: Float = 1f,
        action: () -> Unit
    ) {
        parent.addView(
            makeButton(title, color, action),
            LinearLayout.LayoutParams(
                0,
                dp(40),
                weight
            ).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
        )
    }

    private fun buildInterface() {
        root = column().apply {
            setBackgroundColor(bg)
        }

        setContentView(root)

        buildTopBar()
        buildTransportBar()
        buildToolsBar()
        buildWorkspace()
        buildMixer()

        refreshTracks()
    }

    private fun buildTopBar() {
        val top = row().apply {
            setBackgroundColor(panel)
            setPadding(dp(6), dp(3), dp(6), dp(3))
        }

        top.addView(
            label(
                "AUDIO ELEMENTS MAX",
                15f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                dp(44),
                2f
            )
        )

        addButton(top, "PROJECT", weight = 1f) {
            message("Audio Elements Max Studio")
        }

        addButton(top, "UNDO", weight = 0.7f) {
            message("Undo mbola tsy ampifandraisina")
        }

        addButton(top, "REDO", weight = 0.7f) {
            message("Redo mbola tsy ampifandraisina")
        }

        root.addView(
            top,
            LinearLayout.LayoutParams(-1, dp(48))
        )
    }

    private fun buildTransportBar() {
        val bar = row().apply {
            setPadding(dp(5), dp(2), dp(5), dp(2))
            setBackgroundColor(bg)
        }

        addButton(
            bar,
            "● REC",
            Color.rgb(165, 40, 52),
            0.9f
        ) {
            message("REC: mbola tsy mifandray amin'ny recorder")
        }

        addButton(
            bar,
            "▶ PLAY",
            Color.rgb(0, 120, 105),
            1f
        ) {
            message("PLAY: mbola tsy misy audio engine")
        }

        addButton(
            bar,
            "Ⅱ PAUSE",
            panelLight,
            1f
        ) {
            message("Pause mbola tsy ampifandraisina")
        }

        addButton(
            bar,
            "■ STOP",
            panelLight,
            0.9f
        ) {
            message("Stop")
        }

        bar.addView(
            label("00:00:00", 14f, accent, true),
            LinearLayout.LayoutParams(0, dp(40), 1.1f)
        )

        addButton(bar, "+ TRACK", accent, 1f) {
            addTrack()
        }

        addButton(bar, "MIXER", panelLight, 0.9f) {
            mixerVisible = !mixerVisible

            mixerContainer.visibility =
                if (mixerVisible) View.VISIBLE else View.GONE

            message(
                if (mixerVisible) "Mixer aseho" else "Mixer nafenina"
            )
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(-1, dp(46))
        )
    }

    private fun buildToolsBar() {
        val tools = row().apply {
            setBackgroundColor(panel)
            setPadding(dp(3), dp(2), dp(3), dp(2))
        }

        val toolNames = listOf(
            "SELECT",
            "CUT",
            "COPY",
            "PASTE",
            "MOVE",
            "DELETE",
            "ZOOM +",
            "ZOOM -"
        )

        toolNames.forEach { tool ->
            addButton(
                tools,
                tool,
                if (tool == "SELECT") accent else panelLight,
                1f
            ) {
                handleTool(tool)
            }
        }

        root.addView(
            tools,
            LinearLayout.LayoutParams(-1, dp(46))
        )
    }

    private fun buildWorkspace() {
        val workspace = row().apply {
            gravity = Gravity.TOP
        }

        buildTrackPanel(workspace)
        buildTimeline(workspace)

        root.addView(
            workspace,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )
    }

    private fun buildTrackPanel(parent: LinearLayout) {
        val side = column().apply {
            setBackgroundColor(panel)
        }

        side.addView(
            label("TRACKS", 13f, accent, true),
            LinearLayout.LayoutParams(-1, dp(42))
        )

        side.addView(
            makeButton("+ ADD TRACK", panelLight) {
                addTrack()
            },
            LinearLayout.LayoutParams(-1, dp(40)).apply {
                setMargins(dp(4), dp(2), dp(4), dp(4))
            }
        )

        side.addView(
            makeButton("IMPORT AUDIO", panelLight) {
                message("Import Audio mbola tsy ampidirina")
            },
            LinearLayout.LayoutParams(-1, dp(40)).apply {
                setMargins(dp(4), dp(2), dp(4), dp(4))
            }
        )

        statusText = label("Ready", 10f, muted)

        side.addView(
            statusText,
            LinearLayout.LayoutParams(-1, dp(35))
        )

        parent.addView(
            side,
            LinearLayout.LayoutParams(
                dp(145),
                -1
            )
        )

        val divider = View(this).apply {
            setBackgroundColor(line)
        }

        parent.addView(
            divider,
            LinearLayout.LayoutParams(dp(1), -1)
        )
    }

    private fun buildTimeline(parent: LinearLayout) {
        val area = column().apply {
            setBackgroundColor(bg)
        }

        area.addView(
            label(
                "ARRANGEMENT  /  MULTI-TRACK",
                13f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(-1, dp(38))
        )

        timelineScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            setBackgroundColor(panel)
        }

        timelineContent = row()

        for (bar in 1..32) {
            val rulerCell = column().apply {
                gravity = Gravity.CENTER
                setBackgroundColor(
                    if (bar % 4 == 1) panelLight else panel
                )
            }

            rulerCell.addView(
                label(bar.toString(), 11f, muted, true),
                LinearLayout.LayoutParams(
                    dp(88),
                    dp(34)
                )
            )

            timelineContent.addView(
                rulerCell,
                LinearLayout.LayoutParams(
                    dp(88),
                    dp(34)
                )
            )
        }

        timelineScroll.addView(timelineContent)

        area.addView(
            timelineScroll,
            LinearLayout.LayoutParams(-1, dp(34))
        )

        val scroll = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = true
        }

        trackContainer = column()

        scroll.addView(
    trackContainer,
    android.widget.ScrollView.LayoutParams(
        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
    )
)

        area.addView(
            scroll,
            LinearLayout.LayoutParams(
                -1,
                0,
                1f
            )
        )

        parent.addView(
            area,
            LinearLayout.LayoutParams(
                0,
                -1,
                1f
            )
        )
    }

    private fun refreshTracks() {
        if (!::trackContainer.isInitialized) return

        trackContainer.removeAllViews()

        tracks.forEachIndexed { index, name ->
            val trackRow = row().apply {
                setBackgroundColor(
                    if (index == selectedTrack) {
                        Color.rgb(43, 61, 68)
                    } else {
                        bg
                    }
                )
            }

            val trackName = TextView(this).apply {
                text = name
                textSize = 11f
                setTextColor(
                    if (index == selectedTrack) accent else white
                )
                gravity = Gravity.CENTER
                setPadding(dp(3), dp(3), dp(3), dp(3))
                setOnClickListener {
                    selectedTrack = index
                    statusText.text = "Selected: $name"
                    refreshTracks()
                }
            }

            trackRow.addView(
                trackName,
                LinearLayout.LayoutParams(
                    dp(105),
                    dp(62)
                )
            )

            val clip = TextView(this).apply {
                text = "  ━━━━━━━  AUDIO CLIP  ━━━━━━━"
                textSize = 11f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.rgb(12, 29, 31))
                gravity = Gravity.CENTER
                background = background(
                    if (index % 2 == 0) {
                        Color.rgb(70, 195, 165)
                    } else {
                        Color.rgb(90, 155, 215)
                    },
                    5
                )

                setOnClickListener {
                    selectedTrack = index
                    statusText.text = "Selected: $name"
                    refreshTracks()
                }
            }

            trackRow.addView(
                clip,
                LinearLayout.LayoutParams(
                    dp(270),
                    dp(42)
                ).apply {
                    setMargins(dp(4), dp(4), dp(4), dp(4))
                }
            )

            val remaining = View(this).apply {
                setBackgroundColor(
                    if (index % 2 == 0) bg else panel
                )
            }

            trackRow.addView(
                remaining,
                LinearLayout.LayoutParams(
                    0,
                    dp(62),
                    1f
                )
            )

            trackContainer.addView(
                trackRow,
                LinearLayout.LayoutParams(-1, dp(64))
            )

            val separator = View(this).apply {
                setBackgroundColor(line)
            }

            trackContainer.addView(
                separator,
                LinearLayout.LayoutParams(-1, dp(1))
            )
        }
    }

    private fun buildMixer() {
        mixerContainer = row().apply {
            setBackgroundColor(panel)
            gravity = Gravity.CENTER_VERTICAL
        }

        mixerContainer.addView(
            label("MIXER", 13f, accent, true),
            LinearLayout.LayoutParams(dp(70), -1)
        )

        root.addView(
            mixerContainer,
            LinearLayout.LayoutParams(-1, dp(125))
        )

        refreshMixer()
    }

    private fun refreshMixer() {
        if (!::mixerContainer.isInitialized) return

        mixerContainer.removeAllViews()

        mixerContainer.addView(
            label("MIXER", 12f, accent, true),
            LinearLayout.LayoutParams(dp(65), -1)
        )

        tracks.forEach { track ->
            val channel = column().apply {
                gravity = Gravity.CENTER
                setPadding(dp(3), dp(2), dp(3), dp(2))
                setBackgroundColor(panel)
            }

            channel.addView(
                label(track, 10f, white, true),
                LinearLayout.LayoutParams(-1, dp(25))
            )

            val volume = SeekBar(this).apply {
                max = 100
                progress = 80
            }

            channel.addView(
                volume,
                LinearLayout.LayoutParams(dp(95), dp(28))
            )

            channel.addView(
                label("VOL", 9f, muted),
                LinearLayout.LayoutParams(-1, dp(20))
            )

            mixerContainer.addView(
                channel,
                LinearLayout.LayoutParams(dp(110), -1)
            )
        }

        val master = column().apply {
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(2), dp(3), dp(2))
            setBackgroundColor(panelLight)
        }

        master.addView(
            label("MASTER", 10f, accent, true),
            LinearLayout.LayoutParams(-1, dp(25))
        )

        val masterVolume = SeekBar(this).apply {
            max = 100
            progress = 85
        }

        master.addView(
            masterVolume,
            LinearLayout.LayoutParams(dp(95), dp(28))
        )

        master.addView(
            label("OUTPUT", 9f, muted),
            LinearLayout.LayoutParams(-1, dp(20))
        )

        mixerContainer.addView(
            master,
            LinearLayout.LayoutParams(dp(115), -1)
        )
    }

    private fun addTrack() {
        trackCounter++
        val newTrack = "Audio $trackCounter"

        tracks.add(newTrack)
        selectedTrack = tracks.lastIndex

        refreshTracks()
        refreshMixer()

        statusText.text = "Track added: $newTrack"
    }

    private fun handleTool(tool: String) {
        when (tool) {
            "SELECT" -> message("Safidio ny Track na Clip")
            "CUT" -> message("Cut audio mbola tsy ampidirina")
            "COPY" -> message("Copy mbola tsy ampidirina")
            "PASTE" -> message("Paste mbola tsy ampidirina")
            "MOVE" -> message("Move mbola tsy ampidirina")
            "DELETE" -> {
                if (selectedTrack in tracks.indices) {
                    val deleted = tracks.removeAt(selectedTrack)

                    selectedTrack =
                        if (tracks.isEmpty()) -1
                        else (selectedTrack - 1).coerceAtLeast(0)

                    refreshTracks()
                    refreshMixer()

                    statusText.text = "Deleted: $deleted"
                } else {
                    message("Safidio aloha ny Track")
                }
            }
            "ZOOM +" -> message("Zoom mbola hatsaraina")
            "ZOOM -" -> message("Zoom mbola hatsaraina")
        }
    }

    private fun message(value: String) {
        Toast.makeText(
            this,
            value,
            Toast.LENGTH_SHORT
        ).show()
    }
}
