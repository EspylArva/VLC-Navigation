package com.vlcnavigation.ui.fft;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.VoicemailContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

//import com.android.ide.common.vectordrawable.Svg2Vector;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.vlcnavigation.MainActivity;
import com.vlcnavigation.R;
import com.vlcnavigation.module.audiorecord.SignalView;

import org.apache.commons.lang3.ArrayUtils;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import timber.log.Timber;


public class FFTFragment extends Fragment {

    private static final int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
    private FFTViewModel FFTViewModel;
    private SignalView signalView;
    private ToggleButton tB;
    private Button btnAnalyse;
    private EditText edtName,edtOffset,edtSampleRate;
    protected SeekBar sensBar;

    protected SeekBar sensBarSmooth;
    protected TextView tvLiveFreq, tvWavFreq;
    private Handler updateUIHandler = null;



    //plot
    private XYPlot plot_fft;
    protected int powOf2temp=(int)Math.pow(2,2);
    private String TAG = "FFT Fragment : ";


    //fft
    protected short sData[] = new short[1024];
    protected double sDataAverage=0;
    protected double fftData[]= new double[1024];
    protected int[] oldSentData=new int[3];
    protected SimpleXYSeries series1;
    protected double liveFrequency = 0;
    protected double wavFrequency = 0;

    //parameters
    protected double liveOffset = 21.428;
    protected double wavOffset = 0.07557265176877;
    double sampleRate = 44100;


    //audio record
    private AudioRecord audioInput = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;


    //wav file reader
    double[] absNormalizedSignal;
    int mPeakPos;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FFTViewModel =
                new ViewModelProvider(this).get(FFTViewModel.class);

        View root = initViews(inflater, container);
        initObservers();
        initListeners();

        //initialize the fft_plot
        initPlot();



        //start the thread to read microphone live audio fft and then plot
        //the trigger is the toggle button
        liveAudioFFT();




        //start the wav file audio FFT (results in an array, printed in the terminal)
        btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sampleRate = edtSampleRate.getText().toString();

                if (sampleRate.matches("")) {

                    Timber.d(TAG,FFTFragment.this.sampleRate);
                    FFTFragment.this.sampleRate = 44100;
                }
                else {
                    FFTFragment.this.sampleRate = Double.parseDouble(edtSampleRate.getText().toString());
                }

                String offset = edtOffset.getText().toString();

                if (offset.matches("")) {
                    FFTFragment.this.liveOffset = 21.428;
                    Timber.d(TAG,FFTFragment.this.liveOffset);

                }
                else {
                    FFTFragment.this.liveOffset = Double.parseDouble(edtOffset.getText().toString());
                }


                String filename = edtName.getText().toString();

