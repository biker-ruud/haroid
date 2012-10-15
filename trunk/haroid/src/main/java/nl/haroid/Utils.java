package nl.haroid;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class Utils {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static final int EOF = -1;
    private static final int INDEX_NOT_FOUND = -1;
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String EMPTY = "";
    private static final String YEAR_MONTH_FORMAT = "yyyyMM";
    private static final String YEAR_MONTH_DAY_FORMAT = "yyyyMMdd";

    private Utils() {
        // Utility class
    }

    public static int numberOfDaysPreviousMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        return numberOfDaysInMonth(cal);
    }

    public static int numberOfDaysThisMonth() {
        Calendar cal = Calendar.getInstance();
        return numberOfDaysInMonth(cal);
    }

    public static int bepaalPeriodeNummer(int startDagPeriode) {
        Calendar cal = Calendar.getInstance();
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        if (huidigeDagVdMaand < startDagPeriode) {
            cal.roll(Calendar.MONTH, -1);
        }
        String periodeString = new SimpleDateFormat(YEAR_MONTH_FORMAT).format(cal.getTime());
        return Integer.parseInt(periodeString);
    }

    public static int bepaaldDagInPeriode(int startDagPeriode) {
        Calendar cal = Calendar.getInstance();
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        if (huidigeDagVdMaand < startDagPeriode) {
            int dagenVorigeMaand = Utils.numberOfDaysPreviousMonth();
            int dagenInPeriode = (dagenVorigeMaand - startDagPeriode) + 1;
            return dagenInPeriode += huidigeDagVdMaand;
        } else {
            return (huidigeDagVdMaand - startDagPeriode) + 1;
        }
    }

    public static int bepaalDatumCode(Date datum) {
        String dateCode = new SimpleDateFormat(YEAR_MONTH_DAY_FORMAT).format(datum);
        return Integer.parseInt(dateCode);
    }

    public static Date converteerDatumCode(int datumCode) {
        try {
            return new SimpleDateFormat(YEAR_MONTH_DAY_FORMAT).parse(String.valueOf(datumCode));
        } catch (ParseException e) {
            return null;
        }
    }

    private static int numberOfDaysInMonth(Calendar cal) {
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static Date getLastDayOfPreviousPeriod(int startDayOfPeriod) {
        Calendar cal = Calendar.getInstance();
        return getLastDayOfPreviousPeriod(startDayOfPeriod, cal);
    }

    public static Date getLastDayOfPreviousPeriod(int startDayOfPeriod, Calendar cal) {
        int huidigeDagVdMaand = cal.get(Calendar.DAY_OF_MONTH);
        if (huidigeDagVdMaand >= startDayOfPeriod) {
            int diff = huidigeDagVdMaand - startDayOfPeriod;
            cal.add(Calendar.DAY_OF_MONTH, -(diff + 1));
        } else {
            cal.add(Calendar.DAY_OF_MONTH, -huidigeDagVdMaand);
            while (cal.get(Calendar.DAY_OF_MONTH) >= startDayOfPeriod) {
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
        }
        cal.clear(Calendar.MILLISECOND);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.HOUR);
        return cal.getTime();
    }

    public static String remove(String str, String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        return replace(str, remove, EMPTY, -1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == INDEX_NOT_FOUND) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = (increase < 0 ? 0 : increase);
        increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
        StringBuilder buf = new StringBuilder(text.length() + increase);
        while (end != INDEX_NOT_FOUND) {
            buf.append(text.substring(start, end)).append(replacement);
            start = end + replLength;
            if (--max == 0) {
                break;
            }
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static String deleteWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }

    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.indexOf(searchStr) >= 0;
    }

    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        int len = searchStr.length();
        int max = str.length() - len;
        for (int i = 0; i <= max; i++) {
            if (str.regionMatches(true, i, searchStr, 0, len)) {
                return true;
            }
        }
        return false;
    }

    public static String toString(InputStream input) throws IOException {
        StringWriter writer = new StringWriter();
        InputStreamReader in = new InputStreamReader(input);
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while (EOF != (n = in.read(buffer))) {
            writer.write(buffer, 0, n);
        }
        return writer.toString();
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    public static String substringBetween(String str, String open, String close) {
        if (str == null || open == null || close == null) {
            return null;
        }
        int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            int end = str.indexOf(close, start + open.length());
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return null;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String substringBefore(String str, String separator) {
        if (isEmpty(str) || separator == null) {
            return str;
        }
        if (separator.length() == 0) {
            return EMPTY;
        }
        int pos = str.indexOf(separator);
        if (pos == INDEX_NOT_FOUND) {
            return str;
        }
        return str.substring(0, pos);
    }

    public static String[] substringsBetween(String str, String open, String close) {
        if (str == null || isEmpty(open) || isEmpty(close)) {
            return null;
        }
        int strLen = str.length();
        if (strLen == 0) {
            return EMPTY_STRING_ARRAY;
        }
        int closeLen = close.length();
        int openLen = open.length();
        List list = new ArrayList();
        int pos = 0;
        while (pos < (strLen - closeLen)) {
            int start = str.indexOf(open, pos);
            if (start < 0) {
                break;
            }
            start += openLen;
            int end = str.indexOf(close, start);
            if (end < 0) {
                break;
            }
            list.add(str.substring(start, end));
            pos = end + closeLen;
        }
        if (list.isEmpty()) {
            return null;
        }
        return (String[]) list.toArray(new String [list.size()]);
    }

    public static String join(String[] array, String separator) {
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<array.length; i++) {
            builder.append(array[i]);
            if (i != (array.length-1)) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

}
