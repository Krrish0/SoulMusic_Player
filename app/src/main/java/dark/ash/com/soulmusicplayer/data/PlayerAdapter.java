package dark.ash.com.soulmusicplayer.data;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


    private List<MediaBrowserCompat.MediaItem> mSongList;


    public PlayerAdapter(ArrayList<MediaBrowserCompat.MediaItem> songList) {
        this.mSongList = songList;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mSongList.get(position).getDescription().getTitle().toString();
        Bundle bundle = mSongList.get(position).getDescription().getExtras();
        String album = bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Unknown");
        Uri albumImage = mSongList.get(position).getDescription().getIconUri();
        String genre = bundle.getString(MediaMetadataCompat.METADATA_KEY_GENRE, "Unknown");
        holder.songTitle.setText(title);
        holder.songAlbum.setText(album);
        holder.songGenre.setText(genre);
        if (albumImage != null) {
            holder.songAlbumImage.setImageURI(albumImage);
        }

    }

    public void updateList(List<MediaBrowserCompat.MediaItem> items) {
        mSongList = items;
        notifyDataSetChanged();
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

    public interface AdapterClickListener {
        void onItemClicked();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private ImageView songAlbumImage;
        public TextView songTitle;
        public TextView songAlbum;
        public TextView songGenre;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            songAlbumImage = itemView.findViewById(R.id.image_album_art_icon);
            songTitle = itemView.findViewById(R.id.media_title_name);
            songAlbum = itemView.findViewById(R.id.media_title_album);
            songGenre = itemView.findViewById(R.id.media_title_genre);
        }

        @Override
        public void onClick(View v) {

            int clickedPosition = getAdapterPosition();
        }

    }

}
