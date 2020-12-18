package bekaku.android.forum.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import bekaku.android.forum.R;

public class DialogConfirm {

    private Context mDialogActivity;
    private Dialog mDialog;

    private TextView mDialogOKButton;
    private TextView mDialogCancleButton;

    private String titleText;
    private String bodyText;

    private OnMyDialogResult mDialogResult; // the callback
    public DialogConfirm(Context mActivity, String header, String text) {
        this.mDialogActivity = mActivity;
        this.titleText = header;
        this.bodyText = text;

    }
    public void showDialog() {
        if (mDialog == null) {
            mDialog = new Dialog(mDialogActivity, R.style.CustomDialogTheme);
        }
        mDialog.setContentView(R.layout.dialog_confirm);
        mDialog.setCancelable(true);
        mDialog.show();

        TextView mDialogHeader = mDialog.findViewById(R.id.title);
        TextView mDialogText = mDialog.findViewById(R.id.text);
        mDialogOKButton = mDialog.findViewById(R.id.confirmBtn);
        mDialogCancleButton = mDialog.findViewById(R.id.cancleBtn);

        mDialogHeader.setText(titleText);
        mDialogText.setText(bodyText);

        initDialogButtons();
    }

    private void initDialogButtons() {

        mDialogOKButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                if( mDialogResult != null ){
                    mDialogResult.finish(true);
                }

                mDialog.dismiss();
            }
        });
        mDialogCancleButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                if( mDialogResult != null ){
                    mDialogResult.finish(false);
                }

                mDialog.dismiss();
            }
        });
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }
    public interface OnMyDialogResult{
        void finish(boolean result);
    }


    public void dismissDialog() {
        mDialog.dismiss();
    }
}
