package com.example.discboard.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.example.discboard.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GuidingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GuidingFragment extends Fragment {
    private static final String TAG = "GuidingFragment";
    MediaPlayer mTemplateSharingMP, mAnimationMP, mLoadAndSaveMP,
    mExportingAndSharingMP, mImportingMP, mAutoSaveMP;

    View mRecommendTxtLayout;

    MediaPlayer mCurrentMP;

    SurfaceView mSurfaceView;
    public GuidingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GuidingFragment.
     */
    public static GuidingFragment newInstance(String param1, String param2) {
        GuidingFragment fragment = new GuidingFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_guiding, container, false);
        mRecommendTxtLayout = v.findViewById(R.id.recommend_txt_layout);

        mSurfaceView = v.findViewById(R.id.surface_view);
        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback(){
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) { }
        });

        v.findViewById(R.id.template_sharing_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHintAndShowSV();

                releaseCurrentMP();
                mTemplateSharingMP = MediaPlayer.create(getContext(), R.raw.template_sharing);
                resizeSurfaceView2Video(mTemplateSharingMP);
                mTemplateSharingMP.setDisplay(holder);
                mTemplateSharingMP.start();
                mTemplateSharingMP.setLooping(true);
                setCurrentMP(mTemplateSharingMP);
            }
        });

        // 动画播放演示

        v.findViewById(R.id.animation_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHintAndShowSV();

                releaseCurrentMP();
                mAnimationMP = MediaPlayer.create(getContext(), R.raw.animation);
                resizeSurfaceView2Video(mAnimationMP);
                mAnimationMP.setDisplay(holder);
                mAnimationMP.start();
                mAnimationMP.setLooping(true);
                setCurrentMP(mAnimationMP);
            }
        });

        // 战术读取与修改

        v.findViewById(R.id.load_and_save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHintAndShowSV();

                releaseCurrentMP();
                mLoadAndSaveMP = MediaPlayer.create(getContext(), R.raw.load_and_save);
                resizeSurfaceView2Video(mLoadAndSaveMP);
                mLoadAndSaveMP.setDisplay(holder);
                mLoadAndSaveMP.start();
                mLoadAndSaveMP.setLooping(true);
                setCurrentMP(mLoadAndSaveMP);
            }
        });

        // 导出与分享
        v.findViewById(R.id.exporting_and_sharing_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHintAndShowSV();

                releaseCurrentMP();
                mExportingAndSharingMP = MediaPlayer.create(getContext(), R.raw.exporting_and_sharing);
                resizeSurfaceView2Video(mExportingAndSharingMP);
                mExportingAndSharingMP.setDisplay(holder);
                mExportingAndSharingMP.start();
                mExportingAndSharingMP.setLooping(true);
                setCurrentMP(mExportingAndSharingMP);
            }
        });

        // 导入
        v.findViewById(R.id.importing_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideHintAndShowSV();

                releaseCurrentMP();
                mImportingMP = MediaPlayer.create(getContext(), R.raw.importing);
                resizeSurfaceView2Video(mImportingMP);
                mImportingMP.setDisplay(holder);
                mImportingMP.start();
                mImportingMP.setLooping(true);
                setCurrentMP(mImportingMP);
            }
        });

        v.findViewById(R.id.auto_save_btn).setOnClickListener(view -> {
            hideHintAndShowSV();

            releaseCurrentMP();
            mAutoSaveMP = MediaPlayer.create(getContext(), R.raw.auto_save);
            resizeSurfaceView2Video(mAutoSaveMP);
            mAutoSaveMP.setDisplay(holder);
            mAutoSaveMP.start();
            mAutoSaveMP.setLooping(true);
            setCurrentMP(mAutoSaveMP);
        });

        return v;
    }

    /**
     * hide recommendation hint txt layout, and show SurfaceView
     */
    private void hideHintAndShowSV() {
        if(mRecommendTxtLayout.getVisibility() != View.GONE) {
            mRecommendTxtLayout.setVisibility(View.GONE);
        }
        if(mSurfaceView.getVisibility() != View.VISIBLE) {
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause(){
        super.onPause();

//        if(null != mTemplateSharingMP) mTemplateSharingMP.release();
//        mTemplateSharingMP = null;
    }

    private void resizeSurfaceView2Video(MediaPlayer mp) {

         // Get the dimensions of the video
        int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
//        Log.d(TAG, "videoWidth: " + videoWidth);
//        Log.d(TAG, "videoHeight: " + videoHeight);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels - 5;// minus width with a tiny delta_e

        // Get the width of the screen
        // surfaceView width must be smaller than the (screenWidth - delta_e)
        int surfaceViewWidth = Math.min(mSurfaceView.getWidth(), screenWidth);
        int surfaceViewHeight = mSurfaceView.getHeight();
//        Log.d(TAG, "surfaceViewWidth: " + surfaceViewWidth);
//        Log.d(TAG, "surfaceViewHeight: " + surfaceViewHeight);
        float surfaceProportion = (float) surfaceViewWidth / (float) surfaceViewHeight;

        // Get the SurfaceView layout parameters
        android.view.ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        if (videoProportion > surfaceProportion) {
            lp.width = surfaceViewWidth;
            lp.height = (int) ((float) surfaceViewWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) surfaceViewHeight);
            lp.height = surfaceViewHeight;
        }
        // Commit the layout parameters
        mSurfaceView.setLayoutParams(lp);
    }

    public void setCurrentMP(MediaPlayer currentMP) {
        mCurrentMP = currentMP;
    }

    public void releaseCurrentMP() {
        if(null != mCurrentMP) mCurrentMP.release();
        mCurrentMP = null;
    }
}