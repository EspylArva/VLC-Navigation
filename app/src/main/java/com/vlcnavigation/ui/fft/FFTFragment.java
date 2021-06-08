package com.vlcnavigation.ui.fft;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

//import com.android.ide.common.vectordrawable.Svg2Vector;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PanZoom;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.vlcnavigation.MainActivity;
import com.vlcnavigation.R;
import com.vlcnavigation.module.audiorecord.AudioRecorder;
import com.vlcnavigation.module.audiorecord.SignalView;

import org.apache.commons.lang3.ArrayUtils;
import org.jtransforms.fft.DoubleFFT_1D;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Arrays;

import timber.log.Timber;


public class FFTFragment extends Fragment {


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
    private static final int MESSAGE_UPDATE_TEXT_CHILD_THREAD = 1;
    protected Handler updateUIHandler = null;
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


    //audio record
    private AudioRecord audioInput = null;
    private Thread recordingThread = null;
    public static boolean isRecording = false;


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


/*
        recordingThread = new Thread(new Runnable() {

            public void run() {



                while (isRecording) {
                    //if (tB.isChecked()) {

                    // Record audio input
                    audioInput.read(sData, 0, sData.length);






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
                    FFTFragment.this.liveFrequency = calculateFrequency(fftData, liveOffset, false);
                    //Timber.d("Frequency : %s", liveFrequency);



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


                    //update GUI
                    Message freqMsg = new Message();
                    freqMsg.what = MESSAGE_UPDATE_TEXT_CHILD_THREAD;


                    updateUIHandler.sendMessage(freqMsg);



                        /*


                        //Create a arrays of y-value to plot:
                        final Number[] domainLabels = {1,2,3,6,7,8,9,10,13,14};
                        //Number[] series1Numbers = {1,4,2,8,88,16,8,32,16,64};
                        final Number[] series1Numbers = new Number[fftData.length];
                        final Number[] series2Numbers = new Number[1024/powOf2temp];


                        //series2Numbers = ;

                        // Turn the above arrays into XYSeries
                        SimpleXYSeries series1 = new SimpleXYSeries(Arrays.asList(series1Numbers),
                                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series 1");
                        XYSeries series2 = new SimpleXYSeries(Arrays.asList(series2Numbers),
                                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,"Series 2");



                        //initilize the serie 1
                        for (int j=0;j<series1Numbers.length;j++) {
                            series1Numbers[j]=fftData[j];
                        }

                        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED,Color.GREEN,null,null);
                        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.YELLOW,Color.BLUE,null,null);

                        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10,
                                CatmullRomInterpolator.Type.Centripetal));
                        series2Format.setInterpolationParams(new CatmullRomInterpolator.Params(10,
                                CatmullRomInterpolator.Type.Centripetal));

                        plot_fft.addSeries(series1,series1Format);
                        plot_fft.addSeries(series2,series2Format);

                        // Update plot //
                        for (int j = 0; j < series1.size(); j++) {
                            series1.removeFirst();
                            //series1.addLast(null, fftData[j * powOf2temp] * powOf2temp);
                            series1.addLast(null, fftData[j]);
                        }

                        plot_fft.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                            @Override
                            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                                int i = Math.round( ((Number)obj).floatValue() );
                                return toAppendTo.append(domainLabels[i]);
                            }

                            @Override
                            public Object parseObject(String source, ParsePosition pos) {
                                return null;
                            }
                        });

                        PanZoom.attach(plot_fft);
                        */



                        /*

                        // Update plot //
                        for (int j = 0; j < series1.size(); j++) {
                            series1.removeFirst();
                            series1.addLast(null, fftData[j * powOf2temp] * powOf2temp);
                        }
                       // int sensBarProgress = sensBar.getProgress();
                        //plot_fft.setRangeBoundaries(0, (100-((sensBarProgress==100)?99:sensBarProgress)) * 1000, BoundaryMode.FIXED);
                        ////////////////

                        final int dataToSend[] = new int[3];
                       // int smoothness=sensBarSmooth.getProgress()+192;

                        for (int freqDomain = 0; freqDomain < 3; freqDomain++) {
                            sDataAverage = 0;
                            for (int i = freqDomain * buffersize * 2 / 9; i < (freqDomain + 1) * buffersize * 2 / 9; i++)
                                sDataAverage += fftData[i];
                          //  sDataAverage /= buffersize / (3 * (float)sensBarProgress/500);

                            // Limit the value to 255
                            dataToSend[freqDomain] = (sDataAverage > 255) ? 255 : (int) sDataAverage;
                            // Limit the amplitude fall
                           // dataToSend[freqDomain] =
                           //         (dataToSend[freqDomain]  < oldSentData[freqDomain]*(1-(float)(257-smoothness)/255)) ?
                            //                (int)(oldSentData[freqDomain]*(1-(float)(257-smoothness)/255)) :
                            //                dataToSend[freqDomain];

                            oldSentData[freqDomain] = dataToSend[freqDomain];
                        }
                        */

                        /*
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //TextView tv = (TextView) findViewById(R.id.textView_debug);
                                Log.d(TAG,"Average amplitudes : " + String.valueOf(Arrays.toString(dataToSend)));

                                //sendData("R" + dataToSend[0] + "G" + dataToSend[1] + "B" + dataToSend[2]);
                            }
                        });
                    //}
                }
            }



            //FFTFragment.this.handlerThread


        }, "AudioRecorder Thread"); */



        //start the thread to read microphone live audio fft and then plot
        //the trigger is the toggle button

        //liveAudioFFT();






        //start the wav file audio FFT (results in an array, printed in the terminal)
        btnAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String sampleRate = edtSampleRate.getText().toString();

                if (sampleRate.matches("")) {

                    Timber.d(TAG,FFTFragment.this.sampleRate);
                    FFTFragment.this.sampleRate = FFTFragment.sampleRate;
                }
                else {
                    FFTFragment.this.sampleRate = (int) Double.parseDouble(edtSampleRate.getText().toString());
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



    }

    private View initViews(LayoutInflater inflater, ViewGroup container) {
        View root = inflater.inflate(R.layout.fragment_fft, container, false);

        //signal views
        signalView = root.findViewById(R.id.signalview);
        plot_fft = root.findViewById(R.id.plot_fft);


        //buttons
        btnAnalyse = root.findViewById(R.id.btn_analyse);
        tB = root.findViewById(R.id.toggleButton2);

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
        edtOffset= root.findViewById(R.id.edt_liveoffset);
        edtSampleRate= root.findViewById(R.id.edt_samplerate);





        //signalView.sndAudioBuf(MainActivity.BUFFER, MainActivity.BUFFER_READ_RESULT);



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


        int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        int RECORDER_SAMPLERATE= sampleRate;
        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        audioInput = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);





        // Start recording
        audioInput.startRecording();
        isRecording = true;


        //handlerThread.postDelayed(recordingThread,500);
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
        Toast.makeText(getActivity(), "Using "+ sampleRate+"Hz as sample rate & "+this.liveOffset+" as offset.", Toast.LENGTH_SHORT).show();

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