package dark.ash.com.soulmusicplayer.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.utils.TimeUtils;

/**
 * Created by hp on 24-03-2018.
 */

public class MediaControlsFragment extends Fragment {

    private static final String TAG = MediaControlsFragment.class.getSimpleName();

    private TextView mSongTitle;
    private TextView mSongArtist;
    private ImageView mSongAlbumArt;
    private TextView mDuration;

    private MediaControllerCompat.Callback mCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.e(TAG, "Received playback state change to state " + state.getState());
            MediaControlsFragment.this.onPlaybackStateChanged(state);
        }


        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if (metadata == null) {
                return;
            }
            Log.e(TAG, "Receive the metadata state change to mediaId = " + metadata.getDescription().getMediaId());
            MediaControlsFragment.this.onMetadataChanged(metadata);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(TAG, "OnCreateView is Called");
        View rootView = inflater.inflate(R.layout.media_controls_fragment, container, false);
        mSongAlbumArt = rootView.findViewById(R.id.album_art_song_icon);
        mSongTitle = rootView.findViewById(R.id.song_name);
        mSongArtist = rootView.findViewById(R.id.song_genre);
        mDuration = rootView.findViewById(R.id.song_length);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), FullPlayer.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "fragment.onStart");
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            Log.e(TAG, "onConnected is called where controller = " + controller);
            onConnected();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "fragment.onStop");
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.unregisterCallback(mCallback);
        }
    }

    public void onConnected() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        Log.e(TAG, "onConnected, mediaController== null?" + (controller == null));
        if (controller != null) {
            onMetadataChanged(controller.getMetadata());
            onPlaybackStateChanged(controller.getPlaybackState());
            controller.registerCallback(mCallback);
        }
    }

    private void onMetadataChanged(MediaMetadataCompat metadata) {
        Log.e(TAG, "onMetadataChanged =" + metadata);
        if (getActivity() == null) {
            Log.e(TAG, "Callback was not properly unregistered");
            return;
        }
        if (metadata == null) {
            return;
        }
        mSongTitle.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        mSongArtist.setText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
        String time = TimeUtils.longToTime(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        mDuration.setText(time);
        String albumPath = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        Bitmap albumBitmap = BitmapFactory.decodeFile(albumPath);
        mSongAlbumArt.setImageBitmap(albumBitmap);
    }

    private void onPlaybackStateChanged(PlaybackStateCompat state) {
        Log.e(TAG, "onPlaybackStateChanged " + state.getState());
        if (getActivity() == null) {
            Log.e(TAG, "callback was not properly unregistered");
            return;
        }
        if (state == null) {
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()) {
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay = true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                Log.e(TAG, "error Playback" + state.getErrorMessage());
                break;
        }
        //TODO Play Button Change After Click to Change Image

        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());

    }

    //TODO Set a onClick Listener for Button Play

    private void playMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().play();
        }
    }

    private void pauseMedia() {
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().pause();
        }
    }
}
