package com.example.sanesean.csci571_hw9;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements searchFragment.OnFragmentInteractionListener,
        favFragment.OnFragmentInteractionListener,FragmentToActivity,LocationListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private Context context;
    LocationManager mLocationManager;
    //UI components
    public List<String> fragments = new ArrayList<String>();
    TabLayout tabLayout;
    TextView text1;
    TextView text2;

    //search page
    private double lat=34.0252213;
    private double lng=-118.2822566;
    private int progressStatus=0;
    private StringBuilder sb;
    public  ProgressDialog pd;
    public static Map<String,String> favorites;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        Intent intent = new Intent();
        intent.setAction("com.favFragment");
        intent.putExtra("UI","a");
        sendBroadcast(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } else {
            // Permission has already been granted
        }
        //locaton GPS
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationProvider gpsProvider = mLocationManager.getProvider(LocationManager.GPS_PROVIDER);
        if(mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        }else{
            Toast.makeText(this, "Cannot locate your position, please open your GPS", Toast.LENGTH_SHORT).show();
        }
        //favorite
        favorites=new HashMap<>();

        //page
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        fragments.add(searchFragment.class.getName());
        fragments.add(favFragment.class.getName());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout= (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_title_view);
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_title_view);
        View tab1_view = tabLayout.getTabAt(0).getCustomView();
        View tab2_view = tabLayout.getTabAt(1).getCustomView();

        ImageView img1 = (ImageView) tab1_view.findViewById(R.id.tabimage);
        ImageView img2 = (ImageView) tab2_view.findViewById(R.id.tabimage);
        text1= (TextView) tab1_view.findViewById(R.id.tabtext);
        text2=(TextView) tab2_view.findViewById(R.id.tabtext);
        img1.setImageResource(R.drawable.search);
        img2.setImageResource(R.drawable.favorite);
        text1.setText("SEARCH");
        text2.setText("FAVORITES");
        text1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        text1.setTextColor(Color.argb(255, 255, 255, 255));

        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(ContextCompat.getColor(context, R.color.white));
            drawable.setSize(1, 1);
            drawable.setAlpha(128);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }




        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int numTab = tab.getPosition();


                if(numTab==0){
                    text1.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    text1.setTextColor(Color.argb(255, 255, 255, 255));
                    text2.setTypeface(Typeface.DEFAULT);
                    text2.setTextColor(Color.argb(128, 255, 255, 255));
                }else{
                    text2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    text2.setTextColor(Color.argb(255, 255, 255, 255));
                    text1.setTypeface(Typeface.DEFAULT);
                    text1.setTextColor(Color.argb(128, 255, 255, 255));
                    Intent intent = new Intent();
                    intent.setAction("com.favFragment");
                    intent.putExtra("UI","a");
                    sendBroadcast(intent);
                }
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            public void onPageSelected(int position) {
                TabLayout.Tab tab = tabLayout.getTabAt(position);
                tab.select();
            }
        });

    }
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public List<String> fragmentsA;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentsA = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            //return MyFragment.newInstance();
            return Fragment.instantiate(context, fragmentsA.get(position));

        }

        @Override
        public CharSequence getPageTitle(int position) {
            //return CONTENT[position % CONTENT.length].toUpperCase();
            return fragmentsA.get(position % 2).toUpperCase();
        }

        @Override
        public int getCount() {
            // return CONTENT.length;
            return 2;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

    }
    @Override
    public void onFragmentInteraction(Uri uri){

    }
    @Override
    public void onLocationChanged(Location location) {
        lat=location.getLatitude();
        lng=location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(MainActivity.this, "Please enable the GPS", Toast.LENGTH_SHORT).show();
    }
    public void communicate(List<String> s,boolean isValidate) {
        if(!isValidate){
            Toast.makeText(MainActivity.this,"Please fix all fields with errors", Toast.LENGTH_SHORT).show();
        }
        else{
            pd=new ProgressDialog(MainActivity.this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setMessage("Fetching results");
            pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
            pd.setIndeterminate(true);
            pd.show();

            sb=new StringBuilder();
            sb.append("keyword=");
            sb.append(s.get(0));
            sb.append("&category=");
            sb.append(s.get(1));
            sb.append("&distance=");
            sb.append(s.get(2));
            sb.append("&from=");
            sb.append(s.get(3).equals("true")?"here":"others");
            sb.append("&lat=");
            sb.append(lat);
            sb.append("&lng=");
            sb.append(lng);
            sb.append("&custom=");
            sb.append(s.get(4));
            Thread mThread = new Thread() {
                @Override
                public void run() {
                    String response=fetchData(sb.toString());
                    Intent intent = new Intent(MainActivity.this, placeResultsActivity.class);
                    intent.putExtra("EXTRA_MESSAGE", response);
                    pd.dismiss();
                    startActivity(intent);
                }
            };
            mThread.start();
        }

    }
    public String fetchData(String param){
        GetUrlContentTask http=new GetUrlContentTask();
        String response=http.doInBackground("http://yichisheng-hw9.us-east-2.elasticbeanstalk.com/places",param);
        return response;

    }

}
