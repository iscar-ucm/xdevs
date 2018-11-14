
#include "RulesEvaluator.h"

void RulesEvaluator::parseRules(std::string rulesFilePath, Coupled* model) {
    // Open the rules file with the json wrapper
    std::ifstream ifs(rulesFilePath);
    rapidjson::IStreamWrapper isw(ifs);
    rapidjson::Document d;
    d.ParseStream(isw);

    const std::string arithOperators[] = {"+", "-", "*", "/", "%"};
    const std::string logicOperators[] = {"==", "!=", ">", "<", ">=", "<=", "&&", "||"};

    const std::string arithOps = "+|-|*|/|%";

    // Check the existance of the required json elements
    if(!d.HasMember("rules")) {
        throw std::runtime_error("'rules' element not found in rules file (" + rulesFilePath + ")");
    }

    ///std::cout << d["vars"]["adder"].GetString() << std::endl;

    std::string rgxsVar = "[a-zA-Z_][a-zA-Z0-9]*";
    std::string rgxsPortPath = "((?:" + rgxsVar + "\\.)(?:" + rgxsVar + "\\.?)+)";
    std::string rgxsArray = rgxsPortPath + "\\[([0-9]+):([0-9]+)\\]";
    std::regex rgxNumber("[0-9]+\\.?[0-9]*");
    std::regex rgxPortPath("^" + rgxsPortPath + "$");
    std::regex rgxArray("^" + rgxsArray + "$");
    std::regex rgxValidName("^[0-9a-zA-Z_]+$");
    //std::regex rgxArrayFun("^([a-z]+)\\(" + rgxsArray + "\\)$");
    std::smatch match;

    if(d.HasMember("vars")) {
        // Save the variables expresions and resolve related ports
        for (rapidjson::Value::ConstMemberIterator var = d["vars"].MemberBegin(); var != d["vars"].MemberEnd(); ++var){

            std::string name = var->name.GetString();
            std::string value = var->value.GetString();

            if (!regex_match(name, match, rgxValidName))
                throw std::runtime_error("Invalid variable name: " + name);

            Port* port;

            rulesVars[name] = value;
            std::cout << "--- <VAR> --- " << name << ": " << value << std::endl;

            for(int i = 0; i < (int) value.length(); i++) {
                if((value[i] == ' ' || value[i] == '(' || value[i] == ')')) {
                    continue;
                } else if(value[i] == '{') {
                    std::string term = "";

                    while(i+1 < (int) value.length() && (isdigit(value[i+1]) || std::string(" ,.").find(value[i+1]) != std::string::npos)) {
                        term += value[++i];
                    }

                    if(value[i+1] == '}') {
                        i++;
                        continue;
                    } else {
                        throw std::runtime_error("Invalid term: " + term);
                    }
                } else if(isdigit(value[i])) {
                    std::string term = "";
                    term += value[i];

                    while(i+1 < (int) value.length() && (isdigit(value[i+1]) || value[i+1] == '.')) {
                        term += value[++i];
                    }

                    if(!regex_match(term, match, rgxNumber)) {
                        throw std::runtime_error("Invalid term: " + term);
                    }
                } else if(isalpha(value[i])) {
                    std::string term = "";
                    term += value[i];

                    while(i+1 < (int) value.length() && (isalnum(value[i+1]) || std::string(".:_[]").find(value[i+1]) != std::string::npos)) {
                        term += value[++i];
                    }

                    if (regex_match(term, match, rgxArray)) {
                        std::string port_name = match.str(1);
                        if(rulesPorts.find(port_name) == rulesPorts.end()) {
                            port = model->portFromPath(port_name);
                            rulesPorts[port_name] = port;
                            //std::cout << "Added port: " << port_name << std::endl;
                        }

                    } else if(regex_match(term, match, rgxPortPath)) {
                        if(rulesPorts.find(term) == rulesPorts.end()) {
                            port = model->portFromPath(term);
                            rulesPorts[term] = port;
                            //std::cout << "Added port: " << term << std::endl;
                        }
                    } else if(term == "sum" || term == "max" || term == "min") {
                        //std::cout << "Function recognized: " << term << std::endl;
                        continue;
                    } else {
                        throw std::runtime_error("Invalid term: " + term);
                    }
                } else {
                    std::string term = "";
                    term += value[i];

                    while(i+1 < (int) value.length() && std::string("+-*/%!=<>&|").find(value[i+1]) != std::string::npos)
                        term += value[i+1];

                    if(std::find(std::begin(arithOperators), std::end(arithOperators), term) == std::end(arithOperators))
                        throw std::runtime_error("Invalid term: " + term);
                }
            }
        }
    }

    // Save the rules expresions and check the existance of the used variables
    for (rapidjson::Value::ConstMemberIterator rule = d["rules"].MemberBegin(); rule != d["rules"].MemberEnd(); ++rule){

        std::string name = rule->name.GetString();
        std::string value = "";
        int level = -1;

        if (!regex_match(name, match, rgxValidName))
            throw std::runtime_error("Invalid variable name: " + name);

        /*if(!d[name].HasMember("expr")) {
            throw std::runtime_error("'expr' element not found (" + name + " expr)");
        } else if(!d[name].HasMember("level")) {
            throw std::runtime_error("'level' element not found (" + name + " rule)");
        }*/
        for (rapidjson::Value::ConstMemberIterator ruleItem = rule->value.MemberBegin(); ruleItem != rule->value.MemberEnd(); ++ruleItem){
            std::string ruleItemName = ruleItem->name.GetString();
            std::string ruleItemValue = ruleItem->value.GetString();

            if(ruleItemName == "expr") value = ruleItem->value.GetString();
            else if(ruleItemName == "level") {
                if(ruleItemValue == "info") {
                    level = RulesEvaluator::LVL_INFO;
                } else if (ruleItemValue == "error") {
                    level = RulesEvaluator::LVL_ERROR;
                } else {
                    throw std::runtime_error("'level' must be 'info' or 'error' (" + name + " rule)");
                }
            }
        }

        if(value == "") throw std::runtime_error("'expr' element not found (" + name + " rule)");
        else if(level == -1) throw std::runtime_error("'level' element not found (" + name + " rule)");

        Port* port;
        rules[name] = {value, level};

        //std::cout  << "--- <RULE> --- " << name << ": " << value << std::endl;

        for(int i = 0; i < (int) value.length(); i++) {
            if((value[i] == ' ' || value[i] == '(' || value[i] == ')')) {
                continue;
            } else if(value[i] == '{') {
                std::string term = "";

                while(i+1 < (int) value.length() && (isdigit(value[i+1]) || std::string(" ,.").find(value[i+1]) != std::string::npos)) {
                    term += value[++i];
                }

                if(value[i+1] == '}') {
                    i++;
                    continue;
                } else {
                    throw std::runtime_error("Invalid term: " + term);
                }
            } else if(isdigit(value[i])) {
                std::string term = "";
                term += value[i];

                while(i+1 < (int) value.length() && (isdigit(value[i+1]) || value[i+1] == '.')) {
                    term += value[++i];
                }

                if(!regex_match(term, match, rgxNumber)) {
                    throw std::runtime_error("Invalid term: " + term);
                }
            } else if(isalpha(value[i])) {
                std::string term = "";
                term += value[i];

                while(i+1 < (int) value.length() && (isalnum(value[i+1]) || std::string(".:_[]").find(value[i+1]) != std::string::npos)) {
                    term += value[++i];
                }

                if (regex_match(term, match, rgxArray)) {
                    std::string port_name = match.str(1);
                    if(rulesPorts.find(port_name) == rulesPorts.end()) {
                        port = model->portFromPath(port_name);
                        rulesPorts[port_name] = port;
                        //std::cout << "Added port: " << port_name << std::endl;
                    }

                } else if(regex_match(term, match, rgxPortPath)) {
                    if(rulesPorts.find(term) == rulesPorts.end()) {
                        port = model->portFromPath(term);
                        rulesPorts[term] = port;
                        //std::cout << "Added port: " << term << std::endl;
                    }
                } else if(term == "sum" || term == "max" || term == "min") {
                    //std::cout << "Function recognized: " << term << std::endl;
                    continue;
                } else if(rulesVars.find(term) != rulesVars.end()) {
                    //std::cout << "Var found: " << term << std::endl;
                    continue;
                } else {
                    throw std::runtime_error("Invalid term: " + term);
                }
            } else {
                std::string term = "";
                term += value[i];

                while(i+1 < (int) value.length() && std::string("+-*/%!=<>&|").find(value[i+1]) != std::string::npos)
                    term += value[++i];

                if(std::find(std::begin(arithOperators), std::end(arithOperators), term) == std::end(arithOperators) &&
                   std::find(std::begin(logicOperators), std::end(logicOperators), term) == std::end(logicOperators))
                    throw std::runtime_error("Invalid term: " + term);
            }
        }
    }
}

