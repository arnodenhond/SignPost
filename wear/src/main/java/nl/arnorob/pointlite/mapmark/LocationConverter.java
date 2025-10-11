package nl.arnorob.pointlite.mapmark;


import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

public class LocationConverter {

    public String latitudeAsDMS(double latitude, int decimalPlace) {
        String direction = (latitude > 0)?"N":"S";
        String strLatitude = Location.convert(Math.abs(latitude), Location.FORMAT_SECONDS);
        strLatitude = replaceDelimiters(strLatitude, decimalPlace);
        strLatitude += " "+direction;
        return strLatitude;
    }

    public String longitudeAsDMS(double longitude, int decimalPlace){
        String direction = (longitude > 0)?"W":"E";
        String strLongitude = Location.convert(Math.abs(longitude), Location.FORMAT_SECONDS);
        strLongitude = replaceDelimiters(strLongitude, decimalPlace);
        strLongitude += " "+direction;
        return strLongitude;
    }

    @NonNull
    private static String replaceDelimiters(String pstr, int decimalPlace) {
        String str = pstr;
        Log.d("nl.arnorob.pointline",pstr);
        str = str.replaceFirst(":", "°");
        str = str.replaceFirst(":", "'");
        int pointIndex = str.indexOf(".");
        if (pointIndex==-1)
            pointIndex = str.length()-1;
        int endIndex = pointIndex + 1 + decimalPlace;
        if(endIndex < str.length()) {
            str = str.substring(0, endIndex);
        }
        str = str + "\"";
        Log.d("nl.arnorob.pointline",str);
        return str;
    }

}