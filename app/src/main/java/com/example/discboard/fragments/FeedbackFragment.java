package com.example.discboard.fragments;

import static com.example.discboard.DiscFinal.*;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.discboard.BuildConfig;
import com.example.discboard.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedbackFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedbackFragment extends Fragment {
    public FeedbackFragment() {
        // Required empty public constructor
    }

    ImageView mQRCodeImg;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment FeedbackFragment.
     */
    public static FeedbackFragment newInstance(String param1, String param2) {
        FeedbackFragment fragment = new FeedbackFragment();
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
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        // 复制联系方式到剪切板
        v.findViewById(R.id.copy_contact_btn).setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", CONTACT_INFO);
            clipboard.setPrimaryClip(clip);
        });

        // 复制链接到剪切板
        v.findViewById(R.id.visit_homepage_btn).setOnClickListener(view -> {
            // clipboard
//                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData clip = ClipData.newPlainText("", CONTACT_URL);
//                clipboard.setPrimaryClip(clip);

            try {
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, CONTACT_URL);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse(CONTACT_URL));
                startActivity(i);
            } catch (Exception e) {
                Log.e("In Exception", "Comes here");
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, CONTACT_URL);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse(CONTACT_URL));
                startActivity(i);
            }
        });

        mQRCodeImg = v.findViewById(R.id.qr_code_img);
        mQRCodeImg.post(() -> {
            //resize
            int boardWidth = mQRCodeImg.getWidth();
            int boardHeight = mQRCodeImg.getHeight();
            ViewGroup.LayoutParams lp = mQRCodeImg.getLayoutParams();
//            float ratio = (float)(boardWidth - 20) / boardWidth;
            float ratio = 4 / 5f;
            lp.width = (int) (boardWidth * ratio);
            lp.height = (int) (boardHeight * ratio);
            mQRCodeImg.setLayoutParams(lp);
            mQRCodeImg.setVisibility(View.VISIBLE);
        });

        // unused animation
        Animation fadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
        fadeInAnim.setStartOffset(2 * 1000);
        fadeInAnim.setDuration(2 * 1000);
        fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mQRCodeImg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        mQRCodeImg.startAnimation(fadeInAnim);

        String version_name = BuildConfig.VERSION_NAME;
        TextView verInfoTxt = v.findViewById(R.id.ver_info_txt);
        verInfoTxt.setText("ver" + version_name + "\ndev by Yugar");

        return v;
    }
}