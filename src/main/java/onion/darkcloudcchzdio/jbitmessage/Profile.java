package onion.darkcloudcchzdio.jbitmessage;

import onion.darkcloudcchzdio.jbitmessage.crypto.AESEncryptionProvider;
import onion.darkcloudcchzdio.jbitmessage.crypto.EncryptionProvider;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Profile {

    private static String readPassword() {
        System.out.println("Please enter your password:");
        Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword();
            return new String(passwordChars);
        }
        byte[] buffer = new byte[128];
        try {
            System.in.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static SecretKey generateSecret(String data) {
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA-256");
            digester.update(data.getBytes("UTF-8"));
            byte[] key = digester.digest();
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            return keySpec;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Profile(String name) {
        if (!new File(name).exists()) {
            createProfile(name);
        }
        loadProfile(name);
    }

    private SecretKey secret;
    private EncryptionProvider encryptionProvider;

    private void createProfile(String name) {
        System.out.println("Profile " + name + " not found");
        System.out.println("Create profile: " + name);
        String password = readPassword();
        secret = generateSecret(password);
        encryptionProvider = new AESEncryptionProvider(secret);
        File file  = new File(name);
        try {
            file.createNewFile();
            byte[] bytes = encryptionProvider.serialize(name);
            FileOutputStream out = new FileOutputStream(file);
            out.write(bytes);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProfile(String name) {
        String password = readPassword();
        SecretKey secret = generateSecret(password);
        if (this.secret != null && !this.secret.equals(secret)) {
            System.out.println("Incorrect password. Try again");
            loadProfile(name);
            return;
        }
        if (encryptionProvider == null) {
            encryptionProvider = new AESEncryptionProvider(secret);
        }
        System.out.println("Load profile: " + name);
        File file = new File(name);
        try {
            byte[] bytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
            String data = (String) encryptionProvider.deserialize(bytes);
            if (!data.equals(name)) {
                System.out.println("Incorrect password. Try again");
                loadProfile(name);
                return;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Profile is loaded");
    }
}
