package me.dewrs.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    public static boolean isValidTime(String input) {
        if (input == null) return false;
        String pattern = "(?i)^(\\d+(s|m|h|d|w|mo|ye|seg|min|hour|day|week|month|year)|perma|permanent)$";
        return input.matches(pattern);
    }

    public static Duration getDurationFromTime(String time) {
        time = time.trim().toLowerCase();

        if (time.endsWith("perma") || time.endsWith("permanent")) {
            return null;
        }

        String num = time.replaceAll("[^0-9]", "");
        String suffix = time.replaceAll("[0-9]", "");

        long amount = Long.parseLong(num);

        switch (suffix) {
            case "s":
            case "seg":
                return Duration.ofSeconds(amount);
            case "m":
            case "min":
                return Duration.ofMinutes(amount);
            case "h":
            case "hour":
                return Duration.ofHours(amount);
            case "d":
            case "day":
                return Duration.ofDays(amount);
            case "w":
            case "week":
                return Duration.ofDays(amount * 7);
            case "mo":
            case "month":
                return Duration.ofDays(amount * 30);
            case "ye":
            case "year":
                return Duration.ofDays(amount * 365);
        }
        return null;
    }

    public static String getTimeFromMilis(long milis){
        if(milis == -1){
            return "Permanent";
        }
        long seconds = milis/1000;
        long totalMinWait = seconds/60;
        long totalHourWait = totalMinWait/60;
        long totalDayWait = totalHourWait/24;

        String time = "";
        if(seconds > 59){
            seconds = seconds - 60*totalMinWait;
        }
        if(seconds > 0) {
            time = seconds+"s";
        }
        if(totalMinWait > 59){
            totalMinWait = totalMinWait - 60*totalHourWait;
        }
        if(totalMinWait > 0){
            time = totalMinWait+"m"+" "+time;
        }
        if(totalHourWait > 23) {
            totalHourWait = totalHourWait - 24*totalDayWait;
        }
        if(totalHourWait > 0){
            time = totalHourWait+"h"+" " + time;
        }
        if(totalDayWait > 0) {
            time = totalDayWait+"d"+" " + time;
        }
        if(time.endsWith(" ")) {
            time = time.substring(0, time.length()-1);
        }

        return time;
    }

    public static String getDateFromMillis(long milis){
        ZonedDateTime date = Instant.ofEpochMilli(milis).atZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }
}
