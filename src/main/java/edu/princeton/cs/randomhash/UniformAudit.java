package edu.princeton.cs.randomhash;

/**
 * Companion class to {@link RandomHashFamily}, meant both to be an example
 * client, and to provide a benchmark for the empirical randomness/uniformity of
 * the values produced by these pseudo-random hash functions.
 * 
 * @author Jérémie Lumbroso
 * @version 1.1.1
 */
public class UniformAudit {

    /**
     * The number of buckets in which to group the values produced by the
     * pseudo-random hash functions.
     */
    protected int bucketCount;

    /**
     * The smallest value that we expect to see among the values fed to this class.
     */
    protected long minValue;

    /**
     * The largest value that we expect to see among the values fed to this class.
     */
    protected long maxValue;

    /**
     * The computed bucket size, based on the values provided as
     * {@link bucketCount}, {@link minValue} and {@link maxValue}.
     */
    protected long bucketSize;

    /**
     * The internal array containing the buckets in which we count the number
     * of hash values that have been processed, which fall in the corresponding
     * interval.
     */
    protected long[] buckets;

    /**
     * The total number of hash values that have been processed.
     */
    protected long total;

    /**
     * Creates a new {@link UniformAudit} instance.
     * 
     * @param bucketCount The number of buckets in which to group the hash values
     *                    that are processed
     * @param min         The smallest value that we expect to see among the values
     *                    fed to this class
     * @param max         The largest value that we expect to see among the values
     *                    fed to this class
     */
    public UniformAudit(int bucketCount, long min, long max) {
        this.bucketCount = bucketCount;
        this.minValue = min;
        this.maxValue = max;

        this.bucketSize = (this.maxValue - this.minValue) / ((long) this.bucketCount);

        this.reset();
    }

    /**
     * Resets all internal counters to zero (both the buckets and the running
     * total).
     */
    public void reset() {
        this.buckets = new long[this.bucketCount];
        this.total = 0;
    }

    /**
     * Process a single hash value.
     * 
     * @param value The hash value to process
     * @throws IllegalArgumentException If the given value is not in the range by
     *                                  the internal minimum and maximum values
     *                                  specified at construction time.
     */
    public void update(int value) {
        this.update(Integer.toUnsignedLong(value));
    }

    /**
     * Process a single hash value.
     * 
     * @param value The hash value to process
     * @throws IllegalArgumentException If the given value is not in the range by
     *                                  the internal minimum and maximum values
     *                                  specified at construction time.
     */
    public void update(long value) {
        if (value < this.minValue)
            throw new IllegalArgumentException("value is smaller than min expected value");

        if (value > this.maxValue)
            throw new IllegalArgumentException("value is larger than max expected value");

        int bucketIndex = (int) ((value - this.minValue) / this.bucketSize);
        this.buckets[bucketIndex]++;
        this.total++;
    }

    /**
     * Returns the total number of hash values that have been processed so far.
     * 
     * @return long The total number of hash values that have been processed.
     */
    public long total() {
        return this.total;
    }

    /**
     * Returns the probability distribution of the hash values that have been
     * processed, as an array of values that sum to {@code 1.0}.
     * 
     * @return double[] An array with the discrete probability distribution of the
     *         hash values
     */
    public double[] bucketDistribution() {
        double[] nums = new double[this.bucketCount];
        for (int i = 0; i < nums.length; i++)
            nums[i] = ((double) this.buckets[i]) / ((double) this.total) * 100;
        return nums;
    }

    /**
     * Computes and returns the Chi Square statistic for the hash values that have
     * been processed. <br/>
     * 
     * This is a measure of the distance of the (empirical) distribution of the hash
     * values that have been processed, to the idealized discrete uniform
     * distribution. If the empirical distribution is close to the discrete uniform
     * distribution, then this Chi Square statistic will be below the critical
     * threshold defined in the tables {@see upperTailCV90} (for 90% confidence) and
     * {@see upperTailCV99} (for 99% confidence).
     * 
     * @return double The Chi Square statistic for the empirical distribution of
     *         hash values that have been processed
     */
    public double chiSquaredTest() {
        double chiSquaredTest = 0.0;
        double nums[] = this.bucketDistribution();

        double expected = 100.0 / this.bucketCount;
        for (int i = 0; i < this.bucketCount; i++) {
            double observed = nums[i];

            double sqrtNumerator = observed - expected;
            double numerator = sqrtNumerator * sqrtNumerator;
            double term = numerator / expected;

            chiSquaredTest += term;
        }
        return chiSquaredTest;
    }

