package dark.ash.com.soulmusicplayer.playback;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.model.MusicProvider;
import dark.ash.com.soulmusicplayer.utils.MediaIDHelper;
import dark.ash.com.soulmusicplayer.utils.QueueHelper;

/**
 * Created by hp on 19-03-2018.
 */

public class QueueManager {

    private static final String TAG = QueueManager.class.getSimpleName();

    private MusicProvider mMusicProvider;

    private MetadataUpdateListener mListener;

    private Resources mResources;

    private List<MediaSessionCompat.QueueItem> mPlayingQueue;

    private int mCurrentIndex;

    public QueueManager(@NonNull MusicProvider musicProvider,
                        @NonNull Resources resources,
                        @NonNull MetadataUpdateListener listener) {
        Log.e(TAG, "QueueManager Constructor Called");
        this.mMusicProvider = musicProvider;
        this.mListener = listener;
        this.mResources = resources;
        mPlayingQueue = Collections.synchronizedList(new ArrayList<MediaSessionCompat.QueueItem>());
        mCurrentIndex = 0;
    }

    public boolean setQueueFromSearch(String query, Bundle extras) {
        List<MediaSessionCompat.QueueItem> queue = QueueHelper.getPlayingQueueFromSearch(query, extras, mMusicProvider);
        setCurrentQueue(mResources.getString(R.string.search_queue_title), queue);
        updateMetadata();
        return queue != null && !queue.isEmpty();
    }

    public boolean isSameBrowsingCategory(@NonNull String mediaID) {
        String[] newBrowseHierarchy = MediaIDHelper.getHierarchy(mediaID);
        MediaSessionCompat.QueueItem current = getCurrentMusic();
        if (current == null) {
            return false;
        }
        String[] currentBrowseHierarchy = MediaIDHelper.getHierarchy(current.getDescription().getMediaId());
        return Arrays.deepEquals(newBrowseHierarchy, currentBrowseHierarchy);
    }

    private void setCurrentQueueIndex(int index) {
        if (index >= 0 && index < mPlayingQueue.size()) {
            mCurrentIndex = index;
            mListener.onCurrentQueueIndexUpdated(mCurrentIndex);
        }
    }

    public boolean setCurrentQueueItem(long queueId) {
        int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, queueId);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    private boolean setCurrentQueueItem(String mediaID) {
        int index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, mediaID);
        setCurrentQueueIndex(index);
        return index >= 0;
    }

    public boolean skipQueuePosition(int amount) {
        int index = mCurrentIndex + amount;
        if (index < 0) {
            // skip backwards before the first song will keep you on the first song
            index = 0;
        } else {
            // skip forwards when in last song will cycle back to start of the queue
            index %= mPlayingQueue.size();
        }
        if (!QueueHelper.isIndexPlayable(index, mPlayingQueue)) {
            Log.e(TAG, "Cannot increment queue index by " + amount +
                    ". Current=" + mCurrentIndex + " queue length=" + mPlayingQueue.size());
            return false;
        }
        mCurrentIndex = index;
        return true;
    }

    public void setRandomQueue() {
        setCurrentQueue(mResources.getString(R.string.random_queue_title),
                QueueHelper.getRandomQueue(mMusicProvider));
        updateMetadata();
    }

    public void setQueueFromMusic(String mediaID) {
        Log.e(TAG, "setQueueFromMusic" + mediaID);

        boolean canResueQueue = false;
        if (isSameBrowsingCategory(mediaID)) {
            canResueQueue = setCurrentQueueItem(mediaID);
        }

        if (!canResueQueue) {
            String queueTitle = mResources.getString(R.string.browse_musics_by_genre_subtitle,
                    MediaIDHelper.extractBrowseCategoryValueFromMediaID(mediaID));
            setCurrentQueue(queueTitle, QueueHelper.getPlayingQueue(mediaID, mMusicProvider), mediaID);
        }
        updateMetadata();
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue) {
        setCurrentQueue(title, newQueue, null);
    }

    protected void setCurrentQueue(String title, List<MediaSessionCompat.QueueItem> newQueue, String initialMediaId) {

        mPlayingQueue = newQueue;
        int index = 0;
        if (initialMediaId != null) {
            index = QueueHelper.getMusicIndexOnQueue(mPlayingQueue, initialMediaId);
        }
        mCurrentIndex = Math.max(index, 0);
        mListener.onQueueUpdated(title, newQueue);
    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        Log.e(TAG, "getCurrentMusic is Called");
        if (!QueueHelper.isIndexPlayable(mCurrentIndex, mPlayingQueue)) {
            return null;
        }
        return mPlayingQueue.get(mCurrentIndex);
    }

    public int getCurrentQueueSize() {
        if (mPlayingQueue == null) {
            return 0;
        }
        return mPlayingQueue.size();
    }

    public void updateMetadata() {

        MediaSessionCompat.QueueItem currentMusic = getCurrentMusic();
        if (currentMusic == null) {
            mListener.onMetadataRetrieveError();
            return;
        }
        final String musicId = MediaIDHelper.extractMusicIDFromMediaID(currentMusic.getDescription().getMediaId());
        MediaMetadataCompat metadata = mMusicProvider.getMusic(musicId);
        if (metadata == null) {
            throw new IllegalArgumentException("invalid Music " + musicId);
        }
        mListener.onMetadataChanged(metadata);

        //TODO Set album art Here

    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }


}
