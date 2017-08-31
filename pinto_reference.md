# Pinto Language Reference

### Indexing

An indexing/slicing operator determines which columns in the stack are passed to the following function.  

#### All columns
When no indexer is specified, the entire stack is passed to the following function.  To explicitly show that you want the whole stack you can also use the indexer ```[:]```.

#### Indexing by number
The operator can take numerical indicies or numerical ranges of indicies.  Index numbering starts with ```0```, which represents the top or rightmost column of the stack.  Ranges may specify an inclusive starting index, an exclusive ending index or both.  ```[0:3]```  represents the first through third columns in the stack.  ```[1:]``` represents all stack columns after the first.  Negative indicies are converted to the stack size minus that number.  ```[-2:]```  represents the last two columns in the stack.

#### Indexing by header
The indexing operator can also take string arguments to select stack columns by their header like ```[my_label2]```. Header indicies support ```*``` as a wildcard for matching zero or more characters (potentially returning multiple columns for one argument).

#### Multiple indicies
Lists of indicies are separated by commas.   ```[1,3]``` represents the second and fourth columns.   ```[1,3:]``` is the second and all columns after the third.  ```[1,pin*]``` is the second and all columns with headers starting with "pin".

#### Index modifiers: Or
The Or modifier (```"|"```) tells the indexer to try another index if the first is not found.  For instance, ```[guac|0]``` will select columns with the header "guac", or will return the first column.

#### Index modifiers: Copy
The Copy modifier (```"&"```) forces the indexer make a copy of the column for the subsequent function, leaving the original column on the stack.  For example, ```[:&]``` will make a copy of all columns in the stack for the following function and maintain the originals in the stack.

#### Index modifiers: Repeat
The repeat modifier (```"+"```) causes the indexer to make multiple calls to the following function until the stack no longer contains enough columns for the indexer.  The index ```[:2+]``` will make repeated calls to the following function, each time supplying the top two columns on the stack as the function inputs.  It will stop when there are fewer than two columns left on the stack. 



### Function parameters

Parameters are special inputs to functions that modify how the function operates.  They only use the header of a column and discard the row values.  Parameters may be supplied by position (by being at the top of the stack) or by name (by having a header that starts with ```parametername=```).  As an example, to supply a file name to the read function you could add a header-only column to the top of the stack ```"/tmp/my_file_name" read``` or you could specify the parameter by name ```"source=/tmp/my_file_name" read```.  Multiple parameters may be specified as separate columns on the stack (```"tickers=TACO Equity" "fields=PX_LAST" bbg```) or within one column header by delimiting with ```;``` (```"tickers=TACO Equity;fields=PX_LAST" bbg```).


## Function reference

### Terminal functions

These functions tell the interpreter to start executing your Pinto code.  The most common is **eval** which evaluates the preceding stack of commands over the date range that you specify as arguments.  In console mode the resulting data is printed as a table.

Function name | Parameters |Description
:---:|:---|:---
**eval**|***start:***  Start date of range to evaluate (format: yyyy-mm-dd), ***end:***  End date of range to evaluate (format: yyyy-mm-dd), ***freq:***  Periodicity of range to evaluate {B,W-FRI,BM,BQ,BA} (default: B)|Evaluates the preceding commands over the given date range. 
**def**|***name:*** Name to assign to your function|Defines stack as a name
**export**|***filename:***  File name for csv output, ***start:***  Start date of range to evaluate (format: yyyy-mm-dd), ***end:***  End date of range to evaluate (format: yyyy-mm-dd), ***freq:***  Periodicity of range to evaluate {B,W-FRI,BM,BQ,BA} (default: B)|Evaluates the preceding commands over the given date range and exports csv for *filename* 
**help**|***type:***  Pinto name for which you want help.|Prints help for function ***type*** or prints full help
**del**|***name:*** Name to delete|Deletes previously defined name 
**exec**|***filename:***  Path to .pinto file|Executes pinto program defined in *filename*

### Stack manipulation functions

These commands manipulate stack elements, but do not modify values.

Function name | Parameters |Description
:---:|:---|:---
**label**|***labels:***  Comma delimited headers to apply to columns|Sets the headers of columns 
**copy**|***times:***  Number of copies of stack inputs to make. (default: 2)|Copies inputs
**roll**|***times:***  Number of times to permute (default: 1)|inputs
**clear**|none|Removes inputs from the stack
**only**|none|Clears stack except for inputs 
**rev**|none|Reverses order of input stack 


### Data creation functions

These commands generate data values.

Function name | Parameters |Description
:---:|:---|:---
**moon**|none|Phase of the moon
**range**|***range:***  Range formatted as start(inclusive):stop(exclusive) (default: 0:5)|Creates columns of constant integers 
**read**|***source:***  URL or file path for csv, ***includes_header:***  Whether or not first row contains headers (default: true)|Imports table from a csv formatted file or URL 

### Rolling window functions


