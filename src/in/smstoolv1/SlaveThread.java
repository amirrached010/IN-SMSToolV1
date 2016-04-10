/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package in.smstoolv1;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.Callable;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.Logger;




/**
 *
 * @author amir.rashed
 */
public class SlaveThread implements Runnable {
    
    File currentFile;
    String counter;
    Properties properties;
    String tool="";
    Logger logger;
    String appenderName;
    public SlaveThread(File newFile,String counter,Properties properties){
        
        this.counter = counter;
        this.properties = properties;
        intializeLogger();
        String filePath = (moveFile(newFile, Globals.WORK_DIRECTORY));
        currentFile = new File(filePath);
    }
    
    public void execute(){

        //process the file.
        
        try{
            if(currentFile == null || !currentFile.isFile())
                throw new Exception();
            File newFile = currentFile;
            processFile(newFile);
            //archive the file.
            archiveFile(newFile);
        }catch(Exception e){
            logger.error("Exception in reading file" + Globals.WORK_DIRECTORY+currentFile.getName());
        }   
        stopThread();
//        try{
//            throw new Exception();
//        }catch(Exception e){
//            logger.error("Execution stopped for file : " + Globals.WORK_DIRECTORY+currentFile.getName());
//        }        
    }

    public void stopThread(){
        logger.debug("Thread Stopped for file : "+ currentFile.getName());
        logger.debug("------------------------------------------------------------------------------------------------");
        logger.getAppender(appenderName).close();
        //LogManager.shutdown();
    }
    
