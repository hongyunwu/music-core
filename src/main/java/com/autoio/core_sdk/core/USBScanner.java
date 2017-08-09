package com.autoio.core_sdk.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wuhongyun on 17-7-21.
 * u盘扫描管理
 */

public class USBScanner {


	private static USBScanner mUSBScanner = null;


	private UsbManager mUSBManager;
	private USBListener mUSBListener;
	private MediaScannerConnection mediaScannerConnection;

	private USBScanner(Context context) {
		this.mContext = context;
		mUSBManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

	}

	/**
	 * 获取单例对象
	 *
	 * @param context
	 * @return
	 */
	public static USBScanner getInstance(Context context) {

		if (mUSBScanner == null) {
			synchronized (USBScanner.class) {
				if (mUSBScanner == null) {
					mUSBScanner = new USBScanner(context);
				}
			}
		}
		return mUSBScanner;
	}


	private Context mContext;

	/**
	 * 注册usb插拔监听
	 */
	public void registerListener(USBListener usbListener) {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
		intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
		intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
		intentFilter.addDataScheme("file");
		mUSBBroadcastReceiver = new USBBroadcastReceiver();
		this.mUSBListener = usbListener;
		mContext.registerReceiver(mUSBBroadcastReceiver, intentFilter);
	}

	private USBBroadcastReceiver mUSBBroadcastReceiver = null;

	/**
	 * usb挂载监听
	 */
	class USBBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case UsbManager.ACTION_USB_ACCESSORY_ATTACHED://此处应该是手机作为附属物，比如手机插到电脑usb中时
					Logger.i("ACTION_USB_ACCESSORY_ATTACHED");

					break;
				case UsbManager.ACTION_USB_DEVICE_ATTACHED://USB设备挂载
					if (mUSBListener != null) {
						mUSBListener.onUsbAttached();
					}

					break;
				case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
					Logger.i("ACTION_USB_ACCESSORY_DETACHED");
					break;
				case UsbManager.ACTION_USB_DEVICE_DETACHED://USB设备卸载
					if (mUSBListener != null) {
						mUSBListener.onUsbDettached();
					}
					break;
				case Intent.ACTION_MEDIA_MOUNTED:
					String path = intent.getDataString();
					Logger.i("path:" + path);
					if (mUSBListener != null) {
						mUSBListener.onMediaMountedPath(path);
					}
					break;
				case Intent.ACTION_MEDIA_UNMOUNTED:
					if (mUSBListener != null) {
						mUSBListener.onMediaUnMounted(intent.getDataString());
					}
					break;
				case Intent.ACTION_MEDIA_EJECT:
				case Intent.ACTION_MEDIA_BAD_REMOVAL:


					break;

				case Intent.ACTION_MEDIA_SCANNER_FINISHED:
					//扫描动作，也会发生在sd卡中  1.file:///system/media  2.ile:///storage/emulated/0
					Log.i("USBReceiver", USBScanner.getMountPathList().toString());
					if (mUSBListener != null) {
						mUSBListener.onMediaScannerFinished(intent.getDataString());
					}
					break;
				case Intent.ACTION_MEDIA_SCANNER_SCAN_FILE:

					break;
				case Intent.ACTION_MEDIA_SCANNER_STARTED:
					//扫描动作，也会发生在sd卡中  1.file:///system/media  2.file:///storage/emulated/0
					if (mUSBListener != null) {
						mUSBListener.onMediaScannerStarted(intent.getDataString());
					}
					break;
				case Intent.ACTION_MEDIA_CHECKING:

					break;
				case Intent.ACTION_MEDIA_NOFS:

					break;
				case Intent.ACTION_BOOT_COMPLETED:

