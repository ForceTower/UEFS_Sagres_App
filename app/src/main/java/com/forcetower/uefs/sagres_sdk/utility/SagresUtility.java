package com.forcetower.uefs.sagres_sdk.utility;

import android.util.Log;

import com.forcetower.uefs.sagres_sdk.SagresPortalSDK;
import com.forcetower.uefs.sagres_sdk.domain.SagresAccess;
import com.forcetower.uefs.sagres_sdk.domain.SagresCalendarItem;
import com.forcetower.uefs.sagres_sdk.domain.SagresClassDay;
import com.forcetower.uefs.sagres_sdk.domain.SagresClassDetails;
import com.forcetower.uefs.sagres_sdk.domain.SagresGrade;
import com.forcetower.uefs.sagres_sdk.domain.SagresMessage;
import com.forcetower.uefs.sagres_sdk.domain.SagresProfile;
import com.forcetower.uefs.sagres_sdk.domain.SagresSemester;
import com.forcetower.uefs.sagres_sdk.exception.SagresInfoFetchException;
import com.forcetower.uefs.sagres_sdk.exception.SagresLoginException;
import com.forcetower.uefs.sagres_sdk.parsers.SagresCalendarParser;
import com.forcetower.uefs.sagres_sdk.parsers.SagresClassParser;
import com.forcetower.uefs.sagres_sdk.parsers.SagresFullClassParser;
import com.forcetower.uefs.sagres_sdk.parsers.SagresGradesParser;
import com.forcetower.uefs.sagres_sdk.parsers.SagresMessagesParser;
import com.forcetower.uefs.sagres_sdk.parsers.SagresParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.util.HashMap;
import java.util.List;

/**
 * Created by João Paulo on 18/11/2017.
 */
public class SagresUtility {

