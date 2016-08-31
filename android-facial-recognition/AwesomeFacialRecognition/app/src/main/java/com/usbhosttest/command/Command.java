package com.usbhosttest.command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Command {

    private String procedure;
    private List<String> parameters;

    public String getProcedure() {
        return this.procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public Command(){
        super();
        this.procedure = "";
        this.parameters = new ArrayList<String>();
    }

    public Command(String procedure){
        super();
        this.procedure = procedure;
        this.parameters = new ArrayList<String>();
    }


    public Command(String procedure, List<String> parameters){
        super();
        this.procedure = procedure;
        this.parameters = new ArrayList<String>(parameters);
    }

    public Command(String procedure, String[] parameters){
        super();
        this.procedure = procedure;
        this.parameters = new ArrayList<String>();
        for (int i=0; i<parameters.length; i++){
            this.parameters.add(parameters[i]);
        }
    }


    //^Command,Param1,Param2|
    public static Command bytesToCommand(byte[] bytes) throws IOException {

        String str = new String(bytes);

        int startIndex = str.indexOf('^');
        int endIndex = str.indexOf('|');

        if (startIndex==-1) { throw new IOException("Could not identify start index"); }
        if (endIndex==-1) { throw new IOException("Could not identify end index"); }

        str = str.substring(startIndex+1, endIndex);

        int delimiterIndex = str.indexOf(',');
        if (delimiterIndex==-1) delimiterIndex = str.length();

        String procedure = str.substring(0, delimiterIndex);

        List<String> parameters = new ArrayList<String>();
        while(delimiterIndex<str.length()){
            str = str.substring(delimiterIndex+1, str.length());
            delimiterIndex = str.indexOf(',');
            if (delimiterIndex==-1) delimiterIndex = str.length();
            String parameter = str.substring(0, delimiterIndex);
            parameters.add(parameter);
        }

        Command command = new Command(procedure, parameters);
        return command;
    }


    public static byte[] commandToBytes(Command command) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeBytes("^");
        out.writeBytes(command.getProcedure());
        for(String param : command.getParameters()){
            out.writeBytes(",");
            out.writeBytes(param);
        }
        out.writeBytes("|");
        out.close();
        byte[] bytes = bos.toByteArray();
        bos.close();

        String cleanupStr = new String(bytes);
        cleanupStr = cleanupStr.substring(cleanupStr.indexOf("^"),cleanupStr.length());
        cleanupStr += "\r\n";
        bytes = cleanupStr.getBytes();

        return bytes;
    }

}
