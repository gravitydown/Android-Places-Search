package com.example.sanesean.csci571_hw9;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link searchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link searchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class searchFragment extends Fragment implements  GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String LOG_TAG = "AutoComplete";
    private AutoCompleteTextView mAutocompleteTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    //components
    TextView keywordVComp;
    TextView customVComp;
    private String keyword="";
    private String category="Default";
    private String distance="10";
    private boolean here=true;
    private String custom="";
    private boolean isValidate=false;

    private FragmentToActivity mCallback;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public searchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment searchFragment.
     */

    public static searchFragment newInstance(String param1, String param2) {
        searchFragment fragment = new searchFragment();
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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_search, container, false);

        final EditText keywordComp;
        keywordComp=(EditText)view.findViewById(R.id.keyword);
        keywordComp.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                keyword=s.toString();
            }
        });

        final Spinner categoryComp;
        categoryComp=(Spinner)view.findViewById(R.id.categorySpinner);
        categoryComp.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                category = getResources().getStringArray(R.array.categoryVals)[pos];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        final EditText distanceComp=(EditText)view.findViewById(R.id.distance);
        distanceComp.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                distance=s.toString();
            }
        });

        final EditText customComp=(EditText)view.findViewById(R.id.custom);
        customComp.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                custom=s.toString();
            }
        });

        final RadioGroup hereComp=(RadioGroup)view.findViewById(R.id.radio);
        hereComp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                RadioButton radbtn=(RadioButton)view.findViewById(i);
                if(radbtn.getText().toString().equals("Current location")){
                    here=true;
                    customComp.setEnabled(false);
                }else{
                    here=false;
                    customComp.setEnabled(true);
                }
            }
        });

        keywordVComp=(TextView)view.findViewById(R.id.keywordValidate);
        customVComp=(TextView)view.findViewById(R.id.customValidate);

        Button searchComp=(Button)view.findViewById(R.id.search);
        searchComp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(validate(keyword)){
                    keywordVComp.setVisibility(View.GONE);
                }else{
                    keywordVComp.setVisibility(View.VISIBLE);
                }
                if(here || (!here && validate(custom))){
                    customVComp.setVisibility(View.GONE);
                }else{
                    customVComp.setVisibility(View.VISIBLE);
                }
                if(validate(keyword) && (here || (!here && validate(custom)))){
                    List<String> data=new ArrayList<>();
                    try{
                        data.add(URLEncoder.encode(keyword,"utf-8"));
                        category=category.toLowerCase();
                        category=category.replace(" ","_");
                        data.add(category);
                        data.add(distance.isEmpty()?"16090":Integer.parseInt(distance)*1609+"");
                        data.add(here+"");
                        data.add(URLEncoder.encode(custom,"utf-8"));
                    }catch (UnsupportedEncodingException e) {
                        throw new AssertionError("UTF-8 is unknown");
                    }
                    //progressbar

                    isValidate=true;
                    sendData(data,isValidate);
                }else{
                    isValidate=false;
                    sendData(new ArrayList<String>(),isValidate);
                }
            }
        });
        Button clear=view.findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                keywordComp.setText("");
                categoryComp.setSelection(0);
                distanceComp.setText(null);
                hereComp.check(R.id.here);
                customComp.setText("");
                customComp.setEnabled(false);
            }
        });
        //autoComplete
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), 0, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextView = (AutoCompleteTextView)view.findViewById(R.id.custom);
        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1,
                null, null);
        mAutocompleteTextView.setAdapter(mPlaceArrayAdapter);

        mAutocompleteTextView.setOnItemClickListener(mAutocompleteClickListener);
        return view;
    }
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            CharSequence attributions = places.getAttributions();
            custom=Html.fromHtml(place.getName() + "")+"";
            Log.e("auto result",Html.fromHtml(place.getName() + "")+"");
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }
    private boolean validate(String s){
        s=s.trim();
        return !s.isEmpty();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (FragmentToActivity) context;
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
    private void sendData(List<String> comm,boolean validate)
    {
        mCallback.communicate(comm,isValidate);

    }
}
