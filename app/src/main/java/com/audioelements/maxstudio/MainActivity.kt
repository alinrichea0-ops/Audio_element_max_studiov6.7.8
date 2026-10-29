package com.example.audioelementsmax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import kotlin.random.Random

// ---------- Modèles ----------

data class Track(
    val name: String,
    val color: Color,
    var volume: Float,
    var muted: Boolean = false,
    var solo: Boolean = false
)

data class Clip(
    val name: String,
    val trackIndex: Int,
    val startBar: Float,
    val lengthBars: Float,
    val color: Color,
    val amplitudes: List<Float>
)

// ---------- Données de démo ----------

val demoTracks = listOf(
    Track("Vocal", Color(0xFF22C55E), 0.6f),
    Track("Beat", Color(0xFF3B82F6), 0.7f),
    Track("Keyboard", Color(0xFF8B5CF6), 0.65f),
    Track("Guitar", Color(0xFFF59E0B), 0.68f),
    Track("Bass", Color(0xFFEF4444), 0.66f)
)

fun fakeAmplitudes(n: Int = 200, seed: Int): List<Float> {
    val r = Random(seed)
    return List(n) { i ->
        (0.3f + 0.7f * (0.5f + 0.5f * sin(i * 0.3f)) * r.nextFloat()).coerceIn(0.05f, 1f)
    }
}

val demoClips = listOf(
    Clip("Vocal_01", 0, 1f, 5.9f, Color(0xFF22C55E), fakeAmplitudes(seed = 1)),
    Clip("Vocal_02", 0, 10f, 4.5f, Color(0xFF22C55E), fakeAmplitudes(seed = 2)),
    Clip("Beat_01", 1, 1f, 9.0f, Color(0xFF3B82F6), fakeAmplitudes(seed = 3)),
    Clip("Keyboard_01", 2, 1f, 8.0f, Color(0xFF8B5CF6), fakeAmplitudes(seed = 4)),
    Clip("Keyboard_02", 2, 10.1f, 6.0f, Color(0xFF8B5CF6), fakeAmplitudes(seed = 5)),
    Clip("Guitar_01", 3, 1f, 6.5f, Color(0xFFF59E0B), fakeAmplitudes(seed = 6)),
    Clip("Guitar_02", 3, 9.9f, 5.6f, Color(0xFFF59E0B), fakeAmplitudes(seed = 7)),
    Clip("Bass_01", 4, 1f, 9.5f, Color(0xFFEF4444), fakeAmplitudes(seed = 8))
)

// ---------- Activité ----------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DawScreen()
            }
        }
    }
}

// ---------- Écran principal ----------

@Composable
fun DawScreen() {
    val tracks = remember { mutableStateListOf(*demoTracks.toTypedArray()) }
    var isPlaying by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            kotlinx.coroutines.delay(1000)
            seconds++
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B1220))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TransportBar(
            isPlaying = isPlaying,
            isRecording = isRecording,
            time = seconds,
            onPlay = { isPlaying = !isPlaying },
            onStop = { isPlaying = false; seconds = 0 },
            onRec = { isRecording = !isRecording },
            onAddTrack = {}
        )

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrackList(
                tracks = tracks,
                modifier = Modifier.weight(0.35f)
            )
            Timeline(
                tracks = tracks,
                clips = demoClips,
                modifier = Modifier.weight(0.65f)
            )
        }

        MixerPanel(
            tracks = tracks,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

// ---------- Transport ----------

@Composable
fun TransportBar(
    isPlaying: Boolean,
    isRecording: Boolean,
    time: Int,
    onPlay: () -> Unit,
    onStop: () -> Unit,
    onRec: () -> Unit,
    onAddTrack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onRec,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRecording) Color(0xFFB91C1C) else Color(0xFF7F1D1D)
            )
        ) { Text("● REC") }

        Button(
            onClick = onPlay,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14B8A6))
        ) { Text(if (isPlaying) "❚❚ PAUSE" else "▶ PLAY") }

        Button(onClick = onStop) { Text("■ STOP") }

        Spacer(Modifier.weight(1f))

        Text(
            text = formatTime(time),
            color = Color(0xFF5EEAD4),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )

        Spacer(Modifier.weight(1f))

        OutlinedButton(onClick = onAddTrack) { Text("+ TRACK") }
    }
}

fun formatTime(total: Int): String {
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

// ---------- Liste des pistes ----------

@Composable
fun TrackList(tracks: List<Track>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF111A2E), RoundedCornerShape(8.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("TRACKS", color = Color(0xFF5EEAD4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        tracks.forEach { track ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                    .padding(6.dp)
            ) {
                Text(
                    track.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(track.color, RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "%.1f dB".format(20 * kotlin.math.log10(track.volume.coerceAtLeast(0.001f))),
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ---------- Timeline ----------

@Composable
fun Timeline(
    tracks: List<Track>,
    clips: List<Clip>,
    modifier: Modifier = Modifier
) {
    val rowHeight = 56.dp
    val totalBars = 16f

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
    ) {
        // Règle
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        ) {
            val barWidth = size.width / totalBars
            for (i in 0..totalBars.toInt()) {
                val x = i * barWidth
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(x, size.height * 0.5f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
        }

        // Pistes + clips
        tracks.forEachIndexed { index, track ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .padding(vertical = 2.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / totalBars
                    clips.filter { it.trackIndex == index }.forEach { clip ->
                        val left = (clip.startBar - 1f) * barWidth
                        val width = clip.lengthBars * barWidth
                        drawRoundRect(
                            color = clip.color.copy(alpha = 0.85f),
                            topLeft = Offset(left, 0f),
                            size = Size(width, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
                        )
                        // Waveform
                        val centerY = size.height / 2f
                        val step = width / clip.amplitudes.size
                        clip.amplitudes.forEachIndexed { i, amp ->
                            val x = left + i * step
                            val h = amp * size.height * 0.4f
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(x, centerY - h),
                                end = Offset(x, centerY + h),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- Mixer ----------

@Composable
fun MixerPanel(tracks: MutableList<Track>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFF111A2E), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tracks.forEachIndexed { index, track ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(track.name, color = Color.White, fontSize = 11.sp, maxLines = 1)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = track.muted,
                        onCheckedChange = { track.muted = it; tracks[index] = track.copy() }
                    )
                }

                Slider(
                    value = track.volume,
                    onValueChange = { tracks[index] = track.copy(volume = it) },
                    modifier = Modifier.height(80.dp)
                )

                Text(
                    "%.1f dB".format(20 * kotlin.math.log10(track.volume.coerceAtLeast(0.001f))),
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
