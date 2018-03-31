package dark.ash.com.soulmusicplayer.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import dark.ash.com.soulmusicplayer.SoulMusicService;
import dark.ash.com.soulmusicplayer.model.MusicProvider;
import dark.ash.com.soulmusicplayer.model.MusicProviderSource;
import dark.ash.com.soulmusicplayer.utils.MediaIDHelper;

import static com.google.android.exoplayer2.C.CONTENT_TYPE_MUSIC;
import static com.google.android.exoplayer2.C.USAGE_MEDIA;

/**
 * Created by hp on 13-03-2018.
 */

public class MediaPlayback implements Playback {


    //The volume we set the mediaPlayer to when we lose Audio Focus
    public static final float VOLUME_DUCK = 0.2f;
    //the Volume we set the mediaPlayer when we have audio Focus
    public static final float VOLUME_NORMAL = 1.0f;
    //we don't have audio focus, and can't duck(play a low volume)
    public static final int AUDIO_NO_FOCUS_NO_DUCK = 0;
    //we don't have focus, but can duck(play at low volume)
    public static final int AUDIO_NO_FOCUS_CAN_DUCK = 1;
    // we have full audio focus
    public static final int AUDIO_FOCUSED = 2;
    private static final String TAG = MediaPlayback.class.getSimpleName();

    private final ExoPlayerEventListener mEventListener = new ExoPlayerEventListener();
    //context object from the calling class
    private Context mContext;
    //Flag to set when we gain focus
    private boolean mPlayOnFocusGain;
    //Callback class instance of PlayBack class
    private Callback mCallback;
    //instance of Music Provider to get Music lists
    private MusicProvider mMusicProvider;
    //Flag to set when the Audio become noisy
    private boolean mAudioNoisyReceivedRegistered;
    //MediaId of the Current playing Id
    private String mCurrentMediaId;
    //Variable that shows tha status of the current audio focus state
    private int mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
    //Audio Manager Instance to Gain Audio Focus
    private AudioManager mAudioManager;
    //Exoplayer Instance to Play Music
    private SimpleExoPlayer mExoPlayer;
    //Whether to return STATE_NONE or STATE_STOPPED when mExoPlayer is null;
    private boolean mExoPlayerNullIsStopped = false;

