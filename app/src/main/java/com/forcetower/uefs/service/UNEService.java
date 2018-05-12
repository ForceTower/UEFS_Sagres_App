package com.forcetower.uefs.service;

import android.arch.lifecycle.LiveData;

import com.forcetower.uefs.db_service.entity.AccessToken;
import com.forcetower.uefs.db_service.entity.Account;
import com.forcetower.uefs.db_service.entity.UpdateStatus;
import com.forcetower.uefs.db_service.entity.Version;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by João Paulo on 13/03/2018.
 */

public interface UNEService {
    @FormUrlEncoded
    @POST("oauth/token")
    LiveData<ApiResponse<AccessToken>> login(
            @Field("grant_type")    String grantType,
            @Field("username")      String username,
            @Field("password")      String password,
            @Field("client_id")     int    clientId,
            @Field("client_secret") String clientSecret,
            @Field("scope")         String scopes
    );

    @GET("update")
    LiveData<ApiResponse<UpdateStatus>> getUpdateStatus();

    @GET("version")
    LiveData<ApiResponse<Version>> getLatestVersion();

    @FormUrlEncoded
    @POST("update")
    LiveData<ApiResponse<ActionResult<UpdateStatus>>> changeUpdateStatus(@Field("manager") int manager, @Field("alarm") int alarm);

    @FormUrlEncoded
    @POST("account")
    LiveData<ApiResponse<ActionResult<Account>>> createAccount(
            @Field("name")     String name,
            @Field("username") String username,
            @Field("password") String password,
            @Field("image")    String image,
            @Field("app_account_secret") String secret
    );
}
