package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.media.browse.MediaBrowser
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log

class MainActivity : AppCompatActivity() {
    lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("startConnecting", "*")
        mediaBrowser = MediaBrowserCompat(
            applicationContext,
            ComponentName(applicationContext, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    if(!mediaBrowser.isConnected) {
                        return
                    }

                    Log.e("onConnected", "*" + mediaBrowser.root,)

                    mediaBrowser.subscribe("/", object : MediaBrowserCompat.SubscriptionCallback() {
                        override fun onChildrenLoaded(
                            parentId: String,
                            children: MutableList<MediaBrowserCompat.MediaItem>
                        ) {
                            Log.e("mediaBrowser.subscribe", children.toString())
                        }
                    })

                    mediaController = MediaControllerCompat(
                        applicationContext,
                        mediaBrowser.sessionToken,
                    ).apply {
                        registerCallback(object : MediaControllerCompat.Callback() {
                            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                                Log.e(
                                    "MediaControllerCompat.callback.onPlaybackStateChanged",
                                    state.toString()
                                )
                            }
                            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                                Log.e(
                                    "MediaControllerCompat.callback.onMetadataChanged",
                                    metadata.toString()
                                )
                            }
                        })
                    }
                }
            },
            null
        ).apply { connect() }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.e("test", "destroy")
        mediaBrowser.unsubscribe("/")
        mediaBrowser.disconnect()
    }
}