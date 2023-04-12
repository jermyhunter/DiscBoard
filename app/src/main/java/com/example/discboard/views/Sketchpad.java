package com.example.discboard.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.discboard.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import com.example.discboard.DiscFinal.PaintType;

public class Sketchpad extends View {
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4f;
    private static final float RECT_DELTA = 10f;
    enum ActionType{
        Paint,
        Erase
    }
    ActionType mActionType;
    String TAG = "Sketchpad";
    Bitmap mBitmap;
    Canvas mCanvas;
    Path mPath;
//    Paint mPaint;
    Paint mCirclePaint;
    Path mTouchCirclePath;
    int mPathID;
    ArrayList<Hashtable<Integer, Boolean>> mPathDelMarkHistory;
    // the MAX size of path_del_mark_history
    final static int HISTORY_LIMIT = 20;
    int mHistoryPos;// 0 is the first
    static Paint[] paintTypes;
    int mPaintType;
    public void setPaintType(int paintType) {
        mPaintType = paintType;
    }

    class PathPaint{
        Path path;
        int paintType;
        PathPaint(Path path, int paintType){
            this.path = path;
            this.paintType = paintType;
        }

        public Path getPath() {
            return path;
        }

        public int getPaintType() {
            return paintType;
        }
    }
//    Hashtable<Integer, Path> mPathListTB;
    Hashtable<Integer, PathPaint> mPathPaintListTB;
    Hashtable<Integer, Region> mLineBoundsTB;
    Hashtable<Integer, Boolean> mPathDelMarkTB;
    boolean mPathDelMark;
    HashSet<Integer> mUsedPathID;
    static final float PAINT_WIDTH = 10f;

    public Sketchpad(Context context) {
        super(context);
        init(null);
    }

