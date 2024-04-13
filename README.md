# LLRing
This repository contains the source code of the paper `LLRing: Logarithmic Linkable Ring Signatures with Transparent Setup` at the eprint link: [https://eprint.iacr.org/2024/421.pdf](https://eprint.iacr.org/2024/421.pdf).

`Note that the code is for research purpose only`.

There are four folders:
* `Bulletproofs`: Contain a inner product argument of the [Bulletproofs](https://eprint.iacr.org/2017/1066.pdf) and a segregated bulletproof in [LLRing](https://eprint.iacr.org/2024/421.pdf).
* `Uitls`: Contain common functions used by the rest of the package.
* `Dory`: Implement the non privacy-preserving and privacy-preserving inner product argument of the [Dory](https://eprint.iacr.org/2020/1274.pdf).
* `RingSignature`: Implement the LLRing linkable ring signature schemes, as well as a test.


To run the tests
------------------------
Navigate to `src/main/java/RingSignature/Test.java` .

Execute:

```
javac RingSignature/Test.java
java RingSignature/Test
```

