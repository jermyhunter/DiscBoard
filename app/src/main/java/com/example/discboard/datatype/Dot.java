package com.example.discboard.datatype;

import static com.example.discboard.DiscFinal.*;

public class Dot{
    int dot_type;// -1 def, 1 off, 0 disc, ...
    float x, y;// the 2D coordinate of the dot
    // 方便获取索引
    int seq_No;

    final static int DEF_TYPE = -1, OFF_TYPE = 1, DISC_TYPE = 0;

    final static private String TAG = "Dot Class";
    public Dot(float x, float y, int dot_type, int seq_No) {
        this.x = x;
        this.y = y;
        this.dot_type = dot_type;
        this.seq_No = seq_No;
    }

    public Dot(Dot d) {
        this.x = d.x;
        this.y = d.y;
        this.dot_type = d.dot_type;
        this.seq_No = d.seq_No;
    }

    public Dot() {
    }

    public String getDotID(){
        return "" + dot_type + seq_No;
    }

    // for quick searching, eg. s.startsWith("-1") -> def
    public void setSeqNo(int seqNo){
        this.seq_No = seqNo;
    }
    public int getSeqNo(){
        return this.seq_No;
    }

    // for testing and json file storage
    @Override
    public String toString() {
        return "dot: {dot_type=" + dot_type + ",seq_No=" + seq_No + ",x=" + x + ",y=" + y + "}";
    }

    public void setX(float x) {
        this.x = x;
    }
    public float getX() {
        return x;
    }

    public void setY(float y) {
        this.y = y;
    }
    public float getY() {
        return y;
    }

    public void setType(int dot_type) {
        this.dot_type = dot_type;
    }
    public void setDefType() {
        this.dot_type = DEF_TYPE;
    }
    public void setOffType() {
        this.dot_type = OFF_TYPE;
    }
    public void setDiscType() {
        this.dot_type = DISC_TYPE;
    }
    public int getDotType() {
        return dot_type;
    }
    public String getRelativeInterDotID(){
        return String.valueOf(dot_type * 2) + getSeqNo();
    }

    public boolean isInside(float x, float y) {
        return (getX() - x) * (getX() - x) + (getY() - y) * (getY() - y) <= TOUCH_RADIUS * TOUCH_RADIUS;
    }

    public void scaleByRatio(float X_ratio, float Y_ratio){
        this.x *= X_ratio;
        this.y *= Y_ratio;
    }

    public boolean isOffense(){
        return getDotType() == OFF_TYPE;
    }
    public boolean isDefense(){
        return getDotType() == DEF_TYPE;
    }
    public boolean isDisc(){
        return getDotType() == DISC_TYPE;
    }

    public static boolean isOffense(String id){
        return id.startsWith(String.valueOf(OFF_TYPE));
    }
    public static boolean isDefense(String id){
        return id.startsWith(String.valueOf(DEF_TYPE));
    }
    public static boolean isDisc(String id){
        return id.startsWith(String.valueOf(DISC_TYPE));
    }
}