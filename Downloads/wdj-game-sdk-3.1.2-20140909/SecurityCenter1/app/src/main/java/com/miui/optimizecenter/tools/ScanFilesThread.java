
package com.miui.optimizecenter.tools;

import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;

import com.miui.guardprovider.service.IFileProxy;
import com.miui.guardprovider.service.IFileScanCallback;
import com.miui.optimizecenter.deepclean.LargeFileModel;

import android.os.RemoteException;
import android.os.storage.StorageVolume;

public class ScanFilesThread extends Thread {

    public enum LargeFile {
        All, External, Sdcard
    }

    private IFileProxy mIFileProxy;
    private IFileScanCallback mCallback;
    private LargeFile mLargeFile;

    public ScanFilesThread(IFileProxy proxy, IFileScanCallback callback, LargeFile largeFile) {
        mIFileProxy = proxy;
        mCallback = callback;
        mLargeFile = largeFile;
    }

    @Override
    public void run() {
        switch (mLargeFile) {
            case External:
                scanExternalStorage();
                break;
            case Sdcard:
                scanSdcardStorage();
                break;
            default:
                scanAllStorage();
                break;
        }
    }

    private void scanAllStorage() {
        try {
            if (mIFileProxy != null) {
                String volumeName = "external";
                String[] columns = new String[] {
                        FileColumns.DATA
                };
                String selection = FileColumns.SIZE + " >= " + LargeFileModel.MIN_SIZE;

                mIFileProxy.scanFilesByUri(mCallback, Files.getContentUri(volumeName), columns,
                        selection, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void scanExternalStorage() {
        try {
            if (mIFileProxy != null) {
                String volumeName = "external";
                String[] columns = new String[] {
                        FileColumns.DATA
                };
                String selection = FileColumns.SIZE + " >= " + LargeFileModel.MIN_SIZE;
                StorageVolume volume = mIFileProxy.getExternalStorageVolume();
                if (volume != null) {
                    selection = FileColumns.SIZE + " >= " + LargeFileModel.MIN_SIZE + " and "
                            + FileColumns.DATA + " LIKE '" + volume.getPath() + "%' ";
                }

                mIFileProxy.scanFilesByUri(mCallback, Files.getContentUri(volumeName), columns,
                        selection, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void scanSdcardStorage() {
        try {
            if (mIFileProxy != null) {
                String volumeName = "external";
                String[] columns = new String[] {
                        FileColumns.DATA
                };
                String selection = FileColumns.SIZE + " >= " + LargeFileModel.MIN_SIZE;
                StorageVolume volume = mIFileProxy.getSdcardStorageVolume();
                if (volume != null) {
                    selection = FileColumns.SIZE + " >= " + LargeFileModel.MIN_SIZE + " and "
                            + FileColumns.DATA + " LIKE '" + volume.getPath() + "%' ";
                }

                mIFileProxy.scanFilesByUri(mCallback, Files.getContentUri(volumeName), columns,
                        selection, null, null);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
