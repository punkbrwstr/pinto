# Pinto

Pinto is a domain-specific programming language for creating tables of time series data. Technically it is a [stack-based](https://en.wikipedia.org/wiki/Stack-oriented_programming_language) [functional](https://en.wikipedia.org/wiki/Functional_programming) (a/k/a [concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language)) language that uses [Reverse Polish](https://en.wikipedia.org/wiki/Reverse_Polish_notation) (a/k/a postfix) notation. Unlike general-purpose programming languages, Pinto has only one type of data: a column.  A Pinto column has a text header for its first row and number (double precision floating point) values for every subsequent row. Expressions written in Pinto create a table of columns with number value rows corresponding to a date range that you specify.  Expressions can be evaluated over any range of dates and for any frequency.

Pinto is the hybrid of Excel, pandas, and an HP12C calculator that you have been looking for!

## How does it work?

Every element of a Pinto expression is a function.  These functions take columns as inputs (zero or more) and return columns as outputs (also could be zero or more).  The input and output columns are stored in a [stack](https://en.wikipedia.org/wiki/Stack_(abstract_data_type))*, an ordered collection that operates on last-in-first-out basis.  When evaluated, expressions return a table comprised of the columns that are in the stack after all of the functions are applied.

Here's a simple example of a Pinto expression broken down into numbered steps:

![Alt text](https://pinto.tech/files/diag.png "2 3 +")

A more advanced example computes a moving average for a stock prcie:

![Alt text](https://pinto.tech/files/diag2.png "CMG 200 r_mean")

For language details see the [Pinto Language Reference](./pinto_reference.md).  For examples and additional information see the  [Pinto Wiki](./wiki).


## Try Pinto!
Try Pinto live [online](http://pinto.tech/)


## Key features

 - Concise: One line of pinto code can define an entire table of data
 - Updateable: Automatically update tables over any date range or periodicity 
 - Extensible: Build reusable functions that define specific data or reusable transformations
 - Interoperable: Pinto is accessible through an http interface (works great with python/pandas)
 - Batteries included: Functions for rolling/expanding/cross window statistics, Bloomberg interface, etc.
 - Efficient: Lazy evaluation, range-based caching for supplier ([nullary](https://en.wikipedia.org/wiki/Arity)) functions


## Requirements

The Pinto interpreter is built in Java using Maven. It requires:

 - [Java 8](https://java.com/download)
 - [Maven](https://maven.apache.org/download.cgi)


## How to get up and running locally

If you have the requirements, it's easy to get up and running with the Rinto console:


```
git clone https://github.com/punkbrwstr/pinto.git
cd pinto
mvn -pl pinto-lang compile
mvn exec:java@REPL -pl pinto-lang
```

## *There is no spoon

(There never is any data on the stack it only used to compose the functions)

## License

Copyright (c) 2016 Peter Graf

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
