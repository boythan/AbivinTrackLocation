package com.dxv.abivinTracklocationModule.uitls;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    public static int compareTwoDate(Date from, Date to) throws ParseException {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date fromDate = df.parse(df.format(from));
        Date toDate = df.parse(df.format(to));
        return fromDate.compareTo(toDate);
    }

}
