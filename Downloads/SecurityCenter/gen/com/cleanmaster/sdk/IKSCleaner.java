/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/heyongjun/miui_workspace/Mi3_W_V6/packages/apps/SecurityCenter/src/com/cleanmaster/sdk/IKSCleaner.aidl
 */
package com.cleanmaster.sdk;
public interface IKSCleaner extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cleanmaster.sdk.IKSCleaner
{
private static final java.lang.String DESCRIPTOR = "com.cleanmaster.sdk.IKSCleaner";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cleanmaster.sdk.IKSCleaner interface,
 * generating a proxy if needed.
 */
public static com.cleanmaster.sdk.IKSCleaner asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cleanmaster.sdk.IKSCleaner))) {
return ((com.cleanmaster.sdk.IKSCleaner)iin);
}
return new com.cleanmaster.sdk.IKSCleaner.Stub.Proxy(obj);
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
case TRANSACTION_getVersion:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getVersion();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_init:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.init(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_scanCache:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
com.cleanmaster.sdk.ICacheCallback _arg1;
_arg1 = com.cleanmaster.sdk.ICacheCallback.Stub.asInterface(data.readStrongBinder());
this.scanCache(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_scanResidual:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
com.cleanmaster.sdk.IResidualCallback _arg1;
_arg1 = com.cleanmaster.sdk.IResidualCallback.Stub.asInterface(data.readStrongBinder());
this.scanResidual(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_scanAdDir:
{
data.enforceInterface(DESCRIPTOR);
com.cleanmaster.sdk.IAdDirCallback _arg0;
_arg0 = com.cleanmaster.sdk.IAdDirCallback.Stub.asInterface(data.readStrongBinder());
this.scanAdDir(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_pathCalcSize:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
long _result = this.pathCalcSize(_arg0);
reply.writeNoException();
reply.writeLong(_result);
return true;
}
case TRANSACTION_getResidualFilePaths:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String[] _result = this.getResidualFilePaths(_arg0);
reply.writeNoException();
reply.writeStringArray(_result);
return true;
}
case TRANSACTION_IsEnableAutoUpdate:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.IsEnableAutoUpdate();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_SetEnableAutoUpdate:
{
data.enforceInterface(DESCRIPTOR);
boolean _arg0;
_arg0 = (0!=data.readInt());
this.SetEnableAutoUpdate(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_GetDataVersion:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.GetDataVersion();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_StartUpdateCheck:
{
data.enforceInterface(DESCRIPTOR);
com.cleanmaster.sdk.ICmSdkUpdateCallback _arg0;
_arg0 = com.cleanmaster.sdk.ICmSdkUpdateCallback.Stub.asInterface(data.readStrongBinder());
this.StartUpdateCheck(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_StartUpdateData:
{
data.enforceInterface(DESCRIPTOR);
this.StartUpdateData();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cleanmaster.sdk.IKSCleaner
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
// 接口版本号，最好是每次版本号相等才去调用

@Override public int getVersion() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getVersion, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 初始化函数，每次必须调用

@Override public void init(java.lang.String language, java.lang.String country) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(language);
_data.writeString(country);
mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 扫描缓存，mask标识代表扫描的范围，详见CMCleanConst.java

@Override public void scanCache(int nScanMask, com.cleanmaster.sdk.ICacheCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nScanMask);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanCache, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 扫描残留，mask标识代表扫描的范围，详见CMCleanConst.java

@Override public void scanResidual(int nMask, com.cleanmaster.sdk.IResidualCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(nMask);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanResidual, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 扫描广告

@Override public void scanAdDir(com.cleanmaster.sdk.IAdDirCallback observer) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((observer!=null))?(observer.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_scanAdDir, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 计算路径大小(dirPath是绝对路径)

@Override public long pathCalcSize(java.lang.String dirPath) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(dirPath);
mRemote.transact(Stub.TRANSACTION_pathCalcSize, _data, _reply, 0);
_reply.readException();
_result = _reply.readLong();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 获取残留路径

@Override public java.lang.String[] getResidualFilePaths(java.lang.String pkgName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(pkgName);
mRemote.transact(Stub.TRANSACTION_getResidualFilePaths, _data, _reply, 0);
_reply.readException();
_result = _reply.createStringArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 是否允许自动升级

@Override public boolean IsEnableAutoUpdate() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_IsEnableAutoUpdate, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 设置是否允许自动升级

@Override public void SetEnableAutoUpdate(boolean bEnable) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(((bEnable)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_SetEnableAutoUpdate, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 获取库版本号

@Override public java.lang.String GetDataVersion() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_GetDataVersion, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
// 开始检测升级

@Override public void StartUpdateCheck(com.cleanmaster.sdk.ICmSdkUpdateCallback callBack) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callBack!=null))?(callBack.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_StartUpdateCheck, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
// 开始进行升级

@Override public void StartUpdateData() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_StartUpdateData, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_init = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_scanCache = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_scanResidual = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_scanAdDir = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_pathCalcSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getResidualFilePaths = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_IsEnableAutoUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_SetEnableAutoUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_GetDataVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_StartUpdateCheck = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_StartUpdateData = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
}
// 接口版本号，最好是每次版本号相等才去调用

public int getVersion() throws android.os.RemoteException;
// 初始化函数，每次必须调用

public void init(java.lang.String language, java.lang.String country) throws android.os.RemoteException;
// 扫描缓存，mask标识代表扫描的范围，详见CMCleanConst.java

public void scanCache(int nScanMask, com.cleanmaster.sdk.ICacheCallback observer) throws android.os.RemoteException;
// 扫描残留，mask标识代表扫描的范围，详见CMCleanConst.java

public void scanResidual(int nMask, com.cleanmaster.sdk.IResidualCallback observer) throws android.os.RemoteException;
// 扫描广告

public void scanAdDir(com.cleanmaster.sdk.IAdDirCallback observer) throws android.os.RemoteException;
// 计算路径大小(dirPath是绝对路径)

public long pathCalcSize(java.lang.String dirPath) throws android.os.RemoteException;
// 获取残留路径

public java.lang.String[] getResidualFilePaths(java.lang.String pkgName) throws android.os.RemoteException;
// 是否允许自动升级

public boolean IsEnableAutoUpdate() throws android.os.RemoteException;
// 设置是否允许自动升级

public void SetEnableAutoUpdate(boolean bEnable) throws android.os.RemoteException;
// 获取库版本号

public java.lang.String GetDataVersion() throws android.os.RemoteException;
// 开始检测升级

public void StartUpdateCheck(com.cleanmaster.sdk.ICmSdkUpdateCallback callBack) throws android.os.RemoteException;
// 开始进行升级

public void StartUpdateData() throws android.os.RemoteException;
}
