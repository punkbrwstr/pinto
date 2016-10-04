# Pinto

Pinto is a domain-specific, stack-based or concatenative language.  Pinto expressions (programs) define the manipulations necessary to create one or more time series of floating-point numeric data.  A Pinto expression can produce values for any range of dates and for any periodicity (frequency).  Pinto was designed for financial time series, but it can be used to create any series that has a fixed periodicity.   With Pinto, you can encapsulate an entire Excel spreadsheet, or a multiple regression equation, or an algorithmic trading system into one line of code!

## Key features

 - Concise: One line of pinto code can define an entire table of data
 - Updateable: Automatically recalculate for any date range or periodicity 
 - Extensible: Build on other expression that define specific data, or transformation functions
 - More extensible: Additional functions can be defined in Java 
 - Interoperable: Pinto is accessible through an http interface from other languages (python/pandas, etc.)
 - Batteries included:  Functions

## How does it work?

Pinto expressions are comprised of a sequence of functions.  Expressions are evaluated left-to-right, with the inputs for each function coming from the outputs of the functions to its left.  It is useful to think of the execution of a Pinto expression in terms of a common stack of data.  Each function takes its inputs from the stack, operates on them, and returns them to the stack.  In mathematical terms, a Pinto expression can be thought of as a compositions of functions:  The Pinto expression x f g is equivalent to g(f(x)).

Pinto functions may have multiple inputs and outputs.  By default, all function are variadic, meaning they will accept whatever number of inputs are on the stack when they get called.  (Some functions may have a minimum number of inputs).  An index/slice expression before a function limits the inputs for that function to a certain portion of the stack.  The indexing syntax will look familar to anyone who knows Python.

Pinto functions may optionally take non-data arguments in parentheses after their name that modify how the function operates.  For example, the copy function takes a numeric argument that tells it how many copies of its input data to make.

Pinto functions can be broadly divided into three types: supplier, intermediate and terminal.  Suppliers take no inputs, but return output data.  For example, a function that retrieves online stock closing prices is a supplier.  Intermediate operations take inputs and return outputs.  They may modify their input data or manipulate the composition if the stack.  Terminal functions initiate the evaluation of the expression.  Suppliers and intermediate functions are lazy--they don't perform their operation until a subsequent terminal function tells them to (and specifies the date range over which to operate).  

Any Pinto expression may be saved as a named function.  Named functions may return output data without any inputs (suppliers), or may be a tacit function that require inputs from the stack, or may have some saved ("curried") inputs and take some others from the stack. 

## What can I do it?

Get online stock prices and compute moving averages for each:

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
mvn exec:java -pl pinto-lang -Dexec.mainClass=tech.pinto.Console
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
