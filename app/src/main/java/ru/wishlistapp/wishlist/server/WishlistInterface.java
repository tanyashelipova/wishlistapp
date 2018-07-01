package ru.wishlistapp.wishlist.server;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.wishlistapp.wishlist.model.SuccessAndMessageModel;
import ru.wishlistapp.wishlist.model.UserModel;
import ru.wishlistapp.wishlist.model.UsersModel;
import ru.wishlistapp.wishlist.model.friendlistmodel.FriendListModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishAndMessageModel;
import ru.wishlistapp.wishlist.model.wishmodel.WishesModel;
import ru.wishlistapp.wishlist.model.wishmodel.CommentsModel;


public interface WishlistInterface {

    // логин
    @POST("/api/authenticate")
    @FormUrlEncoded
    Call<UserModel> login(
            @Field("usernameOrEmail") String usernameOrEmail,
            @Field("password") String password

    );

    // регистрация
    @POST("/api/register")
    @FormUrlEncoded
    Call<UserModel> register(
            @Field("name") String name,
            @Field("username") String username,
            @Field("email") String email,
            @Field("bday") String bday,
            @Field("password") String password,
            @Field("password2") String password2,
            @Field("imageLink") String imageLink,
            @Field("imageHashDelete") String imageHashDelete
    );

    // добавление нового желания
    @POST("/api/wishes/")
    @FormUrlEncoded
    Call<WishAndMessageModel> addWish(
            @Field("title") String title,
            @Field("content") String context,
            @Field("link") String link,
            @Field("isReceived") boolean isReceived,
            @Field("imageLink") String imageLink,
            @Field("imageHashDelete") String imageHashDelete,
            @Field("username") String username,
            @Field("userId") String userId
    );

    // получение подарков, которые добавил текущий пользователь
    @GET("/api/wishes")
    Call<WishesModel> getUsersWishes(
            @Query("userId") String userId
    );

    @DELETE("/api/wishes")
    Call<SuccessAndMessageModel> deleteUsersWishes(
            @Query("userId") String userId
    );

    // /api/wishes/:wish_id
    // получить подарок по id
    @GET("/api/wishes/{wish_id}")
    Call<WishAndMessageModel> getWishById(
            @Path("wish_id") String wish_id
    );

    // удалить подарок
    @DELETE("/api/wishes/{wish_id}")
    Call<WishAndMessageModel> deleteWish(
            @Path("wish_id") String wish_id
    );

    // редактировать подарок
    @PUT("/api/wishes/")
    @FormUrlEncoded
    Call<WishAndMessageModel> updateWish(
            @Field("_id") String _id,
            @Field("title") String title,
            @Field("content") String content,
            @Field("link") String link,
            @Field("isReceived") boolean isReceived,
            @Field("imageLink") String imageLink,
            @Field("imageHashDelete") String imageHashDelete
    );

    // Удаление профиля
    @DELETE("/api/users")
    Call<SuccessAndMessageModel> deleteUser(
            @Query("userId") String userId,
            @Query("email") String email
    );

    // Изменение данных пользователя
    @PUT("api/users")
    @FormUrlEncoded
    Call<SuccessAndMessageModel> changeUsersData(
            @Query("action") String action, // addFriend or removeFriend or changePassword or changeData
            @Field("friendUsername") String friendUsername, // username пользователя, которого в друзья добавляем/удаляем
            @Field("name") String name,
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password,
            @Field("bday") String bday,
            @Field("imageLink") String imageLink,
            @Field("imageHashDelete") String imageHashDelete,
            @Field("userId") String userId // id текущего пользователя, чтобы внести изменения в его список друзей
    );

    // Получение пользователя по username
    @GET("/api/users/")
    Call<UserModel> getUserByUsername(
            @Query("username") String username
    );

    // Получить список друзей
    @GET("api/users?friends=true")
    Call<FriendListModel> getFriendList(
            @Query("username") String username // username текущего пользователя
    );

    // Получить список желаний друзей (для ленты)
    @GET("/api/friends_wishes/{friends_usernames}")
    Call<WishesModel> getFriendsWishes(
            @Path("friends_usernames") String friends_usernames
    );

    @POST("/api/send_mail_to_confirm")
    @FormUrlEncoded
    Call<SuccessAndMessageModel> sendMailToConfirm(
            @Field("username") String username,
            @Field("email") String email
    );

    @POST("/api/resetpassword")
    @FormUrlEncoded
    Call<SuccessAndMessageModel> resetPasswordSendEmail(
            @Field("username") String username,
            @Field("email") String email
    );

    @POST("/api/restore")
    @FormUrlEncoded
    Call<SuccessAndMessageModel> restorePassword(
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password
    );


    // Комментарии
    @POST("/api/comments")
    @FormUrlEncoded
    Call<SuccessAndMessageModel> addComment(
            @Field("wishId") String wishId,
            @Field("userId") String userId,  // пользователь, который хочет удалить комментарий
            @Field("username") String username,
            @Field("wishOwnerId") String wishOwnerId,
            @Field("text") String text
    );

    @GET("/api/comments")
    Call<CommentsModel> getComments(
            @Query("wishId") String wishId
    );


    @DELETE("/api/comments")
    Call<SuccessAndMessageModel> deleteComment(
            @Query("commentId") String commentId,
            @Query("userId") String userId  // пользователь, который хочет удалить комментарий
    );

    @GET("/api/search")
    Call<UsersModel> searchUser(
            @Query("username") String username
    );

    @PUT("/api/reserve_wish/")
    @FormUrlEncoded
    Call<SuccessAndMessageModel> reserveWish(
            @Field("_id") String _id,
            @Field("username") String username
    );

}
