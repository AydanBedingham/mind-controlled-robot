#include "Arduino.h"

class AbstractUsbListener
{
  
  private:
  
    String serialStringBufferHistory = "";
    String serialStringBuffer = "";
  
    String serial_readLine(){
      if (Serial.available()>0){
        boolean newLine = false;
        while (Serial.available()) {
          char inChar = (char)Serial.read();
          if (inChar == '\n') {
            String line = serialStringBuffer;
            addToSerialBufferHistory(line);
            serialStringBuffer = "";
            return line;
          }
          serialStringBuffer += inChar;
        }
      }
      return "";
    }
    
    void addToSerialBufferHistory(String str){
      int bCap = 200;
      
      serialStringBufferHistory = str + serialStringBufferHistory;
      
      if (serialStringBufferHistory.length()>bCap){
        serialStringBufferHistory = serialStringBufferHistory.substring(0, bCap);
      }
    }
    
    virtual void interpretCommand(Command* command) = 0;
    /*
    void interpretCommand(Command* command);{
      
      if (command->getProcedure()=="SET_LIGHT"){
        int value = (command->getParameters()[0]).toInt();
        if (value>0){
          //digitalWrite(LED, HIGH); 
        } else{
          //digitalWrite(LED, LOW); 
        }
      } else if (command->getProcedure()=="DEBUG"){
        String str = serialStringBufferHistory;
        str.replace('^', '[');
        str.replace('|', ']');
        str.replace(',', ' ');
        Serial.println("^PRINT," + str + "|");
      }
    }
    */

  
  public:
  
    String getHistory() {return serialStringBufferHistory;}
  
    AbstractUsbListener(){
      //
    }
    
    virtual ~AbstractUsbListener(void){
      // 
    }
    
    void step(){
      //Read
      String line = serial_readLine();
      while(line.length()>0){
        //Serial.println("Found Line: "+line);
        Command* command = Command::makeCommandFromStr(line);
        if (command!=NULL){
          interpretCommand(command);
          delete(command);
        } else{
          Serial.println("FAILED to generate valid command object");
        }
        line = serial_readLine();
      }
    }
  
};
