package com.yourname.qrinventory.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    Call<OffResponse> getProduct(@Path("barcode") String barcode,
                                 @Query("fields") String fields,
                                 @Header("User-Agent") String userAgent);
}