# Pinto

Pinto is a stack-based or concatenative, functional programming language that uses Reverse Polish (postfix) notation. It is specialized for numeric time series data.  Programs written in Pinto are one-liners that define the manipulations necessary to create a table of data.  These expressions can produce values for any range of dates and for any frequency.  

With Pinto, you can encapsulate an Excel spreadsheet, multiple regression equation, or an algorithmic trading system into one line of code!

## What can I do with it?

Calculate the Monthly YTD return of an evenly-weighted portfolio of two stocks:

```
pinto> yhoo(aapl,ibm) fill r_chgpct x_mean 1 + log e_sum(2016-01-01) exp -1 + label(YTD return) eval(2016-01-01,2016-03-30,BM)
╔════════════╤══════════════╗
║ Date       │ YTD return   ║
╠════════════╪══════════════╣
║ 2016-01-29 │ -0.08423500  ║
╟────────────┼──────────────╢
║ 2016-02-29 │ -0.06439658  ║
╟────────────┼──────────────╢
║ 2016-03-31 │ 0.068015989  ║
╚════════════╧══════════════╝
```

Or compute daily differences between the 20 and 200-day moving averages for two stocks:

```
pinto> yhoo(cmg,taco) copy(3) [-2:] r_mean(20) [2:3] r_mean(200) [3,1] - [1:2] - [:1] label(taco MA diff,cmg MA diff) eval(2016-09-26,2016-09-28)
╔════════════╤═══════╤════════════╤════════════════════╤═════════════════════╗
║ Date       │ taco  │ cmg        │ taco MA diff       │ cmg MA diff         ║
╠════════════╪═══════╪════════════╪════════════════════╪═════════════════════╣
║ 2016-09-26 │ 11.71 │ 419.880005 │ 1.28143914473684   │ -26.278440367050678 ║
╟────────────┼───────┼────────────┼────────────────────┼─────────────────────╢
║ 2016-09-27 │ 11.73 │ 418.950012 │ 1.2977686403508741 │ -25.6843770603071   ║
╟────────────┼───────┼────────────┼────────────────────┼─────────────────────╢
║ 2016-09-28 │ 11.79 │ 418.309998 │ 1.3223985745614009 │ -25.030981295230447 ║
╚════════════╧═══════╧════════════╧════════════════════╧═════════════════════╝
```



For more information see the [Pinto Language Reference](./pinto_reference.md)

## Key features

 - Concise: One line of pinto code can define an entire table of data
 - Updateable: Recalculate over any date range or periodicity 
 - Extensible: Build on other expressions that define specific data, or reusable transformation functions
 - Interoperable: Pinto is accessible through an http interface (works great with python/pandas)
 - Batteries included: Functions for rolling/expanding/cross window statistics, Bloomberg interface, etc.
 - Efficient: Lazy evaluation, range-based caching for supplier functions

## How does it work?

Pinto expressions are comprised of a sequence of functions.  Expressions are evaluated left-to-right, with the inputs for each function coming from the outputs of the functions to its left.  It is useful to think of the execution of a Pinto expression in terms of a common stack of data.  Each function takes its inputs from the stack, operates on them, and returns them to the stack.  In mathematical terms, a Pinto expression can be thought of as a compositions of functions:  The Pinto expression x f g is equivalent to g(f(x)).  In finance terms, it works like an HP12C calculator.

Pinto functions may have multiple inputs and outputs.  By default, all function are variadic, meaning they will accept whatever number of inputs are on the stack when they get called.  (Some functions may have a minimum number of inputs).  An index/slice expression before a function limits the inputs for that function to a certain portion of the stack.  The indexing syntax will look familar to Pythonistas.

Pinto functions may optionally take non-data arguments in parentheses after their name that modify how the function operates.  For example, the copy function takes a numeric argument that tells it how many copies of its input data to make.

Pinto functions can be broadly divided into three types: supplier, intermediate and terminal.  Suppliers take no inputs, but return output data.  For example, a function that retrieves online stock closing prices is a supplier.  Intermediate operations take inputs and return outputs.  They may modify their input data or manipulate the composition of the stack.  Terminal functions initiate the evaluation of the expression.  Suppliers and intermediate functions are lazy--they don't perform their operation until a subsequent terminal function tells them to (and specifies the date range over which to operate).  

Any Pinto expression may be saved as a named function.  Named functions may return output data without any inputs (suppliers), may be a tacit function that require inputs from the stack, or may have some saved ("curried") inputs and take some others from the stack. 


## Requirements

The Pinto interpreter is built in Java using Maven. It requires:

 - [Java 8](https://java.com/download)
 - [Maven](https://maven.apache.org/download.cgi)


## How to get up and running

If you have the requirements, it's easy to get up and running with the pinto console:


```
git clone https://github.com/punkbrwstr/pinto.git
cd pinto
mvn -pl pinto-lang compile
mvn exec:java -pl pinto-lang
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
