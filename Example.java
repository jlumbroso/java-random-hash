
import randomhash.RandomHashes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Example {

    public final static int count = 100;
    public final static String INPUT_FILE = "data/normalized.txt";
	public static void main(String[] args) {
        RandomHashes rh = new RandomHashes(count);
        UniformAudit ua = new UniformAudit(10, rh.MIN_VALUE, rh.MAX_VALUE);

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(INPUT_FILE));
            
            String line = null;  
            while ((line = reader.readLine()) != null) { 
                long[] hashes = rh.hashes(line);
                /*System.out.println(line);
                System.out.println(hash);
                System.out.println();*/
                for(int i=0; i < count; i++)
                    ua.update(hashes[i]);
            }
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
        }
        double[] b = ua.audit();
        for (int i=0; i < b.length; i++)
            System.out.printf("%5.2f%%\n", b[i]);
    }
    
    public static class UniformAudit {

        protected int bucketCount;
        protected long minValue;
        protected long maxValue;

        protected long bucketSize;
        
        protected long[] buckets;
        protected long total;

        public UniformAudit(int bucketCount, long min, long max) {
            this.bucketCount = bucketCount;
            this.minValue = min;
            this.maxValue = max;

            this.bucketSize = (this.maxValue - this.minValue) / ((long)this.bucketCount);

            this.reset();
        }

        public void reset() {
            this.buckets = new long[this.bucketCount];
            this.total = 0;
        }

        public void update(int value) { this.update(Integer.toUnsignedLong(value)); }

        public void update(long value) {
            if (value < this.minValue)
                throw new IllegalArgumentException("value is smaller than min expected value");
            
            if (value > this.maxValue)
                throw new IllegalArgumentException("value is larger than max expected value");

            int bucketIndex = (int) ((value - this.minValue)/this.bucketSize);
            this.buckets[bucketIndex]++;
            this.total++;
        }
        
        public double[] audit() {
            double[] nums = new double[this.bucketCount];
            for (int i=0; i < nums.length; i++)
                nums[i] = ((double)this.buckets[i])/((double)this.total)*100;
            return nums;
        }
    }
}
