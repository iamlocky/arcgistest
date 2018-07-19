package cn.lockyluo.arcgis_test.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LockyLuo on 2018/7/19.
 */

public class UrlUtils {



            /**
             * 使用post方式与服务器通讯
             * @param content
             * @return
             */
            public static String sendPostRequest(String urls,String content){
                HttpURLConnection conn=null;
                try {
                    String Strurl=urls;
                    URL url = new URL(Strurl);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("ser-Agent", "Fiddler");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(5 * 1000);
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(URLEncoder.encode(content.toString(), "UTF-8").getBytes());
                    outputStream.flush();
                    outputStream.close();
                    if(HttpURLConnection.HTTP_OK==conn.getResponseCode()){
                        Log.i("PostGetUtil","post请求成功");
                        InputStream in=conn.getInputStream();
                        String backcontent=streamToString(in);
                        backcontent= URLDecoder.decode(backcontent,"UTF-8");
                        Log.i("PostGetUtil",backcontent);
                        in.close();
                    }else {
                        Log.i("PostGetUtil","post请求失败");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    conn.disconnect();
                }
                return null;
            }

            /**
             * 使用get方式与服务器通信
             * @return
             */
            public static String sendGetRequest(String urlstr){
                String backcontent=null;
                HttpURLConnection conn=null;
                try {
                    URL url = new URL(urlstr);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setRequestMethod("GET");
                    if(HttpURLConnection.HTTP_OK==conn.getResponseCode()){
                        Log.i("PostGetUtil","get请求成功");
                        InputStream in=conn.getInputStream();
                        backcontent=streamToString(in);
                        backcontent= URLDecoder.decode(backcontent,"UTF-8");
                        Log.i("PostGetUtil",backcontent);
                        in.close();
                    }
                    else {
                        Log.i("PostGetUtil","get请求失败");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally{
                    conn.disconnect();
                }
                return backcontent;
            }


    public static String streamToString(InputStream is)
    {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"));

            StringBuilder sb = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 把unicode编码转换为中文
     *
     * @param str
     * @return
     */
    public static String decode(String str) {
        String sg = "\\u";
        int a = 0;
        List<String> list = new ArrayList<>();
        while (str.contains(sg)) {
            str = str.substring(2);
            String substring;
            if (str.contains(sg)) {
                substring = str.substring(0, str.indexOf(sg));
            } else {
                substring = str;
            }
            if (str.contains(sg)) {
                str = str.substring(str.indexOf(sg));
            }
            list.add(substring);
        }
        StringBuffer sb = new StringBuffer();
        if (list.size()>0){
            for (String string : list) {
                sb.append((char) Integer.parseInt(string, 16));
            }
        }
        return sb.toString();
    }

    /**
     * 解码 Unicode \\uXXXX
     * @param str
     * @return
     */
    public static String decodeUnicode(String str) {
        Charset set = Charset.forName("UTF-16");
        Pattern p = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
        Matcher m = p.matcher( str );
        int start = 0 ;
        int start2 = 0 ;
        StringBuffer sb = new StringBuffer();
        while( m.find( start ) ) {
            start2 = m.start() ;
            if( start2 > start ){
                String seg = str.substring(start, start2) ;
                sb.append( seg );
            }
            String code = m.group( 1 );
            int i = Integer.valueOf( code , 16 );
            byte[] bb = new byte[ 4 ] ;
            bb[ 0 ] = (byte) ((i >> 8) & 0xFF );
            bb[ 1 ] = (byte) ( i & 0xFF ) ;
            ByteBuffer b = ByteBuffer.wrap(bb);
            sb.append( String.valueOf( set.decode(b) ).trim() );
            start = m.end() ;
        }
        start2 = str.length() ;
        if( start2 > start ){
            String seg = str.substring(start, start2) ;
            sb.append( seg );
        }
        return sb.toString() ;
    }

}
