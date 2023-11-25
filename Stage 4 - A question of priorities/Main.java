package analyzer;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        if (args.length < 2) {
            System.out.println("Please provide folder to search, and pattern file (2 arguments)");
            System.exit(0);
        }

        String folder = args[0];
        String patternFile = args[1];
        File directory = new File(folder);
        List<File> files = getAllFiles(directory);
        List<String> patternsList = new ArrayList<>(readPatterns(new File(patternFile)));
        Collections.reverse(patternsList);
        ExecutorService executor = Executors.newFixedThreadPool(10);

        List<Callable<String>> tasks = new ArrayList<>();

        for (File file : files) {
            Callable<String> task = () -> patternMatcher(file, patternsList);
            tasks.add(task);
        }
        List<Future<String>> futures = executor.invokeAll(tasks);

        executor.shutdown();

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                System.out.println(result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static String patternMatcher(File file, List<String> patternList) {
        String result = null;
        try (InputStream inputStream = new FileInputStream(file)) {
            String bytesToString = new String(inputStream.readAllBytes());
            for (String patternElement : patternList) {
                String pattern = patternElement.split(";", 3)[1]
                        .substring(1, patternElement.split(";", 3)[1].length() - 1);
                String fileType = patternElement.split(";", 3)[2]
                        .substring(1, patternElement.split(";", 3)[2].length() - 1);
                if (rabinKarpSearch(bytesToString, pattern)) {
                    result = file.getName() + ": " + fileType;
                    break;
                } else {
                    result = file.getName() + ": " + "Unknown file type";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
        Following substring search methods are modified implementations of those found on G4G.com
     */
    public static boolean rabinKarpSearch(String text, String pattern) {
        int d = 53;
        long q = 1_000_000_000 + 9;

        boolean found = false;

        int M = pattern.length();
        int N = text.length();
        int i, j;
        long p = 0; // hash value for pattern
        long t = 0; // hash value for txt
        long h = 1;

        // The value of h would be "pow(d, M-1)%q"
        for (i = 0; i < M - 1; i++)
            h = (h * d) % q;

        // Calculate the hash value of pattern and first
        // window of text
        for (i = 0; i < M; i++) {
            p = (d * p + pattern.charAt(i)) % q;
            if (i < N) t = (d * t + text.charAt(i)) % q;
        }

        // Slide the pattern over text one by one
        for (i = 0; i <= N - M; i++) {

            // Check the hash values of current window of text
            // and pattern. If the hash values match then only
            // check for characters on by one
            if (p == t) {
                /* Check for characters one by one */
                for (j = 0; j < M; j++) {
                    if (text.charAt(i + j) != pattern.charAt(j))
                        break;
                }

                // if p == t and pat[0...M-1] = txt[i, i+1, ...i+M-1]
                if (j == M) {
                    found = true;
                    break;
                }
            }
            // Calculate hash value for next window of text: Remove
            // leading digit, add trailing digit
            if (i < N - M) {
                t = (d * (t - text.charAt(i) * h) + text.charAt(i + M)) % q;

                // We might get negative value of t, converting it
                // to positive
                if (t < 0)
                    t = (t + q);
            }
        }
        return found;
    }

    public static List<String> readPatterns(File file) {
        String bytesToString = null;
        try (InputStream inputStream = new FileInputStream(file)) {
            bytesToString = new String(inputStream.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert bytesToString != null;
        return List.of(bytesToString.split("\n"));
    }

    public static List<File> getAllFiles(File directory) {
        List<File> files = new ArrayList<>();
        if (directory.isDirectory()) {
            File[] fileList = directory.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) {
                        files.addAll(getAllFiles(file));
                    } else {
                        files.add(file);
                    }
                }
            }
        }
        return files;
    }
}
