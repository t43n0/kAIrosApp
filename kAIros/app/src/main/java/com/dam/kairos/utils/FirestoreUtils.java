package com.dam.kairos.utils;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirestoreUtils {

    public static String format(Timestamp ts) {
        if (ts == null) return "";
        return format(ts.toDate());
    }

    public static String format(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
