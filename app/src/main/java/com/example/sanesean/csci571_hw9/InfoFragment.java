package com.example.sanesean.csci571_hw9;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FragmentToActivity mCallback;
    private OnFragmentInteractionListener mListener;
    private ReceiveBroadCast receiveBroadCast;
    private RecyclerView recyclerView;
    private TextView tv;
    TextView addr_label;
    TextView addr_content;
    TextView phone_label;
    TextView phone_content;
    TextView price_label;
    TextView price_content;
    TextView rating_label;
    RatingBar rating_content;
    TextView page_label;
    TextView page_content;
    TextView website_label;
    TextView website_content;
    public InfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InfoFragment newInstance(String param1, String param2) {
        InfoFragment fragment = new InfoFragment();
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
        // Inflate the layout for this fragment

        final View view =  inflater.inflate(R.layout.fragment_info, container, false);
        addr_label=view.findViewById(R.id.addr_label);
        addr_content=view.findViewById(R.id.addr_content);
        phone_label=view.findViewById(R.id.phone_label);
        phone_content=view.findViewById(R.id.phone_content);
        price_label=view.findViewById(R.id.price_label);
        price_content=view.findViewById(R.id.price_content);
        rating_label=view.findViewById(R.id.rating_label);
        rating_content=view.findViewById(R.id.rating_content);
        page_label=view.findViewById(R.id.page_label);
        page_content=view.findViewById(R.id.page_content);
        website_label=view.findViewById(R.id.website_label);
        website_content=view.findViewById(R.id.website_content);
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
        filter.addAction("com.InfoFragment");
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
    public interface OnFragmentInteractionListener  {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class ReceiveBroadCast extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.hasExtra("address")){
                String addr = intent.getExtras().getString("address");
                addr_label.setVisibility(View.VISIBLE);
                addr_content.setVisibility(View.VISIBLE);
                addr_content.setText(addr);

            }
            if(intent.hasExtra("phone")){
                String phone = intent.getExtras().getString("phone");
                phone_label.setVisibility(View.VISIBLE);
                phone_content.setVisibility(View.VISIBLE);
                phone_content.setText(phone);
            }
            if(intent.hasExtra("price")){
                String price = intent.getExtras().getString("price");
                price_label.setVisibility(View.VISIBLE);
                price_label.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                price_content.setVisibility(View.VISIBLE);
                int p=Integer.parseInt(price);
                String p$="";
                for(int i=0;i<p;i++){
                    p$+="$";
                }
                price_content.setText(p$);
            }
            if(intent.hasExtra("rating")){
                String rating = intent.getExtras().getString("rating");
                rating_label.setVisibility(View.VISIBLE);
                rating_content.setVisibility(View.VISIBLE);
                float r=Float.parseFloat(rating);
                rating_content.setRating(r);
            }
            if(intent.hasExtra("google")){
                String google = intent.getExtras().getString("google");
                page_label.setVisibility(View.VISIBLE);
                page_content.setVisibility(View.VISIBLE);
                page_content.setText(google);
            }
            if(intent.hasExtra("website")){
                String website = intent.getExtras().getString("website");
                website_label.setVisibility(View.VISIBLE);
                website_content.setVisibility(View.VISIBLE);
                website_content.setText(website);
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
