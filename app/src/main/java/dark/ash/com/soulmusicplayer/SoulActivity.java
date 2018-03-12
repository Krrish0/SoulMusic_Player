package dark.ash.com.soulmusicplayer;

import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

import dark.ash.com.soulmusicplayer.data.PlayerAdapter;
import dark.ash.com.soulmusicplayer.model.LocalDataProvider;

public class SoulActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soul);

        RecyclerView playerRecyclerView = findViewById(R.id.soul_activity_recyclerview);
        playerRecyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        playerRecyclerView.setLayoutManager(layoutManager);
        LocalDataProvider localData = new LocalDataProvider(this);
        ArrayList<MediaMetadataCompat> songsList = localData.iterator();
        PlayerAdapter mAdaper = new PlayerAdapter(songsList);
        playerRecyclerView.setAdapter(mAdaper);
    }
}
