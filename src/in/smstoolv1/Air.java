/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package in.smstoolv1;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author amir.rashed
 */
public class Air {
    String url;
    String password;
    Logger logger;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hh:mm:ss+0200");
    String msisdn;
    Globals.UCIPRequest currentRequest;
    
    public Air(String url, String password, Logger logger) {
        this.url = url;
        this.password = password;
        this.logger = logger;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
    
    private URLConnection getConnection() throws IOException {

        URL url = new URL(this.url);

        URLConnection urlConnection = url.openConnection();

        return urlConnection;
    }

    public Globals.UCIPRequest getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(Globals.UCIPRequest currentRequest) {
        this.currentRequest = currentRequest;
    }
    
    
    public String sendRequest(String request) {

        OutputStreamWriter streamWriter = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String response = "";
        //tring password = "Z3NkYzpnc2Rj";
        // String password = "YWlydXNlcjpsYXVuY2gwNQ==";
        URLConnection urlConnection= null;
        HttpURLConnection httpConnection = null;
        try {
            //   System.out.println("in 1");
            urlConnection = getConnection();
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestProperty("Method", "POST");
            urlConnection.setRequestProperty("User-Agent", "UGw Server/5.0/1.0");
            urlConnection.setRequestProperty("Content-Type", "text/xml");
            urlConnection.setRequestProperty("Authorization",password);
            urlConnection.setRequestProperty("Host", "Air");
            String currentRequest = request.replace("$msisdn", msisdn);
            urlConnection.setRequestProperty("Content-Length", "" + currentRequest.length());
            
            streamWriter = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            
            streamWriter.write(currentRequest);
            streamWriter.flush();
            
            inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            String responseLine = "";
            while ((responseLine = bufferedReader.readLine()) != null) {
                response = response + responseLine;
            }
            
            // System.out.println(response);
        } catch (Exception exception) {
            logger.error("Exception in sendRequest ");
            logger.error(exception);
        } finally {
            if (streamWriter != null) {
                try {
                    streamWriter.close();
                } catch (IOException ioException) {
                    logger.error("Exception in sendRequest ");
                    logger.error(ioException);
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ioException) {
                    logger.error("Exception in sendRequest ");
                    logger.error(ioException);
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    logger.error("Exception in sendRequest ");
                    logger.error(ioException);
                }
            }
            if (urlConnection != null) {
                 httpConnection = (HttpURLConnection) urlConnection;
                 httpConnection.disconnect();
                 logger.debug("The connection to "+this.url+" is closed");
            }
        }
        //   System.out.println("in 5");
        return response;
    }
    
    public String formatRequestV1(HashMap<String,String> inputs,Properties properties){
        String updateBalanceAndDateRequest ="";
        try{
            updateBalanceAndDateRequest = readFromFile(new File(System.getProperty("user.dir")+"/Requests/"+currentRequest+".txt")).toString();
            logger.debug("Request : "+currentRequest+" exists in the Requests folder");
            updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$originTimeStamp",sdf.format(new Date()));
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 6);
            switch(currentRequest){
                case UpdateOffer: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                    break;
                case UpdateOfferWithExpiryDynamic: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$expiryDate", inputs.get("$expiryDate"));
                    break;
                case UpdateOfferWithExpiryStatic: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$expiryDate", inputs.get("$expiryDate"));
                    break;
                case AddPam: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$PAMServiceID", inputs.get("$PAMServiceID"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$PAMCLASSID", inputs.get("$PAMCLASSID"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$PAMSCHEDULEID", inputs.get("$PAMSCHEDULEID"));
                    break;
                case RunPam: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$pamServiceID", inputs.get("$PAMServiceID"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$pamIndicator",inputs.get("$pamIndicator"));
                    break;
                case ResetFiveAccumulator:
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_1", properties.getProperty("AccumulatorID_1"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_2", properties.getProperty("AccumulatorID_2"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_3", properties.getProperty("AccumulatorID_3"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_4", properties.getProperty("AccumulatorID_4"));
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$AccumulatorID_5", properties.getProperty("AccumulatorID_5"));
                    break;
                case DeleteOffer: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$offerID", inputs.get("$offerid"));
                    break;
                case ChangeSC: 
                    updateBalanceAndDateRequest = updateBalanceAndDateRequest.replace("$SC", inputs.get("$SC"));
                    break;
            };
            logger.debug("Format Request has been performed successfully");
        }catch(Exception e){
            logger.error("Failed to Format Request");
            logger.error(e);
        }
        return updateBalanceAndDateRequest;        
    }

