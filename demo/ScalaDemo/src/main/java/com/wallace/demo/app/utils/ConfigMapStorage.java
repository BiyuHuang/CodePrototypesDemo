package com.wallace.demo.app.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: biyu.huang
 * Date: 2024/7/24 10:08
 * Description:
 */
public class ConfigMapStorage {
  private static final Logger logger = LoggerFactory.getLogger(ConfigMapStorage.class);
  private static final String ALGORITHM = "AES/GCM/PKCS5Padding";
  private static final int KEY_LENGTH = 32;

  // AES-GCM needs 96-bit(12 bytes) IV, refer to GaloisCounterMode.DEFAULT_IV_LEN
  private static final int IV_LENGTH = 12;
  private Key encryptionKey;
  private String SAVE_FILE;
  private Map<String, String> configMap;

  private Key generateKey() throws Exception {
    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
    keyGen.init(256);
    SecretKey secretKey = keyGen.generateKey();
    return new SecretKeySpec(secretKey.getEncoded(), "AES");
  }

  public ConfigMapStorage(@Nullable String savePath) {
    String envKey = System.getenv("ENCRYPTION_KEY");
    if (envKey == null) {
      throw new IllegalArgumentException("Environment variable ENCRYPTION_KEY not set");
    }
    byte[] keyBytes = Base64.getDecoder().decode(envKey);
    byte[] newKeyBytes = new byte[KEY_LENGTH];
    System.arraycopy(keyBytes, 0, newKeyBytes, 0, Math.min(keyBytes.length, KEY_LENGTH));
    this.encryptionKey = new SecretKeySpec(newKeyBytes, "AES");

    String SAVE_FILE_NAME = "/.kms_kv";
    if (savePath == null || savePath.trim().isEmpty()) {
      this.SAVE_FILE = "/tmp" + SAVE_FILE_NAME;
    } else {
      String fixedSavePath = savePath.trim();
      if (fixedSavePath.endsWith("/")) {
        this.SAVE_FILE = fixedSavePath.substring(0, fixedSavePath.length() - 1) + SAVE_FILE_NAME;
      } else {
        this.SAVE_FILE = fixedSavePath + SAVE_FILE_NAME;
      }
    }

    this.configMap = new HashMap<>();
    logger.info("SAVE_PATH -> " + this.SAVE_FILE);
  }

  public static ConfigMapStorage getInstance() {
    return new ConfigMapStorage(null);
  }

  public static ConfigMapStorage getInstance(String savePath) {
    return new ConfigMapStorage(savePath);
  }

  @SuppressWarnings("unchecked")
  private void loadConfigMap() {
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
      this.configMap = (Map<String, String>) ois.readObject();
    } catch (FileNotFoundException e) {
      logger.error(this.SAVE_FILE + " not found. A new one should be created.");
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void saveConfigMap() {
    try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(SAVE_FILE)))) {
      oos.writeObject(this.configMap);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addConfig(String key, String value) throws Exception {
    String encryptedValue = encrypt(value);
    logger.info("[ENCRYPTED] " + key + " ===> " + encryptedValue);
    configMap.put(key, encryptedValue);
    saveConfigMap();
  }

  public String getConfig(String key) throws Exception {
    String encryptedToken = this.configMap.get(key);
    return encryptedToken != null ? decrypt(encryptedToken) : null;
  }

  public int getSize() {
    return this.configMap.size();
  }

  private String encrypt(String data) throws Exception {
    byte[] dataBytes = data.getBytes();
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
    byte[] encryptedBytes = cipher.doFinal(dataBytes);
    byte[] ivBytes = cipher.getIV();
    byte[] encryptedData = new byte[IV_LENGTH + encryptedBytes.length];
    System.arraycopy(ivBytes, 0, encryptedData, 0, IV_LENGTH);
    System.arraycopy(encryptedBytes, 0, encryptedData, IV_LENGTH, encryptedBytes.length);
    return Base64.getEncoder().encodeToString(encryptedData);
  }

  private String decrypt(String encryptedData) throws Exception {
    byte[] cipherBytes = Base64.getDecoder().decode(encryptedData);
    byte[] ivBytes = new byte[IV_LENGTH];
    System.arraycopy(cipherBytes, 0, ivBytes, 0, IV_LENGTH);
    GCMParameterSpec gcmParamSpec = new GCMParameterSpec(128, ivBytes, 0, IV_LENGTH);
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, encryptionKey, gcmParamSpec);
    byte[] rawBytes = cipher.doFinal(cipherBytes, IV_LENGTH, cipherBytes.length - IV_LENGTH);
    return new String(rawBytes);
  }

  public static void main(String[] args) throws Exception {
    ConfigMapStorage configMapStorage = ConfigMapStorage.getInstance();
    String dummyToken1 = Base64.getEncoder().encodeToString(configMapStorage.generateKey().getEncoded());
    String dummyToken2 = "fake_token";
    logger.info("[RAW] token1 ===> " + dummyToken1);
    logger.info("[RAW] token2 ===> " + dummyToken2);
    configMapStorage.addConfig("token1", dummyToken1);
    configMapStorage.addConfig("token2", dummyToken2);

    configMapStorage.loadConfigMap();
    logger.info("[LOAD] configMap.size() = " + configMapStorage.getSize());
    logger.info("[LOAD] token2 ===> " + configMapStorage.getConfig("token2"));
    logger.info("[LOAD] token1 ===> " + configMapStorage.getConfig("token1"));
  }
}
