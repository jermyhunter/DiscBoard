package com.example.discboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.discboard.R;

import java.util.List;

/**
 * AniTempItemAdapter
 * this adapter class is for holding every anim_temp_item stored in the shared preferences
 *
 * used on in SelectTempDialog class
 * */
public class AnimTempItemAdapter extends RecyclerView.Adapter<AnimTempItemAdapter.ItemViewHolder> {
    private List<String> mAniTempList;
    private Context mContext;
    private OnAniTempListener mOnAniTempListener;

    /**
     * NOTICE: itemList must be assign to a NonNull value first,
     * so that the UI will update on init correctly
     *
     * if you need to update afterwards, then create add/remove method
     * and call:
     *    notifyDataSetChanged/notifyItemInserted/notifyItemRemoved on your need
    */
    public AnimTempItemAdapter(List<String> itemList, Context context, OnAniTempListener onAniTempListener) {
        this.mAniTempList = itemList;
        this.mContext = context;
        this.mOnAniTempListener = onAniTempListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.anim_temp_item, parent, false);
        return new ItemViewHolder(view, mOnAniTempListener);
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

    public String getData(int position){
        return mAniTempList.get(position);
    }

    /**
     * 对应的视图类
     * */
    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView mCardView;
        TextView mTempNameText;
        OnAniTempListener onAniTempListener;

        public ItemViewHolder(View itemView, OnAniTempListener onAniTempListener){
            super(itemView);
            mCardView = itemView.findViewById(R.id.card_view);

            mTempNameText = itemView.findViewById(R.id.anim_temp_name);
            this.onAniTempListener = onAniTempListener;// 点击事件
            mTempNameText.setOnClickListener(this);
        }

        // ItemViewHolder's onClick event
        @Override
        public void onClick(View view) {
            onAniTempListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnAniTempListener {
        void onItemClick(int position);
    }
}
