
package in.smstoolv1;


import com.etisalatmisr.smpp.SMSSender;
import com.etisalatmisr.smpp.SMSSender.*;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.Logger;




/**
 * This class is the runnable thread called with each file assigned from the main class
 * based on the criteria implemented in the Handle file method in the main class.
 * Implements the interface Runnable.
 */
public class SlaveThread implements Runnable {
    
    File currentFile;
    String counter;
    Properties properties;
    String tool="";
    Logger logger;
    String appenderName;
    SMSSender smscSender;
    /**
     * Initialize the passed parameters.
     * Initialize the logger for the current thread.
     * Move the input file to the working directory.
     * @param newFile
     * @param counter
     * @param properties
     * @param smscSender 
     */
    public SlaveThread(File newFile,String counter,Properties properties,SMSSender smscSender){
        
        this.counter = counter;
        this.properties = properties;
        intializeLogger();
        String filePath = (moveFile(newFile, Globals.WORK_DIRECTORY));
        currentFile = new File(filePath);
        this.smscSender=smscSender;;
    }
    
    /**
     * Handle the path taken by the file since the start of its execution
     * till the end.
     */
    public void execute(){

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

    /**
     * Frees the appender of the logger by closing it.
     */
    public void stopThread(){
        logger.debug("Thread Stopped for file : "+ currentFile.getName());
        logger.debug("------------------------------------------------------------------------------------------------");
        logger.getAppender(appenderName).close();
        //LogManager.shutdown();
    }
    
    /**
     * Initialize the logger for the current thread.
     */
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
    
    /**
     * Processes the working file by process each line one by one.
     * based on the file name, it distribute the lines to the responsible method to handle them.
     * Logs an error if the file name does not start with a pre configured name.
     * @param workFile 
     */
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
                            if(workFile.getName().startsWith("RENELY")){
                                logger.debug("Renely Stream is not processed");
                                //processLineRenely(line,linecounter);
                                //linecounter++;
                            }
                            else{
                                    if(workFile.getName().startsWith("GRENDIZER")){
                                    processLineGrendizer(line,linecounter);
                                    linecounter++;
                                }
                                else{
                                    if(workFile.getName().startsWith("KALASHNIKOV")){
                                        processLineKalashnikov(line,linecounter);
                                        linecounter++;
                                    }
                                    else{
                                        if(workFile.getName().startsWith("HANDSETBUNDLE")){
                                            // to be implemented
                                            //processLineHandsetBundle(line,linecounter);
                                            //linecounter++;
                                        }
                                        else{
                                            if(workFile.getName().startsWith("DynamicaSMS")){
                                                processLineDynamicaSMS(line,linecounter);
                                                linecounter++;
                                            }
                                            else{
                                                
                                                if(workFile.getName().startsWith("RBTRAMADANPROMO")){
                                                    processLineRBTRamadanPromo(line,linecounter);
                                                    linecounter++;
                                                }
                                                else{

                                                    logger.error("Unconfigured product : " + workFile.getName());

                                                }
                                                
                                            }
                                        }
                                    }
                                }
                            }
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

    /**
     * Processes the Old IN SMS Tool Stream.
     * Parses the CDR that contains 16 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * <field 2>,<field 3>, <field 16>,.
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
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
               sendSMS(properties.getProperty("OldINSMSTraffic_sender"),currentCDR.getMsisdn(),arabSMS,lineCounter);
           }
        }
    }
    
    /**
     * Processes the Hanoi Stream.
     * Parses the CDR that contains 4 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * HANOI,<OfferID>,.
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
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
             
             logger.info("the MSISDN : "+msisdn+"   .. the value for the key: "+ key);
             sendSMS(properties.getProperty("HANOI_sender"),msisdn,value,lineCounter);
         }
        
    }
    
    /**
     * Processes the RBTRAMADANPROMO Stream.
     * Parses the CDR that contains 4 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * RBTRAMADANPROMO_SMS_1,RBTRAMADANPROMO_SMS_2
     * Update the offer id sent in the CDR with a pre configured expiry date and
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
    public  void processLineRBTRamadanPromo(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        
        String msisdn=null;
        String input=null;
        String ProjectName=null;
        try{
             msisdn= lineFields[0];
             input = lineFields[2]+","+properties.getProperty("RBTRAMADANPROMO_ExpiryDate");
             ProjectName= lineFields[lineFields.length-1];

        }catch(Exception e){
            logger.error("Exception in parsing the CDR");
        }
        if(ProjectName.equals("RBTRAMADANPROMO")){
            Air newAir =null;
            HashMap<String,String> ucip_inputs1= null;
            try{
                Random rand = new Random();
                int  n = rand.nextInt(6) + 1;
                newAir = new Air(properties.getProperty("AIR_"+n+"_URL"),
                                     properties.getProperty("AIR_"+n+"_PASSWORD"),
                                     logger);
                newAir.setMsisdn(msisdn);
                newAir.setCurrentRequest(Globals.UCIPRequest.UpdateOfferWithExpiryStatic);
                ucip_inputs1 = newAir.parseInputs(input);
                logger.debug("Initialization of Air Request is done");
            }catch(Exception e){
                logger.error(e);
            }
            try {
                String request = newAir.formatRequestV1(ucip_inputs1,properties);
                String response = newAir.sendRequest(newAir.formatRequestV1(ucip_inputs1,properties));
                logger.debug(response);
                String responseCode = newAir.parseResponse(response);
                if(responseCode.equals("0")){
                    logger.debug("The Request was eexcuted successfully with responseCode : "+responseCode);
                    sendSMS(properties.getProperty("RBTRAMADANPROMO_Sender"),msisdn,properties.getProperty("RBTRAMADANPROMO_SMS_1"),lineCounter);
                    sendSMS(properties.getProperty("RBTRAMADANPROMO_Sender"),msisdn,properties.getProperty("RBTRAMADANPROMO_SMS_2"),lineCounter);
                }
                else {
                    logger.error("The request failed to execute with responseCode : "+ responseCode);
                     logger.error(request);
                     logger.error(response);
                }
            }catch(Exception e){
                logger.error(e);
            }
            
        }
        
        
        
    }
    
    /**
     * Processes the RENELY Stream.
     * Parses the CDR that contains 4 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * RENELY,GenericSMS,
     * Replaces the Z in the SMS Script with the value sent in field 3.
     * Replaces the X in the SMS Script with the 5 times value sent in field 3.
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
    public  void processLineRenely(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        
        String msisdn=null;
        String timeStamp=null;
        String TDFValue=null;
        String ProjectName=null;
        
        try{
             msisdn= lineFields[0];
             timeStamp= lineFields[1];
             TDFValue= lineFields[2];
             ProjectName= lineFields[lineFields.length-1];
        }catch(Exception e){
            logger.error("Exception in parsing the CDR");
        }
        
        if(!ProjectName.startsWith("RENELY")){
            logger.error("Not a RENELY CDR : "+ line);
            return;
        }            
        
        String key = null;
        int TDFInt = 0;
        String value =null;
        
        try{
            key = "RENELY,GenericSMS,";
            TDFInt = Integer.parseInt(TDFValue)-102;
            value = properties.getProperty(key);
        }catch(Exception e){
            logger.error("Exception in parsing the TDF Value or getting the template from the resource file");
            logger.error(e);
        }
         if(value == null){
            logger.error("the MSISDN : "+msisdn+"   .. the value for the key: "+ key +" does not exist in config file.");
        }
        else {
             
             if(TDFInt==0){
                 logger.debug("TDF Value = 0; No SMS will be sent");
                 return;
             }
             
             logger.info("the MSISDN : "+msisdn+"   .. the value for the key: "+ key);
             try{
                 value = value.replace("Z", TDFInt+"");
                 value = value.replace("X", (TDFInt*5)+"");
             }catch(Exception e){
                 logger.error("Exception in Replacing the parameters into the templates");
             }
             sendSMS(properties.getProperty("RENELY_sender"),msisdn,value,lineCounter);
         }
        
    }
    
    /**
     * Processes the GRENDIZER Stream.
     * Parses the CDR that contains 4 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * GRENDIZER,<TDFValue>,
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
    public  void processLineGrendizer(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        
        String msisdn=null;
        String timeStamp=null;
        String TDFValue=null;
        String ProjectName=null;
        
        try{
             msisdn= lineFields[0];
             timeStamp= lineFields[1];
             TDFValue= lineFields[2];
             ProjectName= lineFields[lineFields.length-1];
        }catch(Exception e){
            logger.error("Exception in parsing the CDR");
        }
        
        if(!ProjectName.startsWith("GRENDIZER")){
            logger.error("Not a GRENDIZER CDR : "+ line);
            return;
        }            
        
        String key = null;
        int TDFInt = 0;
        String value =null;
        
        try{
            key = "GRENDIZER";
            TDFInt = Integer.parseInt(TDFValue);
            logger.debug("TDF Value : " + key+","+TDFInt);
            value = properties.getProperty(key+","+TDFInt);
        }catch(Exception e){
            logger.error("Exception in parsing the TDF Value or getting the template from the resource file");
            logger.error(e);
        }
         if(value == null){
            logger.error("the MSISDN : "+msisdn+"   .. the value for the key: "+ key +" does not exist in config file.");
        }
        else {
             sendSMS(properties.getProperty("GRENDIZER_sender"),msisdn,value,lineCounter);
         }
        
    }

    /**
     * Processes the Kalashnikov Stream.
     * Parses the CDR that contains 4 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * Kalashnikov,<TDFValue>,
     * Replaces the $3 in the SMS Script with the value sent in field 3.
     * Replaces the $4 in the SMS Script with the value sent in field 4.
     * Replaces the $6 in the SMS Script with the value sent in field 6.
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
    public  void processLineKalashnikov(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        
        String msisdn=null;
        double field3=0.0;
        double field4=0.0;
        double field6=0.0;
        
        try{
             msisdn= lineFields[0];
             field3=Double.parseDouble(lineFields[2]);
             field4=Double.parseDouble(lineFields[3]);
             field6=Double.parseDouble(lineFields[5]);
        }catch(Exception e){
            logger.error("Exception in parsing the CDR");
        }

        int TDFValue = 0; 
        if(field6 != 0.0){
            TDFValue = 1;
        }    
        String value =null;

        try{
            logger.debug("KALASHNIKOV,"+TDFValue);
            value = properties.getProperty("KALASHNIKOV,"+TDFValue);
            value = value.replace("$3", field3+"");
            value = value.replace("$4", field4+"");
            value = value.replace("$6", field6+"");
        }catch(Exception e){
            logger.error("Exception in parsing the TDF Value or getting the template from the resource file");
            logger.error(e);
        }
         if(value == null){
            logger.error("the MSISDN : "+msisdn+"   .. the value for the key: KALASHNIKOV,"+TDFValue+" does not exist in config file.");
        }
        else {
             sendSMS(properties.getProperty("KALASHNIKOV_sender"),msisdn,value,lineCounter);
        }
        
        
        
    }
    
    /**
     * Processes the Old DynamicSMS Tool Stream.
     * Parses the CDR that contains 23 fields comma separated.
     * Gets the corresponding SMS from the config file based on the combination
     * Kalashnikov,<parameters>,
     * Replaces the $3 in the SMS Script with the value sent in field 3.
     * Replaces the $4 in the SMS Script with the value sent in field 4.
     * Replaces the $6 in the SMS Script with the value sent in field 6.
     * Sends the SMS by calling the method sendSMS.
     * @param line // the line containing the CDR.
     * @param lineCounter // the line number in the file.
     */
    public  void processLineDynamicaSMS(String line, int lineCounter) {
        String [] lineFields = line.split(",");
        logger.debug("CDR being processed : "+ line);
        int field17 = 0;
        int field1 = 0;
        int lastField = -1;
        String key="";
        String Script="";
        if(lineFields.length < 23){
            logger.error("Unconfigured CDR for the Dynamic SMS ");
            return;
        }
        try {
            field17 = Integer.parseInt(lineFields[16]);
        }catch(NumberFormatException e){
            logger.error("Wrong format for field 17 : " + lineFields[16]);
            return;
        }
        switch(field17){
                case 1001:
                    logger.debug("Product PPID : 1001");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try {
                        logger.debug("Parameter to be used  : "+ lineFields[4]);
                        Script = Script.replace("X", 15-(Double.parseDouble(lineFields[4]))+"");
                    }catch(Exception e){
                        logger.error("Error in replacing the parameters in the Script");
                        return;
                    }
                    
                    break;
                    
                case 1002:
                    logger.debug("Product PPID : 1002");
                    double parameter =0;
                    key = "DYNAMICSMS,"+lineFields[16]+",";
                    logger.debug("Field 16 : "+lineFields[15]);
                    if(lineFields[15].equals("1"))
                        key +=lineFields[15]+",";
                    else
                        if(lineFields[15].equals("0")){
                            key +=lineFields[15]+","+lineFields[1]+",";
                            logger.debug("Product Key : "+ key);
                            try {
                                double field1Beta = Double.parseDouble(lineFields[1]);
                                field1 = (int)field1Beta;
                            }catch(NumberFormatException e){
                                logger.error("Wrong format for field 2 : " + lineFields[1]);
                                return;
                            }
                            try{
                                    switch(field1){
                                    case 165:
                                        parameter = 5.0 - Double.parseDouble(lineFields[4]);break;
                                    case 166:
                                        parameter = 10.0 - Double.parseDouble(lineFields[4]);break;
                                    case 167:
                                        parameter = 15.0 - Double.parseDouble(lineFields[4]);break;
                                    case 168:
                                        parameter = 20.0 - Double.parseDouble(lineFields[4]);break;
                                    case 169:
                                        parameter = 25.0 - Double.parseDouble(lineFields[4]);break;
                                    case 170:
                                        parameter = 30.0 - Double.parseDouble(lineFields[4]);break;
                                    case 171:
                                        parameter = 40.0 - Double.parseDouble(lineFields[4]);break;
                                    case 172:
                                        parameter = 50.0 - Double.parseDouble(lineFields[4]);break;
                                    case 173:
                                        parameter = 60.0 - Double.parseDouble(lineFields[4]);break;
                                    default:
                                        logger.error("Value not configured for field 2 : "+ field1);
                                        return;
                                }
                            }catch(Exception e){
                                logger.error("Error in parsing the parameter : "+ lineFields[4]);
                                logger.error(e);
                                return;
                            }
                            
                        }
                        else
                            {
                                logger.error("Error in handling Product : "+lineFields[16]);
                                return;
                            }
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    Script = Script.replace("X", parameter+"");
                    break;
                case 1003:
                    logger.debug("Product PPID : 1003");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+","+lineFields[1]+",";
                    logger.debug("Product Key : "+ key);
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try {
                        field1 = Integer.parseInt(lineFields[1]);
                    }catch(NumberFormatException e){
                        logger.error("Wrong format for field 2 : " + lineFields[1]);
                        return;
                    }
                    try{
                        switch(field1){
                            case 158:
                                parameter = 25.0 - Double.parseDouble(lineFields[4]);break;
                            case 159:
                                parameter = 40.0 - Double.parseDouble(lineFields[4]);break;
                            case 160:
                                parameter = 60.0 - Double.parseDouble(lineFields[4]);break;
                            case 161:
                                parameter = 80.0 - Double.parseDouble(lineFields[4]);break;
                            case 162:
                                parameter = 100.0 - Double.parseDouble(lineFields[4]);break;
                            case 163:
                                parameter = 150.0 - Double.parseDouble(lineFields[4]);break;
                            default:
                                logger.error("Value not configured for field 2 : "+ field1);
                                return;
                        }
                    }catch(Exception e){
                        logger.error("Error in parsing parameter : "+ lineFields[4]);
                        return;
                    }
                    logger.debug("Parameter being used  : "+ parameter);
                    Script = Script.replace("X", parameter+"");
                    break;
                case 1004:
                    logger.debug("Product PPID : 1004");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field5 = Double.parseDouble(lineFields[4]);
                        field5 *= 0.5;
                        logger.debug("Parameter being used : "+lineFields[4]);
                        Script = Script.replace("X", field5+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                        return;
                    }
                    break;
                case 1005:
                    logger.debug("Product PPID : 1005");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field8 = Double.parseDouble(lineFields[7]);
                        field8 *= 0.5;
                        logger.debug("Parameter being used : "+lineFields[7]);
                        Script = Script.replace("X", field8+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 8 : " + lineFields[7]);
                        return;
                    }
                    break;
                case 1006:
                    logger.debug("Product PPID : 1006");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    Date currentDate = null;
                    
                    try {
                        currentDate = sdf.parse(lineFields[8]);
                        SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
                        logger.debug("Date sent in the CDR : "+ lineFields[8]);
                        Script = Script.replace("X", sdf1.format(currentDate));
                    } catch (ParseException ex) {
                      logger.error("Error in parsing date : "+ lineFields[8]);
                      return;
                    }
                    break;
                case 1007:
                    logger.debug("Product PPID : 1007");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    if(lineFields[15].equals("0")){
                    try{
                        double field5 = Double.parseDouble(lineFields[4]);
                        field5 = 10 - field5;
                        logger.debug("Parameter being used : "+lineFields[4]);
                        Script = Script.replace("X", field5+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                        return;
                    }
                    }else {
                        if(!lineFields[15].equals("1")){
                            logger.error("field 16 value is not configured : " + lineFields[15]);
                            return;
                        }
                    }
                    break;
                case 1008:
                    logger.debug("Product PPID : 1008");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    break;
                case 1009:
                    logger.debug("Product PPID : 1009");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    if(lineFields[15].equals("0")){
                    try{
                        double field5 = Double.parseDouble(lineFields[4]);
                        field5 = 15 - field5;
                        logger.debug("Parameter being used : "+lineFields[4]);
                        Script = Script.replace("X", field5+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                        return;
                    }
                    }else {
                        if(!lineFields[15].equals("1")){
                            logger.error("field 16 value is not configured : " + lineFields[15]);
                            return;
                        }
                    }
                    
                    break;
                case 1012:
                    logger.debug("Product PPID : 1012");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field5 = Double.parseDouble(lineFields[4]);
                        field5 = 25 - field5;
                        logger.debug("Parameter being used : "+lineFields[4]);
                        Script = Script.replace("X", field5+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                        return;
                    }
                    break;
                case 1014:
                    logger.debug("Product PPID : 1014");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+","+lineFields[2]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    break;
                case 1017:
                    logger.debug("Product PPID : 1017");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    if(!lineFields[20].equals(lineFields[21]) || lineFields[4].equals(lineFields[13])){
                        try{
                            double field5 = Double.parseDouble(lineFields[4]);
                            field5 = 10 - field5;
                             logger.debug("Parameter being used : "+lineFields[4]);
                            Script = Script.replace("X", field5+"");
                        }catch(Exception e){
                            logger.error("Error in parsing field 5 : " + lineFields[4]);
                            return;
                        }
                    }else {
                        logger.error("Field 21 is equal Field 22 and Field 5 is not equal Field 14 : "+ line);
                        return;
                    }
                    break;
                case 1022:
                    logger.debug("Product PPID : 1022");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    break;
                case 1023:
                    logger.debug("Product PPID : 1023");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field5 = Double.parseDouble(lineFields[4]);
                        field5 = 15 - field5;
                        logger.debug("Parameter being used : "+lineFields[4]);
                        Script = Script.replace("X", field5+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                        return;
                    }
                    break;
                case 1024:
                    logger.debug("Product PPID : 1024");
                    logger.debug("Field 21 : " +lineFields[20]+"   Field 22 : "+lineFields[21]);
                    if(lineFields[20].equals(lineFields[21])){
                        key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+",";
                        Script = properties.getProperty(key);
                        if(Script == null || Script.equals("")){
                            logger.error("Script not found for the key : "+ key);
                            return;
                        }
                        try{
                            field1 = Integer.parseInt(lineFields[1]);
                            switch(field1){
                                case 211:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 5 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 212:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 10 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 213:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 15 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 214:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 20 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 215:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 5 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 216:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 10 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 217:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 15 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 218:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 20 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 219:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 5 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 220:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 10 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 221:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 15 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;
                                case 222:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 20 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                    break;

                            }
                        }catch(Exception e){
                            logger.error("Error in parsing field 5 : " + lineFields[4]);
                            return;
                        }
                        break;
                    } else {
                        try{
                            double field21 = Double.parseDouble(lineFields[20]);
                            double field22 = Double.parseDouble(lineFields[21]);
                            
                            if(field21 < field22){
                               key = "DYNAMICSMS,"+lineFields[16]+",";
                               Script = properties.getProperty(key);
                               if(Script == null || Script.equals("")){
                                    logger.error("Script not found for the key : "+ key);
                                    return;
                               } 
                            }
                        }catch(Exception e){
                            logger.error("Error in parsing field 21  : " + lineFields[20] + " or field 22 : "+ lineFields[21]);
                            return;
                        }
                    }
                case 1025:
                    logger.debug("Product PPID : 1025");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[15]+","+lineFields[1]+",";
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                            field1 = Integer.parseInt(lineFields[1]);
                            switch(field1){
                                case 230:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 10 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                case 231:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 15 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                case 232:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 20 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                case 233:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 25 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                                case 234:
                                    try{
                                        double field5 = Double.parseDouble(lineFields[4]);
                                        field5 = 30 - field5;
                                        Script = Script.replace("X", field5+"");
                                    }catch(Exception e){
                                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                                        return;
                                    }
                            }
                    }catch(Exception e){
                        logger.error("Exception in parsing field 2 : "+ field1);
                        return;
                    }
                    break;
                case 1026:
                    logger.debug("Product PPID : 1026");
                    try{
                        double field16 = Double.parseDouble(lineFields[15]);
                        double field14 = Double.parseDouble(lineFields[13]);
                        logger.debug("field16 : "+lineFields[15]);
                        logger.debug("field14 : "+lineFields[13]);
                        if(field16 > field14 && field16 < 10){
                            lastField = 0;
                        }
                        else {
                            
                           double field22 = Double.parseDouble(lineFields[21]);
                           double field21 = Double.parseDouble(lineFields[20]);
                           logger.debug("field22 : "+lineFields[21]);
                           logger.debug("field21 : "+lineFields[20]);
                           if(field22 > field21 ){
                               lastField = 1;
                           } 
                        }                        
                    }catch(Exception e){
                        logger.error("Error in parsing fields 16,14,21,22 : ");
                        return;
                    }
                    
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+","+lastField+",";
                    logger.debug("The Key : "+ key);
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                            double field16 = Double.parseDouble(lineFields[15]);
                            field16 = 10 - field16;
                            Script = Script.replace("X", field16+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 16 : " + lineFields[15]);
                        return;
                    }
                    break;
                case 1027:
                    logger.debug("Product PPID : 1027");
                    try{
                        double field16 = Double.parseDouble(lineFields[15]);
                        double field14 = Double.parseDouble(lineFields[13]);
                        if(field16 > field14 && field16 < 5){
                            lastField = 5;
                            field16 = 5- field16;
                        }
                        else {
                           if(field16 > field14 && field16 < 10){
                                lastField = 10;
                                field16 = 10- field16;
                            }
                            else {
                               if(field16 > field14 && field16 < 15){
                                    lastField = 15;
                                    field16 = 15- field16;
                                }
                                else {
                                   if(field16 > field14 && field16 < 20){
                                        lastField = 20;
                                        field16 = 20- field16;
                                    }
                                    else {
                                        double field22 = Double.parseDouble(lineFields[21]);
                                        double field21 = Double.parseDouble(lineFields[20]);
                                        if(field22 > field21 ){
                                            lastField = 1;
                                        } 
                                    }
                                }
                            }
                        }
                        
                    }catch(Exception e){
                        logger.error("Error in parsing fields 16,14,21,22 : ");
                        return;
                    }
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+","+lastField+",";
                    logger.debug("The Key : "+key);
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field16 = Double.parseDouble(lineFields[15]);
                        field16 = lastField - field16;
                        Script = Script.replace("X", field16+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 16 : " + lineFields[15]);
                        return;
                    }
                    break;
                case 1028:
                    logger.debug("Product PPID : 1028");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[1]+","+lineFields[15]+",";
                    logger.debug("The Key : "+key);
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field5 = Double.parseDouble(lineFields[4]);
                        Script = Script.replace("X", field5+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 5 : " + lineFields[4]);
                        return;
                    }
                    break;
                case 1030:
                    logger.debug("Product PPID : 1030");
                    key = "DYNAMICSMS,"+lineFields[16]+",";
                    logger.debug("The Key : "+key);
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field8 = Double.parseDouble(lineFields[7]);
                        field8 *= 10;
                        Script = Script.replace("X", field8+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 8 : " + lineFields[7]);
                        return;
                    }
                    break;
                case 1031:
                    logger.debug("Product PPID : 1031");
                    key = "DYNAMICSMS,"+lineFields[16]+",";
                    logger.debug("The Key : "+key);
                    Script = properties.getProperty(key);                    
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field22 = Double.parseDouble(lineFields[21]);
                        double field21 = Double.parseDouble(lineFields[20]);
                        field22 = (field22 - field21)/0.25;
                        Script = Script.replace("X", field22+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 21  : " + lineFields[21]+ " or field 22 : "+ lineFields[22]);
                        return;
                    }
                    break;
                case 1032:
                    logger.debug("Product PPID : 1032");
                    key = "DYNAMICSMS,"+lineFields[16]+","+lineFields[6]+",";
                    logger.debug("The Key : "+key);
                    Script = properties.getProperty(key);
                    if(Script == null || Script.equals("")){
                        logger.error("Script not found for the key : "+ key);
                        return;
                    }
                    try{
                        double field22 = Double.parseDouble(lineFields[21]);
                        double field21 = Double.parseDouble(lineFields[20])/0.25;
                        
                        Script = Script.replace("Z", field22+"");
                        Script = Script.replace("X", field21+"");
                    }catch(Exception e){
                        logger.error("Error in parsing field 21  : " + lineFields[21]+ " or field 22 : "+ lineFields[22]);
                        return;
                    }
                    break;
                    
            }
            sendSMS(properties.getProperty("DYNAMICSMS_sender"),lineFields[0],Script,lineCounter);
        
        
    }
    
    /**
     * Sends the SMS directly to the SMSC using the object smscSender and the method sendMessage
     * in the smpp.jar library.
     * @param sender //The sender of the SMS.
     * @param dial  // The dial receiving the SMS.
     * @param toString // The SMS Script.
     * @param lineCounter //The line number of the CDR requesting sending the passed SMS.
     */
    public  void sendSMS(String sender,String dial,String toString,int lineCounter) {
    
        try{
            String [] smsSplit = toString.split(",");
            boolean result = smscSender.sendMessage(sender, "0"+dial, toString,2);
            logger.debug("Successfully Sending SMS");
            if(result){
               logger.debug(toString);
               logger.debug("Successfully Sent SMS to the dial "+dial); 
            } else 
            {
                logger.debug(toString);
                logger.debug("Failed to send SMS to the dial  "+dial); 
            }
        }catch(Exception e){
            logger.error("Failed to send SMS to the dial  "+dial); 
        }
        
//        Writer writer = null;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//        String currentFileName ="";
//        File resultFile = null;
//        File newFile  = null;
//        if(Util.getOSType() == Globals.OS_UNIX){
//            currentFileName = Globals.SMS_PREPARATION_DIRECTORY+"Revamp_IN_SMSTool_"+sdf.format(new Date())+"_L"+lineCounter+"_V"+this.counter+".txt";
//            resultFile = new File(currentFileName);
//            newFile = new File(Globals.SMS_DIRECTORY+resultFile.getName());
//                    
//        }
//        else{
//            currentFileName = Globals.SMS_PREPARATION_DIRECTORY+"Revamp_IN_SMSTool_"+sdf.format(new Date())+"_L"+lineCounter+"_V"+this.counter+".txt";
//            resultFile = new File(currentFileName);
//            newFile = new File(Globals.SMS_DIRECTORY+resultFile.getName());
//        }
//        
//        
//        if(!resultFile.exists())
//            try {
//                resultFile.createNewFile();
//        } catch (IOException ex) {
//           logger.error("Cannot create the SMS file in the SMS Preparation Directory: "+ currentFileName);
//        }
//        try {
//            FileWriter fw = new FileWriter(resultFile,true);
//            //BufferedWriter writer give better performance
//            BufferedWriter bw = new BufferedWriter(fw);
//            bw.append(toString);
//            bw.close();
//            // Send the file to the SMS tool
//            //logger.info("Deleting moved file : "+ newFile.delete());
//            try{
//            Files.move(Paths.get(resultFile.getAbsolutePath()), Paths.get(newFile.getAbsolutePath()),StandardCopyOption.REPLACE_EXISTING);
//            logger.info("File "+ newFile.getAbsolutePath()+" is moved to the SMS Directory");
//            }catch(Exception e){
//                logger.error("Failed to move File "+ resultFile.getAbsolutePath()+" to the SMS Directory : " + Globals.SMS_DIRECTORY + " under name : " + newFile.getName());
//                logger.error("Exception : "+ e);
//            }    
////            if(resultFile.renameTo(newFile)){
////              logger.info("File "+ newFile.getAbsolutePath()+" is moved to the SMS Directory");
////            }
////            else {
////               logger.error("Failed to move File "+ resultFile.getAbsolutePath()+" to the SMS Directory : " + Globals.SMS_DIRECTORY + " under name : " + newFile.getName());
////            }
//        } catch (UnsupportedEncodingException ex) {
//            logger.error("Error in writing in file : "+ currentFileName);
//        } catch (FileNotFoundException ex) {
//            logger.error("File not found : "+ currentFileName);
//        } catch (IOException ex) {
//            logger.error("IO Exception: "+ currentFileName);;
//        }
    }
    
    /**
     * Archives the file after processing it in the archive directory.
     * @param currentFile
     * @return nothing
     */
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

    /**
     * Overridden method from the Runnable class that starts the execution of the thread.
     */
    @Override
    public void run()  {
       execute();      
    }

    /**
     * Moves the passed file to the passed destination.
     * @param file // the file needed to be moved.
     * @param destination // the destination path
     * @return the path to the new file if the move is done successfully or "Failed to move" if the move
     * failed
     */
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
