package com.doubleruis.baduspeech.helper;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by mac on 2018/8/28.
 */

public class HttpUtils {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码

    private static final String BOUNDARY = "FlPm4LpSXsE" ; //UUID.randomUUID().toString(); //边界标识 随机生成 String PREFIX = "--" , LINE_END = "\r\n";
    private static final String PREFIX="--";
    private static final String LINE_END="\n\r";
    private static final String CONTENT_TYPE = "multipart/form-data"; //内容类型

    /**
     * Get请求，获得返回数据
     *
     * @param urlStr
     * @return
     * @throws Exception
     */
    public static String doGet(String urlStr)
    {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try
        {
            url = new URL(urlStr);
            //url=new URL("http://api.365cha.com.cn/api/Charge/PA_GetChargeList?NewEnrolmentID=10101034712360062812160&Kinder_Class_ID=10101034708869961482240&KindergartenID=10101034608628155285504&TrialID=10101034608336332390400");
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            if (conn.getResponseCode() == 200)
            {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else
            {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }
            try
            {
                if (baos != null)
                    baos.close();
            } catch (IOException e)
            {
            }
            conn.disconnect();
        }

        return null ;

    }
    /**
     * Get请求，获得返回数据
     *
     * @param urlStr
     * @return
     * @throws Exception
     */
    public static String doGet(String urlStr,Map<String, Object> map)
    {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try
        {
            StringBuilder request = new StringBuilder();
            request.append(MyAppApiConfig.PRO_SERVER_BASE_URL+urlStr+"?");
            if(map!=null)
            for (String key : map.keySet()) {
                request.append(key + "=" + URLEncoder.encode(map.get(key).toString(), "UTF-8") + "&");
            }
            url = new URL(request.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            int code = conn.getResponseCode();
            if (conn.getResponseCode() == 200)
            {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else
            {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return "{'status':'1000','msg':'服务器出小差了呢～','data':''}";
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }
            try
            {
                if (baos != null)
                    baos.close();
            } catch (IOException e)
            {
            }
            conn.disconnect();
        }
        //return null ;
    }
    /**
     * Get请求，获得返回数据
     *
     * @param urlStr
     * @return
     * @throws Exception
     */
    public static String dopost(String urlStr,Map<String, Object> map)
    {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try
        {
            StringBuilder request = new StringBuilder();
            request.append(MyAppApiConfig.PRO_SERVER_BASE_URL+urlStr+"?");
            if(map!=null)
            for (String key : map.keySet()) {
                //request.append(key + "=" + URLEncoder.encode(map.get(key), "UTF-8") + "&");
                request.append(key + "=" + map.get(key) + "&");
            }
            url = new URL(request.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(20000);//链接超时
            conn.setReadTimeout(20000);//读取超时
            //发送post请求必须设置
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //conn.setRequestProperty("Content-Type", "application/json");
            int code = conn.getResponseCode();
            if (conn.getResponseCode() == 200)
            {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else
            {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return "1000";
            //return "{'status':'1000','msg':'服务器出小差了呢～','data':''}";
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }
            try
            {
                if (baos != null)
                    baos.close();
            } catch (IOException e)
            {
            }
            conn.disconnect();
        }
        //return null ;
    }
    public static String dopost2(String urlStr,Map<String, Object> map)
    {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try
        {
            StringBuilder request = new StringBuilder();
            request.append("http://image.365cha.com.cn/ChatPostFile.ashx"+"?");
            if(map!=null)
                for (String key : map.keySet()) {
                    //request.append(key + "=" + URLEncoder.encode(map.get(key), "UTF-8") + "&");
                    request.append(key + "=" + map.get(key) + "&");
                }
            url = new URL(request.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30 * 1000); //30秒连接超时
            conn.setReadTimeout(30 * 1000);   //30秒读取超时
            conn.setDoInput(true);  //允许文件输入流
            conn.setDoOutput(true); //允许文件输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式为POST
            conn.setRequestProperty("Charset", CHARSET);  //设置编码为utf-8
            conn.setRequestProperty("connection", "keep-alive"); //保持连接
            //conn.setRequestProperty("Cookie", "sid=" + firstCookie + ";" + "cgi_ck=" +secondCookie);//设置cookie，多个cookie用;分开
            conn.setRequestProperty("Content-Type", "multipart/form-data" + ";boundary=" + BOUNDARY);

            int code = conn.getResponseCode();
            if (conn.getResponseCode() == 200)
            {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else
            {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return "{'status':'1000','msg':'服务器出小差了呢～','data':''}";
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }
            try
            {
                if (baos != null)
                    baos.close();
            } catch (IOException e)
            {
            }
            conn.disconnect();
        }
        //return null ;
    }
    /**
     * get json方式
     * @param urlPath
     * @param map
     * @return
     */
    public static String doJsonGet(String urlPath, Map<String, String> map) {
        URL url = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try
        {
            StringBuilder request = new StringBuilder();
            for (String key : map.keySet()) {
                request.append(key + "=" + URLEncoder.encode(map.get(key), "UTF-8") + "&");
            }
            url = new URL(urlPath+'?'+request.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
            conn.setRequestProperty("accept","application/json");
            if (conn.getResponseCode() == 200)
            {
                is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                int len = -1;
                byte[] buf = new byte[128];

                while ((len = is.read(buf)) != -1)
                {
                    baos.write(buf, 0, len);
                }
                baos.flush();
                return baos.toString();
            } else
            {
                throw new RuntimeException(" responseCode is not 200 ... ");
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (is != null)
                    is.close();
            } catch (IOException e)
            {
            }
            try
            {
                if (baos != null)
                    baos.close();
            } catch (IOException e)
            {
            }
            conn.disconnect();
        }

        return null ;
    }
}
