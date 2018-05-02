# Pinto

Pinto is a domain-specific programming language for working with time series data. Pinto expressions produce one thing: tables of data with rows that correspond to periodic points in time.  Each table column contains a consistent type of data, but the values could be constant or time-varying.  Columns also have a text header for identifying metadata.  Pinto expressions can be evaluated to produce values for any periodicity over any range of time.  As a [concatenative](https://en.wikipedia.org/wiki/Concatenative_programming_language) language that uses [postfix](https://en.wikipedia.org/wiki/Reverse_Polish_notation) notation, Pinto expressions are comprised of a sequence of functions that operate on the the same table in left-to-right order.  The table is set up as a [stack](https://en.wikipedia.org/wiki/Stack_(abstract_data_type)), with new columns added to the right and functions operating on the rightmost column first.  Pinto indexing expressions allow convenient filtering of the table--passing only selected columns to the next function by referencing a column's header value or position in the table.   

For language details please see the [Pinto Language Reference](./pinto_reference.md) and for code examples take a look at our [wiki](https://github.com/punkbrwstr/pinto/wiki).

## Key features

 - Expressive: Concise code using recognizable symbols and efficient postfix notation
 - Batteries included: Integrated Bloomberg or IEX data, charting, rich library of statistical functions
 - Performant: Lazy evaluation, range-based caching for [nullary](https://en.wikipedia.org/wiki/Arity) functions 
 - Extensible: User-defined functions behave just like primitives
 - Interoperable: Accessible through an HTTP interface (works great with Python or SAS)

## Why Pinto?

Pinto was designed to prepare financial market data for use in models or visualizations.  A Pinto one-liner can perform data transformations that would require many lines of Pandas operations, multiple SAS data steps, or an Excel sheet full of formulas.  With Pinto's side-effect-free functional paradigm, the expression will return the same values every time.  And, Pinto can reevaluate the same expression for a different frequency or an expand the range of dates without changes to the code.  

Pinto combines the efficiency of an HP-12c calculator, the simplicity of Postscript, the tersity of J, and the familiarity of Python indexing.   

## Try Pinto!
Try Pinto live [online](http://pinto.tech/)

## Requirements

The Pinto interpreter is built in Java. 

Running Pinto requires:

 - [Java 8+](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 
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
