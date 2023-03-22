package com.example.discboard.fragments;

import static android.app.AlertDialog.*;

import static com.example.discboard.DiscFinal.FILE_DUPLICATION_SUFFIX;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.adapter.AnimTempItemAdapter;
import com.example.discboard.R;
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
    Boolean mLoadedMark;// true: if load/create btn has been pressed
    String mTempName;// store the name of already loaded template animation
    Button mSaveOldTempBtn, mLoadTempBtn, mDelFrameBtn, mInsertFrameBtn,
        mRevokeBtn;
    JsonDataHelper mJsonDataHelper;
    static ArrayList<String> mAniTempList;
    Slider mFrameSlider;
    static AnimatedDiscBoard mAnimatedDiscBoard;
    private boolean mDelPressFlag;
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_anim_board, container, false);

        mAnimatedDiscBoard = v.findViewById(R.id.animated_discboard);
        mSaveOldTempBtn = v.findViewById(R.id.save_old_temp_btn);
        mLoadTempBtn = v.findViewById(R.id.load_temp_btn);
        mRevokeBtn = v.findViewById(R.id.revoke_btn);
        setLoadedMarkAndTempName(false, "");

        // load temp
        mLoadTempBtn.setOnClickListener(view -> {
            mAniTempList = mJsonDataHelper.loadTempNamesFromPref();
            if(mAniTempList == null || mAniTempList.size() == 0){
                Toast.makeText(getActivity(), "请先创建新的战术！", Toast.LENGTH_SHORT).show();
            }
            else {
                mLoadTempDialogFragment.show(getChildFragmentManager(), "读取战术模板");
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
            mSaveDialogFragment.setSaveDialogListener(name -> {
                setLoadedMarkAndTempName(true, name);

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

        mSelectTempDialog = new SelectTempDialog(Objects.requireNonNull(getContext()));
        mSelectTempDialog.setOnSelectListener(new SelectTempDialog.OnSelectListener() {
            @Override
            public void onPressMy() {
                mAnimatedDiscBoard.loadMyPreset();
                mSelectTempDialog.dismiss();

                setLoadedMarkAndTempName(false, "");
            }

            @Override
            public void onPress3() {
                mAnimatedDiscBoard.load3Preset();
                mSelectTempDialog.dismiss();

                setLoadedMarkAndTempName(false, "");
            }

            @Override
            public void onPress5() {
                mAnimatedDiscBoard.load5Preset();
                mSelectTempDialog.dismiss();

                setLoadedMarkAndTempName(false, "");
            }

            @Override
            public void onPressHo() {
                mAnimatedDiscBoard.loadHostackPreset();
                mSelectTempDialog.dismiss();

                setLoadedMarkAndTempName(false, "");
            }

            @Override
            public void onPressVert() {
                mAnimatedDiscBoard.loadVerstackPreset();
                mSelectTempDialog.dismiss();

                setLoadedMarkAndTempName(false, "");
            }
        });

        // template selection dialog
        v.findViewById(R.id.select_temp_btn).setOnClickListener(view -> {
//            mSelectTempDialogFragment.show(getChildFragmentManager(), "选择模板");
            mSelectTempDialog.show();
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
            }

            @Override
            public void onAnimationStop() {
                mLoadTempBtn.setEnabled(true);
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


        // Inflate the layout for this fragment
        return v;
    }

    void setLoadedMarkAndTempName(Boolean loadedMark, String tempName) {
        mLoadedMark = loadedMark;
        setSaveOldBtnState(loadedMark);
        mTempName = tempName;
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
                        if(mJsonDataHelper.checkNameDuplication(temp_name)){
                            temp_name = temp_name + FILE_DUPLICATION_SUFFIX;
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
            dismiss();
        }
    }
}