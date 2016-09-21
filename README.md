# Pinto

Pinto is a stack-oriented programming language for manipulating time series
data that uses Reverse Polish notation.

Pinto was designed for financial time series, but it can be used with any
series that has a fixed periodicity.

## What can I do with Pinto?

For example, to get stock prices from a popular online site and compute moving averages for each:

```
pinto> yhoo(cmg,taco) copy(2) r_mean(20,B,2) label(cmg 20-day MA, taco 20-day MA) eval(2016-09-12,2016-09-13)
╔════════════╤════════╤═══════╤═══════════════╤════════════════╗
║ Date       │ cmg    │ taco  │ cmg 20-day MA │ taco 20-day MA ║
╠════════════╪════════╪═══════╪═══════════════╪════════════════╣
║ 2016-09-12 │ 428.89 │ 11.2  │ 410.71        │ 10.99          ║
╟────────────┼────────┼───────┼───────────────┼────────────────╢
║ 2016-09-13 │ 421.35 │ 11.02 │ 410.62        │ 10.99          ║
╚════════════╧════════╧═══════╧═══════════════╧════════════════╝
```

For more information see the [Pinto Language Reference](./pinto_reference.md)


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
