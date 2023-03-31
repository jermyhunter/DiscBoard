package com.example.discboard.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.discboard.R;

public class RenameTempDialog extends AlertDialog {
    String mTempName;
    EditText mNameInput;

    public RenameTempDialog(@NonNull Context context, String tempName) {
        super(context);
        mTempName = tempName;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_rename_temp, null);
        setView(v);

        mNameInput = v.findViewById(R.id.new_name_input);
        mNameInput.setHint(mTempName + getContext().getString(R.string.old_name_string));

        setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.confirm_string), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onDialogPositiveClick(String.valueOf(mNameInput.getText()));
            }
        });

        setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_string), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        super.onCreate(savedInstanceState);
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface RenameTempDialogListener {
        public void onDialogPositiveClick(String tempNameNew);
    }

    public void setDelCheckDialogListener(RenameTempDialogListener renameTempDialogListener){
        listener = renameTempDialogListener;
    }

    // Use this instance of the interface to deliver action events
    RenameTempDialogListener listener;
}