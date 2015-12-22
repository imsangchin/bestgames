/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter/app/src/main/aidl/com/miui/securitycenter/memory/IMemoryScanCallback.aidl
 */
package com.miui.securitycenter.memory;
public interface IMemoryScanCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.miui.securitycenter.memory.IMemoryScanCallback
{
private static final java.lang.String DESCRIPTOR = "com.miui.securitycenter.memory.IMemoryScanCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.miui.securitycenter.memory.IMemoryScanCallback interface,
 * generating a proxy if needed.
 */
public static com.miui.securitycenter.memory.IMemoryScanCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.miui.securitycenter.memory.IMemoryScanCallback))) {
return ((com.miui.securitycenter.memory.IMemoryScanCallback)iin);
}
return new com.miui.securitycenter.memory.IMemoryScanCallback.Stub.Proxy(obj);
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
boolean _result = this.onScanItem(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_onFinishScan:
{
data.enforceInterface(DESCRIPTOR);
this.onFinishScan();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.miui.securitycenter.memory.IMemoryScanCallback
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
@Override public void onStartScan(int totalCount) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(totalCount);
mRemote.transact(Stub.TRANSACTION_onStartScan, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
/**
    * @param descx
    * @return true : force stop
    */
@Override public boolean onScanItem(java.lang.String descx) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(descx);
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
@Override public void onFinishScan() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onFinishScan, _data, _reply, 0);
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
static final int TRANSACTION_onFinishScan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void onStartScan(int totalCount) throws android.os.RemoteException;
/**
    * @param descx
    * @return true : force stop
    */
public boolean onScanItem(java.lang.String descx) throws android.os.RemoteException;
public void onFinishScan() throws android.os.RemoteException;
}
