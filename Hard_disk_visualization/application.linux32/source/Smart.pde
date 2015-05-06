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
  
 void drawLegend() {
  color c1 = color(255,255,255);
  color c2 = lineColor;
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
      color c = lerpColor(c1, c2, inter);
      stroke(c);
      strokeCap(SQUARE);
      line(x, i, x+w, i);
    }
  }  
  else if (axis == 2) {  // Left to right gradient
    for (int i = x; i <= x+w; i++) {
      float inter = map(i, x, x+w, 0, 1);
      color c = lerpColor(c1, c2, inter);
      stroke(c);
      strokeCap(SQUARE);
      line(i, y, i, y+h);
    }
  }
  
  stroke(0);
  strokeWeight(1);
  for(int i=0; i<5;i++){
    strokeCap(SQUARE);
    line(x+w*1.0*i/4, y, x+w*1.0*i/4, y+10);
    textSize(abs(this.ymin - this.ymax)/20);
    fill(fontColor);
    textAlign(CENTER, BOTTOM);
    text(round(exp((this.max_smart-this.min_smart)/4*i + this.min_smart) -1 ),x+w*1.0*i/4, y);
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
