package com.jingang.ad_fabuyun.application;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;

import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.jingang.ad_fabuyun.BuildConfig;
import com.jingang.ad_fabuyun.activity.AD_Player_MainActivity;
import com.jingang.ad_fabuyun.activity.CameraActivity;
import com.jingang.ad_fabuyun.activity.SetUpActivity;
import com.jingang.ad_fabuyun.bitmapfun.ImageCache;
import com.jingang.ad_fabuyun.bitmapfun.ImageFetcher;
import com.jingang.ad_fabuyun.handler.CrashHandler;
import com.jingang.ad_fabuyun.http.StaticHttpUrl;
import com.jingang.ad_fabuyun.ntp.TimeCalibrateHelper;
import com.jingang.ad_fabuyun.obj.SystemOnOrOff;
import com.jingang.ad_fabuyun.thread.ThreadPoolManager;
import com.jingang.ad_fabuyun.utils.BaseSharePreference;
import com.jingang.ad_fabuyun.utils.LoadConfigUtil;
import com.jingang.ad_fabuyun.utils.SetUpSystemUtils;
import com.jingang.ad_fabuyun.utils.StaticFileUtils;
import com.socks.library.KLog;
import com.sohuvideo.api.SohuPlayerSDK;

/**
 * APP全局Application
 * 
 */
public class PlayerApplication extends LitePalApplication {
	/**
	 * 请求服务端地址和端口
	 */
	public String http_ip=StaticHttpUrl.ALL_DATA_URL_IP+StaticHttpUrl.ALL_DATA_URL_SERVICE_NAME;
	/**
	 * 设置wifi页面
	 */
	public SetUpActivity setUpActivity;

	public volatile Semaphore mSemaphore = new Semaphore(1);


	/**
	 * 拍照界面
	 */
	public CameraActivity cameraActivity;

	//异常处理基类
	public CrashHandler catchHandler;
	
	public SQLiteDatabase litepalDB;
	
	// 全局变量创建生命周期方法
	@Override
	public void onCreate() {
		super.onCreate();
		// 绑定app出现异常的处理事件
		catchHandler = CrashHandler.getInstance();
		catchHandler.init(getApplicationContext());

//		SohuPlayerSDK.init(getApplicationContext());

		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=58b0f2ef");

		// 日志初始化
		KLog.init(true, "FaBuYun");
//		KLog.init(false, "FaBuYun");

		// 数据库初始化
		litepalDB = Connector.getDatabase();
		
		// 加载配置文件
		loadConfig();

		http_ip = StaticHttpUrl.ALL_DATA_URL_IP + StaticHttpUrl.ALL_DATA_URL_SERVICE_NAME;
	}