void RulesEvaluator::checkRules() {

    //std::cout << "***************************************************" << std::endl;
    // Solve variables
    for(std::pair<std::string, std::string> expr: rulesVars) {
        //std::cout << "____________________________________" << std::endl;
        //std::cout << expr.first << ": " << expr.second << std::endl;

        try {
            varsValues[expr.first] = parseExpr(expr.first, expr.second, true);
        } catch(const ParseException& error) {}
    }

    //std::cout << "***************************************************" << std::endl;

    // Check rules
    for(std::pair<std::string, ruleInfo> expr: rules) {
        //std::cout << "-------------------------------" << std::endl;
        //std::cout << expr.first << ": " << expr.second.expr << std::endl;
        try {
            valuePair res = parseExpr(expr.first, expr.second.expr);
            if (strcmp(res.first, typeid(bool).name()) == 0) {
                //std::cout << ">>> Rule res: " << *(bool*)res.second.get() << std::endl;
                if(*(bool*)res.second.get()) {
                    if(expr.second.level == LVL_INFO)
                        std::cout << ">>> Rule " << expr.first << " accomplished!!!" << std::endl;
                    else if(expr.second.level == LVL_ERROR)
                        throw std::runtime_error("Rule " + expr.first + " accomplished!!!");
                }
            }
        } catch(const ParseException& error) {}
    }

    // Clear current iteration values
    varsValues.clear();

}

