#ifndef RULESEVALUATOR_H_INCLUDED
#define RULESEVALUATOR_H_INCLUDED

#include "../modeling/Coupled.h"

#include<iostream>
#include<stack>
#include<map>
#include<string>
#include<algorithm>
#include<stdexcept>
#include<fstream>
#include<typeinfo>
#include<memory>
#include<regex>

#include "../util/rapidjson/document.h"
#include "../util/rapidjson/istreamwrapper.h"

class RulesEvaluator
{
    public:
        RulesEvaluator() {};
        void parseRules(std::string rulesFilePath, Coupled* model);
        void checkRules();
        bool hasRules();

    private:
        typedef std::pair<const char*, std::shared_ptr<void>> valuePair;

        const int LVL_INFO = 0;
        const int LVL_ERROR = 1;

        typedef struct { std::string expr; int level; } ruleInfo;

        std::map<std::string, std::string> rulesVars;
        std::map<std::string, ruleInfo> rules;
        std::map<std::string, Port*> rulesPorts;

        std::map<std::string, valuePair> varsValues;
        //std::map<std::string, bool> portsValuesBool;

        valuePair parseExpr(std::string expr_name, std::string expr_rule, bool var_expr = false);
        int precedence(char op);
        template<typename T> valuePair applyOp(T a, T b, char op);
        template<typename T> T applyArithOp(T a, T b, char op);
        template<typename T> bool applyLogicOp(T a, T b, char op);
        template<typename T> bool applyLogicOp(T* a, T* b, int len, char op);
        template<typename S, typename T> S* applyArithOp(T* arr, T* arr2, int len, char op);
        template<typename T> T applyFunction(T* arr, int len, std::string fun);
        bool applyLogicOp(bool a, bool b, char op);
        template<typename T> valuePair getValuePair(T* val);
        template<typename T> valuePair getValuePair(T* val, const char* vtype);
        template<typename T> valuePair getValuePair(T val);
        valuePair getValuePairFromEvent(Event ev);
        valuePair getValuePairFromArrayEvent(Event ev, int pos_start, int pos_end, std::string fun = "");
        void solveOperation(std::stack <valuePair> &values, std::stack <char> &ops);
        template<typename T, typename S> T *genArray(S *arr, int len);
        template<typename T> void printArray(T* arr, int len);
        valuePair arrayFromStr(std::string st, char del);
        int getArrayLen(const char *vp);
        const char *getArrayType(const char *vp);
        /*bool isNumericType(const char *tp);
        const char* solveNumericType(const char *tp1, const char *tp2);*/
};

class ParseException: public std::exception {
private:
    std::string message_;
public:
    explicit ParseException(const std::string& message): message_(message) {};
    virtual const char* what() const throw() {
        return message_.c_str();
    }
};

#endif // RULESEVALUATOR_H_INCLUDED
