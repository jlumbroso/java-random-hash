
import randomhash.RandomHashFamily;
import randomhash.UniformAudit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Example {

    public final static String DEFAULT_INPUT_FILE = "data/normalized.txt";

    public static int count = 100;
    public static int bucketCount = 10;
    public static String input_file = DEFAULT_INPUT_FILE;

    public static void main(String[] args) {

        if (args.length >= 1)
            count = Integer.parseInt(args[0]);
        if (args.length >= 2)
            bucketCount = Integer.parseInt(args[1]);
        if (args.length >= 3)
            input_file = args[2];

        RandomHashFamily rh = new RandomHashFamily(count);
        UniformAudit ua = new UniformAudit(bucketCount, RandomHashFamily.MIN_VALUE, RandomHashFamily.MAX_VALUE);

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(input_file));

            String line = null;
            while ((line = reader.readLine()) != null) {
                long[] hashes = rh.hashes(line);
                /*
                 * System.out.println(line); System.out.println(hash); System.out.println();
                 */
                for (int i = 0; i < count; i++)
                    ua.update(hashes[i]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("input: " + input_file);
        System.out.println("number of hash functions: " + count);
        ua.printReport();
    }
}
