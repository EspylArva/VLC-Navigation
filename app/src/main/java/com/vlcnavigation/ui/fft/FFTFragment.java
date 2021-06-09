package com.vlcnavigation.ui.fft;

import android.annotation.SuppressLint;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

//import com.android.ide.common.vectordrawable.Svg2Vector;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.vlcnavigation.MainActivity;
import com.vlcnavigation.R;
import com.vlcnavigation.module.audiorecord.AudioRecorder;
import com.vlcnavigation.module.audiorecord.SignalView;

import org.apache.commons.lang3.ArrayUtils;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.OptionalDouble;

import timber.log.Timber;

import static com.vlcnavigation.MainActivity.fftBoolCompute;
import static com.vlcnavigation.MainActivity.fftComputing;


public class FFTFragment extends Fragment {


    //views
    private FFTViewModel FFTViewModel;
    private SignalView signalView;
    private ToggleButton tB;
    private Button btnAnalyse;
    private EditText edtName, edtDisplayDelay,edtSampleRate;
    protected TextView tvWavFreq;
    protected TextView tvLiveFreq, tvLiveFreq2, tvLiveFreq3;
    protected TextView tvAmpl, tvAmpl2, tvAmpl3;
    protected TextView tvCurrentLED;
    protected String currentLED;




    //threads
    private static final int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
    protected Handler updateUIHandler = null;
    protected Handler handlerThread = new Handler();
    final Handler handlerLoopDelay = new Handler();
    //thread delay
    final Handler handlerThreadDelay = new Handler();




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
    protected double liveFrequency = 0;
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
    protected int wavSampleRate = 44100;

    //delays parameters
    public static double displayDelay;

    //average
    public static double[] firstFreqAverage = new double[AudioRecorder.buffersize];
    public static double firstFreqAverageValue = 0.0;


    //audio record
    private AudioRecord audioInput = null;
    private Thread fftDebugThread = null;
    public static boolean isRecording = false;


    //wav file reader
    double[] absNormalizedSignal;
    int mPeakPos;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MainActivity.fftViewModel = new ViewModelProvider(this).get(FFTViewModel.class);

        View root = initViews(inflater, container);
        initObservers();
        initListeners();



