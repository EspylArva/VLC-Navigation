package com.vlcnavigation.ui.fft;


import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.lifecycle.MutableLiveData;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.vlcnavigation.MainActivity;
import com.vlcnavigation.module.audiorecord.AudioRecorder;
import com.vlcnavigation.module.audiorecord.SignalView;

import org.apache.commons.lang3.ArrayUtils;
import org.jtransforms.fft.DoubleFFT_1D;

import java.math.BigDecimal;
import java.util.Arrays;

import edu.princeton.cs.algorithms.Complex;
import edu.princeton.cs.algorithms.FFT;
import timber.log.Timber;

public class FFTComputing extends Thread {

    //views
    private FFTViewModel FFTViewModel;
    private SignalView signalView;
    private ToggleButton tB;
    private Button btnAnalyse;
    private EditText edtName,edtOffset,edtSampleRate;
    protected TextView tvWavFreq;
    protected TextView tvLiveFreq, tvLiveFreq2, tvLiveFreq3;
    protected TextView tvAmpl, tvAmpl2, tvAmpl3;
    protected TextView tvCurrentLED;
    protected String currentLED;


    //threads
    public static final int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
    public Handler updateUIHandler = null;
    protected Handler handlerThread = null;



    //plot
    private XYPlot plot_fft;
    protected int powOf2temp=(int)Math.pow(2,2);
    private String TAG = "FFT Fragment : ";


    //fft
    int buffersize = AudioRecorder.buffersize;

    protected short sData[] = new short[buffersize];
    protected double sDataAverage=0;
    protected double[] fftData = new double[buffersize];
    protected int[] oldSentData=new int[3];
    protected SimpleXYSeries series1;



    public double liveFrequency = 0;
    protected double wavFrequency = 0;
    // Fast Fourier Transform from JTransforms
    final DoubleFFT_1D fft = new DoubleFFT_1D(sData.length);

    //frequency computing
    double[] fftIndex = new double[buffersize];
    double[] fftPeaks = new double[buffersize];
    double[] fftFrequencies = new double[buffersize];
    double[] fftDistinctFrequencies = new double[buffersize];
    BigDecimal[] fftFrequenciesBigDecimal = new BigDecimal[buffersize];

    //distance computing
    private double[][] rssData;
    private double[][] distancesArray;





    //parameters
    protected double liveOffset = 21.428;
    protected double wavOffset = 0.07557265176877;
    static int sampleRate = AudioRecorder.FREQUENCY;


    //audio record
    //private AudioRecord audioInput = null;
    //private Thread recordingThread = null;
    public boolean isRecording;


    //wav file reader
    double[] absNormalizedSignal;
    int mPeakPos;






    public FFTComputing(Boolean bool) //, FFTFragment frag)
    {
        this.isRecording = bool;

//        this.fragment = frag;




    }







    @Override
    public void run()
    {
        /*
        int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        int RECORDER_SAMPLERATE= sampleRate;
        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        AudioRecord audioInput = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

*/





        // Start recording
        //audioInput.startRecording();
        //isRecording = true;




        while (isRecording) {
            //if (tB.isChecked()) {

            // Record audio input
            //audioInput.read(sData, 0, sData.length);



                        /* BKP Convert and put sData short array into fftData double array to perform FFT
                        for (int j = 0; j < sData.length; j++) {
                            fftData[j] = (double) sData[j];
                        }

                        */


            // Convert and put sData short array into fftData double array to perform FFT
            for (int j = 0; j < MainActivity.BUFFER.length; j++) {
                fftData[j] = (double) MainActivity.BUFFER[j];
            }

            // Perform 1D fft
            fft.realForward(fftData);
            //Timber.d("fftData before = "+Arrays.toString(fftData));

            //convert abs values
            for (int j = 0; j < fftData.length; j++) fftData[j] = Math.abs(fftData[j]);
            //Timber.d("fftData = "+Arrays.toString(fftData));

            //Timber.d("fftData = "+Arrays.toString(fftData));

            //Frequency calculation
            FFTComputing.this.liveFrequency = calculateFrequency(fftData, liveOffset, false);
            //Timber.d("Frequency : %s", liveFrequency);

                        /*

                        firstBigDecimal.compareTo(secondBigDecimal) < 0 // "<"
                        firstBigDecimal.compareTo(secondBigDecimal) > 0 // ">"
                        firstBigDecimal.compareTo(secondBigDecimal) == 0 // "=="
                        firstBigDecimal.compareTo(secondBigDecimal) >= 0 // ">="

                        */

            //finding First LED name
            if (fftFrequenciesBigDecimal[0].compareTo(new BigDecimal("1100.00")) > 0) {
                if (fftFrequenciesBigDecimal[0].compareTo(new BigDecimal("1300.00")) < 0) {
                    currentLED = "LED 1200 Hz";

                }
            } else if (fftFrequenciesBigDecimal[0].compareTo(new BigDecimal("900.00")) > 0) {
                if (fftFrequenciesBigDecimal[0].compareTo(new BigDecimal("1100.00")) < 0) {
                    currentLED = "LED 1000 Hz";
                }
            } else {
                currentLED = "None";
            }


            Bundle bundle = new Bundle();
            bundle.putString("edttext", "From Activity");
            // set Fragmentclass Arguments
            FFTFragment fftFragment = new FFTFragment();
            fftFragment.setArguments(bundle);





            //update GUI
            Message freqMsg = new Message();
            freqMsg.what = MESSAGE_UPDATE_TEXT_CHILD_THREAD;


            updateUIHandler.sendMessage(freqMsg);




        }
        Timber.d("Stopped computing");
        //this.isRecording = false;
        //audioInput.stop();
        //audioInput.release();

    }

