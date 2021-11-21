package nl.utwente.utgo;

import java.util.List;

/**
 * This class offers static calculation methods that don't fit in other classes
 */
public class PrettyPrint {

    /**
     * Makes a more human readable String from a large number by putting points in between
     * @param number - the number to be converted
     * @return - the human readable format
     */
    public static String integerPrettyPrint(int number) {
        String text = Integer.toString(number);
        if (text.length() > 4) {
            String oldText = text;
            text = "";
            for (int i = oldText.length() - 1; i > 0; i--) {
                text = oldText.charAt(i) + text;
                if ((oldText.length() - i) % 3 == 0) {
                    text = "." + text;
                }
            }
            text = oldText.charAt(0) + text;
        }
        return text;
    }

    /**
     * Converts an amount of time in milliseconds to a human readable format
     * @param time - the amount of milliseconds left
     * @return - the String of the form (x weeks y days) OR (x days y hours) OR (x hours y minutes)
     */
    public static String timePrettyPrint(long time) {
        long minutes = time / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;
        if (days > 7) return weeks + "w " + (days % 7) + "d";
        if (hours > 24) return days + "d " + (hours % 24) + "h";
        return hours + "h " + (minutes % 60) + "m";
    }

    /**
     * Converts an RGB String of the form #(0-F)^6 to a value between 0-360
     * @param rgb RGB value given
     * @return degree on the color wheel (hue)
     */
    public static float rgbToHue(String rgb) {
        //parse string to RGB values
        if (rgb == null) {return 0f;}
        float red, green, blue;
        if (rgb.charAt(0) != '#') {return 0f;}
        red = Long.parseLong("" + rgb.charAt(1) + rgb.charAt(2), 16)/255f;
        green = Long.parseLong("" + rgb.charAt(3) + rgb.charAt(4), 16)/255f;
        blue = Long.parseLong("" + rgb.charAt(5) + rgb.charAt(6), 16)/255f;


        //compute degree on color wheel (hue)
        float min = Math.min(Math.min(red,green),blue);
        float max = Math.max(Math.max(red,green),blue);
        float res = 0f;

        if (min==max) {return 0;}

        if (max == red) {
            res = (green-blue)/(max-min);
        } else if (max == green) {
            res = 2f + (blue-red)/(max-min);
        } else {
            res = 4f + (red-green)/(max-min);
        }

        //convert to degrees
        res *= 60;
        if (res < 0) {res += 360;}
        return res;
    }

    /**
     * Pretty prints a list of team members
     * @param members Members as a string list
     * @return String to be used a team description
     */
    public static String membersPrettyPrint(List<String> members) {
        if (members.size() == 1) {
            return "Your team has 1 member: " + members.get(0);
        }
        String toPrint = "Your team has " + members.size() + " members: ";
        for (int i = 0; i < members.size() - 2; i++) {
            toPrint += members.get(i) + ", ";
        }
        toPrint += members.get(members.size() - 2) + " and " + members.get(members.size() - 1) + ".";
        return toPrint;
    }

    /**
     * Pretty prints a member count for a group
     * @param memberCount Number of players in a group
     * @param title Title of the group (do-group or study association)
     * @return Description for the group
     */
    public static String memberCountPrettyPrint(int memberCount, String title) {
        String titleLower = title.toLowerCase();
        if (memberCount == 1) {
            return "Your " + titleLower + " has 1 member.";
        }
        return "Your " + titleLower + " has " + memberCount + " members.";
    }

    /**
     * Converts a number to an ordinal number (1 -> 1st, 2 -> 2nd, etc.
     * @param number Number as an integer
     * @return Ordinal number as a String
     */
    public static String numberToOrdinal(int number) {
        int lastTwoDigits = number % 100;
        if (lastTwoDigits < 4 || lastTwoDigits > 20) {
            int lastDigit = number % 10;
            if (lastDigit == 1) {
                return integerPrettyPrint(number) + "st";
            }
            if (lastDigit == 2) {
                return integerPrettyPrint(number) + "nd";
            }
            if (lastDigit == 3) {
                return integerPrettyPrint(number) + "rd";
            }
        }
        return integerPrettyPrint(number) + "th";
    }

    /**
     * Converts a parameter used in settings to a String
     * String x -> x, true -> "public", false -> "private"
     * @param val Parameter used in settings
     * @return String form of the value
     */
    public static String settingsValuesToString(Object val) {
        if (val instanceof String) {
            return val.toString();
        }
        if (val instanceof Boolean) {
            if ((Boolean) val) {
                return "public";
            } else {
                return "private";
            }
        }
        return "";
    }

}
