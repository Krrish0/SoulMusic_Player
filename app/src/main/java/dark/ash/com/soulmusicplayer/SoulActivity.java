package dark.ash.com.soulmusicplayer;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import dark.ash.com.soulmusicplayer.data.PlayerAdapter;
import dark.ash.com.soulmusicplayer.model.MusicProvider;
import dark.ash.com.soulmusicplayer.model.MutableMediaMetadata;

public class SoulActivity extends AppCompatActivity {

    private static final String TAG = SoulActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soul);


        final RecyclerView playerRecyclerView = findViewById(R.id.soul_activity_recyclerview);
        playerRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        playerRecyclerView.setLayoutManager(layoutManager);
        final MusicProvider musicProvider = new MusicProvider(this);
        final ArrayList<MediaMetadataCompat> listOfValues = new ArrayList<>();
        musicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
            @Override
            public void onMusicCatalogReady(boolean success) {
                if (!success) {
                    Log.e(TAG, "Unable to Load List");
                }
                if (success) {
                    Log.e(TAG, "List Successfully Loaded");
                    Collection<MutableMediaMetadata> values = musicProvider.getmMusicListById().values();
                    Log.e(TAG, "Call to Songs Started");
                    for (MutableMediaMetadata metadata : values) {
                        listOfValues.add(metadata.getMetadata());
                    }
                    PlayerAdapter adapter = new PlayerAdapter(listOfValues);
                    playerRecyclerView.swapAdapter(adapter, true);
                }

            }
        });



    }
}
