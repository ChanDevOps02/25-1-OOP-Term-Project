package filemanager.util;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

public class EncryptionUtil {

    // AES-256/CBC/PKCS5Padding + PBKDF2 키 파생
    public static void encrypt(File inFile, File outFile, String password) throws Exception {
        // 8바이트 salt
        byte[] salt = new byte[8];
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(salt);

        // PBKDF2로 256비트 키 생성
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = f.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] iv = new byte[cipher.getBlockSize()];
        sr.nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));

        try (FileOutputStream fos = new FileOutputStream(outFile);
             DataOutputStream dos = new DataOutputStream(fos);
             FileInputStream fis = new FileInputStream(inFile)) {

            // 메타데이터: salt, iv
            dos.writeInt(salt.length); dos.write(salt);
            dos.writeInt(iv.length);   dos.write(iv);

            // 파일 데이터 암호화 전송
            byte[] buffer = new byte[4096];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                byte[] out = cipher.update(buffer, 0, n);
                if (out != null) dos.write(out);
            }
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) dos.write(finalBytes);
        }
    }

    public static void decrypt(File inFile, File outFile, String password) throws Exception {
        try (FileInputStream fis = new FileInputStream(inFile);
             DataInputStream dis = new DataInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outFile)) {

            // salt, iv 읽기
            byte[] salt = new byte[dis.readInt()]; dis.readFully(salt);
            byte[] iv   = new byte[dis.readInt()]; dis.readFully(iv);

            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
            SecretKey tmp = f.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

            // 파일 데이터 복호화
            byte[] buffer = new byte[4096];
            int n;
            while ((n = dis.read(buffer)) != -1) {
                byte[] out = cipher.update(buffer, 0, n);
                if (out != null) fos.write(out);
            }
            byte[] finalBytes = cipher.doFinal();
            if (finalBytes != null) fos.write(finalBytes);
        }
    }
}
