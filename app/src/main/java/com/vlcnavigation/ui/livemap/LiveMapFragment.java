package com.vlcnavigation.ui.livemap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pixplicity.sharp.Sharp;
import com.pixplicity.sharp.SharpDrawable;
import com.vlcnavigation.R;
import com.vlcnavigation.module.svg2vector.Utils;
import com.vlcnavigation.module.trilateration.Trilateration;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

public class LiveMapFragment extends Fragment {

    // FIXME: https://github.com/brandonlw/magstripereader


    private static final int FREQUENCY = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int _bufferSize;
    private AudioRecord _audioRecord;

    private AsyncTask<Void, Void, ParseResult> _task;

    private TextView textView;
    private ConstraintLayout container_map;
    private FloatingActionButton fab1, fab2, fab3;

    private LiveMapViewModel liveMapViewModel;

    private List<String> svgs;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        liveMapViewModel = new ViewModelProvider(this).get(LiveMapViewModel.class);
        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        this.svgs = Utils.listSvgAsString(getResources().openRawResource(R.raw.isep_map));
        if(svgs != null) { for(String s : svgs) { System.out.println(s); } }
        else { System.out.println("ARRAY NULL"); }


//        try{
//            Trilateration.triangulate();
//        } catch (Exception ex){ Timber.e(ex);}







//        try
//        {
//            _bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING) * 8;
//
//            Timber.d("Current settings:" + '\n' +
//                    "FREQUENCY | CHANNEL_CONFIG | AUDIO_ENCODING | BUFFER_SIZE"+ '\n' +
//                    "%9s | %14s | %14s | %11s", FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING, _bufferSize);
//
//            _audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING, _bufferSize);
//
//            if(_audioRecord != null)
//            {
//                _audioRecord.startRecording();
//                _task = new MonitorAudioTask();
//                _task.execute(null, null, null);
//            }
//
//        }
//        catch (Exception ex)
//        {
//            Timber.e(ex);
//        }
        return root;
    }

    @SuppressLint("ResourceType")
    private void makeMap(String str) throws IOException {

        ImageView mapPart = new ImageView(getContext());
        mapPart.setDrawingCacheEnabled(true);
        mapPart.setId(View.generateViewId());
        mapPart.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
                        int color = bmp.getPixel((int) event.getX(), (int) event.getY());
                        boolean _return = false;
                        if (color == Color.TRANSPARENT) {}
//                            return false;
                        else {
                            _return = true; //click portion without transparent color
                        }
                        Timber.d("TRANSPARENT : %s", _return);
                        return _return;

                    }

                }
        );
        mapPart.set

        Timber.d("Id: %s", mapPart.getId());

