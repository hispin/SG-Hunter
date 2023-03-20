package com.sensoguard.hunter.classes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import static com.sensoguard.hunter.global.ConstsKt.ALARM_OTHER;

public class AlarmParsing {
    private static final AlarmParsing ourInstance = new AlarmParsing();

    private AlarmParsing() {
    }

    public static AlarmParsing getInstance() {
        return ourInstance;
    }


    //parse intent of push notification to alarm
    public Alarm parsePushToAlarm(Intent inn) {
        String latitudeStr = inn.getStringExtra("latitude");
        double latitude = -1;
        if (latitudeStr != null) {
            latitudeStr = latitudeStr.trim();
            try {
                latitude = Double.parseDouble(latitudeStr);
            } catch (NumberFormatException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        String longitudeStr = inn.getStringExtra("longitude");
        double longitude = -1;
        if (longitudeStr != null) {
            longitudeStr = longitudeStr.trim();
            try {
                longitude = Double.parseDouble(longitudeStr);
            } catch (NumberFormatException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        String cameraId = inn.getStringExtra("cameraId");
        String cameraName = inn.getStringExtra("cameraName");
        String cameraModel = inn.getStringExtra("cameraModel");
        String imageUrl = inn.getStringExtra("imageUrl");
        String networkLevel = inn.getStringExtra("networkLevel");
        String batteryLevelStr = inn.getStringExtra("batteryLevel");
        float batteryLevel = -1;
        if (batteryLevelStr != null && batteryLevelStr.length() > 0) {
            int idx = batteryLevelStr.lastIndexOf('%');
            if (idx != -1) {
                batteryLevelStr = batteryLevelStr.substring(0, batteryLevelStr.lastIndexOf('%'));
                if (batteryLevelStr != null) {
                    batteryLevelStr = batteryLevelStr.trim();
                    try {
                        batteryLevel = Float.parseFloat(batteryLevelStr);
                    } catch (NumberFormatException | NullPointerException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        String dateStr = inn.getStringExtra("date");
        long date = -1;
        if (dateStr != null) {
            dateStr = dateStr.trim();
            try {
                date = Long.parseLong(dateStr);
            } catch (NumberFormatException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }

        ////test
//        String dateStrZuraSent = inn.getStringExtra("sendDate");
//        long dateZuraSent = -1;
//        if (dateStrZuraSent != null) {
//            dateStrZuraSent = dateStrZuraSent.trim();
//            try {
//                dateZuraSent = Long.parseLong(dateStrZuraSent);
//            } catch (NumberFormatException | NullPointerException ex) {
//                ex.printStackTrace();
//            }
//        }
//
//        String dateStrfDateSent = inn.getStringExtra("fDate");
//        long datefDateSent = -1;
//        if (dateStrfDateSent != null) {
//            dateStrfDateSent = dateStrfDateSent.trim();
//            try {
//                datefDateSent = Long.parseLong(dateStrfDateSent);
//            } catch (NumberFormatException | NullPointerException ex) {
//                ex.printStackTrace();
//            }
//        }
//        String dateStrsDateSent = inn.getStringExtra("sDate");
//        long datesDateSent = -1;
//        if (dateStrsDateSent != null) {
//            dateStrsDateSent = dateStrsDateSent.trim();
//            try {
//                datesDateSent = Long.parseLong(dateStrsDateSent);
//            } catch (NumberFormatException | NullPointerException ex) {
//                ex.printStackTrace();
//            }
//        }
//        long acceptDate = Calendar.getInstance().getTimeInMillis();
        ////


        float wifiNotif = -1;//parseToWifiNotif(networkLevel);
        if (networkLevel != null && networkLevel.length() > 4) {
            String wifiStr = networkLevel.substring(0, 2);
            try {
                wifiNotif = Float.parseFloat(wifiStr);
            } catch (NumberFormatException | NullPointerException ex) {
                ex.printStackTrace();
            }
        }

        Alarm myAlarm = new Alarm(cameraId, cameraName, cameraModel, date, imageUrl, latitude, longitude, wifiNotif, batteryLevel);
        return myAlarm;
    }


    void parseByModel(Camera camera, String mySubject, Alarm myAlarm, String myContent, Context context1) {
        if (camera.getCameraModel() == null)
            return;

        WeakReference<Context> wContext = new WeakReference<Context>(context1);

        Calendar myCalendar = null;
        float batteryVal = -1;
        float wifiVal = -1;
        switch (camera.getCameraModel()) {
            case ("HIKVISION"):
                myCalendar = parseDateTimeByContent(myContent);
                break;
            case ("MG-984G-30M"):
                //parsing the date time
                myCalendar = parseDateTimeBySubject(mySubject);
                //parsing wifi by subject
                wifiVal = parseWifiBySubject(mySubject);
                break;
            case ("MG-983G-30M"):
                //parsing the date time
                myCalendar = parseDateTimeBySubject(mySubject);
                //parsing battery by content
                batteryVal = parseBatteryByContent(myContent);
                //parsing wifi by subject
                wifiVal = parseWifiBySubject(mySubject);
                break;
            case ("BG-668"):
            case ("MG-983G-36M"):
            case ("MG-984G-36M"):
                //parsing the date time
                myCalendar = parseDateTimeBySubject(mySubject);


                //parsing battery
                batteryVal = parseBatteryByContent(myContent);


                //parsing wifi
                wifiVal = parseWifiByContent(myContent);


                break;
            default:
                throw new IllegalStateException("Unexpected value: " + camera.getCameraModel());
        }
        if (myCalendar != null) {
            myAlarm.addAlarmToHistory(camera, ALARM_OTHER, wContext.get(), myCalendar, batteryVal, wifiVal);
            Log.d("", "");
        }


    }


    //parsing wifi by subject
    private float parseToWifiNotif(String wifiNotif) {
        float wifiVal = -1;
        try {
            //String[] arr = wifiNotif.split("-");
            if (wifiNotif.length() > 4) {

                String wifiStr = wifiNotif.substring(0, 2);
                float wifi = Float.parseFloat(wifiStr);
                if (wifi >= 1 && wifi < 8) {
                    wifiVal = 24;
                } else if (wifi >= 8 && wifi < 15) {
                    wifiVal = 49;
                } else if (wifi >= 15 && wifi < 24) {
                    wifiVal = 74;
                } else if (wifi >= 24 && wifi < 30) {
                    wifiVal = 99;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wifiVal;
    }

    //parsing wifi by subject
    private float parseWifiBySubject(String mySubject) {
        float wifiVal = -1;
        try {
            String[] arr = mySubject.split("-");
            if (arr.length > 3) {
                String wifiStr = arr[4];
                wifiStr = wifiStr.substring(0, 2);
                float wifi = Float.valueOf(wifiStr);
                if (wifi >= 1 && wifi < 8) {
                    wifiVal = 24;
                } else if (wifi >= 8 && wifi < 15) {
                    wifiVal = 49;
                } else if (wifi >= 15 && wifi < 24) {
                    wifiVal = 74;
                } else if (wifi >= 24 && wifi < 30) {
                    wifiVal = 99;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return wifiVal;
    }

    //parsing wifi
    private float parseWifiByContent(String myContent) {
        try {
            int tmpIdx = myContent.indexOf("Signal");
            String tmpStr = myContent.substring(tmpIdx);
            tmpIdx = tmpStr.indexOf("%");
            String wifiValStr = tmpStr.substring(tmpStr.indexOf(":") + 1, tmpIdx);
            return Float.valueOf(wifiValStr);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1f;
    }

    //parsing battery
    private float parseBatteryByContent(String myContent) {
        try {
            //String myContent = getTextFromMessage(unReadLastDayMsg);
            int tmpIdx = myContent.indexOf("Battery");
            String tmpStr = myContent.substring(tmpIdx);
            tmpIdx = tmpStr.indexOf("%");
            String batteryValStr = tmpStr.substring(tmpStr.indexOf(":") + 1, tmpIdx);
            return Float.valueOf(batteryValStr);
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1f;
        }
    }

    //parsing the date time
    private Calendar parseDateTimeBySubject(String mySubject) {
        String[] arr = mySubject.split("-");
        if (arr.length > 1) {
            String datetimeStr = arr[2];
            String[] datetimeArr = datetimeStr.split(" ");
            if (datetimeArr.length > 1) {
                return getCalendarByString(datetimeArr);
            }
        }
        return null;
    }

    //parsing the date time by content
    private Calendar parseDateTimeByContent(String myContent) {
        int tmpIdx = myContent.indexOf("EVENT TIME");
        String tmpStr = myContent.substring(tmpIdx);
        tmpIdx = tmpStr.indexOf("\n");
        String calendarValStr = tmpStr.substring(tmpStr.indexOf(":") + 1, tmpIdx);
        //remove the space
        calendarValStr = calendarValStr.trim();
        String[] arr = calendarValStr.split(",");
        if (arr.length > 1) {
            return getCalendarByStringHIKVISION(arr);
        }
        return null;
    }

    //parse the String to Calendar
    private Calendar getCalendarByString(String[] datetimeArr) {

        String date = datetimeArr[0];
        String time = datetimeArr[1];

        String[] timeArr = time.split(":");

        try {

            if (timeArr.length > 1) {
                String hour = timeArr[0];
                String minutes = timeArr[1];

                String[] dateArr = date.split("/");
                if (dateArr.length > 1) {
                    String day = dateArr[1];
                    String month = dateArr[0];

                    Calendar myCalendar = Calendar.getInstance();
                    int year = myCalendar.get(Calendar.YEAR);
                    int monthVal = Integer.valueOf(month);
                    int dayVal = Integer.valueOf(day);
                    int hourVal = Integer.valueOf(hour);
                    int minutesVal = Integer.valueOf(minutes);

                    //the month start with 0
                    myCalendar.set(year, monthVal - 1, dayVal, hourVal, minutesVal);
                    return myCalendar;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //parse the String to Calendar
    private Calendar getCalendarByStringHIKVISION(String[] datetimeArr) {

        String date = datetimeArr[0];
        String time = datetimeArr[1];

        String[] timeArr = time.split(":");

        try {

            if (timeArr.length > 2) {
                String hour = timeArr[0];
                String minutes = timeArr[1];
                String seconds = timeArr[2];

                String[] dateArr = date.split("-");
                if (dateArr.length > 2) {
                    String day = dateArr[2];
                    String month = dateArr[1];
                    String year = dateArr[0];

                    Calendar myCalendar = Calendar.getInstance();
                    int yearVal = Integer.valueOf(year);
                    int monthVal = Integer.valueOf(month);
                    int dayVal = Integer.valueOf(day);
                    int hourVal = Integer.valueOf(hour);
                    int minutesVal = Integer.valueOf(minutes);
                    int secondsVal = Integer.valueOf(seconds);

                    //the month start with 0
                    myCalendar.set(yearVal, monthVal - 1, dayVal, hourVal, minutesVal, secondsVal);
                    return myCalendar;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
