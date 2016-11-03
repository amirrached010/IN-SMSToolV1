/*
    This is the comment block to the main method
 */
package in.smstoolv1;

import com.etisalatmisr.smpp.SMSSender;
import static in.smstoolv1.Util.getLogLevel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author Amir.Rashed
 */

public class Main {
    
    static Logger logger;
    static Properties properties;
    static ExecutorService pool;
    static int counter;
    static Set<Future<String>> resultsHashSet = new LinkedHashSet<Future<String>>();
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SMSSender smscSender;
    /**
     * This is the start of the execution.
     * @param args 
     */
    
    public static void main(String args[]) {
        
        logger = Logger.getLogger(Main.class);
        Util.intializeLogger(logger);
        initiatePropertiesFile();
        initiateSMSCConfigurations();
        Globals.DIRECTORY_PATH =properties.getProperty("HOME_DIRECTORY");
        Util.setGlobals();
        logger.debug("Watched Directory is : " + Globals.WATCHED_DIRECTORY );
        Globals.IS_OSWIN = Util.getOSType();
        pool = Executors.newCachedThreadPool();
        
        File file = new File(Globals.WATCHED_DIRECTORY);
        while(true){
            try{
                Util.checkLogCapacity(logger);
            } catch(Exception ex){
                logger.error("Error in checking for log file size");
            }
            File[] directoryListing = file.listFiles();
            if(directoryListing != null && directoryListing.length > 0){
                logger.debug("Number of files in the Watched folder is : "+ directoryListing.length);
                counter =0;
                for(int i=0; i< directoryListing.length;i++){
                    try {
                        Util.archiveWatchDoLogFile(logger);
                        
                    } catch(Exception ex){
                        logger.error("Error in accumulating Watch Dog logs : " + ex);
                    }
                    try {
                        Util.accumulateLogs();
                    }catch(Exception ex){
                        logger.error("Error in accumulating logs : " + ex);
                    }
                    try{
                        //logger.debug("File being processed : "+ directoryListing[i].getName());
                        if(smscSender == null){
                            initiateSMSCConfigurations();
                        }
                        HandleFile(directoryListing[i]);
                        //logger.debug("Done Processing file : "+ directoryListing[i].getName());
                    } catch(Exception ex){
                        logger.error("Error in Handling file : "+ directoryListing[i].getAbsolutePath());
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    logger.error("Thread sleeping fails");
                }
            }
        }
        
        
    }
    
    /**
     * Handles the file passed in the parameter
     * Ignores it if it is a temp file.
     * Assigns a thread to execute it and runs the thread.
     * @param toFile 
     */
    private static void HandleFile(File toFile) {
        File currentFile = toFile;
        try {
            if(currentFile.getAbsolutePath().toLowerCase().contains("tmp") 
                    || currentFile.getName().contains("CLUBPHASE2SMS")
                    ){
                return;
            }
        }catch(Exception ex){
            logger.error("A Temp File check caused an Exception : " + ex);
        }
        try{
            SlaveThread a = new SlaveThread(currentFile,"T"+counter,properties,smscSender);
            a.run();
        } catch(Exception ex){
            logger.error("Error in initializing the Thread : " + ex);
        }
        counter++;
    }

    /**
    * Responsible of loading the properties file "config.properties" located under Resources.
    * Throws an exception if the file was not found or the configuration was not successfully
    * loaded.
    * 
    */
    public static void initiatePropertiesFile(){
        properties = new Properties();
        try {
            File file = new File(System.getProperty("user.dir")+"/Resources/config.properties");
            FileInputStream fileInput = new FileInputStream(file);
            properties.load(fileInput);
            fileInput.close();
            logger.setLevel(getLogLevel(properties.getProperty("LogLevel")));
            logger.debug("Properties read successfully from file : " +file.getAbsolutePath());
        } catch (FileNotFoundException ex) {
            logger.error("Config File Not Found : " + ex.getMessage());
        } catch (IOException ex) {
            logger.error("Config File Parsing Error: " + ex.getMessage());
        }
    }

    /**
    * Responsible of setting the connection with the SMSC and mapping its 
    * configurations from the smpp.cfg file located in the Resources folder.
    * Throws an exception if the file was not found or the configuration was not successfully
    * loaded.
    * @param void
    * @return void
    * 
    */
    public static void initiateSMSCConfigurations(){
        Properties smsSenderProperties = new Properties();
        FileInputStream fileInput;
        File smsPropertiesFile = new File(System.getProperty("user.dir")+"/Resources/smpp.cfg");
        logger.debug("the SMSC config file : "+ smsPropertiesFile.getAbsolutePath());
        try {
            fileInput = new FileInputStream(smsPropertiesFile);
        } catch (FileNotFoundException ex) {
            logger.error("SMSC config file is not found");
            return;
        }
        try {
            smsSenderProperties.load(fileInput);
        } catch (IOException ex) {
            logger.error("Error in loading the SMSC config ");
            return;
        }

            
        
        try {
            smscSender = new SMSSender(smsSenderProperties, 1);
            logger.debug("SMSSender initialized successfully");
        } catch (Exception ex) {
            logger.error("Error in initializing the SMSSender Object");
            logger.error(ex);
        }
        
    }
}
