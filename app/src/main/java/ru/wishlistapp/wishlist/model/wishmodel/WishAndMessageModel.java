package ru.wishlistapp.wishlist.model.wishmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WishAndMessageModel {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("wish")
    @Expose
    private WishModel wish;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public WishModel getWish() {
        return wish;
    }

    public void setWish(WishModel wish) {
        this.wish = wish;
    }

}