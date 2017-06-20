package com.jingang.ad_fabuyun.application;

import android.os.Message;
import android.support.v4.app.FragmentActivity;

/**
 * 处理handler的activity基类
 * 
 */
public abstract class BaseActivity extends FragmentActivity {
	/**
	 * 处理handler返回的message信息
	 * 
	 * @param msg
	 */
	public abstract void handleMessage(Message msg);
}
