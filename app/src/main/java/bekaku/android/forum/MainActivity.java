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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(MainActivity.this, R.layout.activity_main);
        //toobar
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        SqliteHelper sqliteHelper = new SqliteHelper(MainActivity.this);
        forumSetting = sqliteHelper.findCurrentSetting();


        //set toolbar description
        setToolbarDescription();


        //prepare recyclerview and adapter
        threadAdapter = new ThreadAdapter(MainActivity.this, threadModelList, forumSetting);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayout.VERTICAL, false);

        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        binding.recyclerView.setAdapter(threadAdapter);

        //fetch data from server with page 1
        new FetchDataAsync(MainActivity.this).execute(this.page);


        //initial onClick
        binding.loadMore.setOnClickListener(this);

        //implement pull to refresh
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                threadAdapter.clear();//clear old data from list
                new FetchDataAsync(MainActivity.this).execute(page);
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
                ThreadModel threadSeleted = threadModelList.get(position);
                Intent iPost = new Intent(MainActivity.this, PostActivity.class);
                iPost.putExtra("thread_id", threadSeleted.getThreadId());//send value
                iPost.putExtra("bekaku.android.forum.model.ThreadModel", threadSeleted); //send object (implements  Serializable) in model before use
                startActivity(iPost);
            }
        }));

    }

    private void setToolbarDescription(){
        ImageView toolbarImg = toolbar.findViewById(R.id.toolbar_img);
        TextView toolbarPlus = toolbar.findViewById(R.id.toolbar_plus);

        toolbarImg.setOnClickListener(this);
        toolbarPlus.setOnClickListener(this);

        Glide.with(this)
                .load(forumSetting.getPicture())
                .apply(RequestOptions.circleCropTransform())//make img circle
                .into(toolbarImg);
    }

    private static class FetchDataAsync extends AsyncTask<Integer, Void, String>{

        private WeakReference<MainActivity> weakReference;

        public FetchDataAsync(MainActivity mainActivity) {
            weakReference = new WeakReference<>(mainActivity);
        }
        private MainActivity getContext(){
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
            HashMap<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            return ApiUtil.okHttpGet(this.getContext().forumSetting.getApiMainUrl() + "/thread/read.php", params);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            this.getContext().prepareData(s);

        }
    }
    private void prepareData(String response){

        if(!Utility.isEmpty(response)){
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

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.toolbar_img :
                //open seting activity
                break;
            case R.id.toolbar_plus:
                //open thread create
                break;
            case R.id.load_more:
                break;

        }
    }
}
