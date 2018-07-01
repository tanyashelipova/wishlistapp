package ru.wishlistapp.wishlist.model.wishmodel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class WishModel {

    @SerializedName("isReceived")
    @Expose
    private Boolean isReceived;
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("createdDate")
    @Expose
    private String createdDate;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("imageLink")
    @Expose
    private String imageLink;
    @SerializedName("imageHashDelete")
    @Expose
    private String imageHashDelete;
    @SerializedName("reserved")
    @Expose
    private String reserved;

    public Boolean getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(Boolean isReceived) {
        this.isReceived = isReceived;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public String getImageHashDelete() {
        return imageHashDelete;
    }

    public void setReserved(String reserve) {
        this.reserved = reserved;
    }

    public String getReserved() {
        return reserved;
    }

    public void setImageHashDelete(String imageHashDelete) {
        this.imageHashDelete = imageHashDelete;
    }
}
