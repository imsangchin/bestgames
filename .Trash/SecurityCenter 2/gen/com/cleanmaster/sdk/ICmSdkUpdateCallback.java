/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/heyongjun/miui_workspace/Mi3_W_V6/packages/apps/SecurityCenter/src/com/cleanmaster/sdk/ICmSdkUpdateCallback.aidl
 */
package com.cleanmaster.sdk;
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
case TRANSACTION_FinishaUpdateCheck:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
long _arg1;
_arg1 = data.readLong();
java.lang.String _arg2;
_arg2 = data.readString();
this.FinishaUpdateCheck(_arg0, _arg1, _arg2);
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
// 升级检测完成

@Override public void FinishaUpdateCheck(int nErrorCode, long size, java.lang.String strNewVersion) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nErrorCode);
_data.writeLong(size);
_data.writeString(strNewVersion);
mRemote.transact(Stub.TRANSACTION_FinishaUpdateCheck, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 数据升级完成

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
static final int TRANSACTION_FinishaUpdateCheck = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_FinishUpdateData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
// 升级检测完成

public void FinishaUpdateCheck(int nErrorCode, long size, java.lang.String strNewVersion) throws android.os.RemoteException;
// 数据升级完成

public void FinishUpdateData(int nErrorCode) throws android.os.RemoteException;
}
