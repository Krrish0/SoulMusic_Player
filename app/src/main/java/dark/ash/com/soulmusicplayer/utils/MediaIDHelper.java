package dark.ash.com.soulmusicplayer.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.text.TextUtils;

import java.util.Arrays;

/**
 * Created by hp on 19-03-2018.
 */

public class MediaIDHelper {

    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_MUSICS_BY_GENRE = "__BY_GENRE__";
    public static final String MEDIA_ID_MUSICS_BY_SEARCH = "__BY_SEARCH__";
    public static final String MEDIA_ID_MUSICS_BY_ALBUMS = "__BY_ALBUMS__";
    public static final String MEDIA_ID_MUSICS_BY_ARTISTS = "__BY_ARTISTS__";
    public static final String MEDIA_ID_MUSICS_BY_ALL = "__ALL_SONGS";

    private static final char CATEGORY_SEPERATOR = '/';
    private static final char LEAF_SEPERATOR = '|';

    public static String createMediaID(String mediaID, String... categories) {
        StringBuilder sb = new StringBuilder();
        if (categories != null) {
            for (int i = 0; i < categories.length; i++) {
                if (!isValidCategory(categories[i])) {
                    throw new IllegalArgumentException("Invalid Category: " + categories[i]);
                }
                sb.append(categories[i]);
                if (i < categories.length - 1) {
                    sb.append(CATEGORY_SEPERATOR);
                }
            }
        }
        if (mediaID != null) {
            sb.append(LEAF_SEPERATOR).append(mediaID);
        }
        return sb.toString();
    }

    private static boolean isValidCategory(String category) {
        return category == null || (category.indexOf(CATEGORY_SEPERATOR) < 0 && category.indexOf(LEAF_SEPERATOR) < 0);
    }

    public static String extractMusicIDFromMediaID(@NonNull String mediaID) {
        int pos = mediaID.indexOf(LEAF_SEPERATOR);
        if (pos >= 0) {
            return mediaID.substring(pos + 1);
        }
        return null;
    }

    public static @NonNull
    String[] getHierarchy(@NonNull String mediaID) {
        int pos = mediaID.indexOf(LEAF_SEPERATOR);
        if (pos >= 0) {
            mediaID = mediaID.substring(0, pos);
        }
        return mediaID.split(String.valueOf(CATEGORY_SEPERATOR));
    }

    public static String extractBrowseCategoryValueFromMediaID(@NonNull String mediaID) {
        String[] hierarchy = getHierarchy(mediaID);
        if (hierarchy.length == 2) {
            return hierarchy[1];
        }
        return null;
    }

    public static boolean isBrowseable(@NonNull String mediaID) {
        return mediaID.indexOf(LEAF_SEPERATOR) < 0;
    }

    public static String getParentMediaID(@NonNull String mediaID) {
        String[] hierarchy = getHierarchy(mediaID);
        if (!isBrowseable(mediaID)) {
            return createMediaID(null, hierarchy);
        }

        if (hierarchy.length <= 1) {
            return MEDIA_ID_ROOT;
        }
        String[] parentHierarchy = Arrays.copyOf(hierarchy, hierarchy.length - 1);
        return createMediaID(null, parentHierarchy);
    }

    public static boolean isMediaItemPlaying(Activity context, MediaBrowserCompat.MediaItem mediaItem) {

        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(context);
        if (controllerCompat != null && controllerCompat.getMetadata() != null) {
            String currentPlayingMediaID = controllerCompat.getMetadata().getDescription().getMediaId();

            String itemMusicID = MediaIDHelper.extractMusicIDFromMediaID(mediaItem.getDescription().getMediaId());
            return currentPlayingMediaID != null && TextUtils.equals(currentPlayingMediaID, itemMusicID);
        }
        return false;
    }


}
