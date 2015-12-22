/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter/app/src/main/aidl/com/android/fileexplorer/controller/ITrashCleanupService.aidl
 */
package com.android.fileexplorer.controller;
public interface ITrashCleanupService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.fileexplorer.controller.ITrashCleanupService
{
private static final java.lang.String DESCRIPTOR = "com.android.fileexplorer.controller.ITrashCleanupService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.android.fileexplorer.controller.ITrashCleanupService interface,
 * generating a proxy if needed.
 */
public static com.android.fileexplorer.controller.ITrashCleanupService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.fileexplorer.controller.ITrashCleanupService))) {
return ((com.android.fileexplorer.controller.ITrashCleanupService)iin);
}
return new com.android.fileexplorer.controller.ITrashCleanupService.Stub.Proxy(obj);
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
case TRANSACTION_registerCleanupCallbacks:
{
data.enforceInterface(DESCRIPTOR);
com.android.fileexplorer.controller.ITrashCleanupCallbacks _arg0;
_arg0 = com.android.fileexplorer.controller.ITrashCleanupCallbacks.Stub.asInterface(data.readStrongBinder());
this.registerCleanupCallbacks(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unRegisterCleanupCallbacks:
{
data.enforceInterface(DESCRIPTOR);
com.android.fileexplorer.controller.ITrashCleanupCallbacks _arg0;
_arg0 = com.android.fileexplorer.controller.ITrashCleanupCallbacks.Stub.asInterface(data.readStrongBinder());
this.unRegisterCleanupCallbacks(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_startScan:
{
data.enforceInterface(DESCRIPTOR);
this.startScan();
reply.writeNoException();
return true;
}
case TRANSACTION_startCleanup:
{
data.enforceInterface(DESCRIPTOR);
this.startCleanup();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.fileexplorer.controller.ITrashCleanupService
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
@Override public void registerCleanupCallbacks(com.android.fileexplorer.controller.ITrashCleanupCallbacks callbacks) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callbacks!=null))?(callbacks.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCleanupCallbacks, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unRegisterCleanupCallbacks(com.android.fileexplorer.controller.ITrashCleanupCallbacks callbacks) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callbacks!=null))?(callbacks.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unRegisterCleanupCallbacks, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startScan() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startScan, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startCleanup() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_startCleanup, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_registerCleanupCallbacks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_unRegisterCleanupCallbacks = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_startScan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_startCleanup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void registerCleanupCallbacks(com.android.fileexplorer.controller.ITrashCleanupCallbacks callbacks) throws android.os.RemoteException;
public void unRegisterCleanupCallbacks(com.android.fileexplorer.controller.ITrashCleanupCallbacks callbacks) throws android.os.RemoteException;
public void startScan() throws android.os.RemoteException;
public void startCleanup() throws android.os.RemoteException;
}
