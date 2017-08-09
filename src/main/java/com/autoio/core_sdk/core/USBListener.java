package com.autoio.core_sdk.core;

/**
 * Created by lenovo on 2017/7/22.
 */

public interface USBListener {
	/**
	 * Usb挂载
	 */
	void onUsbAttached();

	/**
	 * Usb卸载
	 */
	void onUsbDettached();

	/**
	 * 获取到存储卡的目录
	 * @param path
	 */
	void onMediaMountedPath(String path);

	/**
	 * 储存卡卸载
	 * @param path
	 */
	void onMediaUnMounted(String path);

	void onMediaScannerStarted(String dataString);

	void onMediaScannerFinished(String dataString);
}
