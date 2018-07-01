
package ru.wishlistapp.wishlist.model.wishmodel;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WishesModel {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("wishes")
    @Expose
    private List<WishModel> wishes = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public List<WishModel> getWishes() {
        return wishes;
    }

    public void setWishes(List<WishModel> wishes) {
        this.wishes = wishes;
    }

}
