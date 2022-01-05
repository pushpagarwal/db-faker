package dbfaker.adaptor.memdb.query.planer

import dbfaker.memdb.ArrayValue
import dbfaker.memdb.JsonValue
import dbfaker.memdb.jsonValueOf
import org.junit.Test

import org.junit.Assert.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport

class FunctionResolverTest {

    @Test
    fun stringFunctionCalls() {
        executeCore(true, "RegexMatch", "Test-307", "test-\\d+", "i")
        executeCore(true, "CONTAINS", "Test-307", "test", true)
        executeCore(false, "CONTAINS", "Test-307", "test")
        executeCore("abcdefghi", "CONCAT", "abc", "def", "ghi")
    }

    @Test
    fun arrayFunctionCalls() {
        executeCore(true, "ARRAY_CONTAINS", jsonValueOf(listOf(1, 2, 3, 4, 5, 6)), 4)
        executeCore(
            jsonValueOf(listOf(1, 2, 3, 4, 5, 6)).value,
            "ARRAY_CONCAT",
            jsonValueOf(listOf(1, 2)),
            jsonValueOf(listOf(3, 4)),
            jsonValueOf(listOf(5, 6))
        )
    }

    private fun executeCore(expectedValue: Any, name: String, vararg args: Any?) {
        val argList = StreamSupport.stream(args.asIterable().spliterator(), false)
            .map { jsonValueOf(it) }
            .collect(Collectors.toList())
        assertEquals(expectedValue, FunctionResolver.execute(name, argList).value)
    }
}