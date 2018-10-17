package bekaku.android.forum;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.GlideUtil;
import bekaku.android.forum.util.Utility;

public class CallApiActivity extends AppCompatActivity {

    private ImageView userImg;
    private TextView usernameTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_api);

        userImg = findViewById(R.id.user_img);
        usernameTxt = findViewById(R.id.username_text);

        new MyCallApi().execute();
        new MyCallArray().execute();

    }
    private class MyCallArray extends AsyncTask<Void, Void, String>{

        @Override
        protected String doInBackground(Void... voids) {
            HashMap<String, String> param = new HashMap<>();
            param.put("page", "1");
            return ApiUtil
                    .okHttpGet(
                            "http://192.168.34.5/grandats_project/forum/api/user/read.php",
                            param
                    );

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            if(!Utility.isEmpty(s)){
                try {
                    JSONObject json = new JSONObject(s);
                    JSONArray jsonArray = json.getJSONArray("data");

                    if(jsonArray.length()>0){
                        for (int i =0; i<jsonArray.length();i++){
                            JSONObject object = jsonArray.getJSONObject(i);
                            String username = object.getString("username");
                            String userPictureUrl = object.getString("picture");
                            int userId = object.getInt("id");

                            Log.e("TEST ARRAy", "username=>"+username);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }
    private class MyCallApi extends AsyncTask<Void, Void, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... voids) {

            HashMap<String, String> paramiters = new HashMap<>();
            paramiters.put("_user_id", "25");

            String serverResponse = ApiUtil
                    .okHttpGet(
                            "http://192.168.34.5/grandats_project/forum/api/user/readone.php",
                            paramiters
                            );

            return serverResponse;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);

            if(!Utility.isEmpty(s)){
                try {
                    JSONObject json = new JSONObject(s);
                    JSONObject data = json.getJSONObject("data");

                    String username = data.getString("username");
                    String userPictureUrl = data.getString("picture");
                    int userId = data.getInt("id");

                    usernameTxt.setText(username);
                    Glide.with(CallApiActivity.this)
                            .load(userPictureUrl)
                            .into(userImg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
