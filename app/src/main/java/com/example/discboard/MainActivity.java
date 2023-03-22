package com.example.discboard;

import static com.example.discboard.DiscFinal.*;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.discboard.datatype.Dot;
import com.example.discboard.datatype.InterDot;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mActionBarDrawerToggle;
    NavigationView mNavigationView;
    NavHostFragment mNavHostFragment;
    FragmentManager mSupportFragmentManager;

    JsonDataHelper mJsonDataHelper;
    NavController mNavController;
    String TAG = "MainTest";

    Gson mGson;

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

        mGson = new Gson();

//        // deserialize testing
//        InterDot interDot = mGson.fromJson("{\"dot_type\":-1,\"seq_No\":2,\"x\":695.5,\"y\":628.5,\"touched\"=false}", InterDot.class);
//        if(interDot instanceof InterDot)
//            Log.d(TAG, "onCreate: " + interDot);

//        Dot dot = mGson.fromJson("{\"dot_type\":-1,\"seq_No\":2,\"x\":695.5,\"y\":628.5,\"touched\"=false}", Dot.class);
//        if(dot instanceof Dot)
//            Log.d(TAG, "onCreate: " + dot);

        mJsonDataHelper = new JsonDataHelper(this);
        // TODO:在所有内容制作完成后删除
        Toast.makeText(this, "本版本为预发布版\n主要功能已制作完成", Toast.LENGTH_SHORT).show();

        // 全屏沉浸模式
        setImmersedMode();
        // 保持屏幕常亮
        keepScreenAwake();

        setContentView(R.layout.activity_main);
        // data init on 1st run
        initDataOn1stRun();
        // init the side drawer menu
        initDrawerMenu();

        //-------------Navigation Init-------------
        initNavigationFragment();

        //-------------Navigation Drawer Click Handler-------------
        setDrawerLogic();

        // pos_y:Endzone暂时跳过
//        setContentView(R.layout.endzone);
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
        mNavigationView = findViewById(R.id.navi_drawer_menu);
        // set the default item highlighted
        mNavigationView.getMenu().getItem(0).setChecked(true);

        mNavigationView.setNavigationItemSelectedListener(menu_item -> {
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
        Gson gson = new Gson();

        String s = null;
        SharedPreferences shared = getSharedPreferences(USER_INIT_PREFERENCE, MODE_PRIVATE);


        if(mJsonDataHelper.getBooleanToUserPreferences(USER_DATA_FIRST_RUN_MARK, true))
        {
            SharedPreferences.Editor editor = shared.edit();
            s = mJsonDataHelper.loadJSONFromAsset("dots_3.json");
            editor.putString(USER_DATA_TEMP_3, s); // preload 3 players
            s = mJsonDataHelper.loadJSONFromAsset("dots_5.json");
            editor.putString(USER_DATA_TEMP_5, s); // preload 5 players
            s = mJsonDataHelper.loadJSONFromAsset("dots_7.json");
            editor.putString(USER_DATA_TEMP_VER, s); // preload 7 players / vertical stack
            s = mJsonDataHelper.loadJSONFromAsset("ho_stack.json");
            editor.putString(USER_DATA_TEMP_HO, s); // horizontal stack

            editor.apply();

            // save a copy to the "USER_SHARED_PREFERENCE"
            shared = getSharedPreferences(USER_DATA_PREF, MODE_PRIVATE);
            editor = shared.edit();

            editor.putString(USER_DATA_TEMP_MY, ""); // 预设3棋子
            s = mJsonDataHelper.loadJSONFromAsset("dots_3.json");
            editor.putString(USER_DATA_TEMP_3, s); // 预设3棋子
            s = mJsonDataHelper.loadJSONFromAsset("dots_5.json");
            editor.putString(USER_DATA_TEMP_5, s); // 预设5棋子
            s = mJsonDataHelper.loadJSONFromAsset("dots_7.json");
            editor.putString(USER_DATA_TEMP_VER, s); // 预设7棋子
            s = mJsonDataHelper.loadJSONFromAsset("ho_stack.json");
            editor.putString(USER_DATA_TEMP_HO, s); // horizontal stack

            editor.putBoolean(USER_DATA_FIRST_RUN_MARK, false);

            // init animation playing speed
            editor.putInt(USER_DATA_ANIM_SPEED, ANIM_SPEED_INIT);

            editor.apply();

            Toast.makeText(this, "战术模板初始化成功", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}