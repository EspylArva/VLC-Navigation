package com.vlcnavigation.ui.fft;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

//import com.android.ide.common.vectordrawable.Svg2Vector;
import com.vlcnavigation.R;

//csv reading
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
//import org.apache.commons.math3.complex.Complex;


//FFTPack imports

import android.media.AudioFormat;

import android.media.AudioRecord;

import android.media.MediaRecorder;

import android.os.AsyncTask;

import android.util.Log;

import android.view.View.OnClickListener;

import android.widget.Button;

import ca.uol.aig.fftpack.RealDoubleFFT;


public class FFTFragment extends Fragment {

    public double frequency = 0.0;                      // the frequency given
    int audioSource = MediaRecorder.AudioSource.MIC;    // Audio source is the device MIC
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;    // Recording in mono
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; // Records in 16bit
    int blockSize = 256;                               // deal with this many samples at a time
    int sampleRate = 8000;                             // Sample rate in Hz
    RecordAudio recordTask;                             // Creates a Record Audio command
    TextView textView;                                        // Creates a text view for the frequency
    boolean started = false;
    Button startStopButton, playButton, stopButton;

    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    private FFTViewModel FFTViewModel;
    //private List<String> rssData = new ArrayList<String>();
    private double[][] rssData;
    private double[][] distancesArray;
    //private DoubleFFT_1D fft;                           // The fft double array
    private RealDoubleFFT transformer;



    //https://stackoverflow.com/questions/17429407/get-frequency-wav-audio-using-fft-and-complex-class²
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    short[] audioData;

    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    Complex[] fftTempArray;
    Complex[] fftArray;
    int[] bufferData;
    int bytesRecorded;
    double[] absNormalizedSignal;
    int mPeakPos;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FFTViewModel =
                new ViewModelProvider(this).get(FFTViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fft, container, false);

        //findviews
        textView = root.findViewById(R.id.textView3);
        startStopButton = root.findViewById(R.id.StartStopButton);
        stopButton = root.findViewById(R.id.stopButton);
        playButton = root.findViewById(R.id.PlayButton);
        imageView = root.findViewById(R.id.ImageView01);


        //https://stackoverflow.com/questions/17429407/get-frequency-wav-audio-using-fft-and-complex-class
        bufferSize = AudioRecord.getMinBufferSize
                (RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING)*3;

        audioData = new short [bufferSize]; //short array that pcm data is put into.


        playButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //play sound on speaker

                /*
                //SoundPoolPlayer qSound = new SoundPoolPlayer(getContext());
                //qSound.playShortResource(R.raw.q);

                System.out.println("-----------------"+Environment.getExternalStorageDirectory()+"/"+"Download/q.wav");
                try {
                    PlayShortAudioFileViaAudioTrack(Environment.getExternalStorageDirectory()+"/"+"Download/q.wav");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                */



                //read sound





                /*
                double[] transformed = new double[(int) file.length()];
                for (int j=1;j<file.length();j++) {
                    transformed[j] = byteData[j]; }

                */


