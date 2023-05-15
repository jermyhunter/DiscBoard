package com.example.discboard.fragments;

import static android.app.AlertDialog.*;

import static com.example.discboard.DiscFinal.AUTO_SAVE_DELAY;
import static com.example.discboard.DiscFinal.TEMP_DUPLICATION_SUFFIX;
import static com.example.discboard.DiscFinal.NORMAL_ALPHA;
import static com.example.discboard.DiscFinal.PRESSED_ALPHA;
import static com.example.discboard.DiscFinal.USER_DATA_AUTO_SAVE_MARK;
import static com.example.discboard.DiscFinal.USER_DATA_BOARD_HEIGHT;
import static com.example.discboard.DiscFinal.USER_DATA_BOARD_WIDTH;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discboard.DiscFinal;
import com.example.discboard.JsonDataHelper;
import com.example.discboard.adapter.AnimTempItemAdapter;
import com.example.discboard.R;
import com.example.discboard.dialogs.UnsavedCheckDialog;
import com.example.discboard.views.AnimatedDiscBoard;
import com.example.discboard.dialogs.SelectTempDialog;
import com.example.discboard.views.Sketchpad;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Objects;

/**
 * this fragment is for animation template demonstration
 * */
public class AnimatedBoardFragment extends Fragment {
    String TAG = "AnimatedBoardFragment";
    PaletteButtonSet mPaletteButtonSet;
    static class PaletteButtonSet{
        ImageButton[] paletteButtons;
        ImageButton currentPalette;
        public PaletteButtonSet(ImageButton[] imageButtons){
            paletteButtons = imageButtons;
        }

        public void setCurrentPalette(int paintType) {
            if(this.currentPalette != null)
                this.currentPalette.setAlpha(0.3f);

            paletteButtons[paintType].setAlpha(1f);
            this.currentPalette = paletteButtons[paintType];
        }
    }

