package com.zionhuang.music

import com.zionhuang.music.extensions.jsonArrayOf
import com.zionhuang.music.extensions.toJsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test
import com.google.gson.JsonNull.INSTANCE as NULL
import com.zionhuang.music.extractor.utils.JsInterpreter as JSI

class JsInterpreterTest {
    @Test
    fun testBasic() {
        with(JSI("function x(){;}")) {
            assertEquals(NULL, callFunction("x"))
        }
        with(JSI("function x3(){return 42;}")) {
            assertEquals(42.toJsonPrimitive(), callFunction("x3"))

        }
        with(JSI("var x5 = function(){return 42;}")) {
            assertEquals(42.toJsonPrimitive(), callFunction("x5"))
        }
    }

    @Test
    fun testCalc() {
        with(JSI("function x4(a){return 2*a+1;}")) {
            assertEquals(7.toJsonPrimitive(), callFunction("x4", 3))
        }
    }

    @Test
    fun testEmptyReturn() {
        with(JSI("function f(){return; y()}")) {
            assertEquals(NULL, callFunction("f"))
        }
    }

    @Test
    fun testMoreSpace() {
        with(JSI("function x (a) { return 2 * a + 1 ; }")) {
            assertEquals(7.toJsonPrimitive(), callFunction("x", 3))
        }
    }

    @Test
    fun testStrangeChars() {
        with(JSI("function \$_xY1 (\$_axY1) { var \$_axY2 = \$_axY1 + 1; return \$_axY2; }")) {
            assertEquals(21.toJsonPrimitive(), callFunction("\$_xY1", 20))
        }
    }

    @Test
    fun testOperators() {
        with(JSI("function f(){return 1 << 5;}")) {
            assertEquals(32.toJsonPrimitive(), callFunction("f"))
        }
        with(JSI("function f(){return 19 & 21;}")) {
            assertEquals(17.toJsonPrimitive(), callFunction("f"))
        }
        with(JSI("function f(){return 11 >> 2;}")) {
            assertEquals(2.toJsonPrimitive(), callFunction("f"))
        }
    }

    @Test
    fun testArrayAccess() {
        with(JSI("function f(){var x = [1,2,3]; x[0] = 4; x[0] = 5; x[2] = 7; return x;}")) {
            assertEquals(jsonArrayOf(5, 2, 7), callFunction("f"))
        }
    }

    @Test
    fun testParen() {
        with(JSI("function f(){return (1) + (2) * ((( (( (((((3)))))) )) ));}")) {
            assertEquals(7.toJsonPrimitive(), callFunction("f"))
        }
    }

    @Test
    fun testAssignments() {
        with(JSI("function f(){var x = 20; x = 30 + 1; return x;}")) {
            assertEquals(31.toJsonPrimitive(), callFunction("f"))
        }
        with(JSI("function f(){var x = 20; x += 30 + 1; return x;}")) {
            assertEquals(51.toJsonPrimitive(), callFunction("f"))
        }
        with(JSI("function f(){var x = 20; x -= 30 + 1; return x;}")) {
            assertEquals((-11).toJsonPrimitive(), callFunction("f"))
        }
    }

    @Test
    fun testPrecedence() {
        with(JSI("""
            function x() {
                var a = [10, 20, 30, 40, 50];
                var b = 6;
                a[0]=a[b%a.length];
                return a;
            }
        """)) {
            assertEquals(jsonArrayOf(20, 20, 30, 40, 50), callFunction("x"))
        }
    }

    @Test
    fun testCall() {
        with(JSI("""
            function x() { return 2; }
            function y(a) { return x() + a; }
            function z() { return y(3); }
        """)) {
            assertEquals(5.toJsonPrimitive(), callFunction("z"))
        }
    }

    @Test
    fun testArrayReverse() {
        with(JSI("""
            function x(a) {
                var b = a.split("");
                return b.reverse();
            }
        """)) {
            assertEquals(jsonArrayOf("6", "5", "4", "3", "2", "1"), callFunction("x", "123456"))
        }
    }

    @Test
    fun testArraySplice() {
        with(JSI("""
            function x() {
                var a = [1, 2, 3, 4, 5, 6];
                return a.splice(2, 3);
            }
        """)) {
            assertEquals(jsonArrayOf(3, 4, 5), callFunction("x"))
        }
    }

    @Test
    fun testStringJoin() {
        with(JSI("""
            function x() { 
                var a = "abcdefg";
                var b = a.split("");
                return b.join("");
            }
        """)) {
            assertEquals("abcdefg".toJsonPrimitive(), callFunction("x"))
        }
    }
}