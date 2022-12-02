# Work log

## 18.11.2022
- **Current Problem:** TermSet being too big to build matrix -> OutOfMemoryError
  - I need to reduce amount of data put in without losing quality of global termset
    - Splitting global termset in half is not an option because it's sorted alphabetically. We'd just lose half the ABC
    - Hence, we have to take half the models.

<br>

- **2nd Problem:** Finding unique terms was taking 6h + (even longer, we cut it after 6h)
  - I solved this by re-writing the sorting algorithm. We introduced a dictionary (hashmap) containing all the letters
    from the alphabet as keys & a List<String> as values. We now can look for duplicates in those sub-lists instead of
    iterating over the whole term-array each time.

## 01.12.2022
- **First implementation of the front-end is (mostly) done!**
  - To have somewhat of a service, I decided to implement the search engine as a SpringBoot MVC application.  I've
  implemented a simple interface that lets the user submit queries to the search engine and then shows a results page.

<br>

- **VSM is working slowly**
  - We got to the point where we implemented a simple vector-space-model (VSM) cosine-search. At the moment this search
    takes a lot of time (~ 15s - 18s for a query, depending on which machine the search engine is running on) and returns
    results which have nothing to do with the query at all. :-)
  
<br>

- **Switch to LSI**
  - Due to the classical approach of the VSM-cosine-search being slow (and faulty atm), I'll re-check the construction
    of my document-term-matrix and swap to the Latent Semantics Indexing approach.


## 02.12.2022
- **SVD is taking a lot of time**
  - Performing Singular Value Decomposition (SVD) on a matrix is taking up lots of time, e.g. multiple hours.
  - I can run this overnight, however I don't think the time will be enough.
  - Otherwise I'll have to reduce the data set again

<br>

- **Thymeleaf has problems with CSS styling**
  - No clue why - but it seems to be a common problem https://stackoverflow.com/questions/53203909/spring-boot-thymeleaf-css-is-not-applied-to-template