package dark.ash.com.soulmusicplayer.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.FragmentListener;
import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.data.BrowserAdapter;

public class FragmentGenre extends FragmentBase {

    private static final String TAG = FragmentGenre.class.getSimpleName();

    private static final String ARG_MEDIA_ID = "media_id";
    private final MediaControllerCompat.Callback mMediaControllerCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            if (metadata == null) {
                return;
            }
            Log.e(TAG, "recieived metadata change to media");
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            Log.e(TAG, "Received state change: " + state);
        }
    };
    private String mMediaId;
    private BrowserAdapter mBrowserAdapter;
    private final MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback =
            new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    try {
                        //mAdapter.clear();
                        Log.e(TAG, "fragment onChildrenLoaded , parentId=" + parentId);
                        Log.e(TAG, children.toString());
                        //mAdapter.addAll(children);
                        mBrowserAdapter.updateData(children);
                        //mAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e(TAG, "Error on ChildrenLoaded" + e);
                    }
                }

                @Override
                public void onError(@NonNull String parentId) {
                    Log.e(TAG, "browse fragment subscription onError");
                    Toast.makeText(getActivity(), "Error Loading Media", Toast.LENGTH_LONG).show();
                }
            };
    private FragmentListener mMediaFragmentAlbumsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_genre_holder, container, false);
        RecyclerView listView = rootView.findViewById(R.id.list_genre);
        listView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);


        BrowserAdapter.RecyclerViewClickListener listener = new BrowserAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(MediaBrowserCompat.MediaItem mediaItem) {
                mMediaFragmentAlbumsListener.onMediaItemSelected(mediaItem);
            }
        };
        mBrowserAdapter = new BrowserAdapter(getContext(), listener, new ArrayList<MediaBrowserCompat.MediaItem>());

        listView.setAdapter(mBrowserAdapter);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMediaFragmentAlbumsListener = (FragmentListener) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        MediaBrowserCompat mediaBrowser = mMediaFragmentAlbumsListener.getMediaBrowser();
        Log.e(TAG, "fragment.onStart, mediaId = " + mMediaId);

        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mMediaFragmentAlbumsListener.getMediaBrowser();
        if (mediaBrowser != null && mediaBrowser.isConnected() && mMediaId != null) {
            mediaBrowser.unsubscribe(mMediaId);
        }
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

        if (controller != null) {
            controller.unregisterCallback(mMediaControllerCallback);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMediaFragmentAlbumsListener = null;
    }

    public String getMediaId() {

        Bundle args = getArguments();
        if (args != null) {
            return args.getString(ARG_MEDIA_ID);
        }
        return null;
    }

    public void setMediaId(String mediaId) {
        Log.e(TAG, "setMediaId is called mediaId=" + mediaId);
        Bundle args = new Bundle();
        args.putString(FragmentGenre.ARG_MEDIA_ID, mediaId);
        setArguments(args);
    }

    public void onConnected() {

        Log.e(TAG, "onConnected is Called");
        if (isDetached()) {
            return;
        }
        mMediaId = getMediaId();
        Log.e(TAG, "mMedia Id is mMediaId = " + mMediaId);
        if (mMediaId == null) {
            mMediaId = mMediaFragmentAlbumsListener.getMediaBrowser().getRoot();
        }

        mMediaFragmentAlbumsListener.getMediaBrowser().unsubscribe(mMediaId);

        mMediaFragmentAlbumsListener.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }
    }

}
