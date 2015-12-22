/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter1/app/src/main/aidl/com/cleanmaster/sdk/IAdDirCallback.aidl
 */
package com.cleanmaster.sdk;
/**
 * 
 * Callback function interface for advertisement junks.
 * Caller should implement the interface. SDK will invoke the callback functions to return the scan progress and result.
 * It's much like [ICacheCallback](@ref ICacheCallback)  interface, For samples, see the [Scan junk files](@ref sec32) section.
 */
public interface IAdDirCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cleanmaster.sdk.IAdDirCallback
{
private static final java.lang.String DESCRIPTOR = "com.cleanmaster.sdk.IAdDirCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cleanmaster.sdk.IAdDirCallback interface,
 * generating a proxy if needed.
 */
public static com.cleanmaster.sdk.IAdDirCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cleanmaster.sdk.IAdDirCallback))) {
return ((com.cleanmaster.sdk.IAdDirCallback)iin);
}
return new com.cleanmaster.sdk.IAdDirCallback.Stub.Proxy(obj);
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
case TRANSACTION_onFindAdDir:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.onFindAdDir(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onAdDirScanFinish:
{
data.enforceInterface(DESCRIPTOR);
this.onAdDirScanFinish();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cleanmaster.sdk.IAdDirCallback
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
	 *            User-readable string which represents the current scanning app package name.
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
	 * Callback when an advertisement junk is found
	 * 
	 * @param name
	 *            Localized string that contains the advertisement name. The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 * @param dirPath
	 *            Absolute path of the advertisement file.
	 */
@Override public void onFindAdDir(java.lang.String name, java.lang.String dirPath) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(name);
_data.writeString(dirPath);
mRemote.transact(Stub.TRANSACTION_onFindAdDir, _data, _reply, 0);
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
@Override public void onAdDirScanFinish() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onAdDirScanFinish, _data, _reply, 0);
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
static final int TRANSACTION_onFindAdDir = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onAdDirScanFinish = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
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
	 *            User-readable string which represents the current scanning app package name.
	 * @param nProgressIndex
	 *            Current progress. This value can be used to calculate the current progress by nProgressIndex/nTotalScanItem(@ref onStartScan)
     * @return Flag to stop or continue current scan process:
     *      - true Stop scan
     *      - false Continue scan
	 */
public boolean onScanItem(java.lang.String desc, int nProgressIndex) throws android.os.RemoteException;
/**
	 * Callback when an advertisement junk is found
	 * 
	 * @param name
	 *            Localized string that contains the advertisement name. The string will be localized according to the input language and country parameters in @ref IKSCleaner.init
	 * @param dirPath
	 *            Absolute path of the advertisement file.
	 */
public void onFindAdDir(java.lang.String name, java.lang.String dirPath) throws android.os.RemoteException;
/**
	 * Callback when scan is finished
	 */
public void onAdDirScanFinish() throws android.os.RemoteException;
}
