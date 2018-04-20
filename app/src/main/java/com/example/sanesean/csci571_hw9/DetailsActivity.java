package com.example.sanesean.csci571_hw9;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public  class DetailsActivity extends AppCompatActivity implements InfoFragment.OnFragmentInteractionListener,
        PhotoFragment.OnFragmentInteractionListener,MapFragment.OnFragmentInteractionListener,ReviewFragment.OnFragmentInteractionListener{
    private GetUrlContentTask http;
    TextView text1;
    TextView text2;
    TextView text3;
    TextView text4;
    public List<String> detailFragments = new ArrayList<String>();
    String placeId;
    String response="";
    ImageView shareIcon;
    ImageView favIcon;
    String twitName="";
    String twitAddr="";
    String twitUrl="";
    String twitWeb="";
    private ProgressDialog pd;
    private JSONObject resultsJSON;
    public static Handler mHandler=new Handler();
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent();
        placeId = intent.getStringExtra("placeID");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(intent.getStringExtra("title"));


        shareIcon = (ImageView) findViewById(R.id.shareIcon);
        shareIcon.setClickable(true);
        shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitAddr=twitAddr.replace("#","%23");
                String url=twitWeb;
                if(!twitUrl.isEmpty()){
                    url=twitUrl;
                }
                String tweetUrl = "https://twitter.com/intent/tweet?text=Check out "+twitName+" located at "+
                        twitAddr+". Website: "+url+"&button_hashtag=TravelAndEntertainmentSearch";
                Uri uri = Uri.parse(tweetUrl);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));

            }
        });
        favIcon = (ImageView) findViewById(R.id.favIcon);
        favIcon.setClickable(true);
        if(MainActivity.favorites.containsKey(placeId)){
            favIcon.setImageResource(R.drawable.heart_fill_white);
        }
        favIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.favorites.containsKey(placeId)){//remove
                    Toast.makeText(v.getContext(),
                            twitName+" was removed from favorites",
                            Toast.LENGTH_LONG).show();
                    MainActivity.favorites.remove(placeId);
                    favIcon.setImageResource(R.drawable.heart_outline_white);
                }else{
                    Toast.makeText(v.getContext(), twitName+" was added to favorites", Toast.LENGTH_LONG).show();
                    String temp;
                    try{
                        JSONObject obj=new JSONObject(response);
                        temp=obj.getString("result");
                        MainActivity.favorites.put(placeId,temp);
                        favIcon.setImageResource(R.drawable.heart_fill_white);
                        Intent intent=new Intent();
                        intent.setAction("com.favFragment");
                        intent.putExtra("changeUI","true");
                        v.getContext().sendBroadcast(intent);
                    }catch(Exception e){}

                }
                Intent intent = new Intent();
                intent.setAction("com.placeResultsActivity");
                intent.putExtra("changeUI","true");
                sendBroadcast(intent);
            }
        });

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        detailFragments.add(InfoFragment.class.getName());
        detailFragments.add(PhotoFragment.class.getName());
        detailFragments.add(MapFragment.class.getName());
        detailFragments.add(ReviewFragment.class.getName());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        //info tab
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_title_view);
        View tab1_view = tabLayout.getTabAt(0).getCustomView();
        ImageView img1 = (ImageView) tab1_view.findViewById(R.id.tabimage);
        text1= (TextView) tab1_view.findViewById(R.id.tabtext);
        img1.setImageResource(R.drawable.info_outline);
        text1.setText("INFO");
        text1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        text1.setTextColor(Color.argb(255, 255, 255, 255));
        //photo Tab
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_title_view);
        View tab1_view2 = tabLayout.getTabAt(1).getCustomView();
        ImageView img2 = (ImageView) tab1_view2.findViewById(R.id.tabimage);
        text2= (TextView) tab1_view2.findViewById(R.id.tabtext);
        img2.setImageResource(R.drawable.photos);
        text2.setText("PHOTOS");
        //map tab
        tabLayout.getTabAt(2).setCustomView(R.layout.tab_title_view);
        View tab1_view3 = tabLayout.getTabAt(2).getCustomView();
        ImageView img3 = (ImageView) tab1_view3.findViewById(R.id.tabimage);
        text3= (TextView) tab1_view3.findViewById(R.id.tabtext);
        img3.setImageResource(R.drawable.maps);
        text3.setText("MAP");
        //review tab
        tabLayout.getTabAt(3).setCustomView(R.layout.tab_title_view);
        View tab1_view4 = tabLayout.getTabAt(3).getCustomView();
        ImageView img4 = (ImageView) tab1_view4.findViewById(R.id.tabimage);
        text4= (TextView) tab1_view4.findViewById(R.id.tabtext);
        img4.setImageResource(R.drawable.review);
        text4.setText("REVIEWS");
        //set divider
        for(int i=0;i<3;i++){
            View root = tabLayout.getChildAt(i);
            if (root instanceof LinearLayout) {
                ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(ContextCompat.getColor(DetailsActivity.this, R.color.white));
                drawable.setSize(2, 2);
                drawable.setAlpha(128);
                ((LinearLayout) root).setDividerPadding(10);
                ((LinearLayout) root).setDividerDrawable(drawable);
            }
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int numTab = tab.getPosition();
                mViewPager.setCurrentItem(tab.getPosition());
                if(numTab==0){
                    text1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    text1.setTextColor(Color.argb(255, 255, 255, 255));
                    text2.setTypeface(Typeface.DEFAULT);
                    text2.setTextColor(Color.argb(128, 255, 255, 255));
                    text3.setTypeface(Typeface.DEFAULT);
                    text3.setTextColor(Color.argb(128, 255, 255, 255));
                    text4.setTypeface(Typeface.DEFAULT);
                    text4.setTextColor(Color.argb(128, 255, 255, 255));
                }else if(numTab==1){
                    text1.setTypeface(Typeface.DEFAULT);
                    text1.setTextColor(Color.argb(128, 255, 255, 255));
                    text2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    text2.setTextColor(Color.argb(255, 255, 255, 255));
                    text3.setTypeface(Typeface.DEFAULT);
                    text3.setTextColor(Color.argb(128, 255, 255, 255));
                    text4.setTypeface(Typeface.DEFAULT);
                    text4.setTextColor(Color.argb(128, 255, 255, 255));
                    //photo
                    Intent intent = new Intent();
                    intent.setAction("com.PhotoFragment");
                    if(hasKey(resultsJSON,"photos")){
                        try{
                            JSONArray list=resultsJSON.getJSONArray("photos");
                            intent.putExtra("photos",placeId);
                            sendBroadcast(intent);
                        }catch(Exception e){
                        }
                    }
                }else if(numTab==2){
                    text1.setTypeface(Typeface.DEFAULT);
                    text1.setTextColor(Color.argb(128, 255, 255, 255));
                    text2.setTypeface(Typeface.DEFAULT);
                    text2.setTextColor(Color.argb(128, 255, 255, 255));
                    text3.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    text3.setTextColor(Color.argb(255, 255, 255, 255));
                    text4.setTypeface(Typeface.DEFAULT);
                    text4.setTextColor(Color.argb(128, 255, 255, 255));
                    //map
                    Intent intent = new Intent();
                    intent.setAction("com.MapFragment");
                    if(hasKey(resultsJSON,"geometry")){
                        try{
                            String title=resultsJSON.getString("name");
                            JSONObject obj=resultsJSON.getJSONObject("geometry").getJSONObject("location");
                            intent.putExtra("lat",obj.getString("lat"));
                            intent.putExtra("lng",obj.getString("lng"));
                            intent.putExtra("title",title);
                            sendBroadcast(intent);
                        }catch(Exception e){
                        }
                    }
                }else{
                    text1.setTypeface(Typeface.DEFAULT);
                    text1.setTextColor(Color.argb(128, 255, 255, 255));
                    text2.setTypeface(Typeface.DEFAULT);
                    text2.setTextColor(Color.argb(128, 255, 255, 255));
                    text3.setTypeface(Typeface.DEFAULT);
                    text3.setTextColor(Color.argb(128, 255, 255, 255));
                    text4.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    text4.setTextColor(Color.argb(255, 255, 255, 255));
                    //send reviews
                    if(hasKey(resultsJSON,"reviews")){
                        Intent intent = new Intent();
                        intent.setAction("com.ReviewFragment");
                        try{
                            JSONArray reviews=resultsJSON.getJSONArray("reviews");
                            String [] list=new String[reviews.length()];
                            for(int i=0;i<reviews.length();i++){
                                list[i]=reviews.getJSONObject(i).toString();
                            }
                            if(hasKey(resultsJSON,"formatted_address")){
                                String [] addr=resultsJSON.getString("formatted_address").toString().split(",");
                                int len=addr.length-1;
                                String country=addr[len];
                                String state=addr[len-1].split(" ")[1];
                                String city=addr[len-2];
                                String address="";
                                if(len-3>-1){
                                    address=addr[len-3];
                                }
                                intent.putExtra("name",twitName);
                                intent.putExtra("addr",address);
                                intent.putExtra("city",city);
                                intent.putExtra("state",state);
                                intent.putExtra("country",country);
                            }

                            intent.putExtra("list",list);
                            intent.putExtra("placeId",placeId);
                            sendBroadcast(intent);
                        }catch(Exception e){
                            Log.e("review tab",e+"");
                        }
                    }else{//no google, maybe yelp
                        Intent intent = new Intent();
                        intent.setAction("com.ReviewFragment");
                        try {
                            if (hasKey(resultsJSON, "formatted_address")) {
                                String[] addr = resultsJSON.getString("formatted_address").toString().split(",");
                                int len = addr.length - 1;
                                String country = addr[len];
                                String state = addr[len - 1].split(" ")[1];
                                String city = addr[len - 2];
                                String address = "";
                                if (len - 3 > -1) {
                                    address = addr[len - 3];
                                }
                                intent.putExtra("name", twitName);
                                intent.putExtra("addr", address);
                                intent.putExtra("city", city);
                                intent.putExtra("state", state);
                                intent.putExtra("country", country);
                            }
                            Log.e("here","");
                            intent.putExtra("list", "");
                            intent.putExtra("placeId", placeId);
                            sendBroadcast(intent);
                        }catch(Exception e){
                        }
                    }
                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        //progress bar, set title

        Thread mThread = new Thread() {
            @Override
            public void run() {

                http=new GetUrlContentTask();
                String param="placeID="+placeId;
                response=http.doInBackground("http://yichisheng-hw9.us-east-2.elasticbeanstalk.com/details",param);
                if(placeResultsActivity.pd!=null){
                    placeResultsActivity.pd.dismiss();
                }
                if(favFragment.pd!=null){
                    favFragment.pd.dismiss();
                }
                Intent intent = new Intent();
                intent.setAction("com.InfoFragment");
                try{
                    resultsJSON = new JSONObject(response);
                    resultsJSON=resultsJSON.getJSONObject("result");
                    twitName=resultsJSON.getString("name");
                    if(hasKey(resultsJSON,"formatted_address")){
                        intent.putExtra("address",resultsJSON.getString("formatted_address"));
                        twitAddr=resultsJSON.getString("formatted_address");
                    }
                    if(hasKey(resultsJSON,"formatted_phone_number")){
                        intent.putExtra("phone",resultsJSON.getString("formatted_phone_number"));
                    }
                    if(hasKey(resultsJSON,"price_level")){
                        intent.putExtra("price",resultsJSON.getString("price_level"));
                    }
                    if(hasKey(resultsJSON,"rating")){
                        intent.putExtra("rating",resultsJSON.getString("rating"));
                    }
                    if(hasKey(resultsJSON,"url")){
                        intent.putExtra("google",resultsJSON.getString("url"));
                        twitWeb=resultsJSON.getString("url");
                    }
                    if(hasKey(resultsJSON,"website")){
                        intent.putExtra("website",resultsJSON.getString("website"));
                        twitUrl=resultsJSON.getString("website");
                    }
                    sendBroadcast(intent);



                }catch(Exception e){
                    Log.e("parse detail",e+"");
                }


            }
        };
        mThread.start();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id== android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //FRAGMENT interface
    @Override
    public void onFragmentInteraction(Uri uri){
    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public List<String> fragmentsA;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentsA = detailFragments;
        }

        @Override
        public Fragment getItem(int position) {
            //return MyFragment.newInstance();
            Fragment fragment = null;
            fragment =  Fragment.instantiate(DetailsActivity.this, detailFragments.get(position));
            Bundle bundle = new Bundle();
            bundle.putString("id",""+position);
            fragment.setArguments(bundle);
            return fragment;

        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if(position==0){ Fragment fragment = Fragment.instantiate(DetailsActivity.this, detailFragments.get(position));
                getSupportFragmentManager().beginTransaction().hide(fragment).commit();
            }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            //return CONTENT[position % CONTENT.length].toUpperCase();
            return fragmentsA.get(position%4 ).toUpperCase();
        }

        @Override
        public int getCount() {
            // return CONTENT.length;
            return 4;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }
    public String getResponse(){
        Log.e("inActivity",response);
        return response;
    }
    public boolean hasKey(JSONObject obj,String key){
        try{
            obj.getString(key);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    public class photoURL implements Serializable {
        String url;
        public photoURL(String name) {
            url=name;
        }
        public String getUrl(){
            return url;
        }
    }
}
