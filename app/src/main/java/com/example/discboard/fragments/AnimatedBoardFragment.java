package com.example.discboard.fragments;

import static android.app.AlertDialog.*;

import static com.example.discboard.DiscFinal.AUTO_SAVE_DELAY;
import static com.example.discboard.DiscFinal.TEMP_DUPLICATION_SUFFIX;
import static com.example.discboard.DiscFinal.NORMAL_ALPHA;
import static com.example.discboard.DiscFinal.PRESSED_ALPHA;
import static com.example.discboard.DiscFinal.USER_DATA_AUTO_SAVE_MARK;
import static com.example.discboard.DiscFinal.USER_DATA_BOARD_HEIGHT;
import static com.example.discboard.DiscFinal.USER_DATA_BOARD_WIDTH;
import static com.example.discboard.DiscFinal.USER_DATA_CANVAS_BG_TYPE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discboard.DiscFinal;
import com.example.discboard.JsonDataHelper;
import com.example.discboard.adapter.AnimTempItemAdapter;
import com.example.discboard.R;
import com.example.discboard.dialogs.UnsavedCheckDialog;
import com.example.discboard.views.AnimatedDiscBoard;
import com.example.discboard.dialogs.SelectTempDialog;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Objects;

/**
 * this fragment is for animation template demonstration
 * */
public class AnimatedBoardFragment extends Fragment {
    String TAG = "AnimatedBoardFragment";
    // true: if one saved template has been loaded
    // it determines whether auto-save function is to be turned on
    Boolean mLoadedMark;
    Boolean mAutoSaveMark;
    UnsavedCheckDialog mUCLoadDialog, mUCCreateDialog;
    // to store the name of already loaded template animation
    // if mTempName == "", then it's a newly created temp
    // when user switching, then give a unsaved hint
    String mTempName;
    Button mSaveOldTempBtn, mLoadTempBtn, mDelFrameBtn, mInsertFrameBtn,
        mLastFrameBtn, mNextFrameBtn;
    TextView mHintTxt;
    View mHintLayout;
    AnimationSet mAnimFade;
    JsonDataHelper mJsonDataHelper;
    static ArrayList<String> mAniTempList;
    Slider mFrameSlider;
    static AnimatedDiscBoard mAnimatedDiscBoard;
    private boolean mDelPressFlag;

    static Handler mHandler;// handling runnable threads
    static Runnable mAutoSaveR;
    public AnimatedBoardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AnimaBoardFragment.
     */
    public static AnimatedBoardFragment newInstance(String param1, String param2) {
        AnimatedBoardFragment fragment = new AnimatedBoardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    SelectTempDialog mSelectTempDialog;
    SaveDialogFragment mSaveDialogFragment;
//    LoadTempDialog mLoadTempDialog;
    LoadTempDialogFragment mLoadTempDialogFragment;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJsonDataHelper = new JsonDataHelper(getContext());
        mDelPressFlag = false;

        mLoadTempDialogFragment = new LoadTempDialogFragment();

        mHandler = new Handler();

        mUCLoadDialog = new UnsavedCheckDialog(getContext(), "");
        mUCLoadDialog.setUnsavedDialogListener(() -> {
            // unsaved hint
            mLoadTempDialogFragment.show(getChildFragmentManager(), "读取战术模板");
        });

        mUCCreateDialog = new UnsavedCheckDialog(getContext(), "");
        mUCCreateDialog.setUnsavedDialogListener(() -> {
            // unsaved hint
            mSelectTempDialog.show();
        });
        // initiated as "", for unsaved hint use
        mTempName = "";

        // whether to enable the auto-save function
        mAutoSaveMark = mJsonDataHelper.getBooleanFromUserPreferences(USER_DATA_AUTO_SAVE_MARK, false);

        InitAnimation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_anim_board, container, false);

        mAnimatedDiscBoard = v.findViewById(R.id.animated_discboard);
        mJsonDataHelper.initBGByUserData(mAnimatedDiscBoard);
        storeBoardMeasure();

        mSaveOldTempBtn = v.findViewById(R.id.save_old_temp_btn);
        mLoadTempBtn = v.findViewById(R.id.load_temp_btn);

