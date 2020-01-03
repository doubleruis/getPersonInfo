package com.doubleruis.baduspeech.until;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Environment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

import cn.finedo.adcore.common.AndroidApplication;

/**
 * @author 张尹枫
 * @category 对文件/目录的操作
 */
public class FileStream {
	/**
	 * @author ZhangYinFeng
	 * @Description: 读取Properties文件
	 */
	public static String loadConfig(Context context, String fileName, String key) {
		Properties pro = new Properties();
		try {
			InputStream is = context.getAssets().open(fileName);
			pro.load(is);
		} catch (Exception ex) {
//			//log.e("文件读取", ex.toString());
		}
		return pro.getProperty(key).trim();
	}
	/**
	 * @author wl
	 * @Description: 读取Properties文件
	 */
//	public static String loadConfig(String fileName, String key) {
//		Properties pro = new Properties();
//		AndroidApplication d= AndroidApplication.getInstance();
//		Context context=d.getApplicationContext();
//		InputStream is=null;
//		try {
//			is = context.getAssets().open(fileName);
//			pro.load(is);
//		} catch (Exception ex) {
////			//log.e("文件读取", ex.toString());
//		}finally {
//			try {
//				if (is != null)
//					is.close();
//			}catch (Exception e){}
//		}
//		return pro.getProperty(key).trim();
//	}

