package com.example.sanesean.csci571_hw9;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ReceiveBroadCast receiveBroadCast;
    private static final String LOG_TAG = "AutoComplete";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private GoogleMap mMap;
    private String lat="";
    private String lng="";
    private String title="";
    private String stlat="";
    private String stlng="";
    private String from="";
    private String mode="";
    private Spinner modeView;
    private AutoCompleteTextView mAutocompleteTextView;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private boolean spinnerFlag=false;
    private Polyline polylineToAdd;
    private GetUrlContentTask http;
    List<LatLng> lines;
    private OnFragmentInteractionListener mListener;
    private double maxlat=-9999999,minlng=99999999;
    private double maxlng=-9999999,minlat=99999999;
    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment)this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        modeView=(Spinner)view.findViewById(R.id.travelModeView);
        modeView.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {
                mode = getResources().getStringArray(R.array.travelModeVals)[pos];
                if(spinnerFlag){
                    getDirection();
                }
                spinnerFlag=true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), 0, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextView = (AutoCompleteTextView)view.findViewById(R.id.mapFromView);
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
            from=Html.fromHtml(place.getName() + "")+"";
            Log.e("auto result",Html.fromHtml(place.getName() + "")+"");
            getDirection();
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
        filter.addAction("com.MapFragment");
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
    class ReceiveBroadCast extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if(intent.hasExtra("lat")){
                lat=intent.getExtras().getString("lat");
            }
            if(intent.hasExtra("lng")){
                lng=intent.getExtras().getString("lng");
            }
            if(intent.hasExtra("title")){
                title=intent.getExtras().getString("title");
            }
            LatLng place = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng) );
            maxlat=-999999;
            maxlng=-999999;
            minlat=999999;
            minlng=-999999;
            mMap.clear();
            mAutocompleteTextView.setText("");
            mMap.addMarker(new MarkerOptions()
                    .position(place)
                    .title(title)).showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 12));
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
    public void getDirection(){
        getActivity().runOnUiThread(new Runnable(){
            public void run(){
                mMap.clear();
                LatLng place = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng) );
                mMap.addMarker(new MarkerOptions().position(place).title(title));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 12));
            }
        });
        maxlat=-999999;
        maxlng=-999999;
        minlat=999999;
        minlng=999999;
        String test=from.replaceAll("\\s+","");
        if(!test.isEmpty()){
            String newfrom=from.replaceAll("\\s","+");
            String params="origin="+newfrom+"&dest="+lat+","+lng+"&mode="+mode.toLowerCase();
            http=new GetUrlContentTask();
            String response=http.doInBackground("http://yichisheng-hw9.us-east-2.elasticbeanstalk.com/directions",params);
            try{
                URLEncoder.encode(response, "utf-8");
                JSONObject result = new JSONObject(response);
                JSONArray routes = result.getJSONArray("routes");

                JSONArray steps = routes.getJSONObject(0).getJSONArray("legs")
                        .getJSONObject(0).getJSONArray("steps");
                JSONObject startPoint=steps.getJSONObject(0).getJSONObject("start_location");
                stlat=startPoint.getString("lat");
                stlng=startPoint.getString("lng");
                lines = new ArrayList<LatLng>();

                for(int i=0; i < steps.length(); i++) {
                    String polyline = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                    maxlat=Math.max(maxlat,Double.parseDouble(steps.getJSONObject(i).getJSONObject("end_location").getString("lat")));
                    minlat=Math.min(minlat,Double.parseDouble(steps.getJSONObject(i).getJSONObject("end_location").getString("lat")));
                    maxlng=Math.max(maxlng,Double.parseDouble(steps.getJSONObject(i).getJSONObject("end_location").getString("lng")));
                    minlng=Math.min(minlng,Double.parseDouble(steps.getJSONObject(i).getJSONObject("end_location").getString("lng")));
                    for(LatLng p : decodePolyline(polyline)) {
                        lines.add(p);
                    }
                }
                getActivity().runOnUiThread(new Runnable(){
                    public void run(){
                        LatLng origin = new LatLng(Double.parseDouble(stlat),Double.parseDouble(stlng) );
                        mMap.addMarker(new MarkerOptions().position(origin).title(from));
                        polylineToAdd = mMap.addPolyline(new PolylineOptions().addAll(lines).width(8).color(Color.BLUE));
                        //zoom

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(new LatLng(maxlat,maxlng));
                        builder.include(new LatLng(minlat,minlng));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 20));


                    }
                });

            }catch(Exception e){
                Log.e("JSON ERROR:",e+"");
            }
        }
    }
    private List<LatLng> decodePolyline(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }
}
