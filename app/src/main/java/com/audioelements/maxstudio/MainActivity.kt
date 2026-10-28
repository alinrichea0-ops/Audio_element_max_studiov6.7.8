package com.audioelements.maxstudio

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
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
    private lateinit var statusText: TextView
    private lateinit var timelineContent: LinearLayout

    private var mixerVisible = true
    private var selectedTrack = 0
    private var trackCounter = 3

    private val tracks = mutableListOf(
        "Vocal",
        "Beat",
        "Keyboard"
    )

    private val bg = Color.rgb(17, 20, 26)
    private val panel = Color.rgb(28, 33, 41)
    private val panelLight = Color.rgb(40, 47, 57)
    private val accent = Color.rgb(0, 210, 174)
    private val white = Color.rgb(240, 244, 248)
    private val muted = Color.rgb(145, 157, 173)
    private val line = Color.rgb(54, 63, 75)
    private val red = Color.rgb(200, 55, 67)
    private val blue = Color.rgb(83, 155, 224)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        window.statusBarColor = bg
        window.navigationBarColor = bg

        buildInterface()
    }

    private fun dp(value: Int): Int {
        return (
            value * resources.displayMetrics.density
        ).toInt()
    }

    private fun rounded(
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

            setPadding(
                dp(4),
                dp(3),
                dp(4),
                dp(3)
            )
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

            minHeight = dp(36)
            minimumHeight = dp(36)
            minWidth = 0
            minimumWidth = 0

            setPadding(
                dp(3),
                0,
                dp(3),
                0
            )

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
                dp(39),
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

    private fun message(text: String) {
        Toast.makeText(
            this,
            text,
            Toast.LENGTH_SHORT
        ).show()
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

    // --------------------------------------------------
    // TOP BAR
    // --------------------------------------------------

    private fun buildTopBar() {
        val top = row().apply {
            setBackgroundColor(panel)
            setPadding(
                dp(6),
                dp(2),
                dp(6),
                dp(2)
            )
        }

        top.addView(
            label(
                "AUDIO ELEMENTS MAX",
                14f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                dp(43),
                2.2f
            )
        )

        addButton(
            top,
            "PROJECT",
            panelLight,
            1f
        ) {
            message("Audio Elements Max Studio")
        }

        addButton(
            top,
            "UNDO",
            panelLight,
            0.7f
        ) {
            message("Undo mbola tsy ampidirina")
        }

        addButton(
            top,
            "REDO",
            panelLight,
            0.7f
        ) {
            message("Redo mbola tsy ampidirina")
        }

        root.addView(
            top,
            LinearLayout.LayoutParams(
                -1,
                dp(47)
            )
        )
    }

    // --------------------------------------------------
    // TRANSPORT BAR
    // --------------------------------------------------

    private fun buildTransportBar() {
        val bar = row().apply {
            setPadding(
                dp(4),
                dp(2),
                dp(4),
                dp(2)
            )
        }

        addButton(
            bar,
            "● REC",
            red,
            0.9f
        ) {
            message("REC mbola mila audio engine")
        }

        addButton(
            bar,
            "▶ PLAY",
            Color.rgb(0, 112, 96),
            1f
        ) {
            message("PLAY mbola mila audio engine")
        }

        addButton(
            bar,
            "Ⅱ PAUSE",
            panelLight,
            1f
        ) {
            message("PAUSE mbola tsy ampidirina")
        }

        addButton(
            bar,
            "■ STOP",
            panelLight,
            0.9f
        ) {
            message("STOP")
        }

        bar.addView(
            label(
                "00:00:00",
                13f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                0,
                dp(38),
                1.1f
            )
        )

        addButton(
            bar,
            "+ TRACK",
            accent,
            1f
        ) {
            addTrack()
        }

        addButton(
            bar,
            "MIXER",
            panelLight,
            0.9f
        ) {
            toggleMixer()
        }

        root.addView(
            bar,
            LinearLayout.LayoutParams(
                -1,
                dp(44)
            )
        )
    }

    private fun toggleMixer() {
        mixerVisible = !mixerVisible

        mixerContainer.visibility =
            if (mixerVisible) View.VISIBLE
            else View.GONE

        message(
            if (mixerVisible) {
                "Mixer aseho"
            } else {
                "Mixer nafenina"
            }
        )
    }

    // --------------------------------------------------
    // EDITING TOOLBAR
    // --------------------------------------------------

    private fun buildToolsBar() {
        val tools = row().apply {
            setBackgroundColor(panel)
            setPadding(
                dp(3),
                dp(2),
                dp(3),
                dp(2)
            )
        }

        val names = listOf(
            "SELECT",
            "CUT",
            "COPY",
            "PASTE",
            "MOVE",
            "DELETE",
            "ZOOM +",
            "ZOOM -"
        )

        names.forEach { name ->
            addButton(
                tools,
                name,
                if (name == "SELECT") accent
                else panelLight,
                1f
            ) {
                handleTool(name)
            }
        }

        root.addView(
            tools,
            LinearLayout.LayoutParams(
                -1,
                dp(44)
            )
        )
    }

    // --------------------------------------------------
    // MAIN WORKSPACE
    // --------------------------------------------------

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

    // --------------------------------------------------
    // TRACK SIDEBAR
    // --------------------------------------------------

    private fun buildTrackPanel(
        parent: LinearLayout
    ) {
        val side = column().apply {
            setBackgroundColor(panel)
        }

        side.addView(
            label(
                "TRACKS",
                13f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(38)
            )
        )

        side.addView(
            makeButton("+ ADD TRACK", panelLight) {
                addTrack()
            },
            LinearLayout.LayoutParams(
                -1,
                dp(36)
            ).apply {
                setMargins(
                    dp(4),
                    dp(2),
                    dp(4),
                    dp(3)
                )
            }
        )

        side.addView(
            makeButton("IMPORT AUDIO", panelLight) {
                message("Import Audio mbola tsy ampidirina")
            },
            LinearLayout.LayoutParams(
                -1,
                dp(36)
            ).apply {
                setMargins(
                    dp(4),
                    dp(2),
                    dp(4),
                    dp(3)
                )
            }
        )

        statusText = label(
            "Ready",
            10f,
            muted
        )

        side.addView(
            statusText,
            LinearLayout.LayoutParams(
                -1,
                dp(35)
            )
        )

        parent.addView(
            side,
            LinearLayout.LayoutParams(
                dp(145),
                -1
            )
        )

        parent.addView(
            View(this).apply {
                setBackgroundColor(line)
            },
            LinearLayout.LayoutParams(
                dp(1),
                -1
            )
        )
    }

    // --------------------------------------------------
    // ARRANGEMENT TIMELINE
    // --------------------------------------------------

    private fun buildTimeline(
        parent: LinearLayout
    ) {
        val area = column().apply {
            setBackgroundColor(bg)
        }

        area.addView(
            label(
                "ARRANGEMENT  /  MULTI-TRACK",
                12f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(35)
            )
        )

        val horizontal = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            setBackgroundColor(panel)
            overScrollMode = View.OVER_SCROLL_IF_CONTENT_SCROLLS
        }

        timelineContent = row()

        for (bar in 1..64) {
            val cell = column().apply {
                gravity = Gravity.CENTER
                setBackgroundColor(
                    if ((bar - 1) % 4 == 0) {
                        panelLight
                    } else {
                        panel
                    }
                )
            }

            cell.addView(
                label(
                    bar.toString(),
                    10f,
                    muted,
                    true
                ),
                LinearLayout.LayoutParams(
                    dp(76),
                    dp(31)
                )
            )

            timelineContent.addView(
                cell,
                LinearLayout.LayoutParams(
                    dp(76),
                    dp(31)
                )
            )
        }

        horizontal.addView(
            timelineContent,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        area.addView(
            horizontal,
            LinearLayout.LayoutParams(
                -1,
                dp(32)
            )
        )

        val vertical = ScrollView(this).apply {
            isFillViewport = false
            isVerticalScrollBarEnabled = true
        }

        trackContainer = column()

        vertical.addView(
            trackContainer,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        area.addView(
            vertical,
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

    // --------------------------------------------------
    // TRACKS
    // --------------------------------------------------

    private fun refreshTracks() {
        if (!::trackContainer.isInitialized) return

        trackContainer.removeAllViews()

        tracks.forEachIndexed { index, name ->

            val trackRow = row().apply {
                setBackgroundColor(
                    if (index == selectedTrack) {
                        Color.rgb(38, 58, 64)
                    } else if (index % 2 == 0) {
                        bg
                    } else {
                        panel
                    }
                )
            }

            val trackName = TextView(this).apply {
                text = name
                textSize = 11f

                setTextColor(
                    if (index == selectedTrack) {
                        accent
                    } else {
                        white
                    }
                )

                gravity = Gravity.CENTER

                setPadding(
                    dp(3),
                    dp(3),
                    dp(3),
                    dp(3)
                )

                setOnClickListener {
                    selectTrack(index)
                }
            }

            trackRow.addView(
                trackName,
                LinearLayout.LayoutParams(
                    dp(105),
                    dp(61)
                )
            )

            val clipColor =
                if (index % 2 == 0) {
                    Color.rgb(64, 187, 155)
                } else {
                    blue
                }

            val clip = TextView(this).apply {
                text = "━━━━ AUDIO CLIP ━━━━"
                textSize = 10f
                typeface = Typeface.DEFAULT_BOLD

                setTextColor(
                    Color.rgb(15, 29, 32)
                )

                gravity = Gravity.CENTER

                background = rounded(
                    clipColor,
                    5
                )

                setOnClickListener {
                    selectTrack(index)
                    message("$name nofidina")
                }
            }

            trackRow.addView(
                clip,
                LinearLayout.LayoutParams(
                    dp(250),
                    dp(40)
                ).apply {
                    setMargins(
                        dp(4),
                        dp(4),
                        dp(4),
                        dp(4)
                    )
                }
            )

            val remaining = View(this).apply {
                setBackgroundColor(
                    if (index % 2 == 0) bg
                    else panel
                )
            }

            trackRow.addView(
                remaining,
                LinearLayout.LayoutParams(
                    0,
                    dp(61),
                    1f
                )
            )

            trackContainer.addView(
                trackRow,
                LinearLayout.LayoutParams(
                    -1,
                    dp(62)
                )
            )

            trackContainer.addView(
                View(this).apply {
                    setBackgroundColor(line)
                },
                LinearLayout.LayoutParams(
                    -1,
                    dp(1)
                )
            )
        }
    }

    private fun selectTrack(index: Int) {
        if (index !in tracks.indices) return

        selectedTrack = index
        statusText.text = "Selected: ${tracks[index]}"

        refreshTracks()
    }

    // --------------------------------------------------
    // MIXER
    // --------------------------------------------------

    private fun buildMixer() {
        mixerContainer = row().apply {
            setBackgroundColor(panel)
            gravity = Gravity.CENTER_VERTICAL
        }

        root.addView(
            mixerContainer,
            LinearLayout.LayoutParams(
                -1,
                dp(120)
            )
        )

        refreshMixer()
    }

    private fun refreshMixer() {
        if (!::mixerContainer.isInitialized) return

        mixerContainer.removeAllViews()

        mixerContainer.addView(
            label(
                "MIXER",
                11f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                dp(58),
                -1
            )
        )

        tracks.forEachIndexed { index, track ->

            val channel = column().apply {
                gravity = Gravity.CENTER
                setPadding(
                    dp(3),
                    dp(2),
                    dp(3),
                    dp(2)
                )
                setBackgroundColor(
                    if (index == selectedTrack) {
                        Color.rgb(38, 48, 56)
                    } else {
                        panel
                    }
                )
            }

            channel.addView(
                label(
                    track,
                    10f,
                    white,
                    true
                ),
                LinearLayout.LayoutParams(
                    -1,
                    dp(24)
                )
            )

            val volume = SeekBar(this).apply {
                max = 100
                progress = 80
                contentDescription = "$track volume"
            }

            channel.addView(
                volume,
                LinearLayout.LayoutParams(
                    dp(90),
                    dp(29)
                )
            )

            channel.addView(
                label(
                    "VOL",
                    9f,
                    muted
                ),
                LinearLayout.LayoutParams(
                    -1,
                    dp(18)
                )
            )

            mixerContainer.addView(
                channel,
                LinearLayout.LayoutParams(
                    dp(105),
                    -1
                )
            )
        }

        val master = column().apply {
            gravity = Gravity.CENTER
            setPadding(
                dp(3),
                dp(2),
                dp(3),
                dp(2)
            )
            setBackgroundColor(panelLight)
        }

        master.addView(
            label(
                "MASTER",
                10f,
                accent,
                true
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(24)
            )
        )

        val masterVolume = SeekBar(this).apply {
            max = 100
            progress = 85
            contentDescription = "Master volume"
        }

        master.addView(
            masterVolume,
            LinearLayout.LayoutParams(
                dp(90),
                dp(29)
            )
        )

        master.addView(
            label(
                "OUTPUT",
                9f,
                muted
            ),
            LinearLayout.LayoutParams(
                -1,
                dp(18)
            )
        )

        mixerContainer.addView(
            master,
            LinearLayout.LayoutParams(
                dp(110),
                -1
            )
        )
    }

    // --------------------------------------------------
    // ADD TRACK
    // --------------------------------------------------

    private fun addTrack() {
        trackCounter++

        val newTrack = "Audio $trackCounter"

        tracks.add(newTrack)
        selectedTrack = tracks.lastIndex

        refreshTracks()
        refreshMixer()

        statusText.text = "Track added: $newTrack"
    }

    // --------------------------------------------------
    // EDITING TOOLS
    // --------------------------------------------------

    private fun handleTool(tool: String) {
        when (tool) {

            "SELECT" -> {
                message("Safidio ny Track na Clip")
            }

            "CUT" -> {
                message("CUT mbola mila audio editing engine")
            }

            "COPY" -> {
                message("COPY mbola tsy ampidirina")
            }

            "PASTE" -> {
                message("PASTE mbola tsy ampidirina")
            }

            "MOVE" -> {
                message("MOVE mbola tsy ampidirina")
            }

            "DELETE" -> {
                if (selectedTrack in tracks.indices) {
                    val deleted = tracks.removeAt(selectedTrack)

                    selectedTrack =
                        if (tracks.isEmpty()) {
                            -1
                        } else {
                            (selectedTrack - 1).coerceAtLeast(0)
                        }

                    refreshTracks()
                    refreshMixer()

                    statusText.text = "Deleted: $deleted"
                } else {
                    message("Tsy misy Track hofafana")
                }
            }

            "ZOOM +" -> {
                message("Zoom mbola hatsaraina")
            }

            "ZOOM -" -> {
                message("Zoom mbola hatsaraina")
            }
        }
    }
}
