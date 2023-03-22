package com.example.discboard.datatype;

// NOT USING
public class AnimTemp {
    String mTempName;
    String mAniDotsJsonS;
    AnimTemp(){
        mTempName = "";
        mAniDotsJsonS = "";
    }

    AnimTemp(String tempName, String aniDotsJsonS){
        mTempName = tempName;
        mAniDotsJsonS = aniDotsJsonS;
    }

    public void setTempName(String tempName) {
        mTempName = tempName;
    }

    public String getTempName() {
        return mTempName;
    }

    public void setAniDotsJsonS(String aniDotsJsonS) {
        mAniDotsJsonS = aniDotsJsonS;
    }

    public String getAniDotsJsonS() {
        return mAniDotsJsonS;
    }
}
