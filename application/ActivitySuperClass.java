package com.jingang.ad_fabuyun.application;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jingang.ad_fabuyun.connector.CrashDestroyInterface;
import com.jingang.ad_fabuyun.handler.BaseHandler;
import com.jingang.ad_fabuyun.thread.ThreadPoolManager;
import com.jingang.ad_fabuyun.utils.BaseSharePreference;
import com.jingang.ad_fabuyun.utils.LogUpload;
import com.socks.library.KLog;

/**
 * Activity抽象父类
 * 
 */
public abstract class ActivitySuperClass extends BaseActivity implements
		CrashDestroyInterface {

	protected PlayerApplication application;
	protected BaseSharePreference bspf;
	protected Gson gson;
	/**
	 * h_w[0] 高度
	 * h_w[1] 宽度
	 * h_w[2] DPI
	 */
	protected int h_w[];
	protected ThreadPoolManager th;

	protected BaseHandler handler;
	// 日志处理工具
	public LogUpload mLogUpload;



	/**
	 * 是否需要按两次返回键就退出应用 true 是 false 否。默认不是
	 */
	protected boolean tag_time = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (PlayerApplication) this.getApplication();
		bspf = application.getBaseSharePreference();
		gson = application.getGson();
//		h_w = application.getPixelsHeightWidth(this);
		h_w = application.getResolution(this);
		KLog.e("屏幕信息: heigh-" + h_w[0] + " weight-" + h_w[1] + " dpi-" + h_w[2]);
		
		th = application.getThManager();
		mLogUpload = LogUpload.getLogUpload(th, getApplication());
		KLog.e(this + "-_-onCreate() : th = " + th);
		int longest = (h_w[0] > h_w[1] ? h_w[0] : h_w[1]) / 2;
		//如果使用bitmapfun就不要注释下面代码
		//application.setUpImageFetcher(this, longest, FILE_NAME, image);
		
		BaseHandler.getBaseHandler(this).destroyHandler();

		handler = BaseHandler.getBaseHandler(this);
		application.catchHandler.setCrashDestroyInterface(this);
		onCreateData(savedInstanceState);
	}

	@Override
	protected void onResume() {
		KLog.d("-_-onResume() : " + this);
		super.onResume();
		//如果使用bitmapfun就不要注释下面代码
		//application.setUpImageFetcherTasks();
		onResumeNew();
	}

	@Override
	protected void onPause() {
		KLog.d("-_-onPause() : " + this);
		super.onPause();
		//如果使用bitmapfun就不要注释下面代码
		//application.pauseImageFetcher();
		onPauseNew();
	}

	@Override
	protected void onDestroy() {
		KLog.d("-_-onDestroy() : " + this);
		super.onDestroy();
		onDestroyData();
	}

	@Override
	public void onCrashDestroy() {
		onDestroyData();
	}

	// 记录点击时间值
	private long exitTime;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (tag_time) {
			if (keyCode == KeyEvent.KEYCODE_BACK
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				if ((System.currentTimeMillis() - exitTime) > 2000) {
					Toast.makeText(getApplicationContext(), "再按一次退出",
							Toast.LENGTH_SHORT).show();
					exitTime = System.currentTimeMillis();
				} else {
					onDestroyData();
					System.exit(0);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 弹出土司出来
	 * 
	 * @param context
	 *            土司弹出的内容
	 * @param sign
	 *            时间长度 0是短时间显示，1是长时间显示
	 */
	protected void showToast(String context, int sign) {
		int showduration = 0;
		switch (sign) {
		case 0:
			showduration = Toast.LENGTH_SHORT;
			break;
		case 1:
			showduration = Toast.LENGTH_LONG;
			break;
		}
		Toast.makeText(this, context != null ? context : "", showduration)
				.show();
	}

	/**
	 * 负责Message数据传输
	 * 
	 * @param sign
	 *            传输标识
	 * @param object
	 *            传输内容
	 */
	protected synchronized void putMessage(Handler handler, int sign,
			Object object) {
		Message message = handler.obtainMessage();
		message.what = sign;
		message.obj = object;
		handler.sendMessage(message);
	}

	/**
	 * 开启Thread异步独立线程
	 * 
	 * @param runnable
	 */
	public void asynGetData(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.start();
	}

	/**
	 * 开启线程池短小任务异步队列线程（5个子线程并发运行）
	 * 
	 * @param runnable
	 */
	public void asynGetDataShortTask(Runnable runnable) {
		th.postShortTask(runnable);
	}

	/**
	 * 开启线程池长任务异步队列线程（单个子线程队列执行）
	 * 
	 * @param runnable
	 */
	public void asynGetDataLongTask(Runnable runnable) {
		th.postLongTask(runnable);
	}

	/**
	 * 开启线程池图片提交任务异步队列线程（单个子线程队列执行）
	 * 
	 * @param runnable
	 */
	public void asynGetDataPicTask(Runnable runnable) {
		th.postPicTask(runnable);
	}

	/**
	 * 界面创建方法
	 */
	public abstract void onCreateData(Bundle savedInstanceState);

	/**
	 * 界面结束，数据释放的抽象方法
	 */
	public abstract void onDestroyData();

	// 恢复页面生命
	protected void onResumeNew() {
		KLog.d("-_-onResumeNew() : " + this);
	}

	// 暂停页面生命 
	protected void onPauseNew() {
		KLog.d("-_-onPauseNew() : " + this);
	}
	
	// zzl
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		KLog.d("-_-onStop() : " + this);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		KLog.d("-_-onStart() : " + this);
	}
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		KLog.d("-_-onRestart() : " + this);
	}
	
	
}
