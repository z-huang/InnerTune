package com.zionhuang.music.extractor;

import androidx.core.util.Pair;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.zionhuang.music.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

public class JSInterpreter {
    public static class InterpretException extends java.lang.Exception {
        InterpretException(String msg) {
            super(msg);
        }
    }

    private interface OperatorFunction {
        JSON apply(JSON a, JSON b);
    }

    private static LinkedHashMap<String, OperatorFunction> OPERATORS = new LinkedHashMap<>();
    private static LinkedHashMap<String, OperatorFunction> ASSIGN_OPERATORS = new LinkedHashMap<>();

    static {
        OPERATORS.put("|", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() | b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("^", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() ^ b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("&", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() & b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put(">>", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() >> b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("<<", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() << b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("-", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() - b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("+", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() + b.getAsInt());
            }
            if (a.isString() && b.isString()) {
                return JSON.fromString(a.getAsString() + b.getAsString());
            }
            return JSON.NULL;
        });
        OPERATORS.put("%", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() % b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("/", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() / b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.put("*", (a, b) -> {
            if (a.isNumber() && b.isNumber()) {
                return JSON.fromNumber(a.getAsInt() * b.getAsInt());
            }
            return JSON.NULL;
        });
        OPERATORS.forEach((key, value) -> ASSIGN_OPERATORS.put(key + "=", value));
        ASSIGN_OPERATORS.put("=", (a, b) -> b);
    }

    private String code;
    private JSON objects;
    private HashMap<String, JSFunction> functions;

    public JSInterpreter(String code) {
        this.code = code;
        this.objects = JSON.createObject();
        this.functions = new HashMap<>();
    }

    private Pair<JSON, Boolean> interpretStatement(String stmt, JSON localVars) throws InterpretException {
        return interpretStatement(stmt, localVars, 100);
    }

    private Pair<JSON, Boolean> interpretStatement(String stmt, JSON localVars, int allowRecursion) throws InterpretException {
        if (allowRecursion < 0) {
            throw new InterpretException("Recursion limit reached");
        }
        boolean abort = false;
        String expr;
        stmt = stmt.trim();
        Matcher m;
        m = Pattern.compile("^var\\s").matcher(stmt);
        if (m.find()) {
            expr = stmt.substring(m.end(0));
        } else {
            m = Pattern.compile("^return(?:\\s+|$)").matcher(stmt);
            if (m.find()) {
                expr = stmt.substring(m.end(0));
                abort = true;
            } else {
                expr = stmt;
            }
        }
        return new Pair<>(interpretExpression(expr, localVars, allowRecursion), abort);
    }

    private JSON interpretExpression(String expr, JSON localVars, int allowRecursion) throws InterpretException {
        expr = expr.trim();
        if (expr.isEmpty()) {
            return JSON.NULL;
        }

        Matcher m;
        if (expr.startsWith("(")) {
            int parenCount = 0;
            boolean found = false;
            m = Pattern.compile("[()]").matcher(expr);
            while (m.find()) {
                found = true;
                if (m.group().equals("(")) {
                    parenCount += 1;
                } else {
                    parenCount -= 1;
                    if (parenCount == 0) {
                        String subExpr = expr.substring(1, m.start());
                        JSON subResult = interpretExpression(subExpr, localVars, allowRecursion);
                        String remainingExpr = expr.substring(m.end()).trim();
                        if (remainingExpr.isEmpty()) {
                            return subResult;
                        } else {
                            expr = subResult.toString() + remainingExpr;
                        }
                        break;
                    }
                }
            }
            if (!found) {
                throw new InterpretException("Premature end of parenthesis at " + expr);
            }
        }

        for (Map.Entry<String, OperatorFunction> entry : ASSIGN_OPERATORS.entrySet()) {
            m = Pattern.compile("(?x)(?<out>[a-zA-Z_$][a-zA-Z_$0-9]*)(?:\\[(?<index>[^\\]]+?)\\])?\\s*%s(?<expr>.*)$".replace("%s", Pattern.quote(entry.getKey()))).matcher(expr);
            if (!m.matches()) {
                continue;
            }
            JSON rightVal = interpretExpression(m.group("expr"), localVars, allowRecursion - 1);
            if (m.group("index") != null) {
                int idx = interpretExpression(m.group("index"), localVars, allowRecursion).getAsInt();
                JSON lVar = localVars.get(m.group("out"));
                if (!lVar.isArray()) {
                    throw new InterpretException("Unsupported operator [] in non-array object at " + expr);
                }
                JSON val = entry.getValue().apply(lVar.get(idx), rightVal);
                lVar.set(idx, val);
                return val;
            } else {
                JSON cur = localVars.get(m.group("out"));
                JSON val = entry.getValue().apply(cur, rightVal);
                localVars.add(Objects.requireNonNull(m.group("out")), val);
                return val;
            }
        }

        if (StringUtils.isNumeric(expr)) {
            return JSON.fromNumber(Integer.parseInt(expr));
        }

        m = Pattern.compile("(?!if|return|true|false)(?<name>[a-zA-Z_$][a-zA-Z_$0-9]*)$").matcher(expr);
        if (m.matches()) {
            return localVars.get(m.group("name"));
        }

        try {
            // check if is string first, because method call will be parsed into string
            for (char quote : new char[]{'\"', '\''}) {
                if (expr.charAt(0) == quote && expr.charAt(expr.length() - 1) == quote) {
                    expr = expr.substring(1, expr.length() - 1);
                    return JSON.fromString(expr);
                }
            }
            JSON res = new JSON(JsonParser.parseString(expr));
            if (!res.isString()) {
                return res;
            }
        } catch (JsonSyntaxException ignored) {
        }

        m = Pattern.compile("(?<in>[a-zA-Z_$][a-zA-Z_$0-9]*)\\[(?<idx>.+)\\]$").matcher(expr);
        if (m.matches()) {
            JSON val = localVars.get(m.group("in"));
            JSON idx = interpretExpression(m.group("idx"), localVars, allowRecursion - 1);
            if (val.isObject()) {
                return val.get(idx.getAsString());
            } else if (val.isArray()) {
                return val.get(idx.getAsInt());
            } else if (val.isString()) {
                return JSON.fromChar(val.getAsString().charAt(idx.getAsInt()));
            } else {
                throw new InterpretException("Unsupported operation [] for expression " + expr);
            }
        }

        m = Pattern.compile("(?<var>[a-zA-Z_$][a-zA-Z_$0-9]*)(?:\\.(?<member>[^(]+)|\\[(?<member2>[^]]+)\\])\\s*(?:\\(+(?<args>[^()]*)\\))?$").matcher(expr);
        if (m.matches()) {
            String variable = m.group("var");
            String member = Utils.removeQuotes(m.group("member") != null ? m.group("member") : m.group("member2"));
            String argStr = m.group("args");

            Objects.requireNonNull(variable);
            JSON obj;
            if (localVars.has(variable)) {
                obj = localVars.get(variable);
            } else {
                if (!objects.has(variable)) {
                    objects.add(variable, extractObject(variable));
                }
                obj = objects.get(variable);
            }

            if (argStr == null) {
                if (Objects.equals(member, "length")) {
                    if (obj.isArray()) {
                        return JSON.fromNumber(obj.size());
                    } else if (obj.isString()) {
                        return JSON.fromNumber(obj.getAsString().length());
                    } else {
                        return JSON.NULL;
                    }
                }
                if (obj.isArray()) {
                    return obj.get(Integer.parseInt(Objects.requireNonNull(member)));
                } else if (obj.isObject()) {
                    return obj.get(member);
                } else if (obj.isString()) {
                    return JSON.fromChar(obj.getAsString().charAt(Integer.parseInt(Objects.requireNonNull(member))));
                } else {
                    return JSON.NULL;
                }
            }

            if (!expr.endsWith(")")) {
                throw new InterpretException("Expression missing ) at " + expr);
            }

            // Function call
            JSON argValues = JSON.createArray();
            if (argStr.length() > 0) {
                for (String v : argStr.split(",")) {
                    argValues.add(interpretExpression(v, localVars, allowRecursion));
                }
            }

            if (Objects.equals(member, "split")) {
                if (!obj.isString()) {
                    throw new InterpretException("Calling split function on a non-string object");
                }
                if (argValues.getString(0).length() != 0) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                JSON res = JSON.createArray();
                for (char c : obj.getAsString().toCharArray()) {
                    res.add(c);
                }
                return res;
            }
            if (Objects.equals(member, "join")) {
                if (!obj.isArray()) {
                    throw new InterpretException("Calling join function on non-array type at " + expr);
                }
                if (argValues.size() != 1) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                if (!argValues.get(0).isString()) {
                    throw new InterpretException("Error argument type at " + expr);
                }
                StringJoiner joiner = new StringJoiner(argValues.get(0).getAsString());
                obj.forEach(ele -> joiner.add(Utils.removeQuotes(ele.toString())));
                return JSON.fromString(joiner.toString());
            }
            if (Objects.equals(member, "reverse")) {
                if (!obj.isArray()) {
                    throw new InterpretException("Calling reverse function on non-array type at " + expr);
                }
                if (argValues.size() != 0) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                int size = obj.size();
                // swap
                for (int i = 0; i < (size >> 1); i++) {
                    JSON temp = obj.get(i);
                    obj.set(i, obj.get(size - i - 1));
                    obj.set(size - i - 1, temp);
                }
                return obj;
            }
            if (Objects.equals(member, "slice")) {
                if (argValues.size() != 1) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                return JSON.fromString(obj.getAsString().substring(argValues.getInt(0)));
            }
            if (Objects.equals(member, "splice")) {
                if (!obj.isArray()) {
                    throw new InterpretException("Can't call a function splice which is not a JsonArray type at " + expr);
                }
                int index = argValues.get(0).getAsInt();
                int howMany = argValues.get(1).getAsInt();
                JSON res = JSON.createArray();
                for (int i = index; i < min(index + howMany, obj.size()); i++) {
                    res.add(obj.remove(index));
                }
                return res;
            }

            // custom function
            if (obj.isObject()) {
                JSON fn = obj.get(member);
                if (!fn.isFunction()) {
                    throw new InterpretException("Calling function on a non-function object at " + expr);
                }
                return fn.getAsFunction().apply(argValues);
            }
        }

        for (Map.Entry<String, OperatorFunction> entry : OPERATORS.entrySet()) {
            m = Pattern.compile("(?<x>.+?)" + Pattern.quote(entry.getKey()) + "(?<y>.+)").matcher(expr);
            if (!m.matches()) {
                continue;
            }
            Pair<JSON, Boolean> pairX = interpretStatement(m.group("x"), localVars, allowRecursion - 1);
            if (Objects.requireNonNull(pairX.second)) {
                throw new InterpretException("Premature left-side return of " + entry.getKey() + " at " + expr);
            }
            Pair<JSON, Boolean> pairY = interpretStatement(m.group("y"), localVars, allowRecursion - 1);
            if (Objects.requireNonNull(pairY.second)) {
                throw new InterpretException("Premature right-side return of " + entry.getKey() + " at " + expr);
            }
            return entry.getValue().apply(pairX.first, pairY.first);
        }

        m = Pattern.compile("^(?<func>[a-zA-Z_$][a-zA-Z_$0-9]*)\\((?<args>[a-zA-Z0-9_$,]*)\\)$").matcher(expr);
        if (m.matches()) {
            String funcName = m.group("func");
            String args = m.group("args");
            Objects.requireNonNull(args);

            JSON argValues = JSON.createArray();
            if (args.length() > 0) {
                for (String v : args.split(",")) {
                    argValues.add(StringUtils.isNumeric(v) ? JSON.fromNumber(Integer.parseInt(v)) : localVars.get(v));
                }
            }
            if (!functions.containsKey(funcName)) {
                functions.put(funcName, extractFunction(funcName));
            }
            return Objects.requireNonNull(functions.get(funcName)).apply(argValues);
        }
        throw new InterpretException("Unsupported JS expression " + expr);
    }

    private JSON extractObject(String objName) throws InterpretException {
        JSON obj = JSON.createObject();
        Matcher objM = Pattern.compile("(?x)(?<!this\\.)" + objName + "\\s*=\\s*\\{\\s*(?<fields>((?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+')\\s*:\\s*function\\s*\\(.*?\\)\\s*\\{.*?\\}(?:,\\s*)?)*)\\}\\s*;").matcher(code);
        if (!objM.find()) {
            throw new InterpretException("Failed to extract object " + objName);
        }
        String fields = objM.group("fields");
        Objects.requireNonNull(fields);

        Matcher fieldsM = Pattern.compile("(?x)(?<key>(?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+'))\\s*:\\s*function\\s*\\((?<args>[a-z,]+)\\)\\{(?<code>[^}]+)\\}").matcher(fields);
        while (fieldsM.find()) {
            String[] argNames = Objects.requireNonNull(fieldsM.group("args")).split(",");
            obj.add(Objects.requireNonNull(Utils.removeQuotes(fieldsM.group("key"))), JSON.fromFunction(buildFunction(argNames, fieldsM.group("code"))));
        }
        return obj;
    }

    public JSFunction extractFunction(String funcName) throws InterpretException {
        String escapedFnName = Pattern.quote(funcName);
        Matcher m = Pattern.compile("(?x)(?:function\\s+" + escapedFnName + "|[{;,]\\s*" + escapedFnName + "\\s*=\\s*function|var\\s+" + escapedFnName + "\\s*=\\s*function)\\s*\\((?<args>[^)]*)\\)\\s*\\{(?<code>[^}]+)\\}").matcher(code);
        if (!m.find()) {
            throw new InterpretException("Couldn't find JS function " + funcName);
        }
        String[] argNames = Objects.requireNonNull(m.group("args")).split(",");
        return buildFunction(argNames, m.group("code"));
    }

    public JSON callFunction(String funcName, Object... args) throws InterpretException {
        return callFunction(funcName, JSON.toArray(args));
    }

    public JSON callFunction(String funcName, JSON args) throws InterpretException {
        return extractFunction(funcName).apply(args);
    }

    private JSFunction buildFunction(final String[] argNames, final String code) {
        return new JSFunction() {
            @Override
            public JSON apply(JSON args) throws InterpretException {
                JSON localVars = JSON.createObject();
                for (int i = 0; i < args.size() && i < argNames.length; i++) {
                    localVars.add(argNames[i], args.get(i));
                }
                Pair<JSON, Boolean> pair;
                JSON res = JSON.NULL;
                for (String stmt : code.split(";")) {
                    pair = interpretStatement(stmt, localVars);
                    res = pair.first;
                    if (Objects.requireNonNull(pair.second)) {
                        break;
                    }
                }
                return res;
            }
        };
    }
}
