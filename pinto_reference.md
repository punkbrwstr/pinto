# Pinto Language Reference

### Terminal commands

These commands tell the interpreter to start executing your Pinto code.  The most common is **eval** which evaluates the preceding stack of commands over the date range that you specify as arguments.  In console mode the resulting data is printed as a table.

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*any<sub>1</sub>...any<sub>n</sub>*|**eval(*start date*,*end date*,*periodicity*)**|*any<sub>1</sub>...any<sub>n</sub>*|Evaluates the preceding commands over the given date range. (defaults: *start date=prior period*,*periodicity=B*,*end date=prior period*)
*any<sub>1</sub>...any<sub>n</sub>*|**export(*start date*,*end date*,*periodicity*,*filename*)**|*none*|Evaluates the preceding commands over the given date range and exports csv for *filename*. (defaults: *start date=prior period*,*periodicity=B*,*end date=prior period*)
*any<sub>1</sub>...any<sub>n</sub>*|**def(*name*)**|*none*|Defines the preceding commands as a new command, named *name*. 
*any<sub>1</sub>...any<sub>n</sub>*|**help(*help type*)**|*none*|Prints help for proceding commands or prints *help type*. 
*none*|**del(*name*)**|*none*|Deletes previously defined command *name*. 

### Stack manipulation commands

These commands manipulate stack elements, but do not modify values.

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*any<sub>1</sub>...any<sub>z</sub>*|**label(*label<sub>1</sub>*,*label<sub>z</sub>*)**|*any<sub>1</sub>...any<sub>z</sub>*|Sets arguments as labels for inputs 
*any<sub>1</sub>...any<sub>n</sub>*|**copy(*n*,*m*)**|*any<sub>1</sub>...any<sub>n</sub>*|Copies *n* stack elements *m* times (defaults: *m=2*,*n=all*)
*any<sub>1</sub>...any<sub>n</sub>*|**roll(*n*,*m*)**|*any<sub>1</sub>...any<sub>n</sub>*|Permutes *n* stack elements *m* times (defaults: *m=2*,*n=all*)
*any<sub>1</sub>...any<sub>n</sub>*|**index(*i<sub>1</sub>*,*i<sub>z</sub>*)**|*any<sub>1</sub>...any<sub>z</sub>*|Retrieves stack element for each *i*. *i* may be integer or string to retrieve by label. 

#### Data creation commands

These commands generate data values.

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*none*|**yhoo(*ticker<sub>1</sub>*,*ticker<sub>z</sub>*)**|*double<sub>1</sub>...double<sub>z</sub>*|Retrieves online price history for each *ticker*. 
*none*|**moon**|*double<sub>1</sub>...double<sub>z</sub>*|Calculates moon phase for this day. 

#### Rolling window commands

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*double<sub>1</sub>...double<sub>n</sub>*|**chg(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates change over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**chg_pct(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates change in percent over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**chg_log(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates log change over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_mean(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates mean over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_max(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates maximum over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_min(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates minimum over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_sum(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates sum over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_geomean(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates geometric mean over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_var(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates sample variance over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_varp(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates variance over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_std(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates sample standard deviation over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_zscorep(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates z-score over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_zscore(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates sample z-score over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**r_stdp(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates standard deviation over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**correl(*size*,*periodicity*,*n*)**|*double<sub>1</sub>...double<sub>n</sub>*|Calculates average correlation over rolling window starting *size* number of *periodicity* prior for *n* inputs. (defaults: *size=1*,*periodicity=B*,*n=all*)

#### Cross-sectional commands

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*double<sub>1</sub>...double<sub>n</sub>*|**x_mean(*n*)**|*double*|Calculates mean across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_max(*n*)**|*double*|Calculates maximum across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_min(*n*)**|*double*|Calculates minimum across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_sum(*n*)**|*double*|Calculates sum across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_geomean(*n*)**|*double*|Calculates geometric mean across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_var(*n*)**|*double*|Calculates sample variance across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_varp(*n*)**|*double*|Calculates variance across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_std(*n*)**|*double*|Calculates sample standard deviation across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_zscorep(*n*)**|*double*|Calculates z-score across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_zscore(*n*)**|*double*|Calculates sample z-score across *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>...double<sub>n</sub>*|**x_stdp(*n*)**|*double*|Calculates standard deviation across *n* inputs. (defaults: *n=all*)
*any<sub>1</sub>...any<sub>n</sub>*|**fill(*n*)**|*any<sub>1</sub>...any<sub>n</sub>*|Fills missing data with last good obseration for *n* inputs. (defaults: *n=all*)
*double<sub>1</sub>, double<sub>2</sub>*|**+**|*double*|Binary double operator for addition. 
*double<sub>1</sub>, double<sub>2</sub>*|**-**|*double*|Binary double operator for subtraction. 
*double<sub>1</sub>, double<sub>2</sub>*|**/**|*double*|Binary double operator for division. 
*double<sub>1</sub>, double<sub>2</sub>*|*****|*double*|Binary double operator for multiplication. 
*double<sub>1</sub>, double<sub>2</sub>*|**%**|*double*|Binary double operator for modulo. 
