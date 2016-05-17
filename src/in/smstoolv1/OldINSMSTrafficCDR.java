
package in.smstoolv1;

import java.util.Properties;

/**
 *
 * This class makes an object from the CDR used in the old
 * implementation of the IN-SMS Tool.
 * It was made because the CDR has no reference to the product related to It. 

 */
public class OldINSMSTrafficCDR {
    String msisdn;             // Customer dial
    String pp_b4;              // promotion plan before
    String pp_after;           // promotion plan after
    String time;               // timestamp
    String rech_val;           // recharge value
    String off_b4;             // offers before
    String off_after;          // offers after
    String off_add;            // offers
    String acc_rech_value_b4;  //
    String num_rech_b4;        //
    String acc_rech_value_aft; //
    String num_rech_aft;       //
    String ref_val_b4;         //
    String ref_counter_b4;     //
    String ref_value_aft;      // 
    String ref_counter_aft;    //

    int lineCounter;
    public OldINSMSTrafficCDR(){
        
    }
    
    public OldINSMSTrafficCDR(String [] CDRs) {
        if(CDRs.length < 16)
            return;
        this.msisdn = CDRs[0];
        this.pp_b4 = CDRs[1];
        this.pp_after = CDRs[2];
        this.time = CDRs[3];
        this.rech_val = CDRs[4];
        this.off_b4 = CDRs[5];
        this.off_after = CDRs[6];
        this.off_add = CDRs[7];
        this.acc_rech_value_b4 = CDRs[8];
        this.num_rech_b4 = CDRs[9];
        this.acc_rech_value_aft = CDRs[10];
        this.num_rech_aft = CDRs[11];
        this.ref_val_b4 = CDRs[12];
        this.ref_counter_b4 = CDRs[13];
        this.ref_value_aft = CDRs[14];
        this.ref_counter_aft = CDRs[15];
    }

    public OldINSMSTrafficCDR(String CDR, int linecounter){
        this.lineCounter = linecounter;
        String [] CDRs =CDR.split(",");
        if(CDRs.length < 16)
            return;
        this.msisdn = CDRs[0];
        this.pp_b4 = CDRs[1];
        this.pp_after = CDRs[2];
        this.time = CDRs[3];
        this.rech_val = CDRs[4];
        this.off_b4 = CDRs[5];
        this.off_after = CDRs[6];
        this.off_add = CDRs[7];
        this.acc_rech_value_b4 = CDRs[8];
        this.num_rech_b4 = CDRs[9];
        this.acc_rech_value_aft = CDRs[10];
        this.num_rech_aft = CDRs[11];
        this.ref_val_b4 = CDRs[12];
        this.ref_counter_b4 = CDRs[13];
        this.ref_value_aft = CDRs[14];
        this.ref_counter_aft = CDRs[15];
    }
    
    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getPp_b4() {
        return pp_b4;
    }

    public void setPp_b4(String pp_b4) {
        this.pp_b4 = pp_b4;
    }

    public String getPp_after() {
        return pp_after;
    }

    public void setPp_after(String pp_after) {
        this.pp_after = pp_after;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRech_val() {
        return rech_val;
    }

    public void setRech_val(String rech_val) {
        this.rech_val = rech_val;
    }

    public String getOff_b4() {
        return off_b4;
    }

    public void setOff_b4(String off_b4) {
        this.off_b4 = off_b4;
    }

    public String getOff_after() {
        return off_after;
    }

    public void setOff_after(String off_after) {
        this.off_after = off_after;
    }

    public String getOff_add() {
        return off_add;
    }

    public void setOff_add(String off_add) {
        this.off_add = off_add;
    }

    public String getAcc_rech_value_b4() {
        return acc_rech_value_b4;
    }

    public void setAcc_rech_value_b4(String acc_rech_value_b4) {
        this.acc_rech_value_b4 = acc_rech_value_b4;
    }

    public String getNum_rech_b4() {
        return num_rech_b4;
    }

    public void setNum_rech_b4(String num_rech_b4) {
        this.num_rech_b4 = num_rech_b4;
    }

    public String getAcc_rech_value_aft() {
        return acc_rech_value_aft;
    }

    public void setAcc_rech_value_aft(String acc_rech_value_aft) {
        this.acc_rech_value_aft = acc_rech_value_aft;
    }

    public String getNum_rech_aft() {
        return num_rech_aft;
    }

    public void setNum_rech_aft(String num_rech_aft) {
        this.num_rech_aft = num_rech_aft;
    }

    public String getRef_val_b4() {
        return ref_val_b4;
    }

    public void setRef_val_b4(String ref_val_b4) {
        this.ref_val_b4 = ref_val_b4;
    }

    public String getRef_counter_b4() {
        return ref_counter_b4;
    }

    public void setRef_counter_b4(String ref_counter_b4) {
        this.ref_counter_b4 = ref_counter_b4;
    }

    public String getRef_value_aft() {
        return ref_value_aft;
    }

    public void setRef_value_aft(String ref_value_aft) {
        this.ref_value_aft = ref_value_aft;
    }

    public String getRef_counter_aft() {
        return ref_counter_aft;
    }

    public void setRef_counter_aft(String ref_counter_aft) {
        this.ref_counter_aft = ref_counter_aft;
    }

    @Override
    public String toString() {
        return "CDR{" + "msisdn=" + msisdn + ", pp_b4=" + pp_b4 + ", pp_after=" + pp_after + ", time=" + time + ", rech_val=" + rech_val + ", off_b4=" + off_b4 + ", off_after=" + off_after + ", off_add=" + off_add + ", acc_rech_value_b4=" + acc_rech_value_b4 + ", num_rech_b4=" + num_rech_b4 + ", acc_rech_value_aft=" + acc_rech_value_aft + ", num_rech_aft=" + num_rech_aft + ", ref_val_b4=" + ref_val_b4 + ", ref_counter_b4=" + ref_counter_b4 + ", ref_value_aft=" + ref_value_aft + ", ref_counter_aft=" + ref_counter_aft + '}';
    }
    
    
    
}
