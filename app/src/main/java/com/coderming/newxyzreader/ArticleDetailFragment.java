package com.coderming.newxyzreader;

import android.animation.ObjectAnimator;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.transition.TransitionInflater;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.coderming.newxyzreader.remote.Utility;


public class ArticleDetailFragment extends Fragment {
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();

    public static final String ARG_ITEM_ID = "item_id";
    private static final int LOADER_ID = 1;

//    private static String[] QueryProjection = {
//            ItemsContract.Items._ID,
//            ItemsContract.Items.PUBLISHED_DATE,
//            ItemsContract.Items.AUTHOR,
//            ItemsContract.Items.THUMB_URL,
//            ItemsContract.Items.PHOTO_URL,
//            ItemsContract.Items.ASPECT_RATIO,
//            ItemsContract.Items.BODY,
//    };
//
    static final int _ID = 0;
    static final int PUBLISHED_DATE = 1;
    static final int AUTHOR = 2;
    static final int TITLE = 3;
    static final int THUMB_URL = 4;
    static final int PHOTO_URL = 5;
    static final int ASPECT_RATIO = 6;
    static final int BODY = 7;

    private OnFragmentInteractionListener mListener;
    private Cursor mCursor;
    private int mPosition;

    private ImageView mImageView;
    private TextView mAuthor;
    private TextView mDate;
    private TextView mArticle;
    private View mZoonButton;
    private View mFrameContainer;
    View mCard;

    public ArticleDetailFragment() {
        // Required empty public constructor
    }

    public static ArticleDetailFragment newInstance(Cursor cursor) {
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.mCursor = cursor;
        fragment.mPosition = cursor.getPosition();
        Log.v(LOG_TAG, "newInstance: mPosition="+Long.toString(fragment.mPosition));
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public static final String FULLSCREEN_URL = "FULLSCREEN_URL";
    public static final String FULLSCREEN_RATIO = "FULLSCREEN_RATIO";

    private View.OnClickListener mFullScreenHandler = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mCursor != null) {
                mCursor.moveToPosition(mPosition);
                Bundle args = new Bundle();
                args.putString(FULLSCREEN_URL, mCursor.getString(PHOTO_URL ));
                args.putFloat(FULLSCREEN_RATIO,mCursor.getFloat(ASPECT_RATIO));
                ((ArticleDetailActivity)getActivity()).showDialog(ArticleDetailFragment.this, args);
            }
        }
    };
    boolean mSmall = true;
    private boolean mSymmetric = true;
    float mPivotDelta;
    boolean mIsPortrait;

    private void scaleTextY(View view) {
//        mCard.setPivotY(mPivotDelta);  don't know why, it no longer work

        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_slow_in);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mCard, View.SCALE_Y, (mSmall ? 1.5f : 1f));
        scaleY.setInterpolator(interpolator);
        scaleY.setDuration(600L);
        scaleY.start();

        // toggle the state so that we switch between large/small and symmetric/asymmetric
        mSmall = !mSmall;
        if (mSmall) {
            mSymmetric = !mSymmetric;
        }

    }
    private void scaleTextX(View view) {
//        mCard.setPivotX(0);       // don't know why, it no longer work
        Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.fast_out_slow_in);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mCard, View.SCALE_X, (mSmall ? 1.5f : 1f));
        scaleX.setInterpolator(interpolator);
        scaleX.setDuration(600L);
        scaleX.start();

        // toggle the state so that we switch between large/small and symmetric/asymmetric
        mSmall = !mSmall;
    }
    View.OnTouchListener mCardSizeChange = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
