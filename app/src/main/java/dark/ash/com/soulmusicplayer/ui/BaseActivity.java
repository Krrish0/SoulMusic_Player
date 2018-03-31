package dark.ash.com.soulmusicplayer.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.SoulMusicService;

/**
 * Created by hp on 26-03-2018.
 */

public abstract class BaseActivity extends AppCompatActivity implements MediaBrowserProvider {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private MediaBrowserCompat mMediaBrowser;
    private MediaControlsFragment mControlsFragment;
    //Callback for MediaController set in connectToSession method
    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (shouldShowControls()) {
                showPlaybackControls();
            } else {
                Log.e(TAG, "mediaControllerCallback.onPlaybackStateChanged:" + state.getState());
                hidePlaybackControls();
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (shouldShowControls()) {
                showPlaybackControls();
            } else {
                Log.e(TAG, "mediaControllerCallback.onMetadataChanged: " + "metadata is full");
                hidePlaybackControls();
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
                Log.e(TAG, "could not Connect to mediaController");
                hidePlaybackControls();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Activity onCreate");
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, SoulMusicService.class), mConnectionCallback, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "Activity onStart");

        mControlsFragment = (MediaControlsFragment) getSupportFragmentManager().findFragmentById(R.id.media_controls_fragment);
        if (mControlsFragment == null) {
            throw new IllegalStateException("Mising fragments with id");
        }
        hidePlaybackControls();

        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, "Activity onStop");
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mMediaControllerCallback);
        }
        mMediaBrowser.disconnect();
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    protected void onMediaControllerConnected() {
        //emptyImplementation
    }

    protected void showPlaybackControls() {
        Log.e(TAG, "showPlaybackControls");
        getSupportFragmentManager().beginTransaction().show(mControlsFragment).commit();
    }

    protected void hidePlaybackControls() {
        Log.e(TAG, "hidePlaybackControls");
        getSupportFragmentManager().beginTransaction().hide(mControlsFragment).commit();
    }

    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null || mediaController.getMetadata() == null || mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaControllerCompat = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaControllerCompat);
        mediaControllerCompat.registerCallback(mMediaControllerCallback);

        if (shouldShowControls()) {
            showPlaybackControls();
        } else {
            Log.e(TAG, "connectionCallback.onConnected: " + "hiding controls");
            hidePlaybackControls();
        }

        if (mControlsFragment != null) {
            mControlsFragment.onConnected();
        }
        onMediaControllerConnected();
    }
}
