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

    private String[] listValues = {MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLISTS
            , MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUMS
            , MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL
            , MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTISTS
            , MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE};

    private Integer[] mResources = {R.string.browse_playlists
            , R.string.browse_albums
            , R.string.browse_all_songs
            , R.string.browse_artists
            , R.string.browse_genres
    };

    private Context mContext;
    //Categorized caches for music track data
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByGenre;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByAlbums;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mMusicListByArtists;
    private ConcurrentMap<String, MediaMetadataCompat> mMusicListByTitle;
    //The Variable is declared volatile so that the changes made by any thread
    // in this variable can be reflected back on other threads immediately.
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public MusicProvider(MusicProviderSource source) {
        mSource = source;
        mMusicListByGenre = new ConcurrentHashMap<>();
        mMusicListByAlbums = new ConcurrentHashMap<>();
        mMusicListByArtists = new ConcurrentHashMap<>();
        mMusicListByTitle = new ConcurrentHashMap<>();
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

    public MediaMetadataCompat getMusicByTitle(String title) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByTitle.containsKey(title)) {
            Log.e(TAG, "returning empty list");
            return null;
        }
        return mMusicListByTitle.get(title);
    }

    public List<MediaMetadataCompat> getMusicByAlbums(String albums) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByAlbums.containsKey(albums)) {
            Log.e(TAG, "returning empty list");
            return Collections.emptyList();
        }
        return mMusicListByAlbums.get(albums);
    }

    public List<MediaMetadataCompat> getMusicByArtists(String artists) {
        if (mCurrentState != State.INITIALIZED || !mMusicListByArtists.containsKey(artists)) {
            Log.e(TAG, "returning empty list");
            return Collections.emptyList();
        }
        return mMusicListByArtists.get(artists);
    }

    public Iterable<String> getGenres() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByGenre.keySet();
    }

    public Iterable<String> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByAlbums.keySet();
    }

    public Iterable<String> getArtists() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByArtists.keySet();
    }

    public Iterable<String> getTitle() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mMusicListByTitle.keySet();
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

    private synchronized void buildListsByVariations() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByAlbums = new ConcurrentHashMap<>();
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicLIstByArtists = new ConcurrentHashMap<>();
        ConcurrentMap<String, MediaMetadataCompat> newMusicListByTitle = new ConcurrentHashMap<>();
        for (MutableMediaMetadata m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            String albums = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
            String artists = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            String title = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            List<MediaMetadataCompat> list1 = newMusicListByAlbums.get(albums);
            List<MediaMetadataCompat> list2 = newMusicLIstByArtists.get(artists);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            if (list1 == null) {
                list1 = new ArrayList<>();
                newMusicListByAlbums.put(albums, list1);
            }
            if (list2 == null) {
                list2 = new ArrayList<>();
                newMusicLIstByArtists.put(artists, list);
            }
            list.add(m.metadata);
            list1.add(m.metadata);
            list2.add(m.metadata);
            newMusicListByTitle.put(title, m.metadata);
        }
        mMusicListByGenre = newMusicListByGenre;
        mMusicListByArtists = newMusicLIstByArtists;
        mMusicListByAlbums = newMusicListByAlbums;
        mMusicListByTitle = newMusicListByTitle;
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
                buildListsByVariations();
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
            mediaItems.addAll(createBrowsableMediaItemForRoot(resource));
        } else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE.equals(mediaId)) {
            for (String genre : getGenres()) {
                mediaItems.add(createBrowsableMediaItemForGenre(genre, resource));
            }
        } else if (mediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE)) {
            String genre = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicByGenre(genre)) {
                mediaItems.add(createMediaItem(metadata));
            }
        } else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUMS.equals(mediaId)) {
            for (String albums : getAlbums()) {
                mediaItems.add(createBrowsableMediaItemForAlbums(albums, resource));
            }
        } else if (mediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUMS)) {
            String albums = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicByAlbums(albums)) {
                mediaItems.add(createMediaItem(metadata));
            }
        } else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTISTS.equals(mediaId)) {
            for (String artists : getArtists()) {
                mediaItems.add(createBrowsableMediaItemForArtists(artists, resource));
            }
        } else if (mediaId.startsWith(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTISTS)) {
            String artists = MediaIDHelper.getHierarchy(mediaId)[1];
            for (MediaMetadataCompat metadata : getMusicByArtists(artists))
                mediaItems.add(createMediaItem(metadata));
        } else if (MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL.equals(mediaId)) {
            for (String title : getTitle()) {
                MediaMetadataCompat mediaMetadataCompat = getMusicByTitle(title);
                mediaItems.add(createMediaItemForTitle(mediaMetadataCompat));
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

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForAlbums(String albums, Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUMS, albums))
                .setTitle(albums)
                .setSubtitle(resources.getString(R.string.browse_musics_by_genre_subtitle, albums))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowserCompat.MediaItem createBrowsableMediaItemForArtists(String artists, Resources resources) {
        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaIDHelper.createMediaID(null, MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTISTS, artists))
                .setSubtitle(resources.getString(R.string.browse_musics_by_genre_subtitle, artists))
                .build();
        return new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
    }


    private ArrayList<MediaBrowserCompat.MediaItem> createBrowsableMediaItemForRoot(Resources resources) {
        ArrayList<MediaBrowserCompat.MediaItem> mMediaItem = new ArrayList<>();

        for (int i = 0; i < listValues.length; i++) {
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(listValues[i])
                    .setTitle(resources.getString(mResources[i]))
                    .setSubtitle(resources.getString(R.string.browse_musics_by_genre_subtitle, resources.getString(mResources[i])))
                    .build();
            mMediaItem.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        }
        return mMediaItem;
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

    private MediaBrowserCompat.MediaItem createMediaItemForTitle(MediaMetadataCompat metadataCompat) {
        Log.e(TAG, "createMediaId for Title is Called ");
        String Title = metadataCompat.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
        String hierarchyAwareMediaID = MediaIDHelper.createMediaID(metadataCompat.getDescription().getMediaId(), MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL, Title);
        MediaMetadataCompat copy = new MediaMetadataCompat.Builder(metadataCompat)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                .build();
        return new MediaBrowserCompat.MediaItem(copy.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

}

