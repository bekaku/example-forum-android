package bekaku.android.forum;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import bekaku.android.forum.databinding.ActivitySplashScreenBinding;
import bekaku.android.forum.dialog.DialogSettingServerIp;
import bekaku.android.forum.model.ForumSetting;
import bekaku.android.forum.util.SqliteHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashScreenActivity extends AppCompatActivity {

    private SqliteHelper sqliteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Removing title bar
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //Removing ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        ActivitySplashScreenBinding binding = DataBindingUtil.setContentView(SplashScreenActivity.this, R.layout.activity_splash_screen);

        //check setting data in sqlite
        sqliteHelper = new SqliteHelper(SplashScreenActivity.this);

        // Open Form Main when 1 second
        new Handler().postDelayed(new Runnable() {
            public void run() {
                checkSqliteAction();
            }
        }, 1000);



    }

    private void checkSqliteAction(){

        ForumSetting setting = sqliteHelper.findCurrentSetting();
        if(setting != null){
            if(setting.getUserId()>0 && setting.getUsername()!=null){


                System.out.println("getId="+setting.getId());
                System.out.println("getUserId="+setting.getUserId());
                System.out.println("getUsername="+setting.getUsername());
                System.out.println("getEmail="+setting.getEmail());
                System.out.println("getPicture="+setting.getPicture());
                System.out.println("getServerApi="+setting.getServerApi());

                //go to main activity
                Intent iMain = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(iMain);
                //close current activity
            }else{
                //go to login activity
                Intent iLogin = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(iLogin);
                //close current activity
            }
            finish();
        }else{
            //open Dialog and Create new api ip address and go to login activity
            DialogSettingServerIp dialogSettingServerIp = new DialogSettingServerIp(SplashScreenActivity.this);
            dialogSettingServerIp.showDialog();
            dialogSettingServerIp.setDialogResult(new DialogSettingServerIp.OnMyDialogResult() {
                @Override
                public void finish(String result) {
                    createSettingAction(result);
                }
            });
        }
    }

    private void createSettingAction(String result){

        ForumSetting forumSetting = new ForumSetting();
        forumSetting.setServerApi(result);
        //create dafault setting for this device
        long sqliteId = sqliteHelper.createNewForumSetting(forumSetting);
        System.out.println("create user id and go to login =>>"+sqliteId);
        //go to login activity
        Intent iLogin = new Intent(SplashScreenActivity.this, LoginActivity.class);
        startActivity(iLogin);
        //close current activity
        finish();

    }
}
