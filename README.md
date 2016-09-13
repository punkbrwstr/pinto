# Pinto

Pinto is a stack-oriented programming language for manipulating time series
data that uses Reverse Polish notation.

Pinto was designed for financial time series, but it can be used with any
series that has a fixed periodicity.

## What can I do with Pinto?

Lots of stuff!  For example, to get stock prices from a popular online site try:

```
pinto> yhoo(aapl) yhoo(orcl) eval(2016-01-04,2016-01-08,B)
Evaluating: yhoo(aapl) yhoo(orcl) eval(2016-01-04,2016-01-08,B)
╔════════════╤════════════╤════════════╗
║ Date       │ yhoo(orcl) │ yhoo(aapl) ║
╠════════════╪════════════╪════════════╣
║ 2016-01-04 │ 35.75      │ 105.349998 ║
╟────────────┼────────────┼────────────╢
║ 2016-01-05 │ 35.639999  │ 102.709999 ║
╟────────────┼────────────┼────────────╢
║ 2016-01-06 │ 35.82      │ 100.699997 ║
╟────────────┼────────────┼────────────╢
║ 2016-01-07 │ 35.040001  │ 96.449997  ║
╟────────────┼────────────┼────────────╢
║ 2016-01-08 │ 34.650002  │ 96.959999  ║
╚════════════╧════════════╧════════════╝
```


## Pinto features

 - Define formulas for time series that can be evaluated over different ranges of dates and periodicities (frequencies)
 - Save series definitions and functions and refer to them in other formulas
 - Save defined functions that can be reused



## Requirements

The Pinto interpreter is built in Java using Maven. It requires:

 - [Java 8](https://java.com/download)
 - [Maven](https://maven.apache.org/download.cgi)


## How to get up and running

If you have the requirements, it's easy to get up and running with the pinto console:


```
git clone https://github.com/punkbrwstr/pinto.git
cd pinto
mvn compile
mvn exec:java -Dexec.mainClass="pinto.Console"
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
