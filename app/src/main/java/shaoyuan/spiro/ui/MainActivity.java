package shaoyuan.spiro.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;



import shaoyuan.spiro.R;
//import shaoyuan.spiro.feature.MusicIntentReceiver;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences preferences;
    private BottomNavigationView navigation;
    private static final String TAG = "SYTAG";
//    private MusicIntentReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loading the default fragment
        loadFragment(new HomeFragment());


        //getting bottom navigation view and attaching the listener
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        //preferences = this.getSharedPreferences("spiroAppPrefs", Context.MODE_PRIVATE);
//        preferences = PreferenceManager.getDefaultSharedPreferences(this);
//        preferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
//        navigation.setVisibility(preferences.getBoolean("isMeasuring", true) ? View.INVISIBLE : View.VISIBLE);
//
//        myReceiver = new MusicIntentReceiver();
//
    }
//
//    @Override public void onResume() {
//        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
//        registerReceiver(myReceiver, filter);
//        super.onResume();
//    }
//
//    @Override public void onPause() {
//        unregisterReceiver(myReceiver);
//        super.onPause();
//    }
//    private class MusicIntentReceiver extends BroadcastReceiver {
//        @Override public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
//                int state = intent.getIntExtra("state", -1);
//                switch (state) {
//                    case 0:
//                        Log.d(TAG, "Headset is unplugged");
//                        break;
//                    case 1:
//                        Log.d(TAG, "Headset is plugged");
//                        break;
//                    default:
//                        Log.d(TAG, "I have no idea what the headset state is");
//                }
//            }
//        }
//    }

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("isMeasuring")){
                navigation.setVisibility(preferences.getBoolean("isMeasuring", true) ? View.INVISIBLE : View.VISIBLE);
            }
        }
    };


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                break;

            case R.id.navigation_dashboard:
                fragment = new SettingsFragment();
                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}