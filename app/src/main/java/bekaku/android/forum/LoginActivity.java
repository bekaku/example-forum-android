package bekaku.android.forum;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import bekaku.android.forum.databinding.ActivityLoginBinding;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.SqliteHelper;
import bekaku.android.forum.util.Utility;


public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLoginBinding binding;
    private ForumSetting forumSetting;

    private String username;
    private String password;
    private SqliteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.registerButton.setOnClickListener(this);
        binding.signInButton.setOnClickListener(this);

        sqliteHelper = new SqliteHelper(LoginActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();
    }

    private void loginProcess(){

        username = binding.loginName.getText().toString();
        password = binding.password.getText().toString();
        if(Utility.isEmpty(username) || Utility.isEmpty(password)){
            Toast.makeText(this, getResources().getString(R.string.err_empty_form), Toast.LENGTH_SHORT).show();
        }else{
            new LoginTask(LoginActivity.this).execute();
        }

    }

    private static class LoginTask extends AsyncTask<Void, Void, String> {

        private WeakReference<LoginActivity> weakReference;

        LoginTask(LoginActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private LoginActivity getContext() {
            return weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getContext().binding.loginProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {

            LoginActivity activity = this.getContext();

            HashMap<String, String> params = new HashMap<>();
            params.put("_username",activity.username);
            params.put("_pwd",Utility.hashSHA512(activity.password));

            return ApiUtil.okHttpPost(activity.forumSetting.getApiMainUrl() + "/user/authen.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            getContext().finishLogin(s);
        }
    }
    private void finishLogin(String response){
        System.out.println(response);
        try {
            JSONObject data = new JSONObject(response);
            JSONObject jsonServerStatus = data.getJSONObject("server_status");
            JSONObject jsonUserData = data.getJSONObject("data");
            if(jsonServerStatus!=null){

                int statusCode = jsonServerStatus.getInt("status");
                String statusMsg = jsonServerStatus.getString("message");

                Toast.makeText(this, statusMsg, Toast.LENGTH_LONG).show();
                binding.loginProgress.setVisibility(View.GONE);

                if(statusCode==1){
                    //update user data in device setting and go to main activity if logined success
                    if(jsonUserData!=null){

                        forumSetting.setUserId(jsonUserData.getInt("id"));
                        forumSetting.setUsername(jsonUserData.getString("username"));
                        forumSetting.setPicture(jsonUserData.getString("picture"));
                        forumSetting.setEmail(jsonUserData.getString("email"));
                        forumSetting.setCreated(jsonUserData.getString("created"));

                        int effectrow = sqliteHelper.updateForumSetting(forumSetting);
                        if(effectrow>0){
                            Intent iMain = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(iMain);
                            finish();
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.register_button :
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.sign_in_button :
                loginProcess();
                break;
        }

    }
}
