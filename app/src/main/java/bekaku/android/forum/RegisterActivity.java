package bekaku.android.forum;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bekaku.android.forum.adapter.AvatarAdapter;
import bekaku.android.forum.databinding.ActivityRegisterBinding;
import bekaku.android.forum.model.DefaultAvatar;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.RecyclerTouchListener;
import bekaku.android.forum.util.SqliteHelper;
import bekaku.android.forum.util.Utility;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityRegisterBinding binding;
    private ForumSetting forumSetting;

    private List<DefaultAvatar> avatarList = new ArrayList<>();
    private AvatarAdapter avatarAdapter;

    private String username;
    private String pwd;
    private String email;
    private String picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
        SqliteHelper sqliteHelper = new SqliteHelper(RegisterActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();


        binding.registerBtn.setOnClickListener(this);

        avatarAdapter = new AvatarAdapter(RegisterActivity.this, avatarList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(RegisterActivity.this, LinearLayout.HORIZONTAL, false);

        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(avatarAdapter);

        //get defaut avartar list from server
        new InitDataTask(RegisterActivity.this).execute();

        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                DefaultAvatar avatarSeletec = avatarList.get(position);

                //set false to another img
                for (DefaultAvatar defaultAvatar : avatarList) {
                    defaultAvatar.setSelected(false);
                }

                avatarSeletec.setSelected(true);
                avatarAdapter.notifyDataSetChanged();
//                Log.e("getImgApiUrl", "" + avatarSeletec.getImgApiUrl());
            }
        }));

    }
    //get default avatar from server to listview
    private static class InitDataTask extends AsyncTask<Void, Void, String> {

        private WeakReference<RegisterActivity> weakReference;

        InitDataTask(RegisterActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private RegisterActivity getContext() {
            return weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getContext().binding.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            return ApiUtil.okHttpGet(getContext().forumSetting.getApiMainUrl() + "/util/avatarlist.php", new HashMap<String, String>());
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            getContext().prepareData(s);
        }
    }


    private void prepareData(String response) {
        try {
            JSONObject jo = new JSONObject(response);
            JSONArray jsonArray = jo.getJSONArray("data");

            DefaultAvatar avatar;
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject data = jsonArray.getJSONObject(i);
                    avatar = new DefaultAvatar(data.getString("img_api_url"), data.getString("img_api_folder"));
                    avatarList.add(avatar);
                    avatarAdapter.notifyDataSetChanged();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        binding.progress.setVisibility(View.GONE);

    }


    private void registerProcess(){

        if(isValidateForm()){
            System.out.println("OKAY");
            new RegisterTask(RegisterActivity.this).execute();
        }else{
            System.out.println("NOT OKAY");
        }

    }
    private boolean isValidateForm(){

        boolean isOk = true;
        username = binding.username.getText().toString();
        pwd = binding.password.getText().toString();
        String rePwd = binding.rePassword.getText().toString();
        email = binding.email.getText().toString();

        if(Utility.isEmpty(username) || Utility.isEmpty(pwd) || Utility.isEmpty(rePwd) || Utility.isEmpty(email)){
            isOk = false;
            Toast.makeText(this, getResources().getString(R.string.err_empty_form), Toast.LENGTH_SHORT).show();
        }else if(!pwd.equalsIgnoreCase(rePwd)){
            isOk = false;
            Toast.makeText(this, getResources().getString(R.string.err_pwd_confirm), Toast.LENGTH_SHORT).show();
        }

        if(isOk){
            for (DefaultAvatar defaultAvatar : avatarList){
                if(defaultAvatar.isSelected()){
                    picture = defaultAvatar.getImgApiFolder();
                    break;
                }
            }
        }

        return isOk;
    }

    private static class RegisterTask extends AsyncTask<Void, Void, String> {

        private WeakReference<RegisterActivity> weakReference;

        RegisterTask(RegisterActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private RegisterActivity getContext() {
            return weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getContext().binding.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {

            RegisterActivity activity = this.getContext();

            HashMap<String, String> params = new HashMap<>();
            params.put("_username",activity.username);
            params.put("_pwd",Utility.hashSHA512(activity.pwd));
            params.put("_picture",activity.picture);
            params.put("_email",activity.email);

            return ApiUtil.okHttpPost(activity.forumSetting.getApiMainUrl() + "/user/create.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            getContext().finishRegister(s);
        }
    }
    private void finishRegister(String response){
        System.out.println(response);

        try {
            JSONObject data = new JSONObject(response);

            JSONObject serverStatus = data.getJSONObject("server_status");
            if(serverStatus!=null){

                int statusCode = serverStatus.getInt("status");
                String statusMsg = serverStatus.getString("message");
                Toast.makeText(this, statusMsg, Toast.LENGTH_LONG).show();
                binding.progress.setVisibility(View.GONE);

                if(statusCode==1){
                    finish();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.registerBtn:
                registerProcess();
                break;
        }

    }
}
