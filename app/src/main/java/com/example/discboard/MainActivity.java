package com.example.discboard;

import static com.example.discboard.DiscFinal.*;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.discboard.views.AnimatedDiscBoard;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.Hashtable;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    NavigationView mNavigationView;
    NavHostFragment mNavHostFragment;
    FragmentManager mSupportFragmentManager;

    JsonDataHelper mJsonDataHelper;
    NavController mNavController;

    String TAG = "MainActivity";

    UnSavedCheckDialogFragment mUnSavedCheckDialogFragment;
    InitSettingsDialogFragment mInitSettingsDialogFragment;
    MenuItem mSelectedMenuItem, mSelectedMenuItem1;
    int mMenuPos, mMenuPos1;
    int mNaviDestID, mNaviDestID1;

    Hashtable<Integer, Integer> mMenuItemID2Pos, mMenuItemID2FragmentID;
    static final class FragmentIndex{
        static int StaticBoardIndex = 0,
        AnimatedBoardIndex = 1,
        DelTemplatePadIndex = 2,
        SettingsIndex = 3,
        GuidingIndex = 4,
        FeedbackIndex = 5;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJsonDataHelper = new JsonDataHelper(this);
        // lang setting
        mJsonDataHelper.initAppLocale();

//        // deserialize testing
//        InterDot interDot = mGson.fromJson("{\"dot_type\":-1,\"seq_No\":2,\"x\":695.5,\"y\":628.5,\"touched\"=false}", InterDot.class);
//        if(interDot instanceof InterDot)
//            Log.d(TAG, "onCreate: " + interDot);

//        Dot dot = mGson.fromJson("{\"dot_type\":-1,\"seq_No\":2,\"x\":695.5,\"y\":628.5,\"touched\"=false}", Dot.class);
//        if(dot instanceof Dot)
//            Log.d(TAG, "onCreate: " + dot);

        // 全屏沉浸模式
        setImmersedMode();
        // 保持屏幕常亮
        keepScreenAwake();
        // requestFeature() must be called before adding contents
        setContentView(R.layout.activity_main);

        // init the side drawer menu
        initDrawerMenu();

        //-------------Navigation Init-------------
        initNavigationFragment();

        //------Navigation Drawer Click Handler-------
        setDrawerLogic();

        //----------data init on 1st run-----------
        initDataOn1stRun();

        // test user's default lang
//        Log.d(TAG, "onCreate: " + LocaleList.getDefault());
    }

    private void initExportFolder() {
        File file = mJsonDataHelper.getExportFolder();
//        Toast.makeText(this,"导出文件夹路径为：" + file, Toast.LENGTH_LONG).show();
    }

    private void keepScreenAwake() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initDrawerMenu() {
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        mDrawerLayout = findViewById(R.id.my_drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.nav_open, R.string.nav_close);

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setImmersedMode() {
        // 隐藏状态栏
        setFullScreen();
        // 隐藏标题栏
        hideActionBar();
        // 隐藏下方手势栏
        hideGestureBar();
    }

    private void hideGestureBar() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        // Add a listener to update the behavior of the toggle fullscreen button when
        // the system bars are hidden or revealed.
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            // You can hide the caption bar even when the other system bars are visible.
            // To account for this, explicitly check the visibility of navigationBars()
            // and statusBars() rather than checking the visibility of systemBars().
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
            }
            return view.onApplyWindowInsets(windowInsets);
        });
    }

    private void setDrawerLogic() {
        mUnSavedCheckDialogFragment = new UnSavedCheckDialogFragment();
        mUnSavedCheckDialogFragment.setSaveDialogListener(() -> {
            mNavigationView.getMenu().getItem(mMenuPos).setChecked(false);
            mNavigationView.getMenu().getItem(mMenuPos1).setChecked(true);
            mMenuPos = mMenuPos1;
            mNaviDestID = mNaviDestID1;
            mSelectedMenuItem = mSelectedMenuItem1;
            mNavController.popBackStack();
            mNavController.navigate(mNaviDestID);
        });

        mMenuItemID2Pos = new Hashtable<Integer, Integer>();
        mMenuItemID2Pos.put(R.id.navi_static_board, FragmentIndex.StaticBoardIndex);
        mMenuItemID2Pos.put(R.id.navi_animated_board, FragmentIndex.AnimatedBoardIndex);
        mMenuItemID2Pos.put(R.id.navi_del_template_pad, FragmentIndex.DelTemplatePadIndex);
        mMenuItemID2Pos.put(R.id.navi_settings, FragmentIndex.SettingsIndex);
        mMenuItemID2Pos.put(R.id.navi_guiding, FragmentIndex.GuidingIndex);
        mMenuItemID2Pos.put(R.id.navi_feedback, FragmentIndex.FeedbackIndex);

        mMenuItemID2FragmentID= new Hashtable<Integer, Integer>();
        mMenuItemID2FragmentID.put(R.id.navi_static_board, R.id.staticBoardFragment);
        mMenuItemID2FragmentID.put(R.id.navi_animated_board, R.id.animatedBoardFragment);
        mMenuItemID2FragmentID.put(R.id.navi_del_template_pad, R.id.delTemplatePadFragment);
        mMenuItemID2FragmentID.put(R.id.navi_settings, R.id.settingsFragment);
        mMenuItemID2FragmentID.put(R.id.navi_guiding, R.id.guidingFragment);
        mMenuItemID2FragmentID.put(R.id.navi_feedback, R.id.feedbackFragment);

        mNavigationView = findViewById(R.id.navi_drawer_menu);

        // set the default item highlighted
        mMenuPos = FragmentIndex.StaticBoardIndex;
        mSelectedMenuItem = mNavigationView.getMenu().getItem(mMenuPos);
        mNaviDestID = mSelectedMenuItem.getItemId();
        mSelectedMenuItem.setChecked(true);

        mNavigationView.setNavigationItemSelectedListener(menu_item -> {
            // 记录目的地，如果当前对象为 animBoard，那么 提示 doubleCheck
            // reset last menu_item state
            mSelectedMenuItem1 = menu_item;
            int menu_item_id = mSelectedMenuItem1.getItemId();
            mNaviDestID1 = mMenuItemID2FragmentID.get(menu_item_id);
            mMenuPos1 = mMenuItemID2Pos.get(menu_item_id);
            // if the before-switching pos is static or anim, then show the data check dialog
            if(mMenuPos1 != mMenuPos){// if the start and dest are not the same location
                if(mMenuPos == FragmentIndex.AnimatedBoardIndex && !AnimatedDiscBoard.isSaved()) {
                    mUnSavedCheckDialogFragment.show(mSupportFragmentManager, "数据丢弃确认");
                }
                else {
                    mNavigationView.getMenu().getItem(mMenuPos).setChecked(false);
                    mNavigationView.getMenu().getItem(mMenuPos1).setChecked(true);
                    mNaviDestID = mNaviDestID1;
                    mMenuPos = mMenuPos1;
                    mSelectedMenuItem = mSelectedMenuItem1;
                    mNavController.popBackStack();
                    mNavController.navigate(mNaviDestID);
                }
            }
            // if the start and dest are the same location, then do nothing

            /* abandoned menu switch plan
            int menu_item_id = menu_item.getItemId();
            int menu_size = mNavigationView.getMenu().size();
            switch (menu_item_id){
                case R.id.navi_static_board:
                    if(!mNavigationView.getMenu().getItem(FragmentIndex.StaticBoardIndex).isChecked()) {
                        setItemIChecked(mNavigationView, FragmentIndex.StaticBoardIndex, menu_size);
                        mNavController.popBackStack();
                        mNavController.navigate(R.id.staticBoardFragment);
                    }
                    break;
                case R.id.navi_animated_board:
                    if(!mNavigationView.getMenu().getItem(FragmentIndex.AnimatedBoardIndex).isChecked()) {
                        setItemIChecked(mNavigationView, FragmentIndex.AnimatedBoardIndex, menu_size);
                        mNavController.popBackStack();
                        mNavController.navigate(R.id.animatedBoardFragment);
                    }
                    break;
                case R.id.navi_del_template_pad:
                    if(!mNavigationView.getMenu().getItem(FragmentIndex.DelTemplatePadIndex).isChecked()) {
                        setItemIChecked(mNavigationView, FragmentIndex.DelTemplatePadIndex, menu_size);
                        mNavController.popBackStack();
                        mNavController.navigate(R.id.delTemplatePadFragment);
                    }
                    break;
                case R.id.navi_settings:
                    if(!mNavigationView.getMenu().getItem(FragmentIndex.SettingsIndex).isChecked()) {
                        setItemIChecked(mNavigationView, FragmentIndex.SettingsIndex, menu_size);
                        mNavController.popBackStack();
                        mNavController.navigate(R.id.settingsFragment);
                    }
                    break;

                case R.id.navi_guiding:
                    if(!mNavigationView.getMenu().getItem(FragmentIndex.GuidingIndex).isChecked()) {
                        setItemIChecked(mNavigationView, FragmentIndex.GuidingIndex, menu_size);
                        mNavController.popBackStack();
                        mNavController.navigate(R.id.guidingFragment);
                    }
                    break;

                case R.id.navi_feedback:
                    if(!mNavigationView.getMenu().getItem(FragmentIndex.FeedbackIndex).isChecked()) {
                        setItemIChecked(mNavigationView, FragmentIndex.FeedbackIndex, menu_size);
                        mNavController.popBackStack();
                        mNavController.navigate(R.id.feedbackFragment);
                    }
                    break;
            }
            */
            return true;
        });
    }

    private void initNavigationFragment() {
        mSupportFragmentManager = getSupportFragmentManager();
        mNavHostFragment = (NavHostFragment) mSupportFragmentManager.findFragmentById(R.id.nav_host_fragment);
        mNavController = mNavHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, mNavController);
    }

    private void setItemIChecked(NavigationView navi_v, int checked_index, int menu_size) {
        for (int i = 0; i < menu_size; i++){
            navi_v.getMenu().getItem(i).setChecked(i == checked_index);
        }
    }

    private void setItemIChecked(NavigationView navi_v, int checked_index) {
        int menu_size = navi_v.getMenu().size();
        for (int i = 0; i < menu_size; i++){
            navi_v.getMenu().getItem(i).setChecked(i == checked_index);
        }
    }

    private void hideActionBar() {
        getSupportActionBar().hide();
    }

    private void initDataOn1stRun(){
        String s;

        // 初次使用
        if(mJsonDataHelper.getBooleanFromInitPreferences(USER_DATA_FIRST_RUN_MARK, true)){
            // TODO:在所有内容制作完成后删除
            Toast.makeText(this, R.string.first_launch_hint, Toast.LENGTH_SHORT).show();

            // ------------------ save temps to INIT pref -----------------------
            SharedPreferences shared = getSharedPreferences(USER_INIT_PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();
            s = mJsonDataHelper.loadJSONFromAsset("dots_3.json");
            editor.putString(USER_DATA_TEMP_3, s); // preload 3 players
            s = mJsonDataHelper.loadJSONFromAsset("dots_5.json");
            editor.putString(USER_DATA_TEMP_5, s); // preload 5 players
            s = mJsonDataHelper.loadJSONFromAsset("dots_7.json");
            editor.putString(USER_DATA_TEMP_VER, s); // preload 7 players / vertical stack
            s = mJsonDataHelper.loadJSONFromAsset("ho_stack.json");
            editor.putString(USER_DATA_TEMP_HO, s); // horizontal stack

            editor.putBoolean(USER_DATA_FIRST_RUN_MARK, false);
            editor.apply();

            // ------------------ initial settings -----------------------
            // save a copy to the "USER_SHARED_PREFERENCE"
            shared = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
            editor = shared.edit();

            // init animation playing speed
            editor.putInt(USER_DATA_ANIM_SPEED, ANIM_SPEED_INIT);
            // canvas_bg, default-> full_ground
            editor.putString(USER_DATA_CANVAS_BG_TYPE, CanvasBGType.FULL_GROUND);
            editor.putBoolean(USER_DATA_RESET_MARK, true);

            editor.apply();

            // initiate auto-save setting
            mInitSettingsDialogFragment = new InitSettingsDialogFragment();
            mInitSettingsDialogFragment.setJsonDataHelper(mJsonDataHelper);
            mInitSettingsDialogFragment.setCancelable(false);
            mInitSettingsDialogFragment.setAutoSaveCheckDialogListener(new InitSettingsDialogFragment.InitSettingsDialogListener() {
                @Override
                public void onCancelListener() {
//                    SharedPreferences shared1 = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
//                    SharedPreferences.Editor editor1 = shared1.edit();
//                    editor1.putBoolean(USER_DATA_AUTO_SAVE_MARK, false);
//                    editor1.apply();
                }

                @Override
                public void onConfirmListener() {
//                    SharedPreferences shared1 = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
//                    SharedPreferences.Editor editor1 = shared1.edit();
//                    editor1.putBoolean(USER_DATA_AUTO_SAVE_MARK, true);
//                    editor1.apply();
                }
            });

            mInitSettingsDialogFragment.show(mSupportFragmentManager, "自动保存初始化");

            // 初次使用，创建导出文件夹
            initExportFolder();
        }

        // init/reset temps
        if(mJsonDataHelper.getBooleanFromUserPreferences(USER_DATA_RESET_MARK, true))
        {
            // save a copy to the "USER_SHARED_PREFERENCE"
            SharedPreferences shared = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
            SharedPreferences.Editor editor = shared.edit();

            editor.putString(USER_DATA_TEMP_MY, ""); // 预设3棋子
            s = mJsonDataHelper.loadJSONFromAsset("dots_3.json");
            editor.putString(USER_DATA_TEMP_3, s); // 预设3棋子
            s = mJsonDataHelper.loadJSONFromAsset("dots_5.json");
            editor.putString(USER_DATA_TEMP_5, s); // 预设5棋子
            s = mJsonDataHelper.loadJSONFromAsset("dots_7.json");
            editor.putString(USER_DATA_TEMP_VER, s); // 预设7棋子
            s = mJsonDataHelper.loadJSONFromAsset("ho_stack.json");
            editor.putString(USER_DATA_TEMP_HO, s); // horizontal stack

            editor.putBoolean(USER_DATA_RESET_MARK, false);

            editor.apply();

            Toast.makeText(this, R.string.temps_init_success_hint, Toast.LENGTH_SHORT).show();
        }

    }

    /*
    * params:
    *   enable
    *     true-> 隐藏
    * */
    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void setFullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
        } else {
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);
        }
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            // 两次退出命令的间隔超过2s，则退出程序
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), R.string.exit_check_hint, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static class UnSavedCheckDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();// * 从 requireActivity() 改为 getActivity()

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View dialogView = inflater.inflate(R.layout.dialog_save_check, null);

            TextView txt = dialogView.findViewById(R.id.unsaved_hint_txt);
            txt.setText(R.string.unsaved_hint_10);

            builder.setView(dialogView)
                    .setPositiveButton(R.string.confirm_string, (dialogInterface, i) -> mDoubleCheckDialogListener.onCheckListener())
                    .setNegativeButton(R.string.cancel_string, (dialogInterface, i) -> {
                    });

            return builder.create();
        }

        DoubleCheckDialogListener mDoubleCheckDialogListener;

        public void setSaveDialogListener(DoubleCheckDialogListener DoubleCheckDialogListener) {
            mDoubleCheckDialogListener = DoubleCheckDialogListener;
        }

        public interface DoubleCheckDialogListener{
            void onCheckListener();
        }
    }

    public static class InitSettingsDialogFragment extends DialogFragment{
        JsonDataHelper mJsonDataHelper;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();// * 从 requireActivity() 改为 getActivity()

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View dialogView = inflater.inflate(R.layout.dialog_init_settings, null);

            builder.setView(dialogView)
                    .setPositiveButton(R.string.activate_string, (dialogInterface, i) -> {
//                        mInitSettingsDialogListener.onConfirmListener();
                        mJsonDataHelper.setBooleanToUserPreferences(USER_DATA_AUTO_SAVE_MARK, true);
                    })
                    .setNegativeButton(R.string.cancel_string, (dialogInterface, i) -> {
//                        mInitSettingsDialogListener.onCancelListener();
                        mJsonDataHelper.setBooleanToUserPreferences(USER_DATA_AUTO_SAVE_MARK, false);
                    });

            return builder.create();
        }

        public void setJsonDataHelper(JsonDataHelper jsonDataHelper){
            mJsonDataHelper = jsonDataHelper;
        }

        InitSettingsDialogListener mInitSettingsDialogListener;

        public void setAutoSaveCheckDialogListener(InitSettingsDialogListener listener) {
            mInitSettingsDialogListener = listener;
        }


        public interface InitSettingsDialogListener{
            void onCancelListener();
            void onConfirmListener();
        }
    }
}