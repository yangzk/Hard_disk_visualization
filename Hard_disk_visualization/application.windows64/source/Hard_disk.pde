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
 
//border space, % of the block width and height
float vertiSpace = 0.05;
float horizSpace = 0.05;


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
color lineColor = color(0,0,0);

color eventColor = color(182,73,38);
color highLightColor = color(209,173,27);

color fontColor = color(10,58,64);



//color backColor = color(147,253,249);
color backColor = color(189,187,145);



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
 
void setup(){
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
    tempSmart.mapCoord(width*1.0/ncols*n_col + width*1.0/ncols*horizSpace, 
                       width*1.0/ncols*(n_col+1) - width*1.0/ncols*horizSpace, 
                       600*1.0/nrows*(n_row+1) - 600*1.0/nrows*vertiSpace - 30 + yOffset, 
                       600*1.0/nrows*(n_row) + 600*1.0/nrows*vertiSpace + yOffset ); 
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
 
   
  
   
   
   
   
   



void draw(){
   
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
   

void drawCapacityHistogram(){
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
     
     strokeWeight(histW/capacityCountSort.size()*0.9);
     stroke(0);
 
     line(startX + histW*1.0/capacityCountSort.size()*ii, startY, 
          startX + histW*1.0/capacityCountSort.size()*ii, startY - heighti);
          
     if(dist(mouseX, mouseY, 
     startX + histW*1.0/capacityCountSort.size()*ii, startY ) < 5 ){
       fill(fontColor); 
       textAlign(CENTER, TOP);
      
       text(key + " GB", startX + histW*1.0/capacityCountSort.size()*ii, startY + 5);
     
       textAlign(CENTER, BOTTOM);
       text(value, startX + histW*1.0/capacityCountSort.size()*ii, startY - heighti);
     
     }      
     ii++;
   }
   

}


void highlightCapacityHistogram(String selectedCapacity){
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
     strokeWeight(histW/capacityCountSort.size()*0.9);
     
     stroke(highLightColor);
 
     line(startX + histW*1.0/capacityCountSort.size()*ii, startY, 
          startX + histW*1.0/capacityCountSort.size()*ii, startY - heighti);
          
     
     fill(fontColor); 
     textAlign(CENTER, TOP);
      
     text(key + " GB", startX + histW*1.0/capacityCountSort.size()*ii, startY + 5);
     
     textAlign(CENTER, BOTTOM);
     text(value, startX + histW*1.0/capacityCountSort.size()*ii, startY - heighti);
     }
          
     ii++;
   }
    
 
}


 
 
 
 
  //
  //save("test.jpg");
  //exit();
