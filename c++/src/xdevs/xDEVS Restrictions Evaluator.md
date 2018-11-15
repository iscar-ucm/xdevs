# xDEVS Restrictions Evaluator
The C++ version of the xDEVS interpreter has a built-in restrictions evaluator that allows to check certain conditions of the data flow of the systems through the definition of rules in JSON format. It supports boolean and basic numeric types (char, int, long, float, double) and arrays of these numeric types. When some of the specified conditions is accomplished a message will be shown or the simulation will be interrupted (depending on the specified severity level). This makes easier the debugging and helps to assure that the system is working on the expected way.

## Tutorial
### 1. Syntax
#### 1.1. Rules
The specification of the rules, as commented before, is made through a JSON file. It has to include, at least a 'rules' element in which the conditions will be specified. Each one of its subelements includes has the following format:
```'<rule_name>': {'expr': '<boolean_expr>', 'level': '<info/error>'}```
being:
- **rule_name**: identifier of the rule. It consits of a name composed of uppercase and lowercase letters, numbers and underscores. This identifier will be shown when the expression is accomplished in the output messages.
- **boolean_expr**: boolean expression that indicates the activation condition of the rule. It can contain arithmetic ('+', '-', '*' and '/') and logic operators ('==', "!=", "<", "<=", ">", ">=", "&&" and "||"). As operators, it can contain the path of a port, a number or boolean literal or a variable (seen in the next section).  The path of the port will be expressed in the following format: ``` coupled1.coupled2.atomic1.port_name```.
- **level**: specifies the severity level of the rule. It can be set to 'info' (only shows informative messages when a rule is accomplished) or 'error' (the simulation is stopped).

Teniendo esto en cuenta, a basic restrictions file could have the following format (_mult_ and _adder_ are supposed to be modules with a floating point output):
```
{
	"rules": {
		"mult_60": {"expr": "mult.out == 60.2", "level": "info"}, 
		"a_gt_m": {"expr": "2 * adder.out > mult.out", "level": "info"}, 
		"mult_30_40": {"expr": "30 < mult.out && mult.out < 40", "level": "info"}
	}
}
```

When a model is working with arrays of numbers, it is necessary to specify the length of the array next to the port path, in the following way: ``` coupled1.coupled2.atomic1.port_name[0:5]``` (supposing that port_name outputs arrays of length 5). This method also allows to select subarrays, specifying the appropiate start and end of the desired subarray (```atomic1.port_name[<start>:<end>]```). The operations with arrays have to deal with same length arrays. The arithmetic operations relates each element with the array with the first array with the corresponding position of the second array (ex: ```{1,2,3} + {2,4,6} = {3,6,9}```). The ordering operators ("<", "<=", ">", ">=") starts evaluating the first element of each array and only evaluates the following ones when the previous are equal (ex: ```"{1,2,3} > {1,2,4}" => false```, ```"{2,2,3} > {1,2,4}" => true```).

Some functions can be applied to arrays also. In the current version the following ones are supported:
- **sum(<_arr_>)**: sum all the elements of the array.
- **max(<_arr_>)**: returns the maximum element of the array.
- **min(<_arr_>)**: returns the minimum element of the array.

A basic rules file dealing with arrays is shown below:
```
{
	"rules": {
		"mult_comp": {"expr": "mult.out[0:5] == {1,2,3,4,5}", "level": "info"},
		"a_gt_m": {"expr": "max(adder.out[0:5]) > sum(mult.out[0:5])", "level": "info"}, 
		"mult_30_40": {"expr": ""adder.out[0:5] + adder2.out[0:5] < mult.out[0:5]",  "level": "info"}
	}
}
```

#### 1.2. Variables
To simplify the previous expressions and improve the legibility is recommended to use variables. They are pairs of ```<variable_name>: <arith_expression>``` and are contained in the 'vars' element. For instance, the last rules file could be expressed in the following way using variables:
```
{
	"vars": {
		"adder_arr": "adder.out[0:5]",
		"adders_sum_arr": "adder.out[0:5] + adder2.out[0:5]",
		"mult_arr": "mult.out[0:5]"
	},
	"rules": {
		"mult_comp": {"expr": "mult_arr == {1,2,3,4,5}", "level": "info"},
		"a_gt_m": {"expr": "max(adder_arr) > sum(mult_arr)", "level": "info"},
		"mult_30_40": {"expr": "adders_sum_arr < mult", "level": "info"}
	}
}
```

### 2. How to apply the rules in a model 
Once the rules are written in a file in the specified format, its file path can be passed passed to the Coordinator as the second argument of the constructor. An example of this can be seen below:
```
Ef ef("ef", 3);   // Example root coupled model
Coordinator coordinator(&ef, "rules.txt");  // Rules file as second argument
coordinator.initialize();
coordinator.simulate((long int)100);
coordinator.exit();
```

### 3. Examples
Some examples explaining that concepts can be found in the 'examples' folder of the C++ DEVS interpreter (‘operations’ and ‘operations_arrays’, each one with an example rules file).