        mSelectTempDialog = new SelectTempDialog(Objects.requireNonNull(getContext()));
        mSelectTempDialog.setOnSelectListener(new SelectTempDialog.OnSelectListener() {
            @Override
            public void onPressMy() {
                mAnimatedDiscBoard.loadMyPreset();
            }

            @Override
            public void onPress3() {
                mAnimatedDiscBoard.load3Preset();
            }

            @Override
            public void onPress5() {
                mAnimatedDiscBoard.load5Preset();
            }

            @Override
            public void onPressHo() {
                mAnimatedDiscBoard.loadHostackPreset();
            }

            @Override
            public void onPressVert() {
                mAnimatedDiscBoard.loadVerstackPreset();
            }

            @Override
            public void onPress() {
                // auto-save hint
                mAnimatedDiscBoard.resetSavedFlag();
                setLoadedMarkAndTempName(false, "");
                mSelectTempDialog.dismiss();
            }
        });

        // template selection dialog
        v.findViewById(R.id.select_temp_btn).setOnClickListener(view -> {
//            mSelectTempDialogFragment.show(getChildFragmentManager(), "选择模板");
            // unsaved hint
            if(mAnimatedDiscBoard.isSaved()) {
                mSelectTempDialog.show();
            }
            else {
                mUCCreateDialog.show();
            }
        });

        // load temp
        mLoadTempBtn.setOnClickListener(view -> {
            mAniTempList = mJsonDataHelper.loadTempNamesFromPref();
            if(mAniTempList == null || mAniTempList.size() == 0){
                Toast.makeText(getActivity(), "请先创建新的战术！", Toast.LENGTH_SHORT).show();
            }
            else {
                // unsaved hint
                if(mAnimatedDiscBoard.isSaved()) {
                    mLoadTempDialogFragment.show(getChildFragmentManager(), "读取战术模板");
                }
                else {
                    mUCLoadDialog.show();
                }
            }
        });

