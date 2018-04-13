package dark.ash.com.soulmusicplayer.ui;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.data.QueueAdapter;

public class MediaQueueFragment extends Fragment {

    private static final String TAG = MediaQueueFragment.class.getSimpleName();
    private RecyclerView mQueueList;
    private QueueAdapter mQueueAdapter;
    private MediaBrowserCompat mMediaBrowser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_queue, container, false);
        mQueueList = view.findViewById(R.id.list_queue);
        mQueueList.hasFixedSize();
        MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(getActivity());
        if (mediaControllerCompat != null) {
            mQueueAdapter = new QueueAdapter(mediaControllerCompat.getQueue());
        } else {
            mQueueAdapter = new QueueAdapter(new ArrayList<MediaSessionCompat.QueueItem>());
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mQueueList.setLayoutManager(layoutManager);
        mQueueList.setAdapter(mQueueAdapter);
        return view;
    }
}
