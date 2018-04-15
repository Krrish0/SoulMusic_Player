package dark.ash.com.soulmusicplayer.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import dark.ash.com.soulmusicplayer.R;

public class BrowserAdapter extends RecyclerView.Adapter<BrowserAdapter.ViewHolder> {
    private static final String TAG = BrowserAdapter.class.getSimpleName();

    private List<MediaBrowserCompat.MediaItem> mMediaList;
    private Context mContext;
    private RecyclerViewClickListener mListener;

    public BrowserAdapter(Context context, RecyclerViewClickListener listener, List<MediaBrowserCompat.MediaItem> mediaItems) {
        this.mContext = context;
        this.mListener = listener;
        this.mMediaList = mediaItems;
    }

    public void updateData(List<MediaBrowserCompat.MediaItem> mediaItems) {
        Log.e(TAG, "updateList is Called");
        this.mMediaList.clear();
        this.mMediaList.addAll(mediaItems);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaDescriptionCompat description = mMediaList.get(position).getDescription();
        holder.mSongTitle.setText(description.getTitle());
        holder.mSongSubtitle.setText(description.getSubtitle());
        holder.bind(mMediaList.get(position), mListener);
    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.media_browse_list, parent, false);
        return new ViewHolder(view, mListener);
    }

    public interface RecyclerViewClickListener {
        void onClick(MediaBrowserCompat.MediaItem mediaItem);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mSongTitle;
        private TextView mSongSubtitle;
        private RecyclerViewClickListener mListener;

        public ViewHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            mListener = listener;
            mSongTitle = itemView.findViewById(R.id.text_view_songname);
            mSongSubtitle = itemView.findViewById(R.id.text_view_artist);
        }

        public void bind(final MediaBrowserCompat.MediaItem item, final RecyclerViewClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(item);
                }
            });
        }
    }
}
