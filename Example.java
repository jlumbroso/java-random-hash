
import randomhash.RandomHashFamily;
import randomhash.UniformAudit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class Example {

    public final static String DEFAULT_INPUT_FILE = "data/unique.txt";

    public static int count = 100;
    public static int bucketCount = 10;
    public static String input_file = DEFAULT_INPUT_FILE;
    public static boolean checkDuplicates = true;

    public static void main(String[] args) throws Exception {

        if (args.length >= 1 && args[0].equals("--help")) {
            System.out.println("Usage:\n\n   java Example [<hash function count> [<bucket count> [<input file>]]]\n\n");
            return;
        }

        if (args.length >= 1)
            count = Integer.parseInt(args[0]);
        if (args.length >= 2)
            bucketCount = Integer.parseInt(args[1]);
        if (args.length >= 3)
            input_file = args[2];

        RandomHashFamily rh = new RandomHashFamily(count);
        UniformAudit ua = new UniformAudit(bucketCount, RandomHashFamily.MIN_VALUE, RandomHashFamily.MAX_VALUE);

        HashSet<String> elementsSeen = new HashSet<String>();

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(input_file));

            String line = null;
            while ((line = reader.readLine()) != null) {

                if (checkDuplicates) {
                    if (elementsSeen.contains(line))
                        throw new Exception("duplicate element found: " + line);
                    elementsSeen.add(line);
                }

                long[] hashes = rh.hashes(line);
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
