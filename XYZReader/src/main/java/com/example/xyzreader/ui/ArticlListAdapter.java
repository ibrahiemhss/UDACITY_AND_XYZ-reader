package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.util.ItemClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/*
 * Created by ibrahim on 13/07/18.
 */

class ArticlListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = ArticlListAdapter.class.toString();

    private final Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private final ItemClickListener itemClickListener;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat outputFormat = new SimpleDateFormat();
    private final GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);
    // Most time functions can only handle 1902 - 2037
    private final Context context;
    private int mMutedColor = 0xFF333333;


    public ArticlListAdapter(Context context, Cursor cursor, LayoutInflater mLayoutInflater,
                             ItemClickListener itemClickListener) {
        this.context = context;
        mCursor = cursor;
        this.mLayoutInflater = mLayoutInflater;
        this.itemClickListener = itemClickListener;

    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.list_item_article, parent, false);

        return new ViewHolder(view);
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onlItemClick(getItemId(holder.getAdapterPosition()));
            }
        });


        if (!publishedDate.before(START_OF_EPOCH.getTime())) {

            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }

        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(context).getImageLoader());


        ImageLoaderHelper.getInstance(context).getImageLoader()
                .get(mCursor.getString(ArticleLoader.Query.THUMB_URL), new ImageLoader.ImageListener() {
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette p = Palette.generate(bitmap, 12);
                            mMutedColor = p.getDarkMutedColor(0xFF333333);
                            holder.LinearListContainer
                                    .setBackgroundColor(mMutedColor);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });


        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));


    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}

class ViewHolder extends RecyclerView.ViewHolder {
    public final DynamicHeightNetworkImageView thumbnailView;
    public final TextView titleView;
    public final TextView subtitleView;
    public final LinearLayout LinearListContainer;

    public ViewHolder(View view) {
        super(view);
        thumbnailView = view.findViewById(R.id.thumbnail);
        titleView = view.findViewById(R.id.article_title);
        subtitleView = view.findViewById(R.id.article_subtitle);
        LinearListContainer = view.findViewById(R.id.LinearListContainer);


    }

}
