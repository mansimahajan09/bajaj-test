package com.example.destinationhashapp;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Random;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DestinationHashAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(DestinationHashAppApplication.class, args);
    }
}

@Component
class CommandLineAppStartupRunner implements CommandLineRunner {

    public void run(String... args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java -jar destination-hash-app.jar <PRN Number> <Path to JSON file>");
            System.exit(1);
        }

        String prnNumber = args[0].toLowerCase().trim();
        String jsonFilePath = args[1];

        String destinationValue = null;

        // Parse JSON file
        try (FileReader reader = new FileReader(jsonFilePath, StandardCharsets.UTF_8)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);

            // Traverse JSON to find the first instance of the key "destination"
            destinationValue = findDestinationValue(jsonObject);
        } catch (IOException e) {
            System.err.println("Error reading the JSON file: " + e.getMessage());
            System.exit(1);
        }

        if (destinationValue == null) {
            System.err.println("Key 'destination' not found in the JSON file.");
            System.exit(1);
        }

        // Generate a random alphanumeric string of size 8
        String randomString = generateRandomString(8);

        // Generate MD5 hash
        String concatenatedValue = prnNumber + destinationValue + randomString;
        String md5Hash = generateMD5Hash(concatenatedValue);

        // Output the result
        System.out.println(md5Hash + ";" + randomString);
    }

    private String findDestinationValue(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObject.get(key);

            if (key.equals("destination")) {
                return value.toString();
            } else if (value instanceof JSONObject) {
                String result = findDestinationValue((JSONObject) value);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    private String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hashString = new StringBuilder();
            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }
            return hashString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
