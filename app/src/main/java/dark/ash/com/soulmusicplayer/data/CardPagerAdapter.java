package dark.ash.com.soulmusicplayer.data;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.CardAdapter;
import dark.ash.com.soulmusicplayer.R;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {
    private static final String TAG = CardPagerAdapter.class.getSimpleName();

    private List<CardView> mViews;

    private List<MediaSessionCompat.QueueItem> mData;
    private float mBaseElevation;

    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public CardPagerAdapter(List<MediaSessionCompat.QueueItem> queue) {
        this();
        mData = queue;
        Log.e(TAG, "" + queue.size());
        for (int i = 0; i < queue.size(); i++) {
            mViews.add(null);
        }
    }

    @Override
    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.media_album_list, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = view.findViewById(R.id.card_view);
        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }
        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(MediaSessionCompat.QueueItem queueItem, View view) {

        CircularImageView imageView = view.findViewById(R.id.circular_albumView);
        Uri albumUri = queueItem.getDescription().getIconUri();
        Log.e(TAG, "" + albumUri);

        if (albumUri != null) {
            Picasso.get().load("file://" + albumUri.toString()).fit().centerCrop().into(imageView);
        } else {
            Picasso.get().load(R.drawable.madlove).fit().centerCrop().into(imageView);
        }
    }
}