//            Log.v(LOG_TAG, String.format("onTouch: +++MotionEvent.ACTION_UP=%d,action=%d",
//                    MotionEvent.ACTION_UP, motionEvent.getAction()));
            if (MotionEvent.ACTION_UP == motionEvent.getAction() ) {
                if (mIsPortrait)
                    scaleTextY(mCard);
                else
                    scaleTextX(mCard);
                return true;
            } else
                return false;
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mIsPortrait = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

        mImageView = (ImageView) rootView.findViewById(R.id.photo);
        mAuthor = (TextView) rootView.findViewById(R.id.detail_article_author);
        mDate = (TextView) rootView.findViewById(R.id.detail_article_date);
        mArticle = (TextView) rootView.findViewById(R.id.detail_article_text);
        mArticle.setMovementMethod(new ScrollingMovementMethod());   // textView has scroll
        mFrameContainer = rootView.findViewById(R.id.detail_content);
        mZoonButton = rootView.findViewById(R.id.zoom_photo);
        mZoonButton.setOnClickListener(mFullScreenHandler);
        mImageView.setOnClickListener(mFullScreenHandler);

        if (mIsPortrait){
            mCard = rootView.findViewById(R.id.card_text);
            mPivotDelta = mCard.getPivotY() * 2;
        } else {
            mCard = rootView.findViewById(R.id.anch_to);
        }
// remove size change        mArticle.setOnTouchListener(mCardSizeChange);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setSharedElementEnterTransition(
                    TransitionInflater.from(getContext()).inflateTransition(R.transition.image_move));
        }
    }

    private void showZoomButton() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mZoonButton.setTranslationY(-metrics.heightPixels/4);
        try {
            Interpolator interpolator = new FastOutSlowInInterpolator(); // OvershootInterpolator(), BounceInterpolator();
            mZoonButton.animate().setInterpolator(interpolator)
                    .setDuration(500)
                    .setStartDelay(1500)
                    .translationYBy(metrics.heightPixels/4)
                    .start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        if (mCursor != null) {
            bindViews(mCursor);
        } else {
            Log.v(LOG_TAG, "onResume: mCursor is null");
        }
        showZoomButton();
    }
    private void bindViews( Cursor cursor) {
        mCursor.moveToPosition(mPosition);
        Log.v(LOG_TAG, String.format("++++bindViews: pos=%d, _ID=%d, title=%s, author=%s",mPosition,
                mCursor.getLong(_ID), mCursor.getString(TITLE), cursor.getString(AUTHOR)));
        mAuthor.setText("By "+cursor.getString(AUTHOR));
        String str = mIsPortrait ? ", " : "";
        mDate.setText(str + Utility.formatPublishDate(cursor.getLong(PUBLISHED_DATE)));
        str = Html.fromHtml(cursor.getString(BODY)).toString();
        mArticle.setText( mCursor.getString(TITLE)+": "+str);
        final float ratio = cursor.getFloat(ASPECT_RATIO);
        final String urlStr = cursor.getString(THUMB_URL);
        mFrameContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
                mFrameContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                if (mIsPortrait) {
                    int ht =  mFrameContainer.getHeight();
                    int wt = Math.max(Math.round(ht * ratio), ht);
                    mFrameContainer.getLayoutParams().width = wt;
                    mImageView.getLayoutParams().width = wt;
                } else {
                    int ht = Math.round(mFrameContainer.getWidth() / ratio);
                    mImageView.getLayoutParams().height = ht;
                }
                Glide.with(getContext()).load(urlStr).asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE ).skipMemoryCache((true))
                        .placeholder(R.drawable.not_available)
                        .centerCrop()
                        .into(mImageView);
                Log.v(LOG_TAG, String.format("++++w=%d,h=%d, ratio=%f", mImageView.getLayoutParams().width,
                        mImageView.getLayoutParams().height, ratio));
                return true;    // == allow drawing
            }
        });
    }
    //  Glide.listner(requestListener) won't compile
    private RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            Log.w(LOG_TAG, String.format("Glide.onException: model=%s,isFirstResource=%s, ex=%s",
                    model,isFirstResource,e.getMessage()));     // todo log exception
             // important to return false so the error placeholder can be placed
            return false;
        }
         @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
             Drawable drawable = resource.getCurrent();
             Log.w(LOG_TAG, String.format("Glide.onResourceReady: model=%s,isFirstResource=%s, w=%d, h=%d",
                     model,isFirstResource, drawable.getBounds().width(), drawable.getBounds().height()));     // todo log exception
             return false;
        }
    };
    //    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
