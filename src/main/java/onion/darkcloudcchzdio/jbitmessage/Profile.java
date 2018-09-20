/*******************************************************************************
               __ _____ _ _   _____
            __|  | __  |_| |_|     |___ ___ ___ ___ ___ ___
           |  |  | __ -| |  _| | | | -_|_ -|_ -| .'| . | -_|
           |_____|_____|_|_| |_|_|_|___|___|___|__,|_  |___|
                                                   |___|
 Security oriented Java implementation of @Bitmessage CLIENT and SERVER
 (https://github.com/darkcloudcchzdio-onion/JBitMessage)

 Developed by darkcloudcchzdio.onion Dev Team and other Contributors
 Sponsored by darkcloudcchzdio.onion and other Donators

 Contacts:
 * email: dev+jbitmessage@darkcloudcchzdio.onion
 * url: https://darkcloudcchzdio.onion/project/jbitmessage
 Please note, you need Tor Browser or any Tor related software for access
 to Tor Hidden Services (aka .onion) sites.
 More information about Tor and related - https://www.torproject.org/

 License terms:
 * for open source purpose - GPL3 (LICENSE.GPL3)
 * for education purpose - CC BY-NC-ND 4.0i (LICENSE.CC)
 * for commercial or any other purpose - EULA (LICENSE.EULA)

 *******************************************************************************/
package onion.darkcloudcchzdio.jbitmessage;

import onion.darkcloudcchzdio.jbitmessage.crypto.AESEncryptionProvider;
import onion.darkcloudcchzdio.jbitmessage.crypto.ChunkedSecureHardDriveStorage;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    private ChunkedSecureHardDriveStorage storage;

    private void createProfile(String name) {
        System.out.println("Profile " + name + " not found");
        System.out.println("Create profile: " + name);
        String password = readPassword();
        secret = generateSecret(password);
        File file  = new File(name);
        try {
            file.createNewFile();
            new ChunkedSecureHardDriveStorage(file, new AESEncryptionProvider(secret)).put("PROFILE:CREATE", name);
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
        System.out.println("Load profile: " + name);
        File file = new File(name);
        storage = new ChunkedSecureHardDriveStorage(file, new AESEncryptionProvider(secret));
        try {
            String data = (String) storage.get("PROFILE:CREATE");
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