                //compute fft
                /*

                //audiodataDoubles now holds data to work with
                // fft.complexForward(audioDataDoubles);
                try{
                    transformer.ft(transformed);
                    System.out.println("fft array : "+Arrays.toString(transformed));
                } catch(IllegalArgumentException e){
                    e.printStackTrace();
                }

                //publishProgress(transformed);

                */
                /*


                //FFT
                //   -----------------------------------------------
                double[] re = new double[blockSize];
                double[] im = new double[blockSize];
                double[] magnitude = new double[blockSize];
                //   ----------------------------------------------------
                double[] toTransform = new double[blockSize];

                // Calculate the Real and imaginary and Magnitude.
                System.out.println("------------");
                for (int i = 0; i < blockSize; i++) {
                    try {
                        // real is stored in first part of array
                        re[i] = transformed[i * 2];
                        //System.out.println("Re = "+re[i]);
                        // imaginary is stored in the sequential part
                        im[i] = transformed[(i * 2) + 1];
                        //System.out.println("im = "+im[i]);
                        // magnitude is calculated by the square root of (imaginary^2 + real^2)
                        magnitude[i] = Math.sqrt((re[i] * re[i]) + (im[i] * im[i]));
                        //System.out.println("magnitude = "+magnitude[i]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("test", "NULL");
                    }
                }

                double peak = -1.0;
                // Get the largest magnitude peak
                for (int i = 0; i < blockSize; i++) {
                    if (peak < magnitude[i])
                        peak = magnitude[i];
                    System.out.println("peak = " + peak);
                    System.out.println("magnitude = " + magnitude[i]);

                }
                // calculated the frequency
                frequency = ((sampleRate * peak) / blockSize);
                System.out.println("frequency = " + frequency);

                */




            }
        });


        startStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                //startRecording();


                File file = null;file = new File(Environment.getExternalStorageDirectory()+"/"+"Download/sin_1000.wav");
                byte[] byteData = new byte[(int) file.length()];
                FileInputStream in = null;
                try {
                    in = new FileInputStream( file );
                    in.read( byteData );
                    in.close(); }
                catch (Throwable throwable){
                    throwable.printStackTrace();
                }



                absNormalizedSignal = calculateFFT(byteData);

                System.out.println("Data FFT absNormalizedSignal : "+Arrays.toString(absNormalizedSignal));
                System.out.println("Data FFT peak : "+mPeakPos);

            }
        });

        stopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                //calculate();
            }
        });


        FFTViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);

                //parse data from csv
                /*
                dataParser();
                distanceComputing();
                */


            }
        });

        return root;
    }


    //Most of the work in this activity is done in the following class, called RecordAudio, which extends AsyncTask. Using AsyncTask, we run the methods that will tie up the user interface on a separate thread. Anything that is placed in the doInBackground method will be run in this manner.

    public void dataParser() {

        InputStream inputStream = getResources().openRawResource(R.raw.powerratio2);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int i = 0;

        try (

                CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT
                        .withHeader("Tx1", "Tx2", "Tx3", "Tx4")
                        .withIgnoreHeaderCase()
                        .withSkipHeaderRecord()
                        .withTrim())
        ) {

            System.out.println("record number : " + Math.toIntExact(csvParser.getRecordNumber()));
            //rssData = new double[Math.toIntExact(csvParser.getRecordNumber())][4];
            rssData = new double[5][4];


            for (CSVRecord csvRecord : csvParser) {
                // Accessing values by the names assigned to each column
                Double Tx1 = Double.valueOf(csvRecord.get("Tx1"));
                Double Tx2 = Double.valueOf(csvRecord.get("Tx2"));
                Double Tx3 = Double.valueOf(csvRecord.get("Tx3"));
                Double Tx4 = Double.valueOf(csvRecord.get("Tx4"));

                System.out.println("---------------");
                System.out.println("Line number - " + csvRecord.getRecordNumber());

                System.out.println("Tx1 : " + Tx1);
                System.out.println("Tx2 : " + Tx2);
                System.out.println("Tx3 : " + Tx3);
                System.out.println("Tx4 : " + Tx4);


                rssData[i][0] = Tx1;
                rssData[i][1] = Tx2;
                rssData[i][2] = Tx3;
                rssData[i][3] = Tx4;
                i++;


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("-------LISTE RSS--------\n\n");
        System.out.println(Arrays.deepToString(rssData));

    }

    //onProgressUpdate runs on the main thread in our activity and can therefore interact with the user interface without problems. In this implementation, we are passing in the data after it has been run through the FFT object. This method takes care of drawing the data on the screen as a series of lines at most 100 pixels tall. Each line represents one of the elements in the array and therefore a range of 15.625 Hz. The first line represents frequencies ranging from 0 to 15.625 Hz, and the last line represents frequencies ranging from 3,984.375 to 4,000 Hz. Figure 8-1 shows what this looks like in action.

    public void distanceComputing() {

        distancesArray = new double[5][4];

        double m, P, Ts, H, A, Dx1, Dx2, Dx3, Dx4;
        A = 1;
        m = 1;
        H = 1;

        for (int i = 0; i < rssData.length; i++) {
            for (int j = 0; j < rssData[i].length; j++) {
                System.out.println("-------Distance--------");
                distancesArray[i][j] = Math.pow(rssData[i][j] * ((A * (m + 1) * Math.pow(H, m + 1)) / 2 * Math.PI), 1 / m + 3);
                System.out.println(distancesArray[i][j]);
            }
        }

        //Dx1 = Math.pow( Tx1 * ((A * (m+1) * Math.pow(H,m+1)) / 2 * Math.PI) , 1 / m+3 );


        System.out.println("-------LISTE Distances--------\n\n");
        System.out.println(Arrays.deepToString(distancesArray));


    }

    public void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException {
// We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath == null)
            return;

//Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byteData = new byte[(int) file.length()];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(byteData);
            in.close();

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }
// Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_8BIT, intSize, AudioTrack.MODE_STREAM);
        if (at != null) {
            at.play();
// Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        } else
            Log.d("TCAudio", "audio track is not initialised ");

    }

    private void PlayAudioFileViaAudioTrack(String filePath) throws IOException {
// We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath == null)
            return;

        int intSize = android.media.AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
                AudioFormat.ENCODING_PCM_16BIT, intSize, AudioTrack.MODE_STREAM);


        if (at == null) {
            Log.d("TCAudio", "audio track is not initialised ");
            return;
        }

        int count = 512 * 1024; // 512 kb
//Reading the file..
        byte[] byteData = null;
        File file = null;
        file = new File(filePath);

        byteData = new byte[count];
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);

        } catch (FileNotFoundException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        int bytesread = 0, ret = 0;
        int size = (int) file.length();
        at.play();
        while (bytesread < size) {
            ret = in.read(byteData, 0, count);
            if (ret != -1) {
                // Write the byte array to the track at.write(byteData,0, ret); bytesread += ret; } else break; } in.close(); at.stop(); at.release();
            }
        }
    }

    private class AndroidAudioDevice {


        AudioTrack track;

        short[] buffer = new short[1024];


        public AndroidAudioDevice() {

            int minSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

            track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,

                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,

                    minSize, AudioTrack.MODE_STREAM);

            track.play();

        }


        public void writeSamples(float[] samples) {

            fillBuffer(samples);

            track.write(buffer, 0, samples.length);

        }


        private void fillBuffer(float[] samples) {

            if (buffer.length < samples.length)

                buffer = new short[samples.length];


            for (int i = 0; i < samples.length; i++)

                buffer[i] = (short) (samples[i] * Short.MAX_VALUE);

        }
    }

    public class SoundPoolPlayer {
        private SoundPool mShortPlayer= null;
        private HashMap mSounds = new HashMap();

        public SoundPoolPlayer(Context pContext)
        {
            // setup Soundpool
            this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);


            mSounds.put(R.raw.q, this.mShortPlayer.load(pContext, R.raw.q, 1));

        }

        public void playShortResource(int piResource) {
            int iSoundId = (Integer) mSounds.get(piResource);
            this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
        }

        // Cleanup
        public void release() {
            // Cleanup
            this.mShortPlayer.release();
            this.mShortPlayer = null;
        }
    }


    private class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... params) {

            /*Calculates the fft and frequency of the input*/
            //try{
            int bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioEncoding);                // Gets the minimum buffer needed
            AudioRecord audioRecord = new AudioRecord(audioSource, sampleRate, channelConfig, audioEncoding, bufferSize);   // The RAW PCM sample recording


            short[] buffer = new short[blockSize];          // Save the raw PCM samples as short bytes

            //  double[] audioDataDoubles = new double[(blockSize*2)]; // Same values as above, as doubles
            //   -----------------------------------------------
            double[] re = new double[blockSize];
            double[] im = new double[blockSize];
            double[] magnitude = new double[blockSize];
            //   ----------------------------------------------------
            double[] toTransform = new double[blockSize];

            textView.setText("Hello");
            // fft = new DoubleFFT_1D(blockSize);


            try {
                audioRecord.startRecording();  //Start
                Log.d("AudioRecord", "Recording started");
            } catch (Throwable t) {
                Log.e("AudioRecord", "Recording Failed");
            }

            while (started) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /* Reads the data from the microphone. it takes in data
                 * to the size of the window "blockSize". The data is then
                 * given in to audioRecord. The
                 * *int returned is the number
                 * of bytes that were read*/

                int bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                // Read in the data from the mic to the array
                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {

                    /* dividing the short by 32768.0 gives us the
                     * result in a range -1.0 to 1.0.
                     * Data for the compextForward is given back
                     * as two numbers in sequence. Therefore audioDataDoubles
                     * needs to be twice as large*/

                    // audioDataDoubles[2*i] = (double) buffer[i]/32768.0; // signed 16 bit
                    //audioDataDoubles[(2*i)+1] = 0.0;
                    toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit

                }

                //audiodataDoubles now holds data to work with
                // fft.complexForward(audioDataDoubles);
                transformer.ft(toTransform);
                publishProgress(toTransform);
                //------------------------------------------------------------------------------------------
                // Calculate the Real and imaginary and Magnitude.
                System.out.println("------------");
                for (int i = 0; i < blockSize; i++) {
                    try {
                        // real is stored in first part of array
                        re[i] = toTransform[i * 2];
                        //System.out.println("Re = "+re[i]);
                        // imaginary is stored in the sequential part
                        im[i] = toTransform[(i * 2) + 1];
                        //System.out.println("im = "+im[i]);
                        // magnitude is calculated by the square root of (imaginary^2 + real^2)
                        magnitude[i] = Math.sqrt((re[i] * re[i]) + (im[i] * im[i]));
                        //System.out.println("magnitude = "+magnitude[i]);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.e("test", "NULL");
                    }
                }

                double peak = -1.0;
                // Get the largest magnitude peak
                for (int i = 0; i < blockSize; i++) {
                    if (peak < magnitude[i])
                        peak = magnitude[i];
                    System.out.println("peak = " + peak);
                    System.out.println("magnitude = " + magnitude[i]);

                }
                // calculated the frequency
                frequency = ((sampleRate * peak) / blockSize);
                System.out.println("frequency = " + frequency);
