package com.vlcnavigation.module.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.lifecycle.MutableLiveData;

import timber.log.Timber;

public class AudioRecorder extends Thread {

    public AudioRecorder(MutableLiveData<Boolean> bool)
    {
        this.isRecording = bool;
    }

    private final short AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final short CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private MutableLiveData<Boolean> isRecording;
    private final int FREQUENCY = 32000;

    @Override
    public void run()
    {


        final int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING);
        final AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize);

//        audioRecord.startRecording();
        Timber.d("Start recording");

        while(isRecording.getValue()) {

            Timber.d("Recording");

            short[] buffer = new short[bufferSize];
            int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);// ����bufferSize���ȵ�����

//            if(detectfm !=null){
//                SoundWave soundwave = (SoundWave)findViewById(R.id.soundwave);
//                if(soundwave != null)
//                    soundwave.sndAudioBuf(buffer,bufferReadResult);
//            }
//            int flag = 0;
//            for (int i = 0; i < bufferReadResult; i++) {
//                if (buffer[i] != 0) {
//                    flag = i;
//                    break;
//                }
//            }
//            int len = bufferReadResult - flag - 10;
//            //	System.out.println(bufferReadResult);
//            //	System.out.println(len);
//            int[] tmp;
//            if (len < 50) {
//                tmp = new int[50];
//                for (int i = 0; i < 50; i++)
//                    tmp[i] = 0;
//                continue;
//            } else {// ��������������16λ�ģ��Ұ�����ת����int���ͣ���������tmp
//                tmp = new int[len];
//                for (int i = flag; i < flag + len; i++) {
//                    tmp[i - flag] = buffer[i];
//                }
//            }
//            // ����Decoder�࣬����
//            Decoder_3B dec = new Decoder_3B(tmp, frequency);
//            long VLCID = dec.getID();
//
//            Message message = Message.obtain();
//            message.obj = VLCID;
//            recordHandler.sendMessage(message);

            // do stuff
        }

        Timber.d("Stopped recording");
//        audioRecord.stop();
//        audioRecord.release();
    }
}