    /**
     * Reads from passed file.
     * @param dialsFile // file to be read.
     * @return the content of the file as a String.
     */
    public  String readFromFile(File dialsFile){
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try{
            br = new BufferedReader(new FileReader(dialsFile));
        } catch(FileNotFoundException e){
            
        }
        
        try {
            
            String line = br.readLine();

            while (line != null) {
                
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            
            br.close();
        } catch (IOException ex) {
         
        } 
        return sb.toString();
    }

    public HashMap<String,String>  parseInputs(String s){
        HashMap<String,String> ucip_inputs1 = new HashMap<String,String>();
        logger.debug("Current Request : "+currentRequest.toString() );
        try{
            if(this.currentRequest== Globals.UCIPRequest.AddPam){

                this.currentRequest = Globals.UCIPRequest.AddPam;
                ucip_inputs1.put("$PAMServiceID",s.split(",")[0]);
                ucip_inputs1.put("$PAMCLASSID",s.split(",")[1]);
                ucip_inputs1.put("$PAMSCHEDULEID",s.split(",")[2]);
            }
            if(this.currentRequest== Globals.UCIPRequest.UpdateOffer){
                ucip_inputs1.put("$offerid",s.split(",")[0]);
            }

            if(this.currentRequest== Globals.UCIPRequest.UpdateOfferWithExpiryDynamic){
                ucip_inputs1.put("$offerid",s.split(",")[0]);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH,Integer.parseInt(s.split(",")[1]));
                java.util.Date dt = cal.getTime();
                String currentDate = sdf.format(dt);
                ucip_inputs1.put("$expiryDate",currentDate);
            }
            if(this.currentRequest== Globals.UCIPRequest.UpdateOfferWithExpiryStatic){
                ucip_inputs1.put("$offerid",s.split(",")[0]);
                ucip_inputs1.put("$expiryDate",s.split(",")[1]);
            }
            if(this.currentRequest== Globals.UCIPRequest.DeleteOffer){
                ucip_inputs1.put("$offerid",s.split(",")[0]);
            }
            if(this.currentRequest== Globals.UCIPRequest.ChangeSC){
                ucip_inputs1.put("$SC",s.split(",")[0]);
            }
        }catch(Exception e){
            logger.error("Mapping inputs failed");
            logger.error(e);
        }
        return ucip_inputs1;
    }
    
    public String parseResponse(String response){
        String status = "";
        if (response.length() > 0) {

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            InputSource source = new InputSource(new StringReader(
                    response));

            try {
                status = xpath.evaluate("/methodResponse/params/param/value/struct/member[name='responseCode']/value/i4", source);
                
            } catch (XPathExpressionException ex) {
               logger.error("Cannot Parse Response : " +ex);
               logger.error("Current Response : "+ response);
            }

            xpath.reset();
        } else {
            status = "-1";
        }
        return status;
    }

    public double parseDAValue(String response, int daID) throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, XPathExpressionException {
     try {
        DocumentBuilderFactory factory =DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        StringBuilder xmlStringBuilder = new StringBuilder();
        xmlStringBuilder.append(response);
        ByteArrayInputStream input =  new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
        Document doc = builder.parse(input);
        XPath xPath =  XPathFactory.newInstance().newXPath();
        String expression = "/methodResponse/params/param/value/struct/member[name='dedicatedAccountInformation']/value/array/data";	        
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        String args = nodeList.item(0).getTextContent();
        args = args.replace("\n", "");
        args = args.replace("\t", "");
        args = args.replace("\r", "");
        args = args.replace("                                                                                   ","");
        args = args.replace("dedicatedAccountValue1                                                  ","dedicatedAccountValue1-");
        args = args.replace("dedicatedAccountID                                                  ","dedicatedAccountID=");

        logger.debug("DA area cutted");
        
        while(args.indexOf("dedicatedAccountID") != -1){
            String ID = args.substring(args.indexOf("dedicatedAccountID")+18,args.indexOf("dedicatedAccountValue1"));
            int eeee = Integer.parseInt(ID.trim());
            String value = args.substring(args.indexOf("dedicatedAccountValue1")+22,args.indexOf("expiryDate"));
            double eeee1 = Double.parseDouble(value.trim());
            args = args.substring(args.indexOf("yDate")+1);
            logger.debug("DA ID : "+ eeee +"  DA Value: "+eeee1);
            if(eeee == daID)
                return eeee1;

        }
        
     }
     catch(Exception e){
     logger.error("Exceptinb in parseDA Method");
     logger.error(e);
    }
     return -1;
   }
}
