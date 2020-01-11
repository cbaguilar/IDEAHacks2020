#define pot 11
#define motor 15
int val;
void setup() {
  pinMode(pot, INPUT);
  pinMode(motor, OUTPUT);
  Serial.begin(9600);
 }

void loop() {
  val= analogRead(pot);
  Serial.println(val);
  if (val >=0 && val <=511.5){
    digitalWrite(motor, LOW);
  }
  else if (val >511.5 && val <=1023){
    digitalWrite(motor, HIGH);
  }
}
