package dark.ash.com.soulmusicplayer.data;

import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import dark.ash.com.soulmusicplayer.ui.FragmentAlbums;
import dark.ash.com.soulmusicplayer.ui.FragmentArtists;
import dark.ash.com.soulmusicplayer.ui.FragmentBase;
import dark.ash.com.soulmusicplayer.ui.FragmentGenre;
import dark.ash.com.soulmusicplayer.ui.FragmentPlaylist;
import dark.ash.com.soulmusicplayer.ui.FragmentSongs;
import dark.ash.com.soulmusicplayer.utils.MediaIDHelper;

public class SoulPagerAdapter extends FragmentStatePagerAdapter {

    private static String TAG = SoulPagerAdapter.class.getSimpleName();

    private int mPosition;
    private FragmentBase mCurrentFragment;

    public SoulPagerAdapter(FragmentManager manager, int position) {
        super(manager);
        this.mPosition = position;
    }


    @Override
    public FragmentBase getItem(int position) {
        switch (position) {
            case 1:
                this.mCurrentFragment = new FragmentPlaylist();
                this.mCurrentFragment.setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLISTS);
                break;
            case 2:
                this.mCurrentFragment = new FragmentAlbums();
                this.mCurrentFragment.setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUMS);
                break;
            case 3:
                this.mCurrentFragment = new FragmentArtists();
                this.mCurrentFragment.setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_ARTISTS);
                break;
            case 4:
                this.mCurrentFragment = new FragmentGenre();
                this.mCurrentFragment.setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_GENRE);
                break;
            case 5:
                this.mCurrentFragment = new FragmentSongs();
                this.mCurrentFragment.setMediaId(MediaIDHelper.MEDIA_ID_MUSICS_BY_ALL);
                break;
            default:
                return new FragmentAlbums();
        }

        return this.mCurrentFragment;
    }

    @Override
    public int getCount() {
        return mPosition;
    }

    public FragmentBase getCurrentFragment() {
        return this.mCurrentFragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 1:
                return "Playlists";
            case 2:
                return "Albums";
            case 3:
                return "Artists";
            case 4:
                return "Genres";
            case 5:
                return "All Songs";
            default:
                return null;

        }
    }

}
