package com.bits.medalt.app;

import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ayush on 15/3/14.
 * @author Ayush Kumar
 */
public class MappFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener{
    public String TAG = "MedAlt";
    private GoogleMap mMap;
    public ArrayList<Places> allPlaces = null;

    public MappFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if(status == ConnectionResult.SUCCESS){ // Google Play Services are available
            //TODO : Remove
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

    /**
     * Check if Map is ready or not. If not ready, try to initialize again
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            ;
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
                //TODO : Remove
                Log.d(TAG,"Map verified");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        //Set up map to show user location
        mMap.setMyLocationEnabled(true);

        setHasOptionsMenu(true);
        mMap.setOnInfoWindowClickListener(this);
    }

    //To show details of a Place(Hospital/Pharmacy) on Marker's InfoWindow Click
    @Override
    public void onInfoWindowClick(Marker marker) {
        //Needed to fetch Reference to pass on to PlaceDetailDownloader AsyncTask.
        Places mPlaces = null;
        for(Places places : allPlaces){
            //TODO : More stricter checking to ensure correct match
            if(places.getName().equalsIgnoreCase(marker.getTitle())){
                mPlaces = places;
                break;
            }
        }
        if (mPlaces != null) {
            //Use Places API to show details of a Place(Hospital/Pharmacy)
            String reference = mPlaces.getReference();
            String url = "https://maps.googleapis.com/maps/api/place/details/json?reference="
                    + reference
                    + "&sensor=false&key=AIzaSyBfG886VyUKsOyBqpeIFGtf45O0nb7rQvs";
            new PlaceDetailDownloader().execute(url);
        }

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
                String url = "https://maps.googleapis.com/maps/api/place/search/json?types=pharmacy|hospital&rankby=distance&location=15.386143,73.869277&sensor=false&key=AIzaSyBfG886VyUKsOyBqpeIFGtf45O0nb7rQvs";
                new PlacesDownloader().execute(url);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Created by ayush on 16/3/14.
     * @author Ayush Kumar
     *
     * Asynctask to download information about nearby Hospitals/Pharmacies & display on the map
     */
    public class PlacesDownloader extends AsyncTask<String,String,ArrayList<Places>> {

        //JSON Keys
        private final String GEOMETRY_KEY = "geometry";
        private final String LOCATION_KEY = "location";
        private final String RESULTS_KEY = "results";
        private final String LAT_KEY = "lat";
        private final String LONG_KEY = "lng";
        private final String NAME_KEY = "name";
        private final String TYPES_KEY = "types";
        private final String REF_KEY = "reference";


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
            // TODO : Check for not null
            return parseJson(data);
        }

        @Override
        protected void onPostExecute(ArrayList<Places> listPlaces) {
            super.onPostExecute(listPlaces);
            //TODO: Is any other less hacky way possible?
            MappFragment.this.allPlaces = listPlaces;

            //Add seperate colored markers for Hospitals & Pharmacies
            //Normal Red markers for Hospitals & Blue markers for Pharmacies
            for(Places places : listPlaces){
                //TODO : Remove Log message
                Log.d(TAG, "Result : " + places.getName() + " " + places.getLat() + "," + places.getLng());

                //TODO : IMP -> Check places.getTypes() for non-existent array values
                if ( (places.getTypes()[0]).equalsIgnoreCase("pharmacy") ) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(places.getLat(),places.getLng()))
                            .title(places.getName())
                            .snippet( (places.getTypes())[0].toUpperCase() + "," + (places.getTypes())[1].toUpperCase() )
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                } else {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(places.getLat(),places.getLng()))
                            .title(places.getName())
                            .snippet((places.getTypes())[0].toUpperCase())
                            );
                }

            }


        }

        /**
         * A method to get the response from URL of Places API
         *
         * @param strUrl The URL from which to get the response
         * @return Response in String format
         * @throws IOException
         */
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

        /**
         * Method to parse the response from Places API URL and return a List of Places to onPostExecute
         * @param data Response from Places API URL
         * @return ArrayList of Places containing the nearby Hospitals/Pharmacies
         */
        private ArrayList<Places> parseJson(String data){
            //Convert the String response to JSONObject
            JSONObject responseJSONObject = StringToJsonObject(data);

            //Get corresponding JSONArray from the JSONObject using "result" key
            //See https://developers.google.com/places/training/basic-place-search for format of the JSON
            JSONArray resultsJSONArray = JsonObjectToJsonArray(responseJSONObject,RESULTS_KEY);

            //Return the ArrayList containing all Places(Hospitals/Pharmacies)
            return JsonArrayToArrayListPlaces(resultsJSONArray);
        }

        /**
         * Method to convert the String response of URL to a JSONObject
         * @param data the response from Places API URL
         * @return JSONObject
         */
        private JSONObject StringToJsonObject(String data){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(data);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (StringToJsonObject)" + e.toString());
            }
            return jsonObject;
        }

        /**
         * Method to convert top-level JSONObject to JSONArray using Key
         * @param jsonObject The top-level JSONObject
         * @param key The key of JSONArray
         * @return The JSONArray corresponding to the Key
         */
        private JSONArray JsonObjectToJsonArray(JSONObject jsonObject,String key){
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray(key);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (JsonObjectToJsonArray) :");
            }
            return jsonArray;
        }

        /**
         * Method to convert individual JSONObjects of JSONArray to Places(Hospitals/Pharmacies)
         * @param jsonObject The JSONObjects of the JSONArray
         * @return Corresponding Places objects
         */
        private Places JsonObjectToPlaces(JSONObject jsonObject){
            String lat = null, lng = null, name = null, reference = null;
            String[] types = null;
            try {
                lat = jsonObject.getJSONObject(GEOMETRY_KEY).getJSONObject(LOCATION_KEY).getString(LAT_KEY);
                lng = jsonObject.getJSONObject(GEOMETRY_KEY).getJSONObject(LOCATION_KEY).getString(LONG_KEY);
                name = jsonObject.getString(NAME_KEY);
                reference = jsonObject.getString(REF_KEY);

                types = getTypes(jsonObject);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (JsonObjectToPlaces) : " + e.toString());
            }
            return new Places(name,Double.parseDouble(lat),Double.parseDouble(lng),types,reference);
        }

        /**
         * Method to get types of a particular Place(Hospital/Pharmacy)
         * @param jsonObject The JSONObjects of the JSONArray
         * @return String array containing the types
         */
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
                typesString = typesString.substring(1,typesString.length() - 1);
                types = typesString.split(",");
                for(int i = 0; i < types.length; i++){
                    types[i] =  types[i].substring(1,types[i].length() - 1);
                }
            }
            return types;
        }

        /**
         * Method to return ArrayList of all the Places(Hospitals/Pharmacies)
         * @param resultsJSONArray The JSONArray containing JSONObjects of all the Places(Hospitals/Pharmacies)
         * @return ArrayList containing all nearby Places(Hospitals/Pharmacies)
         */
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

    /**
     * Created by ayush on 16/3/14.
     * @author Ayush Kumar
     *
     * Asynctask to download information about a specific Hospital/Pharmacy & display in a DialogFragment
     */
    public class PlaceDetailDownloader extends AsyncTask<String,String,HashMap<String,String>>{

        //JSON Keys
        private final String RESULT_KEY = "result";
        private final String ADDRESS_KEY = "formatted_address";
        private final String PHN_KEY = "formatted_phone_number";
        private final String RATING_KEY = "rating";
        private final String NAME_KEY = "name";

        boolean dialogCheck = true;
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Set up an indeterminate ProgressDialog
            progressDialog = ProgressDialog.show(getActivity(),"Loading","Fetching Location Details",true);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    dialogCheck = false;
                }
            });
        }

        @Override
        protected HashMap<String, String> doInBackground(String... params) {
            String data = null;
            try {
                data = downloadPlaceDetails(params[0]);
            } catch (IOException e) {
                Log.d(TAG,"IOException (downloadPlaceDetails) : " + e.toString());
            }
            //Parse the String response and return a HashMap containing the details
            return parseJsonPlaceDetails(data);
        }

        @Override
        protected void onPostExecute(HashMap<String, String> hashMap) {
            super.onPostExecute(hashMap);
            //Cancel the ProgressDialog
            if (dialogCheck) {
                progressDialog.dismiss();

                String name = null;
                if(hashMap.containsKey(NAME_KEY)){
                    name = hashMap.get(NAME_KEY);
                }

                //TODO : Use normal String?
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Address : ");
                if(hashMap.containsKey(ADDRESS_KEY)){
                    stringBuilder.append(hashMap.get(ADDRESS_KEY));
                }else {
                    stringBuilder.append("Not Available");
                }

                stringBuilder.append("\n\nPhone Number : ");
                if(hashMap.containsKey(PHN_KEY)){
                    stringBuilder.append(hashMap.get(PHN_KEY));
                }else {
                    stringBuilder.append("Not Available");
                }

                stringBuilder.append("\n\nRating : ");
                if(hashMap.containsKey(RATING_KEY)){
                    stringBuilder.append(hashMap.get(RATING_KEY));
                }else {
                    stringBuilder.append("Not Available");
                }
                String details = stringBuilder.toString();

                //Show the details in a DialogFragment
                CustomDialogFragment customDialogFragment = CustomDialogFragment.newInstance(name,details);
                customDialogFragment.show(getFragmentManager(),TAG);
            }

        }

        /**
         * Method to download response from Places API URL
         * @param strUrl The URL from which to get the response
         * @return Response from the URL in String format
         * @throws IOException
         */
        private String downloadPlaceDetails(String strUrl) throws IOException {
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

        /**
         * Method to convert the String response of URL to a JSONObject
         * @param data the response from Places API URL
         * @return JSONObject
         */
        private JSONObject StringToJsonObject(String data){
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(data);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (StringToJsonObject)" + e.toString());
            }
            return jsonObject;
        }

        /**
         * Method to parse the Json containing the details of a particular Place(Hospital/Pharmacy)
         * @param data The String response from the URL of Places API
         * @return HashMap containing the details of the particular Place(Hospital/Pharmacy)
         */
        private HashMap<String,String> parseJsonPlaceDetails(String data){

            //Convert the String response to JSONObject
            JSONObject responseJsonObject = StringToJsonObject(data);

            JSONObject resultJsonObject = null;
            try {
                resultJsonObject = responseJsonObject.getJSONObject(RESULT_KEY);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (parseJsonPlaceDetails) " + e.toString());
            }
            //Get details from the JSONObject in a HashMap
            return getPlaceDetailsFromJsonObject(resultJsonObject);
        }

        /**
         * Method to get the details of a Place(Hospital/Pharmacy) in a HashMap
         * @param resultJsonObject JSONObject containing the details of the Place(Hospital/Pharmacy)
         * @return HashMap containing the details in Name-Value pairs
         */
        private HashMap<String,String> getPlaceDetailsFromJsonObject(JSONObject resultJsonObject){
            String address = null, phn = null, rating = null, name = null;
            try {
                name = resultJsonObject.getString(NAME_KEY);
                address = resultJsonObject.getString(ADDRESS_KEY);
                phn = resultJsonObject.getString(PHN_KEY);
                rating = resultJsonObject.getString(RATING_KEY);
            } catch (JSONException e) {
                Log.d(TAG,"JSONException (getPlaceDetailsFromJsonObject) " + e.toString());
            }
            HashMap<String,String> hashMap = new HashMap<String, String>();
            if(name != null){
                hashMap.put(NAME_KEY,name);
            }
            if(address != null){
                hashMap.put(ADDRESS_KEY,address);
            }
            if(phn != null){
                hashMap.put(PHN_KEY,phn);
            }
            if(rating != null){
                hashMap.put(RATING_KEY,rating);
            }
            return hashMap;
        }

    }
}
