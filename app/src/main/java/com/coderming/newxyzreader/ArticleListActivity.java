package com.coderming.newxyzreader;

import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.coderming.newxyzreader.data.ItemsContract;
import com.coderming.newxyzreader.data.UpdaterService;
import com.coderming.newxyzreader.remote.Utility;

public class ArticleListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = ArticleListActivity.class.getSimpleName();

    private static int LOADER_ID = 0;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean mIsRefreshing;

    static final  String[] PROJECTION = {
            ItemsContract.Items._ID,
            ItemsContract.Items.TITLE,
            ItemsContract.Items.PUBLISHED_DATE,
            ItemsContract.Items.AUTHOR,
            ItemsContract.Items.THUMB_URL,
            ItemsContract.Items.ASPECT_RATIO
    };


    private CursorRecyclerViewAdapter.MyAdapterOnClickHandler mOnClickListener = new
            CursorRecyclerViewAdapter.MyAdapterOnClickHandler() {
         @Override
        public void onClick(long recId, CursorRecyclerViewAdapter.MyViewHolder vh) {
             Uri uri = ItemsContract.Items.buildItemUri(recId);
             Log.v(LOG_TAG, String.format(LOG_TAG, "+++2+onClick: recId=%d,uri=%s",Long.toString(recId), uri.toString()));
             Intent intent = new Intent(ArticleListActivity.this, ArticleDetailActivity.class)
                     .setData(uri);
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                 View view = vh.mPhoto;
                 String tn = view.getTransitionName();
                 if (tn == null) {
                     view.setTransitionName(getString(R.string.main_transion_photo));
                 }
                 Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(ArticleListActivity.this,
                        view, view.getTransitionName() ).toBundle();
                 startActivity(intent, bundle);
             } else {
                 startActivity(intent);
             }
        }
    };
    class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;
            outRect.top = space;

//            // Add top margin only for the first item to avoid double space between items
//            if (parent.getChildLayoutPosition(view) == 0) {
//                outRect.top = space;
//            } else {
//                outRect.top = 0;
//            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        CursorRecyclerViewAdapter adapter = new CursorRecyclerViewAdapter(this, mOnClickListener);

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(Math.round(getResources().getDimension(R.dimen.gap_grid))));
        Configuration configuration = getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int columnCount = screenWidthDp/130;                              //
        StaggeredGridLayoutManager layoutManager =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        if ((savedInstanceState == null) && Utility.isNetworkAvailable(this)) {
            refresh();
        }

        GlideBuilder builder = new GlideBuilder(this);
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888);

        if (Build.VERSION.SDK_INT >= 21 ) {
            Explode explode = new Explode();
            // set an enter transition
            getWindow().setEnterTransition(new Explode());
            // set an exit transition
//            getWindow().setExitTransition(new Explode());
        }
    }
    boolean mRefreshIssued;
    private void refresh() {
        if (!mRefreshIssued) {
            mRefreshIssued = true;
            startService(new Intent(this, UpdaterService.class));
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }
    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
//                updateRefreshingUI();
                mRefreshIssued = false;
            }
        }
    };
    private void  updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ItemsContract.Items.buildDirUri();
        return new CursorLoader(getApplicationContext(), uri, PROJECTION, null,null,
                ItemsContract.Items.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished, #rec="+Integer.toString(data.getCount()) );
        ((CursorRecyclerViewAdapter)mRecyclerView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorRecyclerViewAdapter)mRecyclerView.getAdapter()).swapCursor(null);
    }
}
