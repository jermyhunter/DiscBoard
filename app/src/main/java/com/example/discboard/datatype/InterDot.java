package com.example.discboard.datatype;

public class InterDot extends Dot{
    boolean touched;
    final static int DEF_INTER_TYPE = -2, OFF_INTER_TYPE = 2;// inter_dot.dot_type = dot.getDotType() * 2;

    public InterDot(float x, float y, int dot_type, int seq_No, boolean touched){
        super(x, y, dot_type, seq_No);
        this.touched = touched;
    }

    public InterDot(InterDot interDot) {
        super(interDot);
        this.touched = interDot.touched;
    }

    public InterDot(Dot dot1, Dot dot2) {
        this.x = (dot1.getX() + dot2.getX()) / 2;
        this.y = (dot1.getY() + dot2.getY()) / 2;
        this.seq_No = dot1.getSeqNo();
        this.dot_type = dot1.getDotType() * 2;
        this.touched = false;
    }
    @Override
    public String getDotID(){
        return "" + dot_type + seq_No;
    }

    public void touched() {
        this.touched = true;
    }

    public void resetTouched() {
        this.touched = false;
    }

    public boolean isTouched(){
        return touched;
    }

    @Override
    public String toString() {
        return "inter_dot: {dot_type=" + dot_type + ",seq_No=" + seq_No + ",x=" + x + ",y=" + y + ",touched=" + touched +"}";
    }

    public boolean isOffInter(){
        return getDotType() == OFF_INTER_TYPE;
    }
    public boolean isDefInter(){
        return getDotType() == DEF_INTER_TYPE;
    }

}

