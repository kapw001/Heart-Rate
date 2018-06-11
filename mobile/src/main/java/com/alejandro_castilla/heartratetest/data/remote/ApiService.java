package com.alejandro_castilla.heartratetest.data.remote;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by yasar on 6/3/18.
 */

public interface ApiService {

    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("mobile/rest/login")
    Observable<String> loginValidate(@Body String json);

    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("mobile/rest/student_heartrate")
    Observable<String> upadateHeartRate(@Body String json);

    @Headers({"Content-Type: application/json;charset=UTF-8"})
    @POST("mobile/rest/update_student_location")
    Observable<String> updateStudentLocation(@Body String json);

}
