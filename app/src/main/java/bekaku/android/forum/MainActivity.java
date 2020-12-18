package bekaku.android.forum;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bekaku.android.forum.adapter.ThreadAdapter;
import bekaku.android.forum.databinding.ActivityMainBinding;
import bekaku.android.forum.dialog.CrudMenuDialog;
import bekaku.android.forum.dialog.DialogConfirm;
import bekaku.android.forum.dialog.DialogSettingServerIp;
import bekaku.android.forum.dialog.LoadingDialog;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.model.ThreadModel;
import bekaku.android.forum.util.ApiUtil;
import bekaku.android.forum.util.RecyclerTouchListener;
import bekaku.android.forum.util.SqliteHelper;
import bekaku.android.forum.util.Utility;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private ActivityMainBinding binding;
    private ForumSetting forumSetting;
    private List<ThreadModel> threadModelList = new ArrayList<>();
    private ThreadAdapter threadAdapter;
    private int page = 1;
    private SqliteHelper sqliteHelper;
    public LoadingDialog loadingDialog;
    private int selectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        //toobar
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        sqliteHelper = new SqliteHelper(MainActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();
        loadingDialog = new LoadingDialog(MainActivity.this);

        //set toolbar description
        setToolbarDescription();


        //prepare recyclerview and adapter
        threadAdapter = new ThreadAdapter(MainActivity.this, threadModelList, forumSetting);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayout.VERTICAL, false);

        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(threadAdapter);

        //fetch data from server with page 1
        new FetchDataAsync(MainActivity.this).execute(this.page, forumSetting.getUserId());


        //initial onClick
        binding.loadMore.setOnClickListener(this);

        //implement pull to refresh
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                threadAdapter.clear();//clear old data from list
                new FetchDataAsync(MainActivity.this).execute(page, forumSetting.getUserId());
            }
        });
        // Configure the refreshing colors
        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);


        //initial recycler con item click open comment post activity
        binding.recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, new RecyclerTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                ThreadModel threadSeleted = threadModelList.get(position);
