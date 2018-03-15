package dark.ash.com.soulmusicplayer.model;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by hp on 13-03-2018.
 */

public class MusicProvider {
    private static String TAG = MusicProvider.class.getSimpleName();
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;
    private MusicProviderSource mSource;
    private Context mContext;
    //Categorized caches for music track data
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    //The Variable is declared volatile so that the changes made by any thread
    // in this variable can be reflected back on other threads immediately.
    private volatile State mCurrentState = State.NON_INITIALIZED;

    //private final Set<String> mFavoriteTracks;

    public MusicProvider(Context context) {
        this(new LocalDataProvider(context));
    }

    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        //mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    }

    public ConcurrentMap<String, MutableMediaMetadata> getmMusicListById() {
        return mMusicListById;
    }

    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    public Iterable<MediaMetadataCompat> getShuffleMusic() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        List<MediaMetadataCompat> shuffled = new ArrayList<>(mMusicListById.size());
        for (MutableMediaMetadata mutableMediaMetadata : mMusicListById.values()) {
            shuffled.add(mutableMediaMetadata.metadata);
        }
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public List<MediaMetadataCompat> getMusicByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListById.containsKey(genre)) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public void retrieveMediaAsync(final Callback callback) {
        Log.e(TAG, "retreiveMediaAsync Called");
        if (mCurrentState == State.INITIALIZED) {
            if (callback != null) {
                callback.onMusicCatalogReady(true);
            }
            return;
        }

        //Asychronously load the music catalog in a seperate thread
        new AsyncTask<Void, Void, State>() {
            @Override
            protected State doInBackground(Void... voids) {
                retrieveMedia();
                Log.e(TAG, "doInBackGround Started");
                return mCurrentState;
            }

            @Override
            protected void onPostExecute(State state) {
                if (callback != null) {
                    Log.e(TAG, "State: " + state.name());
                    callback.onMusicCatalogReady(state == State.INITIALIZED);
                }
            }
        }.execute();
    }

    private synchronized void retrieveMedia() {

        try {
            if (mCurrentState == State.NON_INITIALIZED) {
                mCurrentState = State.INITIALIZED;

                Iterator<MediaMetadataCompat> tracks = mSource.iterator();
                while (tracks.hasNext()) {
                    MediaMetadataCompat item = tracks.next();
                    String musicId = item.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                    mMusicListById.put(musicId, new MutableMediaMetadata(musicId, item));
                }
                //TODO Create a Build Function for genreating list of music by Genre and
                //call it here
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                mCurrentState = State.NON_INITIALIZED;
            }
        }

    }

    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

}
