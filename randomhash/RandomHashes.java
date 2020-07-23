
package randomhash;

import randomhash.MTRandom;

import java.lang.Math;
import java.lang.System;
import java.util.Random;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class RandomHashes {

    public final static long MAX_VALUE = Integer.toUnsignedLong((int)Long.MAX_VALUE);
    public final static long MIN_VALUE = 0;

    protected long seed;
    protected Random prng;

    protected int count;

    // unsigned integer arrays (stored in longs)
    protected long[] numsCoprime;
    protected long[] numsNoise;


    public RandomHashes() { this((int) 1); }

    public RandomHashes(int count) { this(System.currentTimeMillis(), count); }

    public RandomHashes(long seed) { this(seed, (int) 1); }

    public RandomHashes(long seed, int count) {
        prng = new MTRandom();
        prng.setSeed(seed);

        this.count = count;

        this.numsCoprime = new long[this.count];
        this.numsNoise = new long[this.count];
        
        for (int i=0; i < this.count; i++) {
            numsCoprime[i] = this.generateCoprime();
            numsNoise[i] = Integer.toUnsignedLong(prng.nextInt());
        }
    }

    protected long generateCoprime() {
        return 2*Integer.toUnsignedLong(prng.nextInt()) + 1;
    }

    protected final static long affineTransform(long x, long a, long b) {
        return (long) (((long)a)*x + b);
    }

    protected final static long truncateLong(long value) {
        // remove the upper 32 bits by shifting
        return Integer.toUnsignedLong((int) value);
    }

    protected long baseHash(String key) {
        Checksum ck = new CRC32();
        ck.update(key.getBytes());
        return ck.getValue();
    }

    public void hashes(String key, long[] hashes) {
        long baseHash = this.baseHash(key);

        count = Math.min(hashes.length, this.count);
        
        for (int i=0; i < count; i++) {
            hashes[i] = RandomHashes.truncateLong(
                RandomHashes.affineTransform(baseHash, numsCoprime[i], numsNoise[i]));
        }
    }

    public long[] hashes(String key) {
        long[] hashes = new long[this.count];
        this.hashes(key, hashes);
        return hashes;
    }

    public long hash(String key) {
        long[] hash = new long[1];
        this.hashes(key, hash);
        return hash[0];
    }

}
