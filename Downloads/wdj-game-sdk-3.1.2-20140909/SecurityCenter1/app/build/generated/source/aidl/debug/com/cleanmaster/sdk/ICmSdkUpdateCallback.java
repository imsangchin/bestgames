/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter1/app/src/main/aidl/com/cleanmaster/sdk/ICmSdkUpdateCallback.aidl
 */
package com.cleanmaster.sdk;
/**
 * 
 * SDK update callback interface. Interface definition for a callback when SDK update finished.
 * For samples, see the [Update CleanMaster SDK data](@ref sec33) section.
 */
public interface ICmSdkUpdateCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cleanmaster.sdk.ICmSdkUpdateCallback
{
private static final java.lang.String DESCRIPTOR = "com.cleanmaster.sdk.ICmSdkUpdateCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cleanmaster.sdk.ICmSdkUpdateCallback interface,
 * generating a proxy if needed.
 */
public static com.cleanmaster.sdk.ICmSdkUpdateCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cleanmaster.sdk.ICmSdkUpdateCallback))) {
return ((com.cleanmaster.sdk.ICmSdkUpdateCallback)iin);
}
return new com.cleanmaster.sdk.ICmSdkUpdateCallback.Stub.Proxy(obj);
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
case TRANSACTION_FinishUpdateCheck:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
long _arg1;
_arg1 = data.readLong();
java.lang.String _arg2;
_arg2 = data.readString();
this.FinishUpdateCheck(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_FinishUpdateData:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.FinishUpdateData(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cleanmaster.sdk.ICmSdkUpdateCallback
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
    * A callback when update checking finished
    * @param nErrorCode The error codes define by [CMCleanConst.UPDATE_ERROR_CODE_XXXXXX](@ref CMCleanConst) .
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_SUCCESS , There is a new version. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_ALREADY_RUNNING , Update is running. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_NO_NEWER_DB , Already update to the latest database. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_NETWORK_ERROR , Network error during update. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_UNKNOWN_ERROR , Other error during update. 
    * @param size means the size of update package when there is a new version. 
    * @param strNewVersion the new version number, for example "2014.5.22.1757". 
    * @see  CMCleanConst
    */
@Override public void FinishUpdateCheck(int nErrorCode, long size, java.lang.String strNewVersion) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nErrorCode);
_data.writeLong(size);
_data.writeString(strNewVersion);
mRemote.transact(Stub.TRANSACTION_FinishUpdateCheck, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
    * A callback when database update finished
    * @param nErrorCode The error codes define by [CMCleanConst.UPDATE_ERROR_CODE_XXXXXX](@ref CMCleanConst) . 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_SUCCESS , Update success. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_ALREADY_RUNNING , Update is running. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_NETWORK_ERROR , Network error during update. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_UNKNOWN_ERROR , Other error during update. 
    * @see  CMCleanConst 
    */
@Override public void FinishUpdateData(int nErrorCode) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nErrorCode);
mRemote.transact(Stub.TRANSACTION_FinishUpdateData, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_FinishUpdateCheck = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_FinishUpdateData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
    * A callback when update checking finished
    * @param nErrorCode The error codes define by [CMCleanConst.UPDATE_ERROR_CODE_XXXXXX](@ref CMCleanConst) .
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_SUCCESS , There is a new version. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_ALREADY_RUNNING , Update is running. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_NO_NEWER_DB , Already update to the latest database. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_NETWORK_ERROR , Network error during update. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_UNKNOWN_ERROR , Other error during update. 
    * @param size means the size of update package when there is a new version. 
    * @param strNewVersion the new version number, for example "2014.5.22.1757". 
    * @see  CMCleanConst
    */
public void FinishUpdateCheck(int nErrorCode, long size, java.lang.String strNewVersion) throws android.os.RemoteException;
/**
    * A callback when database update finished
    * @param nErrorCode The error codes define by [CMCleanConst.UPDATE_ERROR_CODE_XXXXXX](@ref CMCleanConst) . 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_SUCCESS , Update success. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_ALREADY_RUNNING , Update is running. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_NETWORK_ERROR , Network error during update. 
    * - @ref CMCleanConst.UPDATE_ERROR_CODE_UNKNOWN_ERROR , Other error during update. 
    * @see  CMCleanConst 
    */
public void FinishUpdateData(int nErrorCode) throws android.os.RemoteException;
}
