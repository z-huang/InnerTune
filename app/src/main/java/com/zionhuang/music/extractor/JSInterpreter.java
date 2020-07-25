package com.zionhuang.music.extractor;

import android.util.Pair;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.zionhuang.music.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;

class JSInterpreter {
    static class InterpretException extends java.lang.Exception {
        InterpretException(String msg) {
            super(msg);
        }
    }

    private interface OperatorFunction {
        JsonElement apply(JsonElement a, JsonElement b);
    }

    private static HashMap<String, OperatorFunction> OPERATORS = new HashMap<>();
    private static HashMap<String, OperatorFunction> ASSIGN_OPERATORS = new HashMap<>();

    static {
        OPERATORS.put("-", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() - b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("+", (a, b) -> {
            if (a.isJsonPrimitive() && b.isJsonPrimitive()) {
                JsonPrimitive ap = a.getAsJsonPrimitive();
                JsonPrimitive bp = b.getAsJsonPrimitive();
                if (ap.isNumber() && bp.isNumber()) {
                    return new JsonPrimitive(a.getAsInt() + b.getAsInt());
                }
                if (ap.isString() && bp.isString()) {
                    return new JsonPrimitive(a.getAsString() + b.getAsString());
                }
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("%", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() % b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("/", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() / b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("*", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() * b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("|", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() | b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("^", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() ^ b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("&", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() & b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put(">>", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() >> b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        OPERATORS.put("<<", (a, b) -> {
            if (a.isJsonPrimitive() && a.getAsJsonPrimitive().isNumber() && b.isJsonPrimitive() && b.getAsJsonPrimitive().isNumber()) {
                return new JsonPrimitive(a.getAsInt() << b.getAsInt());
            }
            return JsonNull.INSTANCE;
        });
        ASSIGN_OPERATORS.put("=", (a, b) -> {
            a = b;
            return a;
        });
        for (Map.Entry<String, OperatorFunction> entry : OPERATORS.entrySet()) {
            ASSIGN_OPERATORS.put(entry.getKey() + "=", entry.getValue());
        }
    }

    private String code;
    private JsonObject objects;
    private HashMap<String, JsonFunction> functions;

    JSInterpreter(String code) {
        this.code = code;
        this.objects = new JsonObject();
        this.functions = new HashMap<>();
    }

    private Pair<JsonElement, Boolean> interpretStatement(String stmt, JsonObject local_vars) throws InterpretException {
        return interpretStatement(stmt, local_vars, 100);
    }

    private Pair<JsonElement, Boolean> interpretStatement(String stmt, JsonObject local_vars, int allow_recursion) throws InterpretException {
        if (allow_recursion < 0) {
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
        return new Pair<>(interpretExpression(expr, local_vars, allow_recursion), abort);
    }

    private JsonElement interpretExpression(String expr, JsonObject local_vars, int allow_recursion) throws InterpretException {
        expr = expr.trim();
        if (expr.length() == 0) {
            return JsonNull.INSTANCE;
        }

        Matcher m;
        if (expr.startsWith("(")) {
            int parens_count = 0;
            boolean found = false;
            m = Pattern.compile("[()]").matcher(expr);
            while (m.find()) {
                found = true;
                if (m.group().equals("(")) {
                    parens_count += 1;
                } else {
                    parens_count -= 1;
                    if (parens_count == 0) {
                        String sub_expr = expr.substring(1, m.start());
                        JsonElement sub_result = interpretExpression(sub_expr, local_vars, allow_recursion);
                        String remaining_expr = expr.substring(m.end()).trim();
                        if (remaining_expr.length() == 0) {
                            return sub_result;
                        } else {
                            expr = sub_result.toString() + remaining_expr;
                        }
                        break;
                    }
                }
            }
            if (!found) {
                throw new InterpretException("Premature end of parens at " + expr);
            }
        }

        for (Map.Entry<String, OperatorFunction> entry : ASSIGN_OPERATORS.entrySet()) {
            m = Pattern.compile("(?x)(?<out>[a-zA-Z_$][a-zA-Z_$0-9]*)(?:\\[(?<index>[^\\]]+?)\\])?\\s*%s(?<expr>.*)$".replace("%s", Pattern.quote(entry.getKey()))).matcher(expr);
            if (!m.matches()) {
                continue;
            }
            JsonElement right_val = interpretExpression(m.group("expr"), local_vars, allow_recursion - 1);
            if (m.group("index") != null) {
                int idx = interpretExpression(m.group("index"), local_vars, allow_recursion).getAsInt();
                JsonElement lvar = local_vars.get(m.group("out"));
                if (!lvar.isJsonArray()) {
                    throw new InterpretException("Unsupported operator [] in non-array object at "+expr);
                }
                JsonArray jsonArray = lvar.getAsJsonArray();
                JsonElement cur = jsonArray.get(idx);
                JsonElement val = entry.getValue().apply(cur, right_val);
                jsonArray.set(idx, val);
                return val;
            } else {
                JsonElement cur = local_vars.get(m.group("out"));
                JsonElement val = entry.getValue().apply(cur, right_val);
                local_vars.add(Objects.requireNonNull(m.group("out")), val);
                return val;
            }
        }

        if (StringUtils.isNumeric(expr)) {
            return new JsonPrimitive(Integer.parseInt(expr));
        }

        m = Pattern.compile("(?!if|return|true|false)(?<name>[a-zA-Z_$][a-zA-Z_$0-9]*)$").matcher(expr);
        if (m.matches()) {
            return local_vars.get(m.group("name"));
        }

        try {
            // check if is string first, because method call will be parsed into string
            for (char quote : new char[]{'\"', '\''}) {
                if (expr.charAt(0) == quote && expr.charAt(expr.length() - 1) == quote) {
                    expr = expr.substring(1, expr.length() - 1);
                    return new JsonPrimitive(expr);
                }
            }
            JsonElement res = JsonParser.parseString(expr);
            if (!res.isJsonPrimitive() || !res.getAsJsonPrimitive().isString()) {
                return res;
            }
        } catch (JsonSyntaxException ignored) {
        }

        m = Pattern.compile("(?<in>[a-zA-Z_$][a-zA-Z_$0-9]*)\\[(?<idx>.+)\\]$").matcher(expr);
        if (m.matches()) {
            JsonElement val = local_vars.get(m.group("in"));
            JsonElement idx = interpretExpression(m.group("idx"), local_vars, allow_recursion - 1);
            if (val.isJsonObject()) {
                return val.getAsJsonObject().get(idx.getAsString());
            } else if (val.isJsonArray()) {
                return val.getAsJsonArray().get(idx.getAsInt());
            } else if (val.isJsonPrimitive() && val.getAsJsonPrimitive().isString()) {
                return new JsonPrimitive(val.getAsString().charAt(idx.getAsInt()));
            } else {
                throw new InterpretException("Unsupported operation [] for expression " + expr);
            }
        }

        m = Pattern.compile("(?<var>[a-zA-Z_$][a-zA-Z_$0-9]*)(?:\\.(?<member>[^(]+)|\\[(?<member2>[^]]+)\\])\\s*(?:\\(+(?<args>[^()]*)\\))?$").matcher(expr);
        if (m.matches()) {
            String variable = m.group("var");
            String member = Utils.removeQuotes(m.group("member") != null ? m.group("member") : m.group("member2"));
            String arg_str = m.group("args");

            Objects.requireNonNull(variable);
            JsonElement obj;
            if (local_vars.has(variable)) {
                obj = local_vars.get(variable);
            } else {
                if (!objects.has(variable)) {
                    objects.add(variable, extractObject(variable));
                }
                obj = objects.get(variable);
            }

            if (arg_str == null) {
                if (Objects.equals(member, "length")) {
                    if (obj.isJsonArray()) {
                        return new JsonPrimitive(obj.getAsJsonArray().size());
                    } else if (obj.isJsonPrimitive() && obj.getAsJsonPrimitive().isString()) {
                        return new JsonPrimitive(obj.getAsString().length());
                    } else {
                        return JsonNull.INSTANCE;
                    }
                }
                if (obj.isJsonArray()) {
                    return obj.getAsJsonArray().get(Integer.parseInt(Objects.requireNonNull(member)));
                } else if (obj.isJsonObject()) {
                    return obj.getAsJsonObject().get(member);
                } else if (obj.isJsonPrimitive() && obj.getAsJsonPrimitive().isString()) {
                    return new JsonPrimitive(obj.getAsString().charAt(Integer.parseInt(Objects.requireNonNull(member))));
                } else {
                    return JsonNull.INSTANCE;
                }
            }

            if (!expr.endsWith(")")) {
                throw new InterpretException("Expression missing ) at " + expr);
            }

            // Function call
            JsonArray argvals = new JsonArray();
            if (arg_str.length() > 0) {
                for (String v : arg_str.split(",")) {
                    argvals.add(interpretExpression(v, local_vars, allow_recursion));
                }
            }

            if (Objects.equals(member, "split")) {
                if (!obj.isJsonPrimitive() || !obj.getAsJsonPrimitive().isString()) {
                    throw new InterpretException("Calling split function on non-string object");
                }
                if (argvals.get(0).getAsString().length() != 0) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                JsonArray res = new JsonArray();
                for (char c : obj.getAsString().toCharArray()) {
                    res.add(c);
                }
                return res;
            }
            if (Objects.equals(member, "join")) {
                if (!obj.isJsonArray()) {
                    throw new InterpretException("Calling join function on non-array type at " + expr);
                }
                if (argvals.size() != 1) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                if (!argvals.get(0).isJsonPrimitive() || !argvals.get(0).getAsJsonPrimitive().isString()) {
                    throw new InterpretException("Error argument type at " + expr);
                }
                StringJoiner joiner = new StringJoiner(argvals.get(0).getAsString());
                for (JsonElement ele : obj.getAsJsonArray()) {
                    joiner.add(Utils.removeQuotes(ele.toString()));
                }
                return new JsonPrimitive(joiner.toString());
            }
            if (Objects.equals(member, "reverse")) {
                if (!obj.isJsonArray()) {
                    throw new InterpretException("Calling reverse function on non-array type at " + expr);
                }
                if (argvals.size() != 0) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                JsonArray arr = obj.getAsJsonArray();
                int size = arr.size();
                // swap
                for (int i = 0; i < size >> 1; i++) {
                    JsonElement temp = arr.get(i);
                    arr.set(i, arr.get(size - i - 1));
                    arr.set(size - i - 1, temp);
                }
                return arr;
            }
            if (Objects.equals(member, "slice")) {
                if (argvals.size() != 1) {
                    throw new InterpretException("Parameter error at " + expr);
                }
                return new JsonPrimitive(obj.getAsString().substring(argvals.get(0).getAsInt()));
            }
            if (Objects.equals(member, "splice")) {
                if (!obj.isJsonArray()) {
                    throw new InterpretException("Can't call a function splice which is not a JsonArray type at " + expr);
                }
                int index = argvals.get(0).getAsInt();
                int howMany = argvals.get(1).getAsInt();
                JsonArray objArr = obj.getAsJsonArray();
                JsonArray res = new JsonArray();
                for (int i = index; i < min(index + howMany, objArr.size()); i++) {
                    res.add(objArr.remove(index));
                }
                return res;
            }

            // custom function
            if (obj.isJsonObject()) {
                JsonElement fn = obj.getAsJsonObject().get(member);
                if (!(fn instanceof JsonFunction)) {
                    throw new InterpretException("Calling function on a non-function object at " + expr);
                }
                return ((JsonFunction) fn).apply(argvals);
            }
        }

        for (Map.Entry<String, OperatorFunction> entry : OPERATORS.entrySet()) {
            m = Pattern.compile("(?<x>.+?)" + Pattern.quote(entry.getKey()) + "(?<y>.+)").matcher(expr);
            if (!m.matches()) {
                continue;
            }
            Pair<JsonElement, Boolean> pair_x = interpretStatement(m.group("x"), local_vars, allow_recursion - 1);
            if (pair_x.second) {
                throw new InterpretException("Premature left-side return of " + entry.getKey() + " at " + expr);
            }
            Pair<JsonElement, Boolean> pair_y = interpretStatement(m.group("y"), local_vars, allow_recursion - 1);
            if (pair_y.second) {
                throw new InterpretException("Premature right-side return of " + entry.getKey() + " at " + expr);
            }
            return entry.getValue().apply(pair_x.first, pair_y.first);
        }

        m = Pattern.compile("^(?<func>[a-zA-Z_$][a-zA-Z_$0-9]*)\\((?<args>[a-zA-Z0-9_$,]*)\\)$").matcher(expr);
        if (m.matches()) {
            String fname = m.group("func");
            String args = m.group("args");
            Objects.requireNonNull(args);

            JsonArray argvals = new JsonArray();
            if (args.length() > 0) {
                for (String v : args.split(",")) {
                    argvals.add(StringUtils.isNumeric(v) ? new JsonPrimitive(Integer.parseInt(v)) : local_vars.get(v));
                }
            }
            if (!functions.containsKey(fname)) {
                functions.put(fname, extractFunction(fname));
            }
            return Objects.requireNonNull(functions.get(fname)).apply(argvals);
        }
        throw new InterpretException("Unsupported JS expression " + expr);
    }

    private JsonObject extractObject(String objname) throws InterpretException {
        JsonObject obj = new JsonObject();
        Matcher obj_m = Pattern.compile("(?x)(?<!this\\.)" + objname + "\\s*=\\s*\\{\\s*(?<fields>((?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+')\\s*:\\s*function\\s*\\(.*?\\)\\s*\\{.*?\\}(?:,\\s*)?)*)\\}\\s*;").matcher(code);
        if (!obj_m.find()) {
            throw new InterpretException("Failed to extract object " + objname);
        }
        String fields = obj_m.group("fields");
        Objects.requireNonNull(fields);

        Matcher fields_m = Pattern.compile("(?x)(?<key>(?:[a-zA-Z$0-9]+|\"[a-zA-Z$0-9]+\"|'[a-zA-Z$0-9]+'))\\s*:\\s*function\\s*\\((?<args>[a-z,]+)\\)\\{(?<code>[^}]+)\\}").matcher(fields);
        while (fields_m.find()) {
            String[] argnames = Objects.requireNonNull(fields_m.group("args")).split(",");
            obj.add(Objects.requireNonNull(Utils.removeQuotes(fields_m.group("key"))), buildFunction(argnames, fields_m.group("code")));
        }
        return obj;
    }

    public JsonFunction extractFunction(String funcname) throws InterpretException {
        Matcher m = Pattern.compile("(?x)(?:function\\s+" + funcname + "|[{;,]\\s*" + funcname + "\\s*=\\s*function|var\\s+" + funcname + "\\s*=\\s*function)\\s*\\((?<args>[^)]*)\\)\\s*\\{(?<code>[^}]+)\\}").matcher(code);
        if (!m.find()) {
            throw new InterpretException("Couldn't find JS function " + funcname);
        }
        String[] argnames = Objects.requireNonNull(m.group("args")).split(",");
        return buildFunction(argnames, m.group("code"));
    }

    public JsonElement callFunction(String funcname, JsonArray args) throws InterpretException {
        return extractFunction(funcname).apply(args);
    }

    private JsonFunction buildFunction(final String[] argnames, final String code) {
        return new JsonFunction() {
            @Override
            public JsonElement apply(JsonArray args) throws InterpretException {
                JsonObject local_vars = new JsonObject();
                for (int i = 0; i < args.size() && i < argnames.length; i++) {
                    local_vars.add(argnames[i], args.get(i));
                }
                Pair<JsonElement, Boolean> pair;
                JsonElement res = JsonNull.INSTANCE;
                for (String stmt : code.split(";")) {
                    pair = interpretStatement(stmt, local_vars);
                    res = pair.first;
                    if (pair.second) {
                        break;
                    }
                }
                return res;
            }
        };
    }
}