RulesEvaluator::valuePair RulesEvaluator::parseExpr(std::string expr_name, std::string expr_rule, bool var_expr) {
    int i;

    std::stack <valuePair> values;
    std::stack <char> ops;

    //std::string expr_name = expr.first;
    //std::string expr_rule = expr.second;

    for(i = 0; i < (int) expr_rule.length(); i++){

        if(expr_rule[i] == ' ') {
            continue;

        } else if(expr_rule[i] == '(') {
            ops.push(expr_rule[i]);

        } else if(expr_rule[i] == '{') {
            std::string term = "";

            while(i+1 < (int) expr_rule.length() && (isdigit(expr_rule[i+1]) || std::string(" ,.").find(expr_rule[i+1]) != std::string::npos)) {
                term += expr_rule[++i];
            }

            if(expr_rule[i+1] == '}') {
                i++;
                values.push(arrayFromStr(term, ','));
            } else {
                throw std::runtime_error("Invalid term: " + term);
            }
        } else if(isdigit(expr_rule[i])) {
            std::string value = "";
            value += expr_rule[i];

            while(i+1 < (int) expr_rule.length() && (isdigit(expr_rule[i+1]) || expr_rule[i+1] == '.')) {
                value += expr_rule[++i];
            }

            if(value.find('.') == std::string::npos) {
                valuePair longPair = getValuePair(std::stol(value));
                values.push(longPair);
            } else {
                valuePair doublePair = getValuePair(std::stod(value));
                values.push(doublePair);
            }

        } else if(expr_rule[i] == ')') {
            while(!ops.empty() && ops.top() != '(')
            {
                solveOperation(values, ops);
            }

            ops.pop();  // Opening brace
        } else if(isalpha(expr_rule[i])) {
            std::string name = "";
            name += expr_rule[i];

            while(i+1 < (int) expr_rule.length() && (isalnum(expr_rule[i+1])  || std::string(".:_[]").find(expr_rule[i+1]) != std::string::npos)) {
                name += expr_rule[++i];
            }

            if(name == "true") {
                valuePair boolPair = getValuePair(new bool(true));
                values.push(boolPair);
            } else if(name == "false") {
                valuePair boolPair = getValuePair(new bool(false));
                values.push(boolPair);
            } else {
                if(!var_expr && varsValues.find(name) != varsValues.end()) {
                    //std::cout << "var " << name << " resolved" << std::endl;
                    values.push(varsValues[name]);
                } else {
                    std::string rgxsVar = "[a-zA-Z_][a-zA-Z0-9]*";
                    std::string rgxsPortPath = "((?:" + rgxsVar + "\\.)(?:" + rgxsVar + "\\.?)+)";
                    std::string rgxsArray = rgxsPortPath + "\\[([0-9]+):([0-9]+)\\]";
                    std::regex rgxNumber("[0-9]+\\.?[0-9]*");
                    std::regex rgxPortPath("^" + rgxsPortPath + "$");
                    std::regex rgxArray("^" + rgxsArray + "$");
                    //std::regex rgxArrayFun("^([a-z]+)\\(" + rgxsArray + "\\)$");
                    std::smatch match;

                    if(name == "sum" || name == "max" || name == "min") {
                        if(name == "sum") ops.push(10);
                        else if(name == "max") ops.push(11);
                        else if(name == "min") ops.push(12);

                    } else if(regex_match(name, match, rgxArray)) {
                        std::string port_name = match.str(1);
                        int pos_start = std::stoi(match.str(2));
                        int pos_end = std::stoi(match.str(3));

                        if(rulesPorts[port_name]->isEmpty()) {
                            throw ParseException("Port " + port_name + "is empty");
                        }

                        Event ev = rulesPorts[port_name]->getSingleValue();
                        values.push(getValuePairFromArrayEvent(ev, pos_start, pos_end));
                    } else if (regex_match(name, match, rgxPortPath)) {
                        if(rulesPorts[name]->isEmpty()) {
                            throw ParseException("Port " + name + "is empty");
                        }
                        Event ev = rulesPorts[name]->getSingleValue();
                        values.push(getValuePairFromEvent(ev));
                    } else {
                        throw ParseException("Term " + name + " not recognized");
                    }

                }
            }

        } else {  // Operator
            char op = expr_rule[i];

            if(i+1 < (int) expr_rule.length() && expr_rule[i+1] == '=') {
                if(op == '!'||op == '=') {
                    i++;
                } else if(op == '<') {
                    i++;
                    op = 'l';
                } else if(op == '>') {
                    i++;
                    op = 'g';
                }
            } else if(i+1 < (int) expr_rule.length() && ((expr_rule[i] == '&' && expr_rule[i+1] == '&')||(expr_rule[i] == '|' && expr_rule[i+1] == '|'))) {
                i++;
            }

            while(!ops.empty() && precedence(ops.top()) >= precedence(op)) {
                solveOperation(values, ops);
            }

            ops.push(op);
        }
    }

    // Process last operators (if any)
    while(!ops.empty()) {
        solveOperation(values, ops);
    }

    //valuePair res = values.top();
    return values.top();


    /*switch(ptrs.top()) {
        case 'i': std::cout << "(i): " << ints.top() << std::endl; break;
        case 'b': std::cout << "(b): " << bools.top() << std::endl; break;
    }*/
}