//----------------------------------------------------------------------------------------------
                /* calls onProgressUpdate
                 * publishes the frequency
                 */
                //publishProgress(frequency);

                try {
                    audioRecord.stop();
                } catch (IllegalStateException e) {
                    Log.e("Stop failed", e.toString());

                }
            }

            //    }
            return null;
        }

        protected void onProgressUpdate(double[]... toTransform) {
            //print the frequency
            //String info = Double.toString(frequencies[0]);
            //textView.setText(info);


            canvas.drawColor(Color.BLACK);

            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;
                int downy = (int) (100 - (toTransform[0][i] * 10));
                int upy = 100;

                canvas.drawLine(x, downy, x, upy, paint);
            }

            imageView.invalidate();

        }

    }


    //https://stackoverflow.com/questions/17429407/get-frequency-wav-audio-using-fft-and-complex-class

    /*private void setButtonHandlers() {
        ((Button)findViewById(R.id.btStart)).setOnClickListener(btnClick);
        ((Button)findViewById(R.id.btStop)).setOnClickListener(btnClick);
    }*/



    private String getFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        if(!file.exists()){
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
    }


    public void convert(){



    }

    public double[] calculateFFT(byte[] signal)
    {
        final int mNumberOfFFTPoints =1024;
        double mMaxFFTSample;

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];

        for(int i = 0; i < mNumberOfFFTPoints; i++){
            temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }

        System.out.println("Complex Signal array : "+Arrays.toString(complexSignal));

        y = FFT.fft(complexSignal); // --> Here I use FFT class
        System.out.println("Complex Signal array after FFT : "+Arrays.toString(y));

        mMaxFFTSample = 0.0;
        mPeakPos = 0;
        for(int i = 0; i < (mNumberOfFFTPoints/2); i++)
        {
            absSignal[i] = Math.sqrt(Math.pow(y[i].re(), 2) + Math.pow(y[i].im(), 2));
            if(absSignal[i] > mMaxFFTSample)
            {
                mMaxFFTSample = absSignal[i];
                mPeakPos = i;
            }
        }

        return absSignal;

    }


    private String getTempFilename(){
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath,AUDIO_RECORDER_FOLDER);

        Log.d("getTempFilename","file = "+filepath);

        if(!file.exists()){
            file.mkdirs();
        }

        File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);


        if(tempFile.exists())
            tempFile.delete();
        try{
            file.createNewFile();
        }catch (Error | IOException error){
            Log.e("File creation problem", String.valueOf(error));
        }


        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    private void startRecording(){
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);

        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {

            public void run() {
                writeAudioDataToFile();
            }
        },"AudioRecorder Thread");

        recordingThread.start();
    }

    private void writeAudioDataToFile(){

        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        Log.d("writeAudioDataToFile","Filename = "+filename);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;
        if(null != os){
            while(isRecording){
                read = recorder.read(data, 0, bufferSize);
                if(read > 0){
                    absNormalizedSignal = calculateFFT(data); // --> HERE ^__^
                }

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopRecording(){
        if(null != recorder){
            isRecording = false;

            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(),getFilename());
        // deleteTempFile();
    }

    private void deleteTempFile() {
        File file = new File(getTempFilename());
        file.delete();
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            Log.d("File size: " , String.valueOf(totalDataLen));


            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {
        //another code

    }



}