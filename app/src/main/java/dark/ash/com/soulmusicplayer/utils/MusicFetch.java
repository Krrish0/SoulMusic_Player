package dark.ash.com.soulmusicplayer.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by hp on 10-03-2018.
 */

public class MusicFetch {

    public static final Uri EX_CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;


    public static Cursor getExternalAudioCursor(Context mContext) {
        ContentResolver contentResolver = mContext.getContentResolver();

        Uri uri = EX_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        return cursor;
    }


}
