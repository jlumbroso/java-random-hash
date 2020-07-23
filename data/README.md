# Example Dataset

For the purposes of testing the randomness of the hash functions, this is a book, a French copy of Jules Vernes' *Vingt mille lieues sous les mers*, obtained from [Project Gutenberg](http://www.gutenberg.org/ebooks/54873).

## Description of Each File

- `original.txt` is the original French, plain text version, exactly as downloaded Project Gutenberg.
    ```text
    Project Gutenberg's Vingt mille lieues sous les mers, by Jules Verne
    [...]
      VINGT MILLE LIEUES
      SOUS
      LES MERS




      —LES VOYAGES EXTRAORDINAIRES—


      [Illustration]


      —J. HETZEL, ÉDITEUR—
    [...]
    ```

- `normalized.txt` is the result of a normalization process, wherein:
    1. all punctuation, numbers and other non-alphabetical characters are stripped;
    2. all letters are changed to lowercase;
    3. each line contains a single word.
    ```text
    vingt
    mille
    lieues
    sous
    les
    mers
    les
    voyages
    extraordinaires
    illustration
    j
    hetzel
    editeur
    [...]
    ```
- `unique.txt` is the result of a sorted and filtering process wherein only one occurrence of each word from `normalized.txt` is preserved; and to do so efficiently, the text is first sorted:
    ```text
    a
    abaissa
    abaissant
    abaisse
    abaissee
    abandon
    abandonna
    abandonnaient
    abandonnait
    abandonne
    abandonnee
    abandonner
    abandonnerent
    abandonnes
    abandonnez
    abasourdi
    abattant
    [...]
    ```

## Processing Steps

This is how the data files above were generated:

```shell
wget http://www.gutenberg.org/files/54873/54873-0.txt -O original.txt
./normalizer original.txt 56 > normalized.txt
sort -u normalized.txt > unique.txt
```
