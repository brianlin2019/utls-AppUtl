package brnln.utils.ndUtl._devLab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class C001 {

    public static void main(String[] args) {
        System.out.println(String.format("file.encoding:%s", System.getProperty("file.encoding")));
        try (InputStream in = C001.class.getResourceAsStream("t01.txt");
                InputStreamReader inRdr = new InputStreamReader(in, "UTF-8");
                BufferedReader bufRdr = new BufferedReader(inRdr)) {
            String line = bufRdr.readLine();
            while (line != null) {
                try {
                    System.out.println("line:" + line);
                } finally {
                    line = bufRdr.readLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }

    }

}
