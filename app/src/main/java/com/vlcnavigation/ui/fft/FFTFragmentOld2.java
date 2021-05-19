package com.vlcnavigation.ui.fft;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.vlcnavigation.R;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import ca.uol.aig.fftpack.RealDoubleFFT;

//import com.android.ide.common.vectordrawable.Svg2Vector;
//csv reading
//FFTPack imports


public class FFTFragmentOld2 extends Fragment {

    private FFTViewModel FFTViewModel;
    //private List<String> rssData = new ArrayList<String>();
    private double[][] rssData;
    private double[][] distancesArray;

    //FFTPack
    int frequency = 8000;

    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO; int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    //transformer will be our FFT object, and we'll be dealing with 256 samples at a time from the AudioRecord object through the FFT object. The number of samples we use will correspond to the number of component frequencies we will get after we run them through the FFT object. We are free to choose a different size, but we do need concern ourselves with memory and performance issues as the math required to the calculation is processor-intensive.

    private RealDoubleFFT transformer; int blockSize = 256;

    Button startStopButton; boolean started = false;

    //RecordAudio is an inner class defined here that extends AsyncTask.

    FFTFragmentOld2.RecordAudio recordTask;

    //We'll be using an ImageView to display a Bitmap image. This image will represent the levels of the various frequencies that are in the current audio stream. To draw these levels, we'll use Canvas and Paint objects constructed from the Bitmap.

    ImageView imageView; Bitmap bitmap; Canvas canvas; Paint paint;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        FFTViewModel =
                new ViewModelProvider(this).get(FFTViewModel.class);
        View root = inflater.inflate(R.layout.fragment_fft, container, false);

        //findviews
        final TextView textView = root.findViewById(R.id.textView3);
        startStopButton = (Button) root.findViewById(R.id.StartStopButton);
        imageView = (ImageView) root.findViewById(R.id.ImageView01);


        //FFTpack

        //The RealDoubleFFT class constructor takes in the number of samples that we'll deal with at a time. This also represents the number of distinct ranges of frequencies that will be output.

        transformer = new RealDoubleFFT(blockSize); //Here is the setup of the ImageView and related object for drawing.

        bitmap = Bitmap.createBitmap((int) 256, (int) 100, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);

        paint = new Paint();
        paint.setColor(Color.GREEN);

        imageView.setImageBitmap(bitmap);



        startStopButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (started) {

                    started = false;

                    startStopButton.setText("Start");
                    recordTask.cancel(true);
                } else {

                    started = true;

                    startStopButton.setText("Stop");
                    recordTask = new FFTFragmentOld2.RecordAudio();
                    recordTask.execute();

                }
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

    class RecordAudio extends AsyncTask<Void, double[], Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {

                //We'll set up and use AudioRecord in the normal manner.

                int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(

                        MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);

                //The short array, buffer, will take in the raw PCM samples from the AudioRecord object. The double array, toTransform, will hold the same data but in the form of doubles, as that is what the FFT class requires.

                short[] buffer = new short[blockSize];
                double[] toTransform = new double[blockSize];

                audioRecord.startRecording();

                while (started) {

                    Thread.sleep(100);
                    int bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                    //After we read the data from the AudioRecord object, we loop through and translate it from short values to double values. We can't do this directly by casting, as the values expected should be between -1.0 and 1.0 rather than the full range. Dividing the short by 32,768.0 will do that, as that value is the maximum value of short.

                    //NOTE: There is a constant Short.MAX VALUE that could be used instead.

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {

                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit

                        //Next we'll pass the array of double values to the FFT object. The FFT object re-uses the same array to hold the output values. The data contained will be in the frequency domain rather than the time domain. This means that the first element in the array will not represent the first sample in time—rather, it will represent the levels of the first set of frequencies.

                        //Since we are using 256 values (or ranges) and our sample rate is 8,000, we can determine that each element in the array will cover approximately 15.625 Hz. We come up with this figure by dividing the sample rate in half (as the highest frequency we can capture is half the sample rate) and then dividing by 256. Therefore the data represented in the first element of the array will represent the level of audio that is between 0 and 15.625 Hz.

                        transformer.ft(toTransform);

                        //Calling publishProgress calls onProgressUpdate.

                        publishProgress(toTransform);

                        //Log.e("AudioRecord", "Recording Failed");



                        //onProgressUpdate runs on the main thread in our activity and can therefore interact with the user interface without problems. In this implementation, we are passing in the data after it has been run through the FFT object. This method takes care of drawing the data on the screen as a series of lines at most 100 pixels tall. Each line represents one of the elements in the array and therefore a range of 15.625 Hz. The first line represents frequencies ranging from 0 to 15.625 Hz, and the last line represents frequencies ranging from 3,984.375 to 4,000 Hz. Figure 8-1 shows what this looks like in action.


                    }
                }
            } catch (Error | InterruptedException e) {

            }
            return null;
        }

        protected void onProgressUpdate ( double[]...toTransform){
            canvas.drawColor(Color.BLACK);

            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;

                int downy = (int) (100 - (toTransform[0][i] * 10));
                int upy = 100;

                canvas.drawLine(x, downy, x, upy, paint);

                imageView.invalidate();

            }



        }


    }

    //onProgressUpdate runs on the main thread in our activity and can therefore interact with the user interface without problems. In this implementation, we are passing in the data after it has been run through the FFT object. This method takes care of drawing the data on the screen as a series of lines at most 100 pixels tall. Each line represents one of the elements in the array and therefore a range of 15.625 Hz. The first line represents frequencies ranging from 0 to 15.625 Hz, and the last line represents frequencies ranging from 3,984.375 to 4,000 Hz. Figure 8-1 shows what this looks like in action.



    public void dataParser(){

        InputStream inputStream = getResources().openRawResource(R.raw.powerratio2);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        int i = 0;

        try (

                CSVParser csvParser = new CSVParser(br, CSVFormat.DEFAULT
                        .withHeader("Tx1", "Tx2", "Tx3", "Tx4")
                        .withIgnoreHeaderCase()
                        .withSkipHeaderRecord()
                        .withTrim());
        ) {

            System.out.println("record number : "+Math.toIntExact(csvParser.getRecordNumber()));
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


                rssData[i][0]=Tx1;
                rssData[i][1]=Tx2;
                rssData[i][2]=Tx3;
                rssData[i][3]=Tx4;
                i++;




            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println("-------LISTE RSS--------\n\n");
        System.out.println(Arrays.deepToString(rssData));

    }

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
}