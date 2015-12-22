/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter/app/src/main/aidl/com/miui/securitycenter/memory/IMemoryCleanupCallback.aidl
 */
package com.miui.securitycenter.memory;
public interface IMemoryCleanupCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.miui.securitycenter.memory.IMemoryCleanupCallback
{
private static final java.lang.String DESCRIPTOR = "com.miui.securitycenter.memory.IMemoryCleanupCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.miui.securitycenter.memory.IMemoryCleanupCallback interface,
 * generating a proxy if needed.
 */
public static com.miui.securitycenter.memory.IMemoryCleanupCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.miui.securitycenter.memory.IMemoryCleanupCallback))) {
return ((com.miui.securitycenter.memory.IMemoryCleanupCallback)iin);
}
return new com.miui.securitycenter.memory.IMemoryCleanupCallback.Stub.Proxy(obj);
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
case TRANSACTION_onStartCleanup:
{
data.enforceInterface(DESCRIPTOR);
this.onStartCleanup();
reply.writeNoException();
return true;
}
case TRANSACTION_onCleanupItem:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.onCleanupItem(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_onFinishCleanup:
{
data.enforceInterface(DESCRIPTOR);
this.onFinishCleanup();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.miui.securitycenter.memory.IMemoryCleanupCallback
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
@Override public void onStartCleanup() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onStartCleanup, _data, _reply, 0);
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
@Override public boolean onCleanupItem(java.lang.String descx) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(descx);
mRemote.transact(Stub.TRANSACTION_onCleanupItem, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void onFinishCleanup() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_onFinishCleanup, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onStartCleanup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onCleanupItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onFinishCleanup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
}
public void onStartCleanup() throws android.os.RemoteException;
/**
    * @param descx
    * @return true : force stop
    */
public boolean onCleanupItem(java.lang.String descx) throws android.os.RemoteException;
public void onFinishCleanup() throws android.os.RemoteException;
}
