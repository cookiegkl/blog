package com.mtons.mblog.base.utils;

import com.alibaba.fastjson.JSON;
import com.mtons.mblog.config.SmmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 图床上传工具类
 *
 * @author Alex
 * @date 2019/12/31
 */
public class SmmsUtil {

    private SmmsUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SmmsUtil.class);

    /**
     * 执行上传操作
     *
     * @param file 文件
     * @return 上传结果
     */
    public static SmmsResult executeImport(File file) {
        String urlStr = "https://sm.ms/api/v2/upload";
        Map<String, String> textMap = new HashMap<>(16);
        Map<String, File> fileMap = new HashMap<>(16);
        fileMap.put("smfile", file);
        String result = formUpload(urlStr, textMap, fileMap);
        LOGGER.info("上传文件返回：[{}]", JSON.toJSON(result));
        SmmsResult smmsResult = JSON.parseObject(result, SmmsResult.class);
        return smmsResult;
    }

    /**
     * 上传图片
     *
     * @param urlStr  服务器地址
     * @param textMap 表单数据
     * @param fileMap 文件
     * @return
     */
    public static String formUpload(String urlStr, Map<String, String> textMap, Map<String, File> fileMap) {
        String res = "";
        HttpURLConnection conn = null;
        //boundary就是request头和上传文件内容的分隔符，我模拟的浏览器中为35位
        String BOUNDARY = "---------------------------" + createRandCode();
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            // text
            if (textMap != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator iter = textMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String inputName = (String) entry.getKey();
                    String inputValue = (String) entry.getValue();
                    if (inputValue == null) {
                        continue;
                    }
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
                    strBuf.append(inputValue);
                }
                out.write(strBuf.toString().getBytes());
            }
            // file
            if (fileMap != null) {
                fileMap.forEach((k, v) -> {
                    File file = v;
                    //根据map集合中的file值(即文件地址)构造File对象
//                    File file = new File(inputValue);
                    String filename = file.getName();
                    String contentType = new MimetypesFileTypeMap().getContentType(file);
                    //对文件后缀进行判断衔接
                    if (filename.endsWith(".png")) {
                        contentType = "image/png";
                    } else if (filename.endsWith(".jpg")) {
                        contentType = "image/jpg";
                    } else if (filename.endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (filename.endsWith(".gif")) {
                        contentType = "image/gif";
                    }
                    //判断内容是否为空
                    if (contentType == null || contentType.equals("")) {
                        contentType = "application/octet-stream";
                    }
                    StringBuffer strBuf = new StringBuffer();
                    strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
                    strBuf.append("Content-Disposition: form-data; name=\"" + k + "\"; filename=\"" + filename + "\"\r\n");
                    strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
                    try {
                        out.write(strBuf.toString().getBytes());
                        DataInputStream in;
                        in = new DataInputStream(new FileInputStream(file));
                        int bytes;
                        byte[] bufferOut = new byte[8192];
                        while ((bytes = in.read(bufferOut)) != -1) {
                            out.write(bufferOut, 0, bytes);
                        }
                        in.close();
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();
            // 读取返回数据
            StringBuffer strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return res;
    }

    /**
     * 生成随机数
     *
     * @return 随机数
     */
    public static String createRandCode() {
        char[] chars = new char[35];
        short start = '0';
        //如果此处没有加法，那么一个随机数取到1才会获得字符‘z’。所以此处+1即可，理论封顶是加到14，超过14就会取得其余字符，但我们通过了方法判断字符，不用担心！
        short end = 'z' + 14;
        for (int i = 0; i < chars.length; i++) {
            while (true) {
                char orderNum = (char) (Math.random() * (end - start) + start);
                if (Character.isLetter(orderNum) || Character.isDigit(orderNum)) {
                    chars[i] = orderNum;
                    break;
                }
            }
        }
        String str = new String(chars);
        return str;
    }
}
