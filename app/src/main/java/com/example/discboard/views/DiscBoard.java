package com.example.discboard.views;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.discboard.JsonDataHelper;
import com.example.discboard.R;
import com.example.discboard.datatype.Dot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import static com.example.discboard.DiscFinal.*;
/**
 * Discboard
 * only used for static disc board strategy showing
 */
public class DiscBoard extends View {
    private static final float DELTA_E = 25f;
    private final String TAG = "DiscBoard JAVA CLASS";
    private ArrayList<Dot> mStaticDots;
    private int mToggleDotType;
    private int mOffenseNum, mDefenseNum;
    Paint mDefPaint, mOffPaint, mDiscPaint;
    Paint mSequenceTextPaint;
    float mDeltaY, mTextOffsetY;
    Dot mTouchedDot;
    JsonDataHelper mJsonDataHelper;
    Gson gson;
    Dot SingleDiscDot;

    // storage Mode mark for every different template canvas
    private enum TemplateMark{
        DEFAULT,
        THREE,
        FIVE,
        SEVEN
    }
    private TemplateMark templateMark = TemplateMark.DEFAULT;

    public interface DiscBoardListener{
    }
    DiscBoardListener mDiscBoardListener;

    public void setDiscBoardListener(DiscBoardListener discBoardListener) {
        mDiscBoardListener = discBoardListener;
    }

    public DiscBoard(Context context) {
        super(context);
        init(null);
    }

    public DiscBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public DiscBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public DiscBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    void init(@Nullable AttributeSet set){
        mJsonDataHelper = new JsonDataHelper(getContext());
        initGson();
        initPaintColor();

        mStaticDots = new ArrayList<>();

        mToggleDotType = 1;// init mDotType to offense

        mOffenseNum = 0;
        mDefenseNum = 0;

        loadDefaultTemp();
        invalidate();
    }

    // initiate the gson member
    void initGson() {
        gson = new Gson();
    }

    void initPaintColor() {
        initDotPaint();
        initTextPaint();
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

    // turn the dot type to Offense
    public void setOffense(){
        mToggleDotType = 1;
    }

    // turn the dot type to Defense
    public void setDefense(){
        mToggleDotType = -1;
    }

    /*
    * 添加棋子
    *
    * 功能：添加 mDotType 类型的棋子
    * 限制棋子数目为 7 个
    * */
    public void addDot() {
        if(mToggleDotType == 1 && mOffenseNum < 7) {
            mOffenseNum++;
            mStaticDots.add(new Dot(100f, 100f, mToggleDotType, mOffenseNum));
        }
        else if(mToggleDotType == -1 && mDefenseNum < 7) {
            mDefenseNum++;
            mStaticDots.add(new Dot(100f, 100f, mToggleDotType, mDefenseNum));
        }
        // update the canvas
        postInvalidate();
    }

    /**
     * 保存数据
     *
     * 功能：测试棋子位置的数据是否能保存到本地
     * */
    public void saveTemp(String temp_name) {
        String s = transformData2Json(mStaticDots);

        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(temp_name, s);
        editor.apply();
    }

    String transformData2Json(Object object){
        return gson.toJson(object);
    }

    private void clearBoard() {
        clearDots();
    }
        /*
     * 清空棋子
     *
     * 功能：清空棋子，只留下飞盘棋子
     * */
    private void clearDots() {
        mStaticDots.clear();

        mOffenseNum = 0;
        mDefenseNum = 0;
    }

    // clear board, and add a single white disc dot to it
    public void resetStaticBoard() {
        clearBoard();
        addDiscDot();

        invalidate();
    }

    private void addDiscDot(){
        SingleDiscDot = new Dot(100, 100, 0, 0);
        mStaticDots.add(SingleDiscDot);
    }

    /**
     *  this function is from other LOAD function
     *  load default temp from SharedPreferences, not from assets
     *
     *  if user hasn't saved a template, then adding a single disc to the board
     */
    public void loadDefaultTemp() {
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, getContext().MODE_PRIVATE);//获取关键字为 s 的存储单元
        String s = shared.getString("my_temp", "");

        // clear board first
        clearBoard();

        mOffenseNum = 0;
        mDefenseNum = 0;
        assert s != null;
        if(!s.equals("")) {

            Dot[] dot_array = gson.fromJson(s, Dot[].class);

            for (Dot dot : dot_array) {
                mStaticDots.add(dot);
                // 棋子数量
                if(dot.isOffense()){
                    mOffenseNum++;
                }
                else if(dot.isDefense()){
                    mDefenseNum++;
                }
            }
        }
        //  if this is the first time that user uses default temp
        // then init a single white disc dot to it
        else {
            // the original white disc dot
            addDiscDot();
        }

        invalidate();
    }

