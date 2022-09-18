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
import android.media.session.MediaSession
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

enum class Playlist(val id: String) {
    DEVICE_MUSICS("DEVICE_MUSICS"),
    RADIO_STATIONS("STATIONS")
}

open class MusicService : MediaBrowserServiceCompat() {
    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector
    protected lateinit var mediaController: MediaControllerCompat
    protected lateinit var notificationManager: PlayerNotificationManager

    protected lateinit var deviceMusicsPlayer: ExoPlayer
    protected lateinit var radioStationsPlayer: ExoPlayer

    protected var playlist = Playlist.DEVICE_MUSICS
    protected var stationsList: MutableList<
        MediaBrowserCompat.MediaItem
    > = StationsList.map { station ->
        MediaBrowserCompat.MediaItem(
            MediaDescriptionCompat
                .Builder()
                .setMediaId(station.uri)
                .setMediaUri(Uri.parse(station.uri))
                .setTitle(station.title)
                .setSubtitle("")
                .build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }.toMutableList()
    protected var deviceMusicsList: MutableList<
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
            setOnAudioFocusChangeListener { run {
                deviceMusicsPlayer.pause()
                radioStationsPlayer.pause()
            }}
            build()
        }

        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                isActive = true
            }

        sessionToken = mediaSession.sessionToken

        deviceMusicsList = loadDeviceMusics()

        deviceMusicsPlayer = ExoPlayer.Builder(this).build()
        deviceMusicsPlayer.repeatMode = Player.REPEAT_MODE_ALL;
        radioStationsPlayer = ExoPlayer.Builder(this).build()
        radioStationsPlayer.repeatMode = Player.REPEAT_MODE_ALL;


        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
            override fun getMediaDescription(
                player: Player,
                windowIndex: Int
            ): MediaDescriptionCompat {
                if(playlist === Playlist.DEVICE_MUSICS) {
                    if (windowIndex < deviceMusicsList.size) {
                        return deviceMusicsList[windowIndex].description
                    }
                } else {
                    if (windowIndex < stationsList.size) {
                        return stationsList[windowIndex].description
                    }
                }

                return MediaDescriptionCompat.Builder().build()
            }
        })

        mediaSessionConnector.registerCustomCommandReceiver { _, command, b, _ ->
            Log.e("get command 2", command)
            if(command == "switch player to") {
                Log.e("get command 2", command)
                val newPlayerId = b?.getString("player")
                    ?: return@registerCustomCommandReceiver true

                if(newPlayerId == Playlist.RADIO_STATIONS.id) {
                    deviceMusicsPlayer.pause()
                    mediaSessionConnector.setPlayer(radioStationsPlayer)
                    notificationManager.setPlayer(radioStationsPlayer)
                    playlist = Playlist.RADIO_STATIONS
                } else {
                    radioStationsPlayer.pause()
                    mediaSessionConnector.setPlayer(deviceMusicsPlayer)
                    notificationManager.setPlayer(deviceMusicsPlayer)
                    playlist = Playlist.DEVICE_MUSICS
                }
                return@registerCustomCommandReceiver true
            }
            false
        }

        mediaSessionConnector.setPlayer(deviceMusicsPlayer)

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

        notificationManager.setPlayer(deviceMusicsPlayer)

        deviceMusicsPlayer.setMediaItems(deviceMusicsList.map { mi ->
            MediaItem.Builder()
                .setUri(mi.description.mediaUri!!)
                .setTag(mi)
                .build()
        })
        deviceMusicsPlayer.prepare()

        radioStationsPlayer.setMediaItems(stationsList.map { mi ->
            MediaItem.Builder()
                .setUri(mi.description.mediaUri!!)
                .setTag(mi)
                .build()
        })
        radioStationsPlayer.prepare()
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
        return BrowserRoot(playlist.id, Bundle())
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if(parentId == playlist.id) {
            var newDeviceMusics = loadDeviceMusics()
            var currentMusicsMap = hashMapOf<String?, Pair<Int, MediaBrowserCompat.MediaItem>>()
            deviceMusicsList.forEachIndexed {
                    index, dm -> currentMusicsMap[dm.mediaId] = Pair(index, dm)
            }
            newDeviceMusics.forEach { nDm ->
                run {
                    if (!currentMusicsMap.containsKey(nDm.mediaId)) {
                        Log.e("addMediaItem", nDm.mediaId.toString())
                        deviceMusicsPlayer.addMediaItem(
                            MediaItem.Builder()
                                .setUri(nDm.description.mediaUri!!)
                                .setTag(nDm)
                                .build()
                        )
                    } else {
                        currentMusicsMap.remove(nDm.mediaId)
                    }
                }
            }
            currentMusicsMap.values.toTypedArray()
                .map { entry -> entry.first }
                .sorted()
                .reversed()
                .forEach {
                        removeMediaIndex -> deviceMusicsPlayer.removeMediaItem(removeMediaIndex)
                }

            deviceMusicsList = newDeviceMusics

            result.sendResult(deviceMusicsList)
        } else {
            result.sendResult(stationsList)
        }
    }

    override fun onDestroy() {
        stopForeground(true)
        isForegroundService = false

        mediaSession.run {
            isActive = false
            release()
        }

        deviceMusicsPlayer.release()
        radioStationsPlayer.release()

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