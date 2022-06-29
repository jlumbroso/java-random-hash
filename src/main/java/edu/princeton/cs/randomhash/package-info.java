
/**
 * The {@link edu.princeton.cs.randomhash} package provides an easy-to-use,
 * drop-in library to generate large families of pseudo-random hash functions,
 * with excellent performance and uniformity.<br/>
 * 
 * The implementation relies on the CRC32 algorithm for the base hash function,
 * and affine transformations using an arbitrary coprime numbers, generated
 * with the Mersenne Twister, to generate the family of pseudo-random hash
 * functions from the base hash function. <br/>
 * 
 * The class {@link edu.princeton.cs.randomhash.RandomHashFamily} can define
 * a family of pseudo-random hash functions of arbitrary size.
 * 
 * The class {@link edu.princeton.cs.randomhash.UniformAudit} is both an
 * example client for this class, and an empirical benchmark for the
 * randomness/uniformity of the values produced by these pseudo-random hash
 * functions.<br/>
 * 
 * This family of hash functions was first used by Philippe Flajolet when
 * he invented the data streaming algorithm, Probabilistic Counting (1983), and
 * later in other algorithms such as Adaptive Sampling/Distinct Sampling (1990),
 * LogLog (2000), and HyperLogLog (2012), to name only a few.<br/>
 * <br/>
 * 
 * There is a companion library for Python, available from:
 * <a href=
 * "https://github.com/jlumbroso/python-random-hash">{@code randomhash}</a> and
 * PyPi.
 *
 * @author Jérémie Lumbroso
 * @version 1.1.1
 */
package edu.princeton.cs.randomhash;