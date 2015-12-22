/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter1/app/src/main/aidl/com/cleanmaster/sdk/ICacheCallback.aidl
 */
package com.cleanmaster.sdk;
/**
 * 
 * Callback function interface for SD cache scan.
 * Caller should implement the interface.  SDK will invoke the callback functions to return the scan progress and result.
 * For samples, see the [Scan junk files](@ref sec32) section.
 */
public interface ICacheCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cleanmaster.sdk.ICacheCallback
{
private static final java.lang.String DESCRIPTOR = "com.cleanmaster.sdk.ICacheCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cleanmaster.sdk.ICacheCallback interface,
 * generating a proxy if needed.
 */
public static com.cleanmaster.sdk.ICacheCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cleanmaster.sdk.ICacheCallback))) {
return ((com.cleanmaster.sdk.ICacheCallback)iin);
}
return new com.cleanmaster.sdk.ICacheCallback.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onStartScan:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onStartScan(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onScanItem:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
boolean _result = this.onScanItem(_arg0, _arg1);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_onFindCacheItem:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
boolean _arg3;
_arg3 = (0!=data.readInt());
java.lang.String _arg4;
_arg4 = data.readString();
java.lang.String _arg5;
_arg5 = data.readString();
this.onFindCacheItem(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
reply.writeNoException();
return true;
}
case TRANSACTION_onCacheScanFinish:
{
data.enforceInterface(DESCRIPTOR);
this.onCacheScanFinish();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cleanmaster.sdk.ICacheCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
	 * Callback when scan is started
	 * 
	 * @param nTotalScanItem
	 *            Total number of scan items in the current scan process which can be used to calculate the progress.
	 */
@Override public void onStartScan(int nTotalScanItem) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nTotalScanItem);
mRemote.transact(Stub.TRANSACTION_onStartScan, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Callback for scan progress
	 * 
	 * @param desc
	 *            User-readable string which represents the current scanning App package name.
	 * @param nProgressIndex
	 *            Current progress. This value can be used to calculate the current progress by nProgressIndex/nTotalScanItem(@ref onStartScan)
     * @return Flag to stop or continue current scan process:
     *      - true Stop scan
     *      - false Continue scan
	 */
@Override public boolean onScanItem(java.lang.String desc, int nProgressIndex) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(desc);
_data.writeInt(nProgressIndex);
mRemote.transact(Stub.TRANSACTION_onScanItem, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Callback when a SD cache item is found
	 * 
	 * @param cacheType
	 *            Localized cache type. Ex. "Image Cache","Local Logs","Icon Cache." The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 * @param dirPath
	 *            Absolute path of the cache file.
	 * @param pkgName
	 *            App package name which generated the cache item.
	 * @param bAdviseDel
	 *            Flag to indicate if we suggest user to delete this item. If CMCleanConst.MASK_SCAN_COMMON is used for scan type, this flag will always be true.
	 *            - true Recommend user to delete the item and will not have side-effect.
	 *            - false Recommend user to keep the item. Folder might contain essential files that could impact user.
	 * @param alertInfo Localized string that contains advice for user. The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 *            - The advice is to help users decide what action they should take.
	 *            - null if bAdviseDel is true.
	 * @param descx
	 *            Localized description of the junk item. Ex. "Game Downloaded cache from WeChat.Cleaning is completely safe."
	 *            The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 */
@Override public void onFindCacheItem(java.lang.String cacheType, java.lang.String dirPath, java.lang.String pkgName, boolean bAdviseDel, java.lang.String alertInfo, java.lang.String descx) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(cacheType);
_data.writeString(dirPath);
_data.writeString(pkgName);
_data.writeInt(((bAdviseDel)?(1):(0)));
_data.writeString(alertInfo);
_data.writeString(descx);
mRemote.transact(Stub.TRANSACTION_onFindCacheItem, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Callback when scan is finished
	 */
@Override public void onCacheScanFinish() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onCacheScanFinish, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onStartScan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onScanItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onFindCacheItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onCacheScanFinish = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
/**
	 * Callback when scan is started
	 * 
	 * @param nTotalScanItem
	 *            Total number of scan items in the current scan process which can be used to calculate the progress.
	 */
public void onStartScan(int nTotalScanItem) throws android.os.RemoteException;
/**
	 * Callback for scan progress
	 * 
	 * @param desc
	 *            User-readable string which represents the current scanning App package name.
	 * @param nProgressIndex
	 *            Current progress. This value can be used to calculate the current progress by nProgressIndex/nTotalScanItem(@ref onStartScan)
     * @return Flag to stop or continue current scan process:
     *      - true Stop scan
     *      - false Continue scan
	 */
public boolean onScanItem(java.lang.String desc, int nProgressIndex) throws android.os.RemoteException;
/**
	 * Callback when a SD cache item is found
	 * 
	 * @param cacheType
	 *            Localized cache type. Ex. "Image Cache","Local Logs","Icon Cache." The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 * @param dirPath
	 *            Absolute path of the cache file.
	 * @param pkgName
	 *            App package name which generated the cache item.
	 * @param bAdviseDel
	 *            Flag to indicate if we suggest user to delete this item. If CMCleanConst.MASK_SCAN_COMMON is used for scan type, this flag will always be true.
	 *            - true Recommend user to delete the item and will not have side-effect.
	 *            - false Recommend user to keep the item. Folder might contain essential files that could impact user.
	 * @param alertInfo Localized string that contains advice for user. The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 *            - The advice is to help users decide what action they should take.
	 *            - null if bAdviseDel is true.
	 * @param descx
	 *            Localized description of the junk item. Ex. "Game Downloaded cache from WeChat.Cleaning is completely safe."
	 *            The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 */
public void onFindCacheItem(java.lang.String cacheType, java.lang.String dirPath, java.lang.String pkgName, boolean bAdviseDel, java.lang.String alertInfo, java.lang.String descx) throws android.os.RemoteException;
/**
	 * Callback when scan is finished
	 */
public void onCacheScanFinish() throws android.os.RemoteException;
}
