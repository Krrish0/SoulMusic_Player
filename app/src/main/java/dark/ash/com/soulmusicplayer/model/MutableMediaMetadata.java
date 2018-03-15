package dark.ash.com.soulmusicplayer.model;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

/**
 * Created by hp on 13-03-2018.
 */

public class MutableMediaMetadata {

    public final String trackId;
    public MediaMetadataCompat metadata;

    public MutableMediaMetadata(String trackId, MediaMetadataCompat metadata) {
        this.metadata = metadata;
        this.trackId = trackId;

    }

    public MediaMetadataCompat getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != MutableMediaMetadata.class) {
            return false;
        }

        MutableMediaMetadata that = (MutableMediaMetadata) o;

        return TextUtils.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }
}
