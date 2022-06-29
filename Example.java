import edu.princeton.cs.randomhash.RandomHashFamily;
import edu.princeton.cs.randomhash.UniformAudit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * The {@link edu.princeton.cs.randomhash.Example} class provides an example
 * client for both the {@link edu.princeton.cs.randomhash.RandomHashFamily}
 * class and the {@link edu.princeton.cs.randomhash.UniformAudit} class,
 * which also happens to be a convenient command-line interface to the class.
 * <br/>
 * 
 * It allows for the hashing of arbitrary files, using the pseudo-random hash
 * family.
 *
 * @author Jérémie Lumbroso
 * @version 1.1.1
 */
public class Example {

    /**
     * Default input file to be used, if no other file is specified.
     */
    public final static String DEFAULT_INPUT_FILE = "data/unique.txt";

    /**
     * Number of hash functions to be used in the family.
     */
    public static int count = 100;

    /**
     * Number of buckets to be used when running a {@link UniformAudit}.
     */
    public static int bucketCount = 10;

    /**
     * Input file to read from.
     */
    public static String input_file = DEFAULT_INPUT_FILE;

    /**
     * Boolean determining whether to filter out duplicates.
     */
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
                    if (elementsSeen.contains(line)) {
                        reader.close();
                        throw new Exception("duplicate element found: " + line);
                    }
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
