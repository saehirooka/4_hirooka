import jp.crestmuse.cmx.filewrappers.*;
import jp.crestmuse.cmx.processing.*;
import jp.crestmuse.cmx.elements.*;
import javax.xml.transform.*;
import java.util.*;

class GetNote {
  SCCDataSet.Part[] partlist;
  int i = 0,j = 0;
  int[][] nn;
  long[][] len,rest;
  int partn,n=1000;
  long offset;
  
  GetNote() {
    CMXController cmx = CMXController.getInstance();
    File dir = new  File("C:/Users/user/OneDrive/デスクトップ/卒業研究/music");
    File[] files = dir.listFiles();
    String file = files[0].toString();
    SCCDataSet sccdataset = cmx.readSMFAsMIDIXML(file).toSCC();
    //SCCDataSet sccdataset = cmx.readSMFAsMIDIXML(file).toSCCXML().toDataSet();
    partlist = sccdataset.getPartList(); // パートを取得
    partn = partlist.length;
    
    nn = new int[partn][n];
    len = new long[partn][n];
    rest = new long[partn][n];
    
    for (SCCDataSet.Part part : partlist) { 
      MutableMusicEvent[] notelist = part.getNoteOnlyList(); 
      j=0;
      
      for (MutableMusicEvent note : notelist) {
       // println(note);
        nn[i][j]=note.notenum();
        len[i][j]=note.offset()-note.onset();
        
        if(j!=0 && offset != note.onset()) {
          rest[i][j]=note.onset()-offset;
        }
        
        j++;
        offset=note.offset();
      } 
      
      nn[i][j]=-1;
      i++;
    }
  }
  
  int[][] getNoteNum() {
    return nn;
  }
  
  long[][] getNoteLen() {
    return len;
  }
  
  long[][] getRest() {
    return rest;
  }
}
