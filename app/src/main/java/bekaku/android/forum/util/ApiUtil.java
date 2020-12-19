package bekaku.android.forum.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiUtil {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType X_WWW_FORM_URLENCODE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
//    public static final String API_URI = "/forum/api";
    public static final String API_URI = "/grandats_project/forum/api";

    public static String okHttpGet(String url, HashMap<String, String> params){
        Log.e("okHttpGet",url);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();


//        OkHttpClient client = new OkHttpClient();


        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();

        if(!params.isEmpty()){
            setUrlParamiter(urlBuilder, params);
        }
        String finalUrl = urlBuilder.build().toString();
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                return response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private static void setUrlParamiter(HttpUrl.Builder urlBuilder,  HashMap<String, String> params){
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
    }
    public static String okHttpPost(String url, HashMap<String, String> params){

//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody body = RequestBody.create(JSON, setPostJsonBody(params));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                return response.body().string();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private static String setPostJsonBody(HashMap<String, String> params)  {
        JSONObject json= new JSONObject();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                json.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }
}
