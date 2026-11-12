package com.reader.parse;

import com.reader.common.BizException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HttpFetcher {

    private static final String UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private HttpClient client;

    @Value("${app.fetch.timeout-seconds:10}")
    private int timeoutSeconds;

    @Value("${app.fetch.trust-all-certs:false}")
    private boolean trustAllCerts;

    @PostConstruct
    public void init() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL);
        if (trustAllCerts) {
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }}, new SecureRandom());
                builder.sslContext(ctx);
            } catch (Exception ignored) {
            }
        }
        client = builder.build();
    }

    public String get(String url, String charset) {
        return get(url, charset, Map.of());
    }

    public String get(String url, String charset, Map<String, String> headers) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        applyHeaders(b, headers);
        return send(b.GET().build(), charset);
    }

    public String post(String url, String body, String charset) {
        return post(url, body, charset, Map.of());
    }

    public String post(String url, String body, String charset, Map<String, String> headers) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        applyHeaders(b, headers);
        return send(b.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body)).build(), charset);
    }

    private void applyHeaders(HttpRequest.Builder b, Map<String, String> headers) {
        Map<String, String> h = new HashMap<>();
        if (headers != null) {
            h.putAll(headers);
        }
        h.putIfAbsent("User-Agent", UA);
        h.forEach(b::header);
    }

    private String send(HttpRequest req, String charset) {
        try {
            HttpResponse<byte[]> resp = client.send(req, HttpResponse.BodyHandlers.ofByteArray());
            if (resp.statusCode() >= 400) {
                throw new BizException("抓取失败：HTTP " + resp.statusCode());
            }
            String contentType = resp.headers().firstValue("Content-Type").orElse("");
            String cs = resolveCharset(charset, contentType);
            return new String(resp.body(), Charset.forName(cs));
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("抓取失败：" + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    private String resolveCharset(String configured, String contentType) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        Matcher m = Pattern.compile("charset\\s*=\\s*([\\w-]+)").matcher(contentType);
        if (m.find()) {
            return m.group(1);
        }
        return "UTF-8";
    }
}