Function name | Parameters |Description
:---:|:---|:---
**r_chg**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates change from beginning to end over rolling window 
**r_chglog**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates log change from beginning to end over rolling window 
**r_chgpct**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates percent change from beginning to end over rolling window 
**r_correl**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates correlation over rolling window 
**r_geomean**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates geometric mean over rolling window 
**r_lag**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates lag over rolling window 
**r_max**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates maximum over rolling window 
**r_mean**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates average over rolling window 
**r_min**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates minimum over rolling window 
**r_std**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates sample standard deviation over rolling window 
**r_stdp**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates population standard deviation over rolling window 
**r_sum**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates sum over rolling window 
**r_var**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates sample variance over rolling window 
**r_varp**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates population variance over rolling window 
**r_zscore**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates sample z-score over rolling window 
**r_zscorep**|***size:***  Size of window (default: 1), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ,BA}|Calculates population z-score over rolling window 

### Cross-sectional functions

Function name | Parameters |Description
:---:|:---|:---
**x_geomean**|none|Calculates cross-sectional geometric mean per period for all inputs 
**x_max**|none|Calculates cross-sectional maximum per period for all inputs
**x_mean**|none|Calculates cross-sectional mean per period for all inputs
**x_min**|none|Calculates cross-sectional minimum per period for all inputs
**x_std**|none|Calculates cross-sectional sample standard deviation per period for all inputs
**x_stdp**|none|Calculates cross-sectional population standard deviation per period for all inputs
**x_sum**|none|Calculates cross-sectional sum per period for all inputs
**x_var**|none|Calculates cross-sectional variance per period for all inputs
**x_varp**|none|Calculates cross-sectional portfolio variance per period for all inputs
**x_zscore**|none|Calculates cross-sectional sample z-score per period for all inputs
**x_zscorep**|none|Calculates cross-sectional population z-score per period for all inputs

### Expanding window functions

Function name | Parameters |Description
:---:|:---|:---
**e_chg**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates change over an expanding window 
**e_chglog**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates log change over an expanding window 
**e_chgpct**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates change in percent over an expanding window 
**e_geomean**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates geometric mean over an expanding window 
**e_max**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates maximum over an expanding window 
**e_mean**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates mean over an expanding window 
**e_min**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates minimum over an expanding window 
**e_std**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates sample standard deviation over an expanding window 
**e_stdp**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates standard deviation over an expanding window 
**e_sum**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates sum over an expanding window 
**e_var**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates sample variance over an expanding window 
**e_varp**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates variance over an expanding window 
**e_zscore**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates sample z-score over an expanding window 
**e_zscorep**|***start:***  Start date for expanding window (yyyy-mm-dd), ***freq:***  Periodicity of window {B,W-FRI,BM,BQ-DEC,BA} (default: B)|Calculates z-score over an expanding window 

### Data cleaning functions

Function name | Parameters |Description
:---:|:---|:---
**fill**|***freq:***  Period of time to look back. (default: BQ-DEC)|Fills missing data with last good obseration, looking back one *freq* to fill first. 
**flb**|none|Fills missing data with last good obseration. 
**join**|***dates:***  Cutover dates separated by commas (yyyy-mm-dd)|Joins series a given cutover dates 
**resample**|***freq:***  New perioditicy|Changes periodicity of inputs to *freq* 

### Header functions

Function name | Parameters |Description
:---:|:---|:---
**append**|***suffix:***  Suffix to append|Appends *suffix* header to headers 
**prepend**|***prefix:***  Header to prepend to others|Prepends *prefix* to headers 
**format**|***format:***  Format string ("{}" is existing header)|Formats headers according to supplied format string 

### Binary operators
Function name | Parameters |Description
:---:|:---|:---
**!=**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**%**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
*****|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**+**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**-**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**/**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**<**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**<=**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**==**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**>**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input
**>=**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input 
**^**|***right_count:***  Number of inputs to use as the fixed operand (right hand side operand, e.g. divisor, subtrahend, etc.) (default: 1)|Adds fixed inputs (in order, with recycling) to each subsequent input

### Unary operators

Function name | Parameters |Description
:---:|:---|:---
**inv**|none|Calculates inverse of inputs. 
**log**|none|Calculates natural log of inputs. 
**neg**|none|Calculates additive inverse of inputs. 
**abs**|none|Calculates absolute value of inputs. 
**exp**|none|Calculates e^x for inputs. 
**acgbConvert**|none|Calculates absolute value of inputs. 


### Extra functions (requires additional libraries, see wiki)

Function name | Parameters |Description
:---:|:---|:---
**bbg**|***tickers:***  Bloomberg ticker codes, ***fields:***  Bloomberg field codes (default: PX_LAST)|Retrieves online price history for each ticker and field combination. 
**fut**|***code:***  Two-letter Bloomberg contract series code, ***yellow_key:***  Bloomberg yellow key (default: Comdty), ***price_modified:***  Price modifier pinto function (acgbConvert), ***criteria_field:***  Bloomberg field for criteria to determine front contract (default: OPEN_INT), ***price_field:***  Bloomberg price field to compute returns (default: PX_LAST), ***price_field_previous:***  Bloomberg price field for the start of each return period (default: PX_LAST), ***previous_offset:***  Number of periods back start of each return period (default: -1), ***calc_return:***  Whether or not to calculate returns (default: true)|Calculates returns for front contract of given futures contract series. 

