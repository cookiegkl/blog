/*
+--------------------------------------------------------------------------
|   WeCMS [#RELEASE_VERSION#]
|   ========================================
|   Copyright (c) 2014, 2015 mtons. All Rights Reserved
|   http://www.mtons.com
|
+---------------------------------------------------------------------------
*/
package com.mtons.mblog.web.controller.site.posts;

import com.alibaba.fastjson.JSON;
import com.mtons.mblog.base.lang.Consts;
import com.mtons.mblog.base.lang.Result;
import com.mtons.mblog.base.utils.FileKit;
import com.mtons.mblog.base.utils.FileUtils;
import com.mtons.mblog.config.SmmsResult;
import com.mtons.mblog.modules.entity.BlogUpload;
import com.mtons.mblog.modules.service.BlogUploadService;
import com.mtons.mblog.modules.service.IViewService;
import com.mtons.mblog.web.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Ueditor 文件上传
 *
 * @author langhsu
 */
@Controller
@RequestMapping("/post")
public class UploadController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadController.class);

    @Autowired
    private BlogUploadService uploadService;

    public static HashMap<String, String> errorInfo = new HashMap<>();

    static {
        errorInfo.put("SUCCESS", "SUCCESS");
        errorInfo.put("NOFILE", "未包含文件上传域");
        errorInfo.put("TYPE", "不允许的文件格式");
        errorInfo.put("SIZE", "文件大小超出限制，最大支持2Mb");
        errorInfo.put("ENTYPE", "请求类型ENTYPE错误");
        errorInfo.put("REQUEST", "上传请求异常");
        errorInfo.put("IO", "IO异常");
        errorInfo.put("DIR", "目录创建失败");
        errorInfo.put("UNKNOWN", "未知错误");
    }

    @PostMapping("/upload")
    @ResponseBody
    public UploadResult upload(@RequestParam(value = "file", required = false) MultipartFile multipartFile, HttpServletRequest request) {
        UploadResult result = new UploadResult();
        String crop = request.getParameter("crop");
        int size = ServletRequestUtils.getIntParameter(request, "size", siteOptions.getIntegerValue(Consts.STORAGE_MAX_WIDTH));
        // 检查空
        if (null == multipartFile || multipartFile.isEmpty()) {
            return result.error(errorInfo.get("NOFILE"));
        }
        String fileName = multipartFile.getOriginalFilename();
        // 检查类型
        if (!FileKit.checkFileType(fileName)) {
            return result.error(errorInfo.get("TYPE"));
        }
        // 检查大小
        String limitSize = siteOptions.getValue(Consts.STORAGE_LIMIT_SIZE);
        if (StringUtils.isBlank(limitSize)) {
            limitSize = "2";
        }
        if (multipartFile.getSize() > (Long.parseLong(limitSize) * 1024 * 1024)) {
            return result.error(errorInfo.get("SIZE"));
        }
        // 保存图片
        try {
            File file = FileUtils.multipartFileToFile(multipartFile);
            SmmsResult smmsResult = executeImport(file);
            SmmsResult.Data data = smmsResult.getData();
            result.ok(errorInfo.get("SUCCESS"));
            result.setName(data.getFilename());
            result.setPath(data.getUrl());
            result.setSize(data.getSize());
            BlogUpload upload = new BlogUpload();
            upload.setFileId(data.getFile_id());
            upload.setFileName(data.getFilename());
            upload.setWidth(data.getWidth());
            upload.setHeight(data.getHeight());
            upload.setStoreName(data.getStorename());
            upload.setSize(data.getSize());
            upload.setPath(data.getPath());
            upload.setHash(data.getHash());
            upload.setUrl(data.getUrl());
            upload.setDeletePath(data.getDelete());
            upload.setPage(data.getPage());
            upload.setRequestId(smmsResult.getRequestId());
            upload.setCreateTime(new Date());
            upload.setLastUpdateTime(new Date());
            upload.setIsDelete(false);
            uploadService.addSmms(upload);
        } catch (Exception e) {
            result.error(errorInfo.get("UNKNOWN"));
            e.printStackTrace();
        }
        return result;
    }

    @PostMapping("/uploadXXX")
    @ResponseBody
    public UploadResult uploadBack(@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
        UploadResult result = new UploadResult();
        String crop = request.getParameter("crop");
        int size = ServletRequestUtils.getIntParameter(request, "size", siteOptions.getIntegerValue(Consts.STORAGE_MAX_WIDTH));
        // 检查空
        if (null == file || file.isEmpty()) {
            return result.error(errorInfo.get("NOFILE"));
        }
        String fileName = file.getOriginalFilename();
        // 检查类型
        if (!FileKit.checkFileType(fileName)) {
            return result.error(errorInfo.get("TYPE"));
        }
        // 检查大小
        String limitSize = siteOptions.getValue(Consts.STORAGE_LIMIT_SIZE);
        if (StringUtils.isBlank(limitSize)) {
            limitSize = "2";
        }
        if (file.getSize() > (Long.parseLong(limitSize) * 1024 * 1024)) {
            return result.error(errorInfo.get("SIZE"));
        }
        // 保存图片
        try {
            String path;
            if (StringUtils.isNotBlank(crop)) {
                Integer[] imageSize = siteOptions.getIntegerArrayValue(crop, Consts.SEPARATOR_X);
                int width = ServletRequestUtils.getIntParameter(request, "width", imageSize[0]);
                int height = ServletRequestUtils.getIntParameter(request, "height", imageSize[1]);
                path = storageFactory.get().storeScale(file, Consts.thumbnailPath, width, height);
            } else {
                path = storageFactory.get().storeScale(file, Consts.thumbnailPath, size);
            }
            result.ok(errorInfo.get("SUCCESS"));
            result.setName(fileName);
            result.setPath(path);
            result.setSize(file.getSize());
        } catch (Exception e) {
            result.error(errorInfo.get("UNKNOWN"));
            e.printStackTrace();
        }
        return result;
    }


    @PostMapping("/uploadToSmMs")
    @ResponseBody
    public Result uploadToSmMs(@RequestParam("file") MultipartFile multipartFile) {
        try {
            if (null == multipartFile || multipartFile.isEmpty()) {
                return Result.failure("文件不能为空");
            }
            File file = FileUtils.multipartFileToFile(multipartFile);
            executeImport(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.success();
    }

    public SmmsResult executeImport(File file) {
        String urlStr = "https://sm.ms/api/v2/upload";
        Map<String, String> textMap = new HashMap<>(16);
//        textMap.put("key", "这里可以用来存放请求表单所需的数据");
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
     * @param urlStr
     * @param textMap
     * @param fileMap
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

    public static void inputstreamToFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        ins.close();
    }

    public static File asFile(InputStream inputStream, String tempPath) throws IOException {
        File tmp = File.createTempFile("biuaxia", ".jpg", new File(tempPath));
        OutputStream os = new FileOutputStream(tmp);
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return tmp;
    }


    public void upload(File file) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost("https://sm.ms/api/v2/upload");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            HttpEntity reqEntity = builder.addBinaryBody("smfile", file).build();
            httppost.setEntity(reqEntity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            System.out.println(JSON.toJSON(response));
            try {
                HttpEntity resEntity = response.getEntity();
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Object upload(MultipartFile file) {
        String url = "https://sm.ms/api/v2/upload";
        String result = "";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        Header header = new BasicHeader("Content-Type", "multipart/form-data");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        try {
            builder.setCharset(Charset.forName("UTF-8")).addBinaryBody("smfile", file.getInputStream(), ContentType.MULTIPART_FORM_DATA, file.getOriginalFilename());
            HttpEntity httpEntity = builder.build();
            httpPost.setHeader(header);
            httpPost.setEntity(httpEntity);
            HttpResponse httpResponse = client.execute(httpPost);
            System.out.println(JSON.toJSON(httpResponse));
            HttpEntity responseEntity = httpResponse.getEntity();
            if (responseEntity != null) {
                result = EntityUtils.toString(responseEntity, Charset.forName("UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void upload2(File file) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse httpResponse;
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000000).build();
        HttpPost httpPost = new HttpPost("https://sm.ms/api/v2/upload");
        httpPost.setConfig(requestConfig);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("smfile", file);
        multipartEntityBuilder.addTextBody("format", "json");
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httpPost.setEntity(httpEntity);
        httpResponse = httpClient.execute(httpPost);
        System.out.println(JSON.toJSON(httpResponse));
        httpClient.close();
        if (httpResponse != null) {
            httpResponse.close();
        }
    }

    public static void uploadByHttpClient(File file) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpPost httppost = new HttpPost("https://sm.ms/api/v2/upload");
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(200000).setSocketTimeout(200000).build();
            httppost.setConfig(requestConfig);
            FileBody bin = new FileBody(file);
            StringBody comment = new StringBody("json", ContentType.TEXT_PLAIN);
            HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("smfile", bin).addPart("format", comment).build();
            httppost.setEntity(reqEntity);
            System.out.println("executing request " + httppost.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                System.out.println(JSON.toJSON(response));
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    String responseEntityStr = EntityUtils.toString(response.getEntity());
                    System.out.println(responseEntityStr);
                    System.out.println("Response content length: " + resEntity.getContentLength());
                }
                EntityUtils.consume(resEntity);
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String post() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        CloseableHttpResponse httpResponse;
        HttpPost httpPost = new HttpPost("https://sm.ms/api/v2/upload");
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(20000).setSocketTimeout(22000).build();
        httpPost.setConfig(requestConfig);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("Accept", "*/*"));
        params.add(new BasicNameValuePair("Content-Type", "multipart/form-data"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, Charset.forName("UTF-8"));
        httpPost.setEntity(entity);
        httpResponse = httpClient.execute(httpPost);
        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null) {
            String content = EntityUtils.toString(responseEntity, "UTF-8");
            System.out.println(content);
        }
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
        return null;
    }

    public static class UploadResult {
        public static int OK = 200;
        public static int ERROR = 400;

        /**
         * 上传状态
         */
        private int status;

        /**
         * 提示文字
         */
        private String message;

        /**
         * 文件名
         */
        private String name;

        /**
         * 文件大小
         */
        private long size;

        /**
         * 文件存放路径
         */
        private String path;

        public UploadResult ok(String message) {
            this.status = OK;
            this.message = message;
            return this;
        }

        public UploadResult error(String message) {
            this.status = ERROR;
            this.message = message;
            return this;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
