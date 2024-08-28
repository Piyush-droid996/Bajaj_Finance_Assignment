package com.springmvc.DestinationHashGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class App {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN Number> <path to json file>");
            System.exit(1);
        }

        String prnNumber = args[0];
        String filePath = args[1];

        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = new FileInputStream(filePath)) {
            // Read the JSON file into a JsonNode
            JsonNode root = objectMapper.readTree(inputStream);

            // Traverse the JSON and find the first instance of the key "destination"
            String destinationValue = findDestinationValue(root);
            if (destinationValue != null) {
                // Generate a random alphanumeric string of size 8 characters
                String randomString = generateRandomString(8);
                
                // Concatenate PRN number, destination value, and random string
                String concatenatedString = prnNumber + destinationValue + randomString;

                // Generate the MD5 hash
                String md5Hash = calculateMD5(concatenatedString);
                
                // Print the result in the format: <hash>;<random string>
                System.out.println(md5Hash + ";" + randomString);
            } else {
                System.out.println("No 'destination' key found in the JSON file.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error reading JSON file.");
        }
    }

    // Recursively traverse JSON to find the value of the first "destination" key
    private static String findDestinationValue(JsonNode node) {
        if (node.isObject()) {
            for (String fieldName : ((Iterable<String>) () -> node.fieldNames())) {
                JsonNode childNode = node.get(fieldName);
                if (fieldName.equals("destination")) {
                    return childNode.asText();
                }
                String result = findDestinationValue(childNode);
                if (result != null) {
                    return result;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayElement : node) {
                String result = findDestinationValue(arrayElement);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    // Generate a random alphanumeric string of specified length
    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    // Calculate MD5 hash of the given input string
    private static String calculateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
