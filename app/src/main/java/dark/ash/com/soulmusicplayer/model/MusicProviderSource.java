package dark.ash.com.soulmusicplayer.model;

import android.support.v4.media.MediaMetadataCompat;

import java.util.Iterator;

/**
 * Created by hp on 13-03-2018.
 */

public interface MusicProviderSource {

    String CUSTOM_METADATA_TRACK_SOURCE = "__SOURCE__";

    Iterator<MediaMetadataCompat> iterator();
}
