/*
 * Copyright (c) 2021. Aravind Chowdary
 */

package me.aravi.repost.model;

import java.util.ArrayList;

public class Repost {

    private int id;
    private String username;
    private String full_name;
    private ArrayList<String> display_url;
    private String link;
    private String timestamp;
    private String caption;
    private ArrayList<String> video_urls;
    private String profile_pic_url;

    public Repost() {

    }

    public Repost(String profile_pic_url, ArrayList<String> video_urls, String caption, String username, String full_name, ArrayList<String> display_url, String link, String timestamp) {
        this.username = username;
        this.full_name = full_name;
        this.display_url = display_url;
        this.link = link;
        this.timestamp = timestamp;
        this.caption = caption;
        this.profile_pic_url = profile_pic_url;
        this.video_urls = video_urls;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }


    public String getProfile_pic_url() {
        return profile_pic_url;
    }

    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }

    public ArrayList<String> getVideo_urls() {
        return video_urls;
    }

    public void setVideo_urls(ArrayList<String> video_urls) {
        this.video_urls = video_urls;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<String> getDisplay_url() {
        return display_url;
    }

    public void setDisplay_url(ArrayList<String> display_url) {
        this.display_url = display_url;
    }
}
