# Pinto Language Reference

## Types
Pinto is a dynamically typed language.  Columns have a consistent type over any time range.  All column types also have a string header.

- *double*: Floating point number values
- *doublearray*: One-dimensional array of floating point number values for each period
- *constdouble*: Constant floating point number value
- *conststring*: Constant character value

## Literals
Literals are values in a Pinto expression that are recognized based on their formatting and become columns on their own.

Type | Format
:--- | :---
*constdouble* | `3.0` or `3`
*conststring* | `"string value"`
*name* | `:a_name` (must appear at beginning of expression)

## Comments
Comments start with a `#` character and continue to the end of the line.  Comments cannot be within an expression--they must start after a terminal function (or at the beginning of a program).

## Headers
Pinto will automatically assign a string header to every column according to the functions that are composed together to define the column.  Column headers can also by set manually by using header literals.  Header literals are surrounded by curly braces.  Literals for multiple headers are separated by commas.  There are two formats:

#### Map-style header literals
Map-style header literals define the column and set the header value at the same time.  The header and column-defining Pinto expression are separated by a `:` with the header coming first `{ header : pinto expression }`.  If the expression defines more than one column all columns will have the header value.

Example:
```
pinto> { Column of ones: 1, Column of twos: 1 1 +  } eval
╔════════════╤════════════════╤════════════════╗
║ Date       │ Column of ones │ Column of twos ║
╠════════════╪════════════════╪════════════════╣
║ 2017-12-11 │ 1              │ 2              ║
╚════════════╧════════════════╧════════════════╝
```

#### List-style header literals
List-style header literals set the header for columns already on the stack with the last header literal in the list corresponding to the column at the top stack of the stack.

Example:
```
pinto> 1 2 {Onesy, Twosy} eval
╔════════════╤═══════╤═══════╗
║ Date       │ Onesy │ Twosy ║
╠════════════╪═══════╪═══════╣
║ 2017-12-11 │ 1     │ 2     ║
╚════════════╧═══════╧═══════╝
```

The two types of header literals may be combined within one set of curly braces.

## Indexing

An indexing operator determines which columns in the stack are passed to the following function.  They are also used when defining functions to specify which input columns the function requires.    

#### All columns
When no indexer is specified, the entire stack is passed to the following function.  To explicitly show that you want the whole stack you can also use the indexer ```[:]```.

#### No columns
An indexer to pass an empty stack looks like ```[]```.

#### Indexing by number
The indexer can take numerical indicies or ranges of numerical indicies (ala python indexing/slicing).  Index numbering starts with ```0```, which represents the top or rightmost column of the stack.  Ranges may specify an inclusive starting index, an exclusive ending index or both.  ```[0:3]```  represents the first through third columns in the stack.  ```[1:]``` represents all stack columns after the first.  Negative indicies are converted to the stack size minus that number.  ```[-2:]```  represents the last two columns in the stack.

#### Indexing by header
The indexer can also take string arguments to select stack columns by their header like ```[my_label2]```. Header indicies support ```*``` as a wildcard for matching zero or more characters (potentially returning multiple columns for one argument).

#### Multiple indicies
Lists of indicies are separated by commas.   ```[1,3]``` represents the second and fourth columns.   ```[1,3:]``` is the second and all columns after the third.  ```[1,pin*]``` is the second column and all columns with headers starting with "pin".

#### Index modifiers: Default
The Default modifier (```"="```) tells the indexer to use the following Pinto expression if the column is not found.  For instance, ```[guac="yes"]``` will select columns with the header "guac", or will return a constant string column of "yes".

#### Index modifiers: Copy
The Copy modifier (```"&"```) forces the indexer make a copy of the column for the subsequent function, leaving the original column on the stack.  For example, ```[:&]``` will make a copy of all columns in the stack for the following function and maintain the originals in the stack.

#### Index modifiers: Repeat
The repeat modifier (```"+"```) causes the indexer to make multiple calls to the following function until the stack no longer contains enough columns for the indexer.  The index ```[:2+]``` will make repeated calls to the following function, each time supplying the top two columns on the stack as the function inputs.  It will stop when there are fewer than two columns left on the stack. 


## Function reference

### Terminal functions

These functions tell the interpreter to start executing your Pinto code.  The most common is *eval* which evaluates the columns over the date range that can be specified by constant string with the headers: start, end, freq.  In console mode the resulting table is printed.

Function name | Default indexer |Description
:---:|:---|:---
def|[]|Defines the expression as the preceding name literal.
del|[]|Deletes name specified by the preceding name literal.
eval|[start="today",end="today",freq="B",:]|Evaluates the expression over the date range specified by *start, *end* and *freq* columns, returning the resulting table.
exec|[filename]|Executes pinto expressions contained in the specifed file *filename*.
help|[]|Prints help for the preceding name literal or all names if one has not been specified.
list|[]|Shows description for all names.
write|[filename, start="today",end="today",freq="B",:]|Evaluates the expression over the date range specified by *start, *end* and *freq* columns, exporting the resulting table to csv *filename*.

### Stack manipulation functions

These commands manipulate stack elements, but do not modify values.

Function name | Default indexer |Description
:---:|:---|:---
clear|[:]|Clears indexed columns from stack.
copy|[n=2,:]|Copies indexed columns *n* times.
only|[:]|Clears stack except for indexed columns.
rev|[:]|Reverses order of columns in stack.
roll|[n=1,:]|Permutes columns in stack *n* times.


### Data creation functions

These commands generate data values.

Function name | Default indexer|Description
:---:|:---|:---
moon|[]|Creates a double column with values corresponding the phase of the moon.
pi|[]|Creates a constant double column with the value pi.
range|[n=3]|Creates double columns corresponding to the first *n* positive integers.
read|[source,includes_header="true"]|Reads CSV formatted table from file or URL specified as *source*.

