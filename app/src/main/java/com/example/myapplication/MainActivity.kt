package com.example.myapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.media.browse.MediaBrowser
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Music(
    public val title: String,
    public val subtitle: String,
    public var isPlay: Boolean,
)

class PlayerListAdapter(
    private val dataSet: Array<Music>,
    private val onMusicClick: (Int) -> Unit
) :
    RecyclerView.Adapter<PlayerListAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val musicName: TextView
        val musicAuthor: TextView
        val playStopButton: ImageButton

        init {
            // Define click listener for the ViewHolder's View.
            musicName = view.findViewById(R.id.music_name)
            musicAuthor = view.findViewById(R.id.music_author)
            playStopButton = view.findViewById(R.id.music_play_stop)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.music_card, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val music = dataSet[position]
        viewHolder.musicName.text = music.title
        viewHolder.musicAuthor.text = music.subtitle
        viewHolder.playStopButton.setOnClickListener { onMusicClick(position) }
        if (music.isPlay) {
            viewHolder.playStopButton.setImageResource(R.drawable.ic_pausesmall)
        } else {
            viewHolder.playStopButton.setImageResource(R.drawable.ic_playsmall)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size
}

class MainActivity : AppCompatActivity() {
    lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowser = MediaBrowserCompat(
            applicationContext,
            ComponentName(applicationContext, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                override fun onConnected() {
                    if(!mediaBrowser.isConnected) {
                        return
                    }

                    mediaBrowser.subscribe("/", object : MediaBrowserCompat.SubscriptionCallback() {
                        override fun onChildrenLoaded(
                            parentId: String,
                            children: MutableList<MediaBrowserCompat.MediaItem>
                        ) {
                            val playerList = findViewById<RecyclerView>(R.id.player_list)
                            val playerProgress = findViewById<SeekBar>(R.id.player_sound_progress)
                            val prevBtn = findViewById<ImageButton>(R.id.prev)
                            val playStopBtn = findViewById<ImageButton>(R.id.run)
                            val nextBtn = findViewById<ImageButton>(R.id.next)

                            val musics = children.map {
                                mi -> Music(
                                    mi.description.title.toString(),
                                    mi.description.subtitle.toString(),
                                false
                                )
                            }
                            if(musics.isEmpty()) {
                                return
                            }

                            playerList.adapter = PlayerListAdapter(
                                musics.toTypedArray()
                            ) { musicId ->
                                if (mediaController.playbackState.activeQueueItemId.toInt() == musicId) {
                                    if(mediaController.playbackState.state == PlaybackState.STATE_PLAYING) {
                                        mediaController.transportControls.pause()
                                    } else {
                                        mediaController.transportControls.play()
                                    }
                                } else {
                                    mediaController.transportControls.skipToQueueItem(musicId.toLong())
                                    mediaController.transportControls.play()
                                }
                            }


                            mediaController = MediaControllerCompat(
                                applicationContext,
                                mediaBrowser.sessionToken,
                            )
                            var currentMusicId: Long = mediaController.playbackState.activeQueueItemId
                            when(mediaController.playbackState.state) {
                                PlaybackState.STATE_PLAYING -> {
                                    musics[currentMusicId.toInt()].isPlay = true
                                    playStopBtn.setImageResource(R.drawable.ic_pause)
                                }
                                else -> {
                                    playStopBtn.setImageResource(R.drawable.ic_play)
                                }
                            }
                            mediaController.registerCallback(
                                object : MediaControllerCompat.Callback() {
                                    @SuppressLint("NotifyDataSetChanged")
                                    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                                        if(
                                            state == null ||
                                            state.state == PlaybackState.STATE_BUFFERING
                                        ) {
                                            return
                                        }

                                        musics[currentMusicId.toInt()].isPlay = false
                                        when(state.state) {
                                            PlaybackState.STATE_PLAYING -> {
                                                musics[state.activeQueueItemId.toInt()].isPlay = true
                                                playStopBtn.setImageResource(R.drawable.ic_pause)
                                            }
                                            else -> {
                                                playStopBtn.setImageResource(R.drawable.ic_play)
                                            }
                                        }
                                        currentMusicId = mediaController.playbackState.activeQueueItemId
                                        playerList.adapter?.notifyDataSetChanged()

                                        playerProgress.progress = state.position.toInt()
                                    }
                                    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                                        playerProgress.max = mediaController
                                            .metadata
                                            .getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                                            .toInt()
                                    }
                                }
                            )

                            var isSkipSeekBarUpdate = false
                            playerProgress.max = mediaController
                                .metadata
                                .getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                                .toInt()

                            val handler = Handler()
                            handler.postDelayed(object : Runnable{
                                override fun run() {
                                    handler.postDelayed(this, 1000)
                                    if(isSkipSeekBarUpdate) {
                                        return
                                    }
                                    playerProgress.progress = mediaController.playbackState.position.toInt()
                                }
                            }, 0)
                            playerProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                                override fun onStartTrackingTouch(seekbar: SeekBar?) {
                                    isSkipSeekBarUpdate = true
                                }
                                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                    mediaController.transportControls.seekTo(
                                        playerProgress.progress.toLong()
                                    )
                                    isSkipSeekBarUpdate = false
                                }

                                override fun onProgressChanged(
                                    seekBar: SeekBar?,
                                    position: Int,
                                    isUser: Boolean
                                ) {}
                            })

                            prevBtn.setOnClickListener {
                                mediaController.transportControls.skipToPrevious()
                            }
                            playStopBtn.setOnClickListener {
                                when(mediaController.playbackState.state) {
                                    PlaybackState.STATE_PAUSED -> {
                                        mediaController.transportControls.play()
                                    }
                                    else -> {
                                        mediaController.transportControls.pause()
                                    }
                                }
                            }
                            nextBtn.setOnClickListener {
                                mediaController.transportControls.skipToNext()
                            }

                            playerList.layoutManager = LinearLayoutManager(applicationContext)
                        }
                    })
                }
            },
            null
        ).apply { connect() }
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaBrowser.disconnect()
    }
}