    public double getLiveFrequency() {
        return liveFrequency;
    }



    public double calculateFrequency(double[] fft,double offset,boolean live)
    {
        System.out.println("_______________________Frequency computing________________________________________");
        System.out.println("fft brut = "+ Arrays.toString(fft));

        double[] fftSorted = fft.clone();
        //tri ordre croissant
        Arrays.sort(fftSorted);
        System.out.println("ordre croissant fftSorted = "+ Arrays.toString(fftSorted));

        ArrayUtils.reverse(fftSorted);

        //affichage ordre décroissant
        System.out.println("ordre décroissant fftSorted = "+ Arrays.toString(fftSorted));


        //tri ordre croissant
        ArrayUtils.reverse(fftSorted);
        System.out.println("ordre croissant fftSorted = "+ Arrays.toString(fftSorted));




        double peak1 = fftSorted[fftSorted.length-1];
        //Timber.d("Peak 1 = "+peak1);
        double peak2 = fftSorted[fftSorted.length-2];
        //Timber.d("Peak 2 = "+peak2);
        double peak3 = fftSorted[fftSorted.length-3];
        //Timber.d("Peak 2 = "+peak2);
        double peak4 = fftSorted[fftSorted.length-4];
        //Timber.d("Peak 2 = "+peak2);
        double peak5 = fftSorted[fftSorted.length-5];
        //Timber.d("Peak 2 = "+peak2);



        //saving found frequencies as arrays to filter them
        for(int i=1; i<fftSorted.length; i++) {
            for (int j = 0; j < fft.length; j++) {
                double peak = fftSorted[fftSorted.length-i];
                if (fft[j] == peak) {
                    fftPeaks[i-1] = peak;
                    fftIndex[i-1] = j;
                    fftFrequencies[i-1] = (((j) * sampleRate) / fft.length)/2;
                }
            }
        }

        System.out.println("fftPeaks = "+ Arrays.toString(fftPeaks));
        System.out.println("fftIndex = "+ Arrays.toString(fftIndex));
        System.out.println("fftFrequencies = "+ Arrays.toString(fftFrequencies));





        //filtering frequencies (to find distinct frequencies)


        //convert BigDecimal value to compare values

        for (int j = 0; j < fftFrequencies.length; j++){
            double x = fftFrequencies[j];
            //Timber.d("fftDataWav[j] = "+x+" | ");
            //Timber.d("Math.abs(fftDataWav[j]) = "+Math.abs(x)+" | ");
            fftFrequenciesBigDecimal[j] = BigDecimal.valueOf(x);
        }

        System.out.println("fftFrequenciesBigDecimal = "+ Arrays.toString(fftFrequenciesBigDecimal));

/*
        for(int i=0; i<fftFrequenciesBigDecimal.length; i++) {
            if (fftFrequenciesBigDecimal[i].subtract(fftFrequenciesBigDecimal[i+1]) < new BigDecimal("0.0") ){

            }
        }
        */


        /*
        for(int i=0; i<fftFrequencies.length; i++) {

            if (Double.compare(fftFrequencies[i], fftFrequencies[i+1]) == 0) {

                System.out.println("d1=d2");
            }
            else if (Double.compare(fftFrequencies[i], fftFrequencies[i+1]) < 0) {

                System.out.println("d1<d2");
            }
            else {

                System.out.println("d1>d2");
            }

        }

         */




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
        System.out.println("Method 1 :");
        System.out.println("Peak 1 = "+peak1+ " | Index 1 = "+index1+"/"+fft.length+" | 1st frequency = "+(((index1) * sampleRate) / fft.length)/2);
        System.out.println("Peak 2 = "+peak2+ " | Index 2 = "+index2+"/"+fft.length+" | 2nd frequency = "+(((index2) * sampleRate) / fft.length)/2);
        System.out.println("Peak 3 = "+peak3+ " | Index 3 = "+index3+"/"+fft.length+" | 3rd frequency = "+(((index3) * sampleRate) / fft.length)/2);
        System.out.println("Peak 4 = "+peak4+ " | Index 4 = "+index4+"/"+fft.length+" | 4th frequency = "+(((index4) * sampleRate) / fft.length)/2);
        System.out.println("Peak 5 = "+peak5+ " | Index 5 = "+index5+"/"+fft.length+" | 5th frequency = "+(((index5) * sampleRate) / fft.length)/2);


        /*
        System.out.println("Method 2 :");
        System.out.println("Peak 1 = "+peak1+ " | Index 1 = "+index1+"/"+fft.length+" | 1st frequency = "+((index1+index2)/2)*offset);
        System.out.println("Peak 2 = "+peak2+ " | Index 2 = "+index2+"/"+fft.length+" | 2nd frequency = "+((index1+index2)/2)*offset);
        System.out.println("Peak 3 = "+peak3+ " | Index 3 = "+index3+"/"+fft.length+" | 3rd frequency = "+((index1+index2)/2)*offset);
        System.out.println("Peak 4 = "+peak4+ " | Index 4 = "+index4+"/"+fft.length+" | 4th frequency = "+((index1+index2)/2)*offset);
        System.out.println("Peak 5 = "+peak5+ " | Index 5 = "+index5+"/"+fft.length+" | 5th frequency = "+((index1+index2)/2)*offset);

         */



        if (live){
            if (offset != 0) {
                return ((index1+index2)/2)*offset;
            }
            else{
                return ((index1+index2)/2)*1;
            }
        }
        else {


            return fftFrequencies[0] ;

        }




    }

    }








