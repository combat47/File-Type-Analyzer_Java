package analyzer;

import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide the input file");
            System.exit(0);
        }
        String inputFile = args[0];
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile))) {
            String docType = args[1];

            Scanner sc = new Scanner(inputStream).useDelimiter("\\A");
            String fileString = sc.hasNext() ? sc.next() : "";

            if (fileString.contains(docType)) {
                System.out.println(args[2]);
            } else {
                System.out.println("Unknown file type");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
