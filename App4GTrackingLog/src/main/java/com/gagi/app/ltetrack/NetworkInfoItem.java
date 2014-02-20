package com.gagi.app.ltetrack;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by igaglioti on 17/02/14.
 */
public class NetworkInfoItem {

    private String mSummaryInfo;
    private Location mLocation;
    private String operatorName;
    private int mNetworkType;
    private String mTimeEventInfo;

    public final static SimpleDateFormat FORMAT = new SimpleDateFormat(
            //"yyyy-MM-dd HH:mm:ss", Locale.ITALY);
            "dd-MM-yyyy HH:mm:ss", Locale.ITALY);

    public  NetworkInfoItem(String summaryInfo)
    {
        mSummaryInfo = summaryInfo;
    }

    public String getSummaryInfo() {
        return mSummaryInfo;
    }


    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setNetworkType(int networkType) {
        this.mNetworkType = networkType;
    }

    public int getNetworkType()
    {
        return  mNetworkType;
    }

    public String getNetworkTypeName()
    {
        String txtNetworkType = "";
        switch (mNetworkType)
        {
            case 7:
                txtNetworkType = "1xRTT";
                break;
            case 4:
                txtNetworkType = "CDMA";
                break;
            case 2:
                txtNetworkType = "EDGE";
                break;
            case 14:
                txtNetworkType = "eHRPD";
                break;
            case 5:
                txtNetworkType = "EVDO rev. 0";
                break;
            case 6:
                txtNetworkType = "EVDO rev. A";
                break;
            case 12:
                txtNetworkType = "EVDO rev. B";
                break;
            case 1:
                txtNetworkType = "GPRS";
                break;
            case 8:
                txtNetworkType = "HSDPA";
                break;
            case 10:
                txtNetworkType = "HSPA";
                break;
            case 15:
                txtNetworkType = "HSPA+";
                break;
            case 9:
                txtNetworkType = "HSUPA";
                break;
            case 11:
                txtNetworkType = "iDen";
                break;
            case 13:
                txtNetworkType = "LTE";
                break;
            case 3:
                txtNetworkType = "UMTS";
                break;
            case 0:
            default:
                txtNetworkType = "Unknown";
                break;
        }        
        return txtNetworkType;
    }

    public void setTimeEventInfo(String timeEventInfo) {
        this.mTimeEventInfo = timeEventInfo;
    }

    public String getmTimeEventInfo() {
        return mTimeEventInfo;
    }

}
