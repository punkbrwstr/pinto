# Pinto Language Reference

## Types
Pinto is a dynamically typed language.  Columns have a consistent type over any time range.  All column types also have a string header.

- *double*: Floating point number values
- *window*: Views of multiple double values (e.g. rolling historical window of values)
- *constant double*: Constant floating point number values
- *constant string*: Constant string values
- *constant date*: Constant date values
- *constant periodicity*: Constant periodicity values 

## Literals
Literals are values in a Pinto expression that are recognized based on their formatting and become columns on their own.

Type | Format example
:--- | :---
*double* | `3.0` or `3`
*string* | `"string value"`
*name* | `:a_name` (must appear at beginning of expression) 
*date* | `2018-01-01`
*periodicities* | `{B, W-MON, W-TUE, W-WED, W-THU, W-FRI, BM, BQ-DEC, BA-DEC}` for business days, weekly ending each day of the week, business month end, business quarter (ending Mar, Jun, Sep, Dec) and business year end (ending Dec)
*market data* | `$TACO Equity,CMG Equity:PX_OPEN,PX_LAST$`


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
║ 2018-05-14 │ 1              │ 2              ║
╚════════════╧════════════════╧════════════════╝
```

#### List-style header literals
List-style header literals set the header for columns already on the stack with the last header literal in the list corresponding to the column at the top stack of the stack.

Example:
```
pinto> 1 2 {Ones, Twos} eval
╔════════════╤══════╤══════╗
║ Date       │ Ones │ Twos ║
╠════════════╪══════╪══════╣
║ 2018-05-14 │ 1    │ 2    ║
╚════════════╧══════╧══════╝
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

## Comments
Comments start with a `#` character and continue to the end of the line.  Comments cannot be within an expression--they must start after a terminal function (or at the beginning of a program).

## User-defined functions
Users can define functions that behave identically to built-in functions.  Any references to other names within a user-defined function  will refer to the version of the referenced name that is currently defined at time of evaluation.

#### Defining functions
Function definitions have four parts as in the following example:
````
:my_function [:2] 1 + def
````
Example | Part | Description
:--- | :--- | :---
`:my_function` | *name literal*|  This will be the name for the new function.
`[:2]` | *default indexer* | Optional indexer (default: `[]`) that will determine which columns on the stack will passed to the function.  Any header-based indices that cannot be found in on the stack and do not have defaults will cause an error.
`1 +` | *function body* | This expression will be evaluated when the user-defined function is called.
`def` | *terminal* | The terminal function `def` finishes a function definition.

#### Deleting functions
User-defined functions can be deleted by specifying a *name literal* and calling the `del` function as in this example:
```
:my_function undef
```

## In-line functions
A in-line function is a portion of an expression during which the stack remains indexed by a given indexer.  They allow multiple functions to be applied to the same indexed stack.  In-line functions are demarcated by parenthesis.  

In the following example the `1 +` and `2 *` functions are applied to the stack indexed by `[0]` (containing *constant double* `3`).
```
pinto> 1 2 3 ([0] 1 + 2 *) eval
╔════════════╤═══╤═══╤═══════════╗
║ Date       │ c │ c │ c c + c * ║
╠════════════╪═══╪═══╪═══════════╣
║ 2018-05-14 │ 1 │ 2 │ 8         ║
╚════════════╧═══╧═══╧═══════════╝
```


## Built-in function reference

### Terminal functions

These functions mark the end of the expression.  The most common is *eval* which evaluates the expression over the date range and return the resulting table.  In console mode the resulting table is printed.

Function name | Default indexer |Description
:---:|:---|:---
eval|[periodicity=B,date=today today,:]|Evaluates the expression over the (closed) date range from the first to the second *date* over *periodicity*, returning the resulting table.
def|[]|Defines the expression as the preceding name literal.
undef|[]|Deletes name specified by the preceding name literal.
import|[:]|Executes pinto expressions contained in the specifed file names in constant string columns.
help|[]|Prints help for the preceding name literal or all names if one has not been specified.
list|[]|Shows description for all names.
to_csv|[filename, periodicity=B,date=today today,:]|Evaluates the expression over the date range the (closed) date range from the first to the second *date* with *periodicity*, exporting the resulting table to csv *filename*.

### Stack manipulation functions

These commands manipulate stack elements, but do not modify values.

Function name | Default indexer |Description
:---:|:---|:---
clear|[:]|Clears indexed columns from stack.
copy|[c=2,:]|Copies indexed columns *n* times.
index|[c]|Indexes by constant double ordinals in c (start inclusive, end exclusive).  Assumes 0 for start if c is one constant.
only|[:]|Clears stack except for indexed columns.
rev|[:]|Reverses order of columns in stack.
roll|[c=1,:]|Permutes columns in stack *n* times.


### Data creation functions

These commands add columns to the stack.

