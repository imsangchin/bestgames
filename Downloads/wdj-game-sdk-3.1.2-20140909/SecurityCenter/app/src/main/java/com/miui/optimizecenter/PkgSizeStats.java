
package com.miui.optimizecenter;

import android.content.pm.PackageStats;

public class PkgSizeStats {
    public PkgSizeStats() {
        // ignore
    }

    public String packageName;

    public long cacheSize;
    public long codeSize;
    public long dataSize;
    public long externalCodeSize;
    public long externalDataSize;
    public long externalCacheSize;
    public long internalSize;
    public long externalSize;

    public long getTotalInternalSize(PackageStats ps) {
        if (ps != null) {
            return ps.codeSize + ps.dataSize;
        }
        return 0;
    }

    public long getTotalExternalSize(PackageStats ps) {
        if (ps != null) {
            return ps.externalCodeSize + ps.externalDataSize
                    + ps.externalMediaSize + ps.externalObbSize;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "PkgSizeStats packageName = " + packageName
                + " cacheSize = " + cacheSize
                + " codeSize = " + codeSize
                + " dataSize = " + dataSize
                + " externalCodeSize = " + externalCodeSize
                + " externalDataSize = " + externalDataSize
                + " externalCacheSize = " + externalCacheSize
                + " internalSize = " + internalSize
                + " externalSize = " + externalSize;
    }
}