//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(container_map);
//        constraintSet.constrainDefaultHeight(mapPart.getId(), ConstraintSet.MATCH_CONSTRAINT_SPREAD);
//        constraintSet.constrainDefaultWidth(mapPart.getId(), ConstraintSet.MATCH_CONSTRAINT_SPREAD);
//        constraintSet.applyTo(container_map);

        mapPart.setLayoutParams(new ConstraintLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        Drawable drawable;
        if(str.equals("")) {
            drawable = Sharp.loadResource(getResources(), R.raw.isep_map).getDrawable();
        }
        else {
            InputStream is = new ByteArrayInputStream(str.getBytes());
            drawable = Sharp.loadInputStream(is).getDrawable();
            is.close();
        }

        mapPart.setImageDrawable(drawable);

//        mapPart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getContext(), String.format("Id: %s", mapPart.getId()), Toast.LENGTH_SHORT).show();
//            }
//        });


        container_map.addView(mapPart);
    }



    private void initListeners() {
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for(String svg : svgs)
                    {
//                        Timber.w(svg);
                        makeMap(svg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" width=\"522px\" height=\"202px\" viewBox=\"-0.5 -0.5 522 202\" content=\"&lt;mxfile host=&quot;Electron&quot; modified=&quot;2021-03-09T10:38:23.073Z&quot; agent=&quot;Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) draw.io/11.3.0 Chrome/76.0.3809.139 Electron/6.0.7 Safari/537.36&quot; etag=&quot;4RtL0t270lhN2_pML5CD&quot; version=&quot;11.3.0&quot; type=&quot;device&quot; pages=&quot;1&quot;&gt;&lt;diagram id=&quot;ITolrCyJ6mziRDKKo1it&quot; name=&quot;Page-1&quot;&gt;3Zddr5sgGMc/jZdLVPDtdl17tmQnS06TnWsKjy8ZiqE42336oaLV1aY9J7VbdmPg/wDCj78PaKFVfniSpEyfBQNuuTY7WOiT5bp+FOhnIxw7AUVRJyQyY53knIRt9guMaBu1yhjsJw2VEFxl5VSkoiiAqolGpBT1tFks+PStJUngTNhSws/V14yptFNDNzjpnyFL0v7Njm/Wl5O+sVnJPiVM1CMJrS20kkKorpQfVsAbdj2Xrt/mQnSYmIRC3dJhvREvG/wNP5Xbr68oCV6+o+cPZpSfhFdmwWay6tgTkKIqGDSD2Bb6WKeZgm1JaBOt9ZZrLVU51zVHF+OM85XgQrZ9URzHLqVa3yspfsAowvyd7/k6YiYAUsHh4sqcgZf2GYgclDzqJn0H3yA2HsOmWo82zEjpaK96jRiLJMPAJ4q6YEC+Aaq7LFRGIIxnofo0hF18J6juFOpQH1HF9gxWvBTW8DpW/Y2VTTEnRUX4l6Ks1HW8+y7p4GYnpFBEZaLQ9cg+Rw8O8yCYQx/5ASJ38nP4B3n/L/s5uh08FbIAeZ15ez60xNuDIZxhHYcU5nPHLvSwZy/C+sbUMXwMd2fdn3tLuXyW9EOyNP7XbO0snKcfkywGy152sDuXppfDiv6HOwW+jvWxZsULU31ItvXcd1F9x5VCV09X6zY2+j9B698=&lt;/diagram&gt;&lt;/mxfile&gt;\" style=\"background-color: rgb(255, 255, 255);\"><defs/><g><rect x=\"40\" y=\"80\" width=\"400\" height=\"40\" fill=\"#dae8fc\" stroke=\"#6c8ebf\" pointer-events=\"none\"/></g></svg>";

                    Timber.w(svg);


                    makeMap(svg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    makeMap("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void initObservers() {
        liveMapViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View root = inflater.inflate(R.layout.fragment_live_map, container, false);

        textView = root.findViewById(R.id.text_home);
        container_map = root.findViewById(R.id.container_map);

        fab1 = root.findViewById(R.id.fab_generateTestData_map_1);
        fab2 = root.findViewById(R.id.fab_generateTestData_map_2);
        fab3 = root.findViewById(R.id.fab_generateTestData_map_3);

        return root;
    }

    private class MonitorAudioTask extends AsyncTask<Void, Void, ParseResult>
    {
        @Override
        protected ParseResult doInBackground(Void... params)
        {
            final double QUIET_THRESHOLD = 32768.0 * 0.02; //anything higher than 0.02% is considered non-silence
            final double QUIET_WAIT_TIME_SAMPLES = FREQUENCY * 0.25; //~0.25 seconds of quiet time before parsing
            short[] buffer = new short[_bufferSize];
            Long bufferReadResult = null;
            boolean nonSilence = false;
            ParseResult result = null;

            while (!nonSilence)
            {
                if (isCancelled())
                    break;

                bufferReadResult = new Long(_audioRecord.read(buffer, 0, _bufferSize));
                if (bufferReadResult > 0)
                {
                    for (int i = 0; i < bufferReadResult; i++)
                        if (buffer[i] >= QUIET_THRESHOLD)
                        {
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            long silentSamples = 0;

                            //Save this data so far
                            for (int j = i; j < bufferReadResult; j++)
                            {
                                stream.write(buffer[j] & 0xFF);
                                stream.write(buffer[j] >> 8);
                            }

                            //Keep reading until we've reached a certain amount of silence
                            boolean continueLoop = true;
                            while (continueLoop)
                            {
                                bufferReadResult = new Long(_audioRecord.read(buffer, 0, _bufferSize));
                                if (bufferReadResult < 0)
                                    continueLoop = false;

                                for (int k = 0; k < bufferReadResult; k++)
                                {
                                    stream.write(buffer[k] & 0xFF);
                                    stream.write(buffer[k] >> 8);
                                    if (buffer[k] >= QUIET_THRESHOLD || buffer[k] <= -QUIET_THRESHOLD)
                                        silentSamples = 0;
                                    else
                                        silentSamples++;
                                }

                                if (silentSamples >= QUIET_WAIT_TIME_SAMPLES)
                                    continueLoop = false;
                            }

                            //Convert to array of 16-bit shorts
                            byte[] array = stream.toByteArray();
                            short[] samples = new short[array.length / 2];
                            for (int k = 0; k < samples.length; k++)
                                samples[k] = (short)((short)(array[k * 2 + 0] & 0xFF) | (short)(array[k * 2 + 1] << 8));

                            //Try parsing the data now!
                            result = CardDataParser.Parse(samples);
                            if (result.errorCode != 0)
                            {
                                //Reverse the array and try again (maybe it was swiped backwards)
                                for (int k = 0; k < samples.length / 2; k++)
                                {
                                    short temp = samples[k];
                                    samples[k] = samples[samples.length - k - 1];
                                    samples[samples.length - k - 1] = temp;
                                }
                                result = CardDataParser.Parse(samples);
                            }

                            nonSilence = true;
                            break;
                        }
                }
                else
                    break;
            }

            return result;
        }

        @Override
        protected void onPostExecute(ParseResult result)
        {
            if (result != null)
            {
                String str = "Data:\r\n" + result.data + "\r\n\r\n";
                if (result.errorCode == 0)
                    str += "Success";
                else
                {
                    String err = Integer.toString(result.errorCode);
                    switch (result.errorCode)
                    {
                        case -1:
                        {
                            err = "NOT_ENOUGH_PEAKS";
                            break;
                        }
                        case -2:
                        {
                            err = "START_SENTINEL_NOT_FOUND";
                            break;
                        }
                        case -3:
                        {
                            err = "PARITY_BIT_CHECK_FAILED";
                            break;
                        }
                        case -4:
                        {
                            err = "LRC_PARITY_BIT_CHECK_FAILED";
                            break;
                        }
                        case -5:
                        {
                            err = "LRC_INVALID";
                            break;
                        }
                        case -6:
                        {
                            err = "NOT_ENOUGH_DATA_FOR_LRC_CHECK";
                            break;
                        }
                    }

                    str += "Error: " + err;
                }

                liveMapViewModel.setText(str);
            }
            else
                liveMapViewModel.setText("[Parse Error]");

            //Now start the task again
            _task = new MonitorAudioTask();
            _task.execute(null, null, null);
        }
    }
}