int RulesEvaluator::precedence(char op){
    if(op == '|'||op == '&')
        return 1;
    if(op == '!'||op == '='||op == '>'||op == '<'||op == 'l'||op == 'g')
        return 2;
    if(op == '+'||op == '-')
        return 3;
    if(op == '*'||op == '/')
        return 4;
    if(op >= 10 && op <= 12)  // Array functions
        return 5;
    return 0;
}

template<typename T>
T RulesEvaluator::applyArithOp(T a, T b, char op){
    switch(op){
        case '+': return a + b;
        case '-': return a - b;
        case '*': return a * b;
        case '/': return a / b;
        //case '%': return a % b;
    }
    return 0;
}

template<typename T>
bool RulesEvaluator::applyLogicOp(T a, T b, char op){
    switch(op) {
        case '!': return a != b;
        case '=': return a == b;
        case '<': return a < b;
        case 'l': return a <= b;
        case '>': return a > b;
        case 'g': return a >= b;
    }
    return false;
}

bool RulesEvaluator::applyLogicOp(bool a, bool b, char op){
    switch(op) {
        case '!': return a != b;
        case '=': return a == b;
        case '&': return a && b;
        case '|': return a || b;
    }
    return false;
}

template<typename T>
bool RulesEvaluator::applyLogicOp(T* arr, T* arr2, int len, char op) {
    if(op == '=') {
        for(int i=0; i<len; i++)
            if(arr[i] != arr2[i])
                return false;
    } else if (op == '!') {
        for(int i=0; i<len; i++)
            if(arr[i] == arr2[i])
                return false;
    } else if (op == 'g') {
        for(int i=0; i<len; i++) {
            if(arr[i] > arr2[i]) {
                return true;
            } else if(arr[i] < arr2[i]) {
                return false;
            }
        }
        return true;
    } else if (op == 'l') {
        for(int i=0; i<len; i++) {
            if(arr[i] < arr2[i]) {
                return true;
            } else if(arr[i] > arr2[i]) {
                return false;
            }
        }
        return true;
    } else if (op == '>') {
        for(int i=0; i<len; i++) {
            if(arr[i] > arr2[i]) {
                return true;
            } else if(arr[i] < arr2[i]) {
                return false;
            }
        }
        return false;
    } else if (op == '<') {
        for(int i=0; i<len; i++) {
            if(arr[i] < arr2[i]) {
                return true;
            } else if(arr[i] > arr2[i]) {
                return false;
            }
        }
        return false;
    }

    return true;
}

