package dark.ash.com.soulmusicplayer.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;

import dark.ash.com.soulmusicplayer.utils.MusicFetch;

/**
 * Created by hp on 10-03-2018.
 */

public class LocalDataProvider {

    private static final String PLAYER_MUSIC = "music";
    private static final String PLAYER_TITLE = "title";
    private static final String PLAYER_ALBUM = "album";
    private static final String PLAYER_ARTIST = "artist";
    private static final String PLAYER_GENRE = "genre";
    private static final String PLAYER_SOURCE = "source";
    private static final String PLAYER_IMAGE = "image";
    private static final String PLAYER_TRACK_NUMBER = "trackNumber";
    private static final String PLAYER_TOTAL_TRACK_COUNT = "totalTrackCount";
    private static final String PLAYER_DURATION = "duration";
    private Context mContext;

    public LocalDataProvider(Context context) {
        this.mContext = context;
    }
    //MediaMetadataCompat Class to store the metadaa of the Audio File
    //this class returns the ArrayList of the metadata of the mediaFile.

    public ArrayList<MediaMetadataCompat> iterator() {
        Cursor cursor = MusicFetch.getAudioCursor(mContext);
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
                String artist = cursor.getString(cursor.getColumnIndex(Media.ARTIST));
                String album = cursor.getString(cursor.getColumnIndex(Media.ALBUM));
                String title = cursor.getString(cursor.getColumnIndex(Media.TITLE));

                MediaMetadataCompat mediaData = new MediaMetadataCompat.Builder().putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title).build();
                tracksList.add(mediaData);
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        cursor.close();
        return tracksList;
    }

}
