package com.sensoguard.hunter.classes

import android.app.Activity
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import javax.inject.Singleton

class VideoManager(val callback: Callback) {

    private var playbackStateListener: Player.Listener
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentItem = 0
    private var playbackPosition = 0L

    interface Callback {
        fun stopProgressBar()
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            // inspection fix: removed the unused state string, only READY has an action
            if (playbackState == ExoPlayer.STATE_READY) {
                callback.stopProgressBar()
            }
        }
    }

    /**
     * release video
     */
    fun releasePlayer() {
        player?.let { exoPlayer ->
            playbackPosition = exoPlayer.currentPosition
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }


    init {
        playbackStateListener = playbackStateListener()
    }

    companion object {

        @Volatile
        private var instance: Singleton? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: Singleton().also { instance = it }
            }
    }

    /**
     * initialize video
     */
    @OptIn(UnstableApi::class)
    fun initializePlayer(ivMyVideo: PlayerView?, activity: Activity, imgPath: String) {
        val trackSelector = DefaultTrackSelector(activity).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        player = ExoPlayer.Builder(activity)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->
                ivMyVideo?.player = exoPlayer

                val mediaItem: MediaItem = MediaItem.fromUri(imgPath.toUri()) // lint fix: KTX toUri
                ivMyVideo?.player?.setMediaItem(mediaItem)
                ivMyVideo?.player?.playWhenReady = playWhenReady
                ivMyVideo?.player?.seekTo(currentItem, playbackPosition)
                ivMyVideo?.player?.addListener(playbackStateListener)
                ivMyVideo?.player?.prepare()
                //ivMyVideo?.player?.play()
            }
    }
}
