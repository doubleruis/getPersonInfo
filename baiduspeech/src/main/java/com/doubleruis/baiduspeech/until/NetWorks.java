package com.doubleruis.baiduspeech.until;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class NetWorks {


	public static boolean isNetworkConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null) {
			return ni.isAvailable();
		}
		return false;
	}

	/**
	 * 获取年龄
	 *
	 * @param
	 * @return
	 */
	public static boolean checkdate(String birthstr) throws Exception {
		boolean b = false;
		String checkbirth = birthstr.substring(0, 4) + "-"
				+ birthstr.substring(4, 6) + "-" + birthstr.substring(6, 8);

		java.text.SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd");
//        String s= "2011-07-09 ";
		Date birthDay =  formatter.parse(checkbirth);
		Long exptime = birthDay.getTime()+86399000; //获取当天23:59:59秒的时间
		//获取当前系统时间
//        Calendar cal = Calendar.getInstance();
//        //如果有效日期小于当前时间，则超过有效期
//        if (cal.before(birthDay)) {
//            Log.e("checkdate","时间过期");
//            b = true;
//        }


		/**当前系统时间对应long值  add by wdh 20170411 20:42 start**/
		Date now = new Date();
		Long nowtime = now.getTime();
		//证件失效时间(该时间为00：00：00)
		if(nowtime>=exptime){
			b = true;
		}
		return b;
	}


	/**
	 * @author zhangyinfeng
	 * @Description: 判断wifi网络是否可用
	 */
	public static boolean isWifiConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (ni != null) {
			return ni.isAvailable();
		}
		return false;
	}

	/**
	 * @author zhangyinfeng
	 * @Description: 判断mobile网络是否可用
	 */
	public static boolean isMobileConnected(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (ni != null) {
			return ni.isAvailable();
		}
		return false;
	}

	/**
	 * @author zhangyinfeng
	 * @Description: 判断网络连接类型
	 */
	public static int getConnectedType(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isAvailable()) {
			return ni.getType();
		}
		return -1;
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 获取当前时间
	 * @param mag
	 *            时间格式
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getCurrentDateTime(String format) {
		SimpleDateFormat df = new SimpleDateFormat(format); // 设定时间格式
		return df.format(new Date()); // 获取当前时间
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 获取当前年份
	 */
	public static int getCurrentYear() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.YEAR);
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 获取当前月份
	 */
	public static int getCurrentMonth() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1;
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 获取当前日
	 */
	public static int getCurrentDay() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @author zhangyinfeng
	 * @Description: 获取下载文件的大小
	 * @param filepath
	 *            下载路径
	 */
	public static int getFileLength(String filepath) {
		int fileLength = 0;
		try {
			URL url = new URL(filepath);
			HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
			fileLength = urlCon.getContentLength();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileLength;
	}

	/**
	 * @Description:线程超时操作
	 * @param handler
	 * @return
	 */
	public static Timer startTimer(final Handler handler, int millisecond) {
		Timer timer = new Timer();
		TimerTask tast = new TimerTask() {
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = 10000;
				Bundle bundle = new Bundle();
				bundle.putString("retCode", "访问服务"); // 返回结果编码
				bundle.putString("retDesc", "向服务器请求超时！"); // 返回结果描述
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		};
		timer.schedule(tast, millisecond);
		return timer;
	}

	/**
	 * @author wuliang
	 * @Description: 获取服务地址
	 */
	public static String geturl(Context context) {
		String wsaddr = "http://"
				+ FileStream
				.loadConfig(context, "Connection", "service_ip")
				//+ ":"
				+ FileStream.loadConfig(context, "Connection", "port")
				+ "/"
				+ FileStream.loadConfig(context, "Connection",
				"project_name");
//        String wsaddr =getspareurl();
		return wsaddr;
	}
}
