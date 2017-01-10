# Pinto Language Reference

### Indexing/slicing operators

An indexing/slicing operator determines which stack elements are passed to the following function.  By default the entire stack is passed.  

#### Indexing by number
The operator can take lists or ranges of numerical indicies.  Index numbering starts with ```0```, which represents the top or rightmost element of the stack.  Lists of indicies are separated by commas.   ```[1,3]``` represents the second and fourth stack elements.  Ranges may specify an inclusive starting index, an inclusive ending index or both.  ```[0:3]```  represents the first through fourth stack elements.  ```[1:]``` represents all stack elements after the first.  Negative indicies are converted to the stack size minus that number.  ```[-2:]```  represents the last two elements in the stack.

#### Indexing by label
The indexing operator can also take string arguments to select stack elements by their labels.  Separate label indicies by commas to select multiple elements.  ```[my_label2,my_label1]``` Label indicies support ```*``` as a wildcard for matching zero or more characters (potentially returning multiple stack elements for one argument).

## Function reference

### Terminal functions

These functions tell the interpreter to start executing your Pinto code.  The most common is **eval** which evaluates the preceding stack of commands over the date range that you specify as arguments.  In console mode the resulting data is printed as a table.

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**eval(*start date*,*end date*,*periodicity*)**|*n*|Evaluates the preceding commands over the given date range. (defaults: *start date=prior period*,*periodicity=B*,*end date=prior period*)
**export(*start date*,*end date*,*periodicity*,*filename*)**|*none*|Evaluates the preceding commands over the given date range and exports csv for *filename*. (defaults: *start date=prior period*,*periodicity=B*,*end date=prior period*)
**def(*name*)**|*none*|Defines the preceding commands as a new command, named *name*. 
**help(*help type*)**|*none*|Prints help for proceding commands or prints *help type*. 
**del(*name*)**|*none*|Deletes previously defined command *name*. 

### Stack manipulation functions

These commands manipulate stack elements, but do not modify values.

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**label(*label<sub>1</sub>*,*label<sub>z</sub>*)**|**z**|Sets arguments as labels for inputs 
**copy(*m*)**|*n * m*|Copies stack inputs *m* times (defaults: *m=2*)
**roll(*m*)**|**n**|Permutes input stack elements *m* times (defaults: *m=2*)
**clear**|*None*|Removes inputs from stack 

### Data creation functions

These commands generate data values.

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**yhoo(*ticker<sub>1</sub>*,*ticker<sub>z</sub>*)**|*n + z*|Retrieves online price history for each *ticker*. 
**moon**|*n + 1*|Calculates moon phase for this day. 

### Rolling window functions

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**chg(*size*,*periodicity*)**|*n*|Calculates change over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**chg_pct(*size*,*periodicity*)**|*n*|Calculates change in percent over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**chg_log(*size*,*periodicity*)**|*n*|Calculates log change over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_mean(*size*,*periodicity*)**|*n*|Calculates mean over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_max(*size*,*periodicity*)**|*n*|Calculates maximum over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_min(*size*,*periodicity*)**|*n*|Calculates minimum over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_sum(*size*,*periodicity*)**|*n*|Calculates sum over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_geomean(*size*,*periodicity*)**|*n*|Calculates geometric mean over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_var(*size*,*periodicity*)**|*n*|Calculates sample variance over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_varp(*size*,*periodicity*)**|*n*|Calculates variance over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_std(*size*,*periodicity*)**|*n*|Calculates sample standard deviation over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_zscorep(*size*,*periodicity*)**|*n*|Calculates z-score over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_zscore(*size*,*periodicity*)**|*n*|Calculates sample z-score over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_stdp(*size*,*periodicity*)**|*n*|Calculates standard deviation over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**correl(*size*,*periodicity*)**|*n*|Calculates average correlation over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)

### Cross-sectional functions

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**x_mean**|*1*|Calculates mean across inputs. 
**x_max**|*1*|Calculates maximum across inputs. 
**x_min**|*1*|Calculates minimum across inputs. 
**x_sum**|*1*|Calculates sum across inputs. 
**x_geomean**|*1*|Calculates geometric mean across inputs. 
**x_var**|*1*|Calculates sample variance across inputs. 
**x_varp**|*1*|Calculates variance across inputs. 
**x_std**|*1*|Calculates sample standard deviation across inputs. 
**x_zscorep**|*1*|Calculates z-score across inputs. 
**x_zscore**|*1*|Calculates sample z-score across inputs. 

### Expanding window functions

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**e_mean(*size*,*periodicity*)**|*n*|Calculates mean over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**e_max(*start_date*,*periodicity*)**|*n*|Calculates maximum over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_min(*start_date*,*periodicity*)**|*n*|Calculates minimum over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_sum(*start_date*,*periodicity*)**|*n*|Calculates sum over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_geomean(*start_date*,*periodicity*)**|*n*|Calculates geometric mean over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_var(*start_date*,*periodicity*)**|*n*|Calculates sample variance over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_varp(*start_date*,*periodicity*)**|*n*|Calculates variance over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_std(*start_date*,*periodicity*)**|*n*|Calculates sample standard deviation over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_zscorep(*start_date*,*periodicity*)**|*n*|Calculates z-score over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_zscore(*start_date*,*periodicity*)**|*n*|Calculates sample z-score over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)
**e_stdp(*start_date*,*periodicity*)**|*n*|Calculates standard deviation over an expanding window starting *start_date* over *periodicity*. (defaults: *periodicity=B*,*start_date=1*)

### Data cleaning commands

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**fill**|*n*|Fills missing data with last good obseration. 
**flb(*periodicity*)**|*n*|Fills missing data with last good obseration, looking back one period of *periodicity* if first element of data is missing. (defaults: *periodicity=BQ-DEC*)
**join(*date<sub>1</sub>, date<sub>z</sub>*)**|*1*|Fills missing data with last good obseration. 
**resample(*periodicity*)**|*n*|Changes periodicity of inputs to *periodicity*, rounding down for less frequent periodicities. 

### Binary operators

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**+**|*n - 1*|Binary operator for addition. Applies operation to first input combined with each subsequent input. 
**-**|*n - 1*|Binary operator for subtraction. Applies operation to first input combined with each subsequent input. 
**/**|*n - 1*|Binary operator for division. Applies operation to first input combined with each subsequent input. 
*****|*n - 1*|Binary operator for multiplication. Applies operation to first input combined with each subsequent input. 
**%**|*n - 1*|Binary operator for modulo. Applies operation to first input combined with each subsequent input. 
**==**|*n - 1*|Binary operator for equals. Applies operation to first input combined with each subsequent input. 
**!=**|*n - 1*|Binary operator for not equals. Applies operation to first input combined with each subsequent input. 
**>**|*n - 1*|Binary operator for greater than. Applies operation to first input combined with each subsequent input. 
**<**|*n - 1*|Binary operator for less than. Applies operation to first input combined with each subsequent input. 
**>=**|*n - 1*|Binary operator for greater than or equal to. Applies operation to first input combined with each subsequent input. 
**<=**|*n - 1*|Binary operator for less than or equal to. Applies operation to first input combined with each subsequent input. 

### Unary operators

Function(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**abs**|*n*|Unary operator for absolute value. Applies operation to each input. 
**neg**|*n*|Unary operator for negation. Applies operation to each input. 
**inv**|*n*|Unary operator for inverse. Applies operation to each input. 
**log**|*n*|Unary operator for natural log. Applies operation to each input. 
