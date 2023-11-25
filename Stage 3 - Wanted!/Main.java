package analyzer;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {

        if (args.length < 3) {
            System.out.println("need three arguments");
            System.exit(0);
        }

        File fileName = new File(args[0]);
        final byte[] signature = args[1].getBytes();
        final String outputString = args[2];

        List<Callable<String>> callables = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (File a :
                Objects.requireNonNull(fileName.listFiles())) {
            callables.add(() -> {
                String res = null;
                try (InputStream iStream = new FileInputStream(a)) {
                    byte[] bytes = iStream.readAllBytes();
                    boolean isFileTypeOK = kmp(bytes, signature);
                    if (isFileTypeOK) {
                        res = String.format("%s: %s", a.getName(),outputString);
                    } else {
                        res = String.format("%s: Unknown file type", a.getName());
                    }
                    return res;
                } catch (FileNotFoundException e) {
                    System.out.println("path: " + a.getName() + " could not be found");
                } catch (IOException e) {
                    System.out.println("IO exception");
                }
                return res;
            });
        }

        List<Future<String>> results = executor.invokeAll(callables);

        for (var result: results) {
            System.out.println(result.get());
        }
        executor.shutdown();

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
                    continueWithSymbol = prefixFunction[j];
                    i += (j - prefixFunction[j]);
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
        prefixFunction[0] = 0;  // first position in prefixFunction is always zero
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
}