Function name | Default indexer|Description
:---:|:---|:---
mkt|[tickers,fields]|Adds columns for market data specified by *tickers* and *fields*.
moon|[]|Creates a double column with values corresponding the phase of the moon.
pi|[]|Creates a constant double column with the value pi.
range|[c=1 4]|Creates double columns corresponding to integers between first (inclusive) and second (exclusive) columns in *c*, defaulting to 1 and 4.
read_csv|[source,includes_header="true"]|Reads CSV formatted table from file or URL specified as *source*.

### Data cleaning functions

Function name | Default indexer |Description
:---:|:---|:---
fill|[periodicity=BQ-DEC,lookback="true",default=NaN,:]|Fills missing values with last good value, looking back one period of *periodicity* if *lookback* is true, defaulting to *default* if no prior value exists.
join|[date,:]|Joins columns over time, switching between columns on dates in *date* columns.
resample|[periodicity=BM,:]|Sets prior columns to periodicity *periodicity*, carrying values forward if evaluation periodicity is more frequent.

### Date functions

Function name | Default indexer |Description
:---:|:---|:---
today|[]|Returns constant date column with today's date.
offset|[date=today,periodicity=B,c=-1]|Returns date that is *c* number of periods of *periodicity* from *date*.

### Header functions

Function name | Default indexer |Description
:---:|:---|:---
hcopy|[:]|Copies headers to a comma-delimited constant string column.
hformat|[format,:]|Formats headers, setting new value to *format* and substituting and occurences of "{}" with previous header value.
hpaste|[string,repeat="true",:]|Sets headers of other input columns to the values in comma-delimited constant string column.


### Binary double operators
Function name | Default indexer |Description
:---:|:---|:---
!=|[width=1,:]|Binary double operator != that operates on *width* columns at a time with fixed right-side operand.
%|[width=1,:]|Binary double operator % that operates on *width* columns at a time with fixed right-side operand.
*|[width=1,:]|Binary double operator * that operates on *width* columns at a time with fixed right-side operand.
+|[width=1,:]|Binary double operator + that operates on *width* columns at a time with fixed right-side operand.
-|[width=1,:]|Binary double operator - that operates on *width* columns at a time with fixed right-side operand.
/|[width=1,:]|Binary double operator / that operates on *width* columns at a time with fixed right-side operand.
<|[width=1,:]|Binary double operator < that operates on *width* columns at a time with fixed right-side operand.
<=|[width=1,:]|Binary double operator <= that operates on *width* columns at a time with fixed right-side operand.
==|[width=1,:] |Binary double operator == that operates on *width* columns at a time with fixed right-side operand.
\> |[width=1,:]| Binary double operator > that operates on *width* columns at a time with fixed right-side operand.
\>=|[width=1,:]| Binary double operator >= that operates on *width* columns at a time with fixed right-side operand.
\^|[width=1,:]| Binary double operator ^ that operates on *width* columns at a time with fixed right-side operand.

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

### Window creation functions
Function name | Default indexer |Description
:---:|:---|:---
cross|[:]|Creates a cross sectional window from input columns.
expanding|[date="range",:]|Creates creates an expanding window starting on *start* or the start of the evaluated range.
rev_expanding|[:]|Creates a reverse-expanding window containing values from the current period to the end of the range.
rolling|[c=2,:]|Creates a rolling window of size *c* for each input.

### Window statistics (Aggregation of each window view to a double value)
Function name | Default indexer |Description
:---:|:---|:---
change|[:]|Change from first to last for each view of window column inputs.
first|[:]|First value for each view of window column inputs.
last|[:]|Last value for each view of window column inputs.
max|[:]|Calculates max for each view of window column inputs.
mean|[:]|Calculates mean for each view of window column inputs.
min|[:]|Calculates min for each view of window column inputs.
pct_change|[:]|Calculates pct_change for each view of window column inputs.
product|[:]|Calculates product for each view of window column inputs.
std|[:]|Calculates standard deviation for each view of window column inputs.
sum|[:]|Calculates sum for each view of window column inputs.
zscore|[:]|Calculates zscore for each view of window column inputs.

### Visualization functions
Function name | Default indexer |Description
:---:|:---|:---
chart|[title="",date_format="",number_format="#,##0.00",width=750,height=350,periodicity=B, date=-20 offset today,:]|Creates a const string column with code for an HTML chart.
grid|[columns=3,HTML]|Creates a grid layout in a report with all input columns labelled HTML as cells in the grid.
report|[title="Pinto report",HTML]|Creates a new HTML report containing all *HTML* columns
rt|[functions=" BA-DEC offset expanding pct_change {YTD} today today eval",format="percent",digits=2,:]|Creates a const string column containing an HTML ranking table, applying each *function* to input columns and putting the ranked results in a table column.
table|[periodicity=B, date=-20 offset today,format="decimal",:]|Creates a const string column with code for an HTML ranking table.



