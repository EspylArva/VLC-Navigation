package com.vlcnavigation.ui.fft;

import android.app.Activity;

import android.graphics.Bitmap;

import android.graphics.Canvas;

import android.graphics.Color;

import android.graphics.Paint;

import android.media.AudioFormat;

import android.media.AudioRecord;

import android.media.MediaRecorder;

import android.os.AsyncTask;

import android.os.Bundle;

import android.util.Log;

import android.view.View;

import android.view.View.OnClickListener;

import android.widget.Button;

import android.widget.ImageView;

import com.vlcnavigation.R;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class FFTPack extends Activity implements OnClickListener {

    //We'll use a frequency of 8 kHz, one audio channel, and 16 bit samples in the AudioRecord object.

    int frequency = 8000;

    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO; int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    //transformer will be our FFT object, and we'll be dealing with 256 samples at a time from the AudioRecord object through the FFT object. The number of samples we use will correspond to the number of component frequencies we will get after we run them through the FFT object. We are free to choose a different size, but we do need concern ourselves with memory and performance issues as the math required to the calculation is processor-intensive.

    private RealDoubleFFT transformer; int blockSize = 256;

    Button startStopButton; boolean started = false;

    //RecordAudio is an inner class defined here that extends AsyncTask.

    RecordAudio recordTask;

    //We'll be using an ImageView to display a Bitmap image. This image will represent the levels of the various frequencies that are in the current audio stream. To draw these levels, we'll use Canvas and Paint objects constructed from the Bitmap.

    ImageView imageView; Bitmap bitmap; Canvas canvas; Paint paint;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_fft);

        startStopButton = (Button) this.findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(this);

        //The RealDoubleFFT class constructor takes in the number of samples that we'll deal with at a time. This also represents the number of distinct ranges of frequencies that will be output.

        transformer = new RealDoubleFFT(blockSize); //Here is the setup of the ImageView and related object for drawing.

        imageView = (ImageView) this.findViewById(R.id.ImageView01);

        bitmap = Bitmap.createBitmap((int) 256, (int) 100, Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitmap);

        paint.setColor(Color.GREEN);

        imageView.setImageBitmap(bitmap);

    }

    @Override
    public void onClick(View v) {

        if (started) {

            started = false;

            startStopButton.setText("Start");
            recordTask.cancel(true);
        } else {

            started = true;

            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();

        }

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

    //Most of the work in this activity is done in the following class, called RecordAudio, which extends AsyncTask. Using AsyncTask, we run the methods that will tie up the user interface on a separate thread. Anything that is placed in the doInBackground method will be run in this manner.

        private class RecordAudio extends AsyncTask<Void, double[], Void> {
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

                            Log.e("AudioRecord", "Recording Failed");



                            //onProgressUpdate runs on the main thread in our activity and can therefore interact with the user interface without problems. In this implementation, we are passing in the data after it has been run through the FFT object. This method takes care of drawing the data on the screen as a series of lines at most 100 pixels tall. Each line represents one of the elements in the array and therefore a range of 15.625 Hz. The first line represents frequencies ranging from 0 to 15.625 Hz, and the last line represents frequencies ranging from 3,984.375 to 4,000 Hz. Figure 8-1 shows what this looks like in action.


                        }
                    }
                } catch (Error e) {

                }
                return null;
            }

        }
    }



