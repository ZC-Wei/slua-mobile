package ie.deri.slua.SluaMobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskDetail extends ActionBarActivity {
    static final int TAKE_PHOTO_REQUEST_CODE = 1;
    private static final String TAG = "TaskDetailTag";
    private ImageView mapImageView, photoImageView;
    private TextView titleTextView, descriptionTextView;
    private String fileName, taskImageAddress;
    private String API_upload = "upload";
    private String API_submit = "submit";
    private String API_location = "location";
    public static final String usernameMessage = "com.deri.slua.SluaMobile.USERNAME_MESSAGE";

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLastLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        /*
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = mLocationManager.getBestProvider(criteria, true);
        mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
        */
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        //mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        mapImageView = (ImageView) findViewById(R.id.task_image);
        photoImageView = (ImageView) findViewById(R.id.photo_image);
        titleTextView = (TextView) findViewById(R.id.task_title);
        descriptionTextView = (TextView) findViewById(R.id.task_description);
        JSONObject jsonObject;
        Intent intent = getIntent();
        try {
            jsonObject = new JSONObject(intent.getStringExtra(TaskList.taskObjectMessage));
            titleTextView.setText(jsonObject.getString("title"));
            descriptionTextView.setText(jsonObject.getString("description"));
            fileName = "task" + jsonObject.getString("id") + ".jpg";
            String url = jsonObject.getString("photo");
            String PATH = getString(ie.deri.slua.SluaMobile.R.string.server_address) + url;
            ImageLoader imageLoader = VolleyController.getInstance().getImageLoader();
            imageLoader.get(PATH, new ImageLoader.ImageListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Image Load Error: " + error.getMessage());
                }

                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                    if (response.getBitmap() != null) {
                        // load image into image view
                        mapImageView.setImageBitmap(response.getBitmap());
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_upload) {
            final Intent intent = getIntent();
            try {
                JSONObject jsonObject = new JSONObject(intent.getStringExtra(TaskList.taskObjectMessage));
                final String username = intent.getStringExtra(TaskList.usernameMessage);
                String PATH = getString(R.string.server_address) + "/" + API_submit +
                        "?username=" + username +
                        "&taskId=" + jsonObject.getString("id") +
                        "&response=" + taskImageAddress;
                //Log.e(TAG, PATH);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, PATH, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String status = response.getString("status");
                                    if (status == "true") {
                                        Toast.makeText(TaskDetail.this, "Submit success.", Toast.LENGTH_SHORT).show();
                                        Intent backIntent = new Intent(getApplicationContext(), TaskList.class);
                                        backIntent.putExtra(usernameMessage, username);
                                        startActivity(backIntent);
                                        finish();
                                    } else {
                                        Toast.makeText(TaskDetail.this, "Submit failed.", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e(TAG, volleyError.getMessage(), volleyError);
                            }
                        });
                VolleyController.getInstance().addToRequsetQueue(jsonObjectRequest);
            } catch (JSONException e) {
            e.printStackTrace();
            }
            return true;
        } else if (id == R.id.action_capture) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, TAKE_PHOTO_REQUEST_CODE);
            return true;
        } else {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, mLocationListener);
            mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (mLastLocation != null) {
                String mLatitude = String.valueOf(mLastLocation.getLatitude());
                String mLongitude = String.valueOf(mLastLocation.getLongitude());
                //Toast.makeText(this, mLatitude + "," + mLongitude, Toast.LENGTH_LONG).show();
                final Intent intent = getIntent();
                final String username = intent.getStringExtra(TaskList.usernameMessage);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                String timestamp = dateFormat.format(new Date());
                String PATH = getString(R.string.server_address) + "/" + API_location +
                        "?username=" + username +
                        "&latitude=" + mLatitude +
                        "&longitude=" + mLongitude +
                        "&timestamp=" + URLEncoder.encode(timestamp);
                Log.d(TAG, PATH);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(PATH, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    String checkInId = response.getString("checkinId");
                                    if (checkInId != null){
                                        Toast.makeText(TaskDetail.this, checkInId, Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(TaskDetail.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        }
                );
                VolleyController.getInstance().addToRequsetQueue(jsonObjectRequest);
            } else {
                Toast.makeText(this, "No location detected", Toast.LENGTH_LONG).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PHOTO_REQUEST_CODE:
                if (data.getData() != null) {
                    Bundle extras = data.getExtras();
                    Bitmap bmp = (Bitmap) extras.get("data");
                    photoImageView.setImageBitmap(bmp);
                    String PATH = getString(ie.deri.slua.SluaMobile.R.string.server_address) + "/" + API_upload;
                    try {
                        File imageFile = new File(getApplicationContext().getCacheDir(), fileName);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        if (bmp != null) {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 0, bos);
                        }
                        byte[] bitmapData = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(imageFile);
                        fos.write(bitmapData);
                        fos.flush();
                        fos.close();
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost(PATH);
                        InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(imageFile), -1);
                        reqEntity.setContentType("image/jpeg");
                        reqEntity.setChunked(true); // Send in multiple parts if needed
                        httppost.setEntity(reqEntity);
                        HttpResponse response = httpclient.execute(httppost);
                        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
                        taskImageAddress = jsonObject.getString("response");
                        Toast.makeText(TaskDetail.this, "Uploaded " + taskImageAddress, Toast.LENGTH_SHORT).show();
                    } catch (IOException | JSONException ie) {
                        ie.printStackTrace();
                    }
                }
                break;
        }
    }
}