//                Log.i("recyclerView", "onItemClick : " + position+" "+view.getId());
//                switch (view.getId()) {
//                    case R.id.thread_subject:
//                        Intent iPost = new Intent(MainActivity.this, PostActivity.class);
//                        iPost.putExtra("thread_id", threadSeleted.getThreadId());//send value
//                        iPost.putExtra("bekaku.android.forum.model.ThreadModel", threadSeleted); //send object (implements  Serializable) in model before use
//                        startActivity(iPost);
//                        break;
//                    case R.id.menu_icon:
//                        Log.i("ThredAdapter", "onClickMenu : " + position);
//                        break;
//                }
            }
        }));

    }

    public void openMenuItem(final int position) {
        Log.i("MainActivity", "openMenuItem : " + position);
        //open Dialog and Create new api ip address and go to login activity
        this.selectedItem = position;
        CrudMenuDialog crudMenu = new CrudMenuDialog(MainActivity.this);
        crudMenu.showDialog();
        crudMenu.setDialogResult(new CrudMenuDialog.OnMyDialogResult() {
            @Override
            public void finish(String result) {
                if (result != null) {
                    Log.i("openMenuItem", result);
                    if (result.equalsIgnoreCase("edit")) {
                        openEdit(position);
                    } else if (result.equalsIgnoreCase("delete")) {
                        openDelete(position);
                    }
                }
            }
        });
    }

    private void openDelete(final int position) {
        final ThreadModel threadModel = threadModelList.get(position);
        DialogConfirm dialogConfirm = new DialogConfirm(MainActivity.this
                , getResources().getString(R.string.confirm)
                , getResources().getString(R.string.confirm_delete)
        );
        dialogConfirm.showDialog();
        dialogConfirm.setDialogResult(new DialogConfirm.OnMyDialogResult() {
            @Override
            public void finish(boolean result) {
                if (result) {
                    new DeleteAsync(MainActivity.this).execute(threadModel.getThreadId());
                }
            }
        });
    }

    private void openEdit(int position) {
        ThreadModel threadModel = threadModelList.get(position);
        if (threadModel != null) {
            Intent iPost = new Intent(MainActivity.this, EditTopicActivity.class);
            iPost.putExtra("thread_id", threadModel.getThreadId());//send value
            startActivity(iPost);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.new_topic:
                Toast.makeText(getApplicationContext(), "New topic", Toast.LENGTH_LONG).show();
                Intent newTopic = new Intent(MainActivity.this, NewTopicActivity.class);
                startActivity(newTopic);
                return true;
            case R.id.logout:
                sqliteHelper.deleteAllSetting();
                Toast.makeText(getApplicationContext(), "Logout sucessfull", Toast.LENGTH_LONG).show();
                Intent iLogin = new Intent(MainActivity.this, SplashScreenActivity.class);
                startActivity(iLogin);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setToolbarDescription() {
        ImageView toolbarImg = toolbar.findViewById(R.id.toolbar_img);
//        TextView toolbarPlus = toolbar.findViewById(R.id.toolbar_plus);

        toolbarImg.setOnClickListener(this);
//        toolbarPlus.setOnClickListener(this);

        Glide.with(this)
                .load(forumSetting.getPicture())
                .apply(RequestOptions.circleCropTransform())//make img circle
                .into(toolbarImg);
    }

    private static class FetchDataAsync extends AsyncTask<Integer, Void, String> {

        private WeakReference<MainActivity> weakReference;

        public FetchDataAsync(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        private MainActivity getContext() {
            return this.weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.getContext().binding.swipeContainer.setRefreshing(true);
        }

        @Override
        protected String doInBackground(Integer... integers) {
            int page = integers[0];
            int userId = integers[1];
            HashMap<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("_user_account_id", String.valueOf(userId));
            return ApiUtil.okHttpGet(this.getContext().forumSetting.getApiMainUrl() + "/thread/read.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().prepareData(s);

        }
    }

    private void prepareData(String response) {

        if (!Utility.isEmpty(response)) {
            try {
                JSONObject jo = new JSONObject(response);
                JSONArray jsonArray = jo.getJSONArray("data");

                ThreadModel model;
                if (jsonArray.length() > 0) {
                    //increase page for the next time
                    page++;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject data = jsonArray.getJSONObject(i);
                        model = new ThreadModel();
                        model.setThreadId(data.getInt("id"));
                        model.setSubject(data.getString("subject"));
                        model.setContent(data.getString("content"));
                        model.setCreated(data.getString("created"));
                        model.setUserAccountId(data.getInt("user_account_id"));
                        model.setUserAccountName(data.getString("user_account_name"));
                        model.setUserAccountPicture(data.getString("user_account_picture"));
                        model.setPostCount(data.getInt("post_count"));
                        model.setVoteUp(data.getInt("votes_up"));
                        model.setVoteDown(data.getInt("votes_down"));
                        model.setUserLike(data.getInt("is_user_like") > 0);
                        model.setUserDisLike(data.getInt("is_user_dislike") > 0);
                        model.setUserComment(data.getInt("is_user_comment") > 0);
                        threadModelList.add(model);

                        threadAdapter.notifyDataSetChanged();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        binding.swipeContainer.setRefreshing(false);
    }

    // delete
    private static class DeleteAsync extends AsyncTask<Integer, Void, String> {

        private WeakReference<MainActivity> weakReference;

        public DeleteAsync(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }

        private MainActivity getContext() {
            return this.weakReference.get();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.getContext().loadingDialog.showDialog();
        }

        @Override
        protected String doInBackground(Integer... integers) {
            int id = integers[0];
            HashMap<String, String> params = new HashMap<>();
            params.put("_thread_id", String.valueOf(id));
            return ApiUtil.okHttpGet(this.getContext().forumSetting.getApiMainUrl() + "/thread/delete.php", params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().finishDelete(s);
            this.getContext().loadingDialog.dismissDialog();
        }
    }

    private void finishDelete(String response) {
        Log.e("finishDelete", response);
        try {
            JSONObject data = new JSONObject(response);

            JSONObject serverStatus = data.getJSONObject("server_status");

            int statusCode = serverStatus.getInt("status");
            String statusMsg = serverStatus.getString("message");
            Toast.makeText(this, statusMsg, Toast.LENGTH_LONG).show();
            if (statusCode == 1) {
                threadModelList.remove(this.selectedItem);
                threadAdapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.toolbar_img:
                //open seting activity
                break;
//            case R.id.toolbar_plus:
            //open thread create
//                break;
            case R.id.load_more:
                new FetchDataAsync(MainActivity.this).execute(this.page);
                break;

        }
    }
}
