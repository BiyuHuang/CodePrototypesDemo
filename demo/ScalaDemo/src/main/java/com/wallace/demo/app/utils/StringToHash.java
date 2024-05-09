package com.wallace.demo.app.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.lang.Long;

/**
 * Author: biyu.huang
 * Date: 2024/5/9 11:50
 * Description:
 */
public class StringToHash {
  private static final Logger log = LoggerFactory.getLogger(StringToHash.class);

  public static void main(String[] args) throws Exception {
    String inputStr = "test";

    // 加密
    String kycInfoSecureKey = "o69BYlB9umqDAT3sizrC1Q==";

    byte[] encrypted = aesCbcEncryptPkcs7Iv(kycInfoSecureKey.getBytes(StandardCharsets.UTF_8),
        inputStr.getBytes(StandardCharsets.UTF_8));

    // 哈希计算
    String encryptedHex = bytesToHex(encrypted);
    long hashCode = getHashCode(encryptedHex);

    // 取模运算
    // long tableIdx = Long.parseLong(nameHash, 16) % 1000;

    log.info(String.format("input_str -> %s, hash_code -> %s", inputStr,
        Long.toUnsignedString(hashCode)));
  }

  private static final byte[] AES_CBC_IV = new byte[16];

  public static long getHashCode(String nameEncrypt) {
    return getHashCode(nameEncrypt.getBytes(StandardCharsets.UTF_8));
  }

  public static long getHashCode(byte[] bytes) {
    Hash64 hash64 = new Hash64();
    hash64.write(bytes);
    return hash64.getHashCode();
  }
  public static String aesCbcEncryptWithIv(byte[] key, byte[] plainText) throws Exception {
    SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(AES_CBC_IV));
    byte[] encrypted = cipher.doFinal(plainText, 0, plainText.length);
    byte[] result = new byte[plainText.length];
    System.arraycopy(encrypted, 0, result, 0, plainText.length);
    return bytesToHex(result);
  }

  public static byte[] pkcs7Padding(byte[] ciphertext, int blockSize) {
    int padding = blockSize - (ciphertext.length % blockSize);
    byte[] padText = new byte[padding];
    Arrays.fill(padText, (byte) padding);
    byte[] result = Arrays.copyOf(ciphertext, ciphertext.length + padding);
    System.arraycopy(padText, 0, result, ciphertext.length, padding);
    return result;
  }

  public static byte[] aesCbcEncryptPkcs7Iv(byte[] key, byte[] message) throws Exception {
    byte[] plainText = pkcs7Padding(message, 16);
    String encrypted = aesCbcEncryptWithIv(key, plainText);
    return hexToBytes(encrypted);
  }

  private static String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private static byte[] hexToBytes(String hex) {
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
          + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }


  private static class Hash64 {
    private long hashCode;

    public Hash64() {
      // based on fnv.offset64 in Golang(fnv.go), refer to https://en.wikipedia.org/wiki/Fowler-Noll-Vo_hash_function.
      this.hashCode = -3750763034362895579L;
    }

    public void write(byte[] data) {
      for (byte b : data) {
        hashCode ^= (long) b;
        hashCode *= 1099511628211L;
      }
    }

    public long getHashCode() {
      return this.hashCode;
    }
  }
}


