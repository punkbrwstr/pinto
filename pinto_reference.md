# Pinto Language Reference

### Terminal commands

These commands tell the interpreter to start executing your Pinto code.  The most common is **eval** which evaluates the preceding stack of commands over the date range that you specify as arguments.  In console mode the resulting data is printed as a table.

Command(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**eval(*start date*,*end date*,*periodicity*)**|**n**|Evaluates the preceding commands over the given date range. (defaults: *start date=prior period*,*periodicity=B*,*end date=prior period*)
**export(*start date*,*end date*,*periodicity*,*filename*)**|*none*|Evaluates the preceding commands over the given date range and exports csv for *filename*. (defaults: *start date=prior period*,*periodicity=B*,*end date=prior period*)
**def(*name*)**|*none*|Defines the preceding commands as a new command, named *name*. 
**help(*help type*)**|*none*|Prints help for proceding commands or prints *help type*. 
**del(*name*)**|*None*|Deletes previously defined command *name*. 

### Stack manipulation commands

These commands manipulate stack elements, but do not modify values.

Command(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**label(*label<sub>1</sub>*,*label<sub>z</sub>*)**|**z**|Sets arguments as labels for inputs 
**copy(*m*)**|**n* * *m**|Copies stack inputs *m* times (defaults: *m=2*)
**roll(*m*)**|**n**|Permutes input stack elements *m* times (defaults: *m=2*)
**clear**|*None*|Removes inputs from stack 

### Data creation commands

These commands generate data values.

Command(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**yhoo(*ticker<sub>1</sub>*,*ticker<sub>z</sub>*)**|**n* + *z**|Retrieves online price history for each *ticker*. 
**moon**|**n* + 1*|Calculates moon phase for this day.

### Rolling window commands

Command(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**chg(*size*,*periodicity*)**|**n**|Calculates change over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**chg_pct(*size*,*periodicity*)**|**n**|Calculates change in percent over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**chg_log(*size*,*periodicity*)**|**n**|Calculates log change over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_mean(*size*,*periodicity*)**|**n**|Calculates mean over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_max(*size*,*periodicity*)**|**n**|Calculates maximum over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_min(*size*,*periodicity*)**|**n**|Calculates minimum over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_sum(*size*,*periodicity*)**|**n**|Calculates sum over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_geomean(*size*,*periodicity*)**|**n**|Calculates geometric mean over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_var(*size*,*periodicity*)**|**n**|Calculates sample variance over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_varp(*size*,*periodicity*)**|**n**|Calculates variance over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_std(*size*,*periodicity*)**|**n**|Calculates sample standard deviation over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_zscorep(*size*,*periodicity*)**|**n**|Calculates z-score over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_zscore(*size*,*periodicity*)**|**n**|Calculates sample z-score over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**r_stdp(*size*,*periodicity*)**|**n**|Calculates standard deviation over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)
**correl(*size*,*periodicity*)**|**n**|Calculates average correlation over rolling window starting *size* number of *periodicity* prior for each input. (defaults: *size=1*,*periodicity=B*)

### Cross-sectional commands

Command(Parameters) | Outputs for *n* inputs|Description
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
**x_stdp**|*1*|Calculates standard deviation across inputs. 

### Data cleaning commands

Command(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**fill**|**n**|Fills missing data with last good obseration. 

### Binary double operators

Command(Parameters) | Outputs for *n* inputs|Description
:---:|:---|:---
**+**|**n* - 1*|Binary operator for addition. Applies operation to first input combined with each subsequent input. 
**-**|**n* - 1*|Binary operator for subtraction. Applies operation to first input combined with each subsequent input. 
**/**|**n* - 1*|Binary operator for division. Applies operation to first input combined with each subsequent input. 
*****|**n* - 1*|Binary operator for multiplication. Applies operation to first input combined with each subsequent input. 
**%**|**n* - 1*|Binary operator for modulo. Applies operation to first input combined with each subsequent input. 
