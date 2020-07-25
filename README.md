# java-random-hash

A simple, time-tested, family of random hash functions in Java, based on CRC32.

## Features

This introduces a family of hash functions that can be used to implement probabilistic
algorithms such as HyperLogLog. It is based on *affine transformations of the CRC32 hash
functions*, which has been shown to provide good performance. The pseudo-random numbers
are drawn according to
[David Beaumont's Java implementation](http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/VERSIONS/JAVA/MTRandom.java)
(included here for convenience but with full credit) of the [Mersenne Twister](http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html).

To try out the hash functions, you can do:
```shell
javac Example.java
java Example
```

This will generate a report, such as this one, which show how ten hash functions perform
at provided data that appears pseudo-random:

```
java Example       
input: data/unique.txt
number of hash functions: 100
hashing report:
> bucket count: 10
> total values hashed: 1670700
> [ 10.00% 10.03% 10.03%  9.96% 10.01% 10.00%  9.99%  9.98% 10.02%  9.98%  ]
> chi^2 test: 0.000399
> is uniform (with 90% confidence)? true
```

In practice, you can use it this way:
```java
import randomhash.RandomHashFamily;

RandomHashFamily rhf = new RandomHashFamily(1);

System.out.print("hello -> ");
System.out.print(rhf("hello"));
```
which will print:
```
hellow -> 2852342977
```
it can also generate several pseudo-random hash at the same time, in this case 10:
```java
RandomHashFamily rhf = new RandomHashFamily(10);
long[] hashes = rhf.hashes(); // 10 elements
```

## Some history

In  1983, G. N. N. Martin and Philippe Flajolet introduced the algorithm known
as [_Probabilistic Counting_](http://algo.inria.fr/flajolet/Publications/FlMa85.pdf),
designed to provide an extremely accurate and efficient
estimate of the number of unique words from a document that may contain repetitions.
This was an incredibly important algorithm, which introduced a **revolutionary idea**
at the time:

> The only assumption made is that records can be hashed in a suitably pseudo-uniform
> manner. This does not however appear to be a severe limitation since empirical
> studies on large industrial files [5] reveal that *careful* implementations of
> standard hashing techniques do achieve practically uniformity of hashed values.

The idea is that hash functions can "transform" data into pseudo-random variables.
Then a text can be treated as a sequence of random variables draw from a uniform
distribution, where a given word will always occur as the same random value (i.e.,
`a b c a a b c` could be hashed as `.00889 .31423 .70893 .00889 .00889 .31423 .70893` with
every occurrence of `a` hashing to the same value). While this sounds strange,
empirical evidence suggests it is not true enough in practice, and eventually [some
theoretical basis](https://people.seas.harvard.edu/~salil/research/streamhash-Jun10.pdf)
has come to support the practice.

The original *Probabilistic Counting* (1983) algorithm gave way to *LogLog* (2004),
and then eventually *HyperLogLog* (2007), one of the most famous algorithms in the
world as described in [this article](https://arxiv.org/abs/1805.00612). These algorithms
and others all used the same idea of hashing inputs to treat them as random variables,
and proved remarkably efficient and accurate.

But as highlighted in the above passage, it is important to be *careful*.

## Hash functions in practice

In practice, it is easy to use poor quality hash functions, or to use cryptographic
functions which will significantly slow down the speed (and relevance) of the
probabilistic estimates. However, on most data, some 
