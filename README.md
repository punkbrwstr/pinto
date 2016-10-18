# Pinto

Pinto is a programming language for manipulating time series data. Technically, it is a stack-based or concatenative, functional programming language that uses Reverse Polish notation (operators come after operands, like an HP12c calculator). Expressions written in Pinto define the manipulations necessary to create a table of data. (Pinto programs are called expressions and are almost always one-liners). Pinto expressions can be evaluated over any range of dates and for any frequency.

With Pinto, you can encapsulate an Excel spreadsheet, multiple regression equation, or an algorithmic trading system into one line of code!

## What can I do with it?

Calculate the Monthly YTD return of an evenly-weighted portfolio of two stocks:

![alt text](http://pinto.tech/files/example0.png "Stock portfolio example")

Or find when 20 and 200-day moving averages cross for two stocks:

![alt text](http://pinto.tech/files/example1.png "Stock MA cross")



For language details see the [Pinto Language Reference](./pinto_reference.md).  For examples and additional information see the  [Pinto Wiki](./wiki).


## Try Pinto!
Try Pinto live [online](http://pinto.tech/)


## Key features

 - Concise: One line of pinto code can define an entire table of data
 - Updateable: Recalculate over any date range or periodicity 
 - Extensible: Build on other expressions that define specific data, or reusable transformation functions
 - Interoperable: Pinto is accessible through an http interface (works great with python/pandas)
 - Batteries included: Functions for rolling/expanding/cross window statistics, Bloomberg interface, etc.
 - Efficient: Lazy evaluation, range-based caching for supplier functions

## How does it work?

Pinto expressions are comprised of a sequence of functions.  Expressions are evaluated left-to-right, with the inputs for each function coming from the outputs of the functions to its left.  It is useful to think of the execution of a Pinto expression in terms of a common stack of data.  Each function takes its inputs from the stack, operates on them, and returns them to the stack.  In mathematical terms, a Pinto expression can be thought of as a compositions of functions:  The Pinto expression x f g is equivalent to g(f(x)).  In finance terms, it works like an HP12c calculator.

Pinto functions may have multiple inputs and outputs.  By default, all function are variadic, meaning they will accept whatever number of inputs are on the stack when they get called.  (Some functions may have a minimum number of inputs).  An index/slice expression before a function limits the inputs for that function to a certain portion of the stack.  The indexing syntax will look familar to Pythonistas.

Pinto functions may optionally take non-data arguments in parentheses after their name that modify how the function operates.  For example, the copy function takes a numeric argument that tells it how many copies of its input data to make.

Pinto functions can be broadly divided into three types: supplier, intermediate and terminal.  Suppliers take no inputs, but return output data.  For example, a function that retrieves online stock closing prices is a supplier.  Intermediate operations take inputs and return outputs.  They may modify their input data or manipulate the composition of the stack.  Terminal functions initiate the evaluation of the expression.  Suppliers and intermediate functions are lazy--they don't perform their operation until a subsequent terminal function tells them to (and specifies the date range over which to operate).  

Any Pinto expression may be saved as a named function.  Named functions may return output data without any inputs (suppliers), may be a tacit function that require inputs from the stack, or may have some saved ("curried") inputs and take some others from the stack. 


## Requirements

The Pinto interpreter is built in Java using Maven. It requires:

 - [Java 8](https://java.com/download)
 - [Maven](https://maven.apache.org/download.cgi)


## How to get up and running locally

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
