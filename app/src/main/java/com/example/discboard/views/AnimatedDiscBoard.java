package com.example.discboard.views;

import static android.content.Context.MODE_PRIVATE;

import static com.example.discboard.DiscFinal.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.MainActivity;
import com.example.discboard.R;
import com.example.discboard.datatype.AnimTemp;
import com.example.discboard.datatype.Dot;
import com.example.discboard.datatype.InterDot;
import com.google.android.material.math.MathUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * AnimatedDiscBoard
 * custom view used for showing disc animation, including:
 *      creating new from init temps, loading saved temps,
 *      inserting frame, deleting frame, playing animation,
 *      changing temps, saving new temps
 *
 *
 * animation draw has 2 plans:
 *      plan A, treat it as straight line, using class DeltaXY
 *      plan B, treat it as curved line, using inter_dot
 * */
public class AnimatedDiscBoard extends View {
    public static boolean mSavedFlag;// used for unsaved data checking
    public static boolean isSaved() {
        return mSavedFlag;
    }

    final static int STROKE_WIDTH = 8;
    Gson mGson;
    private static int STEP_NUM = 100;
//    private static float AMOUNT;
//    Hashtable<String, DeltaXY> mDeltaXYHashtable;// 用于动画偏移存储
    Hashtable<String, PathMeasure> mPathMeasureHashtable;// 存储PathMeasure
    Hashtable<String, Dist> mDistHashtable;// 存储剩余长度
    String mTempName;
    JsonDataHelper mJsonDataHelper;

    /* abandoned plan A
    class DeltaXY{
        float delta_X, delta_Y;
        float pre_X, pre_Y;
        DeltaXY(){}
        DeltaXY(float pre_X, float pre_Y, float x, float y){
            this.delta_X = x;
            this.delta_Y = y;
            this.pre_X = pre_X;
            this.pre_Y = pre_Y;
        }
        public void setDelta_X(float delta_X) {
            this.delta_X = delta_X;
        }

        public float getDelta_X() {
            return delta_X;
        }

        public void setDelta_Y(float delta_Y) {
            this.delta_Y = delta_Y;
        }

        public float getDelta_Y() {
            return delta_Y;
        }

        public void setPre_X(float pre_X) {
            this.pre_X = pre_X;
        }

        public float getPre_X() {
            return pre_X;
        }

        public void setPre_Y(float pre_Y) {
            this.pre_Y = pre_Y;
        }

        public float getPre_Y() {
            return pre_Y;
        }
    }
    */

    /*
     * used for animation path length calculation
     */
    class Dist{
        float dist;
        float deltaDis;
        public Dist(float dist, float delta_dis){
            this.dist = dist;
            this.deltaDis = delta_dis;
        }

        public void setDist(float dist) {
            this.dist = dist;
        }

        public float getDist() {
            return dist;
        }

        public void goForward(){
            this.dist += deltaDis;
        }
    }
    int mStep;
    int mAniCurrentFrameNo;// used for animation processing
    int mAniFrameNoLimit;

    private static final String TAG = "AniBoard Class";
    Paint mOffLinePaint, mDefLinePaint, mDiscLinePaint;
    Paint mDefCircletPaint, mOffCircletPaint, mDiscCircletPaint,
            mHollowCirclePaint, mInterCirclePaint;
    private final static float DASH_PORTION = 0.60F;
    private final static int NUM_DASHES = 8;
    // tracking current frameNo for quick index, default -> 0
    private int mCurrentFrameNo;
    float mDeltaY, mTextOffsetY;
    private Dot mTouchedDot, mEnabledDot,
            mEnabledDotPreF;
    private InterDot mEnabledInterDot, mTouchedInterDot;
    float[] pos;
    float[] tan;
    // track the state of animation
    private boolean mAnimationMark;
    // track the state of Painting
    private boolean mPaintingMark;
    /*
    * the outer index - List - is indexed by frame_No for quick search
    * the inner hashtable save every frame dots as the K-V format of DotID-Dot
    *
    * the frame_index of mAnimDotsList is always 1-frame bigger than mInterDotsList
    * it means:
    *       currentFrameNo(AnimDot) - 1 == currentFrameNo(InterDot)
    * */
    ArrayList<Hashtable<String, Dot>> mAnimDotsList;// 存储动画节点
    ArrayList<Hashtable<String, InterDot>> mInterDotsList;// 存储中间节点
    Paint mDefPaint, mOffPaint, mDiscPaint;
    Paint mSequenceTextPaint;

    // 当帧数变化时，修改外部UI
    public interface AnimDiscBoardListener{
        public void onFrameSumChange();
        public void onCurrentFrameNoChange();
        public void onLoad();
        public void onAnimationStart();
        public void onAnimationStop();
        void onDeleteFrame();
    }
    private AnimDiscBoardListener mAnimDiscBoardListener;

    public void setAnimDiscBoardListener(AnimDiscBoardListener mAnimDiscBoardListener){
        this.mAnimDiscBoardListener = mAnimDiscBoardListener;
    }

    private enum TemplateMark{
        THREE,
        FIVE,
        VERTICAL,
        HORIZONTAL
    }
    private TemplateMark templateMark;

    public AnimatedDiscBoard(Context context) {
        super(context);
        init(null);
    }

