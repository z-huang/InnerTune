package com.zionhuang.music;

import com.zionhuang.music.extractor.JSInterpreter;
import com.zionhuang.music.extractor.JSON;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSITest {
    private final static String TAG = "JSITest";
    JSInterpreter jsi;

    @Test
    public void testBasic() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function x(){;}");
        assertEquals(jsi.callFunction("x"), JSON.NULL);

        jsi = new JSInterpreter("function x3(){return 42;}");
        assertEquals(jsi.callFunction("x3"), JSON.fromNumber(42));

        jsi = new JSInterpreter("var x5 = function(){return 42;}");
        assertEquals(jsi.callFunction("x5"), JSON.fromNumber(42));
    }

    @Test
    public void testCalc() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function x4(a){return 2*a+1;}");
        assertEquals(jsi.callFunction("x4", 3), JSON.fromNumber(7));
    }

    @Test
    public void testEmptyReturn() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function f(){return; y()}");
        assertEquals(jsi.callFunction("f"), JSON.NULL);
    }

    @Test
    public void testMoreSpace() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function x (a) { return 2 * a + 1 ; }");
        assertEquals(jsi.callFunction("x", 3), JSON.fromNumber(7));
    }

    @Test
    public void testStrangeChars() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function $_xY1 ($_axY1) { var $_axY2 = $_axY1 + 1; return $_axY2; }");
        assertEquals(jsi.callFunction("$_xY1", 20), JSON.fromNumber(21));
    }

    @Test
    public void testOperators() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function f(){return 1 << 5;}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(32));

        jsi = new JSInterpreter("function f(){return 19 & 21;}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(17));

        jsi = new JSInterpreter("function f(){return 11 >> 2;}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(2));
    }

    @Test
    public void testArrayAccess() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function f(){var x = [1,2,3]; x[0] = 4; x[0] = 5; x[2] = 7; return x;}");
        assertEquals(jsi.callFunction("f"), JSON.toArray(5, 2, 7));
    }

    @Test
    public void testParen() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function f(){return (1) + (2) * ((( (( (((((3)))))) )) ));}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(7));
    }

    @Test
    public void testAssignments() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter("function f(){var x = 20; x = 30 + 1; return x;}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(31));

        jsi = new JSInterpreter("function f(){var x = 20; x += 30 + 1; return x;}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(51));

        jsi = new JSInterpreter("function f(){var x = 20; x -= 30 + 1; return x;}");
        assertEquals(jsi.callFunction("f"), JSON.fromNumber(-11));
    }

    @Test
    public void testPrecedence() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter(
                "function x() {\n" +
                        "    var a = [10, 20, 30, 40, 50];\n" +
                        "    var b = 6;\n" +
                        "    a[0]=a[b%a.length];\n" +
                        "    return a;\n" +
                        "}");
        assertEquals(jsi.callFunction("x"), JSON.toArray(20, 20, 30, 40, 50));
    }

    @Test
    public void testCall() throws JSInterpreter.InterpretException {
        jsi = new JSInterpreter(
                "function x() { return 2; }\n" +
                        "    function y(a) { return x() + a; }\n" +
                        "    function z() { return y(3); }");
        assertEquals(jsi.callFunction("z"), JSON.fromNumber(5));
    }
}
