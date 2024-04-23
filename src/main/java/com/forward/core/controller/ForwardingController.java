package com.forward.core.controller;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpHeaders;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.conn.ssl.NoopHostnameVerifier;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.entity.ContentType;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.ssl.SSLContextBuilder;
//import org.apache.http.util.EntityUtils;

/**
 * 做转发Http请求的转发
 */
@Controller
@Slf4j
public class ForwardingController {
    @Autowired
    private RestTemplate restTemplate;

//    @RequestMapping(value = "/get", method = RequestMethod.GET)
//    public ResponseEntity<String> forwardRequest(@RequestParam String url) {
//
//        log.info("GET url:{} ", url);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Accept", "application/json");
//        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//        log.info("response ", JSON.toJSONString(response));
//        return response;
//    }
//
//    @RequestMapping(value = "/post", method = RequestMethod.POST)
//    public ResponseEntity<String> forwardPostRequest(@RequestParam String url, @RequestBody String requestBody) {
//        log.info("POST url:{},requestBody:{} ", url, requestBody);
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Type", "application/json");
//        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
//
//        log.info("response ", JSON.toJSONString(response));
//        return response;
//    }

//    @Resource
//    protected HsmSendClient hsmClient;
//
//    @RequestMapping(value = "/hsm", method = RequestMethod.POST)
//    public ResponseEntity<String> forwardPostRequest(@RequestBody String hexMsg) throws InterruptedException {
//        log.info("do byte post start ");
//        ResponseEntity responseEntity = new ResponseEntity(HsmUtils.hexToString(sendStringMsg(hexMsg.getBytes())), HttpStatus.OK);
//        return responseEntity;
//    }
//
//    @RequestMapping(value = "/hsm/bytes", method = RequestMethod.POST)
//    public ResponseEntity<String> forwardPostRequest(@RequestBody byte[] hexMsg) throws InterruptedException {
//        log.info("do byte post start ");
//        ResponseEntity responseEntity = new ResponseEntity(HsmUtils.hexToString(sendStringMsg(hexMsg)), HttpStatus.OK);
//        return responseEntity;
//    }

    /**
     * 发送byte消息
     *
     * @param data
     */
//    public String sendStringMsg(byte[] data) {
//        return HsmUtils.resolveResult(callHsmService(data));
//    }


//    private byte[] callHsmService(byte[] data) {
//        // 替换消息总长度
//        byte[] msg = HsmUtils.addHexLength(data, 2);
//        byte[] bytes;
//        try {
//            bytes = hsmClient.sendAndGet(msg);
//        } catch (Exception e) {
//            log.error("Call HSM Exception ! error:{}", e.getMessage(), e);
//            throw new RuntimeException("000000");
//        }
//        return bytes;
//    }

    String targetUrl = "https://oapgwuatkchy.ftcwifi.com/auth/oauth/v2/token/";
    String contentTypeStr = "application/json";
    String requestData = "{\"grant_type\": \"client_credentials\", \"client_id\": \"l76c0af75f7d3d41679688e68eaf8d2361\", \"client_secret\": \"1d309c1b91594eeb9de623a3c200b20f\"}";
    String proxyHost = "10.6.28.10";
    int proxyPort = 8080;

//    @GetMapping("/proxyUrl/{method}")
//    public ResponseEntity<String> proxyUrl(@PathVariable String method, @RequestParam String proxyHost, @RequestParam int proxyPort, @RequestParam String targetUrl, String contentType, @RequestParam String requestData, HttpServletRequest request) throws Exception {
//// 创建信任所有证书的 SSLContext
//        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
//            public void checkClientTrusted(X509Certificate[] chain, String authType) {
//            }
//
//            public void checkServerTrusted(X509Certificate[] chain, String authType) {
//            }
//
//            public X509Certificate[] getAcceptedIssuers() {
//                return new X509Certificate[0];
//            }
//        }};
//        SSLContext sslContext = SSLContext.getInstance("TLS");
//        sslContext.init(null, trustAllCerts, new SecureRandom());
//
//// 设置 SSLContext 到 HttpsURLConnection
//        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
//
//// 创建一个信任所有主机名的 HostnameVerifier
//        HostnameVerifier trustAllHostnames = (hostname, session) -> true;
//
//// 设置 HostnameVerifier 到 HttpsURLConnection
//        HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames);
//
//
//        // 设置代理
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
//
//        // 创建 URL 对象
//        URL url = new URL(targetUrl);
//
//        // 打开连接
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
//
//        // 设置请求方法为 POST
//        connection.setRequestMethod(method.toUpperCase());
//
//        // 设置请求头
//        if (null != request.getHeader("Authorization")) {
//            connection.setRequestProperty("Authorization", request.getHeader("Authorization"));
//        }
//        connection.setRequestProperty("Content-Type", StringUtil.isNullOrEmpty(contentType) ? contentTypeStr : contentType);
//        // 允许输出请求内容
//        connection.setDoOutput(true);
//
//        // 发送请求数据
//        try (OutputStream outputStream = connection.getOutputStream()) {
//            byte[] requestDataBytes = requestData.getBytes("UTF-8");
//            outputStream.write(requestDataBytes);
//            outputStream.flush();
//        }
//
//        // 获取响应内容
//        int responseCode = connection.getResponseCode();
//        StringBuilder response = new StringBuilder();
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            // 输出响应结果
//            log.info("Response Code: " + responseCode);
//            log.info("Response Body: " + response.toString());
//        } finally {
//            // 关闭连接
//            connection.disconnect();
//        }435550594a43
//        ResponseEntity responseEntity = new ResponseEntity(response.toString(), HttpStatus.OK);
//        return responseEntity;
//
//    }


