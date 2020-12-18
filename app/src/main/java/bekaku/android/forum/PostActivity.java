package bekaku.android.forum;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bekaku.android.forum.adapter.PostAdapter;
import bekaku.android.forum.databinding.ActivityPostBinding;
import bekaku.android.forum.dialog.CrudMenuDialog;
import bekaku.android.forum.dialog.LoadingDialog;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.model.PostModel;
import bekaku.android.forum.model.ThreadModel;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.GlideUtil;
import bekaku.android.forum.util.HidingScrollListener;
import bekaku.android.forum.util.SqliteHelper;
import bekaku.android.forum.util.Utility;

public class PostActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPostBinding binding;
    private int threadId;
    private ThreadModel threadModel;
    private int page = 1;
    private ForumSetting forumSetting;

    private List<PostModel> postModelList = new ArrayList<>();
    private PostAdapter postAdapter;
    private HidingScrollListener hidingScrollListener;
    private int threshold = 50;
    TextView replyText;

    public LoadingDialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
//        setContentView(R.layout.activity_post);
        binding = DataBindingUtil.setContentView(PostActivity.this, R.layout.activity_post);
        loadingDialog = new LoadingDialog(PostActivity.this);

        TextView replySubmit = findViewById(R.id.reply_submit);
        replyText = findViewById(R.id.reply_edit_text);

        //get devide setting
        SqliteHelper sqliteHelper = new SqliteHelper(PostActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();

        //get extra from previous activity
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            threadId = bundle.getInt("thread_id");//recive int extra
            threadModel = (ThreadModel) bundle.getSerializable("bekaku.android.forum.model.ThreadModel");//recive objec extra
            if (threadModel != null) {
                System.out.println("thread subject=>>" + threadModel.getSubject());
                setThreadData();
                postAdapter = new PostAdapter(PostActivity.this, postModelList, forumSetting);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(PostActivity.this);
                binding.recyclerView.setLayoutManager(mLayoutManager);
                binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
                binding.recyclerView.setAdapter(postAdapter);

                if (threadModel.getUserAccountId() == forumSetting.getUserId()) {
                    binding.menuIcon.setVisibility(View.VISIBLE);
                } else {
                    binding.menuIcon.setVisibility(View.INVISIBLE);
                }

                //fetch this thread's post data from server
                new FetchDataAsync(PostActivity.this).execute(threadId, page);
            }

        } else {
            finish();
            Toast.makeText(PostActivity.this, "Someting went wrong", Toast.LENGTH_LONG).show();
        }

        System.out.println("Thread id =>" + threadId);

//        implement pull to refresh
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                postAdapter.clear();
                new FetchDataAsync(PostActivity.this).execute(threadId, page);
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        //initial onClick
        binding.threadVoteUpIcon.setOnClickListener(this);
        binding.threadVoteDownIcon.setOnClickListener(this);
        binding.menuIcon.setOnClickListener(this);
        replySubmit.setOnClickListener(this);
        binding.menuIcon.setVisibility(View.INVISIBLE);

        //initial hide layout when scroll recyclerview
