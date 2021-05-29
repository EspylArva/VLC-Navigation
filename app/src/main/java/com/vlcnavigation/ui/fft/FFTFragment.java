package com.vlcnavigation.ui.fft;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
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

import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;


public class FFTFragment extends Fragment {

    private FFTViewModel FFTViewModel;
    private SignalView signalView;
    private ToggleButton tB;
    protected SeekBar sensBar;
    protected SeekBar sensBarSmooth;
    protected TextView tvFreq;


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
    protected double offset = 21.428;
    protected double frequency = 0;

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
        //wavAudioFFT();




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
        tvFreq = root.findViewById(R.id.tvFreq);

        signalView.sndAudioBuf(MainActivity.BUFFER, MainActivity.BUFFER_READ_RESULT);



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

                //update textview
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // This code will always run on the UI thread, therefore is safe to modify UI elements.
                        tvFreq.setText(String.valueOf(frequency));
                    }
                });


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
                        frequency = calculateFrequency(fftData);
                        System.out.println("Frequency : "+ frequency);





                        // Update plot //
                        for (int j = 0; j < series1.size(); j++) {
                            series1.removeFirst();
                            series1.addLast(null, fftData[j * powOf2temp] * powOf2temp);
                        }
                        int sensBarProgress = sensBar.getProgress();
                        plot_fft.setRangeBoundaries(0, (100-((sensBarProgress==100)?99:sensBarProgress)) * 1000, BoundaryMode.FIXED);
                        plot_fft.redraw();
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

    private void wavAudioFFT(){
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

    public double calculateFrequency(double[] fft)
    {
        double[] fftSorted = fft.clone();
        Arrays.sort(fftSorted);
        double peak1 = fftSorted[fftSorted.length-1];
        //System.out.println("Peak 1 = "+peak1);
        double peak2 = fftSorted[fftSorted.length-2];
        //System.out.println("Peak 2 = "+peak2);


        int index1 = 0; int index2 = 0;
        for(int i=0; i<fft.length; i++) {
            if(fft[i] == peak1) {
                index1 = i;
            }
            else if(fft[i] == peak2) {
                index2 = i;
            }
        }
        //System.out.println("Index 1 = "+index1);
        //System.out.println("Index 2 = "+index2);

        return ((index1+index2)/2)*offset;
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