    private void initPaletteButtonSet(View v) {
        ImageButton[] imageButtons = new ImageButton[5];
        imageButtons[0] = v.findViewById(R.id.red_palette);
        imageButtons[1] = v.findViewById(R.id.blue_palette);
        imageButtons[2] = v.findViewById(R.id.orange_palette);
        imageButtons[3] = v.findViewById(R.id.white_palette);
        imageButtons[4] = v.findViewById(R.id.black_palette);
        for(int i = 0; i < 5; i++){
            int finalI = i;
            imageButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSketchpad.setPaintType(finalI);
                    mPaletteButtonSet.setCurrentPalette(finalI);
                }
            });
        }

        mPaletteButtonSet = new PaletteButtonSet(imageButtons);
        mPaletteButtonSet.setCurrentPalette(DiscFinal.PaintType.Red.getValue());
    }

    JsonDataHelper mJsonDataHelper;
    static AnimatedDiscBoard mAnimBoard;
    Animation mAnimSlideInSketch, mAnimSlideOutSketch,
            mAnimSlideInBoard, mAnimSlideOutBoard;
    // sketch pad related
    View mSliderButtonsLayout, mPaintButtonsLayout;
    Sketchpad mSketchpad;
    ImageButton mPaintBtn, mEraseBtn;
    Button mPaintSwitch, mReturnBtn;
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
    static ArrayList<String> mAniTempList;
    Slider mFrameSlider;
    private boolean mDelPressFlag;

    // auto-save thread
    static Handler mHandler;// handling runnable threads
    static Runnable mAutoSaveR;
    public AnimatedBoardFragment() {
        // Required empty public constructor
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
        // unlock load_btn
        mLoadTempDialogFragment.setLoadTempDialogListener(() -> mLoadTempBtn.setEnabled(true));

        mHandler = new Handler();

        mUCLoadDialog = new UnsavedCheckDialog(getContext());
        mUCLoadDialog.setUnsavedDialogListener(() -> {
            // unsaved hint
            showLoadSelector();
        });

        mUCCreateDialog = new UnsavedCheckDialog(getContext());
        mUCCreateDialog.setUnsavedDialogListener(() -> {
            // unsaved hint
            mSelectTempDialog.show();
        });
        // initiated as "", for unsaved hint use
        mTempName = "";

        // whether to enable the auto-save function
        mAutoSaveMark = mJsonDataHelper.getBooleanFromUserPreferences(USER_DATA_AUTO_SAVE_MARK, false);
    }

    private void showLoadSelector() {
        mLoadTempBtn.setEnabled(false);
        mLoadTempDialogFragment.show(getChildFragmentManager(), "读取战术模板");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_anim_board, container, false);

        initAnimation();
        initLayoutSlideAnim();
        initPaletteButtonSet(v);

        mAnimBoard = v.findViewById(R.id.animated_discboard);
        mSliderButtonsLayout = v.findViewById(R.id.slider_buttons_layout);
        initPaintLayout(v);
        String bgType = mJsonDataHelper.initBGByUserData(mAnimBoard);
        // if bg_img is not disc, then resize it to 1:2
        if(!(bgType.equals(getContext().getResources().getString(R.string.disc_full)) || bgType.equals(getContext().getResources().getString(R.string.disc_endzone)))){
            mAnimBoard.post(() -> {
                // resize board to 1:2
                int boardWidth = mAnimBoard.getWidth();
                int boardHeight = mAnimBoard.getHeight();
                //        Log.d(TAG, "surfaceViewWidth: " + surfaceViewWidth);
                float boardProportion = (float) boardWidth / (float) boardHeight;

                // Get the SurfaceView layout parameters
                ViewGroup.LayoutParams lp = mAnimBoard.getLayoutParams();
                // if board's width is less than twice the height
                // then the board is too fat, set the height half the width
                if (2f >= boardProportion) {
                    lp.width = boardWidth;
                    lp.height = boardWidth / 2;
                }
                // if board's width is more than twice the height
                // then the board is too narrow, so cut the width off, set the width twice the height
                else {
                    lp.width = boardHeight * 2;
                    lp.height = boardHeight;
                }
                // Commit the layout parameters
                mAnimBoard.setLayoutParams(lp);
            });
            mSketchpad.post(() -> {
                // resize board to 1:2
                int boardWidth = mAnimBoard.getWidth();
                int boardHeight = mAnimBoard.getHeight();
                //        Log.d(TAG, "surfaceViewWidth: " + surfaceViewWidth);
                float boardProportion = (float) boardWidth / (float) boardHeight;

                // Get the SurfaceView layout parameters
                ViewGroup.LayoutParams lp = mSketchpad.getLayoutParams();
                // if board's width is less than twice the height
                // then the board is too fat, set the height half the width
                if (2f >= boardProportion) {
                    lp.width = boardWidth;
                    lp.height = boardWidth / 2;
                }
                // if board's width is more than twice the height
                // then the board is too narrow, so cut the width off, set the width twice the height
                else {
                    lp.width = boardHeight * 2;
                    lp.height = boardHeight;
                }
                // Commit the layout parameters
                mSketchpad.setLayoutParams(lp);
            });
        }

        storeBoardMeasure();

        mSaveOldTempBtn = v.findViewById(R.id.save_old_temp_btn);
        mLoadTempBtn = v.findViewById(R.id.load_temp_btn);

        mSelectTempDialog = new SelectTempDialog(Objects.requireNonNull(getContext()));
        mSelectTempDialog.setOnSelectListener(new SelectTempDialog.OnSelectListener() {
            @Override
            public void onPressMy() {
                mAnimBoard.loadMyPreset();
            }

            @Override
            public void onPress3() {
                mAnimBoard.load3Preset();
            }

            @Override
            public void onPress5() {
                mAnimBoard.load5Preset();
            }

            @Override
            public void onPressHo() {
                mAnimBoard.loadHostackPreset();
            }

            @Override
            public void onPressVert() {
                mAnimBoard.loadVerstackPreset();
            }

            @Override
            public void onPress() {
                // auto-save hint
                mAnimBoard.resetSavedFlag();
                setLoadedMarkAndTempName(false, "");
                mSelectTempDialog.dismiss();
            }
        });

        // template selection dialog
        v.findViewById(R.id.select_temp_btn).setOnClickListener(view -> {
//            mSelectTempDialogFragment.show(getChildFragmentManager(), "选择模板");
            // unsaved hint
            if(mAnimBoard.isSaved()) {
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
                Toast.makeText(getActivity(), R.string.empty_list_warning, Toast.LENGTH_SHORT).show();
            }
            else {
                // unsaved hint
                if(mAnimBoard.isSaved()) {
                    showLoadSelector();
                }
                else {
                    mUCLoadDialog.show();
                }
            }
        });

        // 如果读取原有战术，那么添加“保存”选项；如果是新创建的战术，不显示“保存”选项
        mSaveOldTempBtn.setOnClickListener(view -> {
            if(isLoaded()) {
                mAnimBoard.saveAniDots(mTempName);
                Toast.makeText(getContext(), R.string.save_success_hint, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), R.string.save_failure_warning, Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.save_new_temp_btn).setOnClickListener(view -> {
            mSaveDialogFragment = new SaveDialogFragment(mTempName);
            mSaveDialogFragment.setSaveDialogListener(tempName -> {
                setLoadedMarkAndTempName(true, tempName);
                Toast.makeText(getContext(), R.string.save_as_success_hint, Toast.LENGTH_SHORT).show();
            });
            mSaveDialogFragment.show(getChildFragmentManager(), "保存模板");
        });

        mInsertFrameBtn = v.findViewById(R.id.insert_frame_btn);
        mInsertFrameBtn.setOnClickListener(view -> {
            mAnimBoard.insertFrame();
        });

        mDelFrameBtn = v.findViewById(R.id.del_frame_btn);
        mDelFrameBtn.setOnLongClickListener(view -> {
            // after pressing the delete_btn, shorten the text
            if(!mDelPressFlag) {
                mDelFrameBtn.setText(R.string.del_frame_string);
                mDelPressFlag = true;
            }
            view.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.del_anim));
            mAnimBoard.deleteFrame();

            return false;
        });

        v.findViewById(R.id.play_anim_btn).setOnClickListener(view -> {
            mAnimBoard.startAnimationPlaying();
        });

        // last/next frame btn
        mLastFrameBtn = v.findViewById(R.id.last_frame_btn);
        mLastFrameBtn.setOnClickListener(view -> {
            int frameNo = mAnimBoard.getCurrentFrameNo() - 1;
            if(frameNo >= 0) {
                mAnimBoard.loadFrame(frameNo);
            }
        });

        mNextFrameBtn = v.findViewById(R.id.next_frame_btn);
        mNextFrameBtn.setOnClickListener(view -> {
            int frameNo = mAnimBoard.getCurrentFrameNo() + 1;
            if(frameNo < mAnimBoard.getFrameSum()) {
                mAnimBoard.loadFrame(frameNo);
            }
        });

        mFrameSlider = v.findViewById(R.id.frame_slider);

        //the Max of seekbar can't be 0, so that hide the seekbar when UI init
        setFrameSliderVisible(false);

        mAnimBoard.setAnimDiscBoardListener(new AnimatedDiscBoard.AnimDiscBoardListener() {
            @Override
            public void onFrameSumChange() {
                if(mAnimBoard.getFrameSum() == 1){
                    setFrameSliderVisible(false);
                }
                else if(mAnimBoard.getFrameSum() >= 2) {
                    // NOTICE: the ValueFrom would start from 0.
                    // So for better user exp, in this proj, ValueFrom set to 1.0
                    // and the FrameSum is always Above 1
                    // Besides, the getCurrentFrameNo is also start from 0, so it needs to be +1
                    mFrameSlider.setValueTo(mAnimBoard.getFrameSum());
                    setFrameSliderVisible(true);
                }
            }

            // 1.change the slider value
            @Override
            public void onCurrentFrameNoChange() {
                mFrameSlider.setValue(mAnimBoard.getCurrentFrameNo() + 1);
            }

            @Override
            public void onLoad() {
                setLoadedMarkAndTempName(true, mAnimBoard.getTempName());
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
                mHintTxt.setText(R.string.del_frame_success_hint);
                mHintLayout.startAnimation(mAnimFade);
            }
        });

        mFrameSlider.addOnChangeListener(new Slider.OnChangeListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
//                Log.d(TAG, "onValueChange: " + value);
                mAnimBoard.loadFrame((int)value - 1);
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
                    if (!mAnimBoard.isSaved()) {
                        mAnimBoard.saveAniDots(mTempName);
                        // auto-save message
//                        Toast.makeText(getContext(), mTempName + " " + getString(R.string.auto_save_success_hint), Toast.LENGTH_SHORT).show();

                        // auto-save success hint with animation
                        mHintTxt.setText(R.string.auto_save_as_success_hint);
                        mHintLayout.startAnimation(mAnimFade);

                        mAnimBoard.setSavedFlag();
                    }
                    mHandler.postDelayed(this, AUTO_SAVE_DELAY);
                }
            }
        };

        // Inflate the layout for this fragment
        return v;
    }

    private void initPaintLayout(View v) {
        mSketchpad = v.findViewById(R.id.sketchpad);
        mPaintButtonsLayout = v.findViewById(R.id.paint_buttons_layout);
        mPaintBtn = v.findViewById(R.id.paint_btn);
        mEraseBtn = v.findViewById(R.id.erase_btn);
        mReturnBtn = v.findViewById(R.id.return_btn);
        mPaintSwitch = v.findViewById(R.id.paint_switch);
        v.findViewById(R.id.clear_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSketchpad.clearAll();
            }
        });

        // from sketchpad to board
        mReturnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBoard();
            }
        });

        // grab board snapshot to sketchpad
        mPaintSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load sketchpad using board canvas
                Bitmap b = DiscFinal.loadBitmapFromView(mAnimBoard);
                BitmapDrawable bd = new BitmapDrawable(getResources(), b);
                mSketchpad.setBackground(bd);
