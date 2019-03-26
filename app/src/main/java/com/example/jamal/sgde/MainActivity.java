package com.example.jamal.sgde;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //canvas for drawing
    private myCanvas drawNumber;

    //textview to display predicition
    private TextView tv;

    //url for http POST
    String url ="http://192.168.2.107:8080/predict";

    //store dataset
    String dataset = "";
    int iterator = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        drawNumber = (myCanvas) findViewById(R.id.myCanvas);

        tv = (TextView)findViewById(R.id.textView);

        dataset = "";
    }

    /*
     * onClick() method of SETTING button
     * sets the current acitivty to the SettingsActivity
     */
    public void settings(View v){
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    /*
     * onClick method of PREDICT button
     * send http post request to server
     */
    public void predict(View v){
        List<Double> point = getPoint();

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        //tv = (TextView)findViewById(R.id.textView);

        // send point in json format
        final String requestBody = "{\"point\" : "+point+"}";

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String ipAdress = pref.getString("example_text", "...");

        url = "http://" + ipAdress +"/predict";
        Toast.makeText(this, url, Toast.LENGTH_LONG).show();


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                url, null, new Response.Listener<JSONObject>(){
            @Override    public void onResponse(JSONObject response) {
                Integer result = 10;
                try {
                    result = (Integer) response.get("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String resultString = Integer.toString(result);
                String output = "PREDICTION = " + resultString;
                tv.setText(output);
                int color = Color.parseColor("#0065BD");
                tv.setTextColor(color);
                tv.bringToFront();
                tv.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override    public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }){
            @Override    public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override    public byte[] getBody() {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }
        };

        // set timeout of request
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // put request into queue
        queue.add(jsonObjectRequest);
    }

    /*
     * onClick() method of CLEAR button
     * clears the current drawing window
     */
    public void clearBitmap(View v) {
        drawNumber.clearBitmap();
        tv.setVisibility(View.INVISIBLE);
    }

    public void store(View v){
        dataset += getPoint().toString() + "\n";
        iterator++;
    }

    public void save(View v){
        writeToFile(dataset);
        Toast.makeText(this,"SAVED! " + iterator + " points", Toast.LENGTH_LONG).show();
        iterator = 0;
        dataset = "";
    }

    /*
     * uses the current drawing in the canvas and formats it
     * @return formatted List<Double> of drawing, this format fits the SG++ MNIST dataset
     */
    private List<Double> getPoint(){
        Bitmap b = drawNumber.getBitmap();
        Bitmap old = b;
        int i = 448;


        Bitmap unScaled = Bitmap.createScaledBitmap(b, 28,28, true);

        //iterate ROWS NOT COLUMNS
        List<Integer> tmp2 = new ArrayList<Integer>();

        for(int j = 0; j < unScaled.getHeight(); j++){
            for(int k = 0; k < unScaled.getWidth(); k++){
                tmp2.add(unScaled.getPixel(k,j));
            }
        }

        //normalize values to [0,1], 0 = white and 1 = black black = -16777216 white = -1
        List<Double> point2 = new ArrayList<Double>();

        for(int k = 0; k < tmp2.size();k++){
            //make in positive
            int positive = Math.abs(tmp2.get(k));

            //move by -1
            int minus = positive - 1;

            //normalize to double ranging from [0,1]
            double normalize = minus / 16777215.0;

            point2.add(normalize);
        }

        //scale bitmap in 10 pixel steps
        while(i>=28) {
            old = Bitmap.createScaledBitmap(old, i, i, true);
            i -= 10;
        }

        //iterate ROWS NOT COLUMNS
        List<Integer> tmp = new ArrayList<Integer>();

        for(int j = 0; j < old.getHeight(); j++){
            for(int k = 0; k < old.getWidth(); k++){
                tmp.add(old.getPixel(k,j));
            }
        }

        //normalize values to [0,1], 0 = white and 1 = black black = -16777216 white = -1
        List<Double> point = new ArrayList<Double>();

        for(int k = 0; k < tmp.size();k++){
            //make in positive
            int positive = Math.abs(tmp.get(k));

            //move by -1
            int minus = positive - 1;

            //normalize to double ranging from [0,1]
            double normalize = minus / 16777215.0;

            point.add(normalize);
        }

        return point;
    }


    /*
     * This method is used to store Bitmaps into the internal storage.
     * Internal storage can only be accessed through the tablet itself with "ES File Explorer"
     */
    private void storeImage(Bitmap image, String name) {
        File pictureFile = getOutputMediaFile(name);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /*
     * Creates a file in which images or videos can be stored
     */
    private  File getOutputMediaFile(String name){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName=name +"_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void writeToFile(String data)
    {
        // Get the directory for the user's public pictures directory.
        final File path =
                Environment.getExternalStoragePublicDirectory
                        (
                                //Environment.DIRECTORY_PICTURES
                                Environment.DIRECTORY_DCIM + "/YourFolder/"
                        );

        // Make sure the path directory exists.
        if(!path.exists())
        {
            // Make it, if it doesn't exit
            path.mkdirs();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String filename = pref.getString("filename", "...");

        final File file = new File(path, filename+".txt");

        // Save your stream, don't forget to flush() it before closing it.

        try
        {
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
