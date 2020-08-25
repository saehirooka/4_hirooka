import processing.net.*;


int QUARTER = 120;
int PLAYER = 1;
int data;
int scene = 0;
int P=0;
float diff = 0;
int[] I = new int[4];
boolean[] receive = new boolean[4];
String[] cltIp= new String[4];
Thread[] read = new Thread[PLAYER];
float[][] lineX = new float[PLAYER][500];
float[] lineY = new float[PLAYER];
float step = 2.5;
int I2 = 0;
int plyCnt =0; //接続してきた人数

Server[] server = new Server[PLAYER];
Client[] client = new Client[PLAYER];


class Notes {
  int player;
  float rectX, rectY, rectD;
  int appear = 0;
  int nn;
  long tick;
  String str;
  byte[] b;

  Notes(int p, float x, float y, int d, long t) {
    player = p;
    rectX=x;
    rectY=y;
    rectD=d;
    tick=t;
  }

  void drawNotes() {
    if (rectX <= width && appear<=2) {
      strokeWeight(0.8);
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
    }
  }

  void moveNotes() {
    if (appear == 2) {
      rectD-=step;
    } else {
      rectX-=step;
    }
    if (rectX+rectD < 0 || appear==2 && rectX >= rectD) {
      appear=3;
    }
  }


  //ノートナンバーを送る
  void sendNum() {
    if (this.appear==1) {
      println("ok");
      println(tick);
      str = String.valueOf(nn)+String.valueOf(tick);
      println(str);
      //int a = nn*1000+(int)tick;
      //println(a);
      b = str.getBytes();
      server[player].write(b);
      this.appear=2;

      diff=(40-rectX);
      rectD-=diff;
      rectX=40;
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
  int d = QUARTER; //音の長さ
  int y0 = 25; //ひとつ前の音の高さ(場所)
  int y = 25;
  int k;
  int high = 25;
  int low = 160;
  long tick = d;

  lineY[0]=high;

  for (int p = 0; p < PLAYER; p++) {
    lineX[p][0]=width;
    if (p>0) {
      lineY[p]=lineY[p-1]+200;
    }
    for (int i = 1; i < 500; i++) {
      lineX[p][i]=lineX[p][i-1]+QUARTER*4;
    }
  }

  for (int p = 0; p < PLAYER; p++) {
    read[p] = new Thread(new Read(p));
    receive[p]=false;
    note[p] = new GetNote();
    for (int i = 0; i < note[p].getNoteNum()[p].length; i++) {

      if (note[p].getNoteNum()[p][i]==-1) {
        i=note[p].getNoteNum()[p].length;
      } else {
        if (i==0) {
          //基準の音
          //y=175-15*((note[p].getNoteNum()[p][i]%12)/2)-15;
          y=low-15*((note[p].getNoteNum()[p][i]%12)/2);
        } else {
          k=note[p].getNoteNum()[p][i]-note[p].getNoteNum()[p][i-1];
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
        tick = note[p].getNoteLen()[p][i];
        d=int(tick/4);

        notes[p][i]=new Notes(p, start, y, d, tick);
        notes[p][i].nn=note[p].getNoteNum()[p][i];
        start+=d+int(note[p].getRest()[p][i+1]/4);
        y0=y;
      }
    }
    high+=200;
    low+=200;
    start=width;
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

void startScreen() {
  fill(0);
  textSize(23);
  text("PLAYER:"+plyCnt, width/2-50, height/2);
}

void draw() {
  background(255);
  if (scene==0) {
    startScreen();
  }
  if (scene==1) {
    drawScore();
    
    stroke(255, 0, 0);
    strokeWeight(1.5);
    line(40, 0, 40, height);
  }
}


void moveLines(int player) {
  stroke(0);
  strokeWeight(1.5);
  //for (int player = 0; player < PLAYER; player++) {
    for (int i = I2; i < 500; i++) {
      if (lineX[player][i] >= 0) {
        if (I2+3 > i && lineX[player][i] <= width) {
          line(lineX[player][i], lineY[player], lineX[player][i], lineY[player]+120);
        }
        lineX[player][i]-=step;
      } else {
        I2=i;
      }
    }
  //}
}

void drawScore() {
  for (int player = 0; player < PLAYER; player++) {
    moveLines(player);
    staff(25+player*200);
    for (int i = I[player]; i < note[player].getNoteNum()[player].length; i++) {
      if (note[player].getNoteNum()[player][i]==-1) { 
        i=note[player].getNoteNum()[player].length; //break
      } else {
        notes[player][i].drawNotes();
        notes[player][i].moveNotes();
      }
    }
  }
}

void sendNotes() {
  while (true) {
    for (int player = 0; player < PLAYER; player++) {
      for (int i = I[player]; i < I[player]+20; i++) {
        if (note[player].getNoteNum()[player][i]==-1) { 
          i=note[player].getNoteNum()[player].length; //break
        } else {
          notes[player][i].over();
          notes[player][i].sendNum();
          if (notes[player][i].appear==2) {
            I[player]=i;
            //println(i);
          }
        }
      }
    }
  }
}

void serverEvent(Server evtServer, Client conClient ) {
  println(conClient.ip()+"から接続");
  plyCnt++;
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