    public Sketchpad(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public Sketchpad(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public Sketchpad(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(Object o) {
        initPaint();

        mPath = new Path();
        mCirclePaint = new Paint();
        mTouchCirclePath = new Path();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(Color.BLUE);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeJoin(Paint.Join.MITER);
        mCirclePaint.setStrokeWidth(4f);

        // init action_type
        mActionType = ActionType.Paint;

//        mPathListTB = new Hashtable<>();
        mPathPaintListTB = new Hashtable<>();
        mLineBoundsTB = new Hashtable<>();
        mPathDelMarkTB = new Hashtable<>();

        mPathDelMarkHistory = new ArrayList<>();
        mPathDelMarkHistory.add(0, new Hashtable<>());// add the first frame
        mHistoryPos = 0;

        mUsedPathID = new HashSet<>();
        mPathID = 0;
    }

    void initPaint(){
//        mPaint = new Paint();
//        mPaint.setAntiAlias(true);
//        mPaint.setDither(true);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeJoin(Paint.Join.ROUND);
//        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        mPaint.setStrokeWidth(PAINT_WIDTH);
//
//        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.paint_red));

        mPaintType = PaintType.Red.getValue();
        paintTypes = new Paint[5];
        for(int i = 0; i < 5; i++){
            paintTypes[i] = new Paint();
            paintTypes[i].setAntiAlias(true);
            paintTypes[i].setDither(true);
            paintTypes[i].setStyle(Paint.Style.STROKE);
            paintTypes[i].setStrokeJoin(Paint.Join.ROUND);
            paintTypes[i].setStrokeCap(Paint.Cap.ROUND);
            paintTypes[i].setStrokeWidth(PAINT_WIDTH);
        }
        paintTypes[0].setColor(ContextCompat.getColor(getContext(), R.color.paint_red));
        paintTypes[1].setColor(ContextCompat.getColor(getContext(), R.color.paint_blue));
        paintTypes[2].setColor(ContextCompat.getColor(getContext(), R.color.paint_orange));
        paintTypes[3].setColor(ContextCompat.getColor(getContext(), R.color.paint_white));
        paintTypes[4].setColor(ContextCompat.getColor(getContext(), R.color.paint_black));
    }

    // force to update mCanvas & mBitmap, used for drawing the canvas
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // create an empty bitmap to fill canvass
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (mActionType){
            case Paint:
                // save line cache to bitmap, then render it to mCanvas
//                mPathListTB.forEach((id, path) -> {
//                    if(mPathDelMarkTB.containsKey(id) &&
//                            Boolean.FALSE.equals(mPathDelMarkTB.get(id))){
//                        canvas.drawPath(path, mPaint);
//                    }
//                });
                mPathPaintListTB.forEach((id, pathPaint) -> {
                    if(mPathDelMarkTB.containsKey(id) &&
                            Boolean.FALSE.equals(mPathDelMarkTB.get(id))){
                        canvas.drawPath(pathPaint.getPath(), paintTypes[pathPaint.getPaintType()]);
                    }
                });
                canvas.drawPath(mPath, paintTypes[mPaintType]);
                canvas.drawPath(mTouchCirclePath, mCirclePaint);
                break;
            case Erase:
//                mPathListTB.forEach((id, path) -> {
//                    if(mPathDelMarkTB.containsKey(id) &&
//                            Boolean.FALSE.equals(mPathDelMarkTB.get(id))){
//                        canvas.drawPath(path, mPaint);
//                    }
//                });
                mPathPaintListTB.forEach((id, pathPaint) -> {
                    if(mPathDelMarkTB.containsKey(id) &&
                            Boolean.FALSE.equals(mPathDelMarkTB.get(id))){
                        canvas.drawPath(pathPaint.getPath(), paintTypes[pathPaint.getPaintType()]);
                    }
                });
                // not to draw the cut line
                canvas.drawPath(mTouchCirclePath, mCirclePaint);
                break;
        }
    }

    private void paintStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void paintMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;

            mTouchCirclePath.reset();
            mTouchCirclePath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void paintEnd() {
        mPath.lineTo(mX, mY);
        mTouchCirclePath.reset();
        // commit the path to our offscreen

        int pathID = mPathID;
        // 1.record the drawn path
//        mPathListTB.put(pathID, mPath);
        mPathPaintListTB.put(pathID, new PathPaint(mPath, mPaintType));

        // 2.precompute the contour for latter erasing use
        RectF rectF = new RectF();
        mPath.computeBounds(rectF, true);
        Region r = new Region();
        r.setPath(mPath, new Region((int) (rectF.left - RECT_DELTA), (int) (rectF.top - RECT_DELTA),
                (int) (rectF.right + RECT_DELTA), (int) (rectF.bottom + RECT_DELTA)));
        mLineBoundsTB.put(pathID, r);
        // 3.assign the delete mark
        mPathDelMarkTB.put(pathID, false);
        incPathID();

        // history
        updateHistory();
        mPathDelMarkHistory.add(new Hashtable<>(mPathDelMarkTB));
        incHistoryPos();
        applyDelMark();

        // kill this so we don't double draw
        mPath = new Path();
    }

    private void eraseStart(float x, float y) {
        mPathDelMark = false;
    }

    private void eraseMove(float x, float y) {
        mPathPaintListTB.forEach((id, pathPaint) -> {
            if(mLineBoundsTB.get(id).contains((int) x, (int) y)) {
//                    mLineBoundsTB.remove(i);
//                    mPathListTB.remove(i);
                mPathDelMarkTB.put(id, true);
                // this touch_event has deleted at least one line
                if(!mPathDelMark)
                    mPathDelMark = true;
            }
        });

        mX = x;
        mY = y;

        mTouchCirclePath.reset();
        mTouchCirclePath.addCircle(mX, mY, 30, Path.Direction.CW);
//        }
    }

    private void eraseEnd() {
        mTouchCirclePath.reset();
        // commit the path to our offscreen

        // history
        if(mPathDelMark) {
            updateHistory();
            mPathDelMarkHistory.add(new Hashtable<>(mPathDelMarkTB));
            incHistoryPos();
            applyDelMark();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (mActionType){
                    case Paint:
                        paintStart(x, y);
                        break;
                    case Erase:
                        eraseStart(x, y);
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mActionType){
                    case Paint:
                        paintMove(x, y);
                        break;
                    case Erase:
                        eraseMove(x, y);
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (mActionType){
                    case Paint:
                        paintEnd();
                        break;
                    case Erase:
                        eraseEnd();
                        break;
                }
                break;
        }
        invalidate();
        return true;
    }

    public void clearAll(){
        clearDrawing();

        clearHistory();
    }

    private void clearHistory() {
        mLineBoundsTB.clear();
        mPathDelMarkTB.clear();

        mPathDelMarkHistory = new ArrayList<>();
        mPathDelMarkHistory.add(0, new Hashtable<>());// add the first frame
        mHistoryPos = 0;

        mUsedPathID.clear();
        mPathID = 0;
    }

    public void clearDrawing()
    {
        // don't forget that one and the match below,
        // or you just keep getting a duplicate when you save.
        mPathPaintListTB.clear();
//        mPathListTB.clear();
        invalidate();
    }

    public void startPainting() {
        mActionType = ActionType.Paint;
    }

    public void startErasing() {
        mActionType = ActionType.Erase;
    }

    public void revokeAction(){
        if(mHistoryPos > 0) {
            decHistoryPos();
            mPathDelMarkTB = new Hashtable<>(mPathDelMarkHistory.get(mHistoryPos));
        }
        invalidate();
    }

    public void redoAction(){
        if(mHistoryPos < mPathDelMarkHistory.size() - 1){
            incHistoryPos();
            mPathDelMarkTB = new Hashtable<>(mPathDelMarkHistory.get(mHistoryPos));
//            Log.d(TAG, "revokeAction: " + mHistoryPos);
//            Log.d(TAG, "redoAction: " + mPathDelMarkTB);
//            Log.d(TAG, "redoAction: " + mPathDelMarkHistory);
        }
        invalidate();
    }

    /**  once mPathDelMarkHistory reaches the LIMIT, check all mark of every history step
     *   delete those all TRUE lines
     *
     *   when invoked, mHistoryPos == mPathDelMarkHistory.size() - 1
     */
    void applyDelMark(){
        if(mPathDelMarkHistory.size() > HISTORY_LIMIT){
            if (mPathDelMarkHistory.size() > mHistoryPos) {
                mPathDelMarkHistory.remove(0);
                mHistoryPos = mPathDelMarkHistory.size() - 1;
            }
            HashSet<Integer> usedPathID = new HashSet<>(mUsedPathID);
            // save line cache to bitmap, then render it to mCanvas
            usedPathID.forEach(id -> {
                boolean del_mark = true;
                for(int i = 0; i < mPathDelMarkHistory.size(); i++){
                    Hashtable<Integer, Boolean> hashtable = mPathDelMarkHistory.get(i);
                    if(Boolean.FALSE.equals(hashtable.get(id))) {
                        del_mark = false;
                        break;
                    }
                }
                if(del_mark) {
                    for(int i = 0; i < mPathDelMarkHistory.size(); i++){
                        Hashtable<Integer, Boolean> hashtable = mPathDelMarkHistory.get(i);
                        hashtable.remove(id);
                    }
                    mLineBoundsTB.remove(id);
//                    mPathListTB.remove(id);
                    mPathPaintListTB.remove(id);
                    mPathDelMarkTB.remove(id);
                    mUsedPathID.remove(id);
                }
            });
        }
    }

    // if mHistoryPos is not the last index of history, cut off [mHistoryPos,...]
    void updateHistory(){
        if(mHistoryPos != mPathDelMarkHistory.size() - 1){
            // clear history from step 0 to mHistoryPos
            if (mPathDelMarkHistory.size() > mHistoryPos + 1) {
                mPathDelMarkHistory.subList(mHistoryPos + 1, mPathDelMarkHistory.size()).clear();
                mHistoryPos = 1;// the index of 0 is empty, so mHistoryPos is at least 1
            }
        }
    }

    // only increase
    void incPathID(){
        // record pathID
        mUsedPathID.add(mPathID);
        mPathID++;
    }

    private void incHistoryPos() {
        mHistoryPos++;
    }

    private void decHistoryPos() {
        mHistoryPos--;
    }
}
