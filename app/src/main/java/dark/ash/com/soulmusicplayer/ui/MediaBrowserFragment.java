package dark.ash.com.soulmusicplayer.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.data.BrowserAdapter;

/**
 * Created by hp on 27-03-2018.
 */

public class MediaBrowserFragment extends android.support.v4.app.Fragment {

    private static final String TAG = MediaBrowserFragment.class.getSimpleName();

    private static final String ARG_MEDIA_ID = "media_id";

    private String mMediaId;
    private BrowserAdapter mBrowserAdapter;
    private MediaFragmentListener mMediaFragmentListener;
    private BrowseAdapter mAdapter;
    //Receives callbacks from the MediaController
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
            //TODO Check user for Visible Errors(false);
        }
    };

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMediaFragmentListener = (MediaFragmentListener) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "fragment.onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        //mAdapter = new BrowseAdapter(getActivity());


        RecyclerView listView = rootView.findViewById(R.id.fragment_list);
        listView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        listView.setLayoutManager(layoutManager);


        BrowserAdapter.RecyclerViewClickListener listener = new BrowserAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(MediaBrowserCompat.MediaItem mediaItem) {
                mMediaFragmentListener.onMediaItemSelected(mediaItem);
            }
        };
        mBrowserAdapter = new BrowserAdapter(getContext(), listener, new ArrayList<MediaBrowserCompat.MediaItem>());

        listView.setAdapter(mBrowserAdapter);

        //listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //    @Override
        //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //        MediaBrowserCompat.MediaItem item = mAdapter.getItem(position);
        //        mMediaFragmentListener.onMediaItemSelected(item);
        //    }
        //});
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
        Log.e(TAG, "fragment.onStart, mediaId = " + mMediaId);

        if (mediaBrowser.isConnected()) {
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MediaBrowserCompat mediaBrowser = mMediaFragmentListener.getMediaBrowser();
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
        mMediaFragmentListener = null;
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
        args.putString(MediaBrowserFragment.ARG_MEDIA_ID, mediaId);
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
            mMediaId = mMediaFragmentListener.getMediaBrowser().getRoot();
        }

        mMediaFragmentListener.getMediaBrowser().unsubscribe(mMediaId);

        mMediaFragmentListener.getMediaBrowser().subscribe(mMediaId, mSubscriptionCallback);

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.registerCallback(mMediaControllerCallback);
        }
    }

    public interface MediaFragmentListener extends MediaBrowserProvider {
        void onMediaItemSelected(MediaBrowserCompat.MediaItem item);

        void setToobarTitle(CharSequence title);
    }

    private static class BrowseAdapter extends ArrayAdapter<MediaBrowserCompat.MediaItem> {

        private TextView mArtistView;
        private TextView mSongsView;

        public BrowseAdapter(Activity context) {
            super(context, R.layout.media_browse_list, new ArrayList<MediaBrowserCompat.MediaItem>());
            Log.e(TAG, "BrowserAdapter Constructor is Called");
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            MediaBrowserCompat.MediaItem item = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.media_browse_list, parent, false);
                mSongsView = convertView.findViewById(R.id.text_view_songname);
                mArtistView = convertView.findViewById(R.id.text_view_artist);
            }

            MediaDescriptionCompat description = item.getDescription();
            mSongsView.setText(description.getTitle());
            mArtistView.setText(description.getSubtitle());
            return convertView;
        }
    }

}
