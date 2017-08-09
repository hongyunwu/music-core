package com.autoio.core_sdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.autoio.core_sdk.core.USBScanner;
import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2017/7/22.
 * 用于监听USB插拔事件,最好动态注册
 */

public class USBReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i("USBReceiver","action->"+action+" ,intent.getData->"+intent.getData());

		switch (action){
			case Intent.ACTION_MEDIA_MOUNTED:
				String path = intent.getDataString();
				Logger.i("path:"+path);

				break;
			case Intent.ACTION_MEDIA_UNMOUNTED:
			case Intent.ACTION_MEDIA_EJECT:
			case Intent.ACTION_MEDIA_BAD_REMOVAL:
				break;

			case Intent.ACTION_MEDIA_SCANNER_FINISHED:
				//扫描动作，也会发生在sd卡中  1.file:///system/media  2.ile:///storage/emulated/0
				Log.i("USBReceiver", USBScanner.getMountPathList().toString());
				break;
			case Intent.ACTION_MEDIA_SCANNER_SCAN_FILE:

				break;
			case Intent.ACTION_MEDIA_SCANNER_STARTED:
				//扫描动作，也会发生在sd卡中  1.file:///system/media  2.file:///storage/emulated/0

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
