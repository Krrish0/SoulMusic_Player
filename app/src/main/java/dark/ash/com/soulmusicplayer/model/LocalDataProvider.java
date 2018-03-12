package dark.ash.com.soulmusicplayer.model;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;

import dark.ash.com.soulmusicplayer.utils.MusicFetch;

/**
 * Created by hp on 10-03-2018.
 */

public class LocalDataProvider {

    private static final String TAG = LocalDataProvider.class.getSimpleName();

    private Context mContext;

    public LocalDataProvider(Context context) {
        this.mContext = context;
    }
    //MediaMetadataCompat Class to store the metadaa of the Audio File
    //this class returns the ArrayList of the metadata of the mediaFile.

    public ArrayList<MediaMetadataCompat> iterator() {
        Cursor cursor = MusicFetch.getExternalAudioCursor(mContext);
        ArrayList<MediaMetadataCompat> tracks = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            tracks.addAll(buildfromCursor(cursor));
        }
        return tracks;
    }

    //A Helper function that is used to Create a ArrayList
    // from a cursor Object and return to the calling function

    private ArrayList<MediaMetadataCompat> buildfromCursor(Cursor cursor) {

        ArrayList<MediaMetadataCompat> tracksList = new ArrayList<>();
        while (cursor.moveToNext()) {
            try {
                String path = cursor.getString(cursor.getColumnIndex(Audio.Media.DATA));
                int mediaId = cursor.getInt(cursor.getColumnIndex(Audio.Media._ID));
                String artist = cursor.getString(cursor.getColumnIndex(Audio.Media.ARTIST));
                String albumKey = cursor.getString(cursor.getColumnIndex(Audio.Media.ALBUM_KEY));
                String album = cursor.getString(cursor.getColumnIndex(Audio.Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndex(Audio.Media.TITLE));
                String genre = getGenreName(mediaId);
                String albumArtUri = getAlbumArt(albumKey);
                if (genre == null) {
                    genre = "Unknown";
                }
                long duration = cursor.getLong(cursor.getColumnIndex(Audio.Media.DURATION));


                MediaMetadataCompat mediaData = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(mediaId))
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
        if (genreCursor.moveToFirst()) {
            stringGenre = genreCursor.getString(genreCursor.getColumnIndex(Audio.Genres.NAME));
            return stringGenre;
        }

        return stringGenre;
    }

    private String getAlbumArt(String albumKey) {

        Uri uri = Audio.Albums.getContentUri("external");
        String albumArtImage = null;
        String selection = Audio.Albums.ALBUM_KEY + " = ?";
        String[] selectionArgs = {albumKey};
        Cursor albumCursor = mContext.getContentResolver().query(uri, null, selection, selectionArgs, null);
        if (albumCursor.moveToFirst()) {
            albumArtImage = albumCursor.getString(albumCursor.getColumnIndex(Audio.Albums.ALBUM_ART));
        }
        return albumArtImage;
    }
}