    /**
     * load3presets
     * 提供读取3棋子预设功能
     */
    public void load3Preset() {
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_3, null);

        assert s != null;//如果没有用过 SharedPreferences 的情况，即 s 中没有数据为null的情况
        if(!s.equals("")) {// 如果存放有数据
//            Log.d(TAG, "load3Preset: " + s);
            mStaticDots.clear();
            mOffenseNum = 3;
            mDefenseNum = 3;
            Dot[] dot_array = gson.fromJson(s, Dot[].class);
            Collections.addAll(mStaticDots, dot_array);
        }

        // refresh the canvas
        invalidate();
    }

    /**
     * load5Presets
     * 提供读取5棋子预设功能
     */
    public void load5Preset() {
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_5, null);

        assert s != null;
        if(!s.equals("")) {
            mStaticDots.clear();
            mOffenseNum = 5;
            mDefenseNum = 5;
            Dot[] dot_array = gson.fromJson(s, Dot[].class);
            Collections.addAll(mStaticDots, dot_array);
        }

        invalidate();// 重新绘图
    }

    /**
    * load7Preset
    * 提供读取竖排棋子预设功能
    */
    public void loadVerstackPreset() {
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_VER, null);

        assert s != null;
        if(!s.equals("")) {
            mStaticDots.clear();
            mOffenseNum = 7;
            mDefenseNum = 7;
            Dot[] dot_array = gson.fromJson(s, Dot[].class);
            Collections.addAll(mStaticDots, dot_array);
        }

        invalidate();// 重新绘图
    }

    /**
     * loadHostackPreset
     * 提供读取横排棋子预设功能
     */
    public void loadHostackPreset() {
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        String s = shared.getString(USER_DATA_TEMP_HO, null);

        assert s != null;
        if(!s.equals("")) {
            mStaticDots.clear();
            mOffenseNum = 7;
            mDefenseNum = 7;
            Dot[] dot_array = gson.fromJson(s, Dot[].class);
            Collections.addAll(mStaticDots, dot_array);
        }

        invalidate();// 重新绘图
    }

    /**
     * the most important function, for user to moving the dots
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStaticDots.forEach(dot -> {
                    if (dot.isInside(event.getX(), event.getY())) {
                        mTouchedDot = dot;
                    }
                });
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchedDot != null){
                    mTouchedDot.setX(event.getX());
                    mTouchedDot.setY(event.getY());
                    /*
                     * 将棋子放在触点左下方，方便用户操作
                     * */
//                    mTouchedDot.x = event.getX() - mTouchOffset;
//                    mTouchedDot.y = event.getY() + mTouchOffset;
//                    Log.d("CustomView", "dot moving x:" + touchedDot.x + " y:" + touchedDot.y);
                    invalidate();
                }
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

                    invalidate();
                    mTouchedDot = null;
                }
                break;
            default:
                break;
        }

        return true;
    }

    /**
     * rendering dots from the mStaticDots
     * */
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        // background color
//        canvas.drawColor(Color.parseColor("#53d16e"));

        /* draw dot with the right
         * 2D Coordinate, Color, Radius, SequenceNumber
         */
        mStaticDots.forEach(dot -> {
            // 将因导入造成的外溢点转移到内部
            moveCircleInbounds(canvas, dot);

            if(dot.isDefense()) {
                canvas.drawCircle(dot.getX(), dot.getY(), CIRCLE_RADIUS, mDefPaint);

                float origin_Y = dot.getY();
                float resY = origin_Y - mTextOffsetY;
                canvas.drawText(String.valueOf(dot.getSeqNo()), dot.getX(), resY, mSequenceTextPaint);
            }
            else if(dot.isOffense()) {
                canvas.drawCircle(dot.getX(), dot.getY(), CIRCLE_RADIUS, mOffPaint);

                float origin_Y = dot.getY();
                float resY = origin_Y - mTextOffsetY;
                canvas.drawText(String.valueOf(dot.getSeqNo()), dot.getX(), resY, mSequenceTextPaint);
            }
            else {
                canvas.drawCircle(dot.getX(), dot.getY(), CIRCLE_RADIUS, mDiscPaint);
            }
        });
    }
}