    public AnimatedDiscBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AnimatedDiscBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public AnimatedDiscBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    void init(@Nullable AttributeSet set){
        pos = new float[2];
        tan = new float[2];

        initGson();
        initPaintColor();
//        mDeltaXYHashtable = new Hashtable<>();
        mPathMeasureHashtable = new Hashtable<>();
        mDistHashtable = new Hashtable<>();

        mAnimDotsList = new ArrayList<Hashtable<String, Dot>>();
        mInterDotsList= new ArrayList<Hashtable<String, InterDot>>();

        setCurrentFrameNo(0);
        loadDefaultTemp();
        mAnimationMark = false;

        mSavedFlag = true;

        mJsonDataHelper = new JsonDataHelper(getContext());
        initAnimSpeed();
    }

    void initPaintColor(){
        initDotPaint();
        initTextPaint();

        initCirclePaint();
        initDashLinePaint();
    }

    void initDotPaint() {
        mDefPaint = new Paint();
        mDefPaint.setColor(ContextCompat.getColor(getContext(), R.color.DefenseColor));
        mDefPaint.setAntiAlias(true);//抗锯齿

        mOffPaint = new Paint();
        mOffPaint.setColor(ContextCompat.getColor(getContext(), R.color.OffenseColor));
        mOffPaint.setAntiAlias(true);

        mDiscPaint = new Paint();
        mDiscPaint.setColor(ContextCompat.getColor(getContext(), R.color.DiscColor));
        mDiscPaint.setAntiAlias(true);
    }

    void initTextPaint() {
        int font_size = 45;
        mSequenceTextPaint = new Paint();
        mSequenceTextPaint.setTextAlign(Paint.Align.CENTER);
        mSequenceTextPaint.setColor(Color.WHITE);
        mSequenceTextPaint.setTextSize(font_size);
        Typeface typeface = getResources().getFont(R.font.bold);
        mSequenceTextPaint.setTypeface(typeface);

        mDeltaY = - (mSequenceTextPaint.descent() + mSequenceTextPaint.ascent()) / 10;// 实际效果偏上，稍稍下移
        mTextOffsetY = ((mSequenceTextPaint.descent() + mSequenceTextPaint.ascent()) / 2) - mDeltaY;
    }


    // initiate the gson member
    void initGson() {
        mGson = new Gson();
    }

    /**
     * called in every load temp
     */
    private void clearBoard() {
        resetSavedFlag();
        // have to set CurrentFrameNo to 0 first, so the slider UI would be correct
        // then clear the dots
        setCurrentFrameNo(0);
        clearDots();
    }
    public void resetSavedFlag(){
        mSavedFlag = false;
    }
    public void setSavedFlag(){
        mSavedFlag = true;
    }

    private void clearDots() {
        mAnimDotsList.clear();
        mInterDotsList.clear();
    }

    public void loadDefaultTemp(){
        clearBoard();

        // load vertical stack as default
        loadVerstackPreset();
    }

    public void loadMyPreset() {
        clearBoard();

        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_MY, null);

        assert s != null;//如果没有用过 SharedPreferences 的情况，即 s 中没有数据为null的情况
        if(!s.equals("")) {// 如果存放有数据
            Dot[] dot_array = mGson.fromJson(s, Dot[].class);
            Hashtable<String, Dot> dots_hashtable = new Hashtable<>();
            for(Dot dot : dot_array) dots_hashtable.put(dot.getDotID(), dot);
            mAnimDotsList.add(mCurrentFrameNo, dots_hashtable);
        }

        onFrameSumChange();

