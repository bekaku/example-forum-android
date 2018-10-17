package bekaku.android.forum.util;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import bekaku.android.forum.R;

public class GlideUtil {

//    ImageView imageView = findViewById(R.id.imageView);
//        Glide.with(this)
//                .load("https://i.pinimg.com/564x/d0/0f/92/d00f9238b6a3d3fb31e8675a6c10369d.jpg")
//                .apply(RequestOptions.circleCropTransform())//make img circle
//            .into(imageView);
    public static RequestOptions defaultOption(){

        return new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.default_image)
                .error(R.mipmap.default_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH)
                .dontAnimate()
                .dontTransform();
    }
    public static RequestOptions circleOption(){
        return RequestOptions.circleCropTransform();
    }

}
