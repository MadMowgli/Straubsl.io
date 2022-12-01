# Notes

## TermSet
- TermSet.writeGlobalTermSet() can't use DataOutputStream.writeUTF(), because we not only have UTF in the data set

## Document by Term matrix
- We can't use the whole dataset, because we're getting a java.lang.OutOfMemoryError when creating a matrix (double[][])
    with the required dimensionality.
- Hence, reduce the data set
  - Did so by introducing WETReader.toEurModelArray()
  - Further enhancement: reduce "duplicates" by removing non-alphabetic characters (e.g.: .,'?![]|) & making everything lowercase
    - BUT: This means the queries have to be treated the same!
    - Crazy - this reduced the size of the global termset from 107Kb to 53Kb
