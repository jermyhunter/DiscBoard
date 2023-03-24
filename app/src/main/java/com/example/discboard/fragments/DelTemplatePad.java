package com.example.discboard.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.adapter.AnimTempItemDelAdapter;

import java.util.ArrayList;

/**
 * DelTemplatePad
 * used for deleting temps stored in the preferences
 * */
public class DelTemplatePad extends Fragment implements AnimTempItemDelAdapter.OnAniTempDelListener {
    private static final String TAG = "DelTemplatePad Fragment";

    DataInitDialogFragment mDataInitDialogFragment;
    JsonDataHelper mJsonDataHelper;
    RecyclerView mRecyclerView;
    AnimTempItemDelAdapter mAnimTempItemDelAdapter;
    ArrayList<String> mAniTempList;
    Button mDataInitBtn;
    public DelTemplatePad() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DeleteTemplatePad.
     */
    public static DelTemplatePad newInstance(String param1, String param2) {
        DelTemplatePad fragment = new DelTemplatePad();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJsonDataHelper = new JsonDataHelper(getContext());
        mAniTempList = mJsonDataHelper.loadTempNamesFromPref();
//        Log.d(TAG, "onCreate: " + mAniTempList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_del_template_pad, container, false);

        mRecyclerView = v.findViewById(R.id.recycler_view);
        mAnimTempItemDelAdapter = new AnimTempItemDelAdapter(mAniTempList, getContext(), this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), getContext().getResources().getInteger(R.integer.anim_temp_del_item_span));

        mRecyclerView.setAdapter(mAnimTempItemDelAdapter);
        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        mDataInitDialogFragment = new DataInitDialogFragment();
        mDataInitDialogFragment.setDataInitDialogListener(new DataInitDialogFragment.DataInitDialogListener() {
            @Override
            public void onDataInitListener() {
                if(mAnimTempItemDelAdapter.removeAllData())
                    Toast.makeText(getContext(), "战术已清空！", Toast.LENGTH_LONG).show();
            }
        });

        mDataInitBtn = v.findViewById(R.id.data_init_btn);
        mDataInitBtn.setOnClickListener(view -> {
            mDataInitDialogFragment.show(getChildFragmentManager(), "初始化战术板数据");
        });

        return v;
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getContext(), mAnimTempItemDelAdapter.getData(position)
                + " 删除成功！", Toast.LENGTH_SHORT).show();
        mAnimTempItemDelAdapter.removeData(position);
    }


    /**
     * DataInitDialogFragment
     * inner dialog fragment, used for guiding the user to name the to-be-exported file
     * */
    public static class DataInitDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = getActivity().getLayoutInflater();// * 从 requireActivity() 改为 getActivity()
            View dialogView = inflater.inflate(R.layout.dialog_data_init, null);

            builder.setView(dialogView)
                    .setPositiveButton("确认删除", (dialogInterface, i) -> {
                        mDataInitDialogListener.onDataInitListener();
                    })
                    .setNegativeButton("取消", (dialogInterface, i) -> {
                    });

            return builder.create();
        }

        // handling the export process
        DataInitDialogListener mDataInitDialogListener;

        public void setDataInitDialogListener(DataInitDialogListener dataInitDialogListener) {
            mDataInitDialogListener = dataInitDialogListener;
        }

        public interface DataInitDialogListener{
            void onDataInitListener();
        }
    }
}