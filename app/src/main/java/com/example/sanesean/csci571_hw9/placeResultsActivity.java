package com.example.sanesean.csci571_hw9;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.sanesean.csci571_hw9.R.id.nextPage;


public class placeResultsActivity extends AppCompatActivity implements placesRecyclerViewAdapter.ItemClickListener {
    private String PageToken="";
    private List<JSONObject> results;
    private String[] placeID=new String [20];
    private List<String> icons;
    private List<String> names;
    private List<String> vicinities;
    private List<String> placeId;
    private int index;
    private TableLayout placeTable;
    private Button prevBtn;
    private Button nextBtn;
    private TextView noResults;
    private GetUrlContentTask http;
    private JSONObject nextJSON;
    private RecyclerView recyclerView;
    private placesRecyclerViewAdapter adapter;
    public  static ProgressDialog pd;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1) {
                showTable(results);
                nextBtn.setEnabled(!PageToken.isEmpty());
                prevBtn.setEnabled(true);
            }
            super.handleMessage(msg);
        }
    };
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        if(adapter!=null){
            adapter.setClickListener(this);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_results);
        Intent intent = getIntent();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search results");
        String response = intent.getStringExtra("EXTRA_MESSAGE");
        recyclerView = (RecyclerView)findViewById(R.id.placesView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noResults=findViewById(R.id.noplaces);
        final PlacePagination thisPlace=new PlacePagination();
        icons=new ArrayList<>();
        names=new ArrayList<>();
        vicinities=new ArrayList<>();
        placeId=new ArrayList<>();
        index=0;
        prevBtn=(Button)findViewById(R.id.prevPage);
        nextBtn=(Button)findViewById(nextPage);

        try {
            JSONObject resultsJSON = new JSONObject(response);
            if(hasKey(resultsJSON,"next_page_token")){
                PageToken = resultsJSON.getString("next_page_token");
                nextBtn.setEnabled(true);
            }else{
                PageToken="";
                thisPlace.setLastPage(index);
            }
            results=getPlacesList(resultsJSON.getJSONArray("results"));
            thisPlace.addPage(index,results);
            showTable(results);
        }catch(Exception e){
        }
        nextBtn.setOnClickListener(new View.OnClickListener(){//click nextBtn
            @Override
            public void onClick(View v) {
                index++;
                if(thisPlace.isNewPage(index)){
                    http=new GetUrlContentTask();
                    Thread mThread = new Thread() {
                        @Override
                        public void run() {
                            String param="token="+PageToken;
                            String response=fetchData(param);
                            try {
                                nextJSON = new JSONObject(response);
                                if(hasKey(nextJSON,"next_page_token")){
                                    PageToken = nextJSON.getString("next_page_token");
                                }else{
                                    PageToken = "";
                                    thisPlace.setLastPage(index);
                                }
                                results=getPlacesList(nextJSON.getJSONArray("results"));
                                thisPlace.addPage(index,results);
                                Message msg = mHandler.obtainMessage();
                                msg.what = 1;
                                msg.sendToTarget();
                            }catch(JSONException e) {
                            }
                        }
                    };
                    mThread.start();
                }else{//this page already exists
                    results=thisPlace.getPage(index);
                    showTable(results);
                    prevBtn.setEnabled(true);
                    if(index==thisPlace.getLast()){
                        nextBtn.setEnabled(false);
                    }
                }
            }
        });
        prevBtn.setOnClickListener(new View.OnClickListener(){//click prevBtn
            @Override
            public void onClick(View v) {
                index--;
                if(index<=0){
                    index=0;
                    prevBtn.setEnabled(false);
                }else{
                    prevBtn.setEnabled(true);
                }
                results=thisPlace.getPage(index);
                showTable(results);
                nextBtn.setEnabled(true);
            }
        });

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private boolean hasKey(JSONObject obj,String key){
        try{
            obj.getString(key);
            return true;
        }catch(Exception e){
            return false;
        }
    }
    private String fetchData(String param){
        return  http.doInBackground("http://yichisheng-hw9.us-east-2.elasticbeanstalk.com/paging",param);
    }
    private List<JSONObject> getPlacesList(JSONArray jary){
        List<JSONObject> list=new ArrayList<>();
        if(jary!=null && jary.length()!=0){
            for (int i=0; i < jary.length(); i++)
            {
                try{
                    list.add(jary.getJSONObject(i));
                }catch (Exception e){
                }
            }
        }
        return list;
    }

    public void showTable(List<JSONObject> list){
        for(int i=0;i<20;i++) placeID[i]="";
        icons.clear();
        names.clear();
        vicinities.clear();
        placeId.clear();
        if(results!=null && results.size()!=0){
            noResults.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
            prevBtn.setVisibility(View.VISIBLE);
            for (int i=0; i < results.size(); i++)
            {
                try {
                    JSONObject oneObject = results.get(i);
                    String objIcon=oneObject.getString("icon");
                    String objName = oneObject.getString("name");
                    String objVicinity=oneObject.getString("vicinity");
                    String objId=oneObject.getString("place_id");
                    placeID[i]=objId;
                    icons.add(objIcon);
                    names.add(objName);
                    vicinities.add(objVicinity);
                    placeId.add(objId);
                } catch (Exception e) {
                    // Oops
                }
            }

            adapter = new placesRecyclerViewAdapter(this, icons,names,vicinities,placeId);
            adapter.setClickListener(this);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        } else{//no results
            noResults.setVisibility(View.VISIBLE);
            noResults.setGravity(Gravity.CENTER);
            nextBtn.setVisibility(View.GONE);
            prevBtn.setVisibility(View.GONE);
        }

    }
    @Override
    public void onItemClick(View v,int position) {
        // TODO Auto-generated method stub

        int clicked_id = position; // here you get id for clicked TableRow
        pd=new ProgressDialog(this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("Fetching results");
        pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
        pd.setIndeterminate(true);
        pd.show();
//        Toast.makeText(this, clicked_id+"", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(placeResultsActivity.this, DetailsActivity.class);
        intent.putExtra("placeID", placeID[(clicked_id)]);
        intent.putExtra("title",names.get(clicked_id));
        startActivity(intent);

    }
}