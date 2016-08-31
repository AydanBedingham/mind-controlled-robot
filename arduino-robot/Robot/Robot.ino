#include "Command.h"
#include "AbstractUsbListener.h"
#include <AFMotor.h>

AF_DCMotor motor1(1, MOTOR12_64KHZ); // set up motors.
AF_DCMotor motor2(2, MOTOR12_8KHZ);

enum MovementInfo {
  miNone,
  miTurnLeft,
  miTurnRight,
  miMoveForward
};

AbstractUsbListener* usbListener;

MovementInfo shouldMove = miNone;

boolean timerStarted = false;
unsigned long timer0;
unsigned long timer_interval = 2000;

//------------------------------------------------

void stopMoving(){
  motor1.run(RELEASE);
  motor2.run (RELEASE);
}

void turnLeft(){
  motor1.setSpeed(170);
  motor2.setSpeed(170);
  motor1.run(FORWARD);
  motor2.run (BACKWARD);
}

void turnRight(){
  motor1.setSpeed(170);
  motor2.setSpeed(170);
  motor1.run(BACKWARD);
  motor2.run (FORWARD);
}

void moveForward(){
  motor1.setSpeed(220);
  motor2.setSpeed(220);
  motor1.run(FORWARD);
  motor2.run (FORWARD);
}

//------------------------------------------------

void setTimer(unsigned long interval){
  timer_interval = interval;
  timer0 = millis();
  timerStarted = true;
}

void checkTimer(){
  if (timerStarted){
    if ((millis()-timer0) > timer_interval) {
      stopMoving();
      shouldMove = miNone;
      timerStarted = false;
    }
  } 
}

//------------------------------------------------


void processCommand(Command* command){
  if (!timerStarted){
    if (command->getProcedure()=="TURN_LEFT"){
      shouldMove = miTurnLeft;
      setTimer(500);
    } else if (command->getProcedure()=="TURN_RIGHT"){
      shouldMove = miTurnRight;
      setTimer(500);
    } else if (command->getProcedure()=="MOVE_FORWARD"){
      shouldMove = miMoveForward;
      setTimer(1000);
    }
  }
  
  if (command->getProcedure()=="DEBUG"){
    String str = usbListener->getHistory();
    str.replace('^', '[');
    str.replace('|', ']');
    str.replace(',', ' ');
    Serial.println("^PRINT," + str + "|");
  }
    
}

class UsbListenerA : public AbstractUsbListener{
  private:
  public:
  UsbListenerA() :  AbstractUsbListener(){}
  virtual ~UsbListenerA(void) {}
  
  virtual void interpretCommand(Command* command){
    processCommand(command);
    

  }
};

//------------------------------------------------

void setup() {
  Serial.begin(9600);
  
  //set the speed of the motors, between 0-255
  motor1.setSpeed(150);
  motor2.setSpeed(150);
  
  usbListener = new UsbListenerA();
  timerStarted = false;
  
  //String line = "^MOVE_FORWARD|";
  //Command* command = Command::makeCommandFromStr(line);
  //processCommand(command);
}

void loop() {  
  usbListener->step();
   
  if (shouldMove == miTurnLeft) turnLeft();
  if (shouldMove == miTurnRight) turnRight();
  if (shouldMove == miMoveForward) moveForward();
  
  checkTimer();
  
  delay(10);
}


