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
  
  color recordColor;
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
  
  void display(){
    
    noStroke();
    if(this.failure ==false){
      fill(0,0,0);
    }else{
      fill(eventColor);
    }
     
    ellipse(this.x, this.y, 1,1);
  }
  
  void displaySpiral(){
    
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
  
  void displayBound(){
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
    color showColor = highLightColor;
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
  
