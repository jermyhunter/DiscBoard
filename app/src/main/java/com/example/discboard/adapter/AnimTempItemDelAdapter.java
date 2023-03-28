package com.example.discboard.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.dialogs.DelCheckDialog;
import com.example.discboard.dialogs.RenameTempDialog;

import java.util.List;

/**
 * AniTempItemDelAdapter
 * this adapter class is for holding every anim_temp_item stored in the shared preferences
 * especially desired for deleting
 *
 * used on in DelTemplatedPad class
 * */
public class AnimTempItemDelAdapter extends RecyclerView.Adapter<AnimTempItemDelAdapter.ItemViewHolder> {

    static String TAG = "AniTempItemDelAdapter";
    private List<String> mAniTempList;
    private Context mContext;
    private OnAniTempDelListener mOnAniTempDelListener;
    JsonDataHelper mJsonDataHelper;

    /**
     * NOTICE: itemList must be assign to a NonNull value first,
     * so that the UI will update on init correctly
     *
     * if you need to update afterwards, then create add/remove method
     * and call:
     *    notifyDataSetChanged/notifyItemInserted/notifyItemRemoved on your need
    */
    public AnimTempItemDelAdapter(List<String> itemList, Context context, OnAniTempDelListener onAniTempDelListener) {
        this.mAniTempList = itemList;
        this.mContext = context;
        this.mOnAniTempDelListener = onAniTempDelListener;

        mJsonDataHelper = new JsonDataHelper(context);
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.anim_temp_del_item, parent, false);
        return new ItemViewHolder(view, mOnAniTempDelListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        final String item = mAniTempList.get(position);
//        holder.mTempNameText.setText(item.getTempName());
        holder.mTempNameText.setText(item);
    }

    @Override
    public int getItemCount() {
        return mAniTempList.size();
    }

    /**
     * public method
     * remove data from adapter's list
     * delete data from the preferences
     * */
    public void removeData(int position) {
        String name = getData(position);
        mAniTempList.remove(name);
        notifyItemRemoved(position);

        mJsonDataHelper.delAniNameFromPref(name);
        mJsonDataHelper.delAniDotsFromPref(name);
    }

    public void changeData(int position, String tempNameNew) {
        String tempNameOld = getData(position);
        mAniTempList.remove(position);
        mAniTempList.add(position, tempNameNew);
        notifyItemChanged(position);

        // copy old temp data to new place
        // and add new name to the temp_list
        mJsonDataHelper.addAniTempToPref(tempNameNew);
        mJsonDataHelper.copyAnimDots(tempNameOld, tempNameNew);
        // delete old temp data
        mJsonDataHelper.delAniNameFromPref(tempNameOld);
        mJsonDataHelper.delAniDotsFromPref(tempNameOld);
    }

    public boolean removeAllData() {
        int sum = getItemCount();
        while(sum > 0) {
            String name = getData(0);
            notifyItemRemoved(0);

            mAniTempList.remove(name);
            mJsonDataHelper.delAniNameFromPref(name);
            mJsonDataHelper.delAniDotsFromPref(name);
            sum--;
        }
        return true;
    }

    public String getData(int position){
        return mAniTempList.get(position);
    }

    /**
     * 对应的视图类
     * all subViews should be assigned by findViewById
     * */
    class ItemViewHolder extends RecyclerView.ViewHolder{
        CardView mCardView;
        TextView mTempNameText;
        ImageView mImageView;

        OnAniTempDelListener onAniTempDelListener;

        public ItemViewHolder(View itemView, OnAniTempDelListener onAniTempDelListener){
            super(itemView);
            mCardView = itemView.findViewById(R.id.card_view);
            mTempNameText = itemView.findViewById(R.id.anim_temp_name);
            mImageView = itemView.findViewById(R.id.img_del);

            this.onAniTempDelListener = onAniTempDelListener;// 点击事件
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DelCheckDialog delCheckDialog = new DelCheckDialog(view.getContext(), getData(getAdapterPosition()));
                    delCheckDialog.setDelCheckDialogListener(tempNameNew -> removeData(getAdapterPosition()));
                    delCheckDialog.show();
                }
            });

            // ItemViewHolder's onClick event
            // renaming
            itemView.setOnClickListener(view -> {
                RenameTempDialog renameTempDialog = new RenameTempDialog(view.getContext(), getData(getAdapterPosition()));
                renameTempDialog.setDelCheckDialogListener(tempNameNew -> changeData(getAdapterPosition(),tempNameNew));
                renameTempDialog.show();
            });
        }
    }

    public interface OnAniTempDelListener {
        void onItemDelClick(int position);
    }
}
