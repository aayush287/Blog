package com.eyev.blog;

import com.google.firebase.Timestamp;

import java.util.Date;

public class BlogPost extends BlogPostId {
    private String description;
    private String image;
    private String userId;
    private String thumbnailUrl;
    private Date timestamp;

    public BlogPost() {
    }

    public BlogPost(String description, String image, String user_id, String thumbnail_url, Date timestamp) {
        this.description = description;
        this.image = image;
        this.userId = user_id;
        this.thumbnailUrl = thumbnail_url;
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String user_id) {
        this.userId = user_id;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnail_url) {
        this.thumbnailUrl = thumbnail_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "BlogPost{" +
                "description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", user_id='" + userId + '\'' +
                ", thumbnail_url='" + thumbnailUrl + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
