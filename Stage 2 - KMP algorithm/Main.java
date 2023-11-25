package analyzer;

import java.io.*;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("need three arguments");
            System.exit(0);
        }

        final String algorithm = args[0];
        final String fileName = args[1];
        final byte[] signature = args[2].getBytes();
        final String outputString = args[3];

        try (InputStream iStream = new FileInputStream(fileName)) {
            byte[] bytes = iStream.readAllBytes();
            switch (algorithm) {
                case "--naive" -> {
                    long startTime = System.nanoTime();
                    boolean isFileTypeOK = naive(bytes, signature);
                    double elapsedTime = (double) (System.nanoTime() - startTime) / 1000000000;
                    if (isFileTypeOK) {
                        System.out.println(outputString);
                    } else {
                        System.out.println("Unknown file type");
                    }
                    System.out.printf("It took %.3f %n seconds", elapsedTime);
                }
                case "--KMP" -> {
                    long startTime = System.nanoTime();
                    boolean isFileTypeOK = kmp(bytes, signature);
                    double elapsedTime = (double) (System.nanoTime() - startTime) / 1000000000;
                    if (isFileTypeOK) {
                        System.out.println(outputString);
                    } else {
                        System.out.println("Unknown file type");
                    }
                    System.out.printf("It took %.3f %n seconds", elapsedTime);
                }
                default -> System.out.println("Unknown algorithm");
            }
        } catch (FileNotFoundException e) {
            System.out.println("path: " + fileName + " could not be found");
        } catch (IOException e) {
            System.out.println("IO exception");
        }
    }

    private static boolean kmp(byte[] buffer, byte[] signature) {
        int[] prefixFunction = calculatePrefixFunction(signature);
        boolean fileTypeOK = false;
        int continueWithSymbol = 0;
        for (int i = 0; i < buffer.length - signature.length + 1; i++) {
            for (int j = continueWithSymbol; j < signature.length; j++) {
                if (buffer[i + j] == signature[j]) {
                    fileTypeOK = true;
                } else {
                    fileTypeOK = false;
                    continueWithSymbol = prefixFunction[j - 1];
                    i += (j - prefixFunction[j - 1]);
                    break;
                }
            }
            if (fileTypeOK) {
                return true;
            }
        }
        return false;
    }

    private static int[] calculatePrefixFunction(byte[] pattern) {
        int[] prefixFunction = new int[pattern.length];
        prefixFunction[0] = 0;  // first position in prefixfunction is always zero
        for (int i = 1; i < pattern.length; i++) {
            prefixFunction[i] = getPrefix(pattern, prefixFunction, i);
        }
        return prefixFunction.clone();
    }

    private static int getPrefix(byte[] pattern, int[] prefixFunction, int currentPosition) {
        int prefix = 0;
        for (int i = prefixFunction[currentPosition - 1]; i >= 0; i--) {
            if (Arrays.equals(Arrays.copyOfRange(pattern, 0, i),
                    Arrays.copyOfRange(pattern, currentPosition - i, currentPosition)))
            {
                prefix = i + 1;
                break;
            }
        }
        return prefix;
    }

    private static boolean naive(byte[] buffer, byte[] signature) {
        boolean fileTypeOK = false;
        for (int i = 0; i < buffer.length - signature.length + 1; i++) {
            for (int j = 1; j < signature.length; j++) {
                if (signature[j] == buffer[i + j]) {
                    fileTypeOK = true;
                } else {
                    fileTypeOK = false;
                    break;
                }
            }
            if (fileTypeOK) {
                return true;
            }
        }
        return false;
    }
}
