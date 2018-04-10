package dark.ash.com.soulmusicplayer.ui;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.SoulMusicService;
import dark.ash.com.soulmusicplayer.data.CardPagerAdapter;

public class FullScreenPlayerActivity extends ActionBarCastActivity {
    private static final String TAG = FullScreenPlayerActivity.class.getSimpleName();
    private static final long PROGRESS_UPDATE_INTERNAL = 1000;
    private static final long PROGRESS_UPDATE_INITIAL_INTERVAL = 100;
    private final Handler mHandler = new Handler();
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ImageView mPlayPause;
    private ImageView mSkipNext;
    private ImageView mSkipPrev;
    private TextView mStartTime;
    private TextView mEndTime;
    private SeekBar mSeekBar;
    private TextView mSongTitle;
    private CardPagerAdapter mCardPagerAdapter;
    private TextView mSongArtist;
    private ImageView mBackgroundImage;
    private ViewPager mMediaPager;
    private String mCurrentArtUri;
    private MediaBrowserCompat mMediaBrowser;
    private ScheduledFuture<?> mScheduleFuture;
    private PlaybackStateCompat mLastPlaybackState;
    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.e(TAG, "onPlaybackState changed" + state);
            updatePlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                updateMediaDescription(metadata.getDescription());
                updateAlbumArtist(metadata);
                updateDuration(metadata);
            }
        }
    };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {
        @Override
        public void onConnected() {
            Log.e(TAG, "onConnected");
            try {
                connectToSession(mMediaBrowser.getSessionToken());
            } catch (RemoteException e) {
                Log.e(TAG, "could not connect to Media Controller");
            }
        }
    };

    private static float dpToPixels(int dp, Context context) {
        return dp * (context.getResources().getDisplayMetrics().density);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_player_activity);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        mPlayPause = findViewById(R.id.playButtion);
        mMediaPager = findViewById(R.id.viewPager);
        mSkipNext = findViewById(R.id.playForward);
        mSkipPrev = findViewById(R.id.playBackward);
        mStartTime = findViewById(R.id.startTime);
        mEndTime = findViewById(R.id.endTime);
        mSeekBar = findViewById(R.id.seekBar);
        mSongTitle = findViewById(R.id.fullscreen_titleTextView);
        mSongArtist = findViewById(R.id.fullscreen_artistTextView);
        mCardPagerAdapter = new CardPagerAdapter();
        mMediaPager.setAdapter(mCardPagerAdapter);
        mMediaPager.setPageMargin(-32);
        mMediaPager.setOffscreenPageLimit(3);
        mSkipNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this).getTransportControls();
                controls.skipToNext();
            }
        });
        mSkipPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this).getTransportControls();
                controls.skipToPrevious();
            }
        });
        mPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaybackStateCompat state = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this).getPlaybackState();
                if (state != null) {
                    MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this).getTransportControls();
                    switch (state.getState()) {
                        case PlaybackStateCompat.STATE_PLAYING:
                        case PlaybackStateCompat.STATE_BUFFERING:
                            controls.pause();
                            stopSeekbarUpdate();
                            break;
                        case PlaybackStateCompat.STATE_PAUSED:
                        case PlaybackStateCompat.STATE_STOPPED:
                            controls.play();
                            scheduleSeekbarUpdate();
                            break;
                        default:
                            Log.e(TAG, "onClick with state " + state.getState());
                    }
                }
            }
        });
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mStartTime.setText(DateUtils.formatElapsedTime(progress / 1000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopSeekbarUpdate();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this).getTransportControls().seekTo(seekBar.getProgress());
                scheduleSeekbarUpdate();
            }
        });

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, SoulMusicService.class), mConnectionCallback, null);

    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    mHandler.post(mUpdateProgressTask);
                }
            }, PROGRESS_UPDATE_INITIAL_INTERVAL, PROGRESS_UPDATE_INTERNAL, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMediaBrowser != null) {
            mMediaBrowser.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaBrowser != null) {
            mMediaBrowser.disconnect();
        }
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSeekbarUpdate();
        mExecutorService.shutdown();
    }

    private void updateMediaDescription(MediaDescriptionCompat description) {
        if (description == null) {
            return;
        }
        Log.e(TAG, "updateMediaDescription called");
        mSongTitle.setText(description.getTitle());
    }

    //method to update Album Artist
    private void updateAlbumArtist(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        Log.e(TAG, "upate AlbumArtist");
        String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        mSongArtist.setText(artist);
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata == null) {
            return;
        }
        Log.e(TAG, "updateDuration Called");
        int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        mSeekBar.setMax(duration);
        mEndTime.setText(DateUtils.formatElapsedTime(duration / 1000));
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        if (state == null) {
            return;
        }
        mLastPlaybackState = state;
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(FullScreenPlayerActivity.this);
        //TODO No Cast Support Yet

        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PLAYING:
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.ic_pause_black_24dp);
                scheduleSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                stopSeekbarUpdate();
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                mPlayPause.setVisibility(View.VISIBLE);
                mPlayPause.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                stopSeekbarUpdate();
                break;
            default:
                Log.e(TAG, "Unhandled state " + state.getState());
        }
        mSkipNext.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_NEXT) == 0
                ? View.INVISIBLE : View.VISIBLE);
        mSkipPrev.setVisibility((state.getActions() & PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) == 0
                ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateProgress() {
        if (mLastPlaybackState == null) {
            return;
        }
        long currentPosition = mLastPlaybackState.getPosition();
        if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
            long timeDelta = SystemClock.elapsedRealtime() - mLastPlaybackState.getLastPositionUpdateTime();
            currentPosition += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
        }
        mSeekBar.setProgress((int) currentPosition);
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(FullScreenPlayerActivity.this, token);
        if (mediaController.getMetadata() == null) {
            finish();
            return;
        }
        MediaControllerCompat.setMediaController(FullScreenPlayerActivity.this, mediaController);
        mediaController.registerCallback(mCallback);
        List<MediaSessionCompat.QueueItem> items = mediaController.getQueue();
        PlaybackStateCompat state = mediaController.getPlaybackState();
        updatePlaybackState(state);
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            updateMediaDescription(metadata.getDescription());
            updateAlbumArtist(metadata);
            updateDuration(metadata);
            mCardPagerAdapter = new CardPagerAdapter(mediaController.getQueue());
            mMediaPager.setAdapter(mCardPagerAdapter);
            mMediaPager.getAdapter().notifyDataSetChanged();
        }
        updateProgress();
        if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
            scheduleSeekbarUpdate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_player_menu, menu);
        return true;
    }


}