    /**
     * This returns FULL information about user. Should only be used in case of full update
     * @param access Access to be used to fetch [Username, Password]
     * @param callback Information about the fetch, can be null
     */
    public static void getInformationFromUserWithCacheAsync(SagresAccess access, AllInformationFetchWithCacheCallback callback) {
        String username = access.getUsername();
        String password = access.getPassword();

        if (!SagresPortalSDK.isSdkInitialized()) {
            if (callback != null) {
                callback.onFailure(new SagresLoginException("Sagres SDK is not initialized"));
            }
            return;
        }

        if (username == null || password == null) {
            if (callback != null) {
                callback.onFailure(new SagresLoginException(true, false, "Fields are null"));
            }
            return;
        }

        try {
            JSONObject loginResponse = SagresConnector.login(username, password);
            if (loginResponse.has("error")) {
                Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Login has error");
                if (callback != null) {
                    callback.onFailure(new SagresLoginException(false, true, "Login has error - Timeout[Prob]"));
                }
                return;
            }

            String html = loginResponse.getString("html");
            html = SagresParser.changeCharset(html);

            boolean connected = SagresParser.connected(html);
            if (!connected) {
                callback.onFailure(new SagresLoginException(true, false, "Invalid Login"));
                return;
            } else {
                if (callback != null) {
                    callback.onLoginSuccess();
                }
            }

            String studentName = SagresParser.getUserName(html);
            String score = SagresParser.getScore(html);
            SagresPortalSDK.alertConnectionListeners(1, studentName);

            SagresPortalSDK.alertConnectionListeners(5, null);

            String studentPageHtml = null;
            for (int i = 0; i < 3; i++) {
                studentPageHtml = SagresConnector.getSagresStudentPage();
                if (studentPageHtml != null) {
                    break;
                } else {
                    SagresPortalSDK.alertConnectionListeners(6, ""+(i + 1));
                }
            }

            HashMap<String, List<SagresClassDay>> classes;
            List<SagresMessage> messages;
            List<SagresCalendarItem> calendar;
            List<SagresClassDetails> classDetails;
            if (studentPageHtml != null) {
                classes = SagresClassParser.getCompleteSchedule(studentPageHtml);
                messages = SagresMessagesParser.getStartPageMessages(studentPageHtml);
                calendar = SagresCalendarParser.getCalendar(studentPageHtml);
                classDetails = SagresFullClassParser.getClassesDetails(Jsoup.parse(studentPageHtml), "20172");
            } else {
                classes = SagresClassParser.getCompleteSchedule(html);
                messages = SagresMessagesParser.getStartPageMessages(html);
                calendar = SagresCalendarParser.getCalendar(html);
                classDetails = SagresFullClassParser.connectAndGetClassesDetails("20172");
            }

            JSONObject gradesResponse = SagresConnector.getStudentGrades();

            HashMap<String, SagresGrade> grades;
            HashMap<SagresSemester, List<SagresGrade>> semesterGrades;
            if (gradesResponse.has("error")) {
                Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Grades Error!!!!!");
                grades = (SagresProfile.getCurrentProfile() != null) ? SagresProfile.getCurrentProfile().getGrades() : null;
                semesterGrades = null;
            } else {
                String gradesHtml = gradesResponse.getString("html");
                grades = SagresUtility.getGradeHashMap(SagresGradesParser.extractGrades(gradesHtml));
                semesterGrades = SagresGradesParser.getAllGrades(gradesHtml);
            }
            SagresPortalSDK.alertConnectionListeners(2, null);

            Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Grades obtained and set");

            SagresProfile profile = new SagresProfile(studentName, messages, classes, grades);
            Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "After here");
            profile.setScore(score);
            if (classDetails != null) profile.setClassDetails(classDetails);
            if (semesterGrades != null) profile.setAllSemestersGrades(semesterGrades);
            profile.setCalendar(calendar);

            if (callback != null) {
                Log.i(SagresPortalSDK.SAGRES_SDK_TAG,"On success");
                callback.onSuccess(profile);
            }

            SagresPortalSDK.alertConnectionListeners(10, null);
            Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Finished Fetching Profile");

        } catch (JSONException e) {
            e.printStackTrace();
            if (callback != null) callback.onFailure(new SagresLoginException("JSONException in parse - Never should've happened"));
            Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "JSONException in login");
        }
    }

    private static HashMap<String,SagresGrade> getGradeHashMap(List<SagresGrade> sagresGrades) {
        HashMap<String,SagresGrade> map = new HashMap<>();
        for (SagresGrade grade : sagresGrades) {
            map.put(grade.getClassCode(), grade);
        }
        return map;
    }

    /**
     * Fetches simple user data, can be called at every need to update
     * @param callback information about the fetch, can be null
     */
    public static void getProfileInformationAsyncWithCallback(final AsyncFetchProfileInformationCallback callback) {
        if (!SagresPortalSDK.isSdkInitialized()) {
            if (callback != null) {
                callback.onFailure(new SagresInfoFetchException("SDK not initialized"));
            }
            return;
        }

        SagresAccess access = SagresAccess.getCurrentAccess();
        if (access == null) {
            if (callback != null)
                callback.onFailure(new SagresInfoFetchException("Invalid Access"));
            return;
        }

        final String username = access.getUsername();
        final String password = access.getPassword();
        if (username == null || password == null) {
            if (callback != null)
                callback.onFailure(new SagresInfoFetchException("Fields are null"));
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject loginResponse = SagresConnector.login(username, password);
                    if (loginResponse.has("error")) {
                        Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Login has error - Time out or no network");
                        if (callback != null) {
                            callback.onFailedConnect();
                        }
                        return;
                    }

                    String html = loginResponse.getString("html");
                    html = SagresParser.changeCharset(html);

                    boolean connected = SagresParser.connected(html);
                    if (!connected) {
                        if (callback != null) callback.onInvalidLogin();
                        return;
                    }

                    String studentName = SagresParser.getUserName(html);
                    String score = SagresParser.getScore(html);
                    //Changed Here
                    String studentPageHtml = null;
                    for (int i = 0; i < 3; i++) {
                        studentPageHtml = SagresConnector.getSagresStudentPage();
                        if (studentPageHtml != null) {
                            break;
                        }
                    }

                    HashMap<String, List<SagresClassDay>> classes;
                    List<SagresMessage> messages;
                    List<SagresCalendarItem> calendar;
                    if (studentPageHtml != null) {
                        classes = SagresClassParser.getCompleteSchedule(studentPageHtml);
                        messages = SagresMessagesParser.getStartPageMessages(studentPageHtml);
                        calendar = SagresCalendarParser.getCalendar(studentPageHtml);
                    } else {
                        classes = SagresClassParser.getCompleteSchedule(html);
                        messages = SagresMessagesParser.getStartPageMessages(html);
                        calendar = SagresCalendarParser.getCalendar(html);
                    }

                    if (SagresProfile.getCurrentProfile() == null) {
                        SagresProfile profile = new SagresProfile(studentName, messages, classes);
                        profile.setScore(score);
                        profile.setCalendar(calendar);
                        SagresProfile.setCurrentProfile(profile);
                    } else {
                        SagresProfile profile = SagresProfile.getCurrentProfile();
                        if (SagresClassParser.failed) classes = profile.getClasses();
                        profile.updateInformation(studentName, messages, classes);
                        profile.setScore(score);
                        profile.setCalendar(calendar);
                    }

                    JSONObject gradesResponse = SagresConnector.getStudentGrades();
                    if (gradesResponse.has("error")) {
                        Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Grades has error - Time out or no network");
                        if (callback != null) {
                            callback.onHalfCompleted(1);
                        }
                    } else {
                        Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Fetching Grades...");
                        String gradesHtml = gradesResponse.getString("html");
                        HashMap<String, SagresGrade> grades = getGradeHashMap(SagresGradesParser.extractGrades(gradesHtml));
                        SagresProfile.getCurrentProfile().placeNewGrades(grades);
                        Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Grades obtained and set");
                    }

                    if (SagresProfile.getCurrentProfile() != null) SagresProfile.setCurrentProfile(SagresProfile.getCurrentProfile());
                    if (callback != null) {
                        callback.onSuccess(SagresProfile.getCurrentProfile());
                    }

                    Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "Finished Fetching Profile");
                } catch (JSONException e) {
                    e.printStackTrace();
                    callback.onDeveloperError();
                    Log.i(SagresPortalSDK.SAGRES_SDK_TAG, "JSONException in login");
                }
            }
        };

        SagresPortalSDK.getExecutor().execute(runnable);
    }

    public interface AllInformationFetchWithCacheCallback {
        void onSuccess(SagresProfile profile);
        void onFailure(SagresLoginException e);
        void onLoginSuccess();
    }

    public interface AsyncFetchProfileInformationCallback {
        void onSuccess(SagresProfile profile);
        void onInvalidLogin();
        void onDeveloperError();
        void onFailure(SagresInfoFetchException e);
        void onFailedConnect();
        void onHalfCompleted(int completedSteps);
    }
}
