package com.example.sanesean.csci571_hw9;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

class ReviewRecyclerViewAdpter extends RecyclerView.Adapter<ReviewRecyclerViewAdpter.ViewHolder> {

    private List<String> reviewList;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    ImageView profileView;
    TextView nameView;
    RatingBar ratingView;
    TextView timeView;
    TextView textView;
    // data is passed into the constructor
    ReviewRecyclerViewAdpter(Context context, List<String>  list) {
        this.mInflater = LayoutInflater.from(context);
        reviewList=list;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.single_review_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        try{
            final JSONObject obj=new JSONObject(reviewList.get(position));
            if(obj.getString("profile_photo_url")!=null){
                new DownloadImageTask(profileView)
                        .execute(obj.getString("profile_photo_url"));
            }
            nameView.setText(obj.getString("author_name"));
            nameView.setTextColor(Color.parseColor("#01A98C"));
            float r=Float.parseFloat(obj.getString("rating"));
            ratingView.setRating(r);
            String formattedDate="";
            if(obj.getString("time").indexOf("-")!=-1){
                formattedDate=obj.getString("time");
            }else{
                long unixSeconds=Long.parseLong(obj.getString("time"));
                Date date = new java.util.Date(unixSeconds*1000L);
                SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                formattedDate = sdf.format(date);
            }
            timeView.setText(formattedDate);
            textView.setText(obj.getString("text"));
        }catch(Exception e){
            Log.e("Reivew JSON PARSE ERROR",e+"");
        }
    }


    // total number of rows
    @Override
    public int getItemCount() {
        return reviewList.size();
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ViewHolder(View itemView) {
            super(itemView);
            profileView=(ImageView)itemView.findViewById(R.id.profileView);
            nameView=(TextView)itemView.findViewById(R.id.reviewName);
            ratingView=(RatingBar) itemView.findViewById(R.id.ratingReview);
            timeView=(TextView)itemView.findViewById(R.id.reviewTime);
            textView=(TextView)itemView.findViewById(R.id.reviewText);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
                mIcon11 = Bitmap.createScaledBitmap(
                        mIcon11, 150, 150, false);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
