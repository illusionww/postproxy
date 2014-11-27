package com.postproxy;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private final static String url = "";

    public static void main(String[] args) throws Exception {
        File inFile = new File("С:\\proxy.txt");
        List<String> list = FileUtils.readLines(inFile);
        for (String item : list) {
            Pattern pattern = Pattern.compile("([^ ]+):([^ ]+)\t(.+)");
            Matcher m = pattern.matcher(item);
            if (m.matches()) {
                String url = m.group(1);
                int port = Integer.parseInt(m.group(2));
                String useragent = m.group(3);
                System.out.println(url + ":" + port + " useragent " + useragent);

                boolean result = false;
                try {
                    result = run(url, port, useragent);
                } catch(IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Result: " + result);
                if (result) {
                    System.out.println("Waiting... " + new Date());
                    Thread.sleep(1000 * 60 * 3);
                }
                System.out.println("----------------------------------------");
            }
        }
    }

    public static boolean run(String proxyURL, int proxyPort, String useragent) throws IOException {
        HttpHost proxy = new HttpHost(proxyURL, proxyPort, "http");
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        String result = null;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpRequest = new HttpPost(url);
            httpRequest.setHeader("User-Agent", useragent);
            httpRequest.setConfig(config);

            HttpEntity reqEntity = MultipartEntityBuilder.create()
                    .addPart("id", new StringBody("7", Charset.defaultCharset()))
                    .build();

            httpRequest.setEntity(reqEntity);

            System.out.println("executing request " + httpRequest.getRequestLine());
            try (CloseableHttpResponse response = httpClient.execute(httpRequest)) {
                System.out.println(response.getStatusLine());
                HttpEntity resEntity = response.getEntity();
                if (resEntity != null) {
                    System.out.println("Response content length: " + resEntity.getContentLength());
                    result = EntityUtils.toString(resEntity).trim();
                    System.out.println(result);
                }
                EntityUtils.consume(resEntity);
            }
        }

        return result != null && result.equals("\uFEFF{\"success\":true}");
    }
}
