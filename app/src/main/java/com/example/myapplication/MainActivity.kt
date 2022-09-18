package com.example.myapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.media.browse.MediaBrowser
import android.media.session.PlaybackState
import android.opengl.Visibility
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

class PlaylistItem(
    public val title: String,
    public val subtitle: String,
    public var isPlay: Boolean,
)

class PlaylistAdapter(
    private val dataSet: Array<PlaylistItem>,
    private val onItemClick: (Int) -> Unit
) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView
        val subtitle: TextView
        val playStopButton: ImageButton

        init {
            // Define click listener for the ViewHolder's View.
            title = view.findViewById(R.id.title)
            subtitle = view.findViewById(R.id.subtitle)
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
        val playlistItem = dataSet[position]
        viewHolder.title.text = playlistItem.title
        viewHolder.subtitle.text = playlistItem.subtitle
        viewHolder.playStopButton.setOnClickListener { onItemClick(position) }
        if(playlistItem.subtitle.isEmpty()) {
            viewHolder.subtitle.visibility = View.GONE
        } else {
            viewHolder.subtitle.visibility = View.VISIBLE
        }
        if(playlistItem.isPlay) {
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
    lateinit var mediaControllerCallback: MediaControllerCompat.Callback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaBrowser = MediaBrowserCompat(
            applicationContext,
            ComponentName(applicationContext, MusicService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
                @SuppressLint("UseCompatLoadingForDrawables")
                override fun onConnected() {
                    if(!mediaBrowser.isConnected) {
                        return
                    }

                    Log.e("root", mediaBrowser.root)

                    var currentList = mediaBrowser.root

                    val deviceMusicsList = findViewById<RecyclerView>(R.id.device_musics_list)
                    val stationsList = findViewById<RecyclerView>(R.id.radio_stations_list)

                    val switchToDeviceMusicsListBtn = findViewById<ImageButton>(
                        R.id.switch_to_list_device_musics
                    )
                    val switchToStationsListBtn = findViewById<ImageButton>(
                        R.id.switch_to_list_radio_stations
                    )

                    val progress = findViewById<SeekBar>(R.id.player_sound_progress)

                    val prevBtn = findViewById<ImageButton>(R.id.prevButton)
                    val playStopBtn = findViewById<ImageButton>(R.id.playStopButton)
                    val nextBtn = findViewById<ImageButton>(R.id.nextButton)

                    mediaBrowser.subscribe(Playlist.DEVICE_MUSICS.id, object : MediaBrowserCompat.SubscriptionCallback() {
                        override fun onChildrenLoaded(
                            parentId: String,
                            children: MutableList<MediaBrowserCompat.MediaItem>
                        ) {
                            val playlistItems = children.map { mi -> PlaylistItem(
                                mi.description.title.toString(),
                                mi.description.subtitle.toString(),
                                false
                            )}.toTypedArray()
                            val playlistAdapter = PlaylistAdapter(playlistItems){};
                            deviceMusicsList.adapter = playlistAdapter;
                            deviceMusicsList.layoutManager = LinearLayoutManager(this@MainActivity)

                            Log.e("test", children.toString())
                        }
                    })
                    mediaBrowser.subscribe(Playlist.RADIO_STATIONS.id, object : MediaBrowserCompat.SubscriptionCallback() {
                        override fun onChildrenLoaded(
                            parentId: String,
                            children: MutableList<MediaBrowserCompat.MediaItem>
                        ) {
                            val playlistItems = children.map { mi -> PlaylistItem(
                                mi.description.title.toString(),
                                mi.description.subtitle.toString(),
                                false
                            )}.toTypedArray()
                            val playlistAdapter = PlaylistAdapter(playlistItems){};
                            stationsList.adapter = playlistAdapter;
                            stationsList.layoutManager = LinearLayoutManager(this@MainActivity)
                            Log.e("test", children.toString())
                        }
                    })

                    val progressThumb = progress.thumb

                    switchToDeviceMusicsListBtn.setOnClickListener {
                        if(currentList != Playlist.DEVICE_MUSICS.id)  {
                            deviceMusicsList.visibility = View.VISIBLE
                            stationsList.visibility = View.GONE

                            progress.thumb = progressThumb

                            currentList = Playlist.DEVICE_MUSICS.id
                        }
                    }
                    switchToStationsListBtn.setOnClickListener {
                        if(currentList != Playlist.RADIO_STATIONS.id) {
                            stationsList.visibility = View.VISIBLE
                            deviceMusicsList.visibility = View.GONE

                            progress.thumb = null

                            currentList = Playlist.RADIO_STATIONS.id
                        }
                    }

//                    mediaBrowser.subscribe("/", object : MediaBrowserCompat.SubscriptionCallback() {
//                        override fun onChildrenLoaded(
//                            parentId: String,
//                            children: MutableList<MediaBrowserCompat.MediaItem>
//                        ) {
//                            val playerList = findViewById<RecyclerView>(R.id.player_list)
//                            val playerProgress = findViewById<SeekBar>(R.id.player_sound_progress)
//                            val prevBtn = findViewById<ImageButton>(R.id.prev)
//                            val playStopBtn = findViewById<ImageButton>(R.id.run)
//                            val nextBtn = findViewById<ImageButton>(R.id.next)
//
//                            val musics = children.map {
//                                mi -> Music(
//                                    mi.description.title.toString(),
//                                    mi.description.subtitle.toString(),
//                                false
//                                )
//                            }
//                            if(musics.isEmpty()) {
//                                return
//                            }
//
//                            playerList.adapter = PlayerListAdapter(
//                                musics.toTypedArray()
//                            ) { musicId ->
//                                if (mediaController.playbackState.activeQueueItemId.toInt() == musicId) {
//                                    if(mediaController.playbackState.state == PlaybackState.STATE_PLAYING) {
//                                        mediaController.transportControls.pause()
//                                    } else {
//                                        mediaController.transportControls.play()
//                                    }
//                                } else {
//                                    mediaController.transportControls.skipToQueueItem(musicId.toLong())
//                                    mediaController.transportControls.play()
//                                }
//                            }
//
//
//                            mediaController = MediaControllerCompat(
//                                applicationContext,
//                                mediaBrowser.sessionToken,
//                            )
//
//                            mediaController.sendCommand("testCommand", null, null);
//
//                            var currentMusicId: Long = mediaController.playbackState.activeQueueItemId
//                            when(mediaController.playbackState.state) {
//                                PlaybackState.STATE_PLAYING -> {
//                                    musics[currentMusicId.toInt()].isPlay = true
//                                    playStopBtn.setImageResource(R.drawable.ic_pause)
//                                }
//                                else -> {
//                                    playStopBtn.setImageResource(R.drawable.ic_play)
//                                }
//                            }
//
//                            mediaControllerCallback = object : MediaControllerCompat.Callback() {
//                                @SuppressLint("NotifyDataSetChanged")
//                                override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
//                                    if(
//                                        state == null ||
//                                        state.state == PlaybackState.STATE_BUFFERING
//                                    ) {
//                                        return
//                                    }
//
//                                    musics[currentMusicId.toInt()].isPlay = false
//                                    when(state.state) {
//                                        PlaybackState.STATE_PLAYING -> {
//                                            musics[state.activeQueueItemId.toInt()].isPlay = true
//                                            playStopBtn.setImageResource(R.drawable.ic_pause)
//                                        }
//                                        else -> {
//                                            playStopBtn.setImageResource(R.drawable.ic_play)
//                                        }
//                                    }
//                                    currentMusicId = mediaController.playbackState.activeQueueItemId
//                                    playerList.adapter?.notifyDataSetChanged()
//
//                                    playerProgress.progress = state.position.toInt()
//                                }
//                                override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
//                                    playerProgress.max = mediaController
//                                        .metadata
//                                        .getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
//                                        .toInt()
//                                }
//                            }
//                            mediaController.registerCallback(mediaControllerCallback)
//
//                            var isSkipSeekBarUpdate = false
//                            playerProgress.max = mediaController
//                                .metadata
//                                .getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
//                                .toInt()
//
//                            val handler = Handler()
//                            handler.postDelayed(object : Runnable{
//                                override fun run() {
//                                    handler.postDelayed(this, 1000)
//                                    if(isSkipSeekBarUpdate) {
//                                        return
//                                    }
//                                    playerProgress.progress = mediaController.playbackState.position.toInt()
//                                }
//                            }, 0)
//                            playerProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                                override fun onStartTrackingTouch(seekbar: SeekBar?) {
//                                    isSkipSeekBarUpdate = true
//                                }
//                                override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                                    mediaController.transportControls.seekTo(
//                                        playerProgress.progress.toLong()
//                                    )
//                                    isSkipSeekBarUpdate = false
//                                }
//
//                                override fun onProgressChanged(
//                                    seekBar: SeekBar?,
//                                    position: Int,
//                                    isUser: Boolean
//                                ) {}
//                            })
//
//                            prevBtn.setOnClickListener {
//                                mediaController.transportControls.skipToPrevious()
//                            }
//                            playStopBtn.setOnClickListener {
//                                when(mediaController.playbackState.state) {
//                                    PlaybackState.STATE_PAUSED -> {
//                                        mediaController.transportControls.play()
//                                    }
//                                    else -> {
//                                        mediaController.transportControls.pause()
//                                    }
//                                }
//                            }
//                            nextBtn.setOnClickListener {
//                                mediaController.transportControls.skipToNext()
//                            }
//
//                            playerList.layoutManager = LinearLayoutManager(applicationContext)
//                        }
//                    })
                }
            },
            null
        ).apply { connect() }
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaController.unregisterCallback(mediaControllerCallback)

        mediaBrowser.unsubscribe("/")
        mediaBrowser.disconnect()
    }
}