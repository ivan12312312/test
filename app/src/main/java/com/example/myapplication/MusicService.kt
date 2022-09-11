package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.ui.PlayerNotificationManager

const val NOW_PLAYING_CHANNEL_ID = "com.example.android.uamp.media.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION_ID = 0xb339 // Arbitrary number used to identify our notification

open class MusicService : MediaBrowserServiceCompat() {
    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaController: MediaControllerCompat
    protected lateinit var notificationManager: PlayerNotificationManager
    protected lateinit var player: ExoPlayer
    protected var deviceMusics: MutableList<
        MediaBrowserCompat.MediaItem
    > = mutableListOf()
    protected var isForegroundService = false

    @SuppressLint("RestrictedApi")
    override fun onSubscribe(id: String?, option: Bundle?) {
        super.onSubscribe(id, option)
        notificationManager.invalidate()
    }

    override fun onCreate() {
        super.onCreate()

        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_GAME)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            setAcceptsDelayedFocusGain(true)
            setOnAudioFocusChangeListener { _ -> player.pause() }
            build()
        }

        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                isActive = true
            }

        sessionToken = mediaSession.sessionToken

        deviceMusics = loadDeviceMusics()

        player = ExoPlayer.Builder(this).build()
        player.repeatMode = Player.REPEAT_MODE_ALL;

        val mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(
                player: Player,
                windowIndex: Int
            ): MediaDescriptionCompat {
                if (windowIndex < deviceMusics.size) {
                    return deviceMusics[windowIndex].description
                }
                return MediaDescriptionCompat.Builder().build()
            }
        })
        mediaSessionConnector.setPlayer(player)

        mediaController = MediaControllerCompat(this, mediaSession.sessionToken)
        mediaController.registerCallback(object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                mediaController.playbackState
                if(state?.state == 3) {
                    audioManager.requestAudioFocus(focusRequest)
                }
            }
        })

        val notificationManagerBuilder = PlayerNotificationManager.Builder(
            this,
            NOW_PLAYING_NOTIFICATION_ID,
            NOW_PLAYING_CHANNEL_ID,
        )
        with(notificationManagerBuilder) {
            setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            setNotificationListener(PlayerNotificationListener())
            setChannelNameResourceId(R.string.notification_channel)
            setChannelDescriptionResourceId(R.string.notification_channel)
        }

        notificationManager = notificationManagerBuilder.build()
        notificationManager.setMediaSessionToken(
            mediaSession.sessionToken
        )

        notificationManager.setSmallIcon(R.drawable.ic_launcher_foreground)
        notificationManager.setUseRewindAction(false)
        notificationManager.setUseFastForwardAction(false)

        notificationManager.setPlayer(player)

        player.setMediaItems(deviceMusics.map { mi ->
            MediaItem.Builder()
                .setUri(mi.description.mediaUri!!)
                .setTag(mi)
                .build()
        })
        player.prepare()
    }

    private inner class PlayerNotificationListener :
        PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    private inner class DescriptionAdapter(private val controller: MediaControllerCompat) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun createCurrentContentIntent(player: Player): PendingIntent? =
            controller.sessionActivity

        override fun getCurrentContentText(player: Player): String {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentContentTitle(player: Player) = "title"

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            return null
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", Bundle())
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        var newDeviceMusics = loadDeviceMusics()
        var currentMusicsMap = hashMapOf<String?, MediaBrowserCompat.MediaItem>()
        deviceMusics.forEach {
            dm -> currentMusicsMap[dm.mediaId] = dm
        }
        newDeviceMusics.forEach { nDm ->
            run {
                if (!currentMusicsMap.containsKey(nDm.mediaId)) {
                    Log.e("addMediaItem", nDm.mediaId.toString())
                    player.addMediaItem(
                        MediaItem.Builder()
                            .setUri(nDm.description.mediaUri!!)
                            .setTag(nDm)
                            .build()
                    )
                }
            }
        }
        deviceMusics = newDeviceMusics

        result.sendResult(newDeviceMusics)
    }

    override fun onDestroy() {
        stopForeground(true)
        isForegroundService = false

        mediaSession.run {
            isActive = false
            release()
        }

        player.release()

        stopSelf()
    }

    fun loadDeviceMusics(): MutableList<MediaBrowserCompat.MediaItem> {
        val musics = mutableListOf<MediaBrowserCompat.MediaItem>()

        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.AudioColumns.TITLE,
            ),
            null,
            null,
            null,
        )
        if(cursor != null) {
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                if(!path.endsWith(".mp3")) {
                    continue;
                }
                val author = cursor.getString(1)
                val title = cursor.getString(2)

                val musicDesc = MediaDescriptionCompat.Builder()
                    .setMediaId(path)
                    .setMediaUri(Uri.parse(path))
                    .setTitle(title)
                    .setSubtitle(author)
                    .build()

                musics.add(
                    MediaBrowserCompat.MediaItem(
                        musicDesc,
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    )
                )
            }

            cursor.close();
        }

        return musics;
    }
}