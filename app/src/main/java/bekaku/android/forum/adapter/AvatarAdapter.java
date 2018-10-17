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
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import bekaku.android.forum.R;
import bekaku.android.forum.model.DefaultAvatar;
import bekaku.android.forum.util.GlideUtil;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    private Context c;
    private List<DefaultAvatar> list;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView check;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.avatartImg);
            check = itemView.findViewById(R.id.checkIcon);
        }
    }

    public AvatarAdapter(Context context, List<DefaultAvatar> listParam) {
        this.list = listParam;
        this.c = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_list_avatar, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        DefaultAvatar defaultAvatar = list.get(i);

        if(defaultAvatar.isSelected()){
            viewHolder.check.setVisibility(View.VISIBLE);
        }else{
            viewHolder.check.setVisibility(View.GONE);
        }

        Glide.with(c)
                .load(defaultAvatar.getImgApiUrl())
                .apply(GlideUtil.defaultOption())
                .into(viewHolder.imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


}
