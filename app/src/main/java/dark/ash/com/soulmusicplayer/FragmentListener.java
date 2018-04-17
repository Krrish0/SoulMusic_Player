package dark.ash.com.soulmusicplayer;

import android.support.v4.media.MediaBrowserCompat;

import dark.ash.com.soulmusicplayer.ui.MediaBrowserProvider;

public interface FragmentListener extends MediaBrowserProvider {
    void onMediaItemSelected(MediaBrowserCompat.MediaItem item);

    void setToobarTitle(CharSequence title);
}
