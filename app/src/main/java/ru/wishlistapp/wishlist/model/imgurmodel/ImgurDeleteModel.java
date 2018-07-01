
package ru.wishlistapp.wishlist.model.imgurmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImgurDeleteModel {

    @SerializedName("data")
    @Expose
    private boolean data;
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("status")
    @Expose
    private Integer status;

    public boolean getData() {
        return data;
    }

    public void setData(boolean data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
