package com.example.bluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity {

    Globals globals;
//    Socket socket = null;
//    private OutputStream mOutputStream = null;//出力ストリーム
//    private InputStream mInputStream = null;

    private String[] spinnerItems = {"1", "2", "3", "4"};
    private String[] spinnerItems2 = {"flute", "alto sax", "trumpet", "horn", "tuba"};
    private TextView textView;

    int port;

    private Button btnSend;//送信用ボタン
    boolean btn= false;
    private Button btnPly;//スタートボタン
    //private TextView textview;//MacAddress表示用

//    Recorder recorder;
//    Play play;

//    public CMXController cmx = CMXController.getInstance();
//    UsbMidiSystem usb;

    Thread btnThread;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //グローバル変数を取得
        globals = (Globals)this.getApplication();
        //初期化
        globals.GlobalsAllInit();

        textView = findViewById(R.id.text_view);

        Spinner spinner = findViewById(R.id.spinner);
        Spinner spinner2 = findViewById(R.id.spinner2);

        // ArrayAdapter
        ArrayAdapter<String> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // 楽器選択spinner
        ArrayAdapter<String> adapter2
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerItems2);

        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        // リスナーを登録
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner = (Spinner)parent;
                String item = (String)spinner.getSelectedItem();
                textView.setText(item+"P");
                if(item=="1") {
                    port=8080;
                    System.out.println("send8080");
                }else if(item == "2") {
                    port=8081;
                }else if(item == "3") {
                    port=8082;
                }else{
                    port=8083;
                }
            }

            //　アイテムが選択されなかった
            public void onNothingSelected(AdapterView<?> parent) {
                port=8080;
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //　アイテムが選択された時
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                Spinner spinner2 = (Spinner)parent;
                String item = (String)spinner2.getSelectedItem();
                if(item=="flute") {
                    globals.inst=74;
                }else if(item == "alto sax") {
                    globals.inst=66;
                }else if(item == "trumpet") {
                    globals.inst=57;
                }else if(item == "horn"){
                    globals.inst=61;
                }else if(item == "tuba") {
                    globals.inst=59;
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                globals.inst=66;
            }
        });


//        AudioAttributes audioAttributes = new AudioAttributes.Builder()
//                .setUsage(AudioAttributes.USAGE_GAME)
//                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
//                .build();


//        usb = new UsbMidiSystem(this);
//        usb.initialize();
//
//        new MidiSystemAdapter(this).adaptAndroidMidiDeviceToKshoji();
//        checkMidiOutDeviceInfo();



        btnSend = (Button) findViewById(R.id.button);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("click" + btn);
                btn = true;
            }
        });

        btnThread = new Thread() {
            public void run() {

                if(btn==false) {
                    System.out.println("btn");
                    try {
                        btnThread.sleep(6000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        };

        intent = new Intent(this, PlayActivity.class);
        btnPly = (Button) findViewById(R.id.button2);
        btnPly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(globals.socket!=null) {
                    System.out.println("click2");
                    startActivity(intent);
                }
            }
        });
    }

    //アクティビティ開始時に呼ばれる
    @Override
    public void onStart() {
        super.onStart();

        Thread thread = new Thread() {

            public void run() {
                btnThread.start();
                try {
                    btnThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    if (globals.socket == null) {
                        //if(btn==true) {
                            connect();
                            System.out.println("connect");
                        //}
                    }

                } catch (Exception e) {
                }
            }
        };


        thread.start();
    }

    // 接続
    public void connect() {
        String IPAddress = "192.168.3.17";

        try {
//接続先のIPアドレスとポートを指定してSocketAddressオブジェクトを生成
            InetSocketAddress endpoint = new InetSocketAddress(IPAddress, port);

//接続されていないソケットを作成
            globals.socket = new Socket();

//指定されたタイムアウト値を使って、サーバーに接続
            globals.socket.connect(endpoint, 10000);
            globals.mOutputStream = globals.socket.getOutputStream();
            globals.mInputStream = globals.socket.getInputStream();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    void checkMidiOutDeviceInfo() {
//        try {
//            if (SoundUtils.getMidiOutDeviceInfo().size() <= 0) {
//                final String appPackageName = "net.volcanomobile.fluidsynthmidi"; // getPackageName() from Context or Activity object
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
//            } else if (SoundUtils.getMidiOutDeviceInfo().size() == 1) {
//                try {
//                    cmx.setMidiOutDevice(SoundUtils.getMidiOutDevice(0).getDeviceInfo().getName());
//                } catch (MidiUnavailableException ex) {
//                    ex.printStackTrace();
//                }
//            }
//        } catch (MidiUnavailableException e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        //usb.terminate();
        //recorder.stop();
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