package com.wandoujia.tools.blr.common;

public class BuildComment {
    
    private String title;
    private String detail;
    private String md5;
    private String time;
    private String version;
    private String name;
    public String getTitle() {
        return title;

    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDetail() {
        return detail;
    }
    public void setDetail(String detail) {
        this.detail = detail;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getMD5() {
        return md5;
    }
    
    public void setMD5(String md5) {
        this.md5 = md5;
    }
    
}