        // refresh the canvas
        invalidate();
    }

    public void load3Preset(){
        clearBoard();

        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_3, null);

        assert s != null;//如果没有用过 SharedPreferences 的情况，即 s 中没有数据为null的情况
        if(!s.equals("")) {// 如果存放有数据
            Dot[] dot_array = mGson.fromJson(s, Dot[].class);
            Hashtable<String, Dot> dots_hashtable = new Hashtable<>();
            for(Dot dot : dot_array) dots_hashtable.put(dot.getDotID(), dot);
            mAnimDotsList.add(mCurrentFrameNo, dots_hashtable);
        }

        onFrameSumChange();

        // refresh the canvas
        invalidate();
    }

    public void load5Preset(){
        clearBoard();

        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_5, null);

        assert s != null;//如果没有用过 SharedPreferences 的情况，即 s 中没有数据为null的情况
        if(!s.equals("")) {// 如果存放有数据
            Dot[] dot_array = mGson.fromJson(s, Dot[].class);
            Hashtable<String, Dot> dots_hashtable = new Hashtable<>();
            for(Dot dot : dot_array) dots_hashtable.put(dot.getDotID(), dot);
            mAnimDotsList.add(mCurrentFrameNo, dots_hashtable);
        }

        onFrameSumChange();
    }

    public void loadVerstackPreset(){
        clearBoard();

        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_VER, null);

        assert s != null;//如果没有用过 SharedPreferences 的情况，即 s 中没有数据为null的情况
        if(!s.equals("")) {// 如果存放有数据
            Dot[] dot_array = mGson.fromJson(s, Dot[].class);
            Hashtable<String, Dot> dots_hashtable = new Hashtable<>();
            for(Dot dot : dot_array) dots_hashtable.put(dot.getDotID(), dot);
            mAnimDotsList.add(mCurrentFrameNo, dots_hashtable);
        }

        onFrameSumChange();
    }

    public void loadHostackPreset(){
        clearBoard();

        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_HO, null);

        assert s != null;//如果没有用过 SharedPreferences 的情况，即 s 中没有数据为null的情况
        if(!s.equals("")) {// 如果存放有数据
            Dot[] dot_array = mGson.fromJson(s, Dot[].class);
            Hashtable<String, Dot> dots_hashtable = new Hashtable<>();
            for(Dot dot : dot_array) dots_hashtable.put(dot.getDotID(), dot);
            mAnimDotsList.add(mCurrentFrameNo, dots_hashtable);
        }

        onFrameSumChange();
    }



    /*
    * deep copy of Hashtable consisted of class objects
    * */
    Hashtable<String, Dot> hashCopy(Hashtable<String, Dot> hashtable){
        Hashtable<String, Dot> new_hashtable = new Hashtable<>();
        hashtable.forEach((k,v) -> {
            new_hashtable.put(k, new Dot(v));
        });
        return new_hashtable;
    }
    /*
    * mCurrentFrameNo
    * mAnimDotsList
    * */
    public void insertFrame(){
        Hashtable<String, Dot> cur_dots_hashtable = hashCopy(mAnimDotsList.get(mCurrentFrameNo));
        mAnimDotsList.add(mCurrentFrameNo, cur_dots_hashtable);// insert frame AFTER the given frameNo

        // update mCurrentFrameNo, and call FrameSumChange
        setCurrentFrameNo(mCurrentFrameNo + 1);
        onFrameSumChange();

        // generate new inter_dot_frame
        int pre_frame_No = mCurrentFrameNo - 1;
        Hashtable<String, InterDot> gen_inter_dots_hashtable = new Hashtable<>();
        Hashtable<String, Dot> pre_dots_hashtable = mAnimDotsList.get(pre_frame_No);
        pre_dots_hashtable.forEach((id, pre_dot) -> {
            Dot cur_dot = cur_dots_hashtable.get(id);
            InterDot inter_dot = new InterDot(cur_dot, pre_dot);
            gen_inter_dots_hashtable.put(cur_dot.getRelativeInterDotID(), inter_dot);
//            Log.d(TAG, "insertFrame: " + interDot);
        });
        mInterDotsList.add(pre_frame_No, gen_inter_dots_hashtable);

        // auto-save refresh
        resetSavedFlag();
        releaseTouchDots();
        invalidate();
    }

    /**
     * delete the frame, and move to the next frame
     * and call onFrameSumChange() in every branch
     */
    public void deleteFrame(){
        int cur_frame_No = mCurrentFrameNo;
        // if the FrameSum >= 2
        if (getFrameSum() >= 2) {
            // if the current frame is the last frame
            // set current frame to the previous one, then delete the last frame
            if (cur_frame_No == getFrameSum() - 1) {
                setCurrentFrameNo(cur_frame_No - 1);
                mAnimDotsList.remove(getFrameSum() - 1);
            }
            // else, current frame is not the last frame, then delete current frame
            else {
                // if there is previous frame and next frame
                // adjust next_frame_inter_dot
                int next_inter_frame_No = (cur_frame_No - 1) + 1;
                int pre_frame_No = cur_frame_No - 1;
                int next_frame_No = cur_frame_No + 1;
                if(pre_frame_No >= 0 && next_frame_No < getFrameSum()){
                    Hashtable<String, InterDot> next_inter_dots_list = mInterDotsList.get(next_inter_frame_No);
                    Hashtable<String, Dot> pre_dots_list = mAnimDotsList.get(pre_frame_No);
                    Hashtable<String, Dot> next_dots_list = mAnimDotsList.get(next_frame_No);
                    next_dots_list.forEach((id, next_dot) -> {
                        InterDot next_inter_dot = next_inter_dots_list.get(next_dot.getRelativeInterDotID());
                        if(!next_inter_dot.isTouched()) {
//                            Log.d(TAG, "deleteFrame: " + "inter_dot前后串联成功");
                            Dot pre_dot = pre_dots_list.get(next_dot.getDotID());
                            next_inter_dot.setX((pre_dot.getX() + next_dot.getX()) / 2);
                            next_inter_dot.setY((pre_dot.getY() + next_dot.getY()) / 2);
                        }
                    });
                }
                mAnimDotsList.remove(cur_frame_No);
            }

            // delete current inter_dot frame
            int inter_frame_No = cur_frame_No - 1;// correlated inter_frame_no
            if(inter_frame_No >= 0 && inter_frame_No < getInterFrameSum()){
                mInterDotsList.remove(inter_frame_No);
            }

            mAnimDiscBoardListener.onDeleteFrame();

            // restart auto-save
            resetSavedFlag();

            onFrameSumChange();
            releaseTouchDots();
            invalidate();
        }
        else {
            Toast.makeText(getContext(), R.string.no_anim_hint, Toast.LENGTH_SHORT).show();
        }
    }

    public void loadFrame(int frameNo){
        if(frameNo >= 0 & frameNo < getFrameSum()) {
            setCurrentFrameNo(frameNo);
            invalidate();
        }
        else
            throw new RuntimeException("FrameNo 帧数越界！");
    }
    public int getFrameSum(){
        return mAnimDotsList.size();
    }

    public int getInterFrameSum(){
//        return mAnimDotsList.size() - 1;
        return mInterDotsList.size();
    }

    public int getCurrentFrameNo(){
        return mCurrentFrameNo;
    }

    /**
     * mTouchedDot 的类型为 Dot，可以会赋值 InterDot 类型，但不需要用到 touched 值
     * */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);

        // priority: touched_inter_dot > touched_dot > enabled_inter_dot
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // if this is the initial state click
                if(mEnabledDot == null){
//                    boolean found_dot = false;
                    Iterator<String> id_keys = mAnimDotsList.get(mCurrentFrameNo).keySet().iterator();
                    Hashtable<String, Dot> cur_hashtable = mAnimDotsList.get(mCurrentFrameNo);
                    String id;
                    while (id_keys.hasNext()) {
                        id = id_keys.next();
                        Dot dot = cur_hashtable.get(id);
                        if (dot.isInside(event.getX(), event.getY())) {
                            mEnabledDot = mTouchedDot = dot;

                            // notice: mEnabledInterDot could be null
                            if (mCurrentFrameNo >= 1) {
                                mEnabledInterDot = mInterDotsList.get(mCurrentFrameNo - 1).get(mEnabledDot.getRelativeInterDotID());
                            }
//                            found_dot = true;
                            break;
                        }
                    }
                }
                else{//  mEnabledDot != null && mEnabledInterDot != null
                    // 需要先检测dot,再是inter_dot,不然操作手感很差
                    if(mEnabledInterDot != null) {
                        if (mEnabledDot.isInside(event.getX(), event.getY())) {
                            mTouchedDot = mEnabledDot;

                        }
                        else if (mEnabledInterDot.isInside(event.getX(), event.getY())){
                            mTouchedInterDot = mEnabledInterDot;

                        }
                        else {
                            /*
                             * 如果选中了某个点，但...
                             *  1. 检测是否点击了其他点
                             *  2. 点击了旁边 -> 即取消选择，则重置状态
                             * */
                            boolean found_dot = false;
                            Iterator<String> id_keys = mAnimDotsList.get(mCurrentFrameNo).keySet().iterator();
                            Hashtable<String, Dot> cur_hashtable = mAnimDotsList.get(mCurrentFrameNo);
                            String id;
                            while (id_keys.hasNext()) {
                                id = id_keys.next();
                                Dot dot = cur_hashtable.get(id);
                                if (dot.isInside(event.getX(), event.getY())) {
                                    mEnabledDot = mTouchedDot = dot;

                                    // notice: mEnabledInterDot could be null
                                    if (mCurrentFrameNo >= 1) {
                                        mEnabledInterDot = mInterDotsList.get(mCurrentFrameNo - 1).get(mEnabledDot.getRelativeInterDotID());
                                    }
                                    found_dot = true;
                                    break;
                                }
                            }
                            if(!found_dot){
                                mEnabledInterDot = null;
                                mEnabledDot = null;
                            }
                        }
                    }
                    else {
                        // 第一帧的处理，无 inter_dot
                        // if there is no inter_dots_list - that is, frameSum == 1
                        // mEnabledDot != null && mEnabledInterDot == null
                        if (mEnabledDot.isInside(event.getX(), event.getY()))
                            mTouchedDot = mEnabledDot;
                        else {
                            boolean found_dot = false;
                            Iterator<String> id_keys = mAnimDotsList.get(mCurrentFrameNo).keySet().iterator();
                            Hashtable<String, Dot> cur_hashtable = mAnimDotsList.get(mCurrentFrameNo);
                            String id;
                            while (id_keys.hasNext()) {
                                id = id_keys.next();
                                Dot dot = cur_hashtable.get(id);
                                if (dot.isInside(event.getX(), event.getY())) {
                                    mEnabledDot = mTouchedDot = dot;
                                    found_dot = true;
                                    break;
                                }
                            }
                            if(!found_dot){
                                mEnabledDot = null;
                            }
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchedDot != null){
                    mTouchedDot.setX(event.getX());
                    mTouchedDot.setY(event.getY());

                    // move dots inbounds; if not, the inter_dot won't be in the right place
                    moveDotInbounds(this, mTouchedDot);
                    // 调整对应中间节点位置 mInterDotsList.get(mCurrentFrameNo).get(mTouchedDot.get)
                    if(mEnabledInterDot != null && !mEnabledInterDot.isTouched()){
                        // 找到上一帧的对应的点，移动当前帧的中间节点
                        Dot enabled_dot_preF = mAnimDotsList.get(mCurrentFrameNo - 1).get(mTouchedDot.getDotID());
                        mEnabledInterDot.setX((enabled_dot_preF.getX() + mTouchedDot.getX())/2);
                        mEnabledInterDot.setY((enabled_dot_preF.getY() + mTouchedDot.getY())/2);
//                        Log.d(TAG, "onTouchEvent: " + mEnabledDotPreF);
                    }

                    // 同时，如果当前帧不是最后一帧，那么也需要移动下一帧的中间节点
                    // notice: the next-frame inter_dot's frame-index is ((mCurrentFrameNo +1) -1)
                    if(mCurrentFrameNo + 1 < getFrameSum()){
                        InterDot next_inter_dot = mInterDotsList.get(mCurrentFrameNo).get(mTouchedDot.getRelativeInterDotID());
                        if(!next_inter_dot.isTouched()) {
                            Dot next_dot = mAnimDotsList.get(mCurrentFrameNo + 1).get(mTouchedDot.getDotID());
                            next_inter_dot.setX((mTouchedDot.getX() + next_dot.getX()) / 2);
                            next_inter_dot.setY((mTouchedDot.getY() + next_dot.getY()) / 2);
                        }
                    }
                }
                else if(mTouchedInterDot != null){
                    mTouchedInterDot.setX(event.getX());
                    mTouchedInterDot.setY(event.getY());
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(mTouchedDot != null) {// reset status
                    // prevent the dot from exceeding the border
                    if(mTouchedDot.getX() - (CIRCLE_RADIUS/2f + DELTA_E) < 0f){
                        mTouchedDot.setX(0f + CIRCLE_RADIUS/2f + DELTA_E);
                    }
                    if(mTouchedDot.getX() + (CIRCLE_RADIUS/2f + DELTA_E) > getWidth()){
                        mTouchedDot.setX(getWidth() - (CIRCLE_RADIUS/2f + DELTA_E));
                    }

                    if(mTouchedDot.getY() - (CIRCLE_RADIUS/2f + DELTA_E) < 0f) {
                        mTouchedDot.setY(0f + CIRCLE_RADIUS/2f + DELTA_E);
                    }
                    if(mTouchedDot.getY() + (CIRCLE_RADIUS/2f + DELTA_E) > getHeight()) {
                        mTouchedDot.setY(getHeight() - (CIRCLE_RADIUS/2f + DELTA_E));
                    }
                    mTouchedDot = null;

                    // restart auto-save
                    resetSavedFlag();
                }
                else if(mTouchedInterDot != null) {// reset status
                    // prevent the dot from exceeding the border
                    if(mTouchedInterDot.getX() - (CIRCLE_RADIUS/2f + DELTA_E) < 0f){
                        mTouchedInterDot.setX(0f + CIRCLE_RADIUS/2f + DELTA_E);
                    }
                    if(mTouchedInterDot.getX() + (CIRCLE_RADIUS/2f + DELTA_E) > getWidth()){
                        mTouchedInterDot.setX(getWidth() - (CIRCLE_RADIUS/2f + DELTA_E));
                    }

                    if(mTouchedInterDot.getY() - (CIRCLE_RADIUS/2f + DELTA_E) < 0f) {
                        mTouchedInterDot.setY(0f + CIRCLE_RADIUS/2f + DELTA_E);
                    }
                    if(mTouchedInterDot.getY() + (CIRCLE_RADIUS/2f + DELTA_E) > getHeight()) {
                        mTouchedInterDot.setY(getHeight() - (CIRCLE_RADIUS/2f + DELTA_E));
                    }
                    // 移动中间节点后，需要设置其为移动过的点，不再随节点调整位置
                    if(!mTouchedInterDot.isTouched())
                        mTouchedInterDot.touched();
                    mTouchedInterDot = null;

                    // restart auto-save
                    resetSavedFlag();
                }
                invalidate();
                break;
            default:
                break;
        }

        return true;
    }

    private void initCirclePaint(){
        // dashed circle paint
        float[] intervals = new float[2];
        double circumference = 2 * Math.PI * CIRCLET_RADIUS;
        float dashPlusGapSize = (float) (circumference / NUM_DASHES);
        intervals[0] = dashPlusGapSize * DASH_PORTION;
        intervals[1] = dashPlusGapSize * (1f - DASH_PORTION);
        DashPathEffect dashCircleEffect = new DashPathEffect(intervals, 0);

        mOffCircletPaint = new Paint();
//        mOffCircletPaint.setStyle(Paint.Style.STROKE);
//        mOffCircletPaint.setStrokeWidth(5f);
//        mOffCircletPaint.setPathEffect(dashCircleEffect);
        mDefCircletPaint = new Paint(mOffCircletPaint);
        mDiscCircletPaint = new Paint(mOffCircletPaint);

        mOffCircletPaint.setColor(ContextCompat.getColor(getContext(), R.color.Gold));
        mOffCircletPaint.setAlpha(DOT_ALPHA);// max 255
        mDefCircletPaint.setColor(ContextCompat.getColor(getContext(), R.color.PaleTurquoise));
        mDefCircletPaint.setAlpha(DOT_ALPHA);// max 255
        mDiscCircletPaint.setColor(ContextCompat.getColor(getContext(), R.color.Seashell));
        mDiscCircletPaint.setAlpha(DOT_ALPHA);// max 255

        // the outline of the circle
        mHollowCirclePaint = new Paint(mDiscCircletPaint);
        mHollowCirclePaint.setStrokeWidth(STROKE_WIDTH);
        mHollowCirclePaint.setStyle(Paint.Style.STROKE);
        mHollowCirclePaint.setAlpha(DOT_ALPHA);// max 255
        mHollowCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.OutlineColor));

        mInterCirclePaint = new Paint(mOffCircletPaint);
        mInterCirclePaint.setColor(ContextCompat.getColor(getContext(), R.color.InterDotColor));
        mInterCirclePaint.setAlpha(INTER_DOT_ALPHA);// max 255
    }

    private void initDashLinePaint() {
        int alpha_line = 120;
        // dashed line paint
        mOffLinePaint = new Paint();
        mOffLinePaint.setStrokeWidth(4f);
        DashPathEffect dashLineEffect = new DashPathEffect(new float[]{15f,18f}, 0f);
        mOffLinePaint.setPathEffect(dashLineEffect);

        mDefLinePaint = new Paint(mOffLinePaint);
        mDiscLinePaint = new Paint(mOffLinePaint);

        mOffLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.Gold));
        mOffLinePaint.setAlpha(alpha_line);// max 255
        mDefLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.PaleTurquoise));
        mDefLinePaint.setAlpha(alpha_line);// max 255
        mDiscLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.Seashell));
        mDiscLinePaint.setAlpha(alpha_line);// max 255
    }

    private boolean isAnimationPlaying(){
        return mAnimationMark;
    }

    public void stopAnimationPlaying(){
        mAnimationMark = false;
        mAnimDiscBoardListener.onAnimationStop();
    }

    void preprocessAniData(int frameNo){
        final Hashtable<String, Dot> cur_dots_list = mAnimDotsList.get(frameNo);//TO CHANGE
        final Hashtable<String, Dot> next_dots_list = mAnimDotsList.get(frameNo + 1);
        final Hashtable<String, InterDot> cur_inter_dots_list = mInterDotsList.get(frameNo);
        cur_dots_list.forEach((id, cur_dot) -> {
            Dot next_dot = next_dots_list.get(id);
            InterDot inter_dot = cur_inter_dots_list.get(cur_dot.getRelativeInterDotID());
            assert next_dot != null;
            float cur_x = cur_dot.getX();
            float cur_y = cur_dot.getY();
            float next_x = next_dot.getX();
            float next_y = next_dot.getY();
            float inter_x = inter_dot.getX();
            float inter_y = inter_dot.getY();

//            float delta_x = MathUtils.lerp(0f, next_x - cur_x, AMOUNT);
//            float delta_y = MathUtils.lerp(0f, next_y - cur_y, AMOUNT);
//            DeltaXY deltaXY = new DeltaXY(cur_x, cur_y, delta_x, delta_y);
//            mDeltaXYHashtable.put(id, deltaXY);

            // 绘制经过中间节点的动画，需要用到 pathMeasure
            Path path = new Path();
            path.moveTo(cur_x,cur_y);
            path.lineTo(inter_x,inter_y);
            path.lineTo(next_x,next_y);
            PathMeasure pathMeasure = new PathMeasure(path, false);
            mPathMeasureHashtable.put(id, pathMeasure);
            // store step_size and the path length that has walked
            Dist dist = new Dist(0, pathMeasure.getLength() / STEP_NUM);
            mDistHashtable.put(id, dist);
        });
    }
    public void startAnimationPlaying() {
        if(getFrameSum() >=2 ) {
            mAniCurrentFrameNo = 0;
            mAniFrameNoLimit = getFrameSum();

            // plan A straight line
//            mDeltaXYHashtable.clear();
            // plan B curved line
            mPathMeasureHashtable.clear();
            mDistHashtable.clear();

            preprocessAniData(mAniCurrentFrameNo);
            mStep = 0;
            mAnimationMark = true;

            mAnimDiscBoardListener.onAnimationStart();
            // 保持滑动帧一致
            setCurrentFrameNo(0);
        }
        // getFrameSum() < 2
        else{
            Toast.makeText(getContext(), R.string.no_anim_hint, Toast.LENGTH_SHORT).show();
        }
    }

    /** for the canvas animation not being blinked when rendering
     * at least one drawCircle function should be called every branch should be
     */
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        /* draw dot with the right
         * 2D Coordinate, Color, Radius, SequenceNumber
         */
        if(!mAnimDotsList.isEmpty()) {
            // animation part
            // commented lines are from plan A
            if(isAnimationPlaying()){
                final Hashtable<String, Dot> cur_dots_list = mAnimDotsList.get(mAniCurrentFrameNo);
                for (Map.Entry<String, Dot> entry : cur_dots_list.entrySet()) {
                    String id = entry.getKey();
                    Dot cur_dot = entry.getValue();
// plan A
//                    DeltaXY deltaXY = mDeltaXYHashtable.get(id);
//                    assert deltaXY != null;
//                    float pos_x = deltaXY.getPre_X() + deltaXY.getDelta_X();
//                    float pos_y = deltaXY.getPre_Y() + deltaXY.getDelta_Y();
//                    deltaXY.setPre_X(pos_x);
//                    deltaXY.setPre_Y(pos_y);
//                    mDeltaXYHashtable.replace(id, deltaXY);

                    // 将因导入造成的外溢点转移到内部
//                    moveDotInbounds(canvas, cur_dot, pos_x, pos_y);

                    releasePos();// reset pathMeasure, in case that getPosTan does nothing to pos
                    PathMeasure pathMeasure = mPathMeasureHashtable.get(id);
                    Dist dist = mDistHashtable.get(id);
                    float d = dist.getDist();
                    pathMeasure.getPosTan(d, pos, tan);
                    dist.goForward();

                    // if the dot hasn't moved, the pos will be 0.0
                    if (pos[0] == 0f) {
                        pos[0] = cur_dot.getX();
                    }
                    if (pos[1] == 0f) {
                        pos[1] = cur_dot.getY();
                    }
//                    Log.d(TAG, "onDraw: " + pos[0] + pos[1]);

                    if (Dot.isDefense(id)) {
//                        canvas.drawCircle(pos_x, pos_y, CIRCLE_RADIUS, mDefPaint);
//
//                        float res_y = pos_y - mTextOffsetY;
//                        canvas.drawText(String.valueOf(cur_dot.getSeqNo()), pos_x, res_y, mSequenceTextPaint);
                        canvas.drawCircle(pos[0], pos[1], CIRCLE_RADIUS, mDefPaint);

                        float res_y = pos[1] - mTextOffsetY;
                        canvas.drawText(String.valueOf(cur_dot.getSeqNo()), pos[0], res_y, mSequenceTextPaint);
                    } else if (Dot.isOffense(id)) {
//                        canvas.drawCircle(pos_x, pos_y, CIRCLE_RADIUS, mOffPaint);
//
//                        float res_y = pos_y - mTextOffsetY;
//                        canvas.drawText(String.valueOf(cur_dot.getSeqNo()), pos_x, res_y, mSequenceTextPaint);
                        canvas.drawCircle(pos[0], pos[1], CIRCLE_RADIUS, mOffPaint);

                        float res_y = pos[1] - mTextOffsetY;
                        canvas.drawText(String.valueOf(cur_dot.getSeqNo()), pos[0], res_y, mSequenceTextPaint);
                    } else {
//                        canvas.drawCircle(pos_x, pos_y, CIRCLE_RADIUS, mDiscPaint);
                        canvas.drawCircle(pos[0], pos[1], CIRCLE_RADIUS, mDiscPaint);
                    }
                }
                mStep += 1;// the Step count mark should be outside the forEach loop
                if (mStep > STEP_NUM) {
                    mAniCurrentFrameNo += 1;
                    if(mAniCurrentFrameNo + 1 < mAniFrameNoLimit) {
                        mStep = 0;
                        preprocessAniData(mAniCurrentFrameNo);
                        // 保持滑动帧数一致
                        setCurrentFrameNo(mAniCurrentFrameNo);
                    }
                    else {
                        stopAnimationPlaying();
                        // 动画播放结束，回到最后一帧
                        setCurrentFrameNo(mAniCurrentFrameNo);
                    }
                }
            }
            else{//if(!isAnimationPlaying())
                // 高亮enabled_dot，并绘制touched_inter_dot
                // enabled_dot and touched_inter_dot highlighted
                if (mEnabledDot != null){
                    moveDotInbounds(canvas, mEnabledDot);
                    canvas.drawCircle(mEnabledDot.getX(), mEnabledDot.getY(), CIRCLE_RADIUS + STROKE_WIDTH/2, mHollowCirclePaint);

                    // draw mEnabledInterDot
                    if(mEnabledInterDot != null) {
                        moveDotInbounds(canvas, mEnabledInterDot);
                        canvas.drawCircle(mEnabledInterDot.getX(), mEnabledInterDot.getY(), INTER_DOT_RADIUS, mInterCirclePaint);
                    }
                }

                // draw lines between every 2 frame, and draw previous frame dots with hollow strokes
                // 前一帧与后一帧的连线，只有在帧数>=2 & mCurrentFrameNo >=1 时才进行操作
                if (getFrameSum() >= 2 & mCurrentFrameNo >= 1) {
                    Hashtable<String, Dot> pre_dots_list = mAnimDotsList.get(mCurrentFrameNo - 1);
                    Hashtable<String, Dot> cur_dots_list = mAnimDotsList.get(mCurrentFrameNo);
                    pre_dots_list.forEach((id, pre_dot) -> {
                        Dot cur_dot = cur_dots_list.get(id);
                        InterDot inter_dot = mInterDotsList.get(mCurrentFrameNo - 1).get(pre_dot.getRelativeInterDotID());
                        assert cur_dot != null;
                        assert inter_dot != null;
                        moveDotInbounds(canvas, cur_dot);
                        float pre_x, pre_y, cur_x, cur_y, inter_x, inter_y;
                        pre_x = pre_dot.getX();
                        pre_y = pre_dot.getY();
                        cur_x = cur_dot.getX();
                        cur_y = cur_dot.getY();
                        inter_x = inter_dot.getX();
                        inter_y = inter_dot.getY();

                        // 将导入的外溢点转移到内部
                        // draw pre-frame dot in canvas
                        if (cur_dot.isOffense()) {
                            canvas.drawCircle(pre_x, pre_y, CIRCLET_RADIUS, mOffCircletPaint);
                            canvas.drawLine(pre_x, pre_y, inter_x, inter_y, mOffLinePaint);
                            canvas.drawLine(inter_x, inter_y, cur_x, cur_y, mOffLinePaint);
                        } else if (cur_dot.isDefense()) {
                            canvas.drawCircle(pre_x, pre_y, CIRCLET_RADIUS, mDefCircletPaint);
                            canvas.drawLine(pre_x, pre_y, inter_x, inter_y, mDefLinePaint);
                            canvas.drawLine(inter_x, inter_y, cur_x, cur_y, mDefLinePaint);
                        } else if (cur_dot.isDisc()) {
                            canvas.drawCircle(pre_x, pre_y, CIRCLET_RADIUS, mDiscCircletPaint);
                            canvas.drawLine(pre_x, pre_y, inter_x, inter_y, mDiscLinePaint);
                            canvas.drawLine(inter_x, inter_y, cur_x, cur_y, mDiscLinePaint);
                        }
                    });
                }

                // draw dots
                mAnimDotsList.get(mCurrentFrameNo).forEach((dot_ID, dot) -> {
                    // 将因为导入造成的外溢点转移到内部
                    moveDotInbounds(canvas, dot);

                    if (dot.isDefense()) {
                        canvas.drawCircle(dot.getX(), dot.getY(), CIRCLE_RADIUS, mDefPaint);

                        float origin_Y = dot.getY();
                        float resY = origin_Y - mTextOffsetY;
                        canvas.drawText(String.valueOf(dot.getSeqNo()), dot.getX(), resY, mSequenceTextPaint);
                    } else if (dot.isOffense()) {
                        canvas.drawCircle(dot.getX(), dot.getY(), CIRCLE_RADIUS, mOffPaint);

                        float origin_Y = dot.getY();
                        float res_Y = origin_Y - mTextOffsetY;
                        canvas.drawText(String.valueOf(dot.getSeqNo()), dot.getX(), res_Y, mSequenceTextPaint);
                    } else {
                        canvas.drawCircle(dot.getX(), dot.getY(), CIRCLE_RADIUS, mDiscPaint);
                    }
                });
            }
        }

        invalidate();
    }


    void releasePos(){
        pos[0] = 0f;
        pos[1] = 0f;
    }
    /** set mCurrentFrameNo, and update the relative UI
     *
     * @param currentFrameNo
     */
    public void setCurrentFrameNo(int currentFrameNo) {
        mCurrentFrameNo = currentFrameNo;
        // update the outer UI
        if(mAnimDiscBoardListener != null)
        {
            mAnimDiscBoardListener.onCurrentFrameNoChange();
        }

        releaseTouchDots();
        invalidate();
    }

    private void onFrameSumChange() {
        if(mAnimDiscBoardListener != null)
        {
            mAnimDiscBoardListener.onFrameSumChange();
        }
    }

    /**
     * save mAniDotsData into one file of SharedPreferences, whose name is user's input
     * use currentFrameNo as a counter to track, and save
     *
     * @param temp_name->the saved anim_dots_list data's name
     * */
    public void saveAniDots(String temp_name){
        mJsonDataHelper.addAniTempToPref(temp_name);//add a new name to the temp list
        mJsonDataHelper.saveAnimDotsToPrefNew(temp_name, mAnimDotsList, mInterDotsList);

        setSavedFlag();
    }

    /**
     * load anim_dots_list data from preferences using template name
     *
     * @param temp_name
     */
    AnimTemp loadAnimDotsFromTemp(String temp_name) {
        ArrayList<Hashtable<String, Dot>> anim_dots_list = new ArrayList<>();
        ArrayList<Hashtable<String, InterDot>> inter_dots_list = new ArrayList<>();
        SharedPreferences shared = getContext().getSharedPreferences(temp_name, MODE_PRIVATE);
        int currentFrameNo = 0;
        String json_s = shared.getString(String.valueOf(currentFrameNo), "");
        if(!Objects.equals(json_s, "")){
            clearBoard();

            while(!Objects.equals(json_s, "")) {
                if(currentFrameNo % 2 == 0) {// 偶数为帧，奇数为中间帧
                    ArrayList<Dot> arr = new ArrayList<>();
                    Dot[] dots = mGson.fromJson(json_s, Dot[].class);
                    Collections.addAll(arr, dots);
//                    Log.d(TAG, "loadAnimDotsFromTemp: " + Arrays.toString(dots));
                    Hashtable<String, Dot> hashtable = mJsonDataHelper.Array2Hashtable(arr);
                    anim_dots_list.add(hashtable);
                }
                else {
                    ArrayList<InterDot> inter_arr = new ArrayList<>();
                    InterDot[] inter_dots = mGson.fromJson(json_s, InterDot[].class);
                    Collections.addAll(inter_arr, inter_dots);
//                    Log.d(TAG, "loadAnimDotsFromTemp: " + Arrays.toString(inter_dots));
                    Hashtable<String, InterDot> inter_hashtable = mJsonDataHelper.InterArray2Hashtable(inter_arr);
                    inter_dots_list.add(inter_hashtable);
                }
                currentFrameNo += 1;
                json_s = shared.getString(String.valueOf(currentFrameNo), "");
            }
        }

        mTempName = temp_name;

        return  new AnimTemp(anim_dots_list, inter_dots_list);
    }

    /**
     * load dots by name, set the currentFrameNo to 0
     * and update UI(eg. slider)
     * */
    public void loadDotsAndUpdateUI(String temp_name){
        AnimTemp animTemp = loadAnimDotsFromTemp(temp_name);
        mAnimDotsList = animTemp.getAnimDotsList();
        mInterDotsList = animTemp.getInterDotsList();

        // listener to change the save btn mark in the fragment
        mAnimDiscBoardListener.onLoad();
        // update the UI
        onFrameSumChange();
        setCurrentFrameNo(0);
        invalidate();
    }

    public String getTempName() {
        return mTempName;
    }

    void initAnimSpeed(){
        int speed = mJsonDataHelper.getIntegerFromUserPreferences(USER_DATA_ANIM_SPEED);

        if(speed == -1){
            STEP_NUM = ANIM_SPEED_INIT;
        }
        else {
            STEP_NUM = speed;
        }
//        AMOUNT = 1f / (float) STEP_NUM;
    }

    void releaseTouchDots(){
        mEnabledDot = null;
        mEnabledInterDot = null;
        mTouchedDot = null;
        mTouchedInterDot = null;
    }
}