template<typename S, typename T>
S* RulesEvaluator::applyArithOp(T* arr, T* arr2, int len, char op) {
    S *res = new S[len];

    if(op == '+') {
        for(int i=0; i<len; i++)
            res[i] = (S) (arr[i] + arr2[i]);
    } else if (op == '-') {
        for(int i=0; i<len; i++)
            res[i] = (S) (arr[i] - arr2[i]);
    } else if (op == '*') {
        for(int i=0; i<len; i++)
            res[i] = (S) (arr[i] * arr2[i]);
    } else if (op == '/') {
        for(int i=0; i<len; i++)
            res[i] = (S) ((S)arr[i] / arr2[i]);
    }

    return res;
}

template<typename T>
void RulesEvaluator::printArray(T* arr, int len) {
    std::cout << "[";
    for(int i=0; i<len; i++) std::cout << arr[i] << " ";
    std::cout << "]";
}

bool RulesEvaluator::hasRules() {
    return !rules.empty();
}

template<typename T>
RulesEvaluator::valuePair RulesEvaluator::getValuePair(T* val) {
    //std::cout << "GVP1: " << std::string(typeid(T).name()) << std::endl;
    return valuePair(typeid(T).name(), std::shared_ptr<T>(val));
}

template<typename T>
RulesEvaluator::valuePair RulesEvaluator::getValuePair(T* val, const char* vtype) {
    //std::cout << "GVP1: " << std::string(typeid(T).name()) << std::endl;
    return valuePair(vtype, std::shared_ptr<T>(val));
}

template<typename T>
RulesEvaluator::valuePair RulesEvaluator::getValuePair(T val) {
    //std::cout << "GVP2: " << std::string(typeid(T).name()) << std::endl;
    return valuePair(typeid(T).name(), std::shared_ptr<T>(new T(val)));
}

