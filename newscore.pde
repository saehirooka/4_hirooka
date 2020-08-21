import processing.net.*;


int QUARTER = 120;
int PLAYER = 1;
int data;
int scene = 0;
int P=0;
int[] I = new int[4];
boolean[] receive = new boolean[4];
String[] cltIp= new String[4];
Thread[] read = new Thread[PLAYER];

Server[] server = new Server[PLAYER];
Client[] client = new Client[PLAYER];


class Notes {
  int player;
  float step = 2.5;
  float rectX, rectY, rectD;
  float lineX, lineY;
  int appear = 0;
  int nn;
  long tick;

  Notes(int p, float x, float y, int d, float x2, float y2, long t) {
    player = p;
    rectX=x;
    rectY=y;
    rectD=d;
    lineX=x2;
    lineY=y2;
    tick=t;
  }

  void drawNotes() {
    // if (appear<3) {
    if (rectX+rectD > 0) {
      if (appear<=2) {
        if (player==0) {
          stroke(255, 0, 0);
          fill(255, 0, 0, 100);
        } else if (player==1) {
          stroke(0, 255, 0);
          fill(0, 255, 0, 100);
        } else if (player==2) {
          stroke(0, 0, 255);
          fill(0, 0, 255, 100);
        }
        rect(rectX, rectY, rectD, 30);
        stroke(0);
        strokeWeight(1.5);
        line(lineX, lineY, lineX, lineY+120);
      }
    } else {
      appear=2;
    }
  }

  void moveNotes() {
    rectX-=step;
    lineX-=step;
  }

  //ノートナンバーを送る
  void sendNum() {
    if (this.appear==1) {
      println("ok");
      println(tick);
      String str = String.valueOf(nn)+String.valueOf(tick);
      println(str);
      //int a = nn*1000+(int)tick;
      //println(a);
      byte[] b = str.getBytes();
      server[player].write(b);
      this.appear=2;
    }
  }

  void over() {
    if (appear==0&&receive[player] && rectX <= 41 && 40 < rectX+rectD-2) {
      appear=1;
      receive[player]=false;
    }
  }
}

Notes[][] notes;
GetNote[] note;

void setup() {
  //fullScreen();
  size(800, 700);
  //myPort = new Serial(this, "COM5", 9600);
  //myPort = new Serial(this, "COM9", 9600);

  for (int i = 0; i < PLAYER; i++) {
    server[i] = new Server(this, 8080+i);
    //println("server address: " + server[i].ip());
  }


  notes = new Notes[PLAYER][1000];
  note = new GetNote[PLAYER];

  int start = width;
  int start2 = width; 
  int d = QUARTER; //音の長さ
  int y0 = 25; //ひとつ前の音の高さ(場所)
  int y = 25;
  int k;
  int part = 0;
  int high = 25;
  int low = 160;
  long tick = d;

  for (int p = 0; p < PLAYER; p++) {
    read[p] = new Thread(new Read(p));
    receive[p]=false;
    note[p] = new GetNote();
    for (int i = 0; i < note[p].getNoteNum()[part].length; i++) {

      if (note[p].getNoteNum()[part][i]==-1) {
        i=note[p].getNoteNum()[part].length;
      } else {
        if (i==0) {
          //基準の音
          //y=175-15*((note[p].getNoteNum()[part][i]%12)/2)-15;
          y=low-15*((note[p].getNoteNum()[part][i]%12)/2);
        } else {
          k=note[p].getNoteNum()[part][i]-note[p].getNoteNum()[part][i-1];
          if (abs(k)!=1) {
            k/=2;
          }
          y=y0-15*(k);

          //五線より下にいたら一オクターブ上げる
          if (y > low) { 
            y-=105;
          }

          //五線より上にいたら一オクターブ下げる
          if (y < high) { 
            y+=105;
          }
        }
        tick = note[p].getNoteLen()[part][i];
        d=int(tick/4);

        notes[p][i]=new Notes(p, start, y, d, start2, high, tick);
        notes[p][i].nn=note[p].getNoteNum()[part][i];
        start+=d+int(note[p].getRest()[part][i+1]/4);
        start2+=QUARTER*4;
        y0=y;
      }
    }
    high+=200;
    low+=200;
    start=width;
    start2 = width;
    part++;
    read[p].start();
  }
  thread("sendNotes");
}

//五線
void staff(int y) {
  stroke(0);
  strokeWeight(1);
  for (int i = 0; i < 5; i++) {
    line(0, y+30*i, width, y+30*i);
  }
}

void draw() {
  background(255);
  if (scene==0) {
    fill(0);
    textSize(23);
    text("PLAYER:", width/2-50, height/2);
  }
  if (scene==1) {
    stroke(255, 0, 0);
    strokeWeight(1.5);
    line(40, 0, 40, height);

    for (int i=0; i < 3; i++) { 
      staff(25+i*200);
    }

    drawScore();
    //int part = 0;
    //for (int player = 0; player < PLAYER; player++) {
    //  //for (int i = I[player]; i < note[player].getNoteNum()[part].length; i++) {
    //  for (int i = I[player]; i < I[player]+20; i++) {
    //    if (note[player].getNoteNum()[part][i]==-1) { 
    //      i=note[player].getNoteNum()[part].length; //break
    //    } else {
    //      notes[player][i].over();
    //      notes[player][i].drawNotes();
    //      notes[player][i].moveNotes();
    //      notes[player][i].sendNum();
    //      if (notes[player][i].appear==2) {
    //        I[player]=i;
    //        println(i);
    //      }
    //    }
    //  }
    //  part++;
    //}
  }
}

void drawScore() {
  int part = 0;
  for (int player = 0; player < PLAYER; player++) {
    for (int i = I[player]; i < note[player].getNoteNum()[part].length; i++) {
      if (note[player].getNoteNum()[part][i]==-1) { 
        i=note[player].getNoteNum()[part].length; //break
      } else {
        notes[player][i].drawNotes();
        notes[player][i].moveNotes();
      }
    }
    part++;
  }
}

void sendNotes() {
  while (true) {
    int part = 0;
    for (int player = 0; player < PLAYER; player++) {
      for (int i = I[player]; i < I[player]+20; i++) {
        if (note[player].getNoteNum()[part][i]==-1) { 
          i=note[player].getNoteNum()[part].length; //break
        } else {
          notes[player][i].over();
          notes[player][i].sendNum();
          if (notes[player][i].appear==2) {
            I[player]=i;
            //println(i);
          }
        }
      }
      part++;
    }
  }
}

void serverEvent(Server evtServer, Client conClient ) {
  println(conClient.ip()+"から接続");
}

class Read implements Runnable {
  int player;
  int inBuffer;

  Read(int p) {
    player=p;
  }

  synchronized void run() {
    while (true) {
      client[player] = server[player].available();
      //byte[] inBuffer = new byte[7];
      if (client[player] != null) {
        inBuffer = 0;
        if (receive[player]==false) {
          inBuffer=client[player].read();
          if (inBuffer == 1) {
            //  // String myString = new String(inBuffer);
            //  //print(myString);
            println("int", inBuffer);
            receive[player]=true;
          }
        }
        client[player]=null;
      }
    }
  }
}



void keyPressed() {
  scene=1;
}
