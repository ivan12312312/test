package com.example.myapplication

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.drm.DrmStore.Playback
import android.media.browse.MediaBrowser
import android.media.session.PlaybackState
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
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
import kotlin.properties.Delegates

class PlaylistItem(
    public val id: Int,
    public val title: String,
    public val subtitle: String,
    public var isPlay: Boolean,
)

class PlaylistAdapter(
    private val dataSet: Array<PlaylistItem>
) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    var onItemClick: (Int) -> Unit = {}

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
        viewHolder.playStopButton.setOnClickListener { onItemClick(playlistItem.id) }
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
                @SuppressLint("NotifyDataSetChanged")
                override fun onConnected() {
                    if(!mediaBrowser.isConnected) {
                        return
                    }

                    lateinit var deviceMusicsListAdapter: PlaylistAdapter
                    lateinit var deviceMusicsListAdapterDataSet: Array<PlaylistItem>
                    val deviceMusicsList = findViewById<RecyclerView>(R.id.device_musics_list)
                    lateinit var radioStationsListAdapter: PlaylistAdapter
                    lateinit var radioStationsListAdapterDataSet: Array<PlaylistItem>
                    val radioStationsList = findViewById<RecyclerView>(R.id.radio_stations_list)

                    val switchToDeviceMusicsListBtn = findViewById<ImageButton>(
                        R.id.switch_to_list_device_musics
                    )
                    val switchToStationsListBtn = findViewById<ImageButton>(
                        R.id.switch_to_list_radio_stations
                    )

                    val progress = findViewById<SeekBar>(R.id.player_sound_progress)
                    val progressThumb = progress.thumb

                    val prevBtn = findViewById<ImageButton>(R.id.prevButton)
                    val playStopBtn = findViewById<ImageButton>(R.id.playStopButton)
                    val nextBtn = findViewById<ImageButton>(R.id.nextButton)

                    var playSoundFrom: String by Delegates.observable("") { _, old, new ->
                        if(old == new) {
                            return@observable
                        }

                        if(new == Playlist.DEVICE_MUSICS.name) {
                            progress.thumb = progressThumb
                            progress.isEnabled = true
                        } else {
                            progress.thumb = null
                            progress.progress = 0
                            progress.max = 1
                            progress.isEnabled = false
                        }
                    }

                    playSoundFrom = mediaBrowser.root

                    var currentViewList: String by Delegates.observable("") { _, old, new ->
                        if(old == new) {
                            return@observable
                        }

                        if(new == Playlist.DEVICE_MUSICS.name) {
                            deviceMusicsList.visibility = View.VISIBLE
                            radioStationsList.visibility = View.GONE
                        } else {
                            radioStationsList.visibility = View.VISIBLE
                            deviceMusicsList.visibility = View.VISIBLE
                        }
                    }
                    currentViewList = mediaBrowser.root

                    var currentMusicId = -1
                    var currentMusicIsPlay: Boolean by Delegates.observable(false) { _, old, new ->
                        if(old == new) {
                            return@observable
                        }

                        Log.e("observer worked", "*")

                        if(new) {
                            playStopBtn.setImageResource(R.drawable.ic_pause)
                        } else {
                            playStopBtn.setImageResource(R.drawable.ic_play)
                        }
                    }

                    var loadedList: Int by Delegates.observable(0) { _, _, count ->
                        if(count != 2) {
                            return@observable
                        }

                        mediaController = MediaControllerCompat(
                            applicationContext,
                            mediaBrowser.sessionToken,
                        )

                        currentMusicId     = mediaController.playbackState.activeQueueItemId.toInt()
                        currentMusicIsPlay =
                            mediaController.playbackState.state == PlaybackState.STATE_PLAYING ||
                            mediaController.playbackState.state == PlaybackState.STATE_BUFFERING

                        if(currentMusicIsPlay) {
                            when(playSoundFrom) {
                                Playlist.DEVICE_MUSICS.name -> {
                                    deviceMusicsListAdapterDataSet[currentMusicId].isPlay = true
                                }
                                Playlist.ONLINE_RADIO_STATIONS.name -> {
                                    radioStationsListAdapterDataSet[currentMusicId].isPlay = true
                                }
                            }
                        }

                        deviceMusicsList.layoutManager = LinearLayoutManager(this@MainActivity)
                        radioStationsList.layoutManager = LinearLayoutManager(this@MainActivity)

                        mediaControllerCallback = object : MediaControllerCompat.Callback() {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                                if(
                                    state == null ||
                                    state.state == PlaybackState.STATE_NONE
                                ) {
                                    return
                                }

                                Log.e("debug", state?.state.toString())
                                Log.e("debug", state?.activeQueueItemId.toString())

                                val currentPlaylistAdapter =
                                    if(playSoundFrom == Playlist.DEVICE_MUSICS.name) {
                                        deviceMusicsListAdapter
                                    } else {
                                        radioStationsListAdapter
                                    }
                                val currentPlaylistAdapterDataset =
                                    if(playSoundFrom == Playlist.DEVICE_MUSICS.name) {
                                        deviceMusicsListAdapterDataSet
                                    } else {
                                        radioStationsListAdapterDataSet
                                    }

                                if(
                                    currentMusicId == state.activeQueueItemId.toInt() &&
                                    currentPlaylistAdapterDataset[
                                       state.activeQueueItemId.toInt()
                                    ].isPlay &&
                                    (state.state == PlaybackState.STATE_PLAYING ||
                                     state.state == PlaybackState.STATE_BUFFERING)
                                ) {
                                    return
                                }
                                currentPlaylistAdapterDataset[currentMusicId].isPlay = false
                                when(state.state) {
                                    PlaybackState.STATE_PLAYING,
                                    PlaybackState.STATE_BUFFERING -> {
                                        currentPlaylistAdapterDataset[
                                           state.activeQueueItemId.toInt()
                                        ].isPlay = true
                                        currentMusicIsPlay = true
                                    }
                                    else -> {
                                        currentMusicIsPlay = false
                                    }
                                }
                                currentMusicId = mediaController.playbackState.activeQueueItemId.toInt()
                                currentPlaylistAdapter.notifyDataSetChanged()

                                if(playSoundFrom == Playlist.DEVICE_MUSICS.name) {
                                    progress.progress = state.position.toInt()
                                }
                            }
                            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                                if(playSoundFrom == Playlist.DEVICE_MUSICS.name) {
                                    progress.progress = mediaController
                                        .playbackState
                                        .position
                                        .toInt()
                                    progress.max = mediaController
                                        .metadata
                                        .getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                                        .toInt()
                                }
                            }
                        }
                        mediaController.registerCallback(mediaControllerCallback)

                        var changeMusicOrPlayStop = {
                            musicId: Int ->
                            if (mediaController.playbackState.activeQueueItemId.toInt() == musicId) {
                                if(mediaController.playbackState.state == PlaybackState.STATE_PLAYING) {
                                    mediaController.transportControls.pause()
                                } else {
                                    if(playSoundFrom == Playlist.ONLINE_RADIO_STATIONS.name) {
                                        mediaController.transportControls.seekTo(Long.MAX_VALUE)
                                    }
                                    mediaController.transportControls.play()
                                }
                            } else {
                                mediaController.transportControls.skipToQueueItem(musicId.toLong())
                                mediaController.transportControls.play()
                            }
                        }
                        var switchPlaylist = {
                            playlist: Playlist,
                            selectedMusic: Int,
                            onSwitch: () -> Unit
                            ->
                            if(playSoundFrom == Playlist.DEVICE_MUSICS.name) {
                                deviceMusicsListAdapterDataSet.forEach { i -> i.isPlay = false }
                                deviceMusicsListAdapter.notifyDataSetChanged()
                            } else {
                                radioStationsListAdapterDataSet.forEach { i -> i.isPlay = false }
                                radioStationsListAdapter.notifyDataSetChanged()
                            }
                            progress.progress = 0
                            playSoundFrom = playlist.name
                            val params = Bundle()
                            params.putString("playlist", playlist.name)
                            params.putInt("selectedMusic", selectedMusic)
                            Log.e("test", "send command")
                            mediaController.sendCommand(
                                MusicServiceCommands.SWITCH_PLAYLIST.name,
                                params,
                                object : ResultReceiver(null) {
                                    override fun onReceiveResult(
                                        resultCode: Int,
                                        resultData: Bundle?
                                    ) {
                                        onSwitch()
                                        Log.e("result receiver", "data")
                                    }
                                }
                            )
                        }

                        deviceMusicsListAdapter.onItemClick = {
                            musicId ->
                            if(playSoundFrom != Playlist.DEVICE_MUSICS.name) {
                                switchPlaylist(Playlist.DEVICE_MUSICS, musicId) {
                                    currentMusicId = musicId
                                    changeMusicOrPlayStop(musicId)
                                }
                            } else {
                                changeMusicOrPlayStop(musicId)
                            }
                        }
                        radioStationsListAdapter.onItemClick = {
                            musicId ->
                            if(playSoundFrom != Playlist.ONLINE_RADIO_STATIONS.name) {
                                Log.e("test", "this ((()))")
                                switchPlaylist(Playlist.ONLINE_RADIO_STATIONS, musicId) {
                                    currentMusicId = musicId
                                    changeMusicOrPlayStop(musicId)
                                }
                            } else {
                                changeMusicOrPlayStop(musicId)
                            }
                        }

                        switchToDeviceMusicsListBtn.setOnClickListener {
                            currentViewList = Playlist.DEVICE_MUSICS.name
                        }
                        switchToStationsListBtn.setOnClickListener {
                            currentViewList = Playlist.ONLINE_RADIO_STATIONS.name
                        }

                        var isSkipSeekBarUpdate = false

                        if(playSoundFrom == Playlist.DEVICE_MUSICS.name) {
                            progress.max = mediaController
                                .metadata
                                .getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
                                .toInt()
                        }

                        val handler = Handler()
                        handler.postDelayed(object : Runnable{
                            override fun run() {
                                handler.postDelayed(this, 1000)
                                if(
                                    isSkipSeekBarUpdate ||
                                    playSoundFrom == Playlist.ONLINE_RADIO_STATIONS.name
                                ) {
                                    return
                                }
                                progress.progress = mediaController.playbackState.position.toInt()
                            }
                        }, 0)
                        progress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onStartTrackingTouch(seekbar: SeekBar?) {
                                if(playSoundFrom != Playlist.DEVICE_MUSICS.name) {
                                    return
                                }

                                isSkipSeekBarUpdate = true
                            }
                            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                isSkipSeekBarUpdate = false

                                if(playSoundFrom != Playlist.DEVICE_MUSICS.name) {
                                    return
                                }

                                mediaController.transportControls.seekTo(
                                    progress.progress.toLong()
                                )
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
                            if(currentMusicIsPlay) {
                                mediaController.transportControls.pause()
                            } else {
                                if(playSoundFrom == Playlist.ONLINE_RADIO_STATIONS.name) {
                                    mediaController.transportControls.seekTo(Long.MAX_VALUE)
                                }
                                mediaController.transportControls.play()
                            }
                        }
                        nextBtn.setOnClickListener {
                            mediaController.transportControls.skipToNext()
                        }
                    }

                    mediaBrowser.subscribe(
                        Playlist.DEVICE_MUSICS.name,
                        object : MediaBrowserCompat.SubscriptionCallback() {
                            override fun onChildrenLoaded(
                                parentId: String,
                                children: MutableList<MediaBrowserCompat.MediaItem>
                            ) {
                                deviceMusicsListAdapterDataSet = children.mapIndexed { index, mi -> PlaylistItem(
                                    index,
                                    mi.description.title.toString(),
                                    mi.description.subtitle.toString(),
                                    false
                                )}.toTypedArray()
                                deviceMusicsListAdapter = PlaylistAdapter(deviceMusicsListAdapterDataSet);
                                deviceMusicsList.adapter = deviceMusicsListAdapter;
                                loadedList += 1
                            }
                        }
                    )
                    mediaBrowser.subscribe(
                        Playlist.ONLINE_RADIO_STATIONS.name,
                        object : MediaBrowserCompat.SubscriptionCallback() {
                            override fun onChildrenLoaded(
                                parentId: String,
                                children: MutableList<MediaBrowserCompat.MediaItem>
                            ) {
                                radioStationsListAdapterDataSet = children.mapIndexed { index, mi -> PlaylistItem(
                                    index,
                                    mi.description.title.toString(),
                                    mi.description.subtitle.toString(),
                                    false
                                )}.toTypedArray()
                                radioStationsListAdapter = PlaylistAdapter(radioStationsListAdapterDataSet);
                                radioStationsList.adapter = radioStationsListAdapter;
                                loadedList += 1
                            }
                        }
                    )
                }
            },
            null
        ).apply { connect() }
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaController.unregisterCallback(mediaControllerCallback)

        mediaBrowser.unsubscribe(Playlist.DEVICE_MUSICS.name)
        mediaBrowser.unsubscribe(Playlist.ONLINE_RADIO_STATIONS.name)
        mediaBrowser.disconnect()
    }
}