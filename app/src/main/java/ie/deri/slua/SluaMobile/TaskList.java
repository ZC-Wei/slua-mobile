package ie.deri.slua.SluaMobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TaskList extends ActionBarActivity {

    public static final String taskObjectMessage = "com.deri.slua.SluaMobile.TASK_OBJECT_MESSAGE";
    public static final String usernameMessage = "com.deri.slua.SluaMobile.USERNAME_MESSAGE";
    private static final String TAG = "TaskListTag";
    private static final String API = "assigns";
    private ArrayList<Map<String, Object>> mTaskLists = new ArrayList<>();
    private SimpleAdapter adapter;
    private Map<String, Object> taskItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        final String username = intent.getStringExtra(usernameMessage);
        String PATH = getString(ie.deri.slua.SluaMobile.R.string.server_address) + "/" + API + "?username=" + username;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(PATH,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject jsonObject = response.getJSONObject(i);
                                taskItem = new HashMap<>();
                                taskItem.put("img", R.drawable.ic_place_white_48dp);
                                taskItem.put("title", jsonObject.getString("title"));
                                taskItem.put("location", jsonObject.getString("location"));
                                taskItem.put("object", jsonObject);
                                mTaskLists.add(taskItem);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(TAG, volleyError.getMessage(), volleyError);
                    }
                }
        );

        VolleyController.getInstance().addToRequsetQueue(jsonArrayRequest);

        setContentView(R.layout.activity_task_list);
        ListView mListView = (ListView) findViewById(R.id.task_list);
        adapter = new SimpleAdapter(this, mTaskLists, R.layout.task_list,
                new String[]{"img", "title", "location"},
                new int[]{ie.deri.slua.SluaMobile.R.id.task_img, R.id.task_title, R.id.task_location});
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String taskObject = mTaskLists.get(i).get("object").toString();
                //Toast.makeText(TaskList.this, taskObject, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), TaskDetail.class);
                intent.putExtra(usernameMessage, username);
                intent.putExtra(taskObjectMessage, taskObject);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
