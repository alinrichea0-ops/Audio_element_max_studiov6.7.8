package com.audioelements.maxstudio

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Typeface
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

    private val tracks = mutableListOf<String>()
    private var trackCounter = 0
    private var mixerVisible = true
    private var selectedTrack = -1

    private val bg = Color.rgb(22, 24, 30)
    private val panel = Color.rgb(34, 37, 46)
    private val accent = Color.rgb(0, 190, 160)
    private val white = Color.WHITE
    private val muted = Color.rgb(160, 170, 185)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        tracks.add("Vocal")
        tracks.add("Beat")
        tracks.add("Keyboard")
        trackCounter = tracks.size

        buildInterface()
    }

    private fun text(
        value: String,
        size: Float = 14f,
        color: Int = white
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
        }
    }

    private fun button(
    label: String,
    action: () -> Unit
): Button {
    return Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 12f
        setTextColor(Color.WHITE)

        backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                when (label.uppercase()) {
                    "● REC" -> Color.rgb(190, 45, 55)
                    "▶ PLAY" -> Color.rgb(0, 145, 130)
                    "Ⅱ PAUSE" -> Color.rgb(65, 75, 90)
                    "■ STOP" -> Color.rgb(75, 80, 90)
                    else -> Color.rgb(38, 43, 52)
                }
            )

        setPadding(12, 8, 12, 8)

        setOnClickListener {
            animate()
                .scaleX(0.96f)
                .scaleY(0.96f)
                .setDuration(80)
                .withEndAction {
                    animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .start()
                    action()
                }
                .start()
          }
       }
    }

    private fun row(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun buildInterface() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        setContentView(root)

        // TOP BAR
        val top = row().apply {
            setBackgroundColor(panel)
            addView(
                text("AUDIO ELEMENTS MAX", 17f, accent),
                LinearLayout.LayoutParams(0, 52, 2f)
            )
            addView(
                button("PROJECT") {
                    message("Project controls")
                }
            )
            addView(
                button("UNDO") {
                    message("Undo mbola mila ampifandraisina")
                }
            )
            addView(
                button("REDO") {
                    message("Redo mbola mila ampifandraisina")
                }
            )
        }
        root.addView(top)

        // TRANSPORT BAR
        val transport = row().apply {
            addView(button("● REC") {
                message("Ampifandraiso amin'ny recorder ny transport")
            })
            addView(button("▶ PLAY") {
                message("Playback engine mbola tsy ampidirina")
            })
            addView(button("Ⅱ PAUSE") {
                message("Pause mbola mila ampifandraisina")
            })
            addView(button("■ STOP") {
                message("Stop")
            })
            addView(
                text("00:00:00", 16f, accent),
                LinearLayout.LayoutParams(0, 48, 1f)
            )
            addView(button("+ TRACK") {
                addTrack()
            })
            addView(button("MIXER") {
                mixerVisible = !mixerVisible
                mixerContainer.visibility =
                    if (mixerVisible) View.VISIBLE else View.GONE
            })
        }
        root.addView(transport)

        // EDIT TOOLS
        val tools = row().apply {
            setBackgroundColor(Color.rgb(27, 30, 37))

            listOf(
                "SELECT", "CUT", "COPY", "PASTE",
                "MOVE", "DELETE", "ZOOM +", "ZOOM -"
            ).forEach { tool ->
                addView(
                    button(tool) {
                        handleTool(tool)
                    },
                    LinearLayout.LayoutParams(0, 48, 1f)
                )
            }
        }
        root.addView(tools)

        // MAIN WORKSPACE
        val workspace = row().apply {
            gravity = Gravity.TOP
        }

        val leftPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(panel)
        }

        leftPanel.addView(
            text("TRACKS", 14f, accent)
        )

        leftPanel.addView(
            button("＋ Add Track") {
                addTrack()
            }
        )

        leftPanel.addView(
            button("Import Audio") {
                message(
                    "Ny import audio dia ampidirina amin'ny dingana manaraka"
                )
            }
        )

        statusText = text("Ready", 12f, muted)
        leftPanel.addView(statusText)

        workspace.addView(
            leftPanel,
            LinearLayout.LayoutParams(155, -1)
        )

        // TIMELINE
        val timelineArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        timelineArea.addView(
            text("ARRANGEMENT / MULTI-TRACK", 15f, accent)
        )

        val ruler = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
        }

        val rulerContent = row()

        for (i in 0..15) {
            rulerContent.addView(
                text("${i + 1}", 12f, muted),
                LinearLayout.LayoutParams(95, 38)
            )
        }

        ruler.addView(rulerContent)
        timelineArea.addView(ruler)

        val trackScroll = ScrollView(this)

        trackContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        trackScroll.addView(trackContainer)
        timelineArea.addView(
            trackScroll,
            LinearLayout.LayoutParams(
                -1, 0, 1f
            )
        )

        workspace.addView(
            timelineArea,
            LinearLayout.LayoutParams(0, -1, 1f)
        )
        root.addView(
    workspace,
    LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        0,
        1f
    )
)

        // MIXER PANEL
        mixerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(panel)
            gravity = Gravity.CENTER_VERTICAL
        }

        root.addView(
            mixerContainer,
            LinearLayout.LayoutParams(-1, 145)
        )

        buildMixer()
        refreshTracks()
    }

    private fun addTrack() {
        trackCounter++
        tracks.add("Audio Track $trackCounter")
        refreshTracks()
        buildMixer()

        statusText.text = "Track added: ${tracks.last()}"
    }

    private fun refreshTracks() {
        trackContainer.removeAllViews()

        tracks.forEachIndexed { index, name ->
            val trackRow = row().apply {
                setBackgroundColor(
                    if (selectedTrack == index)
                        Color.rgb(50, 65, 75)
                    else
                        panel
                )
            }

            trackRow.addView(
                text(name, 13f),
                LinearLayout.LayoutParams(125, 65)
            )

            val clip = TextView(this).apply {
                text = "  ━━━━━  Audio Clip ━━━━━  "
                textSize = 13f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.BLACK)
                gravity = Gravity.CENTER
                setBackgroundColor(
                    if (index % 2 == 0)
                        Color.rgb(80, 205, 170)
                    else
                        Color.rgb(100, 160, 230)
                )
                setOnClickListener {
                    selectedTrack = index
                    refreshTracks()
                    statusText.text = "Selected: $name"
                }
            }

            trackRow.addView(
                clip,
                LinearLayout.LayoutParams(0, 45, 1f)
            )

            trackContainer.addView(
                trackRow,
                LinearLayout.LayoutParams(-1, 70)
            )
        }
    }

    private fun buildMixer() {
        mixerContainer.removeAllViews()

        mixerContainer.addView(
            text("MIXER", 15f, accent),
            LinearLayout.LayoutParams(80, -1)
        )

        tracks.forEach { track ->
            val channel = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(4, 2, 4, 2)
            }

            channel.addView(text(track, 11f))

            val volume = SeekBar(this).apply {
                max = 100
                progress = 80
            }

            channel.addView(
                volume,
                LinearLayout.LayoutParams(100, 35)
            )

            channel.addView(
                text("VOL", 10f, muted)
            )

            mixerContainer.addView(
                channel,
                LinearLayout.LayoutParams(115, -1)
            )
        }

        val master = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(4, 2, 4, 2)
        }

        master.addView(text("MASTER", 12f, accent))

        val masterVolume = SeekBar(this).apply {
            max = 100
            progress = 85
        }

        master.addView(
            masterVolume,
            LinearLayout.LayoutParams(100, 35)
        )

        mixerContainer.addView(
            master,
            LinearLayout.LayoutParams(120, -1)
        )
    }

    private fun handleTool(tool: String) {
        when (tool) {
            "SELECT" -> message("Safidio ny clip iray")
            "CUT" -> message("Cut mbola mila fanapahana audio tena izy")
            "COPY" -> message("Copy mbola mila ampifandraisina")
            "PASTE" -> message("Paste mbola mila ampifandraisina")
            "MOVE" -> message("Move mbola mila drag-and-drop")
            "DELETE" -> {
                if (selectedTrack >= 0 &&
                    selectedTrack < tracks.size
                ) {
                    tracks.removeAt(selectedTrack)
                    selectedTrack = -1
                    refreshTracks()
                    buildMixer()
                    statusText.text = "Track deleted"
                } else {
                    message("Safidio aloha ny track")
                }
            }
            "ZOOM +" -> message("Timeline zoom mbola hatsaraina")
            "ZOOM -" -> message("Timeline zoom mbola hatsaraina")
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