    /**
     * Returns {@code true} if there is at least a 90% probability that the hash
     * values are uniformly distributed, using a Chi Square statistical test.
     * 
     * @return boolean {@code true} if there is at least a 90% probability that the
     *         hash values are uniformly distributed
     */
    public boolean isLikelyUniform() {
        double chiSquaredTest = this.chiSquaredTest();
        return acceptNullHypothesis(this.bucketCount - 1, chiSquaredTest);
    }

    /**
     * Returns {@code true} if there is the Chi Square statistic, with the given
     * degrees of freedom, is below the critical threshold defined in the tables
     * {@see upperTailCV90} (for 90% confidence).
     * 
     * @param df             The number of degrees of freedom in the Chi Square
     *                       statistical test
     * @param chiSquaredTest The Chi Square statistic for the empirical distribution
     * @return boolean {@code true} if there is at least a 90% probability that the
     *         hash values are uniformly distributed
     */
    protected static boolean acceptNullHypothesis(int df, double chiSquaredTest) {
        if (df <= 0)
            throw new IllegalArgumentException("df must be strictly positive");
        if (df > 100)
            throw new IllegalArgumentException("df's larger than 100 not supported");

        // get the critical value
        double cv = upperTailCV90[df - 1];
        return chiSquaredTest < cv;
    }

    /**
     * Prints a summary of the empirical distribution of the hash values processed
     * so far, and computes the Chi Squared statistic, to determine how likely it is
     * for the hash values to seem uniformly distributed.
     */
    public void printReport() {
        double[] distribution = this.bucketDistribution();
        double chiSquaredTest = this.chiSquaredTest();
        boolean likelyUniform = this.isLikelyUniform();

        System.out.println("hashing report:");
        System.out.println("> bucket count: " + this.bucketCount);
        System.out.println("> total values hashed: " + this.total);
        System.out.print("> [ ");
        for (int i = 0; i < distribution.length; i++)
            System.out.printf("%5.2f%% ", distribution[i]);
        System.out.println(" ]");
        System.out.printf("> chi^2 test: %f\n", chiSquaredTest);
        System.out.println("> is uniform (with 90% confidence)? " + likelyUniform);
    }

    // ==============================================================================
    // CRITICAL VALUES of chi squared test

    // From: https://www.itl.nist.gov/div898/handbook/eda/section3/eda3674.htm
    // See also: https://www.itl.nist.gov/div898/handbook/eda/section3/eda35f.htm

    /**
     * Upper-tail critical values of chi-square distribution with ν degrees of
     * freedom and 90% confidence
     * (see: https://www.itl.nist.gov/div898/handbook/eda/section3/eda3674.htm)
     */
    protected final static double[] upperTailCV90 = {
            2.706, // df = 1
            4.605, // df = 2
            6.251, // df = 3
            7.779, // df = 4
            9.236, // df = 5
            10.645, // df = 6
            12.017, // df = 7
            13.362, // df = 8
            14.684, // df = 9
            15.987, // df = 10
            17.275, // df = 11
            18.549, // df = 12
            19.812, // df = 13
            21.064, // df = 14
            22.307, // df = 15
            23.542, // df = 16
            24.769, // df = 17
            25.989, // df = 18
            27.204, // df = 19
            28.412, // df = 20
            29.615, // df = 21
            30.813, // df = 22
            32.007, // df = 23
            33.196, // df = 24
            34.382, // df = 25
            35.563, // df = 26
            36.741, // df = 27
            37.916, // df = 28
            39.087, // df = 29
            40.256, // df = 30
            41.422, // df = 31
            42.585, // df = 32
            43.745, // df = 33
            44.903, // df = 34
            46.059, // df = 35
            47.212, // df = 36
            48.363, // df = 37
            49.513, // df = 38
            50.660, // df = 39
            51.805, // df = 40
            52.949, // df = 41
            54.090, // df = 42
            55.230, // df = 43
            56.369, // df = 44
            57.505, // df = 45
            58.641, // df = 46
            59.774, // df = 47
            60.907, // df = 48
            62.038, // df = 49
            63.167, // df = 50
            64.295, // df = 51
            65.422, // df = 52
            66.548, // df = 53
            67.673, // df = 54
            68.796, // df = 55
            69.919, // df = 56
            71.040, // df = 57
            72.160, // df = 58
            73.279, // df = 59
            74.397, // df = 60
            75.514, // df = 61
            76.630, // df = 62
            77.745, // df = 63
            78.860, // df = 64
            79.973, // df = 65
            81.085, // df = 66
            82.197, // df = 67
            83.308, // df = 68
            84.418, // df = 69
            85.527, // df = 70
            86.635, // df = 71
            87.743, // df = 72
            88.850, // df = 73
            89.956, // df = 74
            91.061, // df = 75
            92.166, // df = 76
            93.270, // df = 77
            94.374, // df = 78
            95.476, // df = 79
            96.578, // df = 80
            97.680, // df = 81
            98.780, // df = 82
            99.880, // df = 83
            100.980, // df = 84
            102.079, // df = 85
            103.177, // df = 86
            104.275, // df = 87
            105.372, // df = 88
            106.469, // df = 89
            107.565, // df = 90
            108.661, // df = 91
            109.756, // df = 92
            110.850, // df = 93
            111.944, // df = 94
            113.038, // df = 95
            114.131, // df = 96
            115.223, // df = 97
            116.315, // df = 98
            117.407, // df = 99
            118.498, // df =100
    };

