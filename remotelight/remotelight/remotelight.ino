//This example code is in the Public Domain (or CC0 licensed, at your option.)
//By Evandro Copercini - 2018
#define LED 5
#define motor1a 13
#define motor1b 12

#define motor2a 14
#define motor2b 27



//This example creates a bridge between Serial and Classical Bluetooth (SPP)
//and also demonstrate that SerialBT have the same functionalities of a normal Serial


#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

void setup() {
  pinMode(LED, OUTPUT);
  pinMode(motor1a, OUTPUT);
  pinMode(motor1b, OUTPUT);

  pinMode(motor2a, OUTPUT);
  pinMode(motor2b, OUTPUT);

  
  digitalWrite(motor1a, HIGH);
  digitalWrite(motor1b, LOW);
  digitalWrite(motor2a, HIGH);
  digitalWrite(motor2b, LOW);

  
  //digitalWrite(LED, HIGH);
  Serial.begin(115200);
  SerialBT.begin("ESP32test"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
}

void loop() {
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  if (SerialBT.available()) {
    char command = SerialBT.read();
    Serial.write(command);
    digitalWrite(LED,(command=='0'));
  }
  delay(20);
}
