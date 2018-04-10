package dark.ash.com.soulmusicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;
import android.util.Log;

import dark.ash.com.soulmusicplayer.ui.BaseActivity;
import dark.ash.com.soulmusicplayer.ui.MediaBrowserFragment;

public class SoulActivity extends BaseActivity implements MediaBrowserFragment.MediaFragmentListener {

    private static final String TAG = SoulActivity.class.getSimpleName();
    private static final String SAVED_MEDIA_ID = "dark.ash.com.soulmusicplayer.MEDIA_ID";
    private static final String FRAGMENT_TAG = "soul_list_container";

    private Bundle mVoiceSearchParamsn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Activity onCreate");
        setContentView(R.layout.activity_soul);
        initializeFromParams(savedInstanceState, getIntent());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        String mediaId = getMediaId();
        if (mediaId != null) {
            outState.putString(SAVED_MEDIA_ID, mediaId);
        }
        super.onSaveInstanceState(outState);
    }

    protected void initializeFromParams(Bundle savedInstantState, Intent intent) {
        Log.e(TAG, "initializeFromParams is called");
        String mediaId = null;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            mVoiceSearchParamsn = intent.getExtras();
            Log.e(TAG, "Starting from voice Search");
        } else {
            if (savedInstantState != null) {
                mediaId = savedInstantState.getString(SAVED_MEDIA_ID);
            }
        }
        navigateToBrowser(mediaId);
    }

    private void navigateToBrowser(String mediaId) {

        Log.e(TAG, "navigate to Browser, mediaId=" + mediaId);
        MediaBrowserFragment fragment = getBrowseFragment();

        if (fragment == null || !TextUtils.equals(fragment.getMediaId(), mediaId)) {
            Log.e(TAG, "fragment == null");
            fragment = new MediaBrowserFragment();
            fragment.setMediaId(mediaId);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.list_container, fragment, FRAGMENT_TAG);
            if (mediaId != null) {
                transaction.addToBackStack(null);
            }
            transaction.commit();
        }


    }


    public String getMediaId() {
        MediaBrowserFragment fragment = getBrowseFragment();
        if (fragment == null) {
            return null;
        }
        return fragment.getMediaId();
    }

    private MediaBrowserFragment getBrowseFragment() {
        return (MediaBrowserFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    @Override
    public void onMediaItemSelected(MediaBrowserCompat.MediaItem item) {

        if (item.isPlayable()) {
            MediaControllerCompat.getMediaController(SoulActivity.this).getTransportControls().playFromMediaId(item.getMediaId(), null);
        } else if (item.isBrowsable()) {
            navigateToBrowser(item.getMediaId());
        } else {
            Log.e(TAG, "Ignoring mediaItem is neither browsable nor playing");
        }
    }

    @Override
    public void setToobarTitle(CharSequence title) {
        Log.e(TAG, "setToolbarTitle");
        Log.e(TAG, "Yet to implemnted");
    }
    //


    @Override
    protected void onMediaControllerConnected() {
        //TODO Add functionality of SearchParameters Later
        getBrowseFragment().onConnected();
    }

}
