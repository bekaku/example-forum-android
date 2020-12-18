package bekaku.android.forum;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import bekaku.android.forum.databinding.ActivityNewTopicBinding;
import bekaku.android.forum.dialog.LoadingDialog;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.model.ThreadModel;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.SqliteHelper;
import bekaku.android.forum.util.Utility;

public class EditTopicActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityNewTopicBinding binding;
    private ForumSetting forumSetting;
    private String subject;
    private String content;
    private int threadId;
    public LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_register);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_topic);
        SqliteHelper sqliteHelper = new SqliteHelper(EditTopicActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();
        binding.submitPost.setOnClickListener(this);

        loadingDialog = new LoadingDialog(EditTopicActivity.this);
        //get extra from previous activity
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            threadId = bundle.getInt("thread_id");//recive int extra
            Log.i("EditTopicActivity", "thread id=>>" + threadId);

            new FetchDataAsync(EditTopicActivity.this).execute(threadId);
        }
    }


    private void submitProcess() {
        if (isValidateForm()) {
            new SubmitTask(EditTopicActivity.this).execute();
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

    private static class FetchDataAsync extends AsyncTask<Integer, Void, String> {

        private WeakReference<EditTopicActivity> weakReference;

        public FetchDataAsync(EditTopicActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private EditTopicActivity getContext() {
            return this.weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.getContext().loadingDialog.showDialog();
        }

        @Override
        protected String doInBackground(Integer... integers) {
            int threadId = integers[0];
            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(threadId));

            return ApiUtil.okHttpGet(this.getContext().forumSetting.getApiMainUrl() + "/thread/readone.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().prepareData(s);
            this.getContext().loadingDialog.dismissDialog();
        }

    }

    private void prepareData(String response) {
        Log.e("EditTopic", response);
        if (!Utility.isEmpty(response)) {
            try {
                JSONObject jo = new JSONObject(response);
                JSONObject data = jo.getJSONObject("data");
                binding.subject.setText(data.getString("subject"));
                binding.content.setText(data.getString("content"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private static class SubmitTask extends AsyncTask<Void, Void, String> {

        private WeakReference<EditTopicActivity> weakReference;

        SubmitTask(EditTopicActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private EditTopicActivity getContext() {
            return weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.getContext().loadingDialog.showDialog();
        }

        @Override
        protected String doInBackground(Void... voids) {

            EditTopicActivity activity = this.getContext();

            HashMap<String, String> params = new HashMap<>();
            params.put("_subject", activity.subject);
            params.put("_content", activity.content);
            params.put("_thread_id", String.valueOf(activity.threadId));

            return ApiUtil.okHttpPost(activity.forumSetting.getApiMainUrl() + "/thread/update.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().loadingDialog.dismissDialog();
            getContext().finishPost(s);
        }
    }

    private void finishPost(String response) {
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
