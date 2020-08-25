package com.example.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import jp.crestmuse.cmx.amusaj.sp.MidiEventSender;
import jp.crestmuse.cmx.amusaj.sp.MidiOutputModule;
import jp.crestmuse.cmx.processing.CMXController;

public class Play implements Runnable{
    PlayActivity plyAct;
    //public boolean touch = false;
    public boolean noteon = false;
    CMXController cmx;
    MidiEventSender midiEventSender;
    String str; //notenum,long
    int data; //notenum,long
    public int notenum = 0;
    public int vel = 0;
    int len = 0;
    byte[] buffer = new byte[5];
    Recorder recorder;
    Socket socket;
    InputStream mInputStream;
    OutputStream mOutputStream;
    int n = 0;
    int inst;

    Play(PlayActivity p, Socket s, Recorder r, InputStream in, OutputStream out, CMXController cc, int i) {
        plyAct = p;
        recorder=r;
        socket=s;
        mInputStream=in;
        mOutputStream=out;
        cmx=cc;
        inst = i;

        midiEventSender = cmx.createMidiEventSender();
        midiEventSender.setTickTimer(cmx);

        MidiOutputModule mo = cmx.createMidiOut();

        cmx.connect(midiEventSender, 0, mo, 0);
        cmx.startSP();

        midiEventSender.sendProgramChange(0, 0, inst);
    }

    public void Send(int i){
        try {
            //if(socket!=null) {
                System.out.println("send now");
                mOutputStream.write(i);
            //}
        } catch (Exception e) {
            System.out.println("send error");
            try{
                socket.close();
            }catch(IOException e1){/*ignore*/}
        }
    }

    synchronized void receive() {
        try {
            if(mInputStream!=null) {
                mInputStream.read(buffer);
                str = new String(buffer);
                System.out.println("buffer" + str);

                data = Integer.parseInt(str);
                notenum = data / 1000;
                len = data % 1000;
            }
        } catch (IOException e) {
            try {
                System.out.println("read error");
                socket.close();
            } catch (IOException e1) {/*ignore*/}
        }
    }

    @Override
    public void run() {
        while (recorder.isRecording) {
            //recordingの回数を減らしてみる
            recorder.recording();
            if(noteon==false) {
                detectionBreath();
            }
            if(noteon==true) {
                volume();
            }
        }
    }

    synchronized void detectionBreath() {
        try {
            wait(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (plyAct.touch == true) {
           // if (noteon == false) {
                if (notenum == 0 && n==0) {
                    System.out.println("touch");
                    Send(1);
                    n=1;
                }

                if (n==1) {
                    receive();
                    n=2;
                }
                if(notenum!=0) {
                    if(len <= 240) { //連符の合図が来たらタッチだけで音　音量だけ指定 240　八分音符以下
                        noteon();
                    }else {
                        if (recorder.getOn() == true) {
                            System.out.println("RECORDER.ON " + recorder.on);
                            noteon();
                        }
                    }
                }

           // }
        }
    }

    synchronized  void noteon() {
        //if (recorder.getOn() == true) {
          //  System.out.println("RECORDER.ON " + recorder.on);
            //if (notenum != 0) {
                vel = recorder.getDiff()*3;
                //System.out.println("vel" + vel);

                //velとdiff分ける
                if (vel > 120) {
                    vel = 120;
                }
                if(vel < 20) {
                    vel = 20;
                }

                //System.out.println("vel" + vel);

                midiEventSender.sendNoteOn(0, 0, notenum, vel);

                noteon = true;
                System.out.println("noteon"+noteon);
           // }
        //}
    }

    synchronized void noteoff() {
        if (notenum != 0 && noteon == true) {
            System.out.println("!noteoff2");
            midiEventSender.sendNoteOff(0, 0, notenum, vel);
        }
        noteon = false;
        notenum = 0;
        n=0;
    }

    void volume() {
        //発音したvelから引いたり足したりしていく
        if (notenum != 0) {
            //System.out.println("vel2:" + recorder.diff*3);
            if(recorder.diff < 0) {
                vel += recorder.diff * 1.4;
            }else if(recorder.diff > 0){
                vel+=recorder.diff*10;
            }
            if(vel > 120) {
                vel=120;
            }
            if(vel < 20) {
                vel=20;
            }else {
                System.out.println("vel2:" + vel);
            }

            midiEventSender.sendControlChange(0, 0, 11, vel);
        }
        try {
            Thread.sleep(80);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }
}

