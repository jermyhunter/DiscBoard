package com.example.discboard.dialogs;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.discboard.R;
import com.example.discboard.adapter.AnimTempItemAdapter;

import java.util.ArrayList;

public class UnsavedCheckDialog extends AlertDialog {
    public UnsavedCheckDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        View content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_save_check, null);
        setView(content);
        setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.confirm_string), (dialog, which) -> mUnsavedCheckDialogListener.onCheckListener());

        setButton(DialogInterface.BUTTON_NEGATIVE, getContext().getString(R.string.cancel_string), (dialog, which) -> {
        });

        super.onCreate(savedInstanceState);
    }

    UnsavedCheckDialogListener mUnsavedCheckDialogListener;

    public void setUnsavedDialogListener(UnsavedCheckDialogListener mListener) {
        mUnsavedCheckDialogListener = mListener;
    }

    public interface UnsavedCheckDialogListener{
        void onCheckListener();
    }
}
