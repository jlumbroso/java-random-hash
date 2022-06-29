
package edu.princeton.cs.randomhash;

import java.lang.Math;
import java.lang.System;
import java.util.Random;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

/**
 * A family of pseudo-random hash functions designed to be very fast, yet
 * suitable for the implementation of data streaming algorithms that rely
 * on the assumption that hash functions "uniformize" the input data. <br/>
 * 
 * This family is based on a hash value computed with the CRC32 algorithm,
 * which is then multiplied by some random coprime number drawn using the
 * Mersenne Twister. Both are fast, broadly used, and well-studied algorithms.
 * This family of pseudo-random hash functions is empirically known to be
 * suitable for data streaming algorithms. <br/>
 * 
 * The class {@link edu.princeton.cs.randomhash.UniformAudit} is both an
 * example client for this class, and an empirical benchmark for the
 * randomness/uniformity of the values produced by these pseudo-random hash
 * functions.<br/>
 * 
 * This family of hash functions was first used by Philippe Flajolet when
 * he invented the data streaming algorithm, Probabilistic Counting (1973).
 * <br/>
 *
 * @author Jérémie Lumbroso
 * @version 1.1.1
 */
public class RandomHashFamily {

    /**
     * The largest value that can be returned by {@link #hash(String)} and related
     * methods.
     */
    public final static long MAX_VALUE = Integer.toUnsignedLong((int) Long.MAX_VALUE);

    /**
     * The smallest value that can be returned by {@link #hash(String)} and related
     * methods.
     */
    public final static long MIN_VALUE = 0;

    /**
     * The internal seed used to initialize the underlying PRNG.
     */
    protected long seed;

    /**
     * The underlying PRNG, which is of type {@see java.util.Random}.
     */
    protected Random prng;

    /**
     * Number of pseudo-random hash functions in this {@link RandomHashFamily}.
     */
    protected int count;

    /**
     * Unsigned integer array (stored as {@code long}) of random coprime numbers
     * used as the linear map of the affine transformation used to generate
     * the pseudo-random hash functions.
     */
    protected long[] numsCoprime;

    /**
     * Unsigned integer arrays (stored as {@code long}) of random uniform numbers
     * used as the translation of the affine transformation used to generate
     * the pseudo-random hash functions.
     */
    protected long[] numsNoise;

    /**
     * Constructor for {@link RandomHashFamily}, which initializes a family
     * with a unique hash function.
     */
    public RandomHashFamily() {
        this((int) 1);
    }

    /**
     * Constructor for {@link RandomHashFamily}, which initializes a family
     * of hash functions.
     * 
     * @param count The number of pseudo-random hash functions in this family.
     */
    public RandomHashFamily(int count) {
        this(System.currentTimeMillis(), count);
    }

    /**
     * Constructor for {@link RandomHashFamily}, which initializes a family
     * with a unique hash function, using the given seed to initialize the
     * underlying PRNG.
     * 
     * @param seed The seed used to initialize the underlying PRNG.
     */
    public RandomHashFamily(long seed) {
        this(seed, (int) 1);
    }

    /**
     * Constructor for {@link RandomHashFamily}, which initializes a family
     * of hash functions, using the given seed to initialize the underlying
     * PRNG.
     * 
     * @param seed  The seed used to initialize the underlying PRNG.
     * @param count The number of pseudo-random hash functions in this family.
     */
    public RandomHashFamily(long seed, int count) {
        prng = new MTRandom();
        prng.setSeed(seed);

        this.count = count;

        this.numsCoprime = new long[this.count];
        this.numsNoise = new long[this.count];

        for (int i = 0; i < this.count; i++) {
            numsCoprime[i] = this.generateCoprime();
            numsNoise[i] = Integer.toUnsignedLong(prng.nextInt());
        }
    }

    /**
     * Generates a random number that is odd, and likely to be coprime
     * with a randomly generated number.
     * 
     * @return long A random number that is odd and likely to be coprime with a
     *         random number.
     */
    protected long generateCoprime() {
        return 2 * Integer.toUnsignedLong(prng.nextInt()) + 1;
    }

    /**
     * Returns an affine transform {@literal x -> ax + b}, used internally to
     * generate many pseudo-random hash functions from a single hashed value.
     * 
     * @param x The hashed value.
     * @param a The multiplier of the affine transform.
     * @param b The translation of the affine transform.
     * @return long The affine transform {@literal x -> ax + b}.
     */
    protected final static long affineTransform(long x, long a, long b) {
        return (long) (((long) a) * x + b);
    }

    /**
     * Returns the 32 low-order bits of the possibly 64-bit value provided.
     * 
     * @param value A possibly 64-bit value, to be truncated to 32 bits.
     * @return long The 32 low-order bits of the possibly 64-bit value provided.
     */
    protected final static long truncateLong(long value) {
        // remove the upper 32 bits by shifting
        return Integer.toUnsignedLong((int) value);
    }

    /**
     * Computes and returns the hash value for the given key, without any
     * additional transformation applied. <br/>
     * 
     * This method will serve as the basis for the all the other calculated
     * pseudo-random hash functions. It currently is implemented using
     * {@see java.util.zip.CRC32}, and to extend this class to other hash functions,
     * you would override this method.
     * 
     * @param key A string key to be hashed
     * @return long The hash value for the given key
     */
    protected long baseHash(String key) {
        Checksum ck = new CRC32();
        ck.update(key.getBytes());
        return ck.getValue();
    }

    /**
     * Computes and returns the pseudo-random hash values for the given key.
     * 
     * @param key    A string key to be hashed
     * @param hashes An array of 32-bit unsigned integers to be filled with the
     *               pseudo-random hash values.
     */
    public void hashes(String key, long[] hashes) {
        long baseHash = this.baseHash(key);

        count = Math.min(hashes.length, this.count);

        for (int i = 0; i < count; i++) {
            hashes[i] = RandomHashFamily.truncateLong(
                    RandomHashFamily.affineTransform(baseHash, numsCoprime[i], numsNoise[i]));
        }
    }

    /**
     * Computes and returns the pseudo-random hash values for the given key.
     * 
     * @param key A string key to be hashed
     * @return long[] An array of 32-bit unsigned integers containing the
     *         pseudo-random hash values
     */
    public long[] hashes(String key) {
        long[] hashes = new long[this.count];
        this.hashes(key, hashes);
        return hashes;
    }

    /**
     * Computes and returns a single pseudo-random hash value. <br/>
     * 
     * This is the same as {@link #hashes(String)}, but only returns the first
     * hash value.
     * 
     * @param key A string key to be hashed
     * @return long A single pseudo-random hash value
     */
    public long hash(String key) {
        long[] hash = new long[1];
        this.hashes(key, hash);
        return hash[0];
    }

}
