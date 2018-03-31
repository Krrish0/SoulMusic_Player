package dark.ash.com.soulmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.model.MusicProvider;
import dark.ash.com.soulmusicplayer.playback.MediaPlayback;
import dark.ash.com.soulmusicplayer.playback.PlaybackManager;
import dark.ash.com.soulmusicplayer.playback.QueueManager;
import dark.ash.com.soulmusicplayer.utils.MediaIDHelper;

/**
 * Created by hp on 25-03-2018.
 */

public class SoulMusicService extends MediaBrowserServiceCompat implements PlaybackManager.PlaybackServiceCallback {

    public static final String ACTION_CMD = "dark.ash.com.soulmusicplayer.ACTION_CMD";
    public static final String CMD_NAME = "CMD_NAME";
    public static final String CMD_PAUSE = "CMD_PAUSE";
    public static final int STOP_DELAY = 30000;
    private static final String TAG = SoulMusicService.class.getSimpleName();
    private final DelayedStopHandler mDelayedStopHandler = new DelayedStopHandler(this);
    private MusicProvider mMusicProvider;
    private PlaybackManager mPlaybackManager;

    //TODO Create A Medianotification variable here
    private MediaSessionCompat mSession;
    private Bundle mSessionExtras;
    private MediaRouter mMediaRouter;

    //TODO Modify more if you want the cast and other devices support


    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        Context context = getApplicationContext();

        mMusicProvider = new MusicProvider(context);


        mMusicProvider.retrieveMediaAsync(null);

        QueueManager queueManager = new QueueManager(mMusicProvider, getResources(), new QueueManager.MetadataUpdateListener() {
            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                mSession.setMetadata(metadata);
            }

            @Override
            public void onMetadataRetrieveError() {
                mPlaybackManager.updatePlaybackState("No Metadata");
            }

            @Override
            public void onCurrentQueueIndexUpdated(int queueIndex) {
                mPlaybackManager.handlePlayRequest();
            }

            @Override
            public void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue) {
                mSession.setQueue(newQueue);
                mSession.setQueueTitle(title);
            }
        });

        MediaPlayback playback = new MediaPlayback(this, mMusicProvider);
        mPlaybackManager = new PlaybackManager(this, getResources(), mMusicProvider, queueManager, playback);

        mSession = new MediaSessionCompat(this, "SoulMusicService");
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(mPlaybackManager.getMediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Intent Class to Play the FullPlayer Activity
        mSessionExtras = new Bundle();
        mSession.setExtras(mSessionExtras);

        mPlaybackManager.updatePlaybackState(null);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String command = intent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
                if (CMD_PAUSE.equals(command)) {
                    mPlaybackManager.handlePauseRequest();
                }
            } else {
                MediaButtonReceiver.handleIntent(mSession, intent);
            }
        }
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.e(TAG, "OnGetRoot: clientPackageName=" + clientPackageName + "; clientUid = " + clientUid + "; rootHints=" + rootHints);

        return new MediaBrowserServiceCompat.BrowserRoot(MediaIDHelper.MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.e(TAG, "onLoadChildren: parentMediaId=" + parentId);
        if (MediaIDHelper.MEDIA_ID_EMPTY_ROOT.equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        } else if (mMusicProvider.isInitialized()) {
            result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
        } else {
            result.detach();
            mMusicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(mMusicProvider.getChildren(parentId, getResources()));
                }
            });
        }
    }

    @Override
    public void onPlaybackStart() {
        mSession.setActive(true);

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        //The service needs to running even after the bound client (usually a MediaController)
        //disconnects,otherwise the playback will stop
        startService(new Intent(getApplicationContext(), SoulMusicService.class));

    }

    @Override
    public void onNotificationRequired() {

        //Not yet Implemented
    }

    @Override
    public void onPlaybackStop() {
        mSession.setActive(false);
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mDelayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        //stopForeground(true);
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mSession.setPlaybackState(newState);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        mPlaybackManager.handleStopRequest(null);
        //TODO MediaNotifications is not implemented yet
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mSession.release();
    }

    private static class DelayedStopHandler extends Handler {
        private final WeakReference<SoulMusicService> mWeakReference;

        private DelayedStopHandler(SoulMusicService service) {
            mWeakReference = new WeakReference<SoulMusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            SoulMusicService service = mWeakReference.get();
            if (service != null && service.mPlaybackManager.getPlayback() != null) {
                if (service.mPlaybackManager.getPlayback().isPlaying()) {
                    Log.e(TAG, "Ignorind delayed stop since the media player in use.");
                    return;
                }
                service.stopSelf();
            }
        }
    }
}
