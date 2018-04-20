package com.example.sanesean.csci571_hw9;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReviewFragment extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String previousId="";
    private ReceiveBroadCast receiveBroadCast;
    private ReviewRecyclerViewAdpter adapter;
    private RecyclerView recyclerView;
    private List<String> reviewList;
    private List<String> googleBackup;
    private List<String> yelpBackup;
    private GetUrlContentTask http;
    private boolean isGoogle=true;
    private int sortMode=0;
    private String yelpResponse="";
    private TextView noReviews;
    Spinner reviewView;
    Spinner sortView;

    private OnFragmentInteractionListener mListener;

    public ReviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReviewFragment newInstance(String param1, String param2) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_review, container, false);
        reviewList=new ArrayList<>();
        noReviews=view.findViewById(R.id.noReviews);
        reviewView = (Spinner)view.findViewById(R.id.reviewView);
        reviewView.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                isGoogle=pos==0?true:false;
                if(isGoogle){
                    if(googleBackup!=null)
                        reviewList=new ArrayList<>(googleBackup);
                }else{
                    yelpBackup=new ArrayList<>();
                    if(yelpResponse.equals("no_records")){
                        receiveBroadCast.sort(sortMode);
                    }
                    try{
                        JSONObject o=new JSONObject(yelpResponse);
                        JSONArray rs=o.getJSONArray("reviews");
                        for(int i=0;i<rs.length();i++){
                            JSONObject oneReview=rs.getJSONObject(i);
                            oneReview.put("profile_photo_url",oneReview.getJSONObject("user").getString("image_url"));
                            oneReview.put("author_name",oneReview.getJSONObject("user").getString("name"));
                            oneReview.put("author_url",oneReview.getString("url"));
                            oneReview.put("time",oneReview.getString("time_created"));
                            oneReview.remove("user");
                            oneReview.remove("id");
                            oneReview.remove("url");
                            oneReview.remove("time_created");
                            yelpBackup.add(oneReview.toString());
                        }
                        reviewList=new ArrayList<>(yelpBackup);
                    }catch(Exception e){
                        Log.e("yelp parse",e+"");
                    }
                }
                receiveBroadCast.sort(sortMode);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        sortView=(Spinner)view.findViewById(R.id.sortView);
        sortView.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                sortMode=pos;
                receiveBroadCast.sort(pos);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        recyclerView = (RecyclerView)view.findViewById(R.id.reviewCards);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        receiveBroadCast = new ReceiveBroadCast();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ReviewFragment");
        context.registerReceiver(receiveBroadCast, filter);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    class ReceiveBroadCast extends BroadcastReceiver  implements ReviewRecyclerViewAdpter.ItemClickListener
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.hasExtra("placeId")){
                String nowId=intent.getExtras().getString("placeId");
                if(nowId.equals(previousId)){
                    return ;
                }
                previousId=nowId;
            }else{

            }
            if(intent.hasExtra("list")){
                String [] temp=intent.getExtras().getStringArray("list");
                googleBackup=new ArrayList<>();
                for(int i=0;i<temp.length;i++){
                    googleBackup.add(temp[i]);
                }
                reviewList=new ArrayList<>(googleBackup);
                adapter=new ReviewRecyclerViewAdpter(getContext(),reviewList);
                if(reviewList.size()==0){
                    noReviews.setVisibility(View.VISIBLE);
                    noReviews.setGravity(Gravity.CENTER);
                    return;
                }else{
                    noReviews.setVisibility(View.GONE);
                    adapter.setClickListener(this);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);
                }
            }
            if(intent.hasExtra("name")){
               StringBuilder sb=new StringBuilder("name="+intent.getExtras().getString("name"));
               sb.append("&addr="+intent.getExtras().getString("addr"));
               sb.append("&city="+intent.getExtras().getString("city"));
               sb.append("&state="+intent.getExtras().getString("state"));
               sb.append("&country="+intent.getExtras().getString("country"));
               http=new GetUrlContentTask();
               yelpResponse=http.doInBackground("http://yichisheng-hw9.us-east-2.elasticbeanstalk.com/yelp",sb.toString());
            }
        }
        @Override
        public void onItemClick(View v,int position) {
            try{
                String url=new JSONObject(reviewList.get(position)).getString("author_url");
                Uri uri = Uri.parse(url);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }catch(Exception e){
                Log.e("Click JSON PARSE ERROR",e+"");
            }
        }
        public void sort(int mode){
            if(reviewList==null || reviewList.size()==0){
                noReviews.setVisibility(View.VISIBLE);
                noReviews.setGravity(Gravity.CENTER);
                return;
            }
            noReviews.setVisibility(View.GONE);
            if(mode==0){
                if(isGoogle)
                    reviewList=new ArrayList<>(googleBackup);
                else
                    reviewList=new ArrayList<>(yelpBackup);
                adapter=new ReviewRecyclerViewAdpter(getContext(),reviewList);
                adapter.setClickListener(this);
                adapter.notifyDataSetChanged();
                recyclerView.setAdapter(adapter);
                return;
            }else if(mode==1){
                Collections.sort(reviewList, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        try{
                            JSONObject o1=new JSONObject(s);
                            JSONObject o2=new JSONObject(t1);
                            return Integer.parseInt(o2.getString("rating"))-Integer.parseInt(o1.getString("rating"));
                        }catch(Exception e){}
                        return 0;
                    }
                });
            }else if(mode==2){
                Collections.sort(reviewList, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        try{
                            JSONObject o1=new JSONObject(s);
                            JSONObject o2=new JSONObject(t1);
                            return Integer.parseInt(o1.getString("rating"))-Integer.parseInt(o2.getString("rating"));
                        }catch(Exception e){}
                        return 0;
                    }
                });
            }else if(mode==3){
                Collections.sort(reviewList, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        try{
                            JSONObject o1=new JSONObject(s);
                            JSONObject o2=new JSONObject(t1);
                            return o2.getString("time").compareTo(o1.getString("time"));
                        }catch(Exception e){}
                        return 0;
                    }
                });
            }else if(mode==4){
                Collections.sort(reviewList, new Comparator<String>() {
                    @Override
                    public int compare(String s, String t1) {
                        try{
                            JSONObject o1=new JSONObject(s);
                            JSONObject o2=new JSONObject(t1);
                            return o1.getString("time").compareTo(o2.getString("time"));
                        }catch(Exception e){}
                        return 0;
                    }
                });
            }
            adapter.notifyDataSetChanged();
            adapter=new ReviewRecyclerViewAdpter(getContext(),reviewList);
            adapter.setClickListener(this);
            recyclerView.setAdapter(adapter);
        }
    }
    @Override
    public void onDestroyView() {
        try{
            getActivity().unregisterReceiver(receiveBroadCast);
        }catch(Exception e){
        }
        super.onDestroyView();
    }

}
