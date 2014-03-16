package com.bits.medalt.app;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ayush on 15/3/14.
 * @author Ayush Kumar
 */
public class MappFragment extends Fragment {
    Context mContext;
    public String TAG = "MedAlt";
    private GoogleMap mMap;

    public MappFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if(status == ConnectionResult.SUCCESS){ // Google Play Services are available
            Log.d(TAG,"Gservices available");
            View rootView = inflater.inflate(R.layout.map_layout,container,false);
            return rootView;

        }else{
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), requestCode);
            dialog.show();
            View rootView = inflater.inflate(R.layout.gserv_unavailable,container,false);
            return rootView;
        }

    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            ;
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                Log.d(TAG,"Map verified");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.mapp_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Created by ayush on 16/3/14.
     * @author Ayush Kumar
     */
    public class PlacesDownloader extends AsyncTask<String,String,ArrayList<Places>> {

        private final String GEOMETRY_KEY = "geometry";
        private final String LOCATION_KEY = "location";
        private final String RESULTS_KEY = "results";
        private final String LAT_KEY = "lat";
        private final String LONG_KEY = "lng";
        private final String NAME_KEY = "name";
        private final String TYPES_KEY = "types";


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Places> doInBackground(String... params) {
            String data = null;
            try {
                data = downloadPlaces(params[0]);
            } catch (Exception e) {
                Log.d(TAG,"PlaceDownloader's doInBackground : " + e.toString());
            }

            return parseJson(data);
        }

        @Override
        protected void onPostExecute(ArrayList<Places> listPlaces) {
            super.onPostExecute(listPlaces);

            for(Places places : listPlaces){
                Log.d(TAG, "Result : " + places.getName() + " " + places.getLat() + "," + places.getLng());
            }
        }

        private String downloadPlaces(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(strUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder sb  = new StringBuilder();
                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }
                data = sb.toString();
                br.close();
            }catch(Exception e){
                Log.d(TAG, "Exception (downloadPlaces): " + e.toString());
            }finally{
                if (iStream!=null && urlConnection!=null) {
                    iStream.close();
                    urlConnection.disconnect();
                }
            }

            return data;
        }

        private ArrayList<Places> parseJson(String data){
            JSONObject responseJSONObject = StringToJsonObject(data);
            JSONArray resultsJSONArray = JsonObjectToJsonArray(responseJSONObject,RESULTS_KEY);
            return JsonArrayToArrayListPlaces(resultsJSONArray);
        }

        private JSONObject StringToJsonObject(String data){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(data);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (StringToJsonObject)" + e.toString());
            }
            return jsonObject;
        }

        private JSONArray JsonObjectToJsonArray(JSONObject jsonObject,String key){
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray(key);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (JsonObjectToJsonArray) :");
            }
            return jsonArray;
        }

        private Places JsonObjectToPlaces(JSONObject jsonObject){
            String lat = null, lng = null, name = null;
            String[] types = null;
            try {
                lat = jsonObject.getJSONObject(GEOMETRY_KEY).getJSONObject(LOCATION_KEY).getString(LAT_KEY);
                lng = jsonObject.getJSONObject(GEOMETRY_KEY).getJSONObject(LOCATION_KEY).getString(LONG_KEY);
                name = jsonObject.getString(NAME_KEY);
                types = getTypes(jsonObject);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (JsonObjectToPlaces) : " + e.toString());
            }
            return new Places(name,Double.parseDouble(lat),Double.parseDouble(lng),types);
        }

        private String[] getTypes(JSONObject jsonObject){
            String[] types = null;
            JSONArray typesJsonArray = null;
            try {
                typesJsonArray = jsonObject.getJSONArray(TYPES_KEY);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (getTypes) : " + e.toString());
            }
            if (typesJsonArray != null) {
                String typesString = typesJsonArray.toString();
                Log.d(TAG,typesString);
                typesString = typesString.substring(1,typesString.length() - 1);
                Log.d(TAG,typesString);
                types = typesString.split(",");
            }
            return types;
        }

        private ArrayList<Places> JsonArrayToArrayListPlaces(JSONArray resultsJSONArray){
            ArrayList<Places> allPlaces = new ArrayList<Places>();
            int resultsJsonArrayLength = resultsJSONArray.length();
            for(int i = 0; i < resultsJsonArrayLength; i++ ){
                JSONObject placesJsonObject = null;
                try {
                    placesJsonObject = resultsJSONArray.getJSONObject(i);
                    Places places = JsonObjectToPlaces(placesJsonObject);
                    allPlaces.add(places);
                } catch (JSONException e) {
                    Log.d(TAG,"JSONException (JsonArrayToArrayListPlaces) : " + e.toString());
                }
            }
            return allPlaces;
        }
    }
}