//        hidingScrollListener = new HidingScrollListener(threshold) {
//            @Override
//            public void onHide() {
//                hideViews();
//            }
//
//            @Override
//            public void onShow() {
//                showViews();
//            }
//        };
//        binding.recyclerView.addOnScrollListener(hidingScrollListener);
//        binding.recyclerView.addOnScrollListener(new HidingScrollListener2() {
//            @Override
//            public void onHide() {
//                hideViews();
//            }
//
//            @Override
//            public void onShow() {
//                showViews();
//            }
//        });
    }

    private void hideViews() {
        binding.threadDataHolder.animate().translationY(-binding.threadDataHolder.getHeight()).setInterpolator(new AccelerateInterpolator(2)).setDuration(500);
//        binding.threadDataHolder.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(0).scaleY(0);

        binding.threadDataHolder.animate().translationY(-binding.threadDataHolder.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
    }

    private void showViews() {
//        binding.threadDataHolder.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).setDuration(500);
//        binding.threadDataHolder.animate().setInterpolator(new AccelerateDecelerateInterpolator()).scaleX(1).scaleY(1);
        binding.threadDataHolder.animate().translationY(binding.threadDataHolder.getTop()).setInterpolator(new AccelerateInterpolator()).start();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.thread_vote_up_icon:
                new VoteThreadUp().execute(threadModel);
                break;
            case R.id.thread_vote_down_icon:
                new VoteThreadDown().execute(threadModel);
                break;
            case R.id.menu_icon:
                openMenuItem();
                break;
            case R.id.reply_submit:
                onReplySubmit();
                break;
        }
    }

    private void onReplySubmit() {
        if (!Utility.isEmpty(replyText.getText())) {
            Log.i("PostActivity", "reply_submit" + " " + replyText.getText());
            new ReplySubmit(PostActivity.this).execute(this.threadId);
        }
    }

    public void openMenuItem() {
        Log.i("PostActivity", "openMenuItem ");
        //open Dialog and Create new api ip address and go to login activity
        CrudMenuDialog crudMenu = new CrudMenuDialog(PostActivity.this);
        crudMenu.showDialog();
        crudMenu.setDialogResult(new CrudMenuDialog.OnMyDialogResult() {
            @Override
            public void finish(String result) {
                if (result != null) {
                    Log.i("openMenuItem", result);
                }
            }
        });
    }

    private static class FetchDataAsync extends AsyncTask<Integer, Void, String> {

        private WeakReference<PostActivity> weakReference;

        public FetchDataAsync(PostActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private PostActivity getContext() {
            return this.weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.getContext().binding.swipeContainer.setRefreshing(true);
            this.getContext().loadingDialog.showDialog();
        }

        @Override
        protected String doInBackground(Integer... integers) {

            int threadId = integers[0];
            int page = integers[1];
            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(threadId));
            params.put("page", String.valueOf(page));

            return ApiUtil.okHttpGet(this.getContext().forumSetting.getApiMainUrl() + "/post/read.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().prepareData(s);
            this.getContext().loadingDialog.dismissDialog();
        }

    }

    private void prepareData(String response) {
        if (!Utility.isEmpty(response)) {
            try {
                JSONObject jo = new JSONObject(response);
                JSONArray jsonArray = jo.getJSONArray("data");

                if (jsonArray.length() > 0) {
                    //increase page for the next time
                    page++;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        setPostModel(jsonArray.getJSONObject(i));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        binding.swipeContainer.setRefreshing(false);
    }

    private static class ReplySubmit extends AsyncTask<Integer, Void, String> {

        private WeakReference<PostActivity> weakReference;

        public ReplySubmit(PostActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        private PostActivity getContext() {
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
            params.put("_content", this.getContext().replyText.getText().toString());
            params.put("_user_account_id", String.valueOf(this.getContext().forumSetting.getUserId()));

            return ApiUtil.okHttpPost(this.getContext().forumSetting.getApiMainUrl() + "/post/create.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().prepareDataReply(s);
            this.getContext().loadingDialog.dismissDialog();
        }

    }

    private void prepareDataReply(String response) {
        if (!Utility.isEmpty(response)) {
            try {
                JSONObject jo = new JSONObject(response);

                JSONObject serverStatus = jo.getJSONObject("server_status");
                String statusMsg = serverStatus.getString("message");
                Toast.makeText(this, statusMsg, Toast.LENGTH_LONG).show();
                setPostModel(jo.getJSONObject("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        replyText.setText("");
    }

    private void setPostModel(JSONObject data) throws JSONException {
        if (data != null) {
            PostModel model = new PostModel();
            model.setPostId(data.getInt("id"));
            model.setContent(data.getString("content"));
            model.setThreadId(data.getInt("threads_id"));
            model.setCreated(data.getString("created"));
            model.setUserAccountId(data.getInt("user_account_id"));
            model.setUserAccountName(data.getString("user_account_name"));
            model.setUserAccountPiture(data.getString("user_account_picture"));
            postModelList.add(model);
            postAdapter.notifyDataSetChanged();
        }
    }

    private void setThreadData() {
        Glide.with(PostActivity.this)
                .load(threadModel.getUserAccountPicture())
                .apply(GlideUtil.circleOption())
                .into(binding.threadUserImg);

        binding.threadUserName.setText(threadModel.getUserAccountName());
        binding.threadDatetime.setText(threadModel.getCreated());
        binding.threadSubject.setText(threadModel.getSubject());
        binding.threadContent.setText(threadModel.getContent());

        binding.threadVoteUp.setText(String.valueOf(threadModel.getVoteUp()));
        binding.threadVoteDown.setText(String.valueOf(threadModel.getVoteDown()));
        binding.threadCommentsCount.setText(String.valueOf(threadModel.getPostCount()));


        if (threadModel.getUserLike()) {
            binding.threadVoteUp.setTextColor(getResources().getColor(R.color.color_green_1));
        } else {
            binding.threadVoteUp.setTextColor(getResources().getColor(R.color.text_mute));
        }
        if (threadModel.getUserDisLike()) {
            binding.threadVoteDown.setTextColor(getResources().getColor(R.color.color_red_1));
        } else {
            binding.threadVoteDown.setTextColor(getResources().getColor(R.color.text_mute));
        }
        if (threadModel.isUserComment()) {
            binding.threadCommentsCount.setTextColor(getResources().getColor(R.color.color_yellow_1));
        } else {
            binding.threadCommentsCount.setTextColor(getResources().getColor(R.color.text_mute));
        }
    }

    private void setThredVoting() {

        binding.threadVoteUp.setText(String.valueOf(threadModel.getVoteUp()));
        binding.threadVoteDown.setText(String.valueOf(threadModel.getVoteDown()));
        binding.threadCommentsCount.setText(String.valueOf(threadModel.getPostCount()));

        if (threadModel.isUserVoteUp()) {
            binding.threadVoteUpIcon.setTextColor(getResources().getColor(R.color.color_green_1));
        } else {
            binding.threadVoteUpIcon.setTextColor(getResources().getColor(R.color.text_mute));
        }
        if (threadModel.isUserVoteDown()) {
            binding.threadVoteDownIcon.setTextColor(getResources().getColor(R.color.color_red_1));
        } else {
            binding.threadVoteDownIcon.setTextColor(getResources().getColor(R.color.text_mute));
        }

        if (threadModel.isUserComment()) {
            binding.threadCommentsIcon.setTextColor(getResources().getColor(R.color.color_yellow_1));
        } else {
            binding.threadCommentsIcon.setTextColor(getResources().getColor(R.color.text_mute));
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class VoteThreadUp extends AsyncTask<ThreadModel, Void, ThreadModel> {

        @Override
        protected ThreadModel doInBackground(ThreadModel... models) {
            ThreadModel model = models[0];

            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(model.getThreadId()));
            params.put("_user_account_id", String.valueOf(forumSetting.getUserId()));

            String response = ApiUtil.okHttpGet(forumSetting.getApiMainUrl() + "/vote/upthread.php", params);
            try {

                assert response != null;
                JSONObject json = new JSONObject(response);
                JSONObject data = json.getJSONObject("data");
                model.setVoteUp(data.getInt("votes_up"));
                model.setVoteDown(data.getInt("votes_down"));
                model.setPostCount(data.getInt("post_count"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return model;
        }

        @Override
        protected void onPostExecute(ThreadModel threadModel) {
            super.onPostExecute(threadModel);
            threadModel.setUserVoteUp(!threadModel.isUserVoteUp());
            setThredVoting();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class VoteThreadDown extends AsyncTask<ThreadModel, Void, ThreadModel> {

        @Override
        protected ThreadModel doInBackground(ThreadModel... models) {
            ThreadModel model = models[0];

            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(model.getThreadId()));
            params.put("_user_account_id", String.valueOf(forumSetting.getUserId()));

            String response = ApiUtil.okHttpGet(forumSetting.getApiMainUrl() + "/vote/downthread.php", params);

            try {

                assert response != null;
                JSONObject json = new JSONObject(response);
                JSONObject data = json.getJSONObject("data");
                model.setVoteUp(data.getInt("votes_up"));
                model.setVoteDown(data.getInt("votes_down"));
                model.setPostCount(data.getInt("post_count"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return model;
        }

        @Override
        protected void onPostExecute(ThreadModel threadModel) {
            super.onPostExecute(threadModel);
            threadModel.setUserVoteDown(!threadModel.isUserVoteDown());
            setThredVoting();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
