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

public class FeedbackFragment extends Fragment {
    public FeedbackFragment() {
        // Required empty public constructor
    }

    ImageView mSponsorImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        // 访问B站
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

        // 访问爱发电赞助页
        v.findViewById(R.id.visit_afdian_btn).setOnClickListener(view -> {
            try {
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, "https://afdian.net/a/yugar");
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://afdian.net/a/yugar"));
                startActivity(i);
            } catch (Exception e) {
                Log.e("In Exception", "Comes here");
                Intent i = new Intent();
                i.putExtra(Intent.EXTRA_TEXT, "https://afdian.net/a/yugar");
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://afdian.net/a/yugar"));
                startActivity(i);
            }
        });

        // 赞助二维码
        mSponsorImg = v.findViewById(R.id.sponsor_img);
        mSponsorImg.post(() -> {
            //resize
            int boardWidth = mSponsorImg.getWidth();
            int boardHeight = mSponsorImg.getHeight();
            ViewGroup.LayoutParams lp = mSponsorImg.getLayoutParams();
            float ratio = 5 / 6f;
            lp.width = (int) (boardWidth * ratio);
            lp.height = (int) (boardHeight * ratio);
            mSponsorImg.setLayoutParams(lp);
            mSponsorImg.setVisibility(View.VISIBLE);
        });

//        View QRCodeLayout = v.findViewById(R.id.qr_code_layout);
//        // fade_in animation
//        Animation fadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.fade_in);
//        fadeInAnim.setStartOffset((int)(2.5 * 1000));
//        fadeInAnim.setDuration(2 * 1000);
//        fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                QRCodeLayout.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        QRCodeLayout.startAnimation(fadeInAnim);


        String version_name = BuildConfig.VERSION_NAME;
        TextView verInfoTxt = v.findViewById(R.id.ver_info_txt);
        verInfoTxt.setText("ver" + version_name + "\ndev by Yugar");

        return v;
    }
}