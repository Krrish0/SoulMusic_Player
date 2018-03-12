package dark.ash.com.soulmusicplayer.data;

import android.net.Uri;
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


    private List<MediaMetadataCompat> mSongList;


    public PlayerAdapter(ArrayList<MediaMetadataCompat> songList) {
        mSongList = songList;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String album = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
        String albumImage = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        String genre = mSongList.get(position).getString(MediaMetadataCompat.METADATA_KEY_GENRE);

        holder.songTitle.setText(title);
        holder.songAlbum.setText(album);
        holder.songGenre.setText(genre);
        if (albumImage != null) {
            Uri imageUri = Uri.parse(albumImage);
            holder.songAlbumImage.setImageURI(imageUri);
        }
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


        public ImageView songAlbumImage;
        public TextView songTitle;
        public TextView songAlbum;
        public TextView songGenre;

        public ViewHolder(View itemView) {
            super(itemView);

            songAlbumImage = itemView.findViewById(R.id.image_album_art_icon);
            songTitle = itemView.findViewById(R.id.media_title_name);
            songAlbum = itemView.findViewById(R.id.media_title_album);
            songGenre = itemView.findViewById(R.id.media_title_genre);
        }
    }
}
