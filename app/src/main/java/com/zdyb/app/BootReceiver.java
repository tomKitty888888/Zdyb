package com.zdyb.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
/**
 * Created by XTER on 2019/3/12.
 * 开机启动
 */

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		//测试时候使用
//		Intent intent1 = new Intent(context, TestSeviceActivity.class);  //MainActivity  TestSeviceActivity
//		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(intent1);

		//正式使用
		Intent intent1 = new Intent(context, EntranceActivity.class);
		intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent1);



	}
}
