package com.example.discboard.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.discboard.R;

public class DelCheckDialog extends AlertDialog {
    String mTempName;
    TextView mTempNameTxt;

    public DelCheckDialog(@NonNull Context context, String tempName) {
        super(context);
        mTempName = tempName;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_del_check, null);
        setView(v);

        mTempNameTxt = v.findViewById(R.id.temp_name_txt);
        mTempNameTxt.setText(mTempName);

        setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.confirm_string), (dialog, which) -> {
            listener.onDialogPositiveClick(String.valueOf(mTempNameTxt.getText()));
            Toast.makeText(getContext(), mTempName
                    + " " + getContext().getString(R.string.delete_success_hint), Toast.LENGTH_SHORT).show();
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_string), (dialog, which) -> {
        });

        super.onCreate(savedInstanceState);
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface DelCheckDialogListener {
        public void onDialogPositiveClick(String tempNameNew);
    }

    public void setDelCheckDialogListener(DelCheckDialogListener delCheckDialogListener){
        listener = delCheckDialogListener;
    }

    // Use this instance of the interface to deliver action events
    DelCheckDialogListener listener;
}