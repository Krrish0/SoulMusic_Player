package dark.ash.com.soulmusicplayer.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;
import java.util.Iterator;

import dark.ash.com.soulmusicplayer.utils.MusicFetch;

/**
 * Created by hp on 10-03-2018.
 */

public class LocalDataProvider implements MusicProviderSource {
    //TODO Use a cursor Loader to implement this class so that we
    //can monitor the changes made to the main Database Program


    private static final String TAG = LocalDataProvider.class.getSimpleName();
    private Cursor cursor = null;
    private boolean IndexCached = false;
    private boolean IndexCachedGenre = false;
    private boolean IndexCachedAlbumArt = false;
    private ArrayMap<String, Integer> mMapMedia = new ArrayMap<>();
    private ArrayMap<String, Integer> mMapGenre = new ArrayMap<>();
    private ArrayMap<String, Integer> mMapAlbumArt = new ArrayMap<>();

    private void cacheColumnIndex() {
        mMapMedia.put(Audio.Media.DATA, cursor.getColumnIndex(Audio.Media.DATA));
        mMapMedia.put(Audio.Media._ID, cursor.getColumnIndex(Audio.Media._ID));
        mMapMedia.put(Audio.Media.ARTIST, cursor.getColumnIndex(Audio.Media.ARTIST));
        mMapMedia.put(Audio.Media.ALBUM_KEY, cursor.getColumnIndex(Audio.Media.ALBUM_KEY));
        mMapMedia.put(Audio.Media.ALBUM, cursor.getColumnIndex(Audio.Media.ALBUM));
        mMapMedia.put(Audio.Media.TITLE, cursor.getColumnIndex(Audio.Media.TITLE));
        mMapMedia.put(Audio.Media.DURATION, cursor.getColumnIndex(Audio.Media.DURATION));

    }

    private Context mContext;


    //MediaMetadataCompat Class to store the metadaa of the Audio File
    //this class returns the ArrayList of the metadata of the mediaFile.

    public LocalDataProvider(Context context) {
        this.mContext = context;
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        cursor = MusicFetch.getExternalAudioCursor(mContext);

        if (!IndexCached) {
            cacheColumnIndex();
            IndexCached = true;
        }

        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            tracks.addAll(buildfromCursor(cursor));
        }
        if (cursor != null) {
            cursor.close();
        }
        return tracks.iterator();
    }

    //A Helper function that is used to Create a ArrayList
    // from a cursor Object and return to the calling function

    private ArrayList<MediaMetadataCompat> buildfromCursor(Cursor cursor) {

        ArrayList<MediaMetadataCompat> tracksList = new ArrayList<>();
        while (cursor.moveToNext()) {
            try {
                String path = cursor.getString(mMapMedia.get(Audio.Media.DATA));
                int mediaId = cursor.getInt(mMapMedia.get(Audio.Media._ID));
                String artist = cursor.getString(mMapMedia.get(Audio.Media.ARTIST));
                String albumKey = cursor.getString(mMapMedia.get(Audio.Media.ALBUM_KEY));
                String album = cursor.getString(mMapMedia.get(Audio.Media.ALBUM));
                String title = cursor.getString(mMapMedia.get(Audio.Media.TITLE));
                String genre = getGenreName(mediaId);
                String albumArtUri = getAlbumArt(albumKey);
                if (genre == null) {
                    genre = "Unknown";
                }
                long duration = cursor.getLong(mMapMedia.get(Audio.Media.DURATION));
                String Id = String.valueOf(path.hashCode());


                MediaMetadataCompat mediaData = new MediaMetadataCompat.Builder()
                        .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, path)
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, Id)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, albumArtUri)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration).build();
                tracksList.add(mediaData);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        return tracksList;
    }

    private String getGenreName(int musicId) {

        Uri uri = Audio.Genres.getContentUriForAudioId("external", musicId);
        String stringGenre = null;

        Cursor genreCursor = mContext.getContentResolver().query(uri, null, null, null, null);
        try {
            if (!IndexCachedGenre) {
                mMapGenre.put(Audio.Genres.NAME, genreCursor.getColumnIndex(Audio.Genres.NAME));
            }
            if (genreCursor.moveToFirst()) {
                stringGenre = genreCursor.getString(mMapGenre.get(Audio.Genres.NAME));
                return stringGenre;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            genreCursor.close();
        }

        return stringGenre;
    }

    private String getAlbumArt(String albumKey) {

        Uri uri = Audio.Albums.getContentUri("external");
        String albumArtImage = null;
        String selection = Audio.Albums.ALBUM_KEY + " = ?";
        String[] selectionArgs = {albumKey};
        Cursor albumCursor = mContext.getContentResolver().query(uri, null, selection, selectionArgs, null);
        try {
            if (!IndexCachedAlbumArt) {
                mMapAlbumArt.put(Audio.Albums.ALBUM_ART, albumCursor.getColumnIndex(Audio.Albums.ALBUM_ART));
            }
            if (albumCursor.moveToFirst()) {
                albumArtImage = albumCursor.getString(mMapAlbumArt.get(Audio.Albums.ALBUM_ART));
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            albumCursor.close();
        }

        return albumArtImage;
    }


}
