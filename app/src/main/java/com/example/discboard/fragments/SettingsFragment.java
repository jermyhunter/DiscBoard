package com.example.discboard.fragments;

import static com.example.discboard.DiscFinal.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.datatype.Dot;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * SettingsFragment
 * used for importing & exporting user's data
 * */
public class SettingsFragment extends Fragment {
    static String TAG = "Settings Fragment";
    JsonDataHelper mJsonDataHelper;
    Button mImportBtn, mExportBtn, mShareBtn;
    Slider mAnimSpeedCtrlSlider;
    TextView mSliderText;
    ArrayList<String> mAniTempList;
    ActivityResultLauncher<String> mImportData;
    ExportDialogFragment mExportDialogFragment;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJsonDataHelper = new JsonDataHelper(getContext());

        mAniTempList = mJsonDataHelper.loadTempNamesFromPref();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        mExportBtn = v.findViewById(R.id.export_btn);
        mImportBtn = v.findViewById(R.id.import_btn);
        mShareBtn = v.findViewById(R.id.share_btn);
        mAnimSpeedCtrlSlider = v.findViewById(R.id.anim_speed_ctrl_slider);
        mAnimSpeedCtrlSlider.setValue(mJsonDataHelper.getIntegerFromUserPreferences(USER_DATA_ANIM_SPEED));

        mAnimSpeedCtrlSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                mJsonDataHelper.setIntegerToUserPreferences(USER_DATA_ANIM_SPEED, (int) slider.getValue());
            }
        });

        // 检查存放路径中的导出文件是否还在 - 用于控制“分享”按钮的启用
