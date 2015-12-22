/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/heyongjun/miui_workspace/Mi3_W_V6/packages/apps/SecurityCenter/src/com/cleanmaster/sdk/ICacheCallback.aidl
 */
package com.cleanmaster.sdk;
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
// 找到一条Cache信息

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
// 扫描缓存结束

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
// 开始扫描时的回调, nTotalScanItem代表总体的进度

public void onStartScan(int nTotalScanItem) throws android.os.RemoteException;
// 扫描的进度
// 返回true停止扫描, false不做处理

public boolean onScanItem(java.lang.String desc, int nProgressIndex) throws android.os.RemoteException;
// 找到一条Cache信息

public void onFindCacheItem(java.lang.String cacheType, java.lang.String dirPath, java.lang.String pkgName, boolean bAdviseDel, java.lang.String alertInfo, java.lang.String descx) throws android.os.RemoteException;
// 扫描缓存结束

public void onCacheScanFinish() throws android.os.RemoteException;
}
