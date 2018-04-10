package dark.ash.com.soulmusicplayer.data;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.R;

public class CustomPagerAdapter extends PagerAdapter {

    private static float PAGE_WIDTH = 0.75f;
    private Context mContext;
    private List<MediaSessionCompat.QueueItem> mQueueList;
    private LayoutInflater inflater;

    public CustomPagerAdapter(Context context) {
        mQueueList = new ArrayList<>();
        this.mContext = context;
    }

    public CustomPagerAdapter(Context context, List<MediaSessionCompat.QueueItem> queueItems) {
        this(context);
        this.mQueueList = queueItems;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        return mQueueList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.fragment_media_pager, container, false);
        CircularImageView imageView = view.findViewById(R.id.album_image);
        bind(imageView, position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private void bind(View view, int position) {
        ImageView imageView = (ImageView) view;
        Uri albumUri = mQueueList.get(position).getDescription().getIconUri();
        if (albumUri != null) {
            Picasso.get().load("file://" + albumUri.toString()).fit().centerCrop().into(imageView);
        } else {
            Picasso.get().load(R.drawable.madlove).fit().centerCrop().into(imageView);
        }
    }


}
