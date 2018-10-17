package bekaku.android.forum.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import bekaku.android.forum.R;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.model.PostModel;
import bekaku.android.forum.util.GlideUtil;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private Context c;
    private List<PostModel> list;
    private ForumSetting forumSetting;

    public PostAdapter(Context context, List<PostModel> postModelList, ForumSetting setting) {

        this.forumSetting = setting;
        this.c = context;
        this.list = postModelList;

    }
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_list_post, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder viewHolder, int positioni) {

        PostModel model = list.get(positioni);
        if(model!=null){
            Glide.with(c)
                    .load(model.getUserAccountPiture())
                    .apply(GlideUtil.circleOption())
                    .into(viewHolder.userImg);
            viewHolder.userName.setText(model.getUserAccountName());
            viewHolder.postDatetime.setText(model.getCreated());
            viewHolder.postContent.setText(model.getContent());

        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView userImg;
        public TextView userName;
        public TextView postDatetime;
        public TextView postContent;

        public ViewHolder(@NonNull View v) {
            super(v);
            userImg = v.findViewById(R.id.post_user_img);
            userName = v.findViewById(R.id.post_user_name);
            postDatetime = v.findViewById(R.id.post_datetime);
            postContent = v.findViewById(R.id.post_content);

        }
    }
}
