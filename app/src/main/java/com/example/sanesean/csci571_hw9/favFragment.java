package com.example.sanesean.csci571_hw9;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link favFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link favFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class favFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ReceiveBroadCast receiveBroadCast;
    private placesRecyclerViewAdapter adapter;
    private List<String> icons;
    private List<String> names;
    private List<String> vicinities;
    private List<String> ids;
    private RecyclerView recyclerView;
    private TextView norecord;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public static ProgressDialog pd;
    private OnFragmentInteractionListener mListener;

    public favFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment favFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static favFragment newInstance(String param1, String param2) {
        favFragment fragment = new favFragment();
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
        final View view =  inflater.inflate(R.layout.fragment_fav, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.favView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        norecord=view.findViewById(R.id.noFav);
        icons=new ArrayList<>();
        names=new ArrayList<>();
        vicinities= new ArrayList<>();
        ids=new ArrayList<>();
        if(MainActivity.favorites.entrySet().size()==0){
            norecord.setVisibility(View.VISIBLE);
            norecord.setGravity(Gravity.CENTER);
        }else{
            norecord.setVisibility(View.GONE);
        }
        for(Map.Entry<String,String> entry:MainActivity.favorites.entrySet()){
            try{
                JSONObject obj=new JSONObject(entry.getValue());
                icons.add(obj.getString("icon"));
                names.add(obj.getString("name"));
                vicinities.add(obj.getString("vicinity"));
                ids.add(entry.getKey());
            }catch(Exception e){}
        }

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
        filter.addAction("com.favFragment");
        context.registerReceiver(receiveBroadCast, filter);
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
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
    class ReceiveBroadCast extends BroadcastReceiver implements placesRecyclerViewAdapter.ItemClickListener
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            icons=new ArrayList<>();
            names=new ArrayList<>();
            vicinities= new ArrayList<>();
            ids=new ArrayList<>();
            if(MainActivity.favorites.entrySet().size()==0){
                norecord.setVisibility(View.VISIBLE);
                norecord.setGravity(Gravity.CENTER);
            }else{
                norecord.setVisibility(View.GONE);
            }
            for(Map.Entry<String,String> entry:MainActivity.favorites.entrySet()){
                try{
                    JSONObject obj=new JSONObject(entry.getValue());
                    icons.add(obj.getString("icon"));
                    names.add(obj.getString("name"));
                    vicinities.add(obj.getString("vicinity"));
                    ids.add(entry.getKey());
                }catch(Exception e){}
            }
            adapter = new placesRecyclerViewAdapter(getContext(), icons,names,vicinities,ids);
            adapter.setClickListener(this);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }
        @Override
        public void onItemClick(View v,int position) {
            try{
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra("placeID", ids.get(position));
                intent.putExtra("title",names.get(position));
                pd=new ProgressDialog(getContext());
                pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pd.setMessage("Fetching results");
                pd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FFFFFF")));
                pd.setIndeterminate(true);
                pd.show();
                startActivity(intent);
            }catch(Exception e){
                Log.e("Click JSON PARSE ERROR",e+"");
            }
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
