package com.example.discboard.fragments;

import static com.example.discboard.DiscFinal.*;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.views.DiscBoard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StaticBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StaticBoardFragment extends Fragment {
    static String TAG = "StaticBoardFragment";
    static DiscBoard mDiscBoard;
    ToggleButton mSwapBtn;
    Animation mAnimationIn, mAnimationOut;
    ImageView mMenuHint;
    final int ANIM_DELAY = 5 * 1000;// the delay of menu hint anim
    JsonDataHelper mJsonDataHelper;

    String mTempName;
    public StaticBoardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StaticBoardFragment.
     */
    public static StaticBoardFragment newInstance(String param1, String param2) {
        StaticBoardFragment fragment = new StaticBoardFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTempName = USER_DATA_TEMP_MY;
        mJsonDataHelper = new JsonDataHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_static_board, container, false);

        mMenuHint = (ImageView)v.findViewById(R.id.menu_hint);

        mAnimationIn = AnimationUtils.loadAnimation(getContext(),
                R.anim.icon_fade_in);

        mAnimationIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mMenuHint.startAnimation(mAnimationOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mAnimationOut = AnimationUtils.loadAnimation(getContext(),
                R.anim.icon_fade_out);
        mAnimationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mMenuHint.startAnimation(mAnimationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mAnimationIn.setStartOffset(ANIM_DELAY);
        mMenuHint.startAnimation(mAnimationIn);

        mDiscBoard = v.findViewById(R.id.static_discboard);
        mSwapBtn = v.findViewById(R.id.swap_btn);

        // external storage space readable test
//        if(isExternalStorageReadable())
//            Toast.makeText(getApplicationContext(),"可读",Toast.LENGTH_LONG).show();
//        if(isExternalStorageWritable())
//            Toast.makeText(getApplicationContext(),"可写",Toast.LENGTH_LONG).show();

//        // 设置背景图片
//        mDiscBoard.setBackground(getResources().getDrawable(R.drawable.disc_space));

        /*
        * 交换按钮
        *
        * 功能：切换棋子的攻/防属性
        * */
        mSwapBtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // The toggle is enabled
                mDiscBoard.setDefense();
            } else {
                // The toggle is disabled
                mDiscBoard.setOffense();
            }
        });

        /*
        * 添加按钮
        *
        * 功能：点击添加棋子
        * */
        v.findViewById(R.id.add_dot_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.addDot();
            }
        });

        v.findViewById(R.id.apply_change_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.saveTemp(mTempName);

                Toast.makeText(getContext(), "模板修改成功！", Toast.LENGTH_SHORT).show();
            }
        });

        v.findViewById(R.id.reset_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.resetStaticBoard();
            }
        });

        v.findViewById(R.id.default_temp_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.loadDefaultTemp();
                setTempName(USER_DATA_TEMP_MY);
            }
        });

        v.findViewById(R.id.three_player_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.load3Preset();
                setTempName(USER_DATA_TEMP_3);
            }
        });

        v.findViewById(R.id.five_player_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.load5Preset();
                setTempName(USER_DATA_TEMP_5);
            }
        });

        v.findViewById(R.id.ver_stack_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.loadVerstackPreset();
                setTempName(USER_DATA_TEMP_VER);
            }
        });

        v.findViewById(R.id.ho_stack_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDiscBoard.loadHostackPreset();
                setTempName(USER_DATA_TEMP_HO);
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    public void setTempName(String tempName) {
        mTempName = tempName;
    }


}