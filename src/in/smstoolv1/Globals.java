
package in.smstoolv1;

/**
 * This is the Globals class for the IN-SMS Tool V1.
 * It contains all the global variables that need to be set only once and used across all the execution.
 * 
 */
public class Globals {
    
//    public static final String WATCHED_DIRECTORY="D:\\Arts\\IN-SMSToolV1\\INPUT\\Ready\\";
//    public static final String SMS_DIRECTORY="D:\\Arts\\IN-SMSToolV1\\SMS_DIRECTORY\\";
//    public static final String DIRECTORY_PATH="D:\\Arts\\IN-SMSToolV1\\";
//    public static final String WORK_DIRECTORY=DIRECTORY_PATH+"INPUT\\Work\\";
//    public static final String SMS_PREPARATION_DIRECTORY=DIRECTORY_PATH+"SMSPreparation\\";
//    public static final String INSTANT_LOG_PATH= DIRECTORY_PATH+"logs\\";
//    public static final String ARCHIVE_LOG_DIRECTORY = DIRECTORY_PATH+"ArchiveLog\\";
//    public static final String ARCHIVE_DIRECTORY = DIRECTORY_PATH+"Archive\\";
    
    
//    public static final String SMS_DIRECTORY="/export/home/etisalatSMS/input/";
//    public static final String DIRECTORY_PATH="/app/IN-SMSToolV1/";
//    //public static final String DIRECTORY_PATH="/arc/INPlanning/IN-SMSToolV1/";
//    public static final String WATCHED_DIRECTORY=DIRECTORY_PATH+"INPUT/Ready/";
//    public static final String WORK_DIRECTORY=DIRECTORY_PATH+"INPUT/Work/";
//    public static final String SMS_PREPARATION_DIRECTORY=DIRECTORY_PATH+"SMSPreparation/";    
//    public static final String INSTANT_LOG_PATH= DIRECTORY_PATH+"logs/";   
//    public static final String ARCHIVE_LOG_DIRECTORY = DIRECTORY_PATH+"ArchiveLog/";
//    public static final String ARCHIVE_DIRECTORY = DIRECTORY_PATH+"Archive/";
  
    
    public static  String DIRECTORY_PATH;
    public static  String WATCHED_DIRECTORY;
    public static  String WORK_DIRECTORY;
    public static  String SMS_PREPARATION_DIRECTORY;    
    public static  String INSTANT_LOG_PATH;   
    public static  String ARCHIVE_LOG_DIRECTORY;
    public static  String ARCHIVE_DIRECTORY;

    
    public static final boolean OS_WIN = true;
    
    public static final boolean OS_UNIX = false;
    
    public static boolean IS_OSWIN;
    
    public static enum UCIPRequest {
        UpdateOffer,
        AddPam,
        GetAccountDetails,
        ResetFiveAccumulator,
        GetOffers,
        RunPam,
        UpdateOfferWithExpiryDynamic,
        UpdateOfferWithExpiryStatic,
        DeleteOffer,
        ChangeSC,
        GetBalanceAndDate
    }
}   