### Data cleaning functions

Function name | Default indexer |Description
:---:|:---|:---
fill|[lookback="true",freq="BQ-DEC",:]|Fills missing values with last good value, looking back one period of *freq* if *lookback* is true.
join|[dates,:]|Joins columns over time, switching between columns on dates supplied in ";" denominated list *dates*.
resample|[freq="BM",:]|Sets frequency of prior columns to periodicity *freq*, carrying values forward if evaluation periodicity is more frequent.

### Header functions

Function name | Default indexer |Description
:---:|:---|:---
hformat|[format,:]|Formats headers, setting new value to *format* and substituting and occurences of "{}" with previous header value.


### Binary double operators
Function name | Default indexer |Description
:---:|:---|:---
!=|[n=1,:]|Binary double operator != that operates on *n* columns at a time with fixed right-side operand.
%|[n=1,:]|Binary double operator % that operates on *n* columns at a time with fixed right-side operand.
*|[n=1,:]|Binary double operator * that operates on *n* columns at a time with fixed right-side operand.
+|[n=1,:]|Binary double operator + that operates on *n* columns at a time with fixed right-side operand.
-|[n=1,:]|Binary double operator - that operates on *n* columns at a time with fixed right-side operand.
/|[n=1,:]|Binary double operator / that operates on *n* columns at a time with fixed right-side operand.
<|[n=1,:]|Binary double operator < that operates on *n* columns at a time with fixed right-side operand.
<=|[n=1,:]|Binary double operator <= that operates on *n* columns at a time with fixed right-side operand.
==|[n=1,:]|Binary double operator == that operates on *n* columns at a time with fixed right-side operand.
>|[n=1,:]|Binary double operator > that operates on *n* columns at a time with fixed right-side operand.
>=|[n=1,:]|Binary double operator >= that operates on *n* columns at a time with fixed right-side operand.
^|[n=1,:]|Binary double operator ^ that operates on *n* columns at a time with fixed right-side operand.

### Unary double operators

Function name | Default indexer |Description
:---:|:---|:---
abs|[:]|Unary double operator for absolute value.
acgbPrice|[:]|Unary double operator for Australian bond futures price calculation.
acos|[:]|Unary double operator for arc cosine.
asin|[:]|Unary double operator for arc sine.
atan|[:]|Unary double operator for arc tangent.
cbrt|[:]|Unary double operator for cbrt.
ceil|[:]|Unary double operator for ceiling.
cos|[:]|Unary double operator for cosine.
cosh|[:]|Unary double operator for cosh.
exp|[:]|Unary double operator for e raised to the x.
expm1|[:]|Unary double operator for expm1.
floor|[:]|Unary double operator for floor.
inv|[:]|Unary double operator for inverse.
log|[:]|Unary double operator for natural log.
log10|[:]|Unary double operator for log base 10.
log1p|[:]|Unary double operator for log1p.
neg|[:]|Unary double operator for additive inverse.
nextDown|[:]|Unary double operator for nextDown.
nextUp|[:]|Unary double operator for nextUp.
rint|[:]|Unary double operator for rint.
signum|[:]|Unary double operator for signum.
sin|[:]|Unary double operator for sine.
sinh|[:]|Unary double operator for sinh.
sqrt|[:]|Unary double operator for square root.
tan|[:]|Unary double operator for tangent.
tanh|[:]|Unary double operator for tanh.
toDegrees|[:]|Unary double operator to convert to degrees.
toRadians|[:]|Unary double operator to convert to radians.
ulp|[:]|Unary double operator for ulp.

### Double array creation functions
Function name | Default indexer |Description
:---:|:---|:---
cross|[:]|Creates a double array column with each row containing values of input columns.
expanding|[start="range",freq="range",initial_zero="false",:]|Creates double array columns for each input column with rows containing values from an expanding window of past data with periodicity *freq* that starts on date *start*.
rolling|[size=2,freq="B",:]|Creates double array columns for each input column with rows containing values from rolling window of past data where the window is *size* periods of periodicity *freq*.

### Double array aggregators (double array to double functions)
Function name | Default indexer |Description
:---:|:---|:---
average|[:]|Aggregates row values in double array columns to a double value by average.
change|[:]|Aggregates row values in double array columns to a double value by change.
changelog|[:]|Aggregates row values in double array columns to a double value by change of logs.
changepct|[:]|Aggregates row values in double array columns to a double value by change in percent.
first|[:]|Aggregates row values in double array columns to a double value by first value.
geomean|[:]|Aggregates row values in double array columns to a double value by geometric mean.
last|[:]|Aggregates row values in double array columns to a double value by last value.
max|[:]|Aggregates row values in double array columns to a double value by max.
min|[:]|Aggregates row values in double array columns to a double value by min.
stdev|[:]|Aggregates row values in double array columns to a double value by standard deviation.
stdevp|[:]|Aggregates row values in double array columns to a double value by population standard deviation.
sum|[:]|Aggregates row values in double array columns to a double value by sum.
var|[:]|Aggregates row values in double array columns to a double value by variance.
varp|[:]|Aggregates row values in double array columns to a double value by population variance.
zscore|[:]|Aggregates row values in double array columns to a double value by zscore.
zscorep|[:]|Aggregates row values in double array columns to a double value by zscorep.

### Extra functions (requires additional libraries, see wiki)

Function name | Default indexer |Description
:---:|:---|:---
bbg|[tickers,fields="PX_LAST"]|Downloads Bloomberg history for each *fields* for each *tickers*
chart|[start="today",end="today",freq="B",title="none",:]|Creates a const string column with code for an HTML chart.
report|[title="Pinto report",HTML]|Creates a new HTML report containing all *HTML* columns



