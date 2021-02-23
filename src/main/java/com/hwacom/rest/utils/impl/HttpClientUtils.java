package com.hwacom.rest.utils.impl;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;


/**
 * HTTP/HTTPS Request Utils
 */
public class HttpClientUtils {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);
    
    private static final int SUCCESS_CODE = 200;

    /**
     * HTTP request GET
     * @param url
     * @param nameValuePairList
     * @return String
     * @throws Exception
     */
    public static String sendGet(String url, List<NameValuePair> nameValuePairList) throws Exception{
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try{
            // step1.建立HttpClients物件
            client = HttpClients.createDefault();
            // step2.建立URIBuilder
            URIBuilder uriBuilder = new URIBuilder(url);
            // step3. 新增參數
            if (nameValuePairList != null) {
                uriBuilder.addParameters(nameValuePairList);
            }
            // step4. 建立HttpGet物件
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            // step5. 設定Header編碼格式
            httpGet.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
            // step6. 設定回傳值編碼格式
            httpGet.setHeader(new BasicHeader("Accept", "application/json; charset=utf-8"));
            // step7. 發送request
            response = client.execute(httpGet);
            // step8. 擷取狀態碼
            int statusCode = response.getStatusLine().getStatusCode();

            if (SUCCESS_CODE == statusCode){
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity,"UTF-8");
                return result;
            }else{
                logger.error("HttpClientService-statusCode: {}", statusCode);
            }
        }catch (Exception e){
            logger.error("HttpClientService-Exception: {}", e);
        } finally {
            response.close();
            client.close();
        }
        return null;
    }

    /**
     * HTTP request POST
     * @param url
     * @param nameValuePairList
     * @return String
     * @throws Exception
     */
    public static String sendPost(String url, List<NameValuePair> nameValuePairList) throws Exception{
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            client = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);

            StringEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
            post.setEntity(entity);
            post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
            post.setHeader(new BasicHeader("Accept", "text/plain;charset=utf-8"));

            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (SUCCESS_CODE == statusCode){
                String result = EntityUtils.toString(response.getEntity(),"UTF-8");
                return result;
            }else{
                logger.error("HttpClientService-statusCode：{}", statusCode);
            }
        }catch (Exception e){
            logger.error("HttpClientService-Exception：{}", e);
        }finally {
            response.close();
            client.close();
        }
        return null;
    }

    /**
     * request參數設置(參數跟值的順序index要一致)
     * @param params 參數名稱(key)
     * @param values 參數值(value)
     * @return 參數清單
     */
    public static List<NameValuePair> getParams(Object[] params, Object[] values){
        // step1. 驗證參數
        boolean flag = params.length>0 && values.length>0 && params.length == values.length;
        if (flag) {
            List<NameValuePair> nameValuePairList = new ArrayList<>();
            for(int i=0; i<params.length; i++) {
                nameValuePairList.add(new BasicNameValuePair(params[i].toString(),values[i].toString()));
            }
            return nameValuePairList;
        } else {
            logger.error("HttpClientService-errorMsg：{}", "傳入參數為空或參數與值數量不相符");
        }
        return null;
    }


    public static String sendHttpsGet(String url, List<NameValuePair> nameValuePairList) throws Exception{
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            PoolingHttpClientConnectionManager connManager = ConnectionManagerBuilder();
            client = HttpClients.custom().setConnectionManager(connManager).build();

            URIBuilder uriBuilder = new URIBuilder(url);
            if (nameValuePairList != null) {
                uriBuilder.addParameters(nameValuePairList);
            }
            HttpGet httpGet = new HttpGet(uriBuilder.build());
            httpGet.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
            httpGet.setHeader(new BasicHeader("Accept", "text/plain;charset=utf-8"));

            response = client.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();

            if (SUCCESS_CODE == statusCode){
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");
                return result;
            } else {
                logger.error("HttpClientService-errorCode: {}, errorMsg{}", statusCode, "HTTP request GET失敗");
            }
        }catch (Exception e){
            logger.error("HttpClientService-Exception: {}", e);
        } finally {
            response.close();
            client.close();
        }
        return null;
    }

    public static String sendHttpsPost(String url, List<NameValuePair> nameValuePairList) throws Exception{
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;

        try{
            PoolingHttpClientConnectionManager connManager = ConnectionManagerBuilder();
            client = HttpClients.custom().setConnectionManager(connManager).build();

            HttpPost post = new HttpPost(url);
            StringEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");
            post.setEntity(entity);
            post.setHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8"));
            post.setHeader(new BasicHeader("Accept", "text/plain;charset=utf-8"));

            response = client.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (SUCCESS_CODE == statusCode){
                String result = EntityUtils.toString(response.getEntity(),"UTF-8");
                return result;
            }else{
                logger.error("HttpClientService-statusCode：{}", statusCode);
            }
        }catch (Exception e){
            logger.error("HttpClientService-Exception：{}", e);
        }finally {
            response.close();
            client.close();
        }
        return null;
    }


    public static PoolingHttpClientConnectionManager ConnectionManagerBuilder() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

        // 複寫X509驗憑證方法，略過驗憑證程序(自簽憑證需要)
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[] { trustManager }, null);

        // 設定HTTP/HTTPS處理socketFactory
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslContext,new String[] { "TLSv1.2" },
                        null,
                        NoopHostnameVerifier.INSTANCE)) // 免驗hostname
                .build();
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        return connManager;
    }



    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

        // 複寫X509驗憑證方法，略過驗憑證程序(自簽憑證需要)
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sslContext.init(null, new TrustManager[] { trustManager }, null);
        return sslContext;
    }
}