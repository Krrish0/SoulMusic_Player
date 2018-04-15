package dark.ash.com.soulmusicplayer.ui;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.SoulMusicService;

public class FullPlayer extends ActionBarCastActivity {

    private static final String TAG = FullPlayer.class.getSimpleName();
    private static final String QUEUE_FLAG = "dark.ash.soulmusicplayer.queue";
    private static final String QUEUE_FRAGMENT_TAG = "dark.ash.com.soulmusicplayer.ui.MediaQueueFragment";
    private final MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.e(TAG, "onPlaybackState changed" + state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata != null) {
                Log.e(TAG, "onMetadataChanged");
                FullScreenPlayerActivity fragment = (FullScreenPlayerActivity) getSupportFragmentManager().findFragmentByTag(FullScreenPlayerActivity.QUEUE_FLAG);
                fragment.onMetadataChanged(metadata);
            }
        }
    };
    private MediaBrowserCompat mMediaBrowser;
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
    private FragmentManager mFragmentManager = getSupportFragmentManager();
    private Toolbar mToolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_player);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("NowPlaying");
        }
        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, SoulMusicService.class), mConnectionCallback, null);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, new FullScreenPlayerActivity(), FullScreenPlayerActivity.QUEUE_FLAG);
        transaction.addToBackStack("FullScreenPlayer");
        transaction.commit();

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
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(FullPlayer.this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(FullPlayer.this, token);
        if (mediaController.getMetadata() == null) {
            finish();
            return;
        }
        MediaControllerCompat.setMediaController(FullPlayer.this, mediaController);
        mediaController.registerCallback(mCallback);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.queue:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(QUEUE_FRAGMENT_TAG);
                if (fragment == null) {
                    transaction.replace(R.id.fragment_container, new MediaQueueFragment(), QUEUE_FRAGMENT_TAG);
                    transaction.addToBackStack(null);
                }
                transaction.commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
