package com.coderming.newxyzreader;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.coderming.newxyzreader.data.ItemsContract;
import com.coderming.newxyzreader.remote.Utility;

/**
 * Created by linna on 7/14/2016.
 */
public class CursorRecyclerViewAdapter
        extends RecyclerView.Adapter<CursorRecyclerViewAdapter.MyViewHolder> {
    private static final String LOG_TAG = CursorRecyclerViewAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;
    private boolean mDataIsValid;
//    private DataSetObserver mDataSetObserver;
    private MyAdapterOnClickHandler mClickHandler;
    private int rowIdColumn;

    int _ID = 0;
    int TITLE = 1;
    int PUBLISHED_DATE = 2;
    int AUTHOR = 3;
    int THUMB_URL = 4;
    int ASPECT_RATIO = 5;

    public static interface MyAdapterOnClickHandler {
        void onClick(long recId, MyViewHolder vh);
    }

    public CursorRecyclerViewAdapter(Context context, MyAdapterOnClickHandler handler ) {
        mClickHandler = handler;
        mContext = context;
        mDataIsValid = false;
   }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        itemView.setFocusable(true);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.v(LOG_TAG, "onBindViewHolder position="+Integer.toString(position));
        if ((mCursor != null) && (mCursor.moveToPosition(position))) {
            String str = Utility.formatAuthorDate(mCursor.getString(AUTHOR), mCursor.getLong(PUBLISHED_DATE));
            holder.mAuthor.setText(str);
            holder.mTitle.setText(mCursor.getString(TITLE));
            String urlStr = mCursor.getString(THUMB_URL);
            final float ratio = mCursor.getFloat(ASPECT_RATIO);
            holder.mPhoto.setAspectRatio(mCursor.getFloat(ASPECT_RATIO));
            Glide.with(mContext).load(urlStr).asBitmap().placeholder(R.drawable.not_available)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE ).fitCenter().dontAnimate()
                    .into(holder.mPhoto);
        }
    }
    @Override
    public int getItemCount() {
        if (mDataIsValid && mCursor != null){
            return mCursor.getCount();
        } else {
            return 0;
        }
    }
    public Cursor swapCursor(Cursor newCursor){
        if (newCursor == mCursor){
            return null;
        }
        final Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (mCursor != null){
            rowIdColumn = newCursor.getColumnIndexOrThrow(ItemsContract.ItemsColumns._ID);
            mDataIsValid = true;
        } else {
            rowIdColumn = -1;
            mDataIsValid = false;
        }
        notifyDataSetChanged();
        return oldCursor;
    }
    @Override public long getItemId(int position) {
        if (mDataIsValid && (mCursor != null) && mCursor.moveToPosition(position)){
            return mCursor.getLong(rowIdColumn);
        } else {
            return position;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        MyImageView mPhoto;
        TextView mAuthor;
        TextView mTitle;

        public MyViewHolder(View itemView){
            super(itemView);
            mPhoto = (MyImageView) itemView.findViewById(R.id.thumb);
            mAuthor = (TextView) itemView.findViewById(R.id.article_author);
            mTitle = (TextView) itemView.findViewById(R.id.article_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long rid = mCursor.getLong(rowIdColumn);
            Log.v(LOG_TAG, "++++onClick: recId="+Long.toString(rid));
            mClickHandler.onClick(rid, this);
        }
    }
}
