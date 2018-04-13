package dark.ash.com.soulmusicplayer.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

import dark.ash.com.soulmusicplayer.R;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.QueueViewHolder> {

    private static final String TAG = QueueAdapter.class.getSimpleName();

    private List<MediaSessionCompat.QueueItem> mQueueItem;

    public QueueAdapter(List<MediaSessionCompat.QueueItem> queueItems) {
        this.mQueueItem = queueItems;
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.queue_list_item, parent, false);
        return new QueueViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        MediaSessionCompat.QueueItem item = mQueueItem.get(position);
        String songTitle = item.getDescription().getTitle().toString();
        String songArtist = item.getDescription().getSubtitle().toString();
        Uri albumUri = item.getDescription().getIconUri();
        holder.songTextView.setText(songTitle);
        holder.artistTextView.setText(songArtist);
        if (albumUri != null) {
            Picasso.get().load("file://" + albumUri.toString()).fit().centerCrop().into(holder.albumImageView);
        } else {
            Picasso.get().load(R.drawable.madlove).fit().centerCrop().into(holder.albumImageView);
        }

    }

    @Override
    public int getItemCount() {
        return mQueueItem.size();
    }

    public class QueueViewHolder extends RecyclerView.ViewHolder {

        public CircularImageView albumImageView;
        public TextView songTextView;
        public TextView artistTextView;
        public TextView durationTextView;

        public QueueViewHolder(View itemView) {
            super(itemView);
            albumImageView = itemView.findViewById(R.id.queue_list_albumArt);
            songTextView = itemView.findViewById(R.id.queue_list_mediaTitle);
            artistTextView = itemView.findViewById(R.id.queue_list_artist);
            durationTextView = itemView.findViewById(R.id.queue_list_duration);
        }
    }

}
