
package com.miui.antivirus;

import java.io.Serializable;

import com.miui.antivirus.VirusCheckManager.ScanItemType;
import com.miui.antivirus.VirusCheckManager.ScanResultType;

public class VirusModel implements Serializable {

    private static final long serialVersionUID = 810927208675811171L;

    private ScanResultType mResultType;
    private ScanItemType mItemType;
    private String mPkgName;
    private String mAppLabel;
    private String mSourceDir;
    private String mVirusDescx;
    private String mVirusName;

    public VirusModel() {
        // ignore
    }

    public void setScanResultType(ScanResultType resultType) {
        mResultType = resultType;
    }

    public ScanResultType getScanResultType() {
        return mResultType;
    }

    public void setScanItemType(ScanItemType itemType) {
        mItemType = itemType;
    }

    public ScanItemType getScanItemType() {
        return mItemType;
    }

    public void setPkgName(String pkgName) {
        mPkgName = pkgName;
    }

    public String getPkgName() {
        return mPkgName;
    }

    public void setAppLabel(String appLabel) {
        mAppLabel = appLabel;
    }

    public String getAppLabel() {
        return mAppLabel;
    }

    public void setSourceDir(String sourceDir) {
        mSourceDir = sourceDir;
    }

    public String getSourceDir() {
        return mSourceDir;
    }

    public void setVirusDescx(String descx) {
        mVirusDescx = descx;
    }

    public String getVirusDescx() {
        return mVirusDescx;
    }

    public void setVirusName(String name) {
        mVirusName = name;
    }

    public String getVirusName() {
        return mVirusName;
    }
}
