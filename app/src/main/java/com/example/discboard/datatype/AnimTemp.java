package com.example.discboard.datatype;

import java.util.ArrayList;
import java.util.Hashtable;

// NOT USING
public class AnimTemp {
    ArrayList<Hashtable<String, Dot>> animDotsList;
    ArrayList<Hashtable<String, InterDot>> interDotsList;

    public AnimTemp(ArrayList<Hashtable<String, Dot>> anim_dots_list,
                    ArrayList<Hashtable<String, InterDot>> inter_dots_list){
        this.animDotsList = anim_dots_list;
        this.interDotsList = inter_dots_list;
    }

    public ArrayList<Hashtable<String, Dot>> getAnimDotsList() {
        return animDotsList;
    }

    public ArrayList<Hashtable<String, InterDot>> getInterDotsList() {
        return interDotsList;
    }
}
