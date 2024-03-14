package com.app.damnvulnerablebank;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;

public class EncryptDecrypt {

    static public String secret = "amazing";
    static public int secretLength = secret.length();

    public static String operate(String input) {
        String result = "";
        for(int i = 0; i < input.length(); i++) {
            int xorVal = (int) input.charAt(i) ^ (int) secret.charAt(i % secretLength);
            char xorChar =  (char) xorVal;

            result += xorChar;
        }

        return result;
    }
/*
    public static String encrypt(String input) {
        String encVal = operate(input);
        String val = Base64.encodeToString(encVal.getBytes(),0);

        return val;
    }

    public static String decrypt(String input) {
        byte[] decodeByte = Base64.decode(input,0);
        String decodeString = new String(decodeByte);
        String decryptString = operate(decodeString);

        return decryptString;
    }*/
private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final String RSA_KEY_ALIAS = "MyRSAKeyAlias";

    public static void generateRSAKeys() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(
                    RSA_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .build());
        }
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
    }

    public static PublicKey getRSAPublicKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return (PublicKey) keyStore.getCertificate(RSA_KEY_ALIAS).getPublicKey();
    }

    public static PrivateKey getRSAPrivateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return (PrivateKey) keyStore.getKey(RSA_KEY_ALIAS, null);
    }
    public static String encrypt(String input) {
        try {
            PublicKey publicKey = getRSAPublicKey();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(input.getBytes());
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String input) {
        try {
            PrivateKey privateKey = getRSAPrivateKey();
            byte[] encryptedBytes = Base64.decode(input, Base64.DEFAULT);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