                if (filename.matches("")) {
                    Toast.makeText(getActivity(), "You did not enter a filename", Toast.LENGTH_SHORT).show();
                    Timber.w("No filename entered");
                    return;
                }
                else {
                    //launch wav audio file fft
                    wavAudioFFT(filename);
                }


            }
        });













        return root;
    }

    private void initObservers() {
    }

    private void initListeners() {


        //this is the trigger of the liveAudioFFT function (this function waits for this button to be clicked)

        tB.setOnContextClickListener(new View.OnContextClickListener() {
            @Override
            public boolean onContextClick(View v) {





                return false;
            }
        });

    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View root = inflater.inflate(R.layout.fragment_fft, container, false);
        signalView = root.findViewById(R.id.signalview);
        plot_fft = root.findViewById(R.id.plot_fft);
        tB = root.findViewById(R.id.toggleButton2);
        sensBar = root.findViewById(R.id.seekBar);
        sensBarSmooth = root.findViewById(R.id.seekBarSmooth);
        tvLiveFreq = root.findViewById(R.id.tv_livefreq);
        tvWavFreq = root.findViewById(R.id.tv_wavfreq);
        btnAnalyse = root.findViewById(R.id.btn_analyse);

        btnAnalyse = root.findViewById(R.id.btn_analyse);
        edtName = root.findViewById(R.id.edt_name);
        edtOffset= root.findViewById(R.id.edt_liveoffset);
        edtSampleRate= root.findViewById(R.id.edt_samplerate);

        //init the handler to record audio
        createUpdateUiHandler();



        //signalView.sndAudioBuf(MainActivity.BUFFER, MainActivity.BUFFER_READ_RESULT);



        return root;
    }



    private void initPlot(){


        // Create a couple arrays of y-values to plot:
        final Number[] series1Numbers = new Number[1024/powOf2temp];

        for (int j=0;j<series1Numbers.length;j++) {
            series1Numbers[j]=j;
        }

        // Turn the above arrays into XYSeries':
        series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        final LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(null);
        series1Format.configure(getActivity(),
                R.xml.line_point_formatter);

        // add a new series' to the xyplot:
        plot_fft.addSeries(series1, series1Format);


        // reduce the number of range labels

        /*
        plot_fft.setTicksPerRangeLabel(9);
        plot_fft.getGraphWidget().setDomainLabelOrientation(-45);
        plot_fft.getLegendWidget().setVisible(false);
        */

        // set axis limits
        plot_fft.setRangeBoundaries(0, 100000, BoundaryMode.FIXED);
        plot_fft.setDomainBoundaries(0, 255, BoundaryMode.FIXED);
        ////////////////////////////////////////////////////////////////////////////////////////////
    }







    private void liveAudioFFT(){


        int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        int RECORDER_SAMPLERATE= 44100;
        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        audioInput = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

        // Fast Fourier Transform from JTransforms
        final DoubleFFT_1D fft = new DoubleFFT_1D(sData.length);

        // Start recording
        audioInput.startRecording();
        isRecording = true;




        recordingThread = new Thread(new Runnable() {






            public void run() {


                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                while (isRecording) {
                    if (tB.isChecked()) {

                        // Record audio input
                        audioInput.read(sData, 0, sData.length);



                        // Convert and put sData short array into fftData double array to perform FFT
                        for (int j = 0; j < sData.length; j++) {
                            fftData[j] = (double) sData[j];
                        }

                        // Perform 1D fft
                        fft.realForward(fftData);
                        //System.out.println("fftData before = "+Arrays.toString(fftData));

                        //convert abs values
                        for (int j = 0; j < fftData.length; j++) fftData[j] = Math.abs(fftData[j]);
                        //System.out.println("fftData = "+Arrays.toString(fftData));

                        //System.out.println("fftData = "+Arrays.toString(fftData));

                        //Frequency
                        FFTFragment.this.liveFrequency = calculateFrequency(fftData, liveOffset,true);
                        System.out.println("Frequency : "+ liveFrequency);

                        Message freqMsg = new Message();
                        freqMsg.what = MESSAGE_UPDATE_TEXT_CHILD_THREAD;


                        updateUIHandler.sendMessage(freqMsg);






                        // Update plot //
                        for (int j = 0; j < series1.size(); j++) {
                            series1.removeFirst();
                            series1.addLast(null, fftData[j * powOf2temp] * powOf2temp);
                        }
                        int sensBarProgress = sensBar.getProgress();
                        //plot_fft.setRangeBoundaries(0, (100-((sensBarProgress==100)?99:sensBarProgress)) * 1000, BoundaryMode.FIXED);
                        plot_fft.redraw();
                        Log.d(TAG,"plot redrawn");
                        ////////////////

                        final int dataToSend[] = new int[3];
                        int smoothness=sensBarSmooth.getProgress()+192;

                        for (int freqDomain = 0; freqDomain < 3; freqDomain++) {
                            sDataAverage = 0;
                            for (int i = freqDomain * 1024 * 2 / 9; i < (freqDomain + 1) * 1024 * 2 / 9; i++)
                                sDataAverage += fftData[i];
                            sDataAverage /= 1024 / (3 * (float)sensBarProgress/500);

                            // Limit the value to 255
                            dataToSend[freqDomain] = (sDataAverage > 255) ? 255 : (int) sDataAverage;
                            // Limit the amplitude fall
                            dataToSend[freqDomain] =
                                    (dataToSend[freqDomain]  < oldSentData[freqDomain]*(1-(float)(257-smoothness)/255)) ?
                                            (int)(oldSentData[freqDomain]*(1-(float)(257-smoothness)/255)) :
                                            dataToSend[freqDomain];

                            oldSentData[freqDomain] = dataToSend[freqDomain];
                        }

                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TextView tv = (TextView) findViewById(R.id.textView_debug);
                                Log.d(TAG,"Average amplitudes : " + String.valueOf(Arrays.toString(dataToSend)));

                                //sendData("R" + dataToSend[0] + "G" + dataToSend[1] + "B" + dataToSend[2]);
                            }
                        });*/
                    }
                }
            }



        }, "AudioRecorder Thread");
        recordingThread.start();
        ////////////////////////////////////////////////////////////////////////////////////////////

    }

    private void wavAudioFFT(String filename){


        File file = null;
        file = new File(Environment.getExternalStorageDirectory()+"/"+"Download/"+filename+".wav");
        if (!file.exists()) {
            Toast.makeText(getActivity(), "This file does not exist.", Toast.LENGTH_SHORT).show();
            Timber.e("No such file or directory");
            return;
        }
        byte[] byteData = new byte[(int) file.length()];
        System.out.println("Using "+file.getName()+" ___________________________________________________");
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            in.close(); }
        catch (Throwable throwable){
            throwable.printStackTrace();
        }
        System.out.println("Wav audio file byteData"+Arrays.toString(byteData));
        Toast.makeText(getActivity(), "Using "+this.sampleRate+"Hz as sample rate & "+this.liveOffset+" as offset.", Toast.LENGTH_SHORT).show();

        try{
            //creation of the audio array
            double[] fftDataWav = new double[byteData.length];
            double[] absFftDataWav = new double[byteData.length];


            // Convert and put sData short array into fftDataWav double array to perform FFT
            for (int j = 0; j < byteData.length; j++) {
                fftDataWav[j] = (double) byteData[j];
            }

            System.out.println("Wav audio file fftDataWav double converted"+Arrays.toString(fftDataWav));


            // Fast Fourier Transform from JTransforms
            final DoubleFFT_1D fftWav = new DoubleFFT_1D(fftDataWav.length);
            // Perform 1D fft
            fftWav.realForward(fftDataWav);
            System.out.println("Wav audio file fftDataWav after FFT = "+Arrays.toString(fftDataWav));

            //convert abs values
            for (int j = 0; j < fftDataWav.length; j++){
                double x = fftDataWav[j];
                //System.out.println("fftDataWav[j] = "+x+" | ");
                //System.out.println("Math.abs(fftDataWav[j]) = "+Math.abs(x)+" | ");
                absFftDataWav[j] = Math.abs(x);
            }
            System.out.println("absFftDataWav = "+Arrays.toString(absFftDataWav));

            wavFrequency = calculateFrequency(absFftDataWav,wavOffset,false);

            tvWavFreq.setText(String.valueOf(wavFrequency));

            System.out.println("Wav Audio File Frequency : "+ wavFrequency);


        /* OLD method
        absNormalizedSignal = calculateFFT(byteData);



        //System.out.println("Data FFT absNormalizedSignal : "+Arrays.toString(absNormalizedSignal));
        //System.out.println("Data FFT peak : "+mPeakPos);
        */
        }
        catch (Error e){
            Timber.d(e.getMessage());
        }



    }

    public double calculateFrequency(double[] fft,double offset,boolean live)
    {
        double[] fftSorted = fft.clone();
        Arrays.sort(fftSorted);
        ArrayUtils.reverse(fftSorted);
        System.out.println("fftSorted = "+Arrays.toString(fftSorted));
        ArrayUtils.reverse(fftSorted);
        double peak1 = fftSorted[fftSorted.length-1];
        //System.out.println("Peak 1 = "+peak1);
        double peak2 = fftSorted[fftSorted.length-2];
        //System.out.println("Peak 2 = "+peak2);
        double peak3 = fftSorted[fftSorted.length-3];
        //System.out.println("Peak 2 = "+peak2);
        double peak4 = fftSorted[fftSorted.length-4];
        //System.out.println("Peak 2 = "+peak2);
        double peak5 = fftSorted[fftSorted.length-5];
        //System.out.println("Peak 2 = "+peak2);


        int index1 = 0; int index2 = 0; int index3 = 0;int index4 = 0;int index5 = 0;
        for(int i=0; i<fft.length; i++) {
            if(fft[i] == peak1) {
                index1 = i;
            }
            else if(fft[i] == peak2) {
                index2 = i;
            }
            else if(fft[i] == peak3) {
                index3 = i;
            }
            else if(fft[i] == peak4) {
                index4 = i;
            }
            else if(fft[i] == peak5) {
                index5 = i;
            }
        }
        System.out.println("Index 1 = "+index1+"/"+fft.length);
        System.out.println("Index 2 = "+index2+"/"+fft.length);
        System.out.println("Index 3 = "+index3+"/"+fft.length);
        System.out.println("Index 4 = "+index4+"/"+fft.length);
        System.out.println("Index 5 = "+index5+"/"+fft.length);

        if (live){
            if (offset != 0) {
                return ((index1+index2)/2)*offset;
            }
            else{
                return ((index1+index2)/2)*1;
            }
        }
        else {







            System.out.println("1st frequency = "+((index1) * this.sampleRate) / fft.length);
            System.out.println("2nd frequency = "+((index2) * this.sampleRate) / fft.length);
            System.out.println("3rd frequency = "+((index3) * this.sampleRate) / fft.length);
            System.out.println("4th frequency = "+((index4) * this.sampleRate) / fft.length);
            System.out.println("5th frequency = "+((index5) * this.sampleRate) / fft.length);


            return ((index1) * this.sampleRate) / fft.length ;

        }




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




    /* Create Handler object in main thread. */
    private void createUpdateUiHandler()
    {
        if(updateUIHandler == null)
        {
            updateUIHandler = new Handler()
            {
                @Override
                public void handleMessage(Message msg) {
                    // Means the message is sent from child thread.
                    if(msg.what == MESSAGE_UPDATE_TEXT_CHILD_THREAD)
                    {
                        // Update ui in main thread.
                        updateText();
                    }
                }
            };
        }
    }

    /* Update ui text.*/
    private void updateText()
    {
       //String userInputText = changeTextEditor.getText().toString();
        tvLiveFreq.setText(String.valueOf(liveFrequency));
        plot_fft.redraw();
        Log.d(TAG,"plot redrawn");
    }









//    private void display() {
//        final Handler handler = new Handler();
//        final int delay = 100;
//            handler.postDelayed(new Runnable() {
//                public void run() {
//                    signalView.sndAudioBuf(MainActivity.BUFFER, MainActivity.BUFFER_READ_RESULT);
//                    handler.postDelayed(this, delay);
//                }
//            }, delay);
//    }

}