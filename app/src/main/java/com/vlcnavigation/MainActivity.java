package com.vlcnavigation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nambimobile.widgets.efab.ExpandableFabLayout;
import com.nambimobile.widgets.efab.FabOption;
import com.vlcnavigation.module.audiorecord.AudioRecorder;
import com.vlcnavigation.module.utils.Util;
import com.vlcnavigation.ui.fft.FFTComputing;
import com.vlcnavigation.ui.fft.FFTFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Arrays;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton fab_record;
    private ExpandableFabLayout container_fabs;
    private FabOption opt_microphone, opt_readFiles, opt_all;
    private final int PERMISSION_REQUEST_MICROPHONE = 301;
    private final int PERMISSION_REQUEST_READ_FILES = 302;
    private final int PERMISSION_REQUEST_ALL = 399;

    private AudioRecorder audioRecorder;


    private MutableLiveData<Boolean> record;
    public static Boolean fftBoolCompute;

    public static FFTComputing fftComputing;
    public static short[] BUFFER;
    public static int BUFFER_READ_RESULT;

    //threads
    private static final int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
    protected Handler updateUIHandler = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.plant(new CustomDebugTree());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setTitle(R.string.app_name);

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
        initListeners();


    }

    private void initViews()
    {
        fab_record = findViewById(R.id.fab_record_mic);
        container_fabs = findViewById(R.id.fab_permissions);
        opt_microphone = findViewById(R.id.fabOpt_listen_microphone);
        opt_readFiles = findViewById(R.id.fabOpt_read_files);
        opt_all = findViewById(R.id.fabOpt_allow_all);

        record = new MutableLiveData<Boolean>();
        record.setValue(false);


        fftBoolCompute = false;



        if(!checkPermissions())
        {
            container_fabs.setVisibility(View.GONE);
        }

    }

    private void initListeners()
    {
        fab_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                record.setValue(!record.getValue());

                if(record.getValue())
                {
                    fab_record.setBackgroundTintList(ColorStateList.valueOf(Util.getAttrColor(v.getContext(), R.attr.colorPrimary)));
                    audioRecorder = new AudioRecorder(record, findViewById(R.id.signalview));
                    audioRecorder.start();

                    fftBoolCompute = true;






                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            fftComputing = new FFTComputing(fftBoolCompute);
                            fftComputing.start();
                            //init the handler to record audio
                            createUpdateUiHandler();
                        }
                    }, 500);



                }
                else
                {
                    fab_record.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red)));
                    audioRecorder.interrupt();

                    fftBoolCompute = false;
                    fftComputing.isRecording = false;
                    fftComputing.interrupt();
                }

            }
        });

        opt_microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_MICROPHONE); }
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
        opt_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_ALL); 
//                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_FILES); }
            }
        });
    }

    private boolean checkPermissions()
    {
        boolean permissionNeeded = false;
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            opt_readFiles.setFabOptionColor(Util.getAttrColor(this, R.attr.colorPrimary));
        } else { permissionNeeded = true; }
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
        {
            opt_microphone.setFabOptionColor(Util.getAttrColor(this, R.attr.colorPrimary));
        } else { permissionNeeded = true; }

        if(!permissionNeeded) { container_fabs.removeAllViews(); }
        return permissionNeeded;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Timber.d("%s - %s - %s", requestCode, Arrays.toString(permissions), Arrays.toString(grantResults));
        checkPermissions();
    }


    /**
     * Custom logging tree for Timber
     */
    private class CustomDebugTree extends Timber.DebugTree {
        @Override
        protected String createStackElementTag(StackTraceElement element) {
            return String.format("[%s:%s | %s]",
                    super.createStackElementTag(element),
                    element.getLineNumber(),
                    element.getMethodName()
            );
        }
    }




    /* Create Handler object in main thread. */
    @SuppressLint("HandlerLeak")
    private void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            fftComputing.updateUIHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    // Means the message is sent from child thread.
                    if(msg.what == MESSAGE_UPDATE_TEXT_CHILD_THREAD)
                    {
                        // Update ui in main thread.
                        //FFTFragment.updateText();
                    }
                }
            };
        }
    }


}