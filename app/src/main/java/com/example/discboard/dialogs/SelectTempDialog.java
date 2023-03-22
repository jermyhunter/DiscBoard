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

        if(mJsonDataHelper.getStringFromUserPreferences(USER_DATA_TEMP_MY).equals("")){
            mMyPresetBtn.setEnabled(false);
        }

        mMyPresetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnSelectListener.onPressMy();
            }
        });

        findViewById(R.id.three_preset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnSelectListener.onPress3();
            }
        });

        findViewById(R.id.five_preset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnSelectListener.onPress5();
            }
        });

        findViewById(R.id.hostack_preset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnSelectListener.onPressHo();
            }
        });

        findViewById(R.id.verstack_preset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnSelectListener.onPressVert();
            }
        });
    }

    public interface OnSelectListener{
        public void onPressMy();
        public void onPress3();
        public void onPress5();
        public void onPressHo();
        public void onPressVert();
    }

    OnSelectListener mOnSelectListener;

    public void setOnSelectListener(OnSelectListener onSelectListener) {
        mOnSelectListener = onSelectListener;
    }
}