    @PostMapping("/doArqcValid")
    public ResponseEntity<String> doArqcValid(@RequestBody String requestStr, @RequestParam String authorization) {
        try {
            return ResponseEntity.ok(bocArqcValid(requestStr, authorization));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/arqcToken")
    public ResponseEntity<String> getArqcToken(@RequestParam String clientId, @RequestParam String clientSecret) {
        try {
            return ResponseEntity.ok(getToken(clientId, clientSecret));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String bocArqcValid(String requestJsonStr, String authorization) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, IOException {
        // 创建SSL上下文，忽略SSL证书验证
        CloseableHttpClient httpClient = getCloseableHttpClient();

        // 创建HttpPost请求
        HttpPost httpPost = new HttpPost("https://oapgwuatkchy.ftcwifi.com/retrieve/arpc/v1.0/verify/arqc");

        // 设置请求头
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, authorization);

        // 设置请求体
        httpPost.setEntity(new StringEntity(requestJsonStr, ContentType.APPLICATION_JSON));

        // 发送请求并获取响应
        HttpResponse response = httpClient.execute(httpPost);

        // 解析响应
        int statusCode = response.getStatusLine().getStatusCode();
        org.apache.http.HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        // 打印响应结果
        log.info("Status Code:{} ", statusCode);
        log.info("Response Body: {}", responseBody);

        // 关闭HttpClient
        httpClient.close();

        if (200 == statusCode) {
            return responseBody;
        }
        throw new IllegalStateException("Unexpected status");
    }

    public String getToken(String clientId, String clientSecret) throws Exception {
        CloseableHttpClient httpClient = getCloseableHttpClient();

        // 创建HttpPost请求
        HttpPost httpPost = new HttpPost("https://oapgwuatkchy.ftcwifi.com/auth/oauth/v2/token");

        // 设置请求头
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");

        // 设置请求参数
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("grant_type", "client_credentials"));
        params.add(new BasicNameValuePair("client_id", clientId));
        params.add(new BasicNameValuePair("client_secret", clientSecret));
        httpPost.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));
        // 发送请求并获取响应
        HttpResponse response = httpClient.execute(httpPost);
        // 解析响应
        int statusCode = response.getStatusLine().getStatusCode();
        org.apache.http.HttpEntity entity = response.getEntity();
        String responseBody = EntityUtils.toString(entity);
        // 打印响应结果
        log.info("Status Code: " + statusCode);
        log.info("Response Body: " + responseBody);
        // 关闭HttpClient
        httpClient.close();
        if (200 == statusCode) {
            return responseBody;
        }
        throw new IllegalStateException("Unexpected status");
    }

    private CloseableHttpClient getCloseableHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        // 创建SSL上下文，忽略SSL证书验证
        SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial((chain, authType) -> true).build();

        // 创建SSL连接套接字工厂
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                NoopHostnameVerifier.INSTANCE);

        // 创建HttpClient实例，并配置代理服务器和SSL连接套接字工厂
        return HttpClients.custom().setSSLSocketFactory(sslSocketFactory)
                .setProxy(new HttpHost("10.6.28.10", 8080))
                .setDefaultRequestConfig(RequestConfig.custom().setProxy(new HttpHost("10.6.28.10", 8080)).build())
                .build();
    }

}
