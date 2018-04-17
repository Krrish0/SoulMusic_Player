package dark.ash.com.soulmusicplayer.ui;

import android.support.v4.app.Fragment;

public abstract class FragmentBase extends Fragment {

    public abstract String getMediaId();

    public abstract void setMediaId(String mediaId);

    public abstract void onConnected();

}