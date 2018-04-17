package dark.ash.com.soulmusicplayer.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dark.ash.com.soulmusicplayer.R;

public class FragmentPlaylist extends FragmentBase {

    private static final String TAG = FragmentPlaylist.class.getSimpleName();
    private static final String ARG_MEDIA_ID = "media_id";

    @Override
    public String getMediaId() {
        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    @Override
    public void setMediaId(String mediaId) {
        Log.e(TAG, "setMediaId is called mediaId=" + mediaId);
        Bundle args = new Bundle();
        args.putString(FragmentPlaylist.ARG_MEDIA_ID, mediaId);
        setArguments(args);

    }

    @Override
    public void onConnected() {
        Log.e(TAG, "onConnect is Called ");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playlist_holder, container, false);
        return rootView;
    }
}