RulesEvaluator::valuePair RulesEvaluator::getValuePairFromEvent(Event ev) {
    //std::cout << "GVP3: " << std::string(ev.getType()) << std::endl;
    //return valuePair(ev.getType(), ev.getSharedPtr());

    if(strcmp(ev.getType(), typeid(char).name()) == 0) {
        return getValuePair((long)*(char*) ev.getPtr());

    } else if(strcmp(ev.getType(), typeid(int).name()) == 0) {
        return getValuePair((long)*(int*) ev.getPtr());

    } else if(strcmp(ev.getType(), typeid(float).name()) == 0) {
        return getValuePair((double)*(float*) ev.getPtr());

    } else if(strcmp(ev.getType(), typeid(long).name()) == 0 || strcmp(ev.getType(), typeid(double).name()) == 0) {
        return valuePair(ev.getType(), ev.getSharedPtr());
    } else {
        throw ParseException("Found a not supported data type: " + std::string(ev.getType()));
    }
}

RulesEvaluator::valuePair RulesEvaluator::getValuePairFromArrayEvent(Event ev, int pos_start, int pos_end, std::string fun) {
    int len = pos_end - pos_start;

    std::string vtype = ":" + std::to_string(len) + ":";
    if(strcmp(ev.getType(), typeid(float).name()) == 0 || strcmp(ev.getType(), typeid(double).name()) == 0) {
        vtype += std::string(typeid(double).name());
    } else {
        vtype += std::string(typeid(long).name());
    }
    char *cvtype = new char[vtype.length() + 1];
    strcpy(cvtype, vtype.c_str());


    if(strcmp(ev.getType(), typeid(char).name()) == 0) {
        long *arr = genArray<long>(((char*)ev.getPtr()) + pos_start, len);
        if(fun == "") return getValuePair(arr, (const char*) cvtype);
        else return getValuePair(applyFunction(arr, len, fun));

    } else if(strcmp(ev.getType(), typeid(int).name()) == 0) {
        long *arr = genArray<long>(((int*)ev.getPtr()) + pos_start, len);
        if(fun == "") return getValuePair(arr, (const char*) cvtype);
        else return getValuePair(applyFunction(arr, len, fun));

    } else if(strcmp(ev.getType(), typeid(float).name()) == 0) {
        double *arr = genArray<double>(((float*)ev.getPtr()) + pos_start, len);
        if(fun == "") return getValuePair(arr, (const char*) cvtype);
        else return getValuePair(applyFunction(arr, len, fun));

    } else if(strcmp(ev.getType(), typeid(long).name()) == 0) {
        if(fun == "") return valuePair((const char*) cvtype, ev.getSharedPtr());
        else return getValuePair(applyFunction((long*) ev.getPtr(), len, fun));

    } else if(strcmp(ev.getType(), typeid(double).name()) == 0) {
        if(fun == "") return valuePair((const char*) cvtype, ev.getSharedPtr());
        else return getValuePair(applyFunction((double*) ev.getPtr(), len, fun));
    } else {
        throw ParseException("Found a not supported data type: " + std::string(ev.getType()));
    }
}

int RulesEvaluator::getArrayLen(const char *vp) {
    std::regex rgxArrayType(":([0-9]+):(.*)");
    std::smatch match;

    if(regex_match(std::string(vp), match, rgxArrayType)) {
        return stoi(match.str(1));
    }
    return -1;
}

const char *RulesEvaluator::getArrayType(const char *vp) {
    std::regex rgxArrayType(":([0-9]+):(.*)");
    std::smatch match;

    if(regex_match(std::string(vp), match, rgxArrayType)) {
        return (const char*) match.str(2).c_str();
    }
    return nullptr;
}

