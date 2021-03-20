package com.vlcnavigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new CustomDebugTree());
//        Sharp.setLogLevel(Sharp.LOG_LEVEL_INFO);

        askPermissions();

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_live_map, R.id.navigation_fft, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void askPermissions() {

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
//            if (shouldShowRequestPermissionRationale(
//                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                // Explain to the user why we need to read the contacts
//
//            }

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 500);

            // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an app-defined int constant that should be quite unique

            return;
        }
    }

    public class CustomDebugTree extends Timber.DebugTree {
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return String.format("[%s:%s | %s]",
                    super.createStackElementTag(element),
                    element.getLineNumber(),
                    element.getMethodName()
            );
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (data != null) {
//            Timber.d("Activity Result caught. Request code: %s. Result code: %s", requestCode, resultCode);
//            switch (requestCode) {
//                case SvgFetcher.READ_SVG_REQUEST_CODE:
//                    if (resultCode == RESULT_OK) {
////                        getSupportFragmentManager().findFragmentById(R.id.navigation_settings).onActivityResult(requestCode, resultCode, data);
//                        String FilePath = data.getData().getPath();
//                        Timber.d(FilePath);
//
//                    } else {
//                        Timber.e("Could not find file");
//                    }
//                    break;
//            }
//        }
//    }
}