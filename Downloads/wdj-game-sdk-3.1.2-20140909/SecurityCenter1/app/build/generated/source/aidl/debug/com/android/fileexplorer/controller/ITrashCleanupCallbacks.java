/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter1/app/src/main/aidl/com/android/fileexplorer/controller/ITrashCleanupCallbacks.aidl
 */
package com.android.fileexplorer.controller;
public interface ITrashCleanupCallbacks extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.fileexplorer.controller.ITrashCleanupCallbacks
{
private static final java.lang.String DESCRIPTOR = "com.android.fileexplorer.controller.ITrashCleanupCallbacks";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.android.fileexplorer.controller.ITrashCleanupCallbacks interface,
 * generating a proxy if needed.
 */
public static com.android.fileexplorer.controller.ITrashCleanupCallbacks asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.fileexplorer.controller.ITrashCleanupCallbacks))) {
return ((com.android.fileexplorer.controller.ITrashCleanupCallbacks)iin);
}
return new com.android.fileexplorer.controller.ITrashCleanupCallbacks.Stub.Proxy(obj);
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
case TRANSACTION_onStart:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onStart(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_onItemStart:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.onItemStart(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onItemProgress:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
long _arg2;
_arg2 = data.readLong();
java.lang.String _arg3;
_arg3 = data.readString();
this.onItemProgress(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_onItemStop:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
int _arg1;
_arg1 = data.readInt();
this.onItemStop(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_onStop:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.onStop(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.fileexplorer.controller.ITrashCleanupCallbacks
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
@Override public void onStart(int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_onStart, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onItemStart(int state, int type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
_data.writeInt(type);
mRemote.transact(Stub.TRANSACTION_onItemStart, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onItemProgress(int state, int type, long size, java.lang.String fileName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
_data.writeInt(type);
_data.writeLong(size);
_data.writeString(fileName);
mRemote.transact(Stub.TRANSACTION_onItemProgress, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onItemStop(int state, int type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
_data.writeInt(type);
mRemote.transact(Stub.TRANSACTION_onItemStop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void onStop(int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_onStop, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_onStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_onItemStart = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_onItemProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_onItemStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_onStop = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public void onStart(int state) throws android.os.RemoteException;
public void onItemStart(int state, int type) throws android.os.RemoteException;
public void onItemProgress(int state, int type, long size, java.lang.String fileName) throws android.os.RemoteException;
public void onItemStop(int state, int type) throws android.os.RemoteException;
public void onStop(int state) throws android.os.RemoteException;
}
