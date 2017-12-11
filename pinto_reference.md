# Pinto Language Reference

## Types
Columns in Pinto are statically typed.  For any requested periodic time range a column will have values that are of the same type.  All column types also have a string header.

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

## Indexing

An indexing operator determines which columns in the stack are passed to the following function.  

#### All columns
When no indexer is specified, the entire stack is passed to the following function.  To explicitly show that you want the whole stack you can also use the indexer ```[:]```.

#### Indexing by number
The indexer can take numerical indicies or ranges of numerical indicies (ala python indexing/slicing).  Index numbering starts with ```0```, which represents the top or rightmost column of the stack.  Ranges may specify an inclusive starting index, an exclusive ending index or both.  ```[0:3]```  represents the first through third columns in the stack.  ```[1:]``` represents all stack columns after the first.  Negative indicies are converted to the stack size minus that number.  ```[-2:]```  represents the last two columns in the stack.

#### Indexing by header
The indexer can also take string arguments to select stack columns by their header like ```[my_label2]```. Header indicies support ```*``` as a wildcard for matching zero or more characters (potentially returning multiple columns for one argument).

#### Multiple indicies
Lists of indicies are separated by commas.   ```[1,3]``` represents the second and fourth columns.   ```[1,3:]``` is the second and all columns after the third.  ```[1,pin*]``` is the second column and all columns with headers starting with "pin".

#### Index modifiers: Or
The Or modifier (```"|"```) tells the indexer to use the following Pinto expression if the column is not found.  For instance, ```[guac|"yes"]``` will select columns with the header "guac", or will return a constant string column of "yes".

#### Index modifiers: Copy
The Copy modifier (```"&"```) forces the indexer make a copy of the column for the subsequent function, leaving the original column on the stack.  For example, ```[:&]``` will make a copy of all columns in the stack for the following function and maintain the originals in the stack.

#### Index modifiers: Repeat
The repeat modifier (```"+"```) causes the indexer to make multiple calls to the following function until the stack no longer contains enough columns for the indexer.  The index ```[:2+]``` will make repeated calls to the following function, each time supplying the top two columns on the stack as the function inputs.  It will stop when there are fewer than two columns left on the stack. 



## Function parameters

Parameters are special inputs to functions that modify how the function operates.  They only use the header of a column and discard the number values.  Parameters may be supplied by position (by being at the top of the stack) or by name (by having a header that starts with ```parametername=```).  As an example, to supply a file name to the read function you could add a header-only column to the top of the stack ```"/tmp/my_file_name" read``` or you could specify the parameter by name ```"source=/tmp/my_file_name" read```.  Multiple parameters may be specified as separate columns on the stack (```"tickers=TACO Equity" "fields=PX_LAST" bbg```) or within one column header by delimiting with ```;``` (```"tickers=TACO Equity;fields=PX_LAST" bbg```).


## Function reference

### Terminal functions

These functions tell the interpreter to start executing your Pinto code.  The most common is *eval* which evaluates the columns over the date range that can be specified by constant string with the headers: start, end, freq.  In console mode the resulting table is printed.

Function name | Parameters |Description
    :---:|:---|:---
def|[x]|Defines the expression as the preceding name literal.
del|[x]|Deletes name specified by the preceding name literal.
eval|[start="today",end="today",freq="B",:]|Evaluates the expression over the date range specified by *start, *end* and *freq* columns, returning the resulting table.
exec|[filename]|Executes pinto expressions contained in the specifed file *filename*.
help|[x]|Prints help for the preceding name literal or all names if one has not been specified.
list|[x]|Shows description for all names.

### Stack manipulation functions

These commands manipulate stack elements, but do not modify values.

Function name | Parameters |Description
:---:|:---|:---
clear|[:]|Clears indexed columns from stack.
copy|[n=2,:]|Copies indexed columns *n* times.
only|[:]|Clears stack except for indexed columns.
rev|[:]|Reverses order of columns in stack.
roll|[n=1,:]|Permutes columns in stack *n* times.


### Data creation functions

These commands generate data values.

Function name | Parameters |Description
:---:|:---|:---
moon|[x]|Creates a double column with values corresponding the phase of the moon.
pi|[x]|Creates a constant double column with the value pi.
range|[n=3]|Creates double columns corresponding to the first *n* positive integers.
read|[source,includes_header="true"]|Reads CSV formatted table from file or URL specified as *source*.

