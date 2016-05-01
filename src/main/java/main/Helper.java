package main;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;

/**
 * Created by vladislav on 01.05.16.
 */
public class Helper {

    public static final int DUPLICATE_ENTRY = 1062;
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String serializeBoolean(boolean input) {
        return (input) ? "1" : "0";
    }

    public static void disableDebugInfo() {
        final Properties p = new Properties(System.getProperties());
        p.setProperty("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.setProperty("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF");
        System.setProperties(p);
    }
}
