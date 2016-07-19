package com.coderming.newxyzreader;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FullScreenFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class FullScreenFragment extends Fragment {
    private static final String LOG_TAG = FullScreenFragment.class.getSimpleName();

    public static final String FULLSCREEN_URL = "FULLSCREEN_URL";
    public static final String FULLSCREEN_RATIO = "FULLSCREEN_RATIO";

    OnFragmentInteractionListener mListener;

    public FullScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.activity_fullscreen_image, container, false);
        Bundle args = getArguments();
        float aspectRatio = args.getFloat(ArticleDetailFragment.FULLSCREEN_RATIO, 1.5f );
        loadImage(args.getString(ArticleDetailFragment.FULLSCREEN_URL),
                (ImageView) rootView.findViewById(R.id.large_photo), aspectRatio );
        rootView.findViewById(R.id.close).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.closeDialog();
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setSharedElementEnterTransition(
                    TransitionInflater.from(getContext()).inflateTransition(R.transition.image_move));
        }
        return rootView;
    }

    private void loadImage(String urlStr, ImageView imageView, float aspectRatio) {
//        DrawableRequestBuilder<String> thumbnailRequest = Glide
//                .with( context )
//                .load( eatFoodyImages[2] );   //thumbnail
        boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (((aspectRatio > 1f) && !isLandscape) || ((aspectRatio<1f) && isLandscape) ) {
            Glide.with(this).load(urlStr).asBitmap()            // .thumbnail(thumbnailRequest)
                    .transform( new RotateTransformation(getContext(), 90f) )
                    .placeholder(R.drawable.not_available).into(imageView);

        } else {
            Glide.with(this).load(urlStr).asBitmap().thumbnail(0.1f).placeholder(R.drawable.not_available).into(imageView);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void closeDialog();
    }
}