void RulesEvaluator::solveOperation(std::stack <valuePair> &values, std::stack <char> &ops) {
    valuePair vp2 = values.top(); values.pop();
    char op = ops.top(); ops.pop();

    if(op >= 10 && op <= 20) {
        // TODO: check if array
        switch(op) {
            case 10:
                if(strcmp(getArrayType(vp2.first), typeid(long).name()) == 0) values.push(getValuePair(applyFunction((long*)vp2.second.get(), getArrayLen(vp2.first), "sum")));
                else values.push(getValuePair(applyFunction((double*)vp2.second.get(), getArrayLen(vp2.first), "sum")));
                break;
            case 11:
                if(strcmp(getArrayType(vp2.first), typeid(long).name()) == 0) values.push(getValuePair(applyFunction((long*)vp2.second.get(), getArrayLen(vp2.first), "max")));
                else values.push(getValuePair(applyFunction((double*)vp2.second.get(), getArrayLen(vp2.first), "max")));
                break;
            case 12:
                if(strcmp(getArrayType(vp2.first), typeid(long).name()) == 0) values.push(getValuePair(applyFunction((long*)vp2.second.get(), getArrayLen(vp2.first), "min")));
                else values.push(getValuePair(applyFunction((double*)vp2.second.get(), getArrayLen(vp2.first), "min")));
                break;
        }
    } else {
        valuePair vp1 = values.top(); values.pop();

        //std::cout << "Operands type are " << std::string(vp1.first) << " and " << std::string(vp2.first) << std::endl;

        if(strcmp(vp1.first, vp2.first) == 0) {
            //std::cout << "Operands type is " << std::string(vp1.first) << std::endl;

            if(vp1.first[0] == ':') { // arrays
                std::regex rgxArrayType(":([0-9]+):(.+)");
                std::smatch match;

                if(regex_match(std::string(vp1.first), match, rgxArrayType)) {
                    int len = std::stoi(match.str(1));
                    const char* vtype = match.str(2).c_str();

                    if(op == '+' || op == '-' || op == '*' || op == '/' || op == '%') {
                        if(strcmp(vtype, typeid(long).name()) == 0) {
                            //printArray((long*) vp1.second.get(), len);
                            //std::cout << " " << op << " ";
                            //printArray((long*) vp2.second.get(), len);
                            //std::cout << std::endl;
                            if(op == '/') {
                                double *res = applyArithOp<double>((long*) vp1.second.get(), (long*)vp2.second.get(), len, op);
                                //printArray(res, len);
                                std::string nvtype = ":" + match.str(1) + ":" + std::string(typeid(double).name());
                                //std::cout << "new type: " << nvtype << std::endl;
                                values.push(valuePair((const char*) nvtype.c_str(), std::shared_ptr<double>(res)));
                            } else {
                                long *res = applyArithOp<long>((long*) vp1.second.get(), (long*)vp2.second.get(), len, op);
                                //printArray(res, len);
                                values.push(valuePair(vp1.first, std::shared_ptr<long>(res)));
                            }

                        } else if(strcmp(vtype, typeid(double).name()) == 0) {
                            //printArray((double*) vp1.second.get(), len);
                            //std::cout << " " << op << " ";
                            //printArray((double*) vp2.second.get(), len);
                            //std::cout << std::endl;
                            double *res = applyArithOp<double>((double*) vp1.second.get(), (double*)vp2.second.get(), len, op);
                            //printArray(res, len);
                            values.push(valuePair(vp1.first, std::shared_ptr<double>(res)));
                        }
                    } else {
                        //printArray((double*) vp1.second.get(), len);
                        //std::cout << " " << op << " ";
                        //printArray((double*) vp2.second.get(), len);
                        //std::cout << std::endl;
                        if(strcmp(vtype, typeid(long).name()) == 0) {
                            bool res = applyLogicOp((long*) vp1.second.get(), (long*)vp2.second.get(), len, op);
                            values.push(getValuePair(res));
                        } else if(strcmp(vtype, typeid(double).name()) == 0) {
                            bool res = applyLogicOp((double*) vp1.second.get(), (double*)vp2.second.get(), len, op);
                            values.push(getValuePair(res));
                        }
                    }

                }

            } else {
                if(strcmp(vp1.first, typeid(long).name()) == 0)
                    values.push(applyOp(*(long*)vp1.second.get(), *(long*)vp2.second.get(), op));

                else if(strcmp(vp1.first, typeid(double).name()) == 0)
                    values.push(applyOp(*(double*)vp1.second.get(), *(double*)vp2.second.get(), op));

                else if(strcmp(vp1.first, typeid(bool).name()) == 0)
                    values.push(getValuePair(applyLogicOp(*(bool*)vp1.second.get(), *(bool*)vp2.second.get(), op)));
            }

        } else if(strcmp(vp1.first, typeid(long).name()) == 0 && strcmp(vp2.first, typeid(double).name()) == 0) {
            values.push(applyOp((double)(*(long*)vp1.second.get()), *(double*)vp2.second.get(), op));

        } else if(strcmp(vp1.first, typeid(double).name()) == 0 && strcmp(vp2.first, typeid(long).name()) == 0) {
            values.push(applyOp(*(double*)vp1.second.get(), (double)(*(long*)vp2.second.get()), op));
        }/// TODO: raise exceptions
    }
}

