/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter1/app/src/main/aidl/com/cleanmaster/sdk/IKSCleaner.aidl
 */
package com.cleanmaster.sdk;
/**
 * SDK main Interface, this interface contains all functions of CM SDK
 * -# @ref scanCache method Scan SD cache of an application.
 * -# @ref scanSystemCache method Scan system cache, system cache is under /data/data/package_name/cache.
 * -# @ref scanResidual method Scan application's residual files after uninstall.
 * -# @ref getCanDeleteResidualFilePaths method Get useless residual file lists of an application, these files can be deleted safely.
 * -# @ref scanAdDir method Scan Ad files. These files are just for display Ad in application, can be deleted safely.
 * -# @ref StartUpdateCheck method Check if the database need to update
 * -# @ref StartUpdateData method Start to update database. Please call StartUpdateCheck first.
 * 
 * Before using these functions, please get @ref IKSCleaner instance,
 * For samples of obtaining IKSCleaner instance , see the [Get the instance of IKSCleaner interface](@ref sec31) section.
 * 
 * Note that, because of IPC calling, these methods may throw<a class="el" href="http://developer.android.com/reference/android/os/RemoteException.html">android.os.RemoteException</a>.
 */
public interface IKSCleaner extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cleanmaster.sdk.IKSCleaner
{
private static final java.lang.String DESCRIPTOR = "com.cleanmaster.sdk.IKSCleaner";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cleanmaster.sdk.IKSCleaner interface,
 * generating a proxy if needed.
 */
public static com.cleanmaster.sdk.IKSCleaner asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cleanmaster.sdk.IKSCleaner))) {
return ((com.cleanmaster.sdk.IKSCleaner)iin);
}
return new com.cleanmaster.sdk.IKSCleaner.Stub.Proxy(obj);
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
case TRANSACTION_getVersion:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getVersion();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_init:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.init(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_scanCache:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
com.cleanmaster.sdk.ICacheCallback _arg1;
_arg1 = com.cleanmaster.sdk.ICacheCallback.Stub.asInterface(data.readStrongBinder());
this.scanCache(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_scanSystemCache:
{
data.enforceInterface(DESCRIPTOR);
com.cleanmaster.sdk.ISystemCacheCallback _arg0;
_arg0 = com.cleanmaster.sdk.ISystemCacheCallback.Stub.asInterface(data.readStrongBinder());
this.scanSystemCache(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_scanResidual:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
com.cleanmaster.sdk.IResidualCallback _arg1;
_arg1 = com.cleanmaster.sdk.IResidualCallback.Stub.asInterface(data.readStrongBinder());
this.scanResidual(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_scanAdDir:
{
data.enforceInterface(DESCRIPTOR);
com.cleanmaster.sdk.IAdDirCallback _arg0;
_arg0 = com.cleanmaster.sdk.IAdDirCallback.Stub.asInterface(data.readStrongBinder());
this.scanAdDir(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_pathCalcSize:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _result = this.pathCalcSize(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getResidualFilePaths:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String[] _result = this.getResidualFilePaths(_arg0);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_getCanDeleteResidualFilePaths:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String[] _result = this.getCanDeleteResidualFilePaths(_arg0);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_IsEnableAutoUpdate:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.IsEnableAutoUpdate();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_SetEnableAutoUpdate:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.SetEnableAutoUpdate(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_GetDataVersion:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.GetDataVersion();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_StartUpdateCheck:
{
data.enforceInterface(DESCRIPTOR);
com.cleanmaster.sdk.ICmSdkUpdateCallback _arg0;
_arg0 = com.cleanmaster.sdk.ICmSdkUpdateCallback.Stub.asInterface(data.readStrongBinder());
this.StartUpdateCheck(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_StartUpdateData:
{
data.enforceInterface(DESCRIPTOR);
this.StartUpdateData();
reply.writeNoException();
return true;
}
case TRANSACTION_SetEnableNetworkAccess:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.SetEnableNetworkAccess(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cleanmaster.sdk.IKSCleaner
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
	 * Get the version of interface, the current SDK version is 2. The version number changed means the interface changed
	 * 
	 * @return current SDK version. 
	 */
@Override public int getVersion() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getVersion, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Initialize language localization. Call this function before calling scan function.The language and country is used as the language of description for the junk files. Now we support:
	 * 
	 * |language|country|
	 * |:------:|:-----:| 
	 * |de| |
	 * |en      |       |
	 * |es      |       |
	 * |es      |us     | 
	 * |fr      |       |  
	 * |in      |       |
	 * |it      |       |
	 * |fr      |       |
	 * |pt      |       |
	 * |pt      |br     |
	 * |ro      |       |
	 * |sk      |       |
	 * |th      |       |
	 * |vi      |       |
	 * |tr      |       |
	 * |el      |       |
	 * |ru      |       |
	 * |uk      |       |
	 * |ko      |       |
	 * |ms      |       |
	 * |zh      |cn     |
	 * |zh      |tw     |
	 * |ja      |       |
	 * |ar      |       |
	 * |nl      |       |
	 * |nb      |       |
	 * |pl      |       |
	 * |hr      |       |
	 * |cs      |       |
	 * |hi      |       |
	 * |sr      |       |
	 * |bg      |       |
	 * |da      |       |
	 * 
	 * Note that, if the language and country you passed is not supported, SDK will choose English as the language of description.
	 * @param language
	 *            language,Ex."en"
	 * @param country
	 *            country,Ex."US", can be null.
	 */
@Override public void init(java.lang.String language, java.lang.String country) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(language);
_data.writeString(country);
mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Scan SD cache of an application
	 * 
	 * @param nScanMask
	 *            Flag to indicate the scan type.
	 * - CMCleanConst.MASK_SCAN_COMMON 
	 *						Standard scan, recommend user to delete the scan result and will not have side-effect
	 * @param observer
	 *						Callback function interface for SD cache scan, SDK will invoke the callback functions to return the scan progress and result.
	 * @see ICacheCallback
	 */
@Override public void scanCache(int nScanMask, com.cleanmaster.sdk.ICacheCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nScanMask);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanCache, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
     * Scan system cache, system cache is under /data/data/package_name/cache. 
     * Application can get this path by android.content.Context.getCacheDir() and write cache file under this path.
     * 
     * @param observer
     *						Callback function interface for system cache scan. 
     *						SDK will invoke the callback functions to return the scan progress and result.
     * @see ISystemCacheCallback
     */
@Override public void scanSystemCache(com.cleanmaster.sdk.ISystemCacheCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanSystemCache, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Scan application's residual files after uninstall. The file is under SD Card and won't be deleted after uninstall automatically.
	 * 
	 * @param nMask
	 *						Flag to indicate the scan type.
	 * - CMCleanConst.MASK_SCAN_COMMON
	 *						Standard scan, recommend user to delete the scan result and will not have side-effect
	 * @param observer
	 *						Callback function interface for residual scan.
	 *						SDK will invoke the callback functions to return the scan progress and result.
	 * 
	 * @see IResidualCallback
	 */
@Override public void scanResidual(int nMask, com.cleanmaster.sdk.IResidualCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nMask);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanResidual, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Scan Ad files. These files are just for display Ad in application, can be deleted safely.
	 * 
	 * @param observer
	 *            Callback function interface for advertisement junks.
	 *						SDK will invoke the callback functions to return the scan progress and result.
	 * @see IAdDirCallback
	 */
@Override public void scanAdDir(com.cleanmaster.sdk.IAdDirCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanAdDir, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Calculate the size of a file or a folder in bytes.
	 * 
	 * @param dirPath
	 *            a file or a folder's absolute path.
	 * @return the file or folder's size
	 */
@Override public long pathCalcSize(java.lang.String dirPath) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dirPath);
mRemote.transact(Stub.TRANSACTION_pathCalcSize, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Get all residual file lists of an application, these files may contain user data.
	 * For example, we can delete these useless residual files immediately after uninstalling an application.
	 * 
	 * @param pkgName
	 * 						the package name of a application
	 * @return all residual file lists, the absolute path. 
	 */
@Override public java.lang.String[] getResidualFilePaths(java.lang.String pkgName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
mRemote.transact(Stub.TRANSACTION_getResidualFilePaths, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Get useless residual file lists of an application, these files can be deleted safely.
	 * For example, we can delete these useless residual files immediately after uninstalling an application.
	 * 
	 * @param pkgName
	 * 						the package name of a application
	 * @return the useless residual file list. 
	 */
@Override public java.lang.String[] getCanDeleteResidualFilePaths(java.lang.String pkgName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
mRemote.transact(Stub.TRANSACTION_getCanDeleteResidualFilePaths, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Check if auto update enabled
	 * 
	 * @deprecated
	 * @return the auto update setting. 
	 * @see IKSCleaner.SetEnableAutoUpdate
	 */
@Override public boolean IsEnableAutoUpdate() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_IsEnableAutoUpdate, _data, _reply, 0);
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
	 * Auto update setting.
	 * 
	 * @deprecated
	 * @param bEnable
	 *            ture will enable auto update, and false will disable auto udate.
	 * @see IKSCleaner.IsEnableAutoUpdate
	 */
@Override public void SetEnableAutoUpdate(boolean bEnable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bEnable)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_SetEnableAutoUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Get database verison, for example "2014.5.22.1757".
	 * 
	 * @return the database version. 
	 */
@Override public java.lang.String GetDataVersion() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_GetDataVersion, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
/**
	 * Check if the database need to be updated
	 * 
	 * @param callBack
	 *						Interface definition for a callback when update checking finished.
	 * @see ICmSdkUpdateCallback
	 */
@Override public void StartUpdateCheck(com.cleanmaster.sdk.ICmSdkUpdateCallback callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_StartUpdateCheck, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Start to update database. Please call StartUpdateCheck first and check the nErrorCode is
	 * @ref CMCleanConst.UPDATE_ERROR_CODE_SUCCESS in @ref ICmSdkUpdateCallback.FinishUpdateCheck before using this function.
	 */
@Override public void StartUpdateData() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_StartUpdateData, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
	 * Network Access setting.
	 * 
	 * @param bEnable
	 *            true will enable network access, and false will disable network access.
	 */
@Override public void SetEnableNetworkAccess(boolean bEnable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bEnable)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_SetEnableNetworkAccess, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_init = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_scanCache = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_scanSystemCache = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_scanResidual = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_scanAdDir = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_pathCalcSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getResidualFilePaths = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getCanDeleteResidualFilePaths = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_IsEnableAutoUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_SetEnableAutoUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_GetDataVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_StartUpdateCheck = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_StartUpdateData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_SetEnableNetworkAccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
}
/**
	 * Get the version of interface, the current SDK version is 2. The version number changed means the interface changed
	 * 
	 * @return current SDK version. 
	 */
public int getVersion() throws android.os.RemoteException;
/**
	 * Initialize language localization. Call this function before calling scan function.The language and country is used as the language of description for the junk files. Now we support:
	 * 
	 * |language|country|
	 * |:------:|:-----:| 
	 * |de| |
	 * |en      |       |
	 * |es      |       |
	 * |es      |us     | 
	 * |fr      |       |  
	 * |in      |       |
	 * |it      |       |
	 * |fr      |       |
	 * |pt      |       |
	 * |pt      |br     |
	 * |ro      |       |
	 * |sk      |       |
	 * |th      |       |
	 * |vi      |       |
	 * |tr      |       |
	 * |el      |       |
	 * |ru      |       |
	 * |uk      |       |
	 * |ko      |       |
	 * |ms      |       |
	 * |zh      |cn     |
	 * |zh      |tw     |
	 * |ja      |       |
	 * |ar      |       |
	 * |nl      |       |
	 * |nb      |       |
	 * |pl      |       |
	 * |hr      |       |
	 * |cs      |       |
	 * |hi      |       |
	 * |sr      |       |
	 * |bg      |       |
	 * |da      |       |
	 * 
	 * Note that, if the language and country you passed is not supported, SDK will choose English as the language of description.
	 * @param language
	 *            language,Ex."en"
	 * @param country
	 *            country,Ex."US", can be null.
	 */
public void init(java.lang.String language, java.lang.String country) throws android.os.RemoteException;
/**
	 * Scan SD cache of an application
	 * 
	 * @param nScanMask
	 *            Flag to indicate the scan type.
	 * - CMCleanConst.MASK_SCAN_COMMON 
	 *						Standard scan, recommend user to delete the scan result and will not have side-effect
	 * @param observer
	 *						Callback function interface for SD cache scan, SDK will invoke the callback functions to return the scan progress and result.
	 * @see ICacheCallback
	 */
public void scanCache(int nScanMask, com.cleanmaster.sdk.ICacheCallback observer) throws android.os.RemoteException;
/**
     * Scan system cache, system cache is under /data/data/package_name/cache. 
     * Application can get this path by android.content.Context.getCacheDir() and write cache file under this path.
     * 
     * @param observer
     *						Callback function interface for system cache scan. 
     *						SDK will invoke the callback functions to return the scan progress and result.
     * @see ISystemCacheCallback
     */
public void scanSystemCache(com.cleanmaster.sdk.ISystemCacheCallback observer) throws android.os.RemoteException;
/**
	 * Scan application's residual files after uninstall. The file is under SD Card and won't be deleted after uninstall automatically.
	 * 
	 * @param nMask
	 *						Flag to indicate the scan type.
	 * - CMCleanConst.MASK_SCAN_COMMON
	 *						Standard scan, recommend user to delete the scan result and will not have side-effect
	 * @param observer
	 *						Callback function interface for residual scan.
	 *						SDK will invoke the callback functions to return the scan progress and result.
	 * 
	 * @see IResidualCallback
	 */
public void scanResidual(int nMask, com.cleanmaster.sdk.IResidualCallback observer) throws android.os.RemoteException;
/**
	 * Scan Ad files. These files are just for display Ad in application, can be deleted safely.
	 * 
	 * @param observer
	 *            Callback function interface for advertisement junks.
	 *						SDK will invoke the callback functions to return the scan progress and result.
	 * @see IAdDirCallback
	 */
public void scanAdDir(com.cleanmaster.sdk.IAdDirCallback observer) throws android.os.RemoteException;
/**
	 * Calculate the size of a file or a folder in bytes.
	 * 
	 * @param dirPath
	 *            a file or a folder's absolute path.
	 * @return the file or folder's size
	 */
public long pathCalcSize(java.lang.String dirPath) throws android.os.RemoteException;
/**
	 * Get all residual file lists of an application, these files may contain user data.
	 * For example, we can delete these useless residual files immediately after uninstalling an application.
	 * 
	 * @param pkgName
	 * 						the package name of a application
	 * @return all residual file lists, the absolute path. 
	 */
public java.lang.String[] getResidualFilePaths(java.lang.String pkgName) throws android.os.RemoteException;
/**
	 * Get useless residual file lists of an application, these files can be deleted safely.
	 * For example, we can delete these useless residual files immediately after uninstalling an application.
	 * 
	 * @param pkgName
	 * 						the package name of a application
	 * @return the useless residual file list. 
	 */
public java.lang.String[] getCanDeleteResidualFilePaths(java.lang.String pkgName) throws android.os.RemoteException;
/**
	 * Check if auto update enabled
	 * 
	 * @deprecated
	 * @return the auto update setting. 
	 * @see IKSCleaner.SetEnableAutoUpdate
	 */
public boolean IsEnableAutoUpdate() throws android.os.RemoteException;
/**
	 * Auto update setting.
	 * 
	 * @deprecated
	 * @param bEnable
	 *            ture will enable auto update, and false will disable auto udate.
	 * @see IKSCleaner.IsEnableAutoUpdate
	 */
public void SetEnableAutoUpdate(boolean bEnable) throws android.os.RemoteException;
/**
	 * Get database verison, for example "2014.5.22.1757".
	 * 
	 * @return the database version. 
	 */
public java.lang.String GetDataVersion() throws android.os.RemoteException;
/**
	 * Check if the database need to be updated
	 * 
	 * @param callBack
	 *						Interface definition for a callback when update checking finished.
	 * @see ICmSdkUpdateCallback
	 */
public void StartUpdateCheck(com.cleanmaster.sdk.ICmSdkUpdateCallback callBack) throws android.os.RemoteException;
/**
	 * Start to update database. Please call StartUpdateCheck first and check the nErrorCode is
	 * @ref CMCleanConst.UPDATE_ERROR_CODE_SUCCESS in @ref ICmSdkUpdateCallback.FinishUpdateCheck before using this function.
	 */
public void StartUpdateData() throws android.os.RemoteException;
/**
	 * Network Access setting.
	 * 
	 * @param bEnable
	 *            true will enable network access, and false will disable network access.
	 */
public void SetEnableNetworkAccess(boolean bEnable) throws android.os.RemoteException;
}