	/*
     * 加载配置文件
     */
    private void loadConfig() {
        try {
            if (BuildConfig.DEBUG) {	// 直接运行	Debug模式
//        	if (! BuildConfig.DEBUG) { 	// 		打包app为Debug模式
//        	if (! BuildConfig.DEBUG) {	// 直接运行	Release版本
//            if (BuildConfig.DEBUG) { 	// 		打包app为Release版本
                LoadConfigUtil.load(this, "config-debug.xml");
            } else {
                LoadConfigUtil.load(this, "config-release.xml");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	private BaseSharePreference bspf;

	/**
	 * 获取单例偏好设置工具方法
	 */
	public synchronized BaseSharePreference getBaseSharePreference() {
		if (bspf == null) {
			bspf = new BaseSharePreference();
		}
		return bspf;
	}

	private Gson gson;

	/**
	 * 获取Gson框架方法
	 */
	public synchronized Gson getGson() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	private Intent intent = null;

	/**
	 * 获取处理之后Intent对象的方法
	 * 
	 * @param activity
	 * 
	 * @param calss
	 *            要处理的对象
	 * @return
	 */
	public Intent getDisposeIntent(Activity activity, Class<?> calss) {
		if (intent == null) {
			intent = new Intent();
		}
		intent.setClass(activity, calss);
		return intent;
	}

	/**
	 * 获取屏幕的对象
	 */
	private DisplayMetrics displayMetrics;
	/**
	 * 返回屏幕获取的高度和宽度数组
	 */
	private int h_w[] = null;

	/**
	 * 返回屏幕获取的高度和宽度
	 * 
	 * @param activity
	 * @return int[0]是高度，int[1]是宽度，int[2]是DPI
	 */
	public synchronized int[] getPixelsHeightWidth(Activity activity) {
		if (displayMetrics == null) {
			displayMetrics = new DisplayMetrics();
		}
		if (h_w == null) {
			h_w = new int[3];
		}
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		h_w[0] = displayMetrics.heightPixels;
		h_w[1] = displayMetrics.widthPixels;
		h_w[2] = displayMetrics.densityDpi;
		return h_w;
	}

	/**
	 * 获取分辨率
	 * @param activity
	 * @return	int[0]是高度，int[1]是宽度，int[2]是DPI
     */
	public synchronized int[] getResolution (Activity activity) {
		WindowManager windowManager = activity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		if (displayMetrics == null) {
			displayMetrics = new DisplayMetrics();
		}
		if (h_w == null) {
			h_w = new int[3];
		}
		try {
			Class c = Class.forName("android.view.Display");
			Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
			method.invoke(display, displayMetrics);
			h_w[0] = displayMetrics.heightPixels;// 得到高度
			h_w[1] = displayMetrics.widthPixels;// 得到宽度
			h_w[2] = displayMetrics.densityDpi;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return h_w;
	}

	// ImageFetcher负责异步加载图片到我们ImageView
	private ImageFetcher mImageFetcher;

	/**
	 * 初始化ImageFetcher对象
	 * 
	 * @param activity
	 * @param longest
	 *            手机屏幕最大的分辨率
	 * @param fileName
	 *            文件名字
	 * @param r_image
	 *            默认显示的图片
	 */
	public synchronized void setUpImageFetcher(FragmentActivity activity,
			int longest, String fileName, int r_image) {
		if (mImageFetcher == null) {
			mImageFetcher = new ImageFetcher(this, longest);
			// 设置保存的路径名字
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
					this, fileName);
			cacheParams.setMemCacheSizePercent(0.25f); // 设置内存缓存应用程序内存的25%
			mImageFetcher.addImageCache(activity.getSupportFragmentManager(),
					cacheParams);
			mImageFetcher.setImageFadeIn(false);
			// 获取图片之前默认显示的图片
			mImageFetcher.setLoadingImage(r_image);
		}
	}

	/**
	 * 设置ImageFetcher的ExitTasksEarly出口线程不可退出（运行时调用）
	 */
	public void setUpImageFetcherTasks() {
		if (mImageFetcher != null) {
			mImageFetcher.setExitTasksEarly(false);
		}
	}

	/**
	 * 设置ImageFetcher的ExitTasksEarly出口线程可以退出,释放高速缓存（暂停时调用）
	 */
	public void pauseImageFetcher() {
		if (mImageFetcher != null) {
			mImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
		}
	}

	/**
	 * 清除ImageFetcher的高速缓存，释放ImageFetcher（销毁时调用）
	 */
	public void closeImageFetcher() {
		if (mImageFetcher != null) {
			mImageFetcher.closeCache();
			mImageFetcher = null;
		}
	}

	/**
	 * ImageFetcher加载图片
	 */
	public void loadImage(String imageUrl, ImageView imageView) {
		if (mImageFetcher != null) {
			mImageFetcher.loadImage(imageUrl, imageView);
		}
	}

	private ThreadPoolManager th;

	/**
	 * 获取线程池管理类
	 * 
	 * @return
	 */
	public ThreadPoolManager getThManager() {
		if (th == null)
			th = ThreadPoolManager.getInstance();

		return th;
	}

	private SetUpSystemUtils systemUtils;

	/**
	 * 获取操作系统对象
	 * 
	 * @param activity
	 * @return
	 */
	public SetUpSystemUtils getSetUpSystemUtils(Activity activity) {
		if (systemUtils == null)
			systemUtils = SetUpSystemUtils.getSetUpSystemUtils(activity);
		return systemUtils;
	}

	private SystemOnOrOff systemOnOrOff;

	/**
	 * 获取重启时间对象
	 * 
	 * @param activity
	 *            子线程跳转主线程传输数据的对象
	 * 
	 * @param time
	 *            开机倒计时(单位秒)
	 */
	public SystemOnOrOff getSystemOnOrOff(long time,Activity activity) {
		if (systemOnOrOff == null) {
			try {
				systemOnOrOff = new SystemOnOrOff(activity);
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			systemOnOrOff.setThisData(time);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return systemOnOrOff;
	}

	/**
	 * 程序结束，释放内存
	 */
	public void destroyAppVariable() {
		if (bspf != null)
			bspf = null;
		if (gson != null)
			gson = null;
		if (intent != null)
			intent = null;
		if (displayMetrics != null)
			displayMetrics = null;
		if (h_w != null)
			h_w = null;
		if (th != null) {
			th.exit();
			th = null;
		}
		if (systemUtils != null) {
			systemUtils = null;
		}
		if (systemOnOrOff != null) {
			systemOnOrOff.destroySerialPort();
			systemOnOrOff = null;
		}
		
		if (litepalDB != null) {
			litepalDB.releaseReference();
			litepalDB = null;
		}
		closeImageFetcher();
	}
	
}
