package ru.wishlistapp.wishlist.server;

import okhttp3.MultipartBody;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurDeleteModel;
import ru.wishlistapp.wishlist.model.imgurmodel.ImgurUploadModel;

public interface ImgurInterface {

    @Headers({
            "Authorization: Client-ID *****"
    })

    @DELETE("image/{imageDeleteHash}")
    Call<ImgurDeleteModel> deleteImage(
            @Path("imageDeleteHash") String imageDeleteHash
    );

    @Multipart
    @Headers({
            "Authorization: Client-ID *****"
    })
    @POST("image")
    Call<ImgurUploadModel> postImage(
            @Query("title") String title,
            @Query("description") String description,
            @Query("album") String albumId,
            @Query("account_url") String username,
            @Part MultipartBody.Part file);

    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.imgur.com/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
