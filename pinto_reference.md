# Pinto Language Reference

### Terminal commands

These commands tell the interpreter to start executing your Pinto code.  The most common is **eval** which evaluates the preceding stack of commands over the date range that you specify as arguments.  In console mode the resulting data is printed as a table.

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*any<sub>1</sub>...any<sub>n</sub>*|**eval(*start date, end date, periodicity*)**|*any*| Evaluates the preceding commands over the given date range. (Date formats: *yyyy-mm-dd*, Valid periodicities: B, W-FRI, BM, BQ, BA ,Defaults: *periodicity*=B)

### Stack manipulation commands

These commands manipulate  

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*any<sub>1</sub>...any<sub>n</sub>*|**copy(*n*,*m*)**|*any*| copies *n* stack elements *m* times (*n* default: all, *m* default: 2) 
*any<sub>1</sub>...any<sub>n</sub>*|**rev(*n*)**|*any*| reverses order of *n* stack elements *(n default: all)*

#### Data creation commands

Inputs| Command(Parameters) | Outputs|Description
---:|:---:|:---|:---
*none*|**yhoo(*ticker<sub>1...n</sub>*)**|*double<sub>1</sub>...double<sub>n</sub>*| retrieves online price data for *n* tickers
