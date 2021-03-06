package com.example.cameron.ethereumtest1.util;

import android.support.annotation.ColorInt;

import com.example.cameron.ethereumtest1.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * Created by cameron on 10/13/17.
 */

public class DataUtils {

    @ColorInt
    public static final int TEAL = 0x00F0FF00;

    public static String convertTimeStampToDateString(long timestamp) {
        Date date = new Date(timestamp);
        long now = System.currentTimeMillis();
        Date nowDate  = new Date(now);
        if ((now - timestamp) < (1000 * 60 * 60)) {
            long timePassed = now - timestamp;
            long hours = timePassed / (1000 * 60);
            return hours + " mins ago";
        } else if ((now - timestamp) < (1000 * 60 * 60 * 24)) {
            long timePassed = now - timestamp;
            long hours = timePassed / (1000 * 60 * 60);
            return hours + " hours ago";
        } else if (date.getYear() == nowDate.getYear()) {
            return convertMonthNumberToString(date.getMonth() + 1) + date.getDate();
        }
        return convertMonthNumberToString(date.getMonth() + 1) + date.getDate() + (date.getYear() + 1900);
    }

    private static String convertMonthNumberToString(int i) {
        switch (i) {
            case 1:
                return "Jan ";
            case 2:
                return "Feb ";
            case 3:
                return "Mar ";
            case 4:
                return "Apr ";
            case 5:
                return "May ";
            case 6:
                return "Jun ";
            case 7:
                return "Jul ";
            case 8:
                return "Aug ";
            case 9:
                return "Sep ";
            case 10:
                return "Oct ";
            case 11:
                return "Nov ";
            case 12:
                return "Dec ";
             default:
                return "";
        }
    }

    public static String formatEthereumAccount(String account) {
        return account.substring(0,4) + "..." + account.substring(account.length() -4,account.length() - 1);
    }

    public static String formatTransactionHash(String account) {
        return "tx:" + account.substring(0,4) + "..." + account.substring(account.length() -4,account.length() - 1);
    }

    public static String convertActionIdForDisplay(int actionId) {
        switch (actionId) {
            case DatabaseHelper.TX_ACTION_ID_SEND_ETH:
                return "SEND ETH";
            case DatabaseHelper.TX_ACTION_ID_REGISTER:
                return "REGISTER";
            case DatabaseHelper.TX_ACTION_ID_UPDATE_USER_PIC:
                return "UPDATE PIC";
            case DatabaseHelper.TX_ACTION_ID_PUBLISH_USER_CONTENT:
                return "PUBLISH USER";
            case DatabaseHelper.TX_ACTION_ID_CREATE_PUBLICATION:
                return "CREATE PUBLICATION";
            case DatabaseHelper.TX_ACTION_ID_SUPPORT_POST:
                return "SUPPORT_POST";
            case DatabaseHelper.TX_ACTION_ID_WITHDRAW_AUTHOR_CLAIM:
                return "WITHDRAW_AUTHOR_CLAIM";
            case DatabaseHelper.TX_ACTION_ID_WITHDRAW_ADMIN_CLAIM:
                return "WITHDRAW_ADMIN_CLAIM";
            case DatabaseHelper.TX_ACTION_ID_PERMISSION_AUTHOR:
            return "PERMISSION_AUTHOR";
            case DatabaseHelper.TX_ACTION_ID_PUBLISH_TO_PUBLICATION:
            default:
                return "PUBLISH PUBLICATION";

        }
    }

    public static String formatAccountBalanceEther(String weiBalance, int numDecimals) {
        String weiString = String.valueOf(weiBalance);
        String ethString = "";
        if (weiString.length() > 18) {
            ethString = weiString.substring(0, weiString.length() - 18) + "." + weiString.substring(weiString.length() - 18, weiString.length() - 18 + numDecimals);
        } else {
            ethString = "0.";
            int zeroCounter = 18 - weiString.length();
            for (int i=0; i<zeroCounter && i < numDecimals; i++) {
                ethString += "0";
            }
            int numsRemaining = numDecimals - (ethString.length() - 2);
            ethString += weiString.substring(0, numsRemaining);
        }
        return ethString + " ETH";
    }

    public static String formatBlockNumber(long blockNumber) {
        String.valueOf(blockNumber);
        DecimalFormat formatter = new DecimalFormat("#,###");
        String blockNumberString = formatter.format(blockNumber);
        return "#" + blockNumberString;
    }
}
