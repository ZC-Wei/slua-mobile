package ie.deri.slua.SluaMobile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class Login extends Activity {

    public static final String usernameMessage = "com.deri.slua.SluaMobile.USERNAME_MESSAGE";
    private static final String TAG = "LoginTag";
    private static final String API = "signin";
    private EditText usernameText, passwordText;
    private ProgressDialog progressDialog;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameText = (EditText) findViewById(R.id.username);
        passwordText = (EditText) findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(ie.deri.slua.SluaMobile.R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == ie.deri.slua.SluaMobile.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void login(View view) {
        deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        SharedPreferences.Editor editor = getSharedPreferences(MQTTService.TAG, MODE_PRIVATE).edit();
        editor.putString(MQTTService.PREF_DEVICE_ID, deviceId);
        editor.apply();
        final String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        String PATH = getString(ie.deri.slua.SluaMobile.R.string.server_address) + "/"
                + API + "?username=" + username + "&password=" + password + "&deviceId=" + deviceId;
        progressDialog.setMessage("Loading");
        progressDialog.show();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, PATH, null,
                new Response.Listener<JSONObject>() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                String deviceIdMessage = "CrowdApp";
                try {
                    deviceIdMessage = response.getString("deviceId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!Objects.equals(deviceIdMessage, deviceId)) {
                    progressDialog.dismiss();
                    try {
                        Toast.makeText(Login.this, response.getString("error"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    progressDialog.dismiss();
                    MQTTService.actionStart(getApplicationContext());
                    Toast.makeText(Login.this, "Login success.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, TaskList.class);
                    intent.putExtra(usernameMessage, username);
                    startActivity(intent);
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
    }
}
