package dark.ash.com.soulmusicplayer.utils;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import dark.ash.com.soulmusicplayer.VoiceSearchParams;
import dark.ash.com.soulmusicplayer.model.MusicProvider;

import static dark.ash.com.soulmusicplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL;
import static dark.ash.com.soulmusicplayer.utils.MediaIDHelper.MEDIA_ID_MUSICS_BY_SEARCH;

/**
 * Created by hp on 19-03-2018.
 */

public class QueueHelper {

    private static final String TAG = QueueHelper.class.getSimpleName();

    private static final int RANDOM_QUEUE_SIZE = 10;

    public static List<MediaSessionCompat.QueueItem> getPlayingQueue(String mediaID, MusicProvider musicProvider) {

        //extract the browsing hierarchy from the mediaID.
        String[] hierarchy = MediaIDHelper.getHierarchy(mediaID);

        if (hierarchy.length != 2) {
            Log.e(TAG, "Could not build a playing queue for this mediaID: " + mediaID);
            return null;
        }
        //Category Type used in the playing Queue
        String categoryType = hierarchy[0];
        //Category Value used in the Playing Queue
        String categoryValue = hierarchy[1];

        Log.e(TAG, "Creating playing queue for " + categoryType + categoryValue);

        Iterable<MediaMetadataCompat> tracks = null;

        if (categoryType.equals(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE)) {
            tracks = musicProvider.getMusicByGenre(categoryValue);
        } else if (categoryType.equals(MEDIA_ID_MUSICS_BY_SEARCH)) {
            tracks = musicProvider.searchMusicBySongTitle(categoryValue);
        } else if (categoryType.equals(MEDIA_ID_MUSICS_BY_ALL)) {
            tracks = musicProvider.searchMusicBySongTitle(categoryValue);
        }
        if (tracks == null) {
            Log.e(TAG, "Unrecgnized category type: " + categoryType + " for media " + mediaID);
            return null;
        }
        return convertToQueue(tracks, hierarchy[0], hierarchy[1]);
    }

    public static List<MediaSessionCompat.QueueItem> getRandomQueue(MusicProvider musicProvider) {
        List<MediaMetadataCompat> result = new ArrayList<>(RANDOM_QUEUE_SIZE);
        Iterable<MediaMetadataCompat> shuffled = musicProvider.getShuffleMusic();
        for (MediaMetadataCompat metadata : shuffled) {
            if (result.size() == RANDOM_QUEUE_SIZE) {
                break;
            }
            result.add(metadata);
        }
        Log.d(TAG, "getRandomQueue: result.size=" + result.size());

        return convertToQueue(result, MEDIA_ID_MUSICS_BY_SEARCH, "random");
    }


    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue, long queueId) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (queueId == item.getQueueId()) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public static List<MediaSessionCompat.QueueItem> getPlayingQueueFromSearch(String query,
                                                                               Bundle queryPrams,
                                                                               MusicProvider musicProvider) {
        Log.e(TAG, "Creating playing queue for musics from search: " + query + " params = " + queryPrams);
        VoiceSearchParams params = new VoiceSearchParams(query, queryPrams);
        Log.e(TAG, "VoiceSearchParams: " + params);

        if (params.isAny) {
            return getRandomQueue(musicProvider);
        }
        List<MediaMetadataCompat> result = null;
        if (params.isAlbumFocus) {
            result = musicProvider.searchMusicByAlbum(params.album);
        } else if (params.isGenreFocus) {
            result = musicProvider.searchMusicByGenre(params.genre);
        } else if (params.isArtistFocus) {
            result = musicProvider.searchMusicByArtist(params.artist);
        } else if (params.isSongFocus) {
            result = musicProvider.searchMusicBySongTitle(params.song);
        }
        if (params.isUnstructured || result == null || result.iterator().hasNext()) {
            result = musicProvider.searchMusicBySongTitle(query);
            if (result.isEmpty()) {
                result = musicProvider.searchMusicByGenre(query);
            }
        }
        return convertToQueue(result, MEDIA_ID_MUSICS_BY_SEARCH, query);
    }

    public static int getMusicIndexOnQueue(Iterable<MediaSessionCompat.QueueItem> queue, String mediaID) {
        int index = 0;
        for (MediaSessionCompat.QueueItem item : queue) {
            if (mediaID.equals(item.getDescription().getMediaId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static List<MediaSessionCompat.QueueItem> convertToQueue(Iterable<MediaMetadataCompat>
                                                                             tracks,
                                                                     String... categories) {
        List<MediaSessionCompat.QueueItem> queue = new ArrayList<>();
        int count = 0;
        for (MediaMetadataCompat track : tracks) {
            String hierarchyAwareMediaID = MediaIDHelper.createMediaID(track.getDescription().getMediaId(), categories);
            MediaMetadataCompat trackCopy = new MediaMetadataCompat.Builder(track)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, hierarchyAwareMediaID)
                    .build();
            MediaSessionCompat.QueueItem item = new MediaSessionCompat.QueueItem(trackCopy.getDescription(), count++);
            queue.add(item);
        }
        return queue;
    }

    public static boolean isIndexPlayable(int index, List<MediaSessionCompat.QueueItem> queue) {
        return (queue != null && index >= 0 && index < queue.size());
    }
}
