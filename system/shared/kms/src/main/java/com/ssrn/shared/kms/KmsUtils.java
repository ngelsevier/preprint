package com.ssrn.shared.kms;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;

import java.nio.ByteBuffer;
import java.util.Base64;

public class KmsUtils {
    public static String usingKmsDecrypt(String encryptedText) {
        AWSKMS kmsClient = AWSKMSClientBuilder.defaultClient();
        ByteBuffer ciphertextBlob = ByteBuffer.wrap(Base64.getDecoder().decode(encryptedText));
        DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(ciphertextBlob);
        DecryptResult decryptResult = kmsClient.decrypt(decryptRequest);

        return new String(decryptResult.getPlaintext().array());
    }
}