        tvLiveFreq = root.findViewById(R.id.tv_livefreq);
        MainActivity.fftViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                Timber.d("Text set for tvLiveFreq : %s",s);
                tvLiveFreq.setText(s);
            }
        });






        //initialize the fft_plot
        initPlot();



        //start the wav file audio FFT (results in an array, printed in the terminal)
        btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sampleRate = edtSampleRate.getText().toString();

                if (sampleRate.matches("")) {

                    Timber.d(TAG,wavSampleRate);

                }
                else {
                    wavSampleRate = (int) Double.parseDouble(edtSampleRate.getText().toString());
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

        //instanciate ui updater handler
        createUpdateUiHandler();


        //listener thread debug fft to update ui
        liveAudioFFT();




        return root;
    }

    private void initObservers() {
    }

    private void initListeners() {



    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {

        Timber.d("Views initialized");
        View root = inflater.inflate(R.layout.fragment_fft, container, false);

        //signal views
        signalView = root.findViewById(R.id.signalview);
        plot_fft = root.findViewById(R.id.plot_fft);


        //buttons
        btnAnalyse = root.findViewById(R.id.btn_analyse);
       // tB = root.findViewById(R.id.toggleButton2);

        //textviews
        tvWavFreq = root.findViewById(R.id.tv_wavfreq);
        tvLiveFreq = root.findViewById(R.id.tv_livefreq);
        tvLiveFreq2 = root.findViewById(R.id.tv_livefreq2);
        tvLiveFreq3 = root.findViewById(R.id.tv_livefreq3);
        tvAmpl = root.findViewById(R.id.tv_ampl);
        tvAmpl2 = root.findViewById(R.id.tv_ampl2);
        tvAmpl3 = root.findViewById(R.id.tv_ampl3);
        tvCurrentLED = root.findViewById(R.id.tv_currentled);


        //edit texts
        edtName = root.findViewById(R.id.edt_name);
        edtDisplayDelay = root.findViewById(R.id.edt_displaydelay);
        edtSampleRate= root.findViewById(R.id.edt_samplerate);




        /*
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 5s = 5000ms
                fftBoolCompute = true;
                fftComputing = new FFTComputing(fftBoolCompute);
                fftComputing.start();






            }
        }, 500);

         */



        return root;
    }



    private void initPlot(){


        // Create a couple arrays of y-values to plot:
        final Number[] series1Numbers = new Number[buffersize/powOf2temp];

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







    public void liveAudioFFT(){




        fftDebugThread = new Thread(new Runnable() {

            public void run() {



                while (true) {
                    while (fftBoolCompute) {
                        //if (tB.isChecked()) {

                            // Record audio input
                            //audioInput.read(sData, 0, sData.length);

                            updateDisplayDelay();


                            //setting up average array
                            int freqAverageSize = (Double.valueOf(displayDelay).intValue()/Double.valueOf(fftComputing.computeDelay).intValue());
                            //Timber.d("Average array size = %s",freqAverageSize);
                            firstFreqAverage = new double[freqAverageSize];
                            fftComputing.loopCount = 0;
                            fftComputing.captureValue = true;








                            //DISPLAY PERIOD
                            long futureTime = System.currentTimeMillis()+Double.valueOf(displayDelay).longValue();
                            while(System.currentTimeMillis() < futureTime){
                                synchronized (this){
                                    try{
                                        wait(futureTime-System.currentTimeMillis());
                                    }catch (Exception e){}
                                }
                            }






                            //Cleaning average array :


                            int targetIndex = 0;
                            for( int sourceIndex = 0;  sourceIndex < firstFreqAverage.length;  sourceIndex++ )
                            {
                                if( firstFreqAverage[sourceIndex] != 0.0 )
                                    firstFreqAverage[targetIndex++] = firstFreqAverage[sourceIndex];
                            }
                            double[] firstFreqAverageClean = new double[targetIndex];
                            System.arraycopy( firstFreqAverage, 0, firstFreqAverageClean, 0, targetIndex );




                            if (firstFreqAverageClean.length != 0) {
                                //Timber.d("Freq average values : %s",Arrays.toString(firstFreqAverage));
                                //Timber.d("Freq average values CLEAN : %s",Arrays.toString(firstFreqAverageClean));
                                firstFreqAverageValue = Arrays.stream(firstFreqAverageClean).average().getAsDouble();

                                //double firstFreqAverageValue = firstFreqAverageValueOptional

                                Timber.d("First freq average value is : %s",String.valueOf(firstFreqAverageValue));

                                //update GUI
                                //updateTextViews(fftComputing.liveFrequency, fftComputing.fftFrequencies, fftComputing.fftPeaks, fftComputing.currentLED);
                                updateTextViews(firstFreqAverageValue, fftComputing.fftFrequencies, fftComputing.fftPeaks, fftComputing.currentLED);

                            }
                            else {
                                Timber.d("No average value.");
                            }






                        //}
                        //fftComputing.captureValue = false;
                    }
                    fftComputing.captureValue = false;

                    //WAITING FOR BUTTON PERIOD
                    long futureTime = System.currentTimeMillis()+500;
                    while(System.currentTimeMillis() < futureTime){
                        synchronized (this){
                            try{
                                wait(futureTime-System.currentTimeMillis());
                            }catch (Exception e){}
                        }
                    }


                }
            }



            //FFTFragment.this.handlerThread



        }, "AudioRecorder Thread");
        //handlerThread.postDelayed(recordingThread,500);
        //handlerThread.postDelayed(fftDebugThread,500);
        fftDebugThread.start();

        ////////////////////////////////////////////////////////////////////////////////////////////

    }

    private void wavAudioFFT(String filename){


        Toast.makeText(getActivity(), "Using "+ wavSampleRate+"Hz as sample rate.", Toast.LENGTH_SHORT).show();
        File file = null;
        file = new File(Environment.getExternalStorageDirectory()+"/"+"Download/"+filename+".wav");
        if (!file.exists()) {
            Toast.makeText(getActivity(), "This file does not exist.", Toast.LENGTH_SHORT).show();
            Timber.e("No such file or directory");
            return;
        }
        byte[] byteData = new byte[(int) file.length()];
        Timber.d("Using "+file.getName()+" ___________________________________________________");
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            in.close(); }
        catch (Throwable throwable){
            throwable.printStackTrace();
        }
        System.out.println( "Wav audio file byteData"+ Arrays.toString(byteData));


        try{
            //creation of the audio array
            double[] fftDataWav = new double[byteData.length];
            double[] absFftDataWav = new double[byteData.length];


            // Convert and put sData short array into fftDataWav double array to perform FFT
            for (int j = 0; j < byteData.length; j++) {
                fftDataWav[j] = (double) byteData[j];
            }

            System.out.println( "Wav audio file fftDataWav double converted"+ Arrays.toString(fftDataWav));


            // Fast Fourier Transform from JTransforms
            final DoubleFFT_1D fftWav = new DoubleFFT_1D(fftDataWav.length);
            // Perform 1D fft
            fftWav.realForward(fftDataWav);
            System.out.println( "Wav audio file fftDataWav after FFT = "+ Arrays.toString(fftDataWav));

            //convert abs values
            for (int j = 0; j < fftDataWav.length; j++){
                double x = fftDataWav[j];
                //Timber.d("fftDataWav[j] = "+x+" | ");
                //Timber.d("Math.abs(fftDataWav[j]) = "+Math.abs(x)+" | ");
                absFftDataWav[j] = Math.abs(x);
            }
            System.out.println("absFftDataWav = "+ Arrays.toString(absFftDataWav));

            wavFrequency = calculateFrequency(absFftDataWav,wavOffset,false);

            tvWavFreq.setText(String.valueOf(wavFrequency));

            Timber.d("Wav Audio File Frequency : %s", wavFrequency);


        /* OLD method
        absNormalizedSignal = calculateFFT(byteData);



        //Timber.d("Data FFT absNormalizedSignal : "+Arrays.toString(absNormalizedSignal));
        //Timber.d("Data FFT peak : "+mPeakPos);
        */
        }
        catch (Error e){
            Timber.d(e);
        }



    }

    public double calculateFrequency(double[] fft,double offset,boolean live) {
        System.out.println("_______________________Frequency computing________________________________________");
        System.out.println("fft brut = " + Arrays.toString(fft));

        double[] fftSorted = fft.clone();
        //tri ordre croissant
        Arrays.sort(fftSorted);
        System.out.println("ordre croissant fftSorted = " + Arrays.toString(fftSorted));

        ArrayUtils.reverse(fftSorted);

        //affichage ordre décroissant
        System.out.println("ordre décroissant fftSorted = " + Arrays.toString(fftSorted));


        //tri ordre croissant
        ArrayUtils.reverse(fftSorted);
        System.out.println("ordre croissant fftSorted = " + Arrays.toString(fftSorted));


        if (live) {

            //saving found frequencies as arrays to filter them
            for (int i = 1; i < fftSorted.length; i++) {
                for (int j = 0; j < fft.length; j++) {
                    double peak = fftSorted[fftSorted.length - i];
                    if (fft[j] == peak) {
                        fftPeaks[i - 1] = peak;
                        fftIndex[i - 1] = j;
                        fftFrequencies[i - 1] = (((j) * sampleRate) / fft.length) / 2;
                    }
                }
            }

            System.out.println("fftPeaks = " + Arrays.toString(fftPeaks));
            System.out.println("fftIndex = " + Arrays.toString(fftIndex));
            System.out.println("fftFrequencies = " + Arrays.toString(fftFrequencies));


            //filtering frequencies (to find distinct frequencies)


            //convert BigDecimal value to compare values

            for (int j = 0; j < fftFrequencies.length; j++) {
                double x = fftFrequencies[j];
                //Timber.d("fftDataWav[j] = "+x+" | ");
                //Timber.d("Math.abs(fftDataWav[j]) = "+Math.abs(x)+" | ");
                fftFrequenciesBigDecimal[j] = BigDecimal.valueOf(x);
            }

            System.out.println("fftFrequenciesBigDecimal = " + Arrays.toString(fftFrequenciesBigDecimal));


            System.out.println("Method 1 :");
            System.out.println("Peak 1 = " + fftPeaks[0] + " | Index 1 = " + fftIndex[0] + "/" + fft.length + " | 1st frequency = " + fftFrequencies[0]);
            System.out.println("Peak 2 = " + fftPeaks[1] + " | Index 2 = " + fftIndex[1] + "/" + fft.length + " | 2nd frequency = " + fftFrequencies[1]);
            System.out.println("Peak 3 = " + fftPeaks[2] + " | Index 3 = " + fftIndex[2] + "/" + fft.length + " | 3rd frequency = " + fftFrequencies[2]);
            System.out.println("Peak 4 = " + fftPeaks[3] + " | Index 4 = " + fftIndex[3] + "/" + fft.length + " | 4th frequency = " + fftFrequencies[3]);
            System.out.println("Peak 5 = " + fftPeaks[4] + " | Index 5 = " + fftIndex[4] + "/" + fft.length + " | 5th frequency = " + fftFrequencies[4]);


            return fftFrequencies[0];
        } else {

            Timber.d("Live fft method = false");

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
            System.out.println("Peak 1 = "+peak1+ " | Index 1 = "+index1+"/"+fft.length+" | 1st frequency = "+(((index1) * wavSampleRate) / fft.length));
            System.out.println("Peak 2 = "+peak2+ " | Index 2 = "+index2+"/"+fft.length+" | 2nd frequency = "+(((index2) * wavSampleRate) / fft.length));
            System.out.println("Peak 3 = "+peak3+ " | Index 3 = "+index3+"/"+fft.length+" | 3rd frequency = "+(((index3) * wavSampleRate) / fft.length));
            System.out.println("Peak 4 = "+peak4+ " | Index 4 = "+index4+"/"+fft.length+" | 4th frequency = "+(((index4) * wavSampleRate) / fft.length));
            System.out.println("Peak 5 = "+peak5+ " | Index 5 = "+index5+"/"+fft.length+" | 5th frequency = "+(((index5) * wavSampleRate) / fft.length));




            /* //USING SAME METHOD AS LIVE (arrays)

            double[] fftWavIndex = new double[fft.length];
            double[] fftWavPeaks = new double[fft.length];
            double[] fftWavFrequencies = new double[fft.length];

            //saving found frequencies as arrays to filter them
            for(int i=1; i<fft.length; i++) {
                for (int j = 0; j < fft.length; j++) {
                    double peak = fftSorted[fftSorted.length-i];
                    if (fft[j] == peak) {
                        fftWavPeaks[i-1] = peak;
                        fftWavIndex[i-1] = j;
                        fftWavFrequencies[i-1] = (((j) * wavSampleRate) / fft.length)/2;
                    }
                }
            }

            /*
            System.out.println("fftPeaks = "+ Arrays.toString(fftWavPeaks));
            System.out.println("fftIndex = "+ Arrays.toString(fftWavIndex));
            System.out.println("fftFrequencies = "+ Arrays.toString(fftWavFrequencies));

             */

            /*




            System.out.println("Method 1 :");
            System.out.println("Peak 1 = "+fftWavPeaks[0]+ " | Index 1 = "+fftWavIndex[0]+"/"+fft.length+" | 1st frequency = "+fftWavFrequencies[0]);
            System.out.println("Peak 2 = "+fftWavPeaks[1]+ " | Index 2 = "+fftWavIndex[1]+"/"+fft.length+" | 2nd frequency = "+fftWavFrequencies[1]);
            System.out.println("Peak 3 = "+fftWavPeaks[2]+ " | Index 3 = "+fftWavIndex[2]+"/"+fft.length+" | 3rd frequency = "+fftWavFrequencies[2]);
            System.out.println("Peak 4 = "+fftWavPeaks[3]+ " | Index 4 = "+fftWavIndex[3]+"/"+fft.length+" | 4th frequency = "+fftWavFrequencies[3]);
            System.out.println("Peak 5 = "+fftWavPeaks[4]+ " | Index 5 = "+fftWavIndex[4]+"/"+fft.length+" | 5th frequency = "+fftWavFrequencies[4]);


            return fftWavFrequencies[0] ;

            */

            return (((index1) * wavSampleRate) / fft.length);

        }





    }


    public double[] calculateFFT(byte[] signal)
    {
        final int mNumberOfFFTPoints =buffersize;
        double mMaxFFTSample;

        double temp;
        Complex[] y;
        Complex[] complexSignal = new Complex[mNumberOfFFTPoints];
        double[] absSignal = new double[mNumberOfFFTPoints/2];

        for(int i = 0; i < mNumberOfFFTPoints; i++){
            temp = (double)((signal[2*i] & 0xFF) | (signal[2*i+1] << 8)) / 32768.0F;
            complexSignal[i] = new Complex(temp,0.0);
        }

        Timber.d("Complex Signal array : %s", Arrays.toString(complexSignal));

        y = FFT.fft(complexSignal); // --> Here I use FFT class
        Timber.d("Complex Signal array after FFT : %s", Arrays.toString(y));

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






    /* Update ui text.*/
    public void updateText()
    {
       //String userInputText = changeTextEditor.getText().toString();
        tvLiveFreq.setText(String.valueOf(liveFrequency));
        tvLiveFreq2.setText(String.valueOf(fftFrequencies[1]));
        tvLiveFreq3.setText(String.valueOf(fftFrequencies[2]));

        tvAmpl.setText(String.valueOf(fftPeaks[0]));
        tvAmpl2.setText(String.valueOf(fftPeaks[1]));
        tvAmpl3.setText(String.valueOf(fftPeaks[2]));

        tvCurrentLED.setText(currentLED);

        plot_fft.redraw();
        //Timber.d("plot redrawn");
    }


    /* Update ui text.*/
    public void updateTextViews(double liveFrequency, double[] fftFrequencies, double[] fftPeaks, String currentLED)
    {
        //String userInputText = changeTextEditor.getText().toString();
        tvLiveFreq.setText(String.valueOf(liveFrequency));
        tvLiveFreq2.setText(String.valueOf(fftFrequencies[1]));
        tvLiveFreq3.setText(String.valueOf(fftFrequencies[2]));

        tvAmpl.setText(String.valueOf(fftPeaks[0]));
        tvAmpl2.setText(String.valueOf(fftPeaks[1]));
        tvAmpl3.setText(String.valueOf(fftPeaks[2]));

        tvCurrentLED.setText(currentLED);

        plot_fft.redraw();
        //Timber.d("plot redrawn");
    }




    public void updateDisplayDelay(){
        //changing the display delay

        String dispDelay = edtDisplayDelay.getText().toString();

        if (dispDelay.matches("")) {
            displayDelay = 500;
            Timber.d("Using %s as display delay",displayDelay);

        }
        else if (Integer.parseInt(dispDelay) > fftComputing.computeDelay){
            displayDelay = Double.parseDouble(edtDisplayDelay.getText().toString());
        }
        else {
            displayDelay = 500;
        }
    }




    /*
    public static double getLiveFrequency(){

        createUpdateUiHandler();
        liveAudioFFT();

        return liveFrequency;
    }

     */



    public void distanceComputing() {

        distancesArray = new double[5][4];

        double m, P, Ts, H, A, Dx1, Dx2, Dx3, Dx4;
        A = 1;
        m = 1;
        H = 1;

        for(int i=0; i<rssData.length; i++)
        {
            for(int j=0; j<rssData[i].length; j++)
            {
                System.out.println("-------Distance--------");
                distancesArray[i][j] = Math.pow( rssData[i][j] * ((A * (m+1) * Math.pow(H,m+1)) / 2 * Math.PI) , 1 / m+3 );
                System.out.println(distancesArray[i][j]);
            }
        }

        //Dx1 = Math.pow( Tx1 * ((A * (m+1) * Math.pow(H,m+1)) / 2 * Math.PI) , 1 / m+3 );



        System.out.println("-------LISTE Distances--------\n\n");
        System.out.println(Arrays.deepToString(distancesArray));


    }

    /* Create Handler object in main thread. */
    @SuppressLint("HandlerLeak")
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
                        //FFTFragment.updateText();
                    }
                }
            };



        }
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