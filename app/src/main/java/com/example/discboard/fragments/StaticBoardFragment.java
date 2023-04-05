package com.example.discboard.fragments;

import static com.example.discboard.DiscFinal.*;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.discboard.DiscFinal;
import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.views.DiscBoard;
import com.example.discboard.views.Sketchpad;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StaticBoardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StaticBoardFragment extends Fragment {
    static String TAG = "StaticBoardFragment";

    PaletteButtonSet mPaletteButtonSet;
    static class PaletteButtonSet{
        ImageButton[] paletteButtons;
        ImageButton currentPalette;
        public PaletteButtonSet(ImageButton[] imageButtons){
            paletteButtons = imageButtons;
        }

        public void setCurrentPalette(int paintType) {
            if(this.currentPalette != null) {
                this.currentPalette.setAlpha(0.3f);
            }

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
        mPaletteButtonSet.setCurrentPalette(PaintType.Red.getValue());
    }

    Animation mAnimSlideInSketch, mAnimSlideOutSketch,
            mAnimSlideInBoard, mAnimSlideOutBoard;
    View mStaticButtonsLayout, mPaintButtonsLayout;
    Sketchpad mSketchpad;
    Button mReturnBtn;
    ImageButton mPaintSwitch;
    ImageButton mPaintBtn, mEraseBtn;
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

        initLayoutSlideAnim();
        initPaletteButtonSet(v);

        mDiscBoard = v.findViewById(R.id.static_board);
        mStaticButtonsLayout = v.findViewById(R.id.static_buttons_layout);
        initPaintLayout(v);
        String bgType = mJsonDataHelper.initBGByUserData(mDiscBoard);
        // if bg_img is not disc, then resize it to 1:2
        if(!(bgType.equals(getResources().getString(R.string.disc_endzone)) || bgType.equals(getResources().getString(R.string.disc_full)))){
            mDiscBoard.post(() -> {
                // resize board to 1:2
                int boardWidth = mDiscBoard.getWidth();
                int boardHeight = mDiscBoard.getHeight();
                //        Log.d(TAG, "surfaceViewWidth: " + surfaceViewWidth);
                float boardProportion = (float) boardWidth / (float) boardHeight;

                // Get the SurfaceView layout parameters
                ViewGroup.LayoutParams lp = mDiscBoard.getLayoutParams();
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
                mDiscBoard.setLayoutParams(lp);
            });
            mSketchpad.post(() -> {
                // resize board to 1:2
                int boardWidth = mDiscBoard.getWidth();
                int boardHeight = mDiscBoard.getHeight();
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

        mMenuHint = v.findViewById(R.id.menu_hint);
        initMenuIconAnim();
        mMenuHint.startAnimation(mAnimationIn);

        // external storage space readable test
//        if(isExternalStorageReadable())
//            Toast.makeText(getApplicationContext(),"可读",Toast.LENGTH_LONG).show();
//        if(isExternalStorageWritable())
//            Toast.makeText(getApplicationContext(),"可写",Toast.LENGTH_LONG).show();

//        // 设置背景图片
//        mDiscBoard.setBackground(getResources().getDrawable(R.drawable.disc_space));

        mSwapBtn = v.findViewById(R.id.swap_btn);
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

                Toast.makeText(getContext(), R.string.temp_updated_success_hint, Toast.LENGTH_SHORT).show();
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

    private void initPaintLayout(View v) {
        mSketchpad = v.findViewById(R.id.sketchpad);
        mPaintButtonsLayout = v.findViewById(R.id.paint_buttons_layout);
        mPaintBtn = v.findViewById(R.id.paint_btn);
        mEraseBtn = v.findViewById(R.id.erase_btn);
        mPaintSwitch = v.findViewById(R.id.paint_switch);
        mReturnBtn = v.findViewById(R.id.return_btn);
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
                // board_buttons slide in, paint_buttons slide out
                showBoard();
            }
        });

        // grab board snapshot to sketchpad
        mPaintSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // load sketchpad using board canvas
                Bitmap b = DiscFinal.loadBitmapFromView(mDiscBoard);
                BitmapDrawable bd = new BitmapDrawable(getResources(), b);
                mSketchpad.setBackground(bd);
//                mSketchpad.clearAll();

                //paint_buttons slide in, board_buttons slide out
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
        mAnimSlideOutSketch = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_top);
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
        mAnimSlideOutBoard = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_to_top);
        mAnimSlideInBoard.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mStaticButtonsLayout.setVisibility(View.VISIBLE);
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
                mStaticButtonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void showBoard() {
        mStaticButtonsLayout.startAnimation(mAnimSlideInBoard);
        mDiscBoard.setVisibility(View.VISIBLE);
        mPaintButtonsLayout.startAnimation(mAnimSlideOutSketch);
        mSketchpad.setVisibility(View.GONE);

        mPaintSwitch.setEnabled(true);
        mReturnBtn.setEnabled(false);
    }

    private void showSketchpad() {
        mStaticButtonsLayout.startAnimation(mAnimSlideOutBoard);
        mDiscBoard.setVisibility(View.GONE);
        mPaintButtonsLayout.startAnimation(mAnimSlideInSketch);
        mSketchpad.setVisibility(View.VISIBLE);

        mPaintSwitch.setEnabled(false);
        mReturnBtn.setEnabled(true);
        // init button's background
        mPaintBtn.setBackgroundResource(R.drawable.pen_focus);
        mEraseBtn.setBackgroundResource(R.drawable.eraser_normal);
    }


    private void initMenuIconAnim() {
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
    }

    public void setTempName(String tempName) {
        mTempName = tempName;
    }


}