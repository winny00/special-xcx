package org.dromara.common.oss.util;

import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.oss.constant.OssConstant;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

/**
 * 公有云 OSS 识别与上传失败文案。
 */
public final class OssCompat {

    private OssCompat() {
    }

    /**
     * config_key 或 endpoint 命中阿里云/腾讯云等即为公有云（非 MinIO）。
     */
    public static boolean isCloudService(String configKey, String endpoint) {
        return containsCloudToken(configKey) || containsCloudToken(endpoint);
    }

    /**
     * 将 SDK/存储异常转成可展示给管理员的上传失败原因。
     */
    public static String uploadFailMessage(Throwable error) {
        String detail = rootMessage(error);
        if (StringUtils.isBlank(detail)) {
            return "OSS 上传失败，请检查对象存储配置与 AccessKey";
        }
        String lower = detail.toLowerCase();
        if (lower.contains("invalidaccesskey") || lower.contains("signaturedoesnotmatch")) {
            return "OSS 上传失败：AccessKey 无效或签名不匹配，请在 RAM 确认密钥并更新数据库配置";
        }
        if (lower.contains("accessdenied") || lower.contains("access denied")) {
            return "OSS 上传失败：AccessKey 没有该存储桶的写入权限";
        }
        if (lower.contains("nosuchbucket")) {
            return "OSS 上传失败：找不到存储桶，请核对 bucket 名称与 endpoint 地域";
        }
        if (lower.contains("chunked")) {
            return "OSS 上传失败：当前上传方式与阿里云 OSS 不兼容（chunked encoding）";
        }
        return "OSS 上传失败：" + StringUtils.substring(detail, 0, 200);
    }

    private static boolean containsCloudToken(String value) {
        return StringUtils.isNotBlank(value) && StringUtils.containsAny(value, OssConstant.CLOUD_SERVICE);
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        String last = null;
        while (current != null) {
            if (current instanceof AwsServiceException aws && aws.awsErrorDetails() != null
                && StringUtils.isNotBlank(aws.awsErrorDetails().errorMessage())) {
                String code = aws.awsErrorDetails().errorCode();
                String message = aws.awsErrorDetails().errorMessage();
                return StringUtils.isBlank(code) ? message : code + ": " + message;
            }
            if (StringUtils.isNotBlank(current.getMessage())) {
                last = current.getMessage();
            }
            current = current.getCause();
        }
        return last;
    }
}
