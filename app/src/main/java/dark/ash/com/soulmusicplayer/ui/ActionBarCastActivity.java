package dark.ash.com.soulmusicplayer.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import dark.ash.com.soulmusicplayer.R;
import dark.ash.com.soulmusicplayer.SoulActivity;

/**
 * Created by hp on 26-03-2018.
 */

public class ActionBarCastActivity extends AppCompatActivity {

    //Yet to Implemented

    private static final String TAG = ActionBarCastActivity.class.getSimpleName();

    private static final int DELAY_MILLIS = 1000;

    //TODO NO cast Support Yet
    private MenuItemCompat mMediaRouteMediaItem;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private final FragmentManager.OnBackStackChangedListener mBackStackChangedListener =
            new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    updateDrawerToggle();
                }
            };
    private DrawerLayout mDrawerLayout;
    private boolean mToolbarInitialized;
    private int mItemToOpenWhenDrawerCloses = -1;
    private final DrawerLayout.DrawerListener mDrawerListener = new DrawerLayout.DrawerListener() {
        @Override
        public void onDrawerClosed(View drawerView) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerClosed(drawerView);
            if (mItemToOpenWhenDrawerCloses >= 0) {
                //TODO set custom Animation

                Class activityClass = null;
                switch (mItemToOpenWhenDrawerCloses) {
                    case R.id.navigation_home:
                        activityClass = SoulActivity.class;
                        break;
                }
                if (activityClass != null) {
                    startActivity(new Intent(ActionBarCastActivity.this, activityClass));
                    finish();
                }
            }
        }

        @Override
        public void onDrawerStateChanged(int newState) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerStateChanged(newState);
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerSlide(drawerView, slideOffset);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if (mDrawerToggle != null) mDrawerToggle.onDrawerOpened(drawerView);
            if (getSupportActionBar() != null) getSupportActionBar()
                    .setTitle(R.string.app_name);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Log.e(TAG, "Activity onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mToolbarInitialized) {
            throw new IllegalStateException("You must run super.initializeToolbar ");
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSupportFragmentManager().addOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackChangedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item != null && item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerToggle != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        mToolbar.setTitle(title);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        mToolbar.setTitle(titleId);
    }

    protected void initializeToolbar() {
        mToolbar = findViewById(R.id.soul_toolbar);
        if (mToolbar == null) {
            throw new IllegalStateException("Layout is required to include a Toolbar with id");
        }
        mToolbar.inflateMenu(R.menu.main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            if (navigationView == null) {
                throw new IllegalStateException("Layout requires a NavigationView ");
            }

            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                    R.string.open_content_drawer, R.string.close_content_drawer);
            mDrawerLayout.addDrawerListener(mDrawerListener);
            populateDrawerItems(navigationView);
            setSupportActionBar(mToolbar);
            updateDrawerToggle();
        } else {
            setSupportActionBar(mToolbar);
        }
        mToolbarInitialized = true;
    }

    private void populateDrawerItems(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setChecked(true);
                mItemToOpenWhenDrawerCloses = item.getItemId();
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        if (SoulActivity.class.isAssignableFrom(getClass())) {
            navigationView.setCheckedItem(R.id.navigation_home);
        }
    }

    protected void updateDrawerToggle() {

        if (mDrawerToggle == null) {
            return;
        }

        boolean isRoot = getSupportFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isRoot);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(!isRoot);
            getSupportActionBar().setDisplayHomeAsUpEnabled(!isRoot);
            getSupportActionBar().setHomeButtonEnabled(!isRoot);
        }
        if (isRoot) {
            mDrawerToggle.syncState();
        }
    }
}
