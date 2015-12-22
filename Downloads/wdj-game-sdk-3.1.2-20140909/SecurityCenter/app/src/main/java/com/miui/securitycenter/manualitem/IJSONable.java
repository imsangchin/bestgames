package com.miui.securitycenter.manualitem;

public interface IJSONable {

    public boolean parseJson(String jsonStr);
    public String toJson();
}
