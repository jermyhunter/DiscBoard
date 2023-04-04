package com.example.discboard.fragments;

import static com.example.discboard.DiscFinal.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.discboard.DiscFinal;
import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.datatype.AnimTemp;
import com.example.discboard.datatype.Dot;
import com.example.discboard.datatype.InterDot;
import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Objects;

/**
 * SettingsFragment
 * used for importing & exporting user's data
 * */
public class SettingsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    static String TAG = "SettingsFragment";
    JsonDataHelper mJsonDataHelper;
    Button mImportBtn, mExportBtn, mShareBtn;
    ImageButton mLangBtn;
    Spinner mCanvasBGSpinner;
    String mCanvasTypeS;
    CheckBox mAutoSaveCB;
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

        fetchTempList();
    }

    private void fetchTempList() {
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
        mAutoSaveCB = v.findViewById(R.id.auto_save_cb);
        mCanvasBGSpinner = v.findViewById(R.id.canvas_bg_spinner);
        mAnimSpeedCtrlSlider = v.findViewById(R.id.anim_speed_ctrl_slider);
        mAnimSpeedCtrlSlider.setValue(mJsonDataHelper.getIntegerFromUserPreferences(USER_DATA_ANIM_SPEED));

        initCanvasBGSpinner();

        // lang_btn state setting
        mLangBtn = v.findViewById(R.id.lang_setting_btn);
        initLangBtn();
        // change between CN and EN
        mLangBtn.setOnClickListener(view -> {
            String localeCode = mJsonDataHelper.getStringFromUserPreferences(LOCALE_CODE, LocaleType.CN);
            if(localeCode.equals(LocaleType.CN)) {
                localeCode = LocaleType.EN;
            }
            else if(localeCode.equals(LocaleType.EN)){
                localeCode = LocaleType.CN;
            }

            mJsonDataHelper.setStringToUserPreferences(LOCALE_CODE, localeCode);
            mJsonDataHelper.setAppLocale(localeCode);
            getActivity().recreate();
        });

        // initiate checkbox from loaded mark state
        boolean auto_save_mark = mJsonDataHelper.getBooleanFromUserPreferences(USER_DATA_AUTO_SAVE_MARK, false);
        mAutoSaveCB.setChecked(auto_save_mark);
        mAutoSaveCB.setOnCheckedChangeListener((compoundButton, b) -> {
            mJsonDataHelper.setBooleanToUserPreferences(USER_DATA_AUTO_SAVE_MARK, b);
        });

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
                Uri uri = Uri.parse(mJsonDataHelper.getStringFromUserPreferences(USER_DATA_EXPORTED_FILE_PATH, ""));
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("application/json");
                startActivity(Intent.createChooser(shareIntent, null));
            } else {
                Toast.makeText(getContext(), R.string.sharing_file_empty_warning, Toast.LENGTH_SHORT).show();
            }
        });

        mExportDialogFragment = new ExportDialogFragment();
        mExportDialogFragment.setExportDialogListener(file_name -> {
            try {
                exportUserData(file_name);
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
                Toast.makeText(getContext(), R.string.no_anim_hint, Toast.LENGTH_SHORT).show();
            }

        });

        mImportBtn.setOnClickListener(view -> {
            importDataFile();
        });

        v.findViewById(R.id.init_temp_btn).setOnClickListener(view -> {
            mJsonDataHelper.setBooleanToUserPreferences(USER_DATA_RESET_MARK, true);
            getActivity().recreate();
//            Toast.makeText(getContext(), "模板初始化完成，请重新启动程序！", Toast.LENGTH_LONG).show();
        });
        /*
         * 导入数据 ->
         *   首先验证文件头(data_type:animation_temps)，为discboard文件
         *   如果是，那么删除所有temp_names，以及所有anim_dots，之后读入新文件内容
         * */
        mImportData = registerForActivityResult(new ActivityResultContracts.GetContent(),
                selectedFileUri -> {
                    // bug: if this line not added, the locale will return to default
                    mJsonDataHelper.initAppLocale();

                    if (null != selectedFileUri) {
                        // check file type
                        // read json data from external file
                        String json_s = mJsonDataHelper.readFromExternalFile(selectedFileUri);
                        // LACK FILE TYPE VERIFYING
                        try {
                            JSONArray ja_whole = new JSONArray(json_s);
                            JSONObject jo_head = new JSONObject(String.valueOf(ja_whole.get(ExportFileIndex.Head)));
                            Log.d(TAG, "onCreateView: " + ja_whole);
                            Log.d(TAG, "onCreateView: " + jo_head);
                            // verify file version
                            if (JsonDataHelper.getFileVersion(jo_head).equals(IO_HEAD_VERSION_1_1)) {
                                JSONArray ja_anim_temp_list = (JSONArray) new JSONObject(String.valueOf(ja_whole.get(ExportFileIndex.TempList))).get(IO_ANIM_TEMP_LIST);// get anim_temp_name_list
                                JSONArray ja_anim_dots_list = (JSONArray) new JSONObject(String.valueOf(ja_whole.get(ExportFileIndex.AniDotsList))).get(IO_ANIM_DOTS_LIST);// get anim_dots_list

                                int temp_counter = 0;// record the current temp position
                                while (temp_counter < ja_anim_temp_list.length()) {
                                    JSONObject jo_anim_dots_list = ja_anim_dots_list.getJSONObject(temp_counter);

                                    String temp_name = jo_anim_dots_list.keys().next();

                                    // dealing with JSONArray data
                                    JSONArray ja_anim_dots = (JSONArray) jo_anim_dots_list.get(temp_name);
                                    // turn json string to anim_dots_lists
                                    AnimTemp animTemp = mJsonDataHelper.JSONArray2ArrayListHashtableNew(ja_anim_dots);
                                    ArrayList<Hashtable<String, Dot>> anim_dots_list = animTemp.getAnimDotsList();
                                    ArrayList<Hashtable<String, InterDot>> inter_dots_list = animTemp.getInterDotsList();

                                    // add data to pref
                                    // check the possibility of name duplication
                                    while (mJsonDataHelper.checkNameDuplication(temp_name)) {
                                        temp_name = temp_name + TEMP_DUPLICATION_SUFFIX;
                                    }
                                    mJsonDataHelper.saveAnimDotsToPrefNew(temp_name, anim_dots_list, inter_dots_list);
                                    mJsonDataHelper.addAniTempToPref(temp_name);
                                    temp_counter++;
                                }
                                Toast.makeText(getContext(), R.string.importing_success_hint, Toast.LENGTH_LONG).show();

                                // 重新读取列表
                                fetchTempList();
                            }
                            else if (JsonDataHelper.getFileVersion(jo_head).equals(IO_HEAD_VERSION_1_2)) {
                                // fetch width & height from JSONArray
                                JSONArray ja_board_measure = (JSONArray) new JSONObject(String.valueOf(ja_whole.get(ExportFileIndex.BoardMeasure))).get(IO_BOARD_MEASURE);// get anim_temp_name_list
                                float width = (float) ja_board_measure.getDouble(0);
                                float height = (float) ja_board_measure.getDouble(1);
                                // fetch target_width & target_height from current
                                float target_width = mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_WIDTH, 0f);
                                float target_height = mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_HEIGHT, 0f);

                                // for testing
//                                    if(target_width == 0f || target_height == 0f){
//                                        Log.d(TAG, "获取本机战术板尺寸失败");
//                                    }

                                float W_ratio = target_width / width;
                                float H_ratio = target_height / height;

                                JSONArray ja_anim_temp_list = (JSONArray) new JSONObject(String.valueOf(ja_whole.get(ExportFileIndex.TempList))).get(IO_ANIM_TEMP_LIST);// get anim_temp_name_list
                                JSONArray ja_anim_dots_list = (JSONArray) new JSONObject(String.valueOf(ja_whole.get(ExportFileIndex.AniDotsList))).get(IO_ANIM_DOTS_LIST);// get anim_dots_list

                                int temp_counter = 0;// record the current temp position
                                while (temp_counter < ja_anim_temp_list.length()) {
                                    JSONObject jo_anim_dots_list = ja_anim_dots_list.getJSONObject(temp_counter);

                                    String temp_name = jo_anim_dots_list.keys().next();

                                    // dealing with JSONArray data
                                    JSONArray ja_anim_dots = (JSONArray) jo_anim_dots_list.get(temp_name);
                                    // turn json string to anim_dots_lists
                                    AnimTemp animTemp = mJsonDataHelper.JSONArray2ArrayListHashtableNew(ja_anim_dots);
                                    ArrayList<Hashtable<String, Dot>> anim_dots_list = animTemp.getAnimDotsList();
                                    ArrayList<Hashtable<String, InterDot>> inter_dots_list = animTemp.getInterDotsList();

                                    // 1st updated in VER_1_2, scale dots' pos by ratio
                                    for(Hashtable<String, Dot> hashtable : anim_dots_list){
                                        hashtable.forEach((id, dot) -> dot.scaleByRatio(W_ratio, H_ratio));
                                    }
                                    for(Hashtable<String, InterDot> hashtable : inter_dots_list){
                                        hashtable.forEach((id, inter_dot) -> inter_dot.scaleByRatio(W_ratio, H_ratio));
                                    }

                                    // add data to pref
                                    // check the possibility of name duplication
                                    while (mJsonDataHelper.checkNameDuplication(temp_name)) {
                                        temp_name = temp_name + TEMP_DUPLICATION_SUFFIX;
                                    }
                                    mJsonDataHelper.saveAnimDotsToPrefNew(temp_name, anim_dots_list, inter_dots_list);
                                    mJsonDataHelper.addAniTempToPref(temp_name);
                                    temp_counter++;
                                }
//                                    Log.d(TAG, "onCreateView: " + "导入成功");
                                Toast.makeText(getContext(), R.string.importing_success_hint, Toast.LENGTH_LONG).show();

                                // 重新读取列表
                                fetchTempList();
                            }
                            else {
                                Toast.makeText(getContext(), R.string.data_file_incompatible_error, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

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
                AnimTemp animTemp = mJsonDataHelper.getAnimDotsFromPref(tempName);
                ArrayList<Hashtable<String, Dot>> anim_dots = animTemp.getAnimDotsList();
                ArrayList<Hashtable<String, InterDot>> inter_dots = animTemp.getInterDotsList();
                String json_anim = mJsonDataHelper.transformData2Json(anim_dots);
                String json_inter = mJsonDataHelper.transformData2Json(inter_dots);

                JSONArray ja_anim = new JSONArray(json_anim);
                JSONArray ja_inter = new JSONArray(json_inter);

                JSONArray ja_dots = JsonDataHelper.mergeAnimInterJsonArray(ja_anim, ja_inter);
//                Log.d(TAG, "exportUserData: " + ja_dots);
                JSONObject jo_dots = new JSONObject();
                jo_dots.put(tempName, ja_dots);
                ja_anim_dots_list.put(jo_dots);

                i++;
            }
            JSONArray ja_whole = new JSONArray();

            // apk version
            JSONObject jo_head = new JSONObject();
            jo_head.put(IO_HEAD, IO_HEAD_VERSION_1_2);
            ja_whole.put(jo_head);

            // temp list
            JSONObject jo_anim_temp_list = new JSONObject();
            jo_anim_temp_list.put(IO_ANIM_TEMP_LIST, ja_anim_temp_list);
            ja_whole.put(jo_anim_temp_list);

            // ani_dots list
            JSONObject jo_anim_dots_list = new JSONObject();
            jo_anim_dots_list.put(IO_ANIM_DOTS_LIST, ja_anim_dots_list);
            ja_whole.put(jo_anim_dots_list);

            // board width & height
            JSONObject jo_board_measure = new JSONObject();
            ArrayList<Float> board_measure_list = new ArrayList<>();
            board_measure_list.add(mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_WIDTH, 0f));
            board_measure_list.add(mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_HEIGHT, 0f));
            JSONArray ja_board_measure = new JSONArray(board_measure_list);
            jo_board_measure.put(IO_BOARD_MEASURE, ja_board_measure);
            ja_whole.put(jo_board_measure);

            mJsonDataHelper.writeToExternalFile(ja_whole.toString(), file_name);
        } else {
            Toast.makeText(getContext(), R.string.no_anim_hint, Toast.LENGTH_SHORT).show();
        }
    }

    // canvas_bg spinner
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mCanvasTypeS = String.valueOf(adapterView.getItemAtPosition(i));
//        // canvas_bg write in
//        mJsonDataHelper.setStringToUserPreferences(USER_DATA_CANVAS_BG_TYPE, CanvasBGType.DISC_FULL);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onPause() {
        super.onPause();

        // save the canvas_bg_type to the preferences
        mJsonDataHelper.setStringToUserPreferences(USER_DATA_CANVAS_BG_TYPE, mCanvasTypeS);
    }

    void initCanvasBGSpinner(){
        // canvas_bg
        // 场地背景类别的下拉菜单
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.canvas_bg_array, R.layout.canvas_bg_type_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCanvasBGSpinner.setAdapter(adapter);
        mCanvasBGSpinner.setOnItemSelectedListener(this);
        // set the default value of the spinner

        if(mCanvasBGSpinner != null){
            mCanvasTypeS = mJsonDataHelper.getStringFromUserPreferences(USER_DATA_CANVAS_BG_TYPE, "");
            ArrayAdapter arrayAdapter = (ArrayAdapter) mCanvasBGSpinner.getAdapter();
            int pos = arrayAdapter.getPosition(mCanvasTypeS);
            mCanvasBGSpinner.setSelection(pos);
        }
        else{
            Log.e(TAG, "CanvasBGSpinner 为 null!", new NullPointerException());
        }
    }

    void initLangBtn(){
        if(mJsonDataHelper.getStringFromUserPreferences(LOCALE_CODE, LocaleType.CN).equals(LocaleType.CN))
            mLangBtn.setBackgroundResource(R.drawable.cn_lang);
        else
            mLangBtn.setBackgroundResource(R.drawable.en_lang);
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
                    .setPositiveButton(R.string.export_string, (dialogInterface, i) -> {
                        String file_name = mExportNameInput.getText().toString();

                        mExportDialogListener.onExportListener(file_name);
                    })
                    .setNegativeButton(R.string.cancel_string, (dialogInterface, i) -> {
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