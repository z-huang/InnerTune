@file:Suppress("RegExpRedundantEscape")

package com.zionhuang.music.youtube.utils

import com.google.gson.*
import com.zionhuang.music.extensions.*
import okhttp3.internal.toImmutableMap
import org.intellij.lang.annotations.Language

typealias OperatorFunction = (JsonElement, JsonElement) -> JsonElement

class JsInterpreter(private val code: String) {
    private val objects = JsonObject()
    private val functions = HashMap<String, JsFunction>()

    @Throws(InterpretException::class)
    private fun interpretStatement(stmt: String, localVars: JsonObject, allowRecursion: Int = 100): Pair<JsonElement, Boolean> {
        if (allowRecursion < 0) throw InterpretException("Recursion limit exceed")
        var abort = false
        val expr: String = with(stmt.trim()) {
            find(VAR_RE)?.let { substring(it.range.last + 1) }
                    ?: find(RETURN_RE)?.let { abort = true;substring(it.range.last + 1) }
                    ?: this
        }
        return Pair(interpretExpression(expr, localVars, allowRecursion), abort)
    }

    @Throws(InterpretException::class)
    private fun interpretExpression(originExpr: String, localVars: JsonObject, allowRecursion: Int): JsonElement {
        var expr = originExpr.trim()
        if (expr.isEmpty()) JsonNull.INSTANCE
        if (expr.startsWith("(")) {
            var parenCount = 0
            var found = false
            for (match in expr.findAll("""[()]""")) {
                found = true
                if (match.value == "(") {
                    parenCount++
                } else {
                    parenCount--
                    if (parenCount == 0) {
                        val subExpr = expr[1, match.range.first]
                        val subResult = interpretExpression(subExpr, localVars, allowRecursion)
                        val remainingExpr = expr.substring(match.range.last + 1).trim()
                        if (remainingExpr.isEmpty()) {
                            return subResult
                        } else {
                            expr = subResult.toString() + remainingExpr
                        }
                        break
                    }
                }
            }
            if (!found) {
                throw InterpretException("Premature end of parenthesis at $expr")
            }
        }

        for ((op, opFn) in ASSIGN_OPERATORS) {
            val match = expr.matchEntire(ASSIGN_OPERATOR_RE % op.escape()) ?: continue
            val group = match.groups
            val rightVal = interpretExpression(group["expr"]!!.value, localVars, allowRecursion - 1)
            return if (group["index"] != null) {
                val idx = interpretExpression(group["index"]!!.value, localVars, allowRecursion).asIntOrNull
                        ?: throw InterpretException("Index is not a integer: $expr")
                val lVar = localVars[group["out"]!!.value].asJsonArrayOrNull
                        ?: throw InterpretException("Expected an array object at the left-hand value: $expr")
                opFn(lVar[idx], rightVal).also {
                    lVar[idx] = it
                }
            } else {
                val lVarName = group["out"]!!.value
                val lVar = localVars[lVarName] ?: JsonNull.INSTANCE
                opFn(lVar, rightVal).also {
                    localVars[lVarName] = it
                }
            }
        }

        expr.toIntOrNull()?.let { return it.toJsonPrimitive() }

        expr.matchEntire(VARIABLE_RE, "name")?.let {
            return localVars[it] ?: throw InterpretException("variable $it not found")
        }

        if (expr.length > 1) {
            for (quote in arrayOf('\"', '\'')) {
                if (expr.first() == quote && expr.last() == quote) {
                    return expr[1, expr.length - 1].toJsonPrimitive()
                }
            }
        }

        try {
            // whenever the expression is not a string type, parseJsonString always parse it into a string
            val parsed = expr.parseJsonString()
            if (!parsed.isString()) return parsed
        } catch (e: JsonSyntaxException) {
        }

        expr.matchEntire(VARIABLE_INDEX_RE)?.let { match ->
            val lVar = localVars[match.groupValue("in")!!]
            val idx = interpretExpression(match.groupValue("idx")!!, localVars, allowRecursion - 1)
            return when {
                idx.isString() -> lVar[idx.asString]
                idx.isNumber() -> lVar[idx.asInt]
                else -> throw InterpretException("index is neither string nor int")
            } ?: throw InterpretException("property $idx doesn't exist")
        }

        expr.matchEntire(MEMBER_RE)?.let { match ->
            val varName = match.groupValue("var")!!
            val obj = localVars[varName] ?: extractObject(varName).also {
                objects[varName] = it
            }
            val member = (match.groupValue("member")
                    ?: match.groupValue("member2")!!).removeQuotes()
            val argStr = match.groupValue("args")

            if (argStr == null) {
                if (member == "length") {
                    return when {
                        obj is JsonArray -> obj.size()
                        obj is JsonPrimitive && obj.isString -> obj.asString.length
                        else -> throw InterpretException("""$varName has no member "length"""")
                    }.toJsonPrimitive()
                }
                return obj.asJsonObjectOrNull[member]
                        ?: throw InterpretException("""$varName doesn't exist or doesn't have the member "$member"""")
            }

            // Function call
            val argValues = argStr.split(",").map { interpretExpression(it, localVars, allowRecursion) }.toJsonArray()
            return when (member) {
                "split" -> {
                    // assert argValues == [""]
                    obj.asString.splitToJsonArray()
                }
                "join" -> {
                    // assert argValues.length == 1
                    obj.asJsonArray.joinToString(argValues[0].asString) {
                        it.asString.removeQuotes()
                    }.toJsonPrimitive()
                }
                "reverse" -> {
                    obj.asJsonArray.selfReverse()
                }
                "slice" -> {
                    obj.asString.substring(argValues[0].asInt).toJsonPrimitive()
                }
                "splice" -> {
                    val (index, howMany) = argValues.map { it.asInt }
                    if (obj !is JsonArray) throw InterpretException("$varName is not an object")
                    JsonArray().apply {
                        for (i in index until minOf(index + howMany, obj.size())) {
                            add(obj.remove(index))
                        }
                    }
                }
                else -> {
                    (obj[member]!! as JsFunction)(argValues)
                }
            }
        }

        for ((op, opFn) in OPERATORS) {
            val match = expr.matchEntire("""(?<x>.+?)${op.escape()}(?<y>.+)""") ?: continue
            val (x, lAbort) = interpretStatement(match.groupValue("x")!!, localVars, allowRecursion - 1)
            if (lAbort) throw InterpretException("Premature left-side return of $op at $expr")
            val (y, rAbort) = interpretStatement(match.groupValue("y")!!, localVars, allowRecursion - 1)
            if (rAbort) throw InterpretException("Premature right-side return of $op at $expr")
            return opFn(x, y)
        }

        expr.matchEntire(FN_CALL_RE)?.let { match ->
            val fnName = match.groupValue("func")!!
            val argStr = match.groupValue("args")!!
            val argValues = argStr.split(",").mapToJsonArray {
                it.toIntOrNull()?.toJsonPrimitive() ?: localVars[it] ?: JsonNull.INSTANCE
            }
            val fn = functions[fnName] ?: extractFunction(fnName).also {
                functions[fnName] = it
            }
            return fn(argValues)
        }

        throw InterpretException("Unsupported JS expression $expr")
    }

    @Throws(InterpretException::class)
    private fun extractObject(objName: String): JsonElement {
        val fields = code.find(OBJECT_RE % objName.escape(), "fields")
                ?: throw InterpretException("Failed to extract object $objName")

        val obj = JsonObject()
        for (match in fields.findAll(FIELDS_RE)) {
            val argNames = match.groupValue("args")!!.split(",")
            obj[match.groupValue("key")!!.removeQuotes()] = buildFunction(argNames, match.groupValue("code")!!)
        }
        return obj
    }

    @Throws(InterpretException::class)
    fun extractFunction(fnName: String): JsFunction {
        val escapedFnName = fnName.escape()
        val match = code.find(FN_RE % escapedFnName % escapedFnName % escapedFnName)
                ?: throw InterpretException("Couldn't find JS function $fnName")
        return buildFunction(match.groupValue("args")!!.split(","), match.groupValue("code")!!)
    }

    @Throws(InterpretException::class)
    fun callFunction(fnName: String, vararg args: Any): JsonElement = callFunction(fnName, args.toJsonArray())

    @Throws(InterpretException::class)
    private fun callFunction(fnName: String, args: JsonArray): JsonElement = extractFunction(fnName)(args)

    private fun buildFunction(argNames: List<String>, code: String): JsFunction = JsFunction { args ->
        val localVars = argNames.zip(args).toJsonObject()
        for (stmt in code.split(";")) {
            val (res, abort) = interpretStatement(stmt, localVars)
            if (abort) return@JsFunction res
        }
        JsonNull.INSTANCE
    }

    class InterpretException(override val message: String) : Exception(message)

    companion object {
        private val OPERATORS = linkedMapOf<String, OperatorFunction>(
                "|" to { l, r -> l or r },
                "^" to { l, r -> l xor r },
                "&" to { l, r -> l and r },
                ">>" to { l, r -> l shr r },
                "<<" to { l, r -> l shl r },
                "-" to { l, r -> l - r },
                "+" to { l, r -> l + r },
                "%" to { l, r -> l % r },
                "/" to { l, r -> l / r },
                "*" to { l, r -> l * r }
        ).toImmutableMap()
        private val ASSIGN_OPERATORS = OPERATORS.mapKeys { (key, _) -> "$key=" }.toMutableMap().apply {
            this["="] = { _, r -> r }
        }.toImmutableMap()

        @Language("RegExp")
        private const val NAME_RE = """[a-zA-Z_$][a-zA-Z_$0-9]*"""
        private const val ASSIGN_OPERATOR_RE = """(?x)(?<out>$NAME_RE)(?:\[(?<index>[^\]]+?)\])?\s*%s(?<expr>.*)$"""
        private const val VAR_RE = """^var\s"""
        private const val RETURN_RE = """^return(?:\s+|$)"""
        private const val VARIABLE_RE = """(?!if|return|true|false)(?<name>$NAME_RE)$"""
        private const val VARIABLE_INDEX_RE = """(?<in>$NAME_RE)\[(?<idx>.+)\]$"""
        private const val MEMBER_RE = """(?<var>$NAME_RE)(?:\.(?<member>[^(]+)|\[(?<member2>[^]]+)\])\s*(?:\(+(?<args>[^()]*)\))?$"""
        private const val FUNC_NAME_RE = """(?:[a-zA-Z$0-9]+|"[a-zA-Z$0-9]+"|'[a-zA-Z$0-9]+')"""
        private const val OBJECT_RE = """(?x)(?<!this\.)%s\s*=\s*\{\s*(?<fields>($FUNC_NAME_RE\s*:\s*function\s*\(.*?\)\s*\{.*?\}(?:,\s*)?)*)\}\s*;"""
        private const val FIELDS_RE = """(?x)(?<key>$FUNC_NAME_RE)\s*:\s*function\s*\((?<args>[a-z,]+)\)\{(?<code>[^}]+)\}"""
        private const val FN_CALL_RE = """^(?<func>$NAME_RE)\((?<args>[a-zA-Z0-9_$,]*)\)$"""
        private const val FN_RE = """(?x)(?:function\s+%s|[{;,]\s*%s\s*=\s*function|var\s+%s\s*=\s*function)\s*\((?<args>[^)]*)\)\s*\{(?<code>[^}]+)\}"""
    }
}