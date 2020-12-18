package bekaku.android.forum.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.StrictMode;
import android.view.View;
import android.widget.TextView;

import bekaku.android.forum.R;

public class LoadingDialog implements View.OnClickListener {

    private Context c;
    private Dialog mDialog;

    private OnMyDialogResult mDialogResult; // the callback

    public LoadingDialog(Context context) {
        this.c = context;
    }

    public void showDialog() {

        if (mDialog == null) {
            mDialog = new Dialog(c, R.style.CustomDialogTheme);
        }

        mDialog.setContentView(R.layout.dialog_loading);
        mDialog.setCancelable(true);
        mDialog.show();

        // Permission StrictMode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

    @Override
    public void onClick(View view) {
    }

    public void setDialogResult(OnMyDialogResult dialogResult) {
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult {
        void finish(String result);
    }

    public void dismissDialog() {
        mDialog.dismiss();
    }
}
