#include "Arduino.h"

class Command
{
  private:
    String _procedure;
    String* _parameters;
    
  public:
 
  Command(){
    _procedure = "";
    _parameters = NULL;
  }
 
 
  Command(String procedure, String* parameters, int paramArraySize){
    _procedure = procedure;
    if (paramArraySize>0){
      _parameters = new String[paramArraySize];
      for(int i=0; i<paramArraySize; i++){
        _parameters[i] = String(parameters[i]);
      }
    } else{
      _parameters = NULL;
    }
  }
  
  virtual ~Command(void){
    if (_parameters!=NULL){
      delete(_parameters);
    }
  }
  
  String getProcedure() {return _procedure;}
  String* getParameters() {return _parameters; }
  
  static Command* makeCommandFromStr(String str){
    int startIndex = str.indexOf('^');
    int endIndex = str.indexOf('|'); 
    str = str.substring(startIndex+1, endIndex);
    
    if ((startIndex>0)||(endIndex>0)){
      int delimiterIndex = str.indexOf(',');
      if (delimiterIndex==-1) { delimiterIndex = str.length(); }
      
      String procedure = str.substring(0, delimiterIndex);
    
      //Pre-processing to caclulate parameter count
      int paramCount = 0;
      String tmp = str;
      
      while(tmp!=""){
        int next = tmp.indexOf(',');
        if (next!=-1){
          tmp = tmp.substring(next+1, tmp.length());
          paramCount++;
        } else{
          tmp = ""; 
      }
    }
    
    String* param = NULL;
    
    int paramCounter = 0;
    if (paramCount>0){
      param = new String[paramCount];
      while(delimiterIndex<str.length()){
        str = str.substring(delimiterIndex+1, str.length());
        delimiterIndex = str.indexOf(',');
        if (delimiterIndex==-1) delimiterIndex = str.length();
        String parameter = str.substring(0, delimiterIndex);
        param[paramCounter] = parameter;
        paramCounter++;
      }
    }
    
    Command* command = new Command(procedure, param, paramCount);
    
    if (param!=NULL) {delete(param); }
     return command;
    } else{
      return NULL;
    }

  }
};
