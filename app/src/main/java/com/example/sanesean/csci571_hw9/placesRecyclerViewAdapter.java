package com.example.sanesean.csci571_hw9;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;
import java.util.List;

/**
 * Created by sanesean on 2018/4/9.
 */

public class placesRecyclerViewAdapter extends RecyclerView.Adapter<placesRecyclerViewAdapter.ViewHolder> {

    private List<String> icons;
    private List<String> names;
    private List<String> vicinities;
    private List<String> placeId;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private String name;
    ImageView iconView;
    TextView myTextView;
    TextView vicView;
    ImageView favView;

    // data is passed into the constructor
    placesRecyclerViewAdapter(Context context, List<String> icons,List<String> names,List<String> vicinities,List<String> placeId) {
        this.mInflater = LayoutInflater.from(context);
        this.icons=icons;
        this.names=names;
        this.vicinities=vicinities;
        this.placeId=placeId;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.place_row_layout, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        final String icon = icons.get(position);
        final String name=names.get(position);
        final String vicinity=vicinities.get(position);
        final String id=placeId.get(position);
        final boolean isFav=MainActivity.favorites.containsKey(id);
        try{
            URL url = new URL(icon);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            iconView.setImageBitmap(bmp.createScaledBitmap(bmp, 200, 200, false));
            myTextView.setText(name);
            vicView.setText(vicinity);
            if(isFav){
                favView.setImageResource(R.drawable.heart_fill_red);
            }
            else{
                favView.setImageResource(R.drawable.heart_outline_black);
            }
            favView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(!isFav){
                        favView.setImageResource(R.drawable.heart_fill_red);
                        Toast.makeText(v.getContext(),name+" was added to favorites",Toast.LENGTH_SHORT).show();
                        JSONObject newFav=new JSONObject();
                        try{
                            newFav.put("name",name);
                            newFav.put("icon",icon);
                            newFav.put("vicinity",vicinity);
                            MainActivity.favorites.put(id,newFav.toString());
                        }catch(Exception e){}
                        notifyDataSetChanged();
                    }else{
                        Toast.makeText(v.getContext(),name+" was removed from favorites",Toast.LENGTH_SHORT).show();
                        favView.setImageResource(R.drawable.heart_outline_black);
                        MainActivity.favorites.remove(id);
                        Intent intent=new Intent();
                        intent.setAction("com.favFragment");
                        intent.putExtra("changeUI","true");
                        v.getContext().sendBroadcast(intent);
                        notifyDataSetChanged();
                    }

                }
            });
        }catch(Exception e){

        }

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return names.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ViewHolder(View itemView) {
            super(itemView);
            iconView=itemView.findViewById(R.id.icon);
            myTextView = itemView.findViewById(R.id.name);
            vicView=itemView.findViewById(R.id.vic);
            favView=itemView.findViewById(R.id.fav);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return names.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }
//    public void clear() {
//        final int size = data.size();
//        data.clear();
//        notifyItemRangeRemoved(0, size);
//    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}