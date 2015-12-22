/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/heyongjun/miui_workspace/Mi3_W_V6/packages/apps/SecurityCenter/src/com/cleanmaster/sdk/IResidualCallback.aidl
 */
package com.cleanmaster.sdk;
public interface IResidualCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cleanmaster.sdk.IResidualCallback
{
private static final java.lang.String DESCRIPTOR = "com.cleanmaster.sdk.IResidualCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cleanmaster.sdk.IResidualCallback interface,
 * generating a proxy if needed.
 */
public static com.cleanmaster.sdk.IResidualCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cleanmaster.sdk.IResidualCallback))) {
return ((com.cleanmaster.sdk.IResidualCallback)iin);
}
return new com.cleanmaster.sdk.IResidualCallback.Stub.Proxy(obj);
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
case TRANSACTION_onFindResidualItem:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
boolean _arg2;
_arg2 = (0!=data.readInt());
java.lang.String _arg3;
_arg3 = data.readString();
this.onFindResidualItem(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_onResidualScanFinish:
{
data.enforceInterface(DESCRIPTOR);
this.onResidualScanFinish();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cleanmaster.sdk.IResidualCallback
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
// 开始扫描时的回调, nTotalScanItem代表总体的进度

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
// 扫描的进度
// 返回true停止扫描, false不做处理

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
// 找到一条缓存信息

@Override public void onFindResidualItem(java.lang.String dirPath, java.lang.String descName, boolean bAdviseDel, java.lang.String alertInfo) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dirPath);
_data.writeString(descName);
_data.writeInt(((bAdviseDel)?(1):(0)));
_data.writeString(alertInfo);
mRemote.transact(Stub.TRANSACTION_onFindResidualItem, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 扫描缓存结束

@Override public void onResidualScanFinish() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onResidualScanFinish, _data, _reply, 0);
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
static final int TRANSACTION_onFindResidualItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onResidualScanFinish = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
// 开始扫描时的回调, nTotalScanItem代表总体的进度

public void onStartScan(int nTotalScanItem) throws android.os.RemoteException;
// 扫描的进度
// 返回true停止扫描, false不做处理

public boolean onScanItem(java.lang.String desc, int nProgressIndex) throws android.os.RemoteException;
// 找到一条缓存信息

public void onFindResidualItem(java.lang.String dirPath, java.lang.String descName, boolean bAdviseDel, java.lang.String alertInfo) throws android.os.RemoteException;
// 扫描缓存结束

public void onResidualScanFinish() throws android.os.RemoteException;
}