//        if (!mJsonDataHelper.sharedFileExists()) {
//            disableShareFunction();
//            mShareBtn.setVisibility(View.INVISIBLE);
//        } else {
//            enableShareFunction();
//            mShareBtn.setVisibility(View.VISIBLE);
//        }

        mShareBtn.setOnClickListener(view -> {
            if (mJsonDataHelper.sharedFileExists()) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                // get exported file path from preferences
                Uri uri = Uri.parse(mJsonDataHelper.getStringFromUserPreferences(USER_DATA_EXPORTED_FILE_PATH));
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("application/json");
                startActivity(Intent.createChooser(shareIntent, null));
            } else {
                Toast.makeText(getContext(), "使用\"分享战术\"前，请先\"导出战术\"", Toast.LENGTH_SHORT).show();
            }
        });


        mExportDialogFragment = new ExportDialogFragment();
        mExportDialogFragment.setExportDialogListener(file_name -> {
            try {
                exportUserData(file_name);

                Toast.makeText(getContext(), "导出成功！", Toast.LENGTH_LONG).show();
//                enableShareFunction();
//                        Log.d(TAG, "onCreateView: " + "导出成功");
            } catch (JSONException e) {
//                        Log.d(TAG, "onCreateView: " + "导出失败");
                throw new RuntimeException(e);
            }
        });
        /*
         * 导出数据->
         *   弹出文件命名引导框
         * */
        mExportBtn.setOnClickListener(view -> {
            if (mAniTempList.size() > 0) {
                mExportDialogFragment.show(getChildFragmentManager(), "导出引导框");
            } else {
                Toast.makeText(getContext(), "请先制作战术模板", Toast.LENGTH_SHORT).show();
            }

        });

        mImportBtn.setOnClickListener(view -> {
            importDataFile();
        });

        v.findViewById(R.id.init_temp_btn).setOnClickListener(view -> {
            mJsonDataHelper.setBooleanToUserPreferences(USER_DATA_FIRST_RUN_MARK, true);
            Toast.makeText(getContext(), "模板初始化完成，请重新启动程序！", Toast.LENGTH_LONG).show();
//            Log.d(TAG, "FIRST MARK: SETTING" + mJsonDataHelper.getBooleanToUserPreferences(USER_DATA_FIRST_RUN_MARK));
        });
        /*
         * 导入数据 ->
         *   首先验证文件头(data_type:animation_temps)，为discboard文件
         *   如果是，那么删除所有temp_names，以及所有anim_dots，之后读入新文件内容
         * */
        mImportData = registerForActivityResult(new ActivityResultContracts.GetContent(),
                selectedFileUri -> {
                    if (null != selectedFileUri) {
                        // read json data from external file
                        String json_s = mJsonDataHelper.readFromExternalFile(selectedFileUri);
                        try {
                            JSONArray js_whole = new JSONArray(json_s);
                            JSONObject jo_head = new JSONObject(String.valueOf(js_whole.get(0)));
                            // check the file's head part, if it's a "animation_templates" file
                            if (mJsonDataHelper.checkFileType(jo_head)) {
                                JSONArray ja_anim_temp_list = (JSONArray) new JSONObject(String.valueOf(js_whole.get(1))).get(IO_ANIM_TEMP_LIST);// get anim_temp_name_list
                                JSONArray ja_anim_dots_list = (JSONArray) new JSONObject(String.valueOf(js_whole.get(2))).get(IO_ANIM_DOTS_LIST);// get anim_dots_list

                                int i = 0;
                                while (i < ja_anim_temp_list.length()) {
                                    JSONObject jo_anim_dots_list = ja_anim_dots_list.getJSONObject(i);

                                    String temp_name = jo_anim_dots_list.keys().next();

                                    JSONArray ja_anim_dots = (JSONArray) jo_anim_dots_list.get(temp_name);
                                    // turn json string to anim_dots_lists
                                    ArrayList<Hashtable<String, Dot>> anim_dots_list = mJsonDataHelper.JSONArray2ArrayListHashtable(ja_anim_dots);

                                    // add data to pref
                                    // check the possibility of name duplication
                                    if (mJsonDataHelper.checkNameDuplication(temp_name)) {
                                        temp_name = temp_name + FILE_DUPLICATION_SUFFIX;
                                    }
                                    mJsonDataHelper.saveAniDotsToPref(temp_name, anim_dots_list);
                                    mJsonDataHelper.addAniTempToPref(temp_name);
                                    i++;
                                }
                            } else {
                                Toast.makeText(getContext(), "文件无效！", Toast.LENGTH_LONG).show();
                            }
//                            Log.d(TAG, "onCreateView: " + "导入成功");
                            Toast.makeText(getContext(), "导入成功", Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
//        Log.d(TAG, "onCreateView: " + mJsonDataHelper.checkNameDuplication("战术板1"));

        return v;
    }

//    private void disableShareFunction() {
//        mShareBtn.setEnabled(false);
//        mShareBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.gray));
//    }

//    private void enableShareFunction() {
//        mShareBtn.setEnabled(true);
//        mShareBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.Ivory));
//    }

    void importDataFile() {
//        mImportData.launch("*/*");
        mImportData.launch("application/json");// 导入文件限定为json文件类型
    }

    /**
     * 使用 JSONArray 存储三个数据：
     * head->animation_templates, anim_temp_list-> name1,name2,..., anim_dots_list-> dots_list1,dots_list2,...
     */
    void exportUserData(String file_name) throws JSONException {
        if (mAniTempList.size() > 0) {
            JSONArray ja_anim_temp_list = new JSONArray(mAniTempList);
            JSONArray ja_anim_dots_list = new JSONArray();

            int i = 0;
            for (String tempName : mAniTempList) {
                ArrayList<Hashtable<String, Dot>> anim_dots = new ArrayList<>(mJsonDataHelper.loadAnimDotsFromPref(tempName));
                String json_s = mJsonDataHelper.transformData2Json(anim_dots);

                JSONArray ja_dots = new JSONArray(json_s);
                JSONObject jo_dots = new JSONObject();
                jo_dots.put(tempName, ja_dots);
                ja_anim_dots_list.put(jo_dots);

                i++;
            }
            JSONArray ja_whole = new JSONArray();

            JSONObject jo_head = new JSONObject();
            jo_head.put(IO_HEAD, IO_HEAD_TYPE);
            ja_whole.put(jo_head);

            JSONObject jo_anim_temp_list = new JSONObject();
            jo_anim_temp_list.put(IO_ANIM_TEMP_LIST, ja_anim_temp_list);
            ja_whole.put(jo_anim_temp_list);

            JSONObject jo_dots_list = new JSONObject();
            jo_dots_list.put(IO_ANIM_DOTS_LIST, ja_anim_dots_list);
            ja_whole.put(jo_dots_list);

            mJsonDataHelper.writeToExternalFile(ja_whole.toString(), file_name);
        } else {
            Toast.makeText(getContext(), "请先制作战术模板", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * ExportDialogFragment
     * inner dialog fragment, used for guiding the user to name the to-be-exported file
     */
    public static class ExportDialogFragment extends DialogFragment {
        EditText mExportNameInput;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();// * 从 requireActivity() 改为 getActivity()
            View dialogView = inflater.inflate(R.layout.dialog_export, null);

            mExportNameInput = dialogView.findViewById(R.id.export_name_input);

            builder.setView(dialogView)
                    .setPositiveButton("导出", (dialogInterface, i) -> {
                        String file_name = mExportNameInput.getText().toString();

                        mExportDialogListener.onExportListener(file_name);
                    })
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    });

            return builder.create();
        }

        // handling the export process
        ExportDialogListener mExportDialogListener;

        public void setExportDialogListener(ExportDialogListener exportDialogListener) {
            mExportDialogListener = exportDialogListener;
        }

        public interface ExportDialogListener {
            void onExportListener(String file_name);
        }
    }
}