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
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.util.Arrays;

import edu.princeton.cs.algorithms.Complex;
import edu.princeton.cs.algorithms.FFT;
import timber.log.Timber;

import static com.vlcnavigation.MainActivity.fftComputing;
import static com.vlcnavigation.ui.fft.FFTFragment.displayDelay;
import static com.vlcnavigation.ui.fft.FFTFragment.firstFreqAverage;

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
    public String currentLED;


    //threads
    public static final int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
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

    //computing period delay
    double computeDelay = 50;
    public int loopCount = 0;
    public Boolean captureValue = false;


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

    public FFTComputing(Boolean bool, TextView tvLiveFreq){

        this.isRecording = bool;


    }







    @Override
    public void run()
    {








        while (isRecording) {




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







            FFTComputing.this.liveFrequency = calculateFrequency(fftData, liveOffset, true);
            //Timber.d("Frequency : %s", liveFrequency);


            if (captureValue) {
                firstFreqAverage[loopCount] = liveFrequency;
                loopCount += 1;
            }





                        /*

                        firstBigDecimal.compareTo(secondBigDecimal) < 0 // "<"
                        firstBigDecimal.compareTo(secondBigDecimal) > 0 // ">"
                        firstBigDecimal.compareTo(secondBigDecimal) == 0 // "=="
                        firstBigDecimal.compareTo(secondBigDecimal) >= 0 // ">="

                        */

            /*
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

            */




            ///WAIT
            //COMPUTING PERIOD
            long futureTime = System.currentTimeMillis()+Double.valueOf(computeDelay).longValue();
            while(System.currentTimeMillis() < futureTime){
                synchronized (this){
                    try{
                        wait(futureTime-System.currentTimeMillis());
                    }catch (Exception e){}
                }
            }




        }
        Timber.d("Stopped computing");
        //this.isRecording = false;
        //audioInput.stop();
        //audioInput.release();

    }

    public double getLiveFrequency() {
        return liveFrequency;
    }

    public void setCurrentLED(String ledDescription){
        this.currentLED = ledDescription;
    }

    public void setIsRecording(Boolean bool){
        this.isRecording = bool;
    }



    public double calculateFrequency(double[] fft,double offset,boolean live)
    {
        //System.out.println("_______________________Frequency computing________________________________________");
        //System.out.println("fft brut = "+ Arrays.toString(fft));

        double[] fftSorted = fft.clone();
        //tri ordre croissant
        Arrays.sort(fftSorted);
        //System.out.println("ordre croissant fftSorted = "+ Arrays.toString(fftSorted));

        ArrayUtils.reverse(fftSorted);

        //affichage ordre décroissant
        //System.out.println("ordre décroissant fftSorted = "+ Arrays.toString(fftSorted));


        //tri ordre croissant
        ArrayUtils.reverse(fftSorted);
        //System.out.println("ordre croissant fftSorted = "+ Arrays.toString(fftSorted));





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

        //System.out.println("fftPeaks = "+ Arrays.toString(fftPeaks));
        //System.out.println("fftIndex = "+ Arrays.toString(fftIndex));
        //System.out.println("fftFrequencies = "+ Arrays.toString(fftFrequencies));





        //filtering frequencies (to find distinct frequencies)


        //convert BigDecimal value to compare values

        for (int j = 0; j < fftFrequencies.length; j++){
            double x = fftFrequencies[j];
            //Timber.d("fftDataWav[j] = "+x+" | ");
            //Timber.d("Math.abs(fftDataWav[j]) = "+Math.abs(x)+" | ");
            fftFrequenciesBigDecimal[j] = BigDecimal.valueOf(x);
        }

        //System.out.println("fftFrequenciesBigDecimal = "+ Arrays.toString(fftFrequenciesBigDecimal));




        if (live){
            return fftFrequencies[0];
        }
        else {


            return fftFrequencies[0] ;

        }




    }

    }








