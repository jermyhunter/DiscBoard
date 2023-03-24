package com.example.discboard.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discboard.adapter.AnimTempItemAdapter;
import com.example.discboard.R;

import java.util.ArrayList;
import java.util.List;

// NOT USING
public class LoadTempDialog extends Dialog implements AnimTempItemAdapter.OnAniTempListener {
    private static String TAG = "LoadTempDialog";
    List<String> mAniTempList;
    RecyclerView mRecyclerView;
    AnimTempItemAdapter mAnimTempItemAdapter;

    String mName;
    public LoadTempDialog(@NonNull Context context, ArrayList<String> aniTempList) {
        super(context);
        this.setContentView(R.layout.dialog_load_anim_temp);

        mAniTempList = aniTempList;

        mRecyclerView = findViewById(R.id.recycler_view);
        mAnimTempItemAdapter = new AnimTempItemAdapter(mAniTempList, getContext(), this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, context.getResources().getInteger(R.integer.anim_temp_item_span));

        mRecyclerView.setAdapter(mAnimTempItemAdapter);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onItemClick(int position) {
        mAnimTempItemAdapter.getData(position);
//        Log.d(TAG, "onAniTempClick: " + name);
    }
}
