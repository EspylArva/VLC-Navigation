package com.vlcnavigation.module.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.lifecycle.MutableLiveData;

import com.vlcnavigation.MainActivity;
import com.vlcnavigation.ui.fft.FFTFragment;


//import org.apache.commons.math3.complex.Complex;
//import org.apache.commons.math3.transform.TransformType;

import java.util.Arrays;

import edu.princeton.cs.algorithms.Complex;
import edu.princeton.cs.algorithms.FFT;
import timber.log.Timber;
//import uk.me.berndporr.kiss_fft.KISSFastFourierTransformer;

public class AudioRecorder extends Thread {

    public static int FREQUENCY = 32000;

    //    private FFTFragment fragment;
    private SignalView signalView;
    private static short AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static short CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private MutableLiveData<Boolean> isRecording;
    //public int FREQUENCY = 32000;
    public static int buffersize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING);


    AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING, buffersize);


    public AudioRecorder(MutableLiveData<Boolean> bool, SignalView signalView) //, FFTFragment frag)
    {
        this.isRecording = bool;
        this.signalView = signalView;
//        this.fragment = frag;
    }



    @Override
    public void run()
    {




        audioRecord.startRecording();
        Timber.d("Start recording");

        while(isRecording.getValue()) {

//            Timber.d("Recording");

            short[] buffer = new short[buffersize];
            int bufferReadResult = audioRecord.read(buffer, 0, buffersize);// ����bufferSize���ȵ�����

            MainActivity.BUFFER = buffer;
            MainActivity.BUFFER_READ_RESULT = bufferReadResult;


            if(signalView != null)
            {
                signalView.sndAudioBuf(MainActivity.BUFFER, MainActivity.BUFFER_READ_RESULT);
            }

            int flag = 0;
            for (int i = 0; i < MainActivity.BUFFER_READ_RESULT; i++) {
                if (MainActivity.BUFFER[i] != 0) {
                    flag = i;
                    break;
                }
            }
            int len = MainActivity.BUFFER_READ_RESULT - flag - 10;
            //	System.out.println(bufferReadResult);
            //	System.out.println(len);
            int[] tmp;
            if (len < 50) {
                tmp = new int[50];
                for (int i = 0; i < 50; i++)
                    tmp[i] = 0;
                continue;
            } else {// ��������������16λ�ģ��Ұ�����ת����int���ͣ���������tmp
                tmp = new int[len];
                for (int i = flag; i < flag + len; i++) {
                    tmp[i - flag] = MainActivity.BUFFER[i];
                }
            }

            // FFT
                // closest power of 2 to the size
            int n = (int) Math.pow(2, tmp.length == 0 ? 0 : 31 - Integer.numberOfLeadingZeros(tmp.length - 1)); //= 1024;
            //Timber.d("Size: %s (initial size: %s)", n, tmp.length);
            Complex[] x = new Complex[n];

            // original data
            for (int i = 0; i < n; i++) {
                x[i] = new Complex(tmp[i], 0);
//                        Math.sin(i), 0);
            }

            Complex[] y = FFT.fft(x);
            //Timber.d("Frequency: %s", getFreq(y));


//            // ����Decoder�࣬����
//            Decoder_3B dec = new Decoder_3B(tmp, frequency);
//            long VLCID = dec.getID();
//
//            Message message = Message.obtain();
//            message.obj = VLCID;
//            recordHandler.sendMessage(message);

        }

        Timber.d("Stopped recording");
        audioRecord.stop();
        audioRecord.release();
    }


}
