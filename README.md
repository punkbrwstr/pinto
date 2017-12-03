# Pinto

Pinto is a domain-specific programming language for manipulating time series. Technically it is a [concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language) language that uses [postfix](https://en.wikipedia.org/wiki/Reverse_Polish_notation) notation. That means that Pinto programs are comprised of a sequence of funcations, written left-to-right, where each function's inputs are the outputs of the previous function (to its left).  These inputs and outputs are in the form of a table with columns representing specific time series definitions and a varying number of rows that correspond to a requested date range.  The rows may contain different types of data with values that vary over time or are constant.  Each column also has a text header to identify it. The table is set up as a [stack](https://en.wikipedia.org/wiki/Stack_(abstract_data_type)), with  new columns added to the right and functions operating on the rightmost column first.


## How does it work?

Here's a simple example of a Pinto expression broken down into numbered steps:

![Alt text](https://pinto.tech/files/diag.png "2 3 +")

For language details see the [Pinto Language Reference](./pinto_reference.md).


## Try Pinto!
Try Pinto live [online](http://pinto.tech/)


## Key features

 - Concise: One line of pinto code can define an entire table of data
 - Updateable: Automatically update tables over any date range or periodicity 
 - Extensible: Build reusable functions that define specific data or transformations
 - Interoperable: Pinto is accessible through an http interface (works great with python or SAS)
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
