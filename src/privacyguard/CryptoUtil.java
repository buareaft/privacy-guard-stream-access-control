package privacyguard;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CryptoUtil {
    private static final String KEY = "PrivacyGuard";

    private CryptoUtil() {
    }

    public static String encrypt(String plainText) {
        byte[] input = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] key = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] output = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = (byte) (input[i] ^ key[i % key.length]);
        }
        return Base64.getEncoder().encodeToString(output);
    }
}
