/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter1/app/src/main/aidl/com/miui/securitycenter/memory/IMemoryCheck.aidl
 */
package com.miui.securitycenter.memory;
public interface IMemoryCheck extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.miui.securitycenter.memory.IMemoryCheck
{
private static final java.lang.String DESCRIPTOR = "com.miui.securitycenter.memory.IMemoryCheck";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.miui.securitycenter.memory.IMemoryCheck interface,
 * generating a proxy if needed.
 */
public static com.miui.securitycenter.memory.IMemoryCheck asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.miui.securitycenter.memory.IMemoryCheck))) {
return ((com.miui.securitycenter.memory.IMemoryCheck)iin);
}
return new com.miui.securitycenter.memory.IMemoryCheck.Stub.Proxy(obj);
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
case TRANSACTION_getScore:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getScore();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_resetScore:
{
data.enforceInterface(DESCRIPTOR);
this.resetScore();
reply.writeNoException();
return true;
}
case TRANSACTION_getCheckResult:
{
data.enforceInterface(DESCRIPTOR);
java.lang.CharSequence _result = this.getCheckResult();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
android.text.TextUtils.writeToParcel(_result, reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_startScan:
{
data.enforceInterface(DESCRIPTOR);
com.miui.securitycenter.memory.IMemoryScanCallback _arg0;
_arg0 = com.miui.securitycenter.memory.IMemoryScanCallback.Stub.asInterface(data.readStrongBinder());
this.startScan(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_startCleanup:
{
data.enforceInterface(DESCRIPTOR);
com.miui.securitycenter.memory.IMemoryCleanupCallback _arg0;
_arg0 = com.miui.securitycenter.memory.IMemoryCleanupCallback.Stub.asInterface(data.readStrongBinder());
this.startCleanup(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getRunningApps:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<java.lang.String> _result = this.getRunningApps();
reply.writeNoException();
reply.writeStringList(_result);
return true;
}
case TRANSACTION_getMemoryUsed:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _result = this.getMemoryUsed(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getLockedComponents:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<java.lang.String> _result = this.getLockedComponents();
reply.writeNoException();
reply.writeStringList(_result);
return true;
}
case TRANSACTION_getAppLockState:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getAppLockState(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_setAppLockState:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _arg1;
_arg1 = data.readInt();
this.setAppLockState(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getMemoryScanCount:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getMemoryScanCount();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.miui.securitycenter.memory.IMemoryCheck
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
@Override public int getScore() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getScore, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void resetScore() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_resetScore, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.CharSequence getCheckResult() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.CharSequence _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCheckResult, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.text.TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void startScan(com.miui.securitycenter.memory.IMemoryScanCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_startScan, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void startCleanup(com.miui.securitycenter.memory.IMemoryCleanupCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_startCleanup, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.util.List<java.lang.String> getRunningApps() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<java.lang.String> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getRunningApps, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArrayList();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public long getMemoryUsed(java.lang.String pkgName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
mRemote.transact(Stub.TRANSACTION_getMemoryUsed, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.util.List<java.lang.String> getLockedComponents() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<java.lang.String> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getLockedComponents, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArrayList();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getAppLockState(java.lang.String pkgName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
mRemote.transact(Stub.TRANSACTION_getAppLockState, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setAppLockState(java.lang.String pkgName, int state) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
_data.writeInt(state);
mRemote.transact(Stub.TRANSACTION_setAppLockState, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getMemoryScanCount() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getMemoryScanCount, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getScore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_resetScore = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getCheckResult = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_startScan = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_startCleanup = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getRunningApps = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getMemoryUsed = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getLockedComponents = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getAppLockState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_setAppLockState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_getMemoryScanCount = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
}
public int getScore() throws android.os.RemoteException;
public void resetScore() throws android.os.RemoteException;
public java.lang.CharSequence getCheckResult() throws android.os.RemoteException;
public void startScan(com.miui.securitycenter.memory.IMemoryScanCallback callback) throws android.os.RemoteException;
public void startCleanup(com.miui.securitycenter.memory.IMemoryCleanupCallback callback) throws android.os.RemoteException;
public java.util.List<java.lang.String> getRunningApps() throws android.os.RemoteException;
public long getMemoryUsed(java.lang.String pkgName) throws android.os.RemoteException;
public java.util.List<java.lang.String> getLockedComponents() throws android.os.RemoteException;
public int getAppLockState(java.lang.String pkgName) throws android.os.RemoteException;
public void setAppLockState(java.lang.String pkgName, int state) throws android.os.RemoteException;
public int getMemoryScanCount() throws android.os.RemoteException;
}