        // 如果读取原有战术，那么添加“保存”选项；如果是新创建的战术，不显示“保存”选项
        mSaveOldTempBtn.setOnClickListener(view -> {
            if(isLoaded()) {
                mAnimatedDiscBoard.saveAniDots(mTempName);
                Toast.makeText(getContext(), R.string.SAVE_SUCCESS_HINT, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "保存标记错误，出现bug！", Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.save_new_temp_btn).setOnClickListener(view -> {
            mSaveDialogFragment = new SaveDialogFragment();
            mSaveDialogFragment.setSaveDialogListener(tempName -> {
                setLoadedMarkAndTempName(true, tempName);
                Toast.makeText(getContext(), R.string.SAVE_AS_SUCCESS_HINT, Toast.LENGTH_SHORT).show();
            });
            mSaveDialogFragment.show(getChildFragmentManager(), "保存模板");
        });

        mInsertFrameBtn = v.findViewById(R.id.insert_frame_btn);
        mInsertFrameBtn.setOnClickListener(view -> {
            mAnimatedDiscBoard.insertFrame();
        });

        mDelFrameBtn = v.findViewById(R.id.del_frame_btn);
        mDelFrameBtn.setOnLongClickListener(view -> {
            // after pressing the delete_btn, shorten the text
            if(!mDelPressFlag) {
                mDelFrameBtn.setText("删除帧");
                mDelPressFlag = true;
            }
            view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.del_anim));
            mAnimatedDiscBoard.deleteFrame();

            return false;
        });

        v.findViewById(R.id.play_anim_btn).setOnClickListener(view -> {
            mAnimatedDiscBoard.startAnimationPlaying();
        });

        // last/next frame btn
        mLastFrameBtn = v.findViewById(R.id.last_frame_btn);
        mLastFrameBtn.setOnClickListener(view -> {
            int frameNo = mAnimatedDiscBoard.getCurrentFrameNo() - 1;
            if(frameNo >= 0) {
                mAnimatedDiscBoard.loadFrame(frameNo);
            }
        });

        mNextFrameBtn = v.findViewById(R.id.next_frame_btn);
        mNextFrameBtn.setOnClickListener(view -> {
            int frameNo = mAnimatedDiscBoard.getCurrentFrameNo() + 1;
            if(frameNo < mAnimatedDiscBoard.getFrameSum()) {
                mAnimatedDiscBoard.loadFrame(frameNo);
            }
        });

        mFrameSlider = v.findViewById(R.id.frame_slider);

        //the Max of seekbar can't be 0, so that hide the seekbar when UI init
        setFrameSliderVisible(false);

        mAnimatedDiscBoard.setAnimDiscBoardListener(new AnimatedDiscBoard.AnimDiscBoardListener() {
            @Override
            public void onFrameSumChange() {
                if(mAnimatedDiscBoard.getFrameSum() == 1){
                    setFrameSliderVisible(false);
                }
                else if(mAnimatedDiscBoard.getFrameSum() >= 2) {
                    // NOTICE: the ValueFrom would start from 0.
                    // So for better user exp, in this proj, ValueFrom set to 1.0
                    // and the FrameSum is always Above 1
                    // Besides, the getCurrentFrameNo is also start from 0, so it needs to be +1
                    mFrameSlider.setValueTo(mAnimatedDiscBoard.getFrameSum());
                    setFrameSliderVisible(true);
                }
            }

            // 1.change the slider value
            @Override
            public void onCurrentFrameNoChange() {
                mFrameSlider.setValue(mAnimatedDiscBoard.getCurrentFrameNo() + 1);
            }

            @Override
            public void onLoad() {
                setLoadedMarkAndTempName(true, mAnimatedDiscBoard.getTempName());
//                Log.d(TAG, "mLoadedMark: " + mLoadedMark);
            }

            @Override
            public void onAnimationStart() {
                mLoadTempBtn.setEnabled(false);
                // lower the alpha value of the slider for better vision experience
                mFrameSlider.setAlpha(NORMAL_ALPHA);
                mLastFrameBtn.setAlpha(NORMAL_ALPHA);
                mNextFrameBtn.setAlpha(NORMAL_ALPHA);
            }

            @Override
            public void onAnimationStop() {
                mLoadTempBtn.setEnabled(true);
                // restore the alpha value
                mFrameSlider.setAlpha(PRESSED_ALPHA);
                mLastFrameBtn.setAlpha(PRESSED_ALPHA);
                mNextFrameBtn.setAlpha(PRESSED_ALPHA);
            }

            @Override
            public void onDeleteFrame() {
                // delete success hint with animation
                mHintTxt.setText(R.string.DEL_FRAME_SUCCESS_HINT);
                mHintLayout.startAnimation(mAnimFade);
            }
        });

        mFrameSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
//                Log.d(TAG, "onValueChange: " + value);
                mAnimatedDiscBoard.loadFrame((int)value - 1);
            }
        });
        mFrameSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                slider.setAlpha(PRESSED_ALPHA);
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                slider.setAlpha(NORMAL_ALPHA);
            }
        });

        mHintTxt = v.findViewById(R.id.hint_txt);
        mHintLayout = v.findViewById(R.id.hint_bg);

        mLoadedMark = false;
        setLoadedMarkAndTempName(false, "");
        mAutoSaveR = new Runnable() {
            public void run() {
                if(mAutoSaveMark) {
                    // unsaved hint
                    if (!mAnimatedDiscBoard.isSaved()) {
                        mAnimatedDiscBoard.saveAniDots(mTempName);
                        // auto-save message
                        Toast.makeText(getContext(), mTempName + " 自动保存成功", Toast.LENGTH_SHORT).show();

                        // auto-save success hint with animation
                        mHintTxt.setText(R.string.AUTO_SAVE_SUCCESS_HINT);
                        mHintLayout.startAnimation(mAnimFade);

                        mAnimatedDiscBoard.setSavedFlag();
                    }
                    mHandler.postDelayed(this, AUTO_SAVE_DELAY);
                }
            }
        };

        // Inflate the layout for this fragment
        return v;
    }

    private void storeBoardMeasure() {
        if(mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_WIDTH, 0f) == 0f ||
                mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_HEIGHT, 0f) == 0f){
            // store this phone's board measure on the first load
            mAnimatedDiscBoard.post(() -> {
//                Log.d(TAG, "storeBoardSize: width" + mAnimatedDiscBoard.getWidth());
//                Log.d(TAG, "storeBoardSize: height" + mAnimatedDiscBoard.getHeight());
                mJsonDataHelper.setFloatToUserPreferences(USER_DATA_BOARD_WIDTH, mAnimatedDiscBoard.getWidth());
                mJsonDataHelper.setFloatToUserPreferences(USER_DATA_BOARD_HEIGHT, mAnimatedDiscBoard.getHeight());
            });
        }

    }

    void InitAnimation(){
        Animation animationIn = AnimationUtils.loadAnimation(getContext(),
                R.anim.fade_in);
        Animation animationOut = AnimationUtils.loadAnimation(getContext(),
                R.anim.fade_out);

        animationIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mHintLayout.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mHintLayout.startAnimation(animationOut);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // the start offset of animation
//        animationIn.setStartOffset(2 * 1000);

        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mHintLayout.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mAnimFade = new AnimationSet(false); //change to false
        mAnimFade.addAnimation(animationIn);
        mAnimFade.addAnimation(animationOut);
    }

    void setLoadedMarkAndTempName(Boolean loadedMark, String tempName) {
        // if previous animation is from loaded, then stop auto-save
        if(mLoadedMark) {
            stopAutoSave();
        }

        // if current animation is from loaded, then start auto-save
        if(loadedMark){
            // if the names are the same, then refresh the counter
            if (tempName.equals(mTempName)) {
                stopAutoSave();
                startAutoSave();
            }
            else
                startAutoSave();
        }

        mTempName = tempName;
        mLoadedMark = loadedMark;
        // auto-save hint
        mAnimatedDiscBoard.setSavedFlag();
        // adjust save_old_btn state
        setSaveOldBtnState(loadedMark);
    }

    public boolean isLoaded(){
        return mLoadedMark;
    }

    private void setSaveOldBtnState(boolean b) {
        if(b){
            mSaveOldTempBtn.setVisibility(View.VISIBLE);
            mSaveOldTempBtn.setEnabled(true);
        }
        else {
            mSaveOldTempBtn.setVisibility(View.INVISIBLE);
            mSaveOldTempBtn.setEnabled(false);
        }
    }

    private void setFrameSliderVisible(boolean b) {
        if(mFrameSlider != null){
            if(b)
                mFrameSlider.setVisibility(View.VISIBLE);
            else
                mFrameSlider.setVisibility(View.INVISIBLE);
        }
    }

    static void startAutoSave(){
        mHandler.postDelayed(mAutoSaveR, AUTO_SAVE_DELAY);
    }
    void stopAutoSave(){
        mHandler.removeCallbacks(mAutoSaveR);
    }

    /**
     * stop auto-saving thread
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();

        stopAutoSave();
        // remove all
//        mHandler.removeCallbacks(mAutoSaveR, null);
    }

    public static class SaveDialogFragment extends DialogFragment {
        EditText mSaveNameInput;
        JsonDataHelper mJsonDataHelper;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mJsonDataHelper = new JsonDataHelper(getContext());
            // Use the Builder class for convenient dialog construction
            Builder builder = new Builder(getContext());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();// * 从 requireActivity() 改为 getActivity()

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View dialogView = inflater.inflate(R.layout.dialog_save_anim_temp, null);

            mSaveNameInput = dialogView.findViewById(R.id.anim_name_input);

            builder.setView(dialogView)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        String temp_name = mSaveNameInput.getText().toString();

                        // check the possibility of name duplication
                        while (mJsonDataHelper.checkNameDuplication(temp_name)){
                            temp_name = temp_name + TEMP_DUPLICATION_SUFFIX;
                        }
                        mAnimatedDiscBoard.saveAniDots(temp_name);
                        mSaveDialogListener.onSaveListener(temp_name);
                    });

            return builder.create();
        }

        SaveDialogListener mSaveDialogListener;

        public void setSaveDialogListener(SaveDialogListener saveDialogListener) {
            mSaveDialogListener = saveDialogListener;
        }

        public interface SaveDialogListener{
            void onSaveListener(String name);
        }
    }

    public static class LoadTempDialogFragment extends DialogFragment implements AnimTempItemAdapter.OnAniTempListener {
        RecyclerView recyclerView;
        AnimTempItemAdapter mAnimTempItemAdapter;
        static String TAG = "LoadTempDialogFragment";
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            Builder builder = new Builder(getContext());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();// * 从 requireActivity() 改为 getActivity()

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View dialogView = inflater.inflate(R.layout.dialog_load_anim_temp, null);

//            aniTempList = mAniTempList;
//            Log.d(TAG, "onCreateDialog: " + mAniTempList);
            recyclerView = dialogView.findViewById(R.id.recycler_view);
            mAnimTempItemAdapter = new AnimTempItemAdapter(mAniTempList, getContext(), this);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), getContext().getResources().getInteger(R.integer.anim_temp_item_span));

            recyclerView.setAdapter(mAnimTempItemAdapter);
            recyclerView.setLayoutManager(gridLayoutManager);

            recyclerView.setItemAnimator(new DefaultItemAnimator());
//            builder.setView(dialogView)
//                    .setPositiveButton("确定", (dialogInterface, i) -> mAnimatedDiscBoard.saveAniDotsToPref(.getText().toString()));

            builder.setView(dialogView);
            return builder.create();
        }

        public void onItemClick(int position) {
            String name = mAnimTempItemAdapter.getData(position);
//            Log.d(TAG, "onAniTempClick: " + name);
            mAnimatedDiscBoard.loadDotsAndUpdateUI(name);
            // auto-save hint
            mAnimatedDiscBoard.setSavedFlag();
            dismiss();
        }
    }
}