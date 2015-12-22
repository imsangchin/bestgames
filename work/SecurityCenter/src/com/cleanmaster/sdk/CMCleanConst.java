package com.cleanmaster.sdk;

/**
 * 
 * The CONSTANT VALUE in CM Clean SDK
 *
 */
public class CMCleanConst {
	/**
	 * Standard junk file scanning
	 */
	public static final int MASK_SCAN_COMMON		= 1<<0;
	/**
	 * Advanced junk file scanning
	 */
	public static final int MASK_SCAN_ADVANCED		= 1<<1;
	
	/**
	 * SDK service action name, the Intent Action when bind SDK service.
	 */
	public static final String ACTION_CLEAN_SERVICE = "com.cleanmaster.action.sdk.CleanService";	
	
	/**
	 * Update success or need update
	 */
	public static final int UPDATE_ERROR_CODE_SUCCESS = 0;

	/**
	 * Already update to the latest database
	 */
	public static final int UPDATE_ERROR_CODE_NO_NEWER_DB = 1;

	/**
	 * Update is running
	 */
	public static final int UPDATE_ERROR_CODE_ALREADY_RUNNING = 2;
	/**
	 * Network error during update
	 */
	public static final int UPDATE_ERROR_CODE_NETWORK_ERROR = 3;
	/**
	 * Other error during update
	 */
	public static final int UPDATE_ERROR_CODE_UNKNOWN_ERROR = 4;
}