	//读取
	public static String loadConfig(String fileName, String key){
		Properties prop = new Properties();
		AndroidApplication d= AndroidApplication.getInstance();
		Context context=d.getApplicationContext();
		try {
			InputStream in = context.getApplicationContext().getAssets().open("Connection");
			prop.load( new InputStreamReader(in,"utf-8"));
		}catch (Exception e){

		}
		String value  = prop.getProperty(key);
		return value;
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 写入Properties文件
	 * @param file
	 *            路径
	 * @param pro
	 *            Properties对象
	 */
	public static void saveConfig(String file, Properties pro) {
		try {
			FileOutputStream os = new FileOutputStream(file);
			pro.store(os, "");
		} catch (Exception ex) {
//			//log.e("文件写入", ex.toString());
		}
	}

	//保存配置文件
	public static String setProperties(Context context, String file, String keyName, String keyValue) {
		Properties props = new Properties();
		InputStream is=null;
		try {
			is = context.getAssets().open(file);
			props.load(is);
			props.setProperty(keyName, keyValue);
			FileOutputStream out = context.openFileOutput(file, Context.MODE_PRIVATE);
			props.store(out, null);
		} catch (Exception e) {
			e.printStackTrace();
			return "修改配置文件失败!";
		} finally {
			try {
				if (is != null)
					is.close();
			}catch (Exception e){}
		}
		return "设置成功";
	}

	//修改
	public static String setValue(String fileName, String key, String value) {
		Properties prop = new Properties();
		AndroidApplication d= AndroidApplication.getInstance();
		Context context=d.getApplicationContext();
		try {
			InputStream in = context.getApplicationContext().getAssets().open(fileName);
			prop.load( new InputStreamReader(in,"utf-8"));
			prop.setProperty (key,value);
			File file = new File("file:///android_asset/"+fileName);
			FileOutputStream fos = new FileOutputStream(file);
			prop.store(fos, "Update '" + key + "' value");
			fos.flush();
			return value;
		} catch (Exception e1) {
			return null;
		} finally {

		}
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 获取当前应用版本号
	 */
	public static String getVersionName(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packInfo = null;
		try {
			packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packInfo.versionName;
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 获取手机SDCARD路径
	 */
	public static String getSDCardPath() {
		String dir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			dir = Environment.getExternalStorageDirectory().toString();// 获取跟目录
		}
		return dir;
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 创建目录
	 */
	public static void createFolder(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	/**
	 * @author ZhangYinFeng
	 * @Description: 删除文件或目录
	 */
	public static void deleteFolder(String fileName) {
		File file = new File(fileName);
		if (file.exists()) { // 判断目录或文件是否存在
			file.delete();
		}
	}

	public static String getStringFromJson(JSONObject json, String key) {
		try {
			return json.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false。
	 */
	public static boolean DeleteFolderall(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 判断指定程序是否安装
	 */
	public static boolean isInstall(Context context, String pageName) {
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> packs = pm.getInstalledPackages(0);
		boolean isExist = false;
		for (PackageInfo pi : packs) {
			if (pi.applicationInfo.packageName.equals(pageName)) {
				isExist = true;
				break;
			}
		}
		return isExist;
	}

	/**
	 * 把ASSETS下文件复制到SD卡中
	 */
	public static boolean copyFromAssets(Context context, String fileName, String path) {
		boolean copyIsFinish = false;
		FileOutputStream fos=null;
		InputStream is=null;
		try {
			is = context.getAssets().open(fileName);
			File file = new File(path);
			file.createNewFile();
			fos = new FileOutputStream(file);
			byte[] temp = new byte[1024];
			int i = 0;
			while ((i = is.read(temp)) > 0) {
				fos.write(temp, 0, i);
			}
			fos.close();
			is.close();
			copyIsFinish = true;
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (fos != null)
					fos.close();
			}catch (Exception e){}
			try {
				if (is != null)
					is.close();
			}catch (Exception e){}
		}
		return copyIsFinish;
	}
	//照相机照完相后向SD卡中存储照片
		/**
		 *   拍照，按照最大的kb数进行压缩
		* @author	ccd
		 * @date 2015-3-31 t下午3:50:32
		 * @Description:
		 * @param
		 */
		public static void saveToSD(Bitmap bitmap, String filedir, String fileName, int maxby) {
			FileOutputStream fos = null;
		    BufferedOutputStream bos = null;
		    ByteArrayOutputStream baos = null; // 字节数组输出流
		    try {  
		        baos = new ByteArrayOutputStream();
		        
		        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);  //30为 演示
		        
		        if(baos.toByteArray().length<maxby*1024){ //如果图片小于 100kb , 不压缩
		        	
		        }else{
		        	int percent = 100;
		        	while(baos.toByteArray().length>=maxby*1024){
		        		percent-=10;
			        	baos.reset();  
			        	bitmap.compress(Bitmap.CompressFormat.JPEG, percent, baos);
		        	}
		        }
		        
		        File dir = new File(filedir);
		        //文件不存在新建
		        if (!dir.exists()) { 
		            dir.mkdir(); // 创建文件夹  
		        }  
		        //这是文件名，，以系统时间命名的
		        String files= String.valueOf(System.currentTimeMillis());
		        File file = new File(fileName);
		        file.delete();  
		        if (!file.exists()) {  
		            file.createNewFile();// 创建文件  
		        }  
		        fos = new FileOutputStream(file);
		        bos = new BufferedOutputStream(fos);
		        //写入字节数组
		        bos.write(baos.toByteArray()); 
		        
		        bitmap.recycle();
		        bitmap=null;
		        
		    } catch (Exception e) {
		        e.printStackTrace();  
		
		    } finally {  
		        if (baos != null) {  
		            try {  
		                baos.close();  
		            } catch (Exception e) {
		                e.printStackTrace();  
		            }  
		        }  
		        if (bos != null) {  
		            try {  
		                bos.close();  
		            } catch (Exception e) {
		                e.printStackTrace();  
		            }  
		        }  
		        if (fos != null) {  
		            try {  
		                fos.close();  
		            } catch (Exception e) {
		                e.printStackTrace();  
		            }  
		        }  
		
		    }  
		}
		
		/***
		 * 
		* @author	ccd
		 * @date 2015-3-26 t下午3:29:26
		 * @Description:读取图片路径获取 byte值
		 * @param
		 * 
		 */
		public static String imagebytefrompath(String path) throws Exception {
			// 1. 加载文件到字节数组
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			DataInputStream dis=new DataInputStream(new FileInputStream(new File(path)));
			byte[] b = new byte[1024];
			int n;
			while ((n = dis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			dis.close();
			bos.close();
			String inputB= (android.util.Base64.encodeToString(bos.toByteArray(),android.util.Base64.DEFAULT));
			inputB=inputB.replaceAll("\n","");
			inputB=inputB.replaceAll("\r","");
			return inputB;
		}

	/**
	 * 删除文件
	 *
	 * @param fileName
	 *            文件名称
	 * @return 文件内容
	 * @throws IOException
	 */
	public static void  delete(String fileName) throws IOException {
		File file = new File(Environment.getExternalStorageDirectory(), fileName);
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			}
			// 如果它是一个目录
			else if (file.isDirectory()) {
				// 声明目录下所有的文件 files[];
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					(files[i]).delete(); // 把每个文件 用这个方法进行迭代
				}
			}
			file.delete();
		}
	}

}
