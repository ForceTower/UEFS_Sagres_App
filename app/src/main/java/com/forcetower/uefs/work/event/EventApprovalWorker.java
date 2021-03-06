package com.forcetower.uefs.work.event;

import android.content.Context;
import android.support.annotation.NonNull;

import com.forcetower.uefs.Constants;
import com.forcetower.uefs.UEFSApplication;
import com.forcetower.uefs.db_service.ServiceDatabase;
import com.forcetower.uefs.db_service.entity.Event;
import com.forcetower.uefs.ntf.NotificationCreator;
import com.forcetower.uefs.service.ActionResult;
import com.forcetower.uefs.service.UNEService;

import java.io.IOException;

import javax.inject.Inject;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;
import timber.log.Timber;

public class EventApprovalWorker extends Worker {
    @Inject
    UNEService service;
    @Inject
    ServiceDatabase database;

    public static void createWorker(String uuid) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        Data data = new Data.Builder()
                .putString("event_uuid", uuid)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(EventApprovalWorker.class)
                .setConstraints(constraints)
                .setInputData(data)
                .addTag(Constants.APPROVE_EVENT_WORKER + uuid)
                .build();

        WorkManager.getInstance().enqueue(request);
    }


    public EventApprovalWorker(Context context, WorkerParameters parameters) {
        super(context, parameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        ((UEFSApplication)getApplicationContext()).getAppComponent().inject(this);

        String uuid = getInputData().getString("event_uuid");
        if (uuid == null) {
            Timber.d("Uuid is null. leaving..");
            return Result.failure();
        }
        Call<ActionResult<Event>> call = service.approveEvent(uuid);
        try {
            Response<ActionResult<Event>> response = call.execute();
            if (response.isSuccessful()) {
                ActionResult<Event> body = response.body();
                if (body != null && body.getData() != null) {
                    database.eventDao().markEventApproved(uuid);
                    return Result.success();
                } else {
                    Timber.d("Response data is null %s", body);
                    NotificationCreator.createNotificationWithMessage(getApplicationContext(), "Approve Event", "Approval failed, response data is null");
                    return Result.failure();
                }
            } else {
                if (response.code() == 403) {
                    NotificationCreator.createNotificationWithMessage(getApplicationContext(), "Approve Event", "No permission");
                    return Result.failure();
                } else if (response.code() == 500) {
                    return Result.failure();
                } else {
                    NotificationCreator.createNotificationWithMessage(getApplicationContext(), "Approve Event", "Failed with code: " + response.code());
                    return Result.failure();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}