    private IntentFilter mAudioNoisyIntentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private BroadcastReceiver mAudioNoisyReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                        Log.e(TAG, "HeadPhones disconnected");
                    }
                    if (isPlaying()) {
                        Intent i = new Intent(context, SoulMusicService.class);
                        i.setAction(SoulMusicService.ACTION_CMD);
                        i.putExtra(SoulMusicService.CMD_NAME, SoulMusicService.CMD_PAUSE);
                        mContext.startService(i);
                    }
                }
            };

    //TODO Set a Intent Filter for AUDIO NOISE FILTER
    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.e(TAG, "Audio Focus Changed : " + focusChange);
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    mCurrentAudioFocusState = AUDIO_FOCUSED;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                    mPlayOnFocusGain = mExoPlayer != null && mExoPlayer.getPlayWhenReady();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                    break;
            }
            if (mExoPlayer != null) {
                configurePlayerState();
            }
        }
    };

    @Override
    public void start() {

    }

    public MediaPlayback(Context context, MusicProvider musicProvider) {
        Log.e(TAG, "MediaPlayback Constructor is Called");
        Context applicationContext = context.getApplicationContext();
        this.mContext = applicationContext;
        this.mMusicProvider = musicProvider;
        this.mAudioManager = (AudioManager) applicationContext.getSystemService(Context.AUDIO_SERVICE);

    }

    @Override
    public void updateLastKnownStreamPosition() {
        //Do Nothing
    }

    @Override
    public void stop(boolean notifyListeners) {
        Log.e(TAG, "On Stopped Method Called");
        giveUpAudioFocus();
        unregisterAudioNoisyReceiver();
        releaseResources(true);
    }

    @Override
    public int getState() {
        if (mExoPlayer == null) {
            return mExoPlayerNullIsStopped ? PlaybackStateCompat.STATE_STOPPED : PlaybackStateCompat.STATE_NONE;
        }
        switch (mExoPlayer.getPlaybackState()) {
            case Player.STATE_IDLE:
                return PlaybackStateCompat.STATE_PAUSED;
            case Player.STATE_READY:
                return mExoPlayer.getPlayWhenReady() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            case Player.STATE_ENDED:
                return PlaybackStateCompat.STATE_PAUSED;
            default:
                return PlaybackStateCompat.STATE_NONE;

        }
    }

    private void giveUpAudioFocus() {
        Log.e(TAG, "giveUpAudioFocus");
        if (mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
        }
    }

    private void releaseResources(boolean releasePlayer) {
        Log.e(TAG, "release Resources.releasePlayer = " + releasePlayer);

        //Stops and release Player
        if (releasePlayer && mExoPlayer != null) {
            mExoPlayer.release();
            mExoPlayer.removeListener(mEventListener);
            mExoPlayer = null;
            mExoPlayerNullIsStopped = true;
            mPlayOnFocusGain = false;
        }
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return mPlayOnFocusGain || (mExoPlayer != null && mExoPlayer.getPlayWhenReady());
    }

    @Override
    public void setState(int state) {
        //Nothing to do as ExoPlayer has their own State

    }

    @Override
    public void play(MediaSessionCompat.QueueItem queueItem) {
        mPlayOnFocusGain = true;
        tryToGetAudioFocus();
        registerAudioNoisyReceiver();
        String mediaId = queueItem.getDescription().getMediaId();
        boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
        if (mediaHasChanged) {
            mCurrentMediaId = mediaId;
        }

        if (mediaHasChanged || mExoPlayer == null && mediaId != null) {
            releaseResources(false); //release everything except the player
            MediaMetadataCompat track = mMusicProvider.getMusic(MediaIDHelper.extractMusicIDFromMediaID(mediaId));
            Uri songUri = null;
            String source = track.getString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE);
            if (source != null) {
                songUri = Uri.fromFile(new File(source));
            }

            if (mExoPlayer == null) {
                mExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, new DefaultTrackSelector());
                mExoPlayer.addListener(mEventListener);
            }

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(CONTENT_TYPE_MUSIC)
                    .setUsage(USAGE_MEDIA).build();
            mExoPlayer.setAudioAttributes(audioAttributes);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "SoulMusicPlayer"), null);

            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

            MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(songUri);
            mExoPlayer.prepare(mediaSource);
        }
        configurePlayerState();
    }

    @Override
    public void pause() {
        if (mExoPlayer != null) {
            mExoPlayer.setPlayWhenReady(false);
        }
        releaseResources(false);
        unregisterAudioNoisyReceiver();

    }

    @Override
    public String getCurrentMediaId() {
        return mCurrentMediaId;
    }

    @Override
    public void setCurrentMediaId(String mediaId) {

        this.mCurrentMediaId = mediaId;
    }

    @Override
    public void seekTo(long position) {
        Log.e(TAG, "seekTo called with " + position);
        if (mExoPlayer != null) {
            registerAudioNoisyReceiver();
            mExoPlayer.seekTo(position);
        }

    }

    @Override
    public void setCallback(Callback callback) {
        this.mCallback = callback;

    }

    private void tryToGetAudioFocus() {
        Log.e(TAG, "tryToGetAudioFocus");
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC
                , AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mCurrentAudioFocusState = AUDIO_FOCUSED;
            Log.e(TAG, "Audio Focus Successfully Gained");
        } else {
            mCurrentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
            Log.e(TAG, "Audio No Focus can Duck");
        }

    }

    private void configurePlayerState() {
        Log.e(TAG, "configurePlayerState.mCurrentAudioFocusState = " + mCurrentAudioFocusState);
        if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_NO_DUCK) {
            pause();
        } else {
            registerAudioNoisyReceiver();

            if (mCurrentAudioFocusState == AUDIO_NO_FOCUS_CAN_DUCK) {
                mExoPlayer.setVolume(VOLUME_DUCK);
            } else {
                mExoPlayer.setVolume(VOLUME_NORMAL);
            }
            //If we were playing when we lost focus, we need to resume playing
            if (mPlayOnFocusGain) {
                mExoPlayer.setPlayWhenReady(true);
                mPlayOnFocusGain = false;
            }
        }
    }

    @Override
    public long getCurrentStreamPosition() {
        return mExoPlayer != null ? mExoPlayer.getCurrentPosition() : 0;
    }

    private void registerAudioNoisyReceiver() {
        if (!mAudioNoisyReceivedRegistered) {
            mContext.registerReceiver(mAudioNoisyReceiver, mAudioNoisyIntentFilter);
            mAudioNoisyReceivedRegistered = true;
        }
    }

    private void unregisterAudioNoisyReceiver() {
        if (mAudioNoisyReceivedRegistered) {
            mContext.unregisterReceiver(mAudioNoisyReceiver);
            mAudioNoisyReceivedRegistered = false;
        }
    }

    public final class ExoPlayerEventListener implements Player.EventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                case Player.STATE_BUFFERING:
                case Player.STATE_READY:
                    if (mCallback != null) {
                        mCallback.onPlaybackStatusChanged(getState());
                    }
                    break;
                case Player.STATE_ENDED:
                    if (mCallback != null) {
                        mCallback.onCompletion();
                    }
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            final String what;
            switch (error.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    what = error.getSourceException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_RENDERER:
                    what = error.getRendererException().getMessage();
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    what = error.getUnexpectedException().getMessage();
                    break;
                default:
                    what = "Unknown: " + error;
            }

            Log.e(TAG, "ExoPlayer error: what=" + what);
            if (mCallback != null) {
                mCallback.onError("ExoPlayer error " + what);
            }

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }


    }
}
