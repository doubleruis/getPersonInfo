 package com.doubleruis.getPersonInfo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

 /**
  * @author 张尹枫
  * @category 对本地存储的操作
  */
 public class LocalStorage {
     private static SharedPreferences sharedPreferences; // 一种轻型的数据存储方式,它的本质是基于XML文件存储key-value键值对数据

     /**
      * 向SharedPreferences中注入数据
      */
     public static void putValues(Context context, String name, String key, String value) {
         sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
         Editor editor = sharedPreferences.edit();
         editor.putString(key, value);
         editor.commit(); // 这个提交很重要，别忘记，对xml修改一定别忘了commit()
     }

     /**
      * 从SharedPreferences中获取数据
      */
     public static String getValues(Context context, String name, String key) {
         sharedPreferences = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
         return sharedPreferences.getString(key, null);
     }

     /**
      * 清除SharedPreferences中的数据
      */
     public void clear() {
         Editor editor = sharedPreferences.edit();
         editor.clear();
         editor.commit();
     }



 }
