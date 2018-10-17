package bekaku.android.forum.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import bekaku.android.forum.R;
import bekaku.android.forum.util.Utility;

public class DialogSettingServerIp implements View.OnClickListener {

    private Context c;
    private Dialog mDialog;
    private EditText serversIp;
    private ProgressBar progressBar;

    private OnMyDialogResult mDialogResult; // the callback
    public DialogSettingServerIp(Context context){
        this.c = context;
    }
    public void showDialog() {

        if (mDialog == null) {
            mDialog = new Dialog(c, R.style.CustomDialogTheme);
        }

        mDialog.setContentView(R.layout.dialog_setting_server_id);
        mDialog.setCancelable(true);
        mDialog.show();

        // Permission StrictMode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TextView btnConfirm = mDialog.findViewById(R.id.confirmBtn);
        TextView btnCancle = mDialog.findViewById(R.id.cancleBtn);
        serversIp = mDialog.findViewById(R.id.serverIp);
        progressBar = mDialog.findViewById(R.id.progress);

        btnConfirm.setOnClickListener(this);
        btnCancle.setOnClickListener(this);

    }
    private class CheckServerTask extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return Utility.pingToHost(strings[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            checkServerPost(aBoolean);
        }
    }

    private void checkServerPost(boolean isOk){

        if (isOk){
            mDialogResult.finish(serversIp.getText().toString());
            mDialog.dismiss();
        }else{
            String errText = c.getResources().getString(R.string.err_ipaddress);
            Toast.makeText(c, errText, Toast.LENGTH_LONG).show();
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.cancleBtn :
                if( mDialogResult != null ){
                    mDialogResult.finish(null);
                }
                mDialog.dismiss();
                break;
            case R.id.confirmBtn :

                if(!Utility.isEmpty(serversIp.getText().toString())){
                    if( mDialogResult != null ){
                        //ping to this ip to test conection
                        new CheckServerTask().execute(serversIp.getText().toString());
                    }
                }else{
                    Toast.makeText(c, c.getResources().getString(R.string.err_ipaddress_empty), Toast.LENGTH_LONG).show();
                }
                break;
        }

    }
    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String result);
    }
    public void dismissDialog() {
        mDialog.dismiss();
    }
}
