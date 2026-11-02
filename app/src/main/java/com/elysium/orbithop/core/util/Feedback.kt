package com.elysium.orbithop.core.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.elysium.orbithop.R
import com.elysium.orbithop.core.store.LocalStore

/** Retours sonores et haptiques (exigence « retours immédiats »). */
object Feedback {

    enum class Sound { HOP, STAR, CRASH, CLICK, REWARD }

    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<Sound, Int>()
    private var vibrator: Vibrator? = null
    private var music: MediaPlayer? = null

    fun init(context: Context) {
        if (soundPool != null) return
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build().also { pool ->
            soundIds[Sound.HOP] = pool.load(context, R.raw.sfx_hop, 1)
            soundIds[Sound.STAR] = pool.load(context, R.raw.sfx_star, 1)
            soundIds[Sound.CRASH] = pool.load(context, R.raw.sfx_crash, 1)
            soundIds[Sound.CLICK] = pool.load(context, R.raw.sfx_click, 1)
            soundIds[Sound.REWARD] = pool.load(context, R.raw.sfx_reward, 1)
        }

        vibrator = if (Build.VERSION.SDK_INT >= 31) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        music = MediaPlayer.create(context, R.raw.music_loop)?.apply {
            isLooping = true
            setVolume(0.3f, 0.3f)
        }
    }

    fun play(sound: Sound) {
        if (!LocalStore.soundEnabled) return
        val id = soundIds[sound] ?: return
        soundPool?.play(id, 0.75f, 0.75f, 1, 0, 1f)
    }

    fun startMusic() {
        if (!LocalStore.soundEnabled) return
        val m = music ?: return
        if (!m.isPlaying) runCatching { m.start() }
    }

    fun pauseMusic() {
        runCatching { music?.pause() }
    }

    fun release() {
        runCatching {
            soundPool?.release()
            music?.release()
        }
        soundPool = null
        music = null
    }

    /** Applique un changement du réglage son (relance ou coupe la musique). */
    fun applySoundSetting() {
        if (LocalStore.soundEnabled) startMusic() else pauseMusic()
    }

    fun vibrate(milliseconds: Long, amplitude: Int = 120) {
        if (!LocalStore.vibrationEnabled) return
        val v = vibrator ?: return
        runCatching {
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(milliseconds, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(milliseconds)
            }
        }
    }
}
