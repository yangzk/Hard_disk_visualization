import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.io.*; 
import java.util.*; 
import processing.pdf.*; 
import java.util.Date; 
import java.text.*; 
import java.util.GregorianCalendar; 
import java.util.Calendar; 
import java.util.Date; 
import java.util.Comparator; 
import java.util.HashMap; 
import java.util.Map; 
import java.util.TreeMap; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Hard_disk extends PApplet {















 
//border space, % of the block width and height
float vertiSpace = 0.05f;
float horizSpace = 0.05f;


//drawLine or drawBar 
boolean drawLine = false;
 
/*
color lineColor = color(96,91,133);
color eventColor = color(217,135,35);
color highLightColor = color(242,192,41);
color fontColor = color(30,35,64);
color backColor = color(255,255,255);*/

/*
color lineColor = color(33,64,47);
color eventColor = color(182,73,38);
color highLightColor = color(255,188,46);*/



//color lineColor = color(11,69,52);
int lineColor = color(0,0,0);

int eventColor = color(182,73,38);
int highLightColor = color(209,173,27);

int fontColor = color(10,58,64);



//color backColor = color(147,253,249);
int backColor = color(189,187,145);



Table table;


//number of rows and cols in the plot
int nrows = 2;
int ncols = 3; 
int yOffset = 50;

//list of variables to display, 

List<String> smartVars = Arrays.asList("smart_5_raw","smart_187_raw",
"smart_188_raw", "smart_197_raw","smart_198_raw");

/*
List<String> smartVars = Arrays.asList("Dim.1","Dim.2",
"Dim.3","Dim.4","Dim.5");*/
 
 
//min and max survival time
int minSurvTime = Integer.MAX_VALUE;
int maxSurvTime = Integer.MIN_VALUE;

int maxCapacityCount;

   
//ArrayList<Disk> disks ;
ArrayList<Smart> smarts ;

Map<String, ArrayList<Disk>> smartDisks = new LinkedHashMap<String, ArrayList<Disk>>();

Map<String, ArrayList<Disk>> serialDisks = new LinkedHashMap<String, ArrayList<Disk>>(); 

Map<Disk, ArrayList<Disk>> diskDisks = new LinkedHashMap<Disk, ArrayList<Disk>>(); 

Map<String, Integer> capacityCount = new LinkedHashMap<String, Integer>();

Map<String, Integer> capacityCountSort = new LinkedHashMap<String, Integer>();
 
public void setup(){
  //colorMode(HSB, 255);
  //size(1200,1200,PDF,"test.pdf");
  size(900,600 + yOffset );
 
  point(0,height);
  table = loadTable("out_fail.csv", "header");
  background(backColor);
  


  

  smarts = new ArrayList<Smart>();
  
for(String smartVar : smartVars){ 
   //for each SMART column, 
   //initialize hierarchical structure smart(disk(record))

  Smart tempSmart = new Smart(smartVar);
  ArrayList<Disk> disks = new ArrayList<Disk>();  
  Map<String, ArrayList<Record>> diskRecords = new TreeMap<String, ArrayList<Record>>();
  ArrayList<Record> records = new ArrayList<Record>();
  
    
  for(TableRow row : table.rows()){ //for each row
  
  
      
      String date = row.getString("date");
         
      String serial_number = row.getString("serial_number");
      String model = row.getString("model");
      String capacity_bytes = row.getString("capacity_bytes");  
      int failure = row.getInt("failure");   
      
      
      
      
     
      //println("capacity = " + capacity_bytes); 
    
      float smart_raw =  log( row.getInt(smartVar) +1 ) ; 
     
     //float smart_raw =   row.getInt(smartVar) ;  
    
      if(smart_raw > tempSmart.max_smart){
        tempSmart.max_smart = smart_raw;
      }
    
      if(smart_raw < tempSmart.min_smart){
        tempSmart.min_smart = smart_raw;
      }
         
      //add new record
      Record newRecord = new Record(serial_number, date, smartVar, model);
      if(failure == 1){
        newRecord.failure = true;
      }     
      newRecord.smart_raw = smart_raw;
      newRecord.model = model;
      newRecord.capacity_bytes ="" + (Long.parseLong(capacity_bytes)/(1024*1024*1024));
      
      //newRecord.smartVar = '' 
      records.add(newRecord);
      

      ArrayList<Record> tempRecords = diskRecords.get(serial_number);
      if(tempRecords == null){
        tempRecords = new ArrayList<Record>();
      } 
    
      tempRecords.add(newRecord);
      
      diskRecords.put(serial_number, tempRecords);
 
  }  
  
   //sort  diskCapacity
   
  
  tempSmart.diskRecords = diskRecords; 
  
  //add disks to current smart
  for(String serial_number:diskRecords.keySet()){
    Disk newDisk = new Disk(serial_number, smartVar);
    ArrayList<Record> newRecords = diskRecords.get(serial_number);
    for(Record record:newRecords){
      newDisk.addRecord(record);
      
      if(record.failure == true){
        newDisk.hasFailure = true;
        
        //add only add failed disks   ----->!!!!!!!                  
      
    
    newDisk.calculateSurvTime();
    //println("surv time = " + newDisk.survTime);
          // add all disks
      newDisk.capacity_bytes = newDisk.records.get(0).capacity_bytes;
      disks.add(newDisk); 
       }     
      
    }
      
       //add serialDisks map
       ArrayList<Disk> tempDisks =  serialDisks.get(serial_number);
       if( tempDisks == null){
         tempDisks = new ArrayList<Disk>();        
       }
       tempDisks.add(newDisk);
       serialDisks.put(serial_number, tempDisks);
      
  }
  
  Collections.sort(disks);
  tempSmart.disks = disks;
  
  tempSmart.calculate();
  
  //add the new constructed smart 
  smarts.add(tempSmart);
    
}

for(int i =0; i < smarts.size(); i++){
  if(nrows * ncols >= smarts.size()){
    int n_row = i / ncols; 
    int n_col = i % ncols;
    
    //print("n_row = " + n_row + "\n");
    //print("n_col = " + n_col + "\n");
    Smart tempSmart = smarts.get(i);
    
    //map coordiante of records in that smart to fit window
    tempSmart.mapCoord(width*1.0f/ncols*n_col + width*1.0f/ncols*horizSpace, 
                       width*1.0f/ncols*(n_col+1) - width*1.0f/ncols*horizSpace, 
                       600*1.0f/nrows*(n_row+1) - 600*1.0f/nrows*vertiSpace - 30 + yOffset, 
                       600*1.0f/nrows*(n_row) + 600*1.0f/nrows*vertiSpace + yOffset ); 
    /*
    ArrayList<Disk> tempDisks = tempSmart.disks;    
    Collections.sort(tempDisks);   
    tempSmart.disks = tempDisks;*/
    Collections.sort(tempSmart.disks);

    smarts.set(i, tempSmart);
  }else{
    println("Number of SMART exceeds ncols*nrows");
    exit();
  }
  
}

//prepare to draw histogram of capcity
maxCapacityCount = 0;
Smart tempSmart = smarts.get(0);
for(Disk disk:tempSmart.disks){
   
  int tempCount;
  if(capacityCount.get(disk.capacity_bytes) == null){
    tempCount = 1;
  }else{      
    tempCount = capacityCount.get(disk.capacity_bytes);    
    tempCount++;
  } 
 
if(tempCount > maxCapacityCount){
  maxCapacityCount = tempCount;
}  
  capacityCount.put(disk.capacity_bytes, tempCount);
}


//sort capacityCount by key
Map sortedMap = new TreeMap<String,Integer>();

Set keySet = capacityCount.keySet();
ArrayList<String> list = new ArrayList(keySet);   
Collections.sort(list, new CustomComparator());

 
//println("keys = " + list);
for(String listItem : list){
  capacityCountSort.put(listItem, capacityCount.get(listItem));
}


/*
Set keys = capacityCountSort.keySet();
   for (Iterator i = keys.iterator(); i.hasNext();) {
     String key = (String) i.next();
     Integer value = (Integer) capacityCountSort.get(key);
     System.out.println(key + " = " + value);
   } */


  
}
  


public class CustomComparator implements Comparator<String> {
    @Override
    public int compare(String str1, String str2) {
        //return   (Long.parseLong(str1).compare((Long.parseLong(str2))));
        return Long.compare(Long.parseLong(str1), Long.parseLong(str2));
    }
}
 
   
  
   
   
   
   
   



public void draw(){
   
  Disk mouseDisk;
   
  //draw by disk
  background(backColor);
  
  //add title
  String s = "Hard Drive Reliability Data Visualization";
  textSize(30);
  fill(fontColor);
  textAlign(LEFT, TOP);
  text(s,10,10); 
  s = "By: Sina Kargar & Zhenkai Yang\n" + "Master of AI, KU Levuen\n" + "May 2015"; 
  textSize(10);
  fill(fontColor);
  textAlign(RIGHT, TOP);
  text(s,width-10,10);
  
  //draw the capacity distribution on bottom-right panel
  drawCapacityHistogram();
  
  ArrayList<Disk> selectedDisks = new ArrayList<Disk>();
  
  
  int recordIndex = 0;
  
  
  //draw SMART panels
  aLoopName: for(Smart smart : smarts){   
    smart.display();
    smart.displayName();
    smart.drawLegend();
    
    //limit search region
    if(smart.mouseOnSmart()){
      for(Disk disk : smart.disks){
         for(Record record : disk.records){
           
        
           //select the record on mouse position
           if(record.mouseOnRecord()){
             
             disk.highlight();
             
             //draw record information around mouse
             record.drawMouse();
             
             //get index of selected disk
             int diskIndex =   smart.disks.indexOf(disk);
             
             //get index of selected record
               recordIndex = disk.records.indexOf(record);
             
             //get the records's values' corrsponding position on legend
             float xpos=map(record.smart_raw, smart.min_smart, smart.max_smart, smart.xmin, smart.xmax);
             
             //show the highlight bar on legend corresponding to mouse position
             record.showOnLegend(xpos, smart.ymin + 30 );
             
             
             //get all selected disked from different SMART panels
             selectedDisks = serialDisks.get(record.serial_number);
             /*
             int ii =0;
             for(Disk disk2 : tempDisks){
               
               //do same highlight as the one with mouse on it
               disk2.highlight();
               
               //get the record index with failure
               int failIndex = -1;
               for(Record record2 : disk2.records){              
                 if(record2.failure == true){
                   failIndex = disk2.records.indexOf(record2);
                   continue;
                 }  
               }
               
               //draw arc with color fill on the bottom-down position, and highlight the recordIndex one  
               //also highlight the failure record, using the  failIndex obtained
               disk2.drawArc(ii,tempDisks.size(), recordIndex,failIndex);
                              
               //highlight the selected disk on histogram
               highlightCapacityHistogram(disk2.capacity_bytes);
               
                
               ii++;
             }*/
             //when found the record, exit loop to save time
              continue aLoopName;                       
           }
         }
          
        }
        
        
      }    
    }
    


             int ii =0;
             for(Disk disk2 : selectedDisks){
               
               //do same highlight as the one with mouse on it
               disk2.highlight();
               
               //get the record index with failure
               int failIndex = -1;
               for(Record record2 : disk2.records){              
                 if(record2.failure == true){
                   failIndex = disk2.records.indexOf(record2);
                   continue;
                 }  
               }
               
               //draw arc with color fill on the bottom-down position, and highlight the recordIndex one  
               //also highlight the failure record, using the  failIndex obtained
               disk2.drawArc(ii,selectedDisks.size(), recordIndex,failIndex);
                              
               //highlight the selected disk on histogram
               highlightCapacityHistogram(disk2.capacity_bytes);
               
                
               ii++;
             }    
       
     
    
    
     
    
}
   

public void drawCapacityHistogram(){
    int startX = 650;
    int startY = height - 20;
    
    int histW = width - startY  - 10;
    int histH = 75;
    
   Set keys = capacityCountSort.keySet();
   
   int ii=0;
   for (Iterator i = keys.iterator(); i.hasNext();) {
     String key = (String) i.next();
     Integer value = (Integer) capacityCountSort.get(key);
     
     float heighti = map(value, 0, maxCapacityCount, 0, histH);
     
     strokeWeight(histW/capacityCountSort.size()*0.9f);
     stroke(0);
 
     line(startX + histW*1.0f/capacityCountSort.size()*ii, startY, 
          startX + histW*1.0f/capacityCountSort.size()*ii, startY - heighti);
          
     if(dist(mouseX, mouseY, 
     startX + histW*1.0f/capacityCountSort.size()*ii, startY ) < 5 ){
       fill(fontColor); 
       textAlign(CENTER, TOP);
      
       text(key + " GB", startX + histW*1.0f/capacityCountSort.size()*ii, startY + 5);
     
       textAlign(CENTER, BOTTOM);
       text(value, startX + histW*1.0f/capacityCountSort.size()*ii, startY - heighti);
     
     }      
     ii++;
   }
   

}


public void highlightCapacityHistogram(String selectedCapacity){
    int startX = 650;
    int startY = height - 20;
    
     
    int histW = width - startY  - 10;
    int histH = 75;
    
       Set keys = capacityCountSort.keySet();
   
   int ii=0;
   for (Iterator i = keys.iterator(); i.hasNext();) {
     String key = (String) i.next();
     Integer value = (Integer) capacityCountSort.get(key);
     
     float heighti = map(value, 0, maxCapacityCount, 0, histH);
     
     if( key.equals(selectedCapacity)){
     strokeWeight(histW/capacityCountSort.size()*0.9f);
     
     stroke(highLightColor);
 
     line(startX + histW*1.0f/capacityCountSort.size()*ii, startY, 
          startX + histW*1.0f/capacityCountSort.size()*ii, startY - heighti);
          
     
     fill(fontColor); 
     textAlign(CENTER, TOP);
      
     text(key + " GB", startX + histW*1.0f/capacityCountSort.size()*ii, startY + 5);
     
     textAlign(CENTER, BOTTOM);
     text(value, startX + histW*1.0f/capacityCountSort.size()*ii, startY - heighti);
     }
          
     ii++;
   }
    
 
}


 
 
 
 
  //
  //save("test.jpg");
  //exit();
public class Disk implements Comparable<Disk>{
  String serial_number;
  String model;
  String smartVar;
  String capacity_bytes;
  
  boolean hasFailure;
  
  int startDay;
  int endDay;
  int diskIndex;
      
  int survTime;
  int diskSize;
  
  float windowHeight;
  
  float x;
  float y;
  float rad;
  float radInt;
  float r;
  
  
  ArrayList<Record> records = new ArrayList<Record>();
  
  float min_smart;
  float max_smart;
  
  

  
  Disk(String serial_number, String smartVar){
    this.serial_number = serial_number;
    this.smartVar = smartVar;
    this.hasFailure = false;
  }
  
  
  public void addRecord(Record newRecord){
    records.add(newRecord);
  }
  
  public ArrayList getRecords(){
    return records;
  }
  
  public int getSurvTime(){
    return this.survTime;
  }
  
  public void calculateSurvTime(){
    this.startDay = this.records.get(0).dayOfYear;
    this.endDay = this.records.get(this.records.size() - 1).dayOfYear;
    this.survTime = endDay - startDay;
    int i = 0;
    for(Record record : this.records){
      record.startDay = this.startDay;
      record.endDay = this.endDay;
      record.recordIndex = i;
      record.diskIndex = this.diskIndex;
       
      i++;
    }
  }

@Override  
  public int compareTo(Disk compareDisk){
    int compareTime = ((Disk) compareDisk).getSurvTime();
    
    //ascending order
    return this.survTime - compareTime;
  }
  
  public  Comparator<Disk> survTimeComparator 
  = new Comparator<Disk>(){
    public int compare(Disk disk1, Disk disk2){
      int diskSurvTime1 = disk1.getSurvTime();
      int diskSurvTime2 = disk2.getSurvTime();
      
      //ascending order
      //return diskSurvTime1.compareTo(diskSurvTime2);
      return Integer.compare(diskSurvTime1, diskSurvTime2);
      
  }
  };
  
  
  public void mapCoord(int min_day, int max_day, float min_smart, float max_smart, 
                        float xmin, float xmax, float ymin, float ymax){
    this.windowHeight = abs(ymax - ymin);
    if(drawLine == true){
      for(Record record : this.records){    
        record.mapCoord(min_day, max_day, min_smart, max_smart, xmin, xmax, ymin, ymax);      
      }
    }else{
      for(Record record : this.records){    
        record.mapCoord(min_day, max_day, min_smart, max_smart, xmin, xmax, ymin, ymax);      
      }
    }    
    
  }
  
  //draw a line for each disk
  public void displayLine(){
    //get min and max for the records
    for(int i = 0; i < this.records.size() - 1; i++){
      Record record1 = records.get(i);
      Record record2 = records.get(i+1);
      
      //draw line connecting records, if it has failure, draw blue line; if not draw grey line
      if(this.hasFailure == true){
         
           
           
         
        strokeWeight(2);
        //strokeWeight(this.windowHeight*1.0/this.diskSize);
        stroke(record2.recordColor);
        
         
        
        strokeCap(SQUARE);
         
        line(record1.x, record1.y, record2.x, record2.y);  
              
         
      }
      
      //draw red dot for failure record
      if(record2.failure == true){
        noStroke();
        fill(eventColor);
        ellipse(record2.x, record2.y, 5,5);
      }
      
    }
    
  }
  
  
  
    public void highlight(){
    //get min and max for the records
    for(int i = 0; i < this.records.size() - 1; i++){
      Record record1 = records.get(i);
      Record record2 = records.get(i+1);
      
      //draw line connecting records
       
        stroke(highLightColor);
        strokeWeight(2);
        strokeCap(SQUARE);
        line(record1.x, record1.y, record2.x, record2.y);   
       
      
      
      //highlight failure record
      if(record2.failure == true){
        noStroke();
        fill(eventColor);
        ellipse(record2.x, record2.y, 5,5);
      }
      
    }
    
  }
  
  
  //draw radius for each disk
  public void displaySpiral(){
    stroke(120);
    strokeWeight(1);
    strokeCap(SQUARE);
    line(width/2, height/2, width/2 + this.r*sin(this.rad), 
    height/2 - this.r*cos(this.rad));
  }
  
  
  //draw records for in spiral 
  public void displayRecords(){
    ArrayList<Record> records = this.getRecords();
    
    for(int i=records.size()-1; i>=0; i--){
      Record tempRecord = records.get(i);
      tempRecord.rInt = this.r / records.size();
      tempRecord.r = (i+1)*1.0f * tempRecord.rInt;
      tempRecord.rad = this.rad;
      tempRecord.radInt = this.radInt;
      tempRecord.displaySpiral();
      
      if(i==records.size()-1){
        tempRecord.displayBound();   
      } 
    }    
  }
  
  public void drawArc(int idx, int total, int recordIndex, int failIndex){
    
    //size of arc diameter
    float maxSize = min(width*1.0f/ncols, height*1.0f/nrows)*0.65f;
    int centerOffset = -45;
    
    stroke(120);
    strokeWeight(1);
    /*line(width*1.0/ncols * 2.5, height*1.0/nrows * 1.5, 
         width*1.0/ncols * 2.5 + 0.9*maxSize*sin(2*PI/total*idx ), 
         height*1.0/nrows * 1.5 - 0.9*maxSize*cos(2*PI/total*idx)); */
    
    
    int drawSize = this.records.size();
    for(int i = drawSize -1; i>=0; i--){
       
      
      if(i == drawSize -1){ 
        
        //out border
        stroke(0,0,0,120);
        strokeWeight(2);
        fill(this.records.get(i).recordColor);
      }else if(i == recordIndex ){
        
        //highlight mouse on record
        noFill();        
        stroke(highLightColor);
      }else if(i == failIndex){
        
        //fail record
        stroke(eventColor);
        strokeWeight(5);
        noFill();
      }else{
        
        //other records
        noStroke();
        fill(this.records.get(i).recordColor);
      }
 
 
      if(i == drawSize -1){
      arc(width*1.0f/ncols * 2.5f, 600*1.0f/nrows * 1.5f + yOffset + centerOffset, 
      0.9f*maxSize/drawSize*(i+1),  0.9f*maxSize/drawSize*(i+1), 
      2*PI/total*idx - PI/2, 2*PI/total*(idx+1) - PI/2, PIE);
      }else{
       arc(width*1.0f/ncols * 2.5f, 600*1.0f/nrows * 1.5f + yOffset + centerOffset, 
      0.9f*maxSize/drawSize*(i+1),  0.9f*maxSize/drawSize*(i+1), 
      2*PI/total*idx - PI/2, 2*PI/total*(idx+1) - PI/2);
      }
      
    
    }
    
    String s = this.smartVar;
    textSize(10);
    fill(fontColor);
    textAlign(CENTER, BOTTOM);
    text(s, 
    width*1.0f/ncols * 2.5f + 0.9f*maxSize*sin(2*PI/total*(idx + 0.5f))/2 , 
    600*1.0f/nrows * 1.5f - 0.9f*maxSize*cos(2*PI/total*(idx + 0.5f))/2 + yOffset+centerOffset);

  }

  
}
public class Record  {
  String date;
  String smartVar; 
  String model;
  String capacity_bytes;
  
  int year;
  int month;
  int day;
  int dayOfYear;
  String serial_number;
  
  int startDay;
  int endDay;
  int recordIndex;
  int diskIndex;
   
  //in normal mode
  float x;
  float y;
  
  int recordColor;
  int diskSize;
  
  //in spiral mode
  float rad;
  float radInt;
  float r;
  float rInt;
  
 
  
  boolean failure;
  
  
 
  
  float smart_raw;
  
  Record( String serial_number, String date, String smartVar, String model){
    this.serial_number = serial_number;
    this.date = date;
    this.smartVar = smartVar;
    this.model = model;
   
    this.failure = false;
    
    String dateAll[] = split(date, "-");
    this.year = Integer.parseInt(dateAll[0]);
    this.month = Integer.parseInt(dateAll[1]);
    this.day = Integer.parseInt(dateAll[2]);
     
    Date newDate = new Date(this.year,this.month-1,this.day);
    SimpleDateFormat ft = new SimpleDateFormat("D");
    this.dayOfYear = Integer.parseInt(ft.format(newDate));
    
    //println("date = " + date + " month = " + month + " day = "+ day + 
    //" --> dayOfYear = " + ft.format(newDate));
    
  }
  
  
  public void mapCoord(int min_day, int max_day, float min_smart, float max_smart, 
                        float xmin, float xmax, float ymin, float ymax){
      if(drawLine == true){
        this.x = map(this.dayOfYear, min_day, max_day, xmin, xmax);
        this.y = map(this.smart_raw, min_smart, max_smart, ymin, ymax);
      }else{
        this.x = map(this.dayOfYear - this.startDay, 0, 365, xmin, xmax);       
        this.y = map(this.diskIndex, 0, this.diskSize, ymax, ymin);         
        //this.recordColor = color(map(this.smart_raw, min_smart, max_smart, 255,0),
        //map(this.smart_raw, min_smart, max_smart, 255,0),255);
         this.recordColor = color(map(this.smart_raw, min_smart, max_smart, 255, red(lineColor)),
           map(this.smart_raw, min_smart, max_smart, 255, green(lineColor)),
           map(this.smart_raw, min_smart, max_smart, 255, blue(lineColor)));
      }  
 
      
    
    
  }
  
  public void display(){
    
    noStroke();
    if(this.failure ==false){
      fill(0,0,0);
    }else{
      fill(eventColor);
    }
     
    ellipse(this.x, this.y, 1,1);
  }
  
  public void displaySpiral(){
    
    //float c = map(this.smart_raw, this.min_smart, max_smart, 255, 0);
    float c = 0;
    
    if(this.failure == false){
      noStroke();
      fill(c,c,255);
    }else{
      stroke(eventColor);
      strokeWeight(5);
      fill(eventColor);
    }
 
    arc(width/2, height/2, this.r*2, this.r*2, this.rad - PI/2, 
    (this.rad - PI/2+this.radInt));
    /*
    fill(255,255,255);
    arc(width/2, height/2, (this.r-this.rInt)*2, (this.r-this.rInt)*2, this.rad - PI/2, 
    (this.rad - PI/2+this.radInt));
    */
    
    
  }
  
  public void displayBound(){
    stroke(120,50);
    strokeWeight(2);
    arc(width/2, height/2, this.r*2, this.r*2, this.rad - PI/2, 
    (this.rad - PI/2+this.radInt));  
    strokeCap(SQUARE);
    line(width/2, height/2, width/2 + this.r*sin(this.rad), 
    height/2 - this.r*cos(this.rad));    
    line(width/2, height/2, width/2 + this.r*sin(this.rad + this.radInt), 
    height/2 - this.r*cos(this.rad + this.radInt));  
  }
  
  
  public void highlight(){
    
  }
  
 
  public boolean mouseOnRecord(){
    float dist = sqrt( (this.x - mouseX)*(this.x - mouseX) + 
                       (this.y - mouseY)*(this.y - mouseY) );
    return dist < 5;                     
    
  }
  
  public void showOnLegend(float px, float py){
    int showColor = highLightColor;
    stroke(showColor);
    strokeWeight(2);
    line(px,py,px,py+10);
  }
  

  public void drawMouse(){
    String s = "Serial = " + this.serial_number + "\n"
    + "Model = " + this.model + "\n"
    + "Capacity = " + this.capacity_bytes + " GB \n";
    
    fill(fontColor); 
    textAlign(LEFT, BOTTOM);
    text(s, mouseX, mouseY);
  }
 
    
   
  
}
  
public class Smart {
  String smartVar;
 
 
  
  int min_day;
  int max_day;
  
  float min_smart;
  float max_smart;
  
  float xmin,xmax,ymin,ymax;

  Map<String, ArrayList<Record>> diskRecords = new TreeMap<String, ArrayList<Record>>();

  
  ArrayList<Disk> disks;
  
  Smart(String smartVar){
    this.smartVar = smartVar; 
    this.disks = new ArrayList<Disk>();  
    
    this.min_day = 1;
    this.max_day = 365;
    
    this.max_smart = Float.MIN_VALUE;
    this.min_smart = Float.MAX_VALUE;
  }
  
 
  
  public void calculate(){
    int i = 0;
    for(Disk disk : this.disks){
      disk.diskIndex = i;
      disk.diskSize =(int) this.disks.size();
      int j = 0;
      for(Record record : disk.records){
        record.diskSize = disk.diskSize;
        record.diskIndex = i;
        record.recordIndex = j;
        j++;
      }
      i++;
    }
  }
  
  public void mapCoord(float xmin, float xmax, float ymin, float ymax){
    this.xmin = xmin;
    this.ymin = ymin;
    this.xmax = xmax;
    this.ymax = ymax;
    for(Disk disk : this.disks){     
      disk.mapCoord(this.min_day, this.max_day, this.min_smart, this.max_smart, xmin, xmax, ymin, ymax);      
    }    
  }
  
 public void drawLegend() {
  int c1 = color(255,255,255);
  int c2 = lineColor;
  int axis = 2;
  int x=(int) this.xmin;
  int w=round( this.xmax - this.xmin);
  int y=(int) this.ymin + 30;
  float h= 10;
  
  
  noFill();
  strokeWeight(1);

  if (axis == 1) {  // Top to bottom gradient
    for (int i = y; i <= y+h; i++) {
      float inter = map(i, y, y+h, 0, 1);
      int c = lerpColor(c1, c2, inter);
      stroke(c);
      strokeCap(SQUARE);
      line(x, i, x+w, i);
    }
  }  
  else if (axis == 2) {  // Left to right gradient
    for (int i = x; i <= x+w; i++) {
      float inter = map(i, x, x+w, 0, 1);
      int c = lerpColor(c1, c2, inter);
      stroke(c);
      strokeCap(SQUARE);
      line(i, y, i, y+h);
    }
  }
  
  stroke(0);
  strokeWeight(1);
  for(int i=0; i<5;i++){
    strokeCap(SQUARE);
    line(x+w*1.0f*i/4, y, x+w*1.0f*i/4, y+10);
    textSize(abs(this.ymin - this.ymax)/20);
    fill(fontColor);
    textAlign(CENTER, BOTTOM);
    text(round(exp((this.max_smart-this.min_smart)/4*i + this.min_smart) -1 ),x+w*1.0f*i/4, y);
  }
  
}


  public boolean mouseOnSmart(){
    //mouseY's min and max are inversed because of drawing order
    return (mouseX >= this.xmin && mouseX <= this.xmax && mouseY <= this.ymin && mouseY >= this.ymax);
     
    
  }
  
  public void display(){
    for(Disk disk : this.disks){
      disk.displayLine();
    }      
  }
  
  public void displayName(){
    textSize(abs(this.ymin - this.ymax)/20);
    fill(fontColor);
    textAlign(LEFT, BOTTOM);
    text(this.smartVar,this.xmin,this.ymax);
  }
  
  
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Hard_disk" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
