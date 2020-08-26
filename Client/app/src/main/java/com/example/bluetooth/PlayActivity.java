package com.example.bluetooth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import jp.crestmuse.cmx.processing.CMXController;
import jp.crestmuse.cmx.sound.SoundUtils;
import jp.kshoji.javax.sound.midi.MidiUnavailableException;
import jp.kshoji.javax.sound.midi.UsbMidiSystem;
import jp.kthrlab.midi.adapter.MidiSystemAdapter;

public class PlayActivity extends AppCompatActivity {
    Globals globals;
    TextView textview;
    Recorder recorder;
    Play play;
    boolean touch = false;

    public CMXController cmx = CMXController.getInstance();
    UsbMidiSystem usb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //グローバル変数を取得
        globals = (Globals)this.getApplication();

        textview = (TextView)findViewById(R.id.textView);

        usb = new UsbMidiSystem(this);
        usb.initialize();

        new MidiSystemAdapter(this).adaptAndroidMidiDeviceToKshoji();
        checkMidiOutDeviceInfo();

        recorder = new Recorder();
        play = new Play(this, globals.socket, recorder, globals.mInputStream, globals.mOutputStream, cmx, globals.inst);
        (new Thread(play)).start();
    }

    public synchronized boolean onTouchEvent(MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) { //||motionEvent.getAction()==MotionEvent.ACTION_POINTER_DOWN
                touch = true;
                play.noteoff();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                play.noteoff();
                touch = false;
            }

        return true;
    }

    void checkMidiOutDeviceInfo() {
        try {
            if (SoundUtils.getMidiOutDeviceInfo().size() <= 0) {
                final String appPackageName = "net.volcanomobile.fluidsynthmidi"; // getPackageName() from Context or Activity object
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            } else if (SoundUtils.getMidiOutDeviceInfo().size() == 1) {
                try {
                    cmx.setMidiOutDevice(SoundUtils.getMidiOutDevice(0).getDeviceInfo().getName());
                } catch (MidiUnavailableException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usb.terminate();
        recorder.stop();
        if (globals.socket != null) {
            try {
                globals.mOutputStream.close();
                globals.mInputStream.close();
                globals.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            globals.socket = null;
        }
    }
}