					break;
			}
		}
	}

	/**
	 * 取消注册usb插拔监听
	 */
	public void unRegisterListener() {
		try {
			if (mUSBBroadcastReceiver != null) {
				mContext.unregisterReceiver(mUSBBroadcastReceiver);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mUSBBroadcastReceiver = null;
		mUSBListener = null;

	}

	/**
	 * 使用mount命令直接获取配置文件的存储卡内容,包括sd卡
	 *
	 * @return 外置卡列表
	 */
	public static List<String> getMountPathList() {
		List<String> pathList = new ArrayList<String>();
		final String cmd = "cat /proc/mounts";
		Runtime run = Runtime.getRuntime();//取得当前JVM的运行时环境
		try {
			Process p = run.exec(cmd);//执行命令
			BufferedInputStream inputStream = new BufferedInputStream(p.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				// 获得命令执行后在控制台的输出信息
				Logger.i(line);
				// /data/media /storage/emulated/0 sdcardfs rw,nosuid,nodev,relatime,uid=1023,gid=1023 0 0
				String[] temp = TextUtils.split(line, " ");
				//分析内容可看出第二个空格后面是路径
				String result = temp[1];
				File file = new File(result);
				//类型为目录、可读、可写，就算是一条挂载路径,
				if (isSdCardFile(file)) {
					Logger.d("add -->" + file.getAbsolutePath());
					pathList.add(result);
				}

				// 检查命令是否执行失败
				if (p.waitFor() != 0 && p.exitValue() == 1) {
					// p.exitValue()==0表示正常结束，1：非正常结束
					Logger.e("命令执行失败!");
				}
			}
			bufferedReader.close();
			inputStream.close();
		} catch (Exception e) {
			Logger.e(e.toString());
			e.printStackTrace();
		}
		return pathList;
	}

	/**
	 * 是否是外挂sd卡 - 通过文件是否能够读写判断
	 *
	 * @param file
	 * @return
	 */
	public static boolean isSdCardFile(File file) {
		return file.isDirectory() && file.canRead() && file.canWrite();
	}

	/**
	 * linux分区 根据/proc/mounts文件里边line的第一个item判断是否是系统分区
	 *
	 * @param line
	 * @return
	 */
	public static boolean isLinuxBranch(String line) {
		// 将常见的linux分区过滤掉
		if (line.contains("proc") || line.contains("tmpfs") || line.contains("media") || line.contains("asec") || line.contains("secure") || line.contains("system") || line.contains("cache")
				|| line.contains("sys") || line.contains("data") || line.contains("shell") || line.contains("root") || line.contains("acct") || line.contains("misc") || line.contains("obb")) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是外挂文件系统 也是根据/proc/mounts文件line的第一个item类型判断是否属于外挂文件系统
	 *
	 * @param line
	 * @return
	 */
	public static boolean isFileSystem(String line) {
		if (line.contains("fat") || line.contains("fuse") || (line.contains("ntfs"))) {
			return true;
		}
		return false;

	}

	/**
	 * 过滤u盘的路径
	 *
	 * @param mountsPath
	 * @return
	 */
	public static String filterUSBPath(List<String> mountsPath) {
		String usbPath = "";
		if (mountsPath != null && mountsPath.size() > 0) {

			for (String path : mountsPath) {
				try {
					//如果外置路径里边存在一条带有usb的，那说明是u盘路径
					if (path.toLowerCase().contains("usb") || path.toLowerCase().contains("udisk")) {
						usbPath = path;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			Logger.i("没有外置的存储卡：" + mountsPath);
		}
		return usbPath;

	}

	/**
	 * 扫描指定文件
	 *
	 * @param context
	 * @param path
	 */
	public void scanMediaFile(Context context, final String path) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // 判断SDK版本是不是4.4或者高于4.4
			usbMediaPaths.clear();
			String[] paths =new String[]{};
			mediaScannerConnection = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
				@Override
				public void onMediaScannerConnected() {
					scanFile(new File(path), mediaScannerConnection);
				}

				@Override
				public void onScanCompleted(String path, Uri uri) {
					usbMediaPaths.remove(path);
					if (usbMediaPaths.size()==0){
						if (mUSBListener != null) {
							mUSBListener.onMediaScannerFinished(path);
						}
					}

				}
			});
			mediaScannerConnection.connect();

			/*MediaScannerConnection.scanFile(context, paths, new String[]{
					"audio/mpeg"
			}, new MediaScannerConnection.OnScanCompletedListener() {
				@Override
				public void onScanCompleted(String path, Uri uri) {
					if (mUSBListener != null) {
						mUSBListener.onMediaScannerFinished(path);
					}
				}
			});*/
		} else {
			final Intent intent;
			File file = new File(path);
			if (file.isDirectory()) {
				intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_DIR");
				intent.setData(Uri.fromFile(file));
			} else {
				intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				intent.setData(Uri.fromFile(file));
			}
			Toast.makeText(context, "scanMediaFile->sendBroadcast", Toast.LENGTH_SHORT).show();
			context.sendBroadcast(intent);
		}

		Logger.i("scanMediaFile 00000->" + path);
		//Toast.makeText(context, "scanMediaFile000->" + path, Toast.LENGTH_SHORT).show();
	}

	private String[] getScanPath( Context context,ArrayList<String> usbMediaPaths) {
		String[] filePaths = new String[usbMediaPaths.size()];
		for (int i = 0; i<usbMediaPaths.size();i++){
			filePaths[i] = usbMediaPaths.get(i);
			Toast.makeText(context, "getScanPath->path" + filePaths[i], Toast.LENGTH_LONG).show();
		}

		return filePaths;
	}

	CopyOnWriteArrayList<String> usbMediaPaths = new CopyOnWriteArrayList<>();


	private void scanFile(File dir, MediaScannerConnection mediaScannerConnection) {
		if (!dir.exists()){
			return ;
		}
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();//将指定文件夹下面的文件全部列出来
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory())
						scanFile(files[i], mediaScannerConnection);
					else {
						//调用mediaScannerConnection.scanFile()方法，更新指定类型的文件到数据库中
						usbMediaPaths.add(files[i].getAbsolutePath());
						mediaScannerConnection.scanFile(files[i].getAbsolutePath(),"audio/mpeg");
					}
				}
			}
		}else{
			usbMediaPaths.add(dir.getAbsolutePath());
			mediaScannerConnection.scanFile(dir.getAbsolutePath(),"audio/mpeg");
		}
	}

}
