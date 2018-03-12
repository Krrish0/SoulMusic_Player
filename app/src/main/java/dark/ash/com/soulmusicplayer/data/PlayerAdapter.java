package dark.ash.com.soulmusicplayer.data;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.R;

/**
 * Created by hp on 09-03-2018.
 */

/*
 *A class that is a Adapter class that is used to show List of Tracks
 * on the Music List.
 */
public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.ViewHolder> {

    private List<MediaMetadataCompat> mSongList;


    public PlayerAdapter(ArrayList<MediaMetadataCompat> songList) {
        mSongList = songList;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String album = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        String artist = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
        holder.songText.append(" " + album);
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.media_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView songText;

        public ViewHolder(View itemView) {
            super(itemView);
            songText = itemView.findViewById(R.id.media_item_name);
        }
    }
}