/*bool RulesEvaluator::isNumericType(const char *tp) {
    return strcmp(tp, typeid(int).name()) == 0 ||
           strcmp(tp, typeid(long).name()) == 0 ||
           strcmp(tp, typeid(float).name()) == 0 ||
           strcmp(tp, typeid(double).name()) == 0 ||
           strcmp(tp, typeid(char).name()) == 0;
}

const char* RulesEvaluator::solveNumericType(const char *tp1, const char *tp2) {
    if(strcmp(tp1, typeid(double).name()) == 0 || strcmp(tp2, typeid(int).name()) == 0) {
        return typeid(double).name();
    } else if(strcmp(tp1, typeid(float).name()) == 0 || strcmp(tp2, typeid(float).name()) == 0) {
        return typeid(float).name();
    } else if(strcmp(tp1, typeid(long).name()) == 0 || strcmp(tp2, typeid(long).name()) == 0) {
        return typeid(long).name();
    } else if(strcmp(tp1, typeid(int).name()) == 0 || strcmp(tp2, typeid(int).name()) == 0) {
        return typeid(int).name();
    } else {
        return typeid(char).name();
    }
}*/

template<typename T>
RulesEvaluator::valuePair RulesEvaluator::applyOp(T a, T b, char op) {
    //std::cout << a << " " << op << " " << b << std::endl;

    if(op == '+' || op == '-' || op == '*' || op == '/' || op == '%') {
        /// TODO: raise exception, div by 0
        return getValuePair(applyArithOp(a, b, op));
    } else {
        return getValuePair(applyLogicOp(a, b, op));
    }
}

template<typename T, typename S>
T* RulesEvaluator::genArray(S *arr, int len) {
    T *nArr = new T[len];
    for(int i=0; i < len; i++) nArr[i] = (T) arr[i];
    //std::cout << "[";
    //for(int i=0; i < len; i++) std::cout << nArr[i] << " ";
    //std::cout << "]" << std::endl;
    return nArr;
}

template<typename T>
T RulesEvaluator::applyFunction(T* arr, int len, std::string fun) {
    T res = 0;
    if(fun == "sum") {
        for(int i=0; i<len; i++) res += arr[i];
    } else if (fun == "max") {
        res = *std::max_element(arr, arr + len);
    } else if (fun == "min") {
        res = *std::min_element(arr, arr + len);
    }
    //std::cout << "Result of " << fun << " function: " << res << std::endl;
    return res;
}

RulesEvaluator::valuePair RulesEvaluator::arrayFromStr(std::string st, char del) {
    int cnt = std::count(st.begin(), st.end(), del);
    double *arr = new double[cnt + 1];

    size_t last = 0;
    size_t next;
    int i = 0;
    while((next = st.find(del, last)) != std::string::npos) {
        arr[i++] = stod(st.substr(last, next-last));
        last = next+1;
    }
    arr[i] = stod(st.substr(last));

    std::string vtype = ":" + std::to_string(cnt+1) + ":" + std::string(typeid(double).name());
    char *cvtype = new char[vtype.length() + 1];
    strcpy(cvtype, vtype.c_str());
    //printArray(arr, cnt+1);

    return valuePair((const char*) cvtype, std::shared_ptr<double>(arr));
}
