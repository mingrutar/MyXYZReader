package com.coderming.newxyzreader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coderming.newxyzreader.data.ItemsContract;

/**
 * Created by linna on 7/13/2016.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, FullScreenFragment.OnFragmentInteractionListener {
    private static final String LOG_TAG = ArticleDetailActivity.class.getSimpleName();

    private static final String DIALOGFRAGMENT_TAG = "DIALOGFRAGMENT_TAG";

    private static final String SELECTED_ITEM = "SELECTED_ITEM";
    private static final String ARTICLLE_SHARE_HASHTAG = "#XYZREADERSHARE";
    private static final int LOADER_ID = 0;

    // we only have 17 items, so load all of them here and pass cursor to ArticleDetailFragment.
    // if we have more items, we should only query _ID and Title and ArticleDetailFragment query itself.
    private static String[] QueryProjection = {
            ItemsContract.Items._ID,
            ItemsContract.Items.PUBLISHED_DATE,
            ItemsContract.Items.AUTHOR,
            ItemsContract.Items.TITLE,
            ItemsContract.Items.THUMB_URL,
            ItemsContract.Items.PHOTO_URL,
            ItemsContract.Items.ASPECT_RATIO,
            ItemsContract.Items.BODY,
    };

    private String mMyShareString;  //TODO:  url of article or title?
    private ShareActionProvider mShareActionProvider;
    private Cursor mCursor;
    private ViewPager mViewPager;
//    private long mStartId;
    private FloatingActionButton mFab;
    private TextView mDetailTitle;

    private long mSelectedItemId;

    public void setArticleTitle(String title) {
        mDetailTitle.setText( title );
    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (mSelectedItemId > 0)
            outState.putLong(SELECTED_ITEM, mSelectedItemId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSelectedItemId = -1;
        if (getIntent() != null && getIntent().getData() != null) {
            Uri uri = getIntent().getData();
            try {
                mSelectedItemId = Long.parseLong(uri.getLastPathSegment());
                Log.v(LOG_TAG, String.format(LOG_TAG, "+++3+onCreate: recId=%d,uri=%s", Long.toString(mSelectedItemId), uri.toString()));
            } catch (Exception ex) {
                Log.w(LOG_TAG, "fail to parse id: url="+uri.toString());
            }
        }

        mDetailTitle = (TextView) findViewById(R.id.detail_article_title);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(mPageListener);

        if ((savedInstanceState != null) && (savedInstanceState.containsKey(SELECTED_ITEM))
                && ( mSelectedItemId < 0)) {
            Long val = savedInstanceState.getLong(SELECTED_ITEM);
            mSelectedItemId = (val == null) ? 0 : val;
//            if (getIntent() != null && getIntent().getData() != null) {
//                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
//            }
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        //TODO: verify if there is other way
        mShareActionProvider = new ShareActionProvider(ArticleDetailActivity.this);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareingIntent = new Intent(Intent.ACTION_SEND)
 //                   .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    .setType("text/plain")
                        // see https://guides.codepath.com/android/Sharing-Content-with-Intents
                        // text "Share using", url="Share link using"
                    .putExtra(Intent.EXTRA_TEXT, mMyShareString+ARTICLLE_SHARE_HASHTAG);
                startActivity(Intent.createChooser(shareingIntent, "Share using"));
            }
        });

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

        Log.v(LOG_TAG, "++++onCreate, mSelectedItemId="+Long.toString(mSelectedItemId));
    }
    int mSelPos = -1;
    ViewPager.OnPageChangeListener mPageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int state) {
            Log.v(LOG_TAG, String.format("-+++onPageScrollStateChanged, state=%d",state));
        }
        @Override
        public void onPageSelected(int position) {
            Log.v(LOG_TAG, String.format("-+++onPageSelected, pos=%d, mCursor=%s",position
                    , (mCursor==null)?"null":"yes" ) );
            if (mCursor != null) {
                mCursor.moveToPosition(position);
                mSelectedItemId = mCursor.getLong(ArticleDetailFragment._ID);
                setArticleTitle(mCursor.getString(ArticleDetailFragment.TITLE));
                if (mSelPos != position) {
                    mViewPager.setCurrentItem(position);
                    mSelPos = position;
                }
            }
        }
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            Log.v(LOG_TAG, String.format("-+++onPageScrolled, pos=%d, positionOffset=%f,positionOffsetPixels=%d"
//                    , position, positionOffset, positionOffsetPixels));
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> ret = null;
        if (id == LOADER_ID) {
            Uri uri = ItemsContract.Items.buildDirUri();
            ret = new CursorLoader(this, uri, QueryProjection, null, null, ItemsContract.Items.DEFAULT_SORT);
        }
        return ret;
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
//        if (Build.VERSION.SDK_INT >= 21) {
//            Slide slide = new Slide(Gravity.BOTTOM);
//            slide.addTarget(R.id.detail_article_text);
//            slide.setInterpolator(AnimationUtils.loadInterpolator(this,
//                    android.R.interpolator.linear_out_slow_in));
//            slide.setDuration(350);
//            slide.setStartDelay(1500);
//            getWindow().setEnterTransition(slide);
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void showDialog(ArticleDetailFragment frag,  Bundle args ) {
        FullScreenFragment fullScreenFragment = new FullScreenFragment();
        fullScreenFragment.setArguments(args);
        findViewById(R.id.dialg_container).setVisibility(View.VISIBLE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .add(R.id.dialg_container, fullScreenFragment, DIALOGFRAGMENT_TAG)
                .addToBackStack("transaction");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            Transition changeTransform = TransitionInflater.from(this).
                    inflateTransition(R.transition.change_image_transform);
            Transition explodeTransform = TransitionInflater.from(this).
                    inflateTransition(android.R.transition.explode);

            frag.setSharedElementReturnTransition(changeTransform);
            frag.setExitTransition(explodeTransform);

            fullScreenFragment.setSharedElementEnterTransition(changeTransform);
            fullScreenFragment.setEnterTransition(explodeTransform);

            View imageView = findViewById(R.id.photo);
            // Add second fragment by replacing first
            ft.addSharedElement(imageView, imageView.getTransitionName());
        }
        // Apply the transaction
        ft.commit();
        mFab.setEnabled(false);
    }
    @Override
    public void closeDialog() {
        // Add second fragment by replacing first
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DIALOGFRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment).commit();
        }
        mFab.setEnabled(true);
        findViewById(R.id.dialg_container).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ((null != data) && data.moveToFirst())  {
            mCursor = data;
            // Select the start ID, try to find last mSelectedItemId
            if (mSelectedItemId != -1) {
                while (!mCursor.isAfterLast()) {
                    if (mCursor.getLong(ArticleDetailFragment._ID) == mSelectedItemId) {
                        final int position = mCursor.getPosition();
                        Log.v(LOG_TAG, String.format("++++onLoadFinished: found: mSelectedItemId=%d, pos=%d", mSelectedItemId, position));
                        mViewPager.getAdapter().notifyDataSetChanged();
                       mViewPager.setCurrentItem(position);
                        return;
                    }
                    mCursor.moveToNext();
                }
            }
            Log.w(LOG_TAG, "+++ ?no selected item...setCurrentItem(0)");
            mViewPager.getAdapter().notifyDataSetChanged();
            mViewPager.setCurrentItem(0);   // could it happend?
        }
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
        mViewPager.getAdapter().notifyDataSetChanged();
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private ArticleDetailFragment mLastItem;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

//        @Override
//        public void setPrimaryItem(ViewGroup container, int position, Object object) {
//            super.setPrimaryItem(container, position, object);
//            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
//            if ((fragment != mLastItem) && (fragment != null)) {
//                mDetailTitle.setText(mCursor.getString(TITLE));
//                mLastItem = fragment;
//            }
//        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            Log.v(LOG_TAG, String.format("++++getItem: pos=%d, _ID=%d, mSelectedItemId=%d",
                    position, mCursor.getLong(ArticleDetailFragment._ID), mSelectedItemId));
            return ArticleDetailFragment.newInstance(mCursor);
        }
        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