//                mSketchpad.clearAll();

                showSketchpad();
            }
        });

        mPaintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSketchpad.startPainting();
                mPaintBtn.setBackgroundResource(R.drawable.pen_focus);
                mEraseBtn.setBackgroundResource(R.drawable.eraser_normal);
            }
        });

        mEraseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSketchpad.startErasing();
                mPaintBtn.setBackgroundResource(R.drawable.pen_normal);
                mEraseBtn.setBackgroundResource(R.drawable.eraser_focus);
            }
        });

        v.findViewById(R.id.revoke_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSketchpad.revokeAction();
            }
        });

        v.findViewById(R.id.redo_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSketchpad.redoAction();
            }
        });
    }

    // layout slide animation
    public void initLayoutSlideAnim(){
        mAnimSlideInSketch = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_to_right);
        mAnimSlideOutSketch = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_bottom);
        mAnimSlideInSketch.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mPaintButtonsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAnimSlideOutSketch.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mPaintButtonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mAnimSlideInBoard = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_to_right);
        mAnimSlideOutBoard = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_bottom);
        mAnimSlideInBoard.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mSliderButtonsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAnimSlideOutBoard.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mSliderButtonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showBoard() {
        mSliderButtonsLayout.startAnimation(mAnimSlideInBoard);
        mAnimBoard.setVisibility(View.VISIBLE);
        mPaintButtonsLayout.startAnimation(mAnimSlideOutSketch);
        mSketchpad.setVisibility(View.GONE);

        mPaintSwitch.setEnabled(true);
        mReturnBtn.setEnabled(false);
    }

    private void showSketchpad() {
        mSliderButtonsLayout.startAnimation(mAnimSlideOutBoard);
        mAnimBoard.setVisibility(View.GONE);
        mPaintButtonsLayout.startAnimation(mAnimSlideInSketch);
        mSketchpad.setVisibility(View.VISIBLE);

        mPaintSwitch.setEnabled(false);
        mReturnBtn.setEnabled(true);
        // init button's background
        mPaintBtn.setBackgroundResource(R.drawable.pen_focus);
        mEraseBtn.setBackgroundResource(R.drawable.eraser_normal);
    }

    private void storeBoardMeasure() {
        if(mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_WIDTH, 0f) == 0f ||
                mJsonDataHelper.getFloatFromUserPreferences(USER_DATA_BOARD_HEIGHT, 0f) == 0f){
            // store this phone's board measure on the first load
            mAnimBoard.post(() -> {
//                Log.d(TAG, "storeBoardSize: width" + mAnimBoard.getWidth());
//                Log.d(TAG, "storeBoardSize: height" + mAnimBoard.getHeight());
                mJsonDataHelper.setFloatToUserPreferences(USER_DATA_BOARD_WIDTH, mAnimBoard.getWidth());
                mJsonDataHelper.setFloatToUserPreferences(USER_DATA_BOARD_HEIGHT, mAnimBoard.getHeight());
            });
        }
    }

    void initAnimation(){
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
        mAnimBoard.setSavedFlag();
        // adjust save_old_btn state
        setSaveOldBtnState(loadedMark);
    }

    public boolean isLoaded(){
        return mLoadedMark;
    }

    private void setSaveOldBtnState(boolean b) {
        if(b){
            mSaveOldTempBtn.setVisibility(View.VISIBLE);
//            mSaveOldTempBtn.setEnabled(true);
        }
        else {
            mSaveOldTempBtn.setVisibility(View.GONE);
//            mSaveOldTempBtn.setEnabled(false);
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
        String mTempName;// save the temp_name for quick edit
        public SaveDialogFragment(String tempName){
            this.mTempName = tempName;
        }
        @NonNull
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
            mSaveNameInput.setText(mTempName);

            builder.setView(dialogView)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        String temp_name = mSaveNameInput.getText().toString();

                        // check the possibility of name duplication
                        while (mJsonDataHelper.checkNameDuplication(temp_name)){
                            temp_name = temp_name + TEMP_DUPLICATION_SUFFIX;
                        }
                        mAnimBoard.saveAniDots(temp_name);
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
//                    .setPositiveButton("确定", (dialogInterface, i) -> mAnimBoard.saveAniDotsToPref(.getText().toString()));

            builder.setView(dialogView);
            return builder.create();
        }

        public void onItemClick(int position) {
            String name = mAnimTempItemAdapter.getData(position);
//            Log.d(TAG, "onAniTempClick: " + name);
            mAnimBoard.loadDotsAndUpdateUI(name);
            // auto-save hint
            mAnimBoard.setSavedFlag();

            mListener.onLoad();
            dismiss();
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {
            super.onCancel(dialog);

            mListener.onLoad();
        }

        LoadTempDialogListener mListener;

        public void setLoadTempDialogListener(LoadTempDialogListener listener) {
            mListener = listener;
        }

        public interface LoadTempDialogListener{
            void onLoad();
        }
    }
}