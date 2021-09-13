package com.example.eam.common;

import android.graphics.Bitmap;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Common {
    public static Bitmap IMAGE_BITMAP;

    public static String getFormattedDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String returnedDate = formatter.format(date);
        return returnedDate;
    }

    public static String getJodaTimeFormattedDate(LocalDate date){
        String formattedDate = date.toString("dd-MM-yyyy");
        return formattedDate;
    }
}
