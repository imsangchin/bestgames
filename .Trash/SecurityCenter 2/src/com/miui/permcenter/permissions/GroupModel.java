
package com.miui.permcenter.permissions;

public class GroupModel {

    public GroupModel() {
        // ignore
    }

    private long mId;
    private String mName;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public String toString() {
        return "GroupModel mId = " + mId + " mName = " + mName;
    }
}
