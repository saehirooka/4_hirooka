package com.example.bluetooth;

import android.app.Application;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Globals extends Application {
    Socket socket;
    OutputStream mOutputStream;
    InputStream mInputStream;
    int inst; //楽器

    public void GlobalsAllInit() {
        socket = null;
        mOutputStream = null;
        mInputStream = null;
        inst = 74;
    }
}
