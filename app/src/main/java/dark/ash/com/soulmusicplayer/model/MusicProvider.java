package dark.ash.com.soulmusicplayer.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.utils.MediaIDHelper;

/**
 * Created by hp on 13-03-2018.
 */

public class MusicProvider {
    private static String TAG = MusicProvider.class.getSimpleName();
    private final ConcurrentMap<String, MutableMediaMetadata> mMusicListById;
    private MusicProviderSource mSource;

    private final Set<String> mFavoriteTracks;

    private Context mContext;
    //Categorized caches for music track data
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    //The Variable is declared volatile so that the changes made by any thread
    // in this variable can be reflected back on other threads immediately.
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        mFavoriteTracks = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    }

    public MusicProvider(Context context) {
        this(new LocalDataProvider(context));
    }

    public ConcurrentMap<String, MutableMediaMetadata> getMusicListById() {
        return mMusicListById;
    }

    public List<MediaMetadataCompat> getMusicByGenre(String genre) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByGenre.containsKey(genre)) {
            Log.e(TAG, "returning empty list");
            return Collections.emptyList();
        }
        return mMusicListByGenre.get(genre);
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

    public List<MediaMetadataCompat> searchMusicBySongTitle(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_TITLE, query);
    }

    public List<MediaMetadataCompat> searchMusicByAlbum(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ALBUM, query);
    }

    public List<MediaMetadataCompat> searchMusicByArtist(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_ARTIST, query);
    }

    public List<MediaMetadataCompat> searchMusicByGenre(String query) {
        return searchMusic(MediaMetadataCompat.METADATA_KEY_GENRE, query);
    }

    private List<MediaMetadataCompat> searchMusic(String metadataField, String query) {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        ArrayList<MediaMetadataCompat> result = new ArrayList<>();
        //TODO Try to Experiment more with Locale
        query = query.toLowerCase(Locale.ENGLISH);
        for (MutableMediaMetadata track : mMusicListById.values()) {
            if (track.metadata.getString(metadataField).toLowerCase(Locale.ENGLISH)
                    .contains(query)) {
                result.add(track.metadata);
            }
        }
        return result;
    }

    //Starting point of the class where the @parameter{mMusicListById,mMusicListByGenre} is
    //filled with data Asyncronously.
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
                Log.e(TAG, "onPostExecute is Called");
                if (callback != null) {
                    Log.e(TAG, "State: " + state.name());
                    callback.onMusicCatalogReady(state == State.INITIALIZED);
                }
                return;
            }
        }.execute();
    }

    public boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    private synchronized void buildListsByGenre() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
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
                buildListsByGenre();
                mCurrentState = State.INITIALIZED;
            }
        } finally {
            if (mCurrentState != State.INITIALIZED) {
                mCurrentState = State.NON_INITIALIZED;
            }
        }

    }

    public synchronized void updateMusicArt(String musicID, Bitmap albumArt, Bitmap icon) {
        MediaMetadataCompat metadata = getMusic(musicID);
        metadata = new MediaMetadataCompat.Builder(metadata)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, icon).build();

        MutableMediaMetadata mutableMediaMetadata = mMusicListById.get(musicID);
        if (mutableMediaMetadata == null) {
            throw new IllegalStateException("UnExpected error: Inconsistent data structure in" + musicID);
        }
        mutableMediaMetadata.metadata = metadata;
    }

    public MediaMetadataCompat getMusic(String musicId) {
        return mMusicListById.containsKey(musicId) ? mMusicListById.get(musicId).metadata : null;
    }

    public void setFavorite(String musicID, boolean favourite) {
        if (favourite) {
            mFavoriteTracks.add(musicID);
        } else {
            mFavoriteTracks.remove(musicID);
        }
    }

    public boolean isFavorite(String musicID) {
        return mFavoriteTracks.contains(musicID);
    }

    public List<MediaBrowserCompat.MediaItem> getChildren(String mediaId, Resources resource) {
        Log.e(TAG, "getChildren is Called");
        Log.e(TAG, mediaId);

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        if (!MediaIDHelper.isBrowseable(mediaId)) {
            Log.e(TAG, "return mediaItems is Called");
            return mediaItems;
        }

        if (MediaIDHelper.MEDIA_ID_ROOT.equals(mediaId)) {
            mediaItems.add(createBrowsableMediaItemForRoot(resource));
        } else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE.equals(mediaId)) {
            for (String genre : getGenres()) {
                mediaItems.add(createBrowsableMediaItemForGenre(genre, resource));
            }
        } else if (mediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE)) {
            String genre = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicByGenre(genre)) {
                mediaItems.add(createMediaItem(metadata));
            }
        } else {
            Log.e(TAG, "Skipping unmatched mediaID: " + mediaId);
        }

        return mediaItems;
    }

    public interface Callback {
        void onMusicCatalogReady(boolean success);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForGenre(String genre, Resources resources) {

        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, genre))
                .setTitle(genre)
                .setSubtitle(resources.getString(R.string.browse_musics_by_genre_subtitle, genre))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForRoot(Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE)
                .setTitle(resources.getString(R.string.browse_genres))
                .setSubtitle(resources.getString(R.string.browse_genre_subtitle))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createMediaItem(MediaMetadataCompat metadata) {
        Log.e(TAG, "createMediaItem is Called");
        String genre = metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
        String albumUri = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        Log.e(TAG, "" + metadata.getDescription());
        String hierarchyAwareMediaID = MediaIDHelper.createMediaID(metadata.getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE, genre);
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadata)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        albumUri = copy.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI);
        return new MediaBrowserCompat.MediaItem(copy.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

}

