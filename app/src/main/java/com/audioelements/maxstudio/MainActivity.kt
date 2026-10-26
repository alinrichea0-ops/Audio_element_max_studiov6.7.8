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
            setOnClickListener { action() }
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
            LinearLayout.LayoutParams(0, 0, 1f)
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
}package com.audioelements.maxstudio

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.SeekBar
import android.widget.Toast
import android.content.ContentValues
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private val bg = Color.rgb(15, 18, 29)
    private val panel = Color.rgb(28, 33, 48)
    private val accent = Color.rgb(0, 220, 180)
    private val white = Color.WHITE
    private val muted = Color.rgb(165, 175, 195)

    private var recorder: MediaRecorder? = null
    private var recording = false
    private var recordUri: android.net.Uri? = null
    private var recordDescriptor: android.os.ParcelFileDescriptor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showHome()
    }

    private fun baseLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 20)
            setBackgroundColor(bg)
        }
    }

    private fun label(text: String, size: Float, color: Int = white): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
        }
    }

    private fun addGap(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, height))
    }

    private fun button(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(white)
            isAllCaps = false
            setBackgroundColor(panel)
            setOnClickListener { action() }
        }
    }

    private fun showHome() {
        val root = baseLayout()

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(label("AUDIO ELEMENTS", 24f, accent).apply {
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        content.addView(label("MAX STUDIO  •  6.7.8 BETA", 12f, muted).apply {
            gravity = Gravity.CENTER
        })

        addGap(content, 22)

        content.addView(label("YOUR WORKSPACE", 13f, muted))
        addGap(content, 10)

        val tools = listOf(
            "🎙  Vocal Recording" to { showRecording() },
            "🎚  Parametric EQ" to { showEffects("PARAMETRIC EQ", listOf("Low", "Mid", "High")) },
            "🎛  Compressor" to { showEffects("COMPRESSOR", listOf("Threshold", "Ratio", "Attack", "Release")) },
            "🌊  Reverb & Delay" to { showEffects("REVERB & DELAY", listOf("Reverb", "Delay", "Mix")) },
            "🎹  Piano Roll" to { showInfo("PIANO ROLL", "Ity no toerana hanomanana ny MIDI notes. Mbola tsy tafiditra ny MIDI playback amin'ity beta ity.") },
            "🎵  Multi-track" to { showInfo("MULTI-TRACK", "Ny multi-track tena izy dia mbola mila ampiana audio tracks sy playback engine.") },
            "📤  Export Audio" to { showInfo("EXPORT AUDIO", "Rehefa ampiana ny audio engine sy ny mixer dia azo amboarina ny export audio.") }
        )

        for (i in tools.indices step 2) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            for (j in i until minOf(i + 2, tools.size)) {
                val item = tools[j]
                val b = button(item.first) { item.second() }
                val params = LinearLayout.LayoutParams(
                    0,
                    88,
                    1f
                )
                params.setMargins(4, 4, 4, 4)
                row.addView(b, params)
            }

            if (tools.size % 2 == 1 && i + 1 >= tools.size) {
                row.addView(View(this), LinearLayout.LayoutParams(0, 88, 1f))
            }

            content.addView(row)
        }

        addGap(content, 20)

        content.addView(label("STATUS", 12f, muted))
        content.addView(label("●  Audio Elements engine: basic beta", 14f, accent))

        addGap(content, 20)
        content.addView(label("Tools are under development.", 11f, muted).apply {
            gravity = Gravity.CENTER
        })

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        setContentView(root)
    }

    private fun showRecording() {
        val root = baseLayout()
        addHeader(root, "VOCAL RECORDING")

        root.addView(label(
            "Raketo amin'ny microphone ny feonao. Tehirizina ao amin'ny Music/AudioElementsMax.",
            14f, muted
        ))
        addGap(root, 24)

        val status = label(
            if (recording) "● RECORDING" else "● READY",
            18f,
            if (recording) Color.RED else accent
        )
        root.addView(status)
        addGap(root, 16)

        val recordButton = button(
            if (recording) "■  Stop Recording" else "●  Start Recording"
        ) {
            if (recording) stopRecording() else requestRecordingPermission()
            showRecording()
        }

        root.addView(recordButton, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 64
        ))

        addGap(root, 12)
        root.addView(button("← Back to Studio") { showHome() })
        setContentView(root)
    }

    private fun requestRecordingPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 101)
        } else {
            startRecording()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording()
                showRecording()
            } else {
                Toast.makeText(this, "Mila alalana amin'ny microphone.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startRecording() {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME,
                    "AEM_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.m4a")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/AudioElementsMax")
                }
            }

            recordUri = contentResolver.insert(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values
            ) ?: throw Exception("Tsy afaka namorona rakitra audio.")

            recordDescriptor = contentResolver.openFileDescriptor(recordUri!!, "w")
                ?: throw Exception("Tsy afaka nanokatra rakitra audio.")

            recorder = if (Build.VERSION.SDK_INT >= 31) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recordDescriptor!!.fileDescriptor)
                prepare()
                start()
            }

            recording = true
            Toast.makeText(this, "Manomboka ny recording!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            recording = false
            recorder?.release()
            recorder = null
            recordDescriptor?.close()
            recordDescriptor = null
            recordUri?.let { contentResolver.delete(it, null, null) }
            recordUri = null
            Toast.makeText(this, "Tsy nety ny recording: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRecording() {
        try {
            recorder?.stop()
            Toast.makeText(this, "Voatahiry ny recording!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            recordUri?.let { contentResolver.delete(it, null, null) }
            Toast.makeText(this, "Tsy voatahiry tsara ilay recording.", Toast.LENGTH_LONG).show()
        } finally {
            recorder?.release()
            recorder = null
            recordDescriptor?.close()
            recordDescriptor = null
            recording = false
            recordUri = null
        }
    }

    private fun showEffects(title: String, controls: List<String>) {
        val root = baseLayout()
        addHeader(root, title)

        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        body.addView(label(
            "Réglage interface beta. Tsy mbola mampihatra DSP amin'ny feo ireo contrôles ireo.",
            13f, muted
        ))
        addGap(body, 20)

        controls.forEach { control ->
            val value = label("$control: 50%", 15f, white)
            body.addView(value)
            val seek = SeekBar(this).apply {
                max = 100
                progress = 50
                progressTintList = android.content.res.ColorStateList.valueOf(accent)
                thumbTintList = android.content.res.ColorStateList.valueOf(accent)
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        value.text = "$control: $progress%"
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
            }
            body.addView(seek)
            addGap(body, 14)
        }

        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
        ))
        root.addView(button("← Back to Studio") { showHome() })
        setContentView(root)
    }

    private fun showInfo(title: String, message: String) {
        val root = baseLayout()
        addHeader(root, title)
        root.addView(label(message, 15f, muted))
        addGap(root, 24)
        root.addView(button("← Back to Studio") { showHome() })
        setContentView(root)
    }

    private fun addHeader(root: LinearLayout, title: String) {
        root.addView(label(title, 22f, accent).apply {
            typeface = Typeface.DEFAULT_BOLD
        })
        addGap(root, 16)
    }

    override fun onDestroy() {
        if (recording) stopRecording()
        super.onDestroy()
    }
}