    /**
     * Upper-tail critical values of chi-square distribution with ν degrees of
     * freedom and 99% confidence
     * (see: https://www.itl.nist.gov/div898/handbook/eda/section3/eda3674.htm)
     */
    protected final static double[] upperTailCV99 = {
            6.635, // df = 1
            9.210, // df = 2
            11.345, // df = 3
            13.277, // df = 4
            15.086, // df = 5
            16.812, // df = 6
            18.475, // df = 7
            20.090, // df = 8
            21.666, // df = 9
            23.209, // df = 10
            24.725, // df = 11
            26.217, // df = 12
            27.688, // df = 13
            29.141, // df = 14
            30.578, // df = 15
            32.000, // df = 16
            33.409, // df = 17
            34.805, // df = 18
            36.191, // df = 19
            37.566, // df = 20
            38.932, // df = 21
            40.289, // df = 22
            41.638, // df = 23
            42.980, // df = 24
            44.314, // df = 25
            45.642, // df = 26
            46.963, // df = 27
            48.278, // df = 28
            49.588, // df = 29
            50.892, // df = 30
            52.191, // df = 31
            53.486, // df = 32
            54.776, // df = 33
            56.061, // df = 34
            57.342, // df = 35
            58.619, // df = 36
            59.893, // df = 37
            61.162, // df = 38
            62.428, // df = 39
            63.691, // df = 40
            64.950, // df = 41
            66.206, // df = 42
            67.459, // df = 43
            68.710, // df = 44
            69.957, // df = 45
            71.201, // df = 46
            72.443, // df = 47
            73.683, // df = 48
            74.919, // df = 49
            76.154, // df = 50
            77.386, // df = 51
            78.616, // df = 52
            79.843, // df = 53
            81.069, // df = 54
            82.292, // df = 55
            83.513, // df = 56
            84.733, // df = 57
            85.950, // df = 58
            87.166, // df = 59
            88.379, // df = 60
            89.591, // df = 61
            90.802, // df = 62
            92.010, // df = 63
            93.217, // df = 64
            94.422, // df = 65
            95.626, // df = 66
            96.828, // df = 67
            98.028, // df = 68
            99.228, // df = 69
            100.425, // df = 70
            101.621, // df = 71
            102.816, // df = 72
            104.010, // df = 73
            105.202, // df = 74
            106.393, // df = 75
            107.583, // df = 76
            108.771, // df = 77
            109.958, // df = 78
            111.144, // df = 79
            112.329, // df = 80
            113.512, // df = 81
            114.695, // df = 82
            115.876, // df = 83
            117.057, // df = 84
            118.236, // df = 85
            119.414, // df = 86
            120.591, // df = 87
            121.767, // df = 88
            122.942, // df = 89
            124.116, // df = 90
            125.289, // df = 91
            126.462, // df = 92
            127.633, // df = 93
            128.803, // df = 94
            129.973, // df = 95
            131.141, // df = 96
            132.309, // df = 97
            133.476, // df = 98
            134.642, // df = 99
            135.807, // df =100
    };

    // ==============================================================================
}