    public  void intializeLogger(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        RollingFileAppender appender = new RollingFileAppender();
        
        appender.setAppend(true);
        appender.setMaxFileSize("1MB");
        appender.setMaxBackupIndex(1);
        String fileName="logs/IN_SMSTool_ThreadLog_"+dateFormat.format(new Date())+"_"+this.counter;
        appenderName = fileName;
        appender.setName(appenderName);
        logger = Logger.getLogger(fileName);    
        appender.setFile(fileName + ".log");
        PatternLayout layOut = new PatternLayout();
        layOut.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} %-5p :%L - %m%n");
        appender.setLayout(layOut);
        appender.activateOptions();
        logger.removeAllAppenders();
        logger.addAppender(appender);
        logger.debug("Log appended : " + fileName);
        
    }

    public  void processFile(File workFile){
        FileInputStream inputStream = null;
        Scanner sc = null;
        try {
            inputStream = new FileInputStream(workFile.getAbsolutePath());
            sc = new Scanner(inputStream, "UTF-8");
            logger.debug("stating to processing File "+workFile.getName()+" line by line ");
            int linecounter = 1;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                try{
                    if(workFile.getName().startsWith("INAI")){
                        processLineOldINSMSTraffic(line,linecounter);
                        linecounter++;
                    }
                    else
                    {
                        if(workFile.getName().startsWith("HANOI")){
                            processLineHanoi(line,linecounter);
                            linecounter++;
                        }
                        else{
                            logger.error("Unconfigured product : " + workFile.getName());
                        }
                    }
                }catch(Exception ex){
                    logger.error("Processing line failed for line : " + line);
                }
            }
            // note that Scanner suppresses exceptions
            if (sc.ioException() != null) {
                throw sc.ioException();
            }
        } 
        catch (FileNotFoundException ex) {
            logger.error("Method : processFile");
            logger.error("File not found exception for file : " + workFile.getAbsolutePath());
        }
        catch(IOException ex){
            logger.error("Method : processFile");
            logger.error("Error in parsing file : " + workFile.getAbsolutePath());
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                    logger.error("Method : processFile");
                    logger.error("Error in closing input stream while parsing file : " + workFile.getAbsolutePath());
                }
            }
            if (sc != null) {
                sc.close();
                logger.debug("Scanner is closing for file : "+workFile.getName());
                
            }
        }
    }

    public  void processLineOldINSMSTraffic(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        OldINSMSTrafficCDR currentCDR = new OldINSMSTrafficCDR(lineFields);
        if(currentCDR.getRef_counter_aft().equals("0.000000"))
            currentCDR.setRef_counter_aft("0");
        
        
        
        String key = currentCDR.getPp_b4()+","+currentCDR.getPp_after()+","+currentCDR.getRef_counter_aft()+",";
        
        String value = properties.getProperty(key);
        
        if(value == null){
            logger.error("the MSISDN : "+currentCDR.getMsisdn()+"   .. the value for the key: "+ key +" does not exist in config file.");
        }
        else {
           String [] valueString = value.split(",");
           if(valueString.length < 3)
               logger.error("the MSISDN"+currentCDR.getMsisdn()+"wrong config format for the configuration: "+ key);
           else {
               String engSMS = valueString[0];
               String arabSMS = valueString[1];
               String name = valueString[2];
               logger.info("the MSISDN : "+currentCDR.getMsisdn()+"   .. the value for the key: "+ key);
               sendSMS(currentCDR.getMsisdn()+","+arabSMS+",2\n",lineCounter);
           }
        }
    }
    
    public  void processLineHanoi(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        // handle the Hanoi CDR.
        String msisdn= lineFields[0];
        String timeStamp= lineFields[1];
        String OfferId= lineFields[2];
        String ProjectName= lineFields[lineFields.length-1];
        
        if(!ProjectName.startsWith("HANOI")){
            logger.error("Not a Hanoi CDR : "+ line);
            return;
        }            
        
        String key = "HANOI,"+OfferId+",";
        String value = properties.getProperty(key);
         if(value == null){
            logger.error("the MSISDN : "+msisdn+"   .. the value for the key: "+ key +" does not exist in config file.");
        }
        else {
             String sms=msisdn+","+value+",2\n";
             logger.info("the MSISDN : "+msisdn+"   .. the value for the key: "+ key);
             sendSMS(sms,lineCounter);
         }
        
    }

    public  void sendSMS(String toString,int lineCounter) {
        Writer writer = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentFileName ="";
        File resultFile = null;
        File newFile  = null;
        if(Util.getOSType() == Globals.OS_UNIX){
            currentFileName = Globals.SMS_PREPARATION_DIRECTORY+"Revamp_IN_SMSTool_"+sdf.format(new Date())+"_L"+lineCounter+"_V"+this.counter+".txt";
            resultFile = new File(currentFileName);
            newFile = new File(Globals.SMS_DIRECTORY+resultFile.getName());
                    
        }
        else{
            currentFileName = Globals.SMS_PREPARATION_DIRECTORY+"Revamp_IN_SMSTool_"+sdf.format(new Date())+"_L"+lineCounter+"_V"+this.counter+".txt";
            resultFile = new File(currentFileName);
            newFile = new File(Globals.SMS_DIRECTORY+resultFile.getName());
        }
        
        
        if(!resultFile.exists())
            try {
                resultFile.createNewFile();
        } catch (IOException ex) {
           logger.error("Cannot create the SMS file in the SMS Preparation Directory: "+ currentFileName);
        }
        try {
            FileWriter fw = new FileWriter(resultFile,true);
            //BufferedWriter writer give better performance
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(toString);
            bw.close();
            // Send the file to the SMS tool
            //logger.info("Deleting moved file : "+ newFile.delete());
            try{
            Files.move(Paths.get(resultFile.getAbsolutePath()), Paths.get(newFile.getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
            logger.info("File "+ newFile.getAbsolutePath()+" is moved to the SMS Directory");
            }catch(Exception e){
                logger.error("Failed to move File "+ resultFile.getAbsolutePath()+" to the SMS Directory : " + Globals.SMS_DIRECTORY + " under name : " + newFile.getName());
                logger.error("Exception : "+ e);
            }    
//            if(resultFile.renameTo(newFile)){
//              logger.info("File "+ newFile.getAbsolutePath()+" is moved to the SMS Directory");
//            }
//            else {
//               logger.error("Failed to move File "+ resultFile.getAbsolutePath()+" to the SMS Directory : " + Globals.SMS_DIRECTORY + " under name : " + newFile.getName());
//            }
        } catch (UnsupportedEncodingException ex) {
            logger.error("Error in writing in file : "+ currentFileName);
        } catch (FileNotFoundException ex) {
            logger.error("File not found : "+ currentFileName);
        } catch (IOException ex) {
            logger.error("IO Exception: "+ currentFileName);;
        }
    }
    
    public  StringBuilder archiveFile(File currentFile){
        File newFile = null;
        StringBuilder resultBuilder = new StringBuilder();
        SimpleDateFormat  sdf1 = new SimpleDateFormat("yyyyMMdd");
        if(Globals.IS_OSWIN){
            File file = new File (Globals.ARCHIVE_DIRECTORY+sdf1.format(new Date()));
            if (!file.exists()) {
		if (file.mkdir()) {
			logger.debug("Directory "+file.getAbsolutePath()+" is created!");
                        newFile = new File(file.getAbsolutePath()+"\\"+currentFile.getName());
		} else {
			logger.error("Failed to create directory "+ file.getAbsolutePath());
		}
            }
            else {
                logger.debug("Directory "+file.getAbsolutePath()+" already exists");
                newFile = new File(file.getAbsolutePath()+"\\"+currentFile.getName());
            }
        }
        else {
            File file = new File (Globals.ARCHIVE_DIRECTORY+sdf1.format(new Date()));
            if (!file.exists()) {
		if (file.mkdir()) {
			logger.debug("Directory "+file.getAbsolutePath()+" is created!");
                        newFile = new File(file.getAbsolutePath()+"/"+currentFile.getName());
		} else {
			logger.error("Failed to create directory "+ file.getAbsolutePath());
		}
            }
            else {
                logger.debug("Directory "+file.getAbsolutePath()+" already exists");
                newFile = new File(file.getAbsolutePath()+"/"+currentFile.getName());
            }
        }
        
        newFile.delete();
        if(currentFile.renameTo(newFile)){
           
            logger.debug("File "+ currentFile.getAbsolutePath() + " is moved to " + newFile.getAbsolutePath());
            logger.debug("work file "+newFile.getName()+" is archived");
        }
        else {
            logger.error("Failed to move "+ currentFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
            logger.debug("work file "+newFile.getName()+" was not archived");
        }
        
        return resultBuilder;
    }

    @Override
    public void run()  {
       execute();      
    }

    public String moveFile(File file, String destination){
        File directory = new File(destination);
        if(!directory.isDirectory()){
            logger.error("Not a directory");
            return "Not a directory";
        }
        if(!file.isFile()){
            logger.error("Not a File");
            return "Not a File";
        }
        File newFile =null;
        if(Globals.IS_OSWIN){
            newFile = new File(destination+"\\"+file.getName());
        }
        else {
            newFile = new File(destination+"/"+file.getName());
        }
        newFile.delete();
        if(file.renameTo(newFile)){
            logger.debug("File "+ file.getName() +" is moved to "+newFile.getAbsolutePath());
            return newFile.getAbsolutePath();
        }
        else {
            logger.debug("Failed to move File "+ file.getName() +" to "+newFile.getAbsolutePath());
            return "Failed to move";
        }
    }
    
    
}
