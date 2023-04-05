package com.example.discboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;

import com.example.discboard.datatype.Dot;
import com.example.discboard.datatype.InterDot;

import java.io.File;
import java.util.Hashtable;
import java.util.Objects;

/**
 * DiscFinal
 * frequently used data, all static
 * */
public class DiscFinal {
    public final static String USER_DATA_CANVAS_BG_TYPE = "canvas_bg_type";
    public static class LocaleType{
        public final static String CN = "zh";
        public final static String EN = "en";
    }
    public final static String LOCALE_CODE = "locale_code";

    // auto-save delay, determine how often the auto-save function would launch
    public final static int AUTO_SAVE_DELAY = 28 * 1000;
    public final static int CIRCLE_RADIUS = 35;
    public final static int INTER_DOT_RADIUS = 30;
    public final static int CIRCLET_RADIUS = 12;
    public final static int TOUCH_RADIUS = 52;
    public final static int TOUCH_OFFSET = 50;

    //  alpha value of off/def/disc circle pre-frame dot
    public final static int DOT_ALPHA = 120;
    //  alpha value of inter_dot circle pre-frame dot
    public final static int INTER_DOT_ALPHA = 170;

    public final static String CONTACT_INFO = "479395433";
    public final static String CONTACT_EMAIL = "jermyhunter123@gmail.com";
    public final static String CONTACT_URL = "https://space.bilibili.com/177731643";
    public final static float DELTA_E = 25f;
    public final static String USER_DATA_PREF = "user_data";
    public final static String USER_DATA_FIRST_RUN_MARK = "first_time";
    public final static String USER_DATA_RESET_MARK = "reset_mark";
    public final static String USER_DATA_TEMP_MY = "my_temp";
    public final static String USER_DATA_TEMP_3 = "3";
    public final static String USER_DATA_TEMP_5 = "5";
    public final static String USER_DATA_TEMP_VER = "ver_stack";
    public final static String USER_DATA_TEMP_HO = "ho_stack";
    public final static String USER_DATA_EXPORTED_FILE_PATH = "exported_file_path";
    public final static String USER_DATA_ANIM_TEMP_LIST = "anim_temp_list";
    public final static String USER_DATA_ANIM_SPEED = "anim_speed";
    public final static String USER_DATA_AUTO_SAVE_MARK = "auto_save";
    public final static String USER_INIT_PREF = "init_data";
    public final static String USER_DATA_BOARD_WIDTH = "width";
    public final static String USER_DATA_BOARD_HEIGHT = "height";
    public final static int ANIM_SPEED_INIT = 75;
    // ------------------ EX/IMPORTING ------------------

    final public static class ExportFileIndex{
        final public static int Head = 0;
        final public static int TempList = 1;
        final public static int AniDotsList = 2;
        final public static int BoardMeasure = 3;
    }

    public final static String IO_HEAD = "head";
    // add ver. number
    // IO_HEAD_VERSION_X_XX 对应版本号，可以据此编写向前的版本兼容，将导入的文件转化为兼容版
    public final static String IO_HEAD_VERSION_1_1 = "ver1.1";
    public final static String IO_HEAD_VERSION_1_2 = "ver1.2";
    // store the board measure, "WIDTH" "HEIGHT" in order
    public final static String IO_BOARD_MEASURE = "board_measure";
    public final static String IO_ANIM_TEMP_LIST = "anim_temp_list";
    public final static String IO_ANIM_DOTS_LIST = "anim_dots_list";
    public final static String EXPORT_FOLDER_SUB_PATH = "" + File.separator;
    public final static String EXPORT_FILE_SUFFIX = ".json";
    public final static String EXPORT_FILE_PREFIX = "disc_";
    public final static String TEMP_DUPLICATION_SUFFIX = "_1";

    public final static float NORMAL_ALPHA = 0.5f;
    public final static float PRESSED_ALPHA = 1.0f;

    public static float convertPixelsToDp(float px, Context context){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void moveDotInbounds(Canvas canvas, Dot dot_new){
//        Dot dot_new = new Dot(dot);
        float x = dot_new.getX();
        float y = dot_new.getY();
        if(x - (CIRCLE_RADIUS/2f + DELTA_E) < 0f){
            dot_new.setX(0f + CIRCLE_RADIUS/2f + DELTA_E);
        }
        if(x + (CIRCLE_RADIUS/2f + DELTA_E) > canvas.getWidth()){
            dot_new.setX(canvas.getWidth() - (CIRCLE_RADIUS/2f + DELTA_E));
        }

        if(y - (CIRCLE_RADIUS/2f + DELTA_E) < 0f) {
            dot_new.setY(0f + CIRCLE_RADIUS/2f + DELTA_E);
        }
        if(y + (CIRCLE_RADIUS/2f + DELTA_E) > canvas.getHeight()) {
            dot_new.setY(canvas.getHeight() - (CIRCLE_RADIUS/2f + DELTA_E));
        }
    }

    public static void moveDotInbounds(View canvas, Dot dot_new){
//        Dot dot_new = new Dot(dot);
        float x = dot_new.getX();
        float y = dot_new.getY();
        if(x - (CIRCLE_RADIUS/2f + DELTA_E) < 0f){
            dot_new.setX(0f + CIRCLE_RADIUS/2f + DELTA_E);
        }
        if(x + (CIRCLE_RADIUS/2f + DELTA_E) > canvas.getWidth()){
            dot_new.setX(canvas.getWidth() - (CIRCLE_RADIUS/2f + DELTA_E));
        }

        if(y - (CIRCLE_RADIUS/2f + DELTA_E) < 0f) {
            dot_new.setY(0f + CIRCLE_RADIUS/2f + DELTA_E);
        }
        if(y + (CIRCLE_RADIUS/2f + DELTA_E) > canvas.getHeight()) {
            dot_new.setY(canvas.getHeight() - (CIRCLE_RADIUS/2f + DELTA_E));
        }
    }

    public static void moveDotInbounds(Canvas canvas, InterDot inter_dot_new){
//        InterDot inter_dot_new = new InterDot(inter_dot);
        float x = inter_dot_new.getX();
        float y = inter_dot_new.getY();
        if(x - (CIRCLE_RADIUS/2f + DELTA_E) < 0f){
            inter_dot_new.setX(0f + CIRCLE_RADIUS/2f + DELTA_E);
        }
        if(x + (CIRCLE_RADIUS/2f + DELTA_E) > canvas.getWidth()){
            inter_dot_new.setX(canvas.getWidth() - (CIRCLE_RADIUS/2f + DELTA_E));
        }

        if(y - (CIRCLE_RADIUS/2f + DELTA_E) < 0f) {
            inter_dot_new.setY(0f + CIRCLE_RADIUS/2f + DELTA_E);
        }
        if(y + (CIRCLE_RADIUS/2f + DELTA_E) > canvas.getHeight()) {
            inter_dot_new.setY(canvas.getHeight() - (CIRCLE_RADIUS/2f + DELTA_E));
        }
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public enum PaintType{
        Red(0),
        Blue(1),
        Orange(2),
        White(3),
        Black(4);
        private final int value;
        PaintType(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
}
