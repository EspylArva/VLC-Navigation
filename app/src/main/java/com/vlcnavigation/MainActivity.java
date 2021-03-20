package com.vlcnavigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nambimobile.widgets.efab.ExpandableFabLayout;
import com.nambimobile.widgets.efab.FabOption;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private ExpandableFabLayout container_fabs;
//    private ExpandableFab fab_permissions;
    private FabOption opt_microphone, opt_readFiles;
    private final int PERMISSION_REQUEST_MICROPHONE = 301;
    private final int PERMISSION_REQUEST_READ_FILES = 302;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new CustomDebugTree());
//        Sharp.setLogLevel(Sharp.LOG_LEVEL_INFO);

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

        initViews();
        if(checkPermissions()){ initListeners(); }
    }

    private void initViews()
    {
        container_fabs = findViewById(R.id.fab_permissions);
        opt_microphone = findViewById(R.id.fabOpt_listen_microphone);
        opt_readFiles = findViewById(R.id.fabOpt_read_files);
    }

    private void initListeners()
    {
        opt_microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_MICROPHONE); }
                else { Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_already_granted), Toast.LENGTH_SHORT).show(); }
            }
        });
        opt_readFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_FILES); }
                else { Toast.makeText(getApplicationContext(), getResources().getString(R.string.permission_already_granted), Toast.LENGTH_SHORT).show(); }
            }
        });
    }

    private boolean checkPermissions()
    {
        boolean permissionNeeded = false;
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            opt_readFiles.setFabOptionColor(getColor(R.color.green));
        } else { permissionNeeded = true; }
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
        {
            opt_microphone.setFabOptionColor(getColor(R.color.green));
        } else { permissionNeeded = true; }

        if(!permissionNeeded) { container_fabs.removeAllViews(); }
        return permissionNeeded;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Timber.d("%s - %s - %s", requestCode, Arrays.toString(permissions), Arrays.toString(grantResults));
//        if(grantResults[0] == RESULT_OK)
//        {
//            switch (requestCode)
//            {
//                case PERMISSION_REQUEST_MICROPHONE:
//                    break;
//                case PERMISSION_REQUEST_READ_FILES:
//                    break;
//            }
//        }
        checkPermissions();
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
}