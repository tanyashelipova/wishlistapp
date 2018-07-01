package ru.wishlistapp.wishlist.model.friendlistmodel;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendListModel {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("friends")
    @Expose
    private List<FriendModel> friends = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<FriendModel> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendModel> friends) {
        this.friends = friends;
    }

}