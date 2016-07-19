package com.coderming.newxyzreader;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by linna on 7/16/2016.
 */
public class FullScreenDialog extends DialogFragment {
    private static final String LOG_TAG = FullScreenDialog.class.getSimpleName();

    public static final String FULLSCREEN_URL = "FULLSCREEN_URL";
    public static final String FULLSCREEN_RATIO = "FULLSCREEN_RATIO";

    public FullScreenDialog() {

    }
    public static FullScreenDialog newInstance(String urlStr, float aspectRatio) {
        Log.v(LOG_TAG, "newInstance, urlStr="+urlStr);
        FullScreenDialog frag = new FullScreenDialog();
        Bundle args = new Bundle();
        args.putString(FULLSCREEN_URL, urlStr);
        args.putFloat(FULLSCREEN_RATIO,aspectRatio);
        frag.setArguments(args);

        frag.setShowsDialog(false);         //???
//        frag.setCancelable(false);  not cancelable by touch outside
        return frag;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle bundle) {
        View rootView =  inflater.inflate(R.layout.activity_fullscreen_image, parent);
        Bundle args = getArguments();
        float aspectRatio = args.getFloat(ArticleDetailFragment.FULLSCREEN_RATIO, 1.5f );
        loadImage(args.getString(ArticleDetailFragment.FULLSCREEN_URL),
                (ImageView) rootView.findViewById(R.id.large_photo), aspectRatio );
        rootView.findViewById(R.id.close).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        return rootView;
    }

    private void loadImage(String urlStr, ImageView imageView, float aspectRatio) {
        boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (((aspectRatio > 1f) && !isLandscape) || ((aspectRatio<1f) && isLandscape) ) {
            Glide.with(this).load(urlStr).asBitmap()
                    .transform( new RotateTransformation(getContext(), 90f) )
                    .error(R.drawable.not_available).into(imageView);

        } else {
            Glide.with(this).load(urlStr).asBitmap().error(R.drawable.not_available).into(imageView);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
     }
    // remove title bar
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onResume() {
        // Get existing layout params for the window
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        // Call super onResume after sizing
        super.onResume();
    }
}
