package ru.itone.illya4gurenko.service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import ru.itone.illya4gurenko.config.Base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApacheMultipartSenderService extends Base implements FileSender {

    private static final ApacheMultipartSenderService INSTANCE = new ApacheMultipartSenderService();

    private final CloseableHttpClient httpClient;

    private ApacheMultipartSenderService() {
        this.httpClient = HttpClients.createDefault();
    }

    public static ApacheMultipartSenderService getInstance() {
        return INSTANCE;
    }

    @Override
    public void sendFile(Path filePath, String targetUrl) throws IOException {
        String fileName = filePath.getFileName().toString();
        File file = filePath.toFile();

        long fileSize = file.length();
        info("starting Apache multipart transfer for: {}, file size: {} bytes", fileName, fileSize);

        if (fileSize == 0) {
            warn("file {} size 0 ", fileName);
        }

        if (!file.exists()) {
            error("file not found: {}", filePath);
            throw new IOException("file not found: " + filePath);
        }

        HttpPost uploadFile = new HttpPost(targetUrl);

        ContentType fileContentType = ContentType.create("text/plain", config.getFileCharset());

        HttpEntity multipart = MultipartEntityBuilder.create()

                .addTextBody("title", "Bank Client Registry", ContentType.TEXT_PLAIN)
                .addBinaryBody("file", file, fileContentType, fileName)
                .build();

        uploadFile.setEntity(multipart);
        debug("Sending request to {}", targetUrl);

        try (CloseableHttpResponse response = httpClient.execute(uploadFile)) {
            int statusCode = response.getCode();

            String responseString = "";
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                responseString = EntityUtils.toString(responseEntity);
            }

            if (statusCode >= 200 && statusCode < 300) {
                info("file {} successfully transmitted. Code: {}. Response: {}",
                        fileName, statusCode, responseString);
            } else {
                error("failed to send file {}. Code: {}, Response: {}",
                        fileName, statusCode, responseString);
                throw new IOException("remote server returned HTTP status: " + statusCode);
            }
        } catch (Exception e) {
            error("error during Apache HTTP transfer for file: {}", fileName, e);
            throw new IOException("multipart transfer error", e);
        }
    }
}