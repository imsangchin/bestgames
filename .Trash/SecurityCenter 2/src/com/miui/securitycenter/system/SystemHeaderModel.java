
package com.miui.securitycenter.system;


public class SystemHeaderModel {

    private SystemType mSystemType;
    private String mHeader;

    public SystemHeaderModel() {

    }

    public SystemType getProtectionType() {
        return mSystemType;
    }

    public void setSystemType(SystemType systemType) {
        this.mSystemType = systemType;
    }

    public String getHeader() {
        return mHeader;
    }

    public void setHeader(String header) {
        this.mHeader = header;
    }

    @Override
    public String toString() {
        return "SystemHeaderModel mSystemType = " + mSystemType + " Header = " + mHeader;
    }
}
