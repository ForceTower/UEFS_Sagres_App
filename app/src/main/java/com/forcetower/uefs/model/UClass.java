package com.forcetower.uefs.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by João Paulo on 10/11/2017.
 */

public class UClass {
    private String name;
    private String code;
    private List<String> classes;
    private List<UClassDay> days;

    public UClass(String code) {
        this.code = code;
        classes = new ArrayList<>();
        days = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void addClazz(String aClass) {
        aClass = aClass.trim();
        if (!containsClazz(aClass))
            this.classes.add(aClass);
    }

    public List<String> getClasses() {
        return classes;
    }

    public String getClazz() {
        return classes.size() > 0 ? classes.get(0) : "Unique";
    }

    public void addStartEndTime(String start, String finish, String day, String classType) {
        UClassDay classDay = new UClassDay(start, finish, day, classType, this);
        days.add(classDay);
    }

    public long getTotalTimePerWeek() {
        long value = 0;
        for (UClassDay classDay : days) {
            value += classDay.getDuration();
        }

        return value;
    }

    private boolean containsClazz(String clazz) {
        return classes.contains(clazz);
    }

    @Override
    public String toString() {
        return "class: {\n" +
                "\tcode: " + code + "\n" +
                "\tname: " + name + "\n" +
                "\ttime: " + getTotalTimePerWeek() + "h per week\n" +
                "\tdays: [\n" + getClassDaysString() + "\n" +
                "\t]\n" +
                "},";
    }

    public String getClassDaysString() {
        StringBuilder builder = new StringBuilder();

        for (UClassDay classDay : days) {
            String day = classDay.getDay();
            String start = classDay.getStartString();
            String finish = classDay.getFinishString();
            String classType = classDay.getClassType();
            String allocatedRoom = classDay.getAllocatedRoom();
            String place = classDay.getPlace();

            builder.append("\t\t{\n");
            builder.append("\t\t\tday: ").append(day).append("\n");
            builder.append("\t\t\tstarts_at: ").append(start).append("\n");
            builder.append("\t\t\tends_at: ").append(finish).append("\n");
            builder.append("\t\t\tclass_type: ").append(classType).append("\n");
            builder.append("\t\t\tallocated_room: ").append(allocatedRoom).append("\n");
            builder.append("\t\t\tplace: ").append(place).append("\n");
            builder.append("\t\t},\n");
        }

        return builder.toString();
    }

    public void addAtToAllClasses(String at) {
        String[] parts = at.split(",");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        String allocatedRoom;
        String place;
        String campus;

        if (parts.length == 3) {
            campus = parts[0];
            place = parts[1];
            allocatedRoom = removeRoomName(parts[2]);
        } else if (parts.length == 2) {
            campus = parts[0];
            allocatedRoom = removeRoomName(parts[1]);
            place = "testing_please_report";
        } else {
            allocatedRoom = removeRoomName(parts[0]);
            campus = "testing_please_report_2";
            place = "testing_please_report_2";
        }


        for (UClassDay classDay : days) {
            classDay.setAllocatedRoom(allocatedRoom);
            classDay.setCampus(campus);
            classDay.setPlace(place);
        }
    }

    public void addAtToSpecificClass(String at, String day, String type) {
        String[] parts = at.split(",");

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        String allocatedRoom;
        String place;
        String campus;

        if (parts.length == 3) {
            campus = parts[0];
            place = parts[1];
            allocatedRoom = removeRoomName(parts[2]);
        } else if (parts.length == 2) {
            campus = parts[0];
            allocatedRoom = removeRoomName(parts[1]);
            place = "testing_please_report_3";
        } else {
            allocatedRoom = removeRoomName(parts[0]);
            campus = "testing_please_report_4";
            place = "testing_please_report_4";
        }

        for (UClassDay classDay : days) {
            if (classDay.getDay().equals(day) && classDay.getClassType().equals(type)) {
                classDay.setAllocatedRoom(allocatedRoom);
                classDay.setCampus(campus);
                classDay.setPlace(place);
            }
        }
    }

    private String removeRoomName(String part) {
        if (part.startsWith("Sala")) {
            part = part.substring(4);
        }

        return part.trim();
    }

    public List<UClassDay> getDays() {
        return days;
    }
}