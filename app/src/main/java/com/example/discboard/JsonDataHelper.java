package com.example.discboard;

import static android.content.Context.MODE_PRIVATE;

import static com.example.discboard.DiscFinal.EXPORT_FILE_PREFIX;
import static com.example.discboard.DiscFinal.EXPORT_FILE_SUFFIX;
import static com.example.discboard.DiscFinal.IO_HEAD;
import static com.example.discboard.DiscFinal.IO_HEAD_TYPE;
import static com.example.discboard.DiscFinal.USER_DATA_EXPORTED_FILE_PATH;
import static com.example.discboard.DiscFinal.USER_DATA_PREF;
import static com.example.discboard.DiscFinal.USER_DATA_ANIM_TEMP_LIST;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.discboard.datatype.AnimTemp;
import com.example.discboard.datatype.Dot;
import com.example.discboard.datatype.InterDot;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

// not finished, and not being used
public class JsonDataHelper {
    static String TAG = "JsonDataHelper";
    Gson mGson;
    Context mContext;

    public JsonDataHelper(Context context){
        mGson = new Gson();
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * for exporting use
     * loading all temp names from USER_DATA_ANIM_TEMP_LIST of USER_DATA_PREF in shared preferences
     * */
    public ArrayList<String> loadTempNamesFromPref(){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        Set<String> hashset = shared.getStringSet(USER_DATA_ANIM_TEMP_LIST, new HashSet<>());
//        Log.d(TAG, "loadTempNamesFromPref: " + hashset);
//        Log.d(TAG, "loadTempNamesFromPref: " + hashset.size());
        return new ArrayList<>(hashset);
    }

    /**
     * for exporting use
     * loading all temp names from USER_DATA_ANIM_TEMP_LIST of USER_DATA_PREF in shared preferences
     * */
    public Set<String> loadTempNamesSetFromPref(){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
//        Log.d(TAG, "loadTempNamesFromPref: " + hashset);
        return shared.getStringSet(USER_DATA_ANIM_TEMP_LIST, new HashSet<>());
    }

    public Boolean checkNameDuplication(String temp_name){
        Set<String> hashset = loadTempNamesSetFromPref();
        return hashset.contains(temp_name);
    }

    /**
     * delete all ani temps from the app
     */
    public void delAllTemps(){
        ArrayList<String> temp_names = loadTempNamesFromPref();
        temp_names.forEach(tame_name -> {
            delAniNameFromPref(tame_name);
            delAniDotsFromPref(tame_name);
        });
    }


//    /**
//     * loading templates from shared preferences
//     * */
//    public ArrayList<Hashtable<String, Dot>> loadAnimDotsFromPref(String name) {
//        ArrayList<Hashtable<String, Dot>> anim_dots_list = new ArrayList<>();
//        SharedPreferences shared = getContext().getSharedPreferences(name, MODE_PRIVATE);
//        int currentFrameNo = 0;
//        String json_s = shared.getString(String.valueOf(currentFrameNo), "");
//        if(!Objects.equals(json_s, "")){
//            while(!Objects.equals(json_s, "")) {
//                ArrayList<Dot> arr = new ArrayList<>();
//                Dot[] dots = mGson.fromJson(json_s, Dot[].class);
//                Collections.addAll(arr, dots);
//
//                Hashtable<String, Dot> hashtable = Array2Hashtable(arr);
//                anim_dots_list.add(hashtable);
//                currentFrameNo += 1;
//                json_s = shared.getString(String.valueOf(currentFrameNo), "");
//            }
//        }
//        return  anim_dots_list;
//    }

    /**
     * loading templates from shared preferences
     *      including inter_dots
     * */
    public AnimTemp loadAnimDotsFromPrefNew(String name) {
        ArrayList<Hashtable<String, Dot>> anim_dots_list = new ArrayList<>();
        ArrayList<Hashtable<String, InterDot>> inter_dots_list = new ArrayList<>();
        SharedPreferences shared = getContext().getSharedPreferences(name, MODE_PRIVATE);
        int currentFrameNo = 0;
        String json_s = shared.getString(String.valueOf(currentFrameNo), "");
        if(!Objects.equals(json_s, "")){
            while(!Objects.equals(json_s, "")) {
                if(currentFrameNo % 2 == 0) {// 偶数为帧，奇数为中间帧
                    ArrayList<Dot> arr = new ArrayList<>();
                    Dot[] dots = mGson.fromJson(json_s, Dot[].class);
                    Collections.addAll(arr, dots);

                    Hashtable<String, Dot> hashtable = Array2Hashtable(arr);
                    anim_dots_list.add(hashtable);
                }
                else {
                    ArrayList<InterDot> inter_arr = new ArrayList<>();
                    InterDot[] inter_dots = mGson.fromJson(json_s, InterDot[].class);
                    Collections.addAll(inter_arr, inter_dots);

                    Hashtable<String, InterDot> inter_hashtable = InterArray2Hashtable(inter_arr);
                    inter_dots_list.add(inter_hashtable);
                }
                currentFrameNo += 1;
                json_s = shared.getString(String.valueOf(currentFrameNo), "");
            }
        }
        return  new AnimTemp(anim_dots_list, inter_dots_list);
    }

    /**
     * save arraylist type data to shared preferences
     * usually called with addAniTempToPref()
     */
    public void saveAniDotsToPrefNew(String tempName, ArrayList<Hashtable<String, Dot>> animDotsList, ArrayList<Hashtable<String, InterDot>> interDotsList){
        SharedPreferences shared = getContext().getSharedPreferences(tempName, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();

        int currentFrameNo = 0;
        int length = interDotsList.size();
        ArrayList<Dot> arr;
        while(currentFrameNo < length){
            arr = new ArrayList<>(animDotsList.get(currentFrameNo).values());
            String json_s = transformData2Json(arr);
//            Log.d(TAG, "save:" + currentFrameNo + ": " + json_s);
            editor.putString(String.valueOf(currentFrameNo * 2), json_s);

            arr = new ArrayList<>(interDotsList.get(currentFrameNo).values());
            json_s = transformData2Json(arr);
//            Log.d(TAG, "save:" + currentFrameNo + ": " + json_s);
            editor.putString(String.valueOf(currentFrameNo * 2 + 1), json_s);

            currentFrameNo += 1;
        }

        arr = new ArrayList<>(animDotsList.get(currentFrameNo).values());
        String json_s = transformData2Json(arr);
        editor.putString(String.valueOf(currentFrameNo * 2), json_s);

        editor.apply();
    }

//    public void saveAniDotsToPref(String name, ArrayList<Hashtable<String, Dot>> anim_dots_list){
//        SharedPreferences shared = getContext().getSharedPreferences(name, getContext().MODE_PRIVATE);
//        SharedPreferences.Editor editor = shared.edit();
//
//        int currentFrameNo = 0;
//        for (Hashtable<String, Dot> hashtable: anim_dots_list) {
//            ArrayList<Dot> arr = new ArrayList<>(anim_dots_list.get(currentFrameNo).values());
//            String json_s = transformData2Json(arr);
////            Log.d(TAG, "save:" + currentFrameNo + ": " + json_s);
//            editor.putString(String.valueOf(currentFrameNo), json_s);
//            currentFrameNo += 1;
//        }
//        editor.apply();
//    }

    /**
     * add a new anim_temp_name to the shared preferences
     * usually called with saveAniDotsToPref()
     * */
    public void addAniTempToPref(String name){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        // update template name list
        HashSet<String> temp_names = new HashSet<>(shared.getStringSet(USER_DATA_ANIM_TEMP_LIST, new HashSet<>()));
        temp_names.add(name);
//        Log.d(TAG, "saveAniDotsToPref: " + temp_names);
        editor.putStringSet(USER_DATA_ANIM_TEMP_LIST, temp_names);
        editor.apply();
    }

    /**
     * delete the animation template name from the user_data file of preferences
     * */
    public void delAniNameFromPref(String name){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        // update template name list
        HashSet<String> temp_names = new HashSet<>(shared.getStringSet(USER_DATA_ANIM_TEMP_LIST, new HashSet<>()));
        temp_names.remove(name);
        editor.putStringSet(USER_DATA_ANIM_TEMP_LIST, temp_names);
        editor.apply();
    }

    /**
     * delete one xml file of dots list data from the preferences
     * */
    public void delAniDotsFromPref(String name){
        SharedPreferences shared = getContext().getSharedPreferences(name, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.clear();
        editor.apply();
    }


    /**
     * change data from arraylist to hashtable
     * */
    public static Hashtable<String, Dot> Array2Hashtable(ArrayList<Dot> arr){
        Hashtable<String, Dot> hashtable = new Hashtable<>();
        arr.forEach(dot -> hashtable.put(dot.getDotID(), dot));
        return hashtable;
    }

    public static Hashtable<String, InterDot> InterArray2Hashtable(ArrayList<InterDot> inter_arr){
        Hashtable<String, InterDot> hashtable = new Hashtable<>();
        inter_arr.forEach(inter_dot -> hashtable.put(inter_dot.getDotID(), inter_dot));
        return hashtable;
    }

    /**
     * transform object type data to json string
     *
     * @param object
     */
    public String transformData2Json(Object object){
        return mGson.toJson(object);
    }

    /**
     * check if the app only has the right to read external files
     * */
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }

    /**
     * check if the app has the right to write external files
     * */
    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    /**
     * loadJSONFromAsset
     * 从 assets 文件夹中读取 Json 文件
     * @param file_name
     * @return json string
     */
    public String loadJSONFromAsset(String file_name) {
        String json;
        try {
            // 以字节流形式读入json文件字符串
            InputStream is = getContext().getAssets().open(file_name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     * for exporting use
     * write string to external file in form of UTF-8
     * 导出的文件前缀为 disc_, 后缀为 .json
     * */
    public void writeToExternalFile(String s, String file_name){

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), EXPORT_FILE_PREFIX + file_name + EXPORT_FILE_SUFFIX);
//        Log.d(TAG, "writeFile: " +file);
//        Log.d(TAG, "writeFile: " +file.getAbsolutePath());

        try {
            // Very simple code to copy a picture from the application's
            // resource into the external file.  Note that this code does
            // no error checking, and assumes the picture is small (does not
            // try to copy it in chunks).  Note that if external storage is
            // not currently mounted this will silently fail.
            InputStream is = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            OutputStream os = Files.newOutputStream(file.toPath());
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 设置导出文件路径，用于分享用
        setStringToUserPreferences(USER_DATA_EXPORTED_FILE_PATH, file.getAbsolutePath());
    }

    /**
     * for importing use
     * reading JSON file as UTF-8 stream
     *
     * */
    public String readFromExternalFile(Uri uri) {
        String json_s;
        try {
            // 以字节流形式读入json文件字符串
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json_s = new String(buffer, StandardCharsets.UTF_8);
//            Log.d(TAG, "readExternalFile: " + json_s);
        } catch (IOException ex) {
            ex.printStackTrace();
//            Log.d(TAG, "readExternalFile: " + "读取失败");
            return null;
        }
        return json_s;
    }

    /**
     * check if the head of the JSONArray is IO_HEAD_TYPE
     * return true if it is; return false if not
     * */
    public static Boolean checkFileType(JSONObject jo){
        try {
            return jo.get(IO_HEAD).equals(IO_HEAD_TYPE);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

//    /**
//     * for importing use
//     * for the use of unwrapping JSONArray type data
//     * */
//    public ArrayList<Hashtable<String, Dot>> JSONArray2ArrayListHashtable(JSONArray ja) throws JSONException {
//        ArrayList<Hashtable<String, Dot>> anim_dots_list = new ArrayList<>();
//
//        int current_frame_No = 0;
//        int frame_sum = ja.length();
//        while(current_frame_No < frame_sum){
//            Hashtable<String, Dot> frame_dots = new Hashtable<>();
//            JSONObject jo_dots_1F = (JSONObject) ja.get(current_frame_No);
//            Iterator<String> id_keys = jo_dots_1F.keys();
////            Log.d(TAG, "JSONArray2ArrayListHashtable: " + jo_dots_1F);
//            while(id_keys.hasNext()){
//                String id = id_keys.next();
//                //sample -> {"dot_type":1,"seq_No":1,"x":501.5,"y":414}
//                Dot dot = mGson.fromJson(String.valueOf(jo_dots_1F.get(id)), Dot.class);
////                Log.d(TAG, "JSONArray2ArrayListHashtable: " + jo_dots_1F.get(id));
//                frame_dots.put(id, dot);
//            }
//            anim_dots_list.add(frame_dots);
//            current_frame_No += 1;
//        }
//        return  anim_dots_list;
//    }

    /**
     * for importing use
     * for the use of unwrapping JSONArray type data
     * */
    public AnimTemp JSONArray2ArrayListHashtableNew(JSONArray ja) throws JSONException {
        ArrayList<Hashtable<String, Dot>> anim_dots_list = new ArrayList<>();
        ArrayList<Hashtable<String, InterDot>> inter_dots_list = new ArrayList<>();

        int current_frame_No = 0;
        int frame_sum = ja.length();// 中间帧 * 2 + 1（或 普通帧 * 2 - 1）
        JSONObject jo_dots_1F;
        while(current_frame_No < frame_sum){
            if(current_frame_No % 2 == 0){
                Hashtable<String, Dot> frame_dots = new Hashtable<>();
                jo_dots_1F = (JSONObject) ja.get(current_frame_No);
                Iterator<String> id_keys = jo_dots_1F.keys();
//            Log.d(TAG, "JSONArray2ArrayListHashtable: " + jo_dots_1F);
                while (id_keys.hasNext()) {
                    String id = id_keys.next();
                    //sample -> {"dot_type":1,"seq_No":1,"x":501.5,"y":414}
                    Dot dot = mGson.fromJson(String.valueOf(jo_dots_1F.get(id)), Dot.class);
//                Log.d(TAG, "JSONArray2ArrayListHashtable: " + jo_dots_1F.get(id));
                    frame_dots.put(id, dot);
                }
                anim_dots_list.add(frame_dots);
            }
            else {
                Hashtable<String, InterDot> frame_inter_dots = new Hashtable<>();
                jo_dots_1F = (JSONObject) ja.get(current_frame_No);
                Iterator<String> id_keys = jo_dots_1F.keys();
//            Log.d(TAG, "JSONArray2ArrayListHashtable: " + jo_dots_1F);
                while (id_keys.hasNext()) {
                    String id = id_keys.next();
                    //sample -> {"dot_type":1,"seq_No":1,"x":501.5,"y":414,"touched":false}
                    InterDot inter_dot = mGson.fromJson(String.valueOf(jo_dots_1F.get(id)), InterDot.class);
//                Log.d(TAG, "JSONArray2ArrayListHashtable: " + jo_dots_1F.get(id));
                    frame_inter_dots.put(id, inter_dot);
                }
                inter_dots_list.add(frame_inter_dots);
            }
            current_frame_No += 1;
        }
        return new AnimTemp(anim_dots_list, inter_dots_list);
    }

    /**
     * fetching and getting data from USER_DATA in shared prefences
     * */
    public String getStringFromUserPreferences(String key){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        return shared.getString(key, "");
    }

    public void setStringToUserPreferences(String key, String s){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putString(key, s);
        editor.apply();
    }

    public void setBooleanToUserPreferences(String key, Boolean b){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putBoolean(key, b);
        editor.apply();
    }

    public Boolean getBooleanToUserPreferences(String key, Boolean b){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        return shared.getBoolean(key, b);
    }

    /**
     * return -1 if no data
     * */
    public int getIntegerFromUserPreferences(String key){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        return shared.getInt(key, -1);
    }

    public void setIntegerToUserPreferences(String key, int i){
        SharedPreferences shared = getContext().getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        editor.putInt(key, i);
        editor.apply();
    }

    /**
     * check exported file path stored in preferences
     *
     * only used in SettingsFragment
     * if exists, then enable the share btn
     * */
    public Boolean sharedFileExists(){
        String exported_file_path = getStringFromUserPreferences(USER_DATA_EXPORTED_FILE_PATH);
//        Uri exported_file_uri = Uri.parse(exported_file_path);
        File exported_file = new File(exported_file_path);
        return exported_file.exists();
    }

    public static JSONArray mergeAnimInterJsonArray(JSONArray jaAnim, JSONArray jaInter) {
        JSONArray outArray = new JSONArray();
        int length = jaInter.length();
        int i = 0;
        while(i < length){
            outArray.put(jaAnim.optJSONObject(i));
            outArray.put(jaInter.optJSONObject(i));
            i++;
        }
        outArray.put(jaAnim.optJSONObject(i));

        return outArray;
    }
}
