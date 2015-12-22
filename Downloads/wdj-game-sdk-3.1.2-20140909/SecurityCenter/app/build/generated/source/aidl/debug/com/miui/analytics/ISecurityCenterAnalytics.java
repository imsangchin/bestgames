/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/huwei/Downloads/wdj-game-sdk-3.1.2-20140909/SecurityCenter/app/src/main/aidl/com/miui/analytics/ISecurityCenterAnalytics.aidl
 */
package com.miui.analytics;
public interface ISecurityCenterAnalytics extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.miui.analytics.ISecurityCenterAnalytics
{
private static final java.lang.String DESCRIPTOR = "com.miui.analytics.ISecurityCenterAnalytics";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.miui.analytics.ISecurityCenterAnalytics interface,
 * generating a proxy if needed.
 */
public static com.miui.analytics.ISecurityCenterAnalytics asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.miui.analytics.ISecurityCenterAnalytics))) {
return ((com.miui.analytics.ISecurityCenterAnalytics)iin);
}
return new com.miui.analytics.ISecurityCenterAnalytics.Stub.Proxy(obj);
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
case TRANSACTION_trackEvent:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.trackEvent(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_trackEventWithValue:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
this.trackEventWithValue(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_trackEventWithParams:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.util.Map _arg1;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg1 = data.readHashMap(cl);
this.trackEventWithParams(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_trackEventWithValueAndParams:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _arg1;
_arg1 = data.readLong();
java.util.Map _arg2;
java.lang.ClassLoader cl = (java.lang.ClassLoader)this.getClass().getClassLoader();
_arg2 = data.readHashMap(cl);
this.trackEventWithValueAndParams(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.miui.analytics.ISecurityCenterAnalytics
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
@Override public void trackEvent(java.lang.String eventId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventId);
mRemote.transact(Stub.TRANSACTION_trackEvent, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void trackEventWithValue(java.lang.String eventId, long value) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventId);
_data.writeLong(value);
mRemote.transact(Stub.TRANSACTION_trackEventWithValue, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void trackEventWithParams(java.lang.String eventId, java.util.Map parameters) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventId);
_data.writeMap(parameters);
mRemote.transact(Stub.TRANSACTION_trackEventWithParams, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void trackEventWithValueAndParams(java.lang.String eventId, long value, java.util.Map parameters) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(eventId);
_data.writeLong(value);
_data.writeMap(parameters);
mRemote.transact(Stub.TRANSACTION_trackEventWithValueAndParams, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_trackEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_trackEventWithValue = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_trackEventWithParams = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_trackEventWithValueAndParams = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void trackEvent(java.lang.String eventId) throws android.os.RemoteException;
public void trackEventWithValue(java.lang.String eventId, long value) throws android.os.RemoteException;
public void trackEventWithParams(java.lang.String eventId, java.util.Map parameters) throws android.os.RemoteException;
public void trackEventWithValueAndParams(java.lang.String eventId, long value, java.util.Map parameters) throws android.os.RemoteException;
}
