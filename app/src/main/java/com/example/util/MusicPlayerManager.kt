package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.model.MusicTrackEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sin

/**
 * Singleton In-App Music Player Manager.
 * Supports playing local audio files, cloud/Google Drive imports,
 * and harmonized acoustic tone progressions for offline scorecards.
 */
object MusicPlayerManager {
    private const val TAG = "MusicPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private var synthAudioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null

    private val _currentTrack = MutableStateFlow<MusicTrackEntity?>(null)
    val currentTrack: StateFlow<MusicTrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var playlist: List<MusicTrackEntity> = emptyList()

    fun playTrack(track: MusicTrackEntity, list: List<MusicTrackEntity>, context: Context) {
        playlist = list
        _currentTrack.value = track
        _durationMs.value = if (track.durationMs > 0) track.durationMs else 180000L
        _currentPositionMs.value = 0L

        stopInternal()

        NotificationHelper.showMusicNotification(
            context = context,
            trackTitle = track.songName,
            artistName = track.artist,
            isPlaying = true
        )

        if (track.uriString.isNotBlank() && !track.uriString.startsWith("ledger://")) {
            try {
                val uri = Uri.parse(track.uriString)
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context.applicationContext, uri)
                    isLooping = _isLooping.value
                    prepareAsync()
                    setOnPreparedListener { player ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            try {
                                player.playbackParams = player.playbackParams.setSpeed(_playbackSpeed.value)
                            } catch (e: Exception) {
                                Log.w(TAG, "Speed setting not supported", e)
                            }
                        }
                        player.start()
                        _isPlaying.value = true
                        _durationMs.value = player.duration.toLong().coerceAtLeast(1000L)
                        startProgressUpdates()
                    }
                    setOnCompletionListener {
                        _isPlaying.value = false
                        if (_isLooping.value) {
                            seekTo(0L)
                            start()
                            _isPlaying.value = true
                        } else {
                            playNext(context)
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra. Falling back to synth.")
                        stopInternal()
                        startSynthMelody(track, context)
                        true
                    }
                }
                mediaPlayer = mp
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaPlayer for URI: ${track.uriString}", e)
                startSynthMelody(track, context)
            }
        } else {
            // Built-in / Sample / Offline score card synthesized acoustic notes
            startSynthMelody(track, context)
        }
    }

    private fun startSynthMelody(track: MusicTrackEntity, context: Context) {
        stopInternal()
        _isPlaying.value = true
        val trackDuration = if (track.durationMs > 0) track.durationMs else 180000L
        _durationMs.value = trackDuration

        // Acoustic piano/bell harmonic frequencies depending on category
        val baseFreqs = when (track.category.lowercase()) {
            "classical" -> doubleArrayOf(261.63, 329.63, 392.00, 523.25, 440.00, 349.23, 329.63, 261.63) // C major / Debussy
            "jazz" -> doubleArrayOf(293.66, 369.99, 440.00, 554.37, 493.88, 440.00, 369.99, 293.66) // D maj7
            "ambient" -> doubleArrayOf(220.00, 277.18, 329.63, 440.00, 329.63, 277.18, 220.00, 164.81) // A minor / Living Water
            else -> doubleArrayOf(261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 440.00, 392.00) // Melody Pentatonic
        }

        synthJob = scope.launch(Dispatchers.Default) {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(4096)

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            synthAudioTrack = audioTrack
            try {
                audioTrack.play()
                val noteDurationSamples = (sampleRate * 0.45).toInt()
                val buffer = ShortArray(noteDurationSamples)
                var noteIdx = 0

                while (isActive && _isPlaying.value) {
                    val freq = baseFreqs[noteIdx % baseFreqs.size]
                    for (i in 0 until noteDurationSamples) {
                        val t = i.toDouble() / sampleRate
                        // Soft envelope with gentle attack and warm decay
                        val envelope = (1.0 - (i.toDouble() / noteDurationSamples)) * 0.45
                        val sample = (sin(2.0 * Math.PI * freq * t) * envelope * Short.MAX_VALUE).toInt()
                        buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                    noteIdx++
                    delay(30)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Synth playback error", e)
            } finally {
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        }

        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                val mp = mediaPlayer
                if (mp != null) {
                    try {
                        if (mp.isPlaying) {
                            _currentPositionMs.value = mp.currentPosition.toLong()
                            _durationMs.value = mp.duration.toLong().coerceAtLeast(1000L)
                        }
                    } catch (_: Exception) {}
                } else {
                    // Simulated progress for synthesized scores
                    _currentPositionMs.value = (_currentPositionMs.value + 500L)
                    if (_currentPositionMs.value >= _durationMs.value) {
                        if (_isLooping.value) {
                            _currentPositionMs.value = 0L
                        } else {
                            _isPlaying.value = false
                            break
                        }
                    }
                }
                delay(500)
            }
        }
    }

    fun togglePlayPause(context: Context) {
        val current = _currentTrack.value ?: return
        if (_isPlaying.value) {
            pause()
        } else {
            resume(context)
        }
    }

    fun pause(context: Context? = null) {
        _isPlaying.value = false
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.pause()
            } catch (_: Exception) {}
        }
        synthJob?.cancel()
        synthJob = null

        val track = _currentTrack.value
        if (context != null && track != null) {
            NotificationHelper.showMusicNotification(
                context = context,
                trackTitle = track.songName,
                artistName = track.artist,
                isPlaying = false
            )
        }
    }

    fun resume(context: Context) {
        val track = _currentTrack.value ?: return
        if (mediaPlayer != null) {
            try {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressUpdates()
            } catch (e: Exception) {
                playTrack(track, playlist, context)
            }
        } else {
            startSynthMelody(track, context)
        }
        NotificationHelper.showMusicNotification(
            context = context,
            trackTitle = track.songName,
            artistName = track.artist,
            isPlaying = true
        )
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        mediaPlayer?.let {
            try {
                it.seekTo(positionMs.toInt())
            } catch (_: Exception) {}
        }
    }

    fun playNext(context: Context) {
        if (playlist.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = playlist.indexOfFirst { it.id == current?.id }
        val nextIndex = if (_isShuffle.value) {
            (playlist.indices).random()
        } else {
            if (currentIndex in playlist.indices) (currentIndex + 1) % playlist.size else 0
        }
        playTrack(playlist[nextIndex], playlist, context)
    }

    fun playPrevious(context: Context) {
        if (playlist.isEmpty()) return
        val current = _currentTrack.value
        val currentIndex = playlist.indexOfFirst { it.id == current?.id }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
        playTrack(playlist[prevIndex], playlist, context)
    }

    fun toggleLoop() {
        val newLoop = !_isLooping.value
        _isLooping.value = newLoop
        mediaPlayer?.isLooping = newLoop
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mediaPlayer?.let {
                try {
                    it.playbackParams = it.playbackParams.setSpeed(speed)
                } catch (_: Exception) {}
            }
        }
    }

    fun stop() {
        stopInternal()
        _currentTrack.value = null
        _isPlaying.value = false
        _currentPositionMs.value = 0L
    }

    private fun stopInternal() {
        progressJob?.cancel()
        progressJob = null
        synthJob?.cancel()
        synthJob = null
        try {
            synthAudioTrack?.stop()
            synthAudioTrack?.release()
        } catch (_: Exception) {}
        synthAudioTrack = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
    }
}
