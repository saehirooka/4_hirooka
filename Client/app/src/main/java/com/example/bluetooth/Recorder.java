package com.example.bluetooth;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

//public class Recorder  implements Runnable {
public class Recorder {
    private static final int SAMPLE_RATE = 44100;

    private int bufferSize;
    public AudioRecord audioRecord;
    public boolean isRecording;
    public boolean isPausing;
    private double baseValue;
    public double db;
    public boolean on=false;
    public double pre = 0;
    double diff = 0;
    public double on_diff;
    int read;
    int maxValue;
    double min_db;
    short[] buffer;

    public Recorder() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        buffer = new short[bufferSize];
        isRecording = true;
        baseValue = 12.0;
        pause();
        android.os.Process
                .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        resume();
    }

    //@Override
    public void recording() {
    //public void run() {
//            android.os.Process
//                    .setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//
//            resume();
//
//
//        short[] buffer = new short[bufferSize];
        //while (isRecording) {
            if (!isPausing) {
                //System.out.println("recording");
                read = audioRecord.read(buffer, 0, bufferSize);
                if (read < 0) {
                    throw new IllegalStateException();
                }
                maxValue = 0;
                for (int i = 0; i < read; i++) {
                    maxValue = Math.max(maxValue, buffer[i]);
                }

                db = 20.0 * Math.log10(maxValue / baseValue);
                if(db < 0) {
                    db = 0;
                }

//System.out.println("db:"+db);
                diff = db-pre;
                //Log.d("SoundLevelMeter", "diff:" + diff);
                //System.out.println("ii:"+bluetooth.i);
                //if (on == false && 30 < diff && diff < 65) {

                //if(db<35) {
                if(on==false) {
                    if (8 <= diff && diff < 50) {
                        //Log.d("SoundLevelMeter", "dB:" + db);
                        //System.out.println("truediff" + diff);
                        on_diff = diff;
                        on = true;
                        min_db=db;
                    }
                } else {
                    //if (diff < -10) {
                    if (diff < -4) {
                        on = false;
                    }
                }

                pre = db;


         }


    }


    public void pause() {
        if (!isPausing)
            audioRecord.stop();

        isPausing = true;
    }



    public void resume() {
        if (isPausing)
            audioRecord.startRecording();

        isPausing = false;
    }

    public boolean getOn() {
        return on;
    }

    public int getDiff() {
        return (int)on_diff;
    }

    public void stop() {
        audioRecord.stop();
        audioRecord.release();
        isRecording = false;
    }
}

