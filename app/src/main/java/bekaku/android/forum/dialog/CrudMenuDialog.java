package bekaku.android.forum.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import bekaku.android.forum.R;

public class CrudMenuDialog implements View.OnClickListener {

    private Context c;
    private Dialog mDialog;

    private OnMyDialogResult mDialogResult; // the callback

    public CrudMenuDialog(Context context) {
        this.c = context;
    }

    public void showDialog() {

        if (mDialog == null) {
            mDialog = new Dialog(c, R.style.CustomDialogTheme);
        }

        mDialog.setContentView(R.layout.dialog_crud_menu);
        mDialog.setCancelable(true);
        mDialog.show();

        // Permission StrictMode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        TextView btnCancle = mDialog.findViewById(R.id.cancleBtn);
        LinearLayout btnEdit = mDialog.findViewById(R.id.edit);
        LinearLayout btnDelete = mDialog.findViewById(R.id.delete);
        btnCancle.setOnClickListener(this);
        btnEdit.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancleBtn:
            case R.id.dialog_holder:
                if (mDialogResult != null) {
                    mDialogResult.finish(null);
                }
                mDialog.dismiss();
                break;
            case R.id.edit:
                if (mDialogResult != null) {
                    mDialogResult.finish("edit");
                }
                mDialog.dismiss();
                break;
            case R.id.delete:
                if (mDialogResult != null) {
                    mDialogResult.finish("delete");
                }
                mDialog.dismiss();
                break;
        }

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
