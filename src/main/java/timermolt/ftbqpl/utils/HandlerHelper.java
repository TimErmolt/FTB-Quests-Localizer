package timermolt.ftbqpl.utils;

import timermolt.ftbqpl.FTBQuestPrecisionLocalizerMod;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class HandlerHelper {
    public static final TreeMap<String, String> transKeys = new TreeMap<>();
    private static String prefix;
    private static int counter = 0;
    private static int description = 0;
    private static int image = 0;
    public static List<String> descList = new ArrayList<>();

    public static Logger log = FTBQuestPrecisionLocalizerMod.log;

    private HandlerHelper() {
    }

    public static String getPrefix() {
        return prefix;
    }

    public static void setPrefix(String prefix) {
        HandlerHelper.prefix = prefix;
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        HandlerHelper.counter = counter;
    }
    public static void addCounter() {
        HandlerHelper.counter++;
    }

    public static int getDescription() {
        return description;
    }

    public static void setDescription(int description) {
        HandlerHelper.description = description;
    }
    public static void addDescription() {
        HandlerHelper.description++;
    }
    public static int getImage() {
        return image++;
    }

    public static void setImage(int image) {
        HandlerHelper.image = image;
    }
    public static void addImage() {
        HandlerHelper.image++;
    }
}
