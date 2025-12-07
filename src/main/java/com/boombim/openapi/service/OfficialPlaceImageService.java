package com.boombim.openapi.service;

import com.boombim.common.properties.S3Properties;
import com.boombim.place.repository.OfficialPlaceImageDao;
import com.boombim.place.repository.OfficialPlaceImageDao.BasicRow;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficialPlaceImageService {

    private static final String PREFIX = "official-places";
    private static final String EXT = "jpg";

    private static final int CONNECT_TIMEOUT_MS = 10_000;
    private static final int READ_TIMEOUT_MS = 30_000;

    private final S3Client s3;
    private final S3Properties s3Props;
    private final OfficialPlaceImageDao imageDao;

    public void syncAll() {
        var rows = imageDao.findAllWithoutImageUrl();
        log.info("[ImageSync] start targets={}", rows.size());

        for (BasicRow row : rows) {
            String key = keyFor(row.poiCode());
            String url = publicUrl(key);

            try {
                if (s3Exists(key)) {
                    imageDao.updateImageUrlById(row.id(), url);
                    log.info("[ImageSync] S3 HIT → url set id={} poi={} key={}", row.id(), row.poiCode(), key);
                    continue;
                }

                byte[] bytes = downloadHoldOnce(row.name());
                if (bytes != null && bytes.length > 0) {
                    putToS3(key, bytes, "image/jpeg");
                    imageDao.updateImageUrlById(row.id(), url);
                    log.info("[ImageSync] uploaded id={} poi={} size={}B key={}", row.id(), row.poiCode(), bytes.length, key);
                } else {
                    log.warn("[ImageSync] origin FAIL id={} name={}", row.id(), row.name());
                }

            } catch (Exception e) {
                log.error("[ImageSync] error id={} poi={} name={} err={}", row.id(), row.poiCode(), row.name(), e.toString());
            }
        }

        log.info("[ImageSync] done");
    }

    private String keyFor(String poiCode) {
        return PREFIX + "/" + poiCode + "." + EXT;
    }

    private boolean s3Exists(String key) {
        try {
            s3.headObject(
                HeadObjectRequest.builder()
                    .bucket(s3Props.bucketName())
                    .key(key)
                    .build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void putToS3(String key, byte[] bytes, String contentType) {
        s3.putObject(
            PutObjectRequest.builder()
                .bucket(s3Props.bucketName())
                .key(key)
                .contentType(contentType)
                .cacheControl("public, max-age=31536000, immutable")
                .build(),
            RequestBody.fromBytes(bytes)
        );
    }

    private String publicUrl(String key) {
        String base = s3Props.baseUrl();
        if (StringUtils.hasText(base)) {
            return base.endsWith("/") ? base + key : base + "/" + key;
        }
        return "https://" + s3Props.bucketName() + ".s3." + s3Props.region() + ".amazonaws.com/" + key;
    }

    private byte[] downloadHoldOnce(String areaNm) {
        String enc = UriUtils.encodePathSegment(areaNm, StandardCharsets.UTF_8);
        String url = "https://data.seoul.go.kr/SeoulRtd/images/hotspot/" + enc + ".jpg";
        log.info("[ImageFetch] ▶ {}", url);

        HttpURLConnection conn = null;
        try {
            URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Safari");
            conn.setRequestProperty("Accept", "image/*,*/*;q=0.8");
            conn.setRequestProperty("Referer", "https://data.seoul.go.kr/");

            long start = System.currentTimeMillis();
            conn.connect();

            int code = conn.getResponseCode();
            String ctype = conn.getContentType();
            log.info("[ImageFetch] ◀ status={} content-type={}", code, ctype);

            if (code / 100 != 2) {
                try (InputStream es = conn.getErrorStream()) {
                    if (es != null) {
                        es.readAllBytes();
                    }
                }
                return null;
            }

            try (InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream(32 * 1024)) {

                byte[] buf = new byte[64 * 1024];
                int n, total = 0;
                while ((n = is.read(buf)) != -1) {
                    baos.write(buf, 0, n);
                    total += n;
                    if (total % (1024 * 1024) < n) {
                        log.info("[ImageFetch] ...received ~{} KB", total / 1024);
                    }
                }
                byte[] out = baos.toByteArray();
                long elapsed = System.currentTimeMillis() - start;
                log.info("[ImageFetch] ✅ bytes={} elapsed={}ms", out.length, elapsed);
                return out;
            }

        } catch (Exception e) {
            log.warn("[ImageFetch] error url={} err={}", url, e.toString());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
