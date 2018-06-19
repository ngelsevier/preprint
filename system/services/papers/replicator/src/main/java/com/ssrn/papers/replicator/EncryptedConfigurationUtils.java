package com.ssrn.papers.replicator;

import static com.ssrn.shared.kms.KmsUtils.usingKmsDecrypt;

public class EncryptedConfigurationUtils {
    public static String getDecryptedPassword(String defaultPassword, String environmentKey) {
        return Boolean.parseBoolean(System.getenv("SIMULATED_ENVIRONMENT")) ?
                defaultPassword :
                usingKmsDecrypt(System.getenv(environmentKey));
    }
}