### Data cleaning functions

Function name | Parameters |Description
:---:|:---|:---
fill|[lookback="true",freq="BQ-DEC",:]|Fills missing values with last good value, looking back one period of *freq* if *lookback* is true.
join|[dates,:]|Joins columns over time, switching between columns on dates supplied in ";" denominated list *dates*.
resample|[freq="BM",:]|Sets frequency of prior columns to periodicity *freq*, carrying values forward if evaluation periodicity is more frequent.

### Header functions

Function name | Parameters |Description
:---:|:---|:---
hformat|[format,:]|Formats headers, setting new value to *format* and substituting and occurences of "{}" with previous header value.


### Binary double operators
Function name | Parameters |Description
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

Function name | Parameters |Description
:---:|:---|:---
abs|[:]|Unary double operator abs
acgbPrice|[:]|Unary double operator acgbPrice
acos|[:]|Unary double operator acos
asin|[:]|Unary double operator asin
atan|[:]|Unary double operator atan
cbrt|[:]|Unary double operator cbrt
ceil|[:]|Unary double operator ceil
cos|[:]|Unary double operator cos
cosh|[:]|Unary double operator cosh
exp|[:]|Unary double operator exp
expm1|[:]|Unary double operator expm1
floor|[:]|Unary double operator floor
inv|[:]|Unary double operator inv
log|[:]|Unary double operator log
log10|[:]|Unary double operator log10
log1p|[:]|Unary double operator log1p
neg|[:]|Unary double operator neg
nextDown|[:]|Unary double operator nextDown
nextUp|[:]|Unary double operator nextUp
rint|[:]|Unary double operator rint
signum|[:]|Unary double operator signum
sin|[:]|Unary double operator sin
sinh|[:]|Unary double operator sinh
sqrt|[:]|Unary double operator sqrt
tan|[:]|Unary double operator tan
tanh|[:]|Unary double operator tanh
toDegrees|[:]|Unary double operator toDegrees
toRadians|[:]|Unary double operator toRadians
ulp|[:]|Unary double operator ulp

### Double array creation functions
Function name | Parameters |Description
:---:|:---|:---
cross|[:]|Creates a double array column with each row containing values of input columns.
expanding|[start="range",freq="range",initial_zero="false",:]|Creates double array columns for each input column with rows containing values from an expanding window of past data with periodicity *freq* that starts on date *start*.
rolling|[size=2,freq="B",:]|Creates double array columns for each input column with rows containing values from rolling window of past data where the window is *size* periods of periodicity *freq*.

### Double array aggregators (double array to double functions)
Function name | Parameters |Description
:---:|:---|:---
average|[:]|Aggregates row values in double array columns to a double value by average
change|[:]|Aggregates row values in double array columns to a double value by change
changelog|[:]|Aggregates row values in double array columns to a double value by changelog
changepct|[:]|Aggregates row values in double array columns to a double value by changepct
first|[:]|Aggregates row values in double array columns to a double value by first
geomean|[:]|Aggregates row values in double array columns to a double value by geomean
last|[:]|Aggregates row values in double array columns to a double value by last
max|[:]|Aggregates row values in double array columns to a double value by max
min|[:]|Aggregates row values in double array columns to a double value by min
stdev|[:]|Aggregates row values in double array columns to a double value by stdev
stdevp|[:]|Aggregates row values in double array columns to a double value by stdevp
sum|[:]|Aggregates row values in double array columns to a double value by sum
var|[:]|Aggregates row values in double array columns to a double value by var
varp|[:]|Aggregates row values in double array columns to a double value by varp
write|[filename, start="today",end="today",freq="B",:]|Evaluates the expression over the date range specified by *start, *end* and *freq* columns, exporting the resulting table to csv *filename*.
zscore|[:]|Aggregates row values in double array columns to a double value by zscore
zscorep|[:]|Aggregates row values in double array columns to a double value by zscorep

### Extra functions (requires additional libraries, see wiki)

Function name | Parameters |Description
:---:|:---|:---
bbg|[tickers,fields="PX_LAST"]|Downloads Bloomberg history for each *fields* for each *tickers*
chart|[start="today",end="today",freq="B",title="none",:]|Creates a const string column with code for an HTML chart.
report|[title="Pinto report",HTML]|Creates a new HTML report containing all *HTML* columns



