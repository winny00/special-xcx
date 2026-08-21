package org.dromara.common.oss.util;

import org.dromara.common.oss.exception.S3StorageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tags({@Tag("local"), @Tag("dev"), @Tag("prod")})
@DisplayName("OSS 云厂商识别与错误文案")
class OssCompatTest {

    @Test
    @DisplayName("config_key=aliyun 视为公有云")
    void aliyunConfigKeyIsCloud() {
        assertTrue(OssCompat.isCloudService("aliyun", "127.0.0.1:9000"));
    }

    @Test
    @DisplayName("阿里云 endpoint 视为公有云")
    void aliyunEndpointIsCloud() {
        assertTrue(OssCompat.isCloudService("minio", "oss-cn-guangzhou.aliyuncs.com"));
    }

    @Test
    @DisplayName("MinIO 本地 endpoint 不是公有云")
    void minioIsNotCloud() {
        assertFalse(OssCompat.isCloudService("minio", "127.0.0.1:9000"));
    }

    @Test
    @DisplayName("InvalidAccessKey 给出可操作提示")
    void invalidAccessKeyMessage() {
        RuntimeException cause = new RuntimeException("InvalidAccessKeyId: The Access Key Id you provided does not exist");
        String msg = OssCompat.uploadFailMessage(new S3StorageException(cause));
        assertTrue(msg.contains("AccessKey"));
        assertTrue(msg.startsWith("OSS 上传失败"));
    }

    @Test
    @DisplayName("PermanentRedirect 提示核对桶地域")
    void permanentRedirectMessage() {
        String msg = OssCompat.uploadFailMessage(new RuntimeException(
            "PermanentRedirect: The bucket you are attempting to access must be addressed using the specified endpoint."));
        assertTrue(msg.contains("地域"));
    }

    @Test
    @DisplayName("未知错误保留原始信息")
    void genericMessageKeepsDetail() {
        assertEquals(
            "OSS 上传失败：connection refused",
            OssCompat.uploadFailMessage(new RuntimeException("connection refused"))
        );
    }
}
