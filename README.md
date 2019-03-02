# Pinto

Pinto is a domain-specific language for working with time series. Programs in Pinto are written as expressions that produce tables of data. Each column in the table is a time series and the rows correspond to periodic points in time. Expressions are not tied to specific dates--they can be evaluated to produce a table with rows for any range of time. As a [concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language) language that uses [postfix](https://en.wikipedia.org/wiki/Reverse_Polish_notation) notation, Pinto expressions are comprised of a sequence of functions that operate in the order they are written. The expression is the mathematical composition of these functions. There are no variables in Pinto. All data is held as columns in the table. The table is set up as a stack, with new columns added to the right and functions operating on the rightmost column first. By using an indexer, the columns that are passed to a function can be filtered by header value or position in the table.

For language details please see the [Pinto Language Reference](./pinto_reference.md) and for code examples take a look at our [wiki](https://github.com/punkbrwstr/pinto/wiki).

## Key features

 - Expressive: The cleanliness of concatenative code with flexible indexing to tame the stack
 - Batteries included: Integrated market data, charting, rich library of statistical functions
 - Performant: Lazy evaluation, range-based caching for [nullary](https://en.wikipedia.org/wiki/Arity) functions 
 - Extensible: User-defined functions behave just like primitives
 - Interoperable: Accessible through an HTTP interface (works great with Python, R and SAS)

## Why Pinto?

Pinto was designed to prepare financial market data for use in models or visualizations. A Pinto expression can perform data transformations that would require many lines of Pandas operations, multiple SAS data steps, or an Excel sheet full of formulas. With Pinto's side-effect-free functional paradigm, the expression will return the same values every time. And, Pinto can reevaluate the same expression for a different frequency or an expand the range of dates without changes to the code.

## Code examples

[Getting started](https://github.com/punkbrwstr/pinto/wiki/Getting-started)

[Market data](https://github.com/punkbrwstr/pinto/wiki/Market-data)

## Try Pinto!
Try Pinto live [online](http://pinto.tech/)

## Requirements

The Pinto interpreter is built in Java. 

Running Pinto requires:

 - [Java 10](http://jdk.java.net/10/)
 
Building Pinto also requires:

 - [Maven](https://maven.apache.org/download.cgi)


## How to get up and running

The easiest way to get up and running with the Pinto console is to download a [release jar](https://github.com/punkbrwstr/pinto/releases) and run:

```
java -jar pinto-iex.jar
```

To build and run Pinto from source:
```
git clone https://github.com/punkbrwstr/pinto.git
cd pinto
mvn -pl pinto-lang compile
mvn exec:java@REPL -pl pinto-lang
```

## License

Copyright (c) 2019 Peter Graf

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
