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
      tempRecord.r = (i+1)*1.0 * tempRecord.rInt;
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
    float maxSize = min(width*1.0/ncols, height*1.0/nrows)*0.65;
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
      arc(width*1.0/ncols * 2.5, 600*1.0/nrows * 1.5 + yOffset + centerOffset, 
      0.9*maxSize/drawSize*(i+1),  0.9*maxSize/drawSize*(i+1), 
      2*PI/total*idx - PI/2, 2*PI/total*(idx+1) - PI/2, PIE);
      }else{
       arc(width*1.0/ncols * 2.5, 600*1.0/nrows * 1.5 + yOffset + centerOffset, 
      0.9*maxSize/drawSize*(i+1),  0.9*maxSize/drawSize*(i+1), 
      2*PI/total*idx - PI/2, 2*PI/total*(idx+1) - PI/2);
      }
      
    
    }
    
    String s = this.smartVar;
    textSize(10);
    fill(fontColor);
    textAlign(CENTER, BOTTOM);
    text(s, 
    width*1.0/ncols * 2.5 + 0.9*maxSize*sin(2*PI/total*(idx + 0.5))/2 , 
    600*1.0/nrows * 1.5 - 0.9*maxSize*cos(2*PI/total*(idx + 0.5))/2 + yOffset+centerOffset);

  }

  
}
