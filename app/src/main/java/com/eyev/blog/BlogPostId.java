package com.eyev.blog;

import com.google.firebase.firestore.Exclude;

import javax.annotation.Nonnull;

public class BlogPostId {
    @Exclude
    public String blogPostId;

    public <T extends BlogPostId> T withId(@Nonnull final String id){
        this.blogPostId = id;
        return (T) this;
    }
}
