package com.bits.medalt.app;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bits.medalt.app.com.bits.medalt.db.Medicine;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ayush on 15/3/14.
 * @author Ayush Kumar
 */
public class QueryFragment extends Fragment implements View.OnClickListener{

    String TAG = "MedAlt";
    EditText editText;
    ListView lv_result;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        lv_result = (ListView) rootView.findViewById(R.id.lv_results);
        editText = (EditText) rootView.findViewById(R.id.et_query);
        Button button = (Button) rootView.findViewById(R.id.bt_query);
        button.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (editText.getText().toString() != null) {
            new QueryDownloader().execute(editText.getText().toString());
        }
    }

    public class QueryDownloader extends AsyncTask<String, String, String> {

        //private String BASE_URL = "http://ayushkr19.kd.io/query.php";
        //private String BASE_URL = "http://192.168.1.2/query.php";
        //private String BASE_URL = "http://ayushkumar.site90.net/query_webhost.php";
        private String BASE_URL = "http://ayushkumar.site90.net/select_test.php";
        int QueryLength = 0;
        int status = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {
            String data = "";
            try {
                data = downloadQueryData(params[0]);
            }catch (IOException e){
                Log.d(TAG,"QueryDownloader doInBackground IOException");
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity()!=null) {
                Toast.makeText(getActivity(),s,Toast.LENGTH_LONG).show();
            }
            new MedicineParser().execute(s);
            super.onPostExecute(s);
        }

        private String downloadQueryData(String query) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try{
                URL url = new URL(BASE_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setReadTimeout(4000);
                urlConnection.setDoOutput(true);

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("query",query));

                String formatted_query = getQuery(params);
                urlConnection.setFixedLengthStreamingMode(QueryLength);
                urlConnection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");

                OutputStream outputStream = urlConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream,"UTF-8"));
                bufferedWriter.write(formatted_query);

                bufferedWriter.flush();
                bufferedWriter.close();

                urlConnection.connect();
                status = urlConnection.getResponseCode();
                Log.d(TAG,"downloadQueryData Status " + status);
                iStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
                StringBuilder sb  = new StringBuilder();
                String line = "";
                while( ( line = br.readLine())  != null){
                    sb.append(line);
                }
                data = sb.toString();

                Log.d(TAG, "downloadQueryData" + data);
                br.close();
            }catch(Exception e){
                Log.d(TAG, "Exception (downloadQueryData): " + e.toString());
            }finally{
                if (iStream!=null && urlConnection!=null) {
                    iStream.close();
                    urlConnection.disconnect();
                }
            }

            return data;
        }

        private String getQuery(List<NameValuePair> params)
                throws UnsupportedEncodingException {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for (NameValuePair pair : params) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }
            QueryLength = result.toString().getBytes().length;
            return result.toString();
        }

    }

    public class MedicineParser extends AsyncTask<String,String,ArrayList<Medicine>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }



        @Override
        protected ArrayList<Medicine> doInBackground(String... params) {
            JSONArray jsonArray = StringToJSONArray(params[0]);
            ArrayList<Medicine> medicineArrayList = JSONArrayToMedicine(jsonArray);
            return medicineArrayList;
        }

        @Override
        protected void onPostExecute(ArrayList<Medicine> medicines) {
            lv_result.setAdapter(new QueryResultListAdapter(medicines,getActivity()));
            super.onPostExecute(medicines);
        }

        private JSONArray StringToJSONArray(String data){
            JSONArray jsonArray = null;
            try{
                jsonArray = new JSONArray(data);
            }catch (JSONException e){
                Log.d(TAG,"JSONException (StringToJsonArray) :");
            }
            return jsonArray;
        }

        private ArrayList<Medicine> JSONArrayToMedicine(JSONArray jsonArray){
            ArrayList<Medicine> medicineArrayList = new ArrayList<Medicine>();
            if (jsonArray!=null) {
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = jsonArray.getJSONObject(i);
                        Log.d(TAG,i + " " + jsonObject.toString());
                        String trade_name = jsonObject.getString("trade_name");
                        String api = jsonObject.getString("api");
                        String dosage = jsonObject.getString("dosage");
                        String category = jsonObject.getString("category");

                        Medicine medicine = new Medicine(trade_name,api,dosage,category);
                        medicineArrayList.add(medicine);
                    } catch (JSONException e) {
                        Log.d(TAG,"JOSNArrayToMedicine exception");
                    }
                }
            }

            return medicineArrayList;
        }
    }
}