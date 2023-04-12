package com.example.discboard.dialogs;

import static com.example.discboard.DiscFinal.*;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;

/**
 * SelectTempDialog
 * this dialog class is for template selecting options' showing
 * */
public class SelectTempDialog extends Dialog {
    Button mMyPresetBtn;
    JsonDataHelper mJsonDataHelper;
    public SelectTempDialog(@NonNull Context context) {
        super(context);
        this.setContentView(R.layout.dialog_select_temp);
        initViews();
    }

    private void initViews() {
        mJsonDataHelper = new JsonDataHelper(getContext());

        mMyPresetBtn = findViewById(R.id.my_preset_btn);

        if(mJsonDataHelper.getStringFromUserPreferences(USER_DATA_TEMP_MY, "").equals("")){
            mMyPresetBtn.setEnabled(false);
        }

        mMyPresetBtn.setOnClickListener(view -> {
            mOnSelectListener.onPressMy();
            mOnSelectListener.onPress();
        });

        findViewById(R.id.three_preset_btn).setOnClickListener(view -> {
            mOnSelectListener.onPress3();
            mOnSelectListener.onPress();
        });

        findViewById(R.id.five_preset_btn).setOnClickListener(view -> {
            mOnSelectListener.onPress5();
            mOnSelectListener.onPress();
        });

        findViewById(R.id.verstack_preset_btn).setOnClickListener(view -> {
            mOnSelectListener.onPressVert();
            mOnSelectListener.onPress();
        });

        findViewById(R.id.hostack_preset_btn).setOnClickListener(view -> {
            mOnSelectListener.onPressHo();
            mOnSelectListener.onPress();
        });
    }

    public interface OnSelectListener{
        public void onPressMy();
        public void onPress3();
        public void onPress5();
        public void onPressHo();
        public void onPressVert();
        void onPress();
    }

    OnSelectListener mOnSelectListener;

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }
}
