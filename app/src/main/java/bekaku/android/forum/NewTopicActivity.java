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
import bekaku.android.forum.databinding.ActivityNewTopicBinding;
import bekaku.android.forum.model.DefaultAvatar;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.RecyclerTouchListener;
import bekaku.android.forum.util.SqliteHelper;
import bekaku.android.forum.util.Utility;

public class NewTopicActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityNewTopicBinding binding;
    private ForumSetting forumSetting;
    private String subject;
    private String content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_topic);
        SqliteHelper sqliteHelper = new SqliteHelper(NewTopicActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();
        binding.submitPost.setOnClickListener(this);
    }


    private void submitProcess() {
        if (isValidateForm()) {
            new SubmitTask(NewTopicActivity.this).execute();
        }
    }

    private boolean isValidateForm() {

        boolean isOk = true;
        subject = binding.subject.getText().toString();
        content = binding.content.getText().toString();

        if (Utility.isEmpty(subject) || Utility.isEmpty(content)) {
            isOk = false;
            Toast.makeText(this, getResources().getString(R.string.err_empty_form), Toast.LENGTH_SHORT).show();
        }
        return isOk;
    }

    private static class SubmitTask extends AsyncTask<Void, Void, String> {

        private WeakReference<NewTopicActivity> weakReference;

        SubmitTask(NewTopicActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private NewTopicActivity getContext() {
            return weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getContext().binding.progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {

            NewTopicActivity activity = this.getContext();

            HashMap<String, String> params = new HashMap<>();
            params.put("_subject", activity.subject);
            params.put("_content", activity.content);
            params.put("_user_account_id", String.valueOf(activity.forumSetting.getUserId()));

            return ApiUtil.okHttpPost(activity.forumSetting.getApiMainUrl() + "/thread/create.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            getContext().finishRegister(s);
        }
    }

    private void finishRegister(String response) {
        binding.progress.setVisibility(View.GONE);
        try {
            JSONObject data = new JSONObject(response);
            JSONObject serverStatus = data.getJSONObject("server_status");
            int statusCode = serverStatus.getInt("status");
            String statusMsg = serverStatus.getString("message");
            Toast.makeText(this, statusMsg, Toast.LENGTH_LONG).show();
            if (statusCode == 1) {
                finish();
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
            case R.id.submitPost:
                submitProcess();
                break;
        }

    }
}
