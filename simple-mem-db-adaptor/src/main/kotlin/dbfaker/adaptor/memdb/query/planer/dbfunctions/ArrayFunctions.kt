package dbfaker.adaptor.memdb.query.planer.dbfunctions

import dbfaker.adaptor.memdb.query.planer.DbFunction
import dbfaker.adaptor.memdb.query.planer.DbFunctions
import dbfaker.memdb.ArrayValue
import dbfaker.memdb.BaseJsonValue
import dbfaker.memdb.ObjectValue
import dbfaker.memdb.UndefinedValue
import java.util.stream.Collectors
import java.util.stream.StreamSupport

object ArrayFunctions : DbFunctions {
    @DbFunction(name = "ARRAY_CONCAT")
    fun concat(vararg arrays: ArrayValue): ArrayValue {
        val list = StreamSupport.stream(arrays.asIterable().spliterator(), false)
            .flatMap { it.value.stream() }
            .collect(Collectors.toUnmodifiableList())
        return ArrayValue(list)
    }

    @DbFunction(name = "ARRAY_CONTAINS")
    fun contains(array: ArrayValue, o: BaseJsonValue, partialMatch: Boolean = false): Boolean {
        return array.value.stream()
            .anyMatch { match(it, o, partialMatch) }
    }

    private fun match(v1: BaseJsonValue, v2: BaseJsonValue, partialMatch: Boolean): Boolean {
        return if (v1 == v2) true
        else if (partialMatch && v1 is ObjectValue && v2 is ObjectValue) {
            v2.value.entries.stream().allMatch { o -> v1.value.getOrDefault(o.key, UndefinedValue) == o.value }
        } else
            false
    }

    @DbFunction(name = "ARRAY_LENGTH")
    fun length(array: ArrayValue): Int = array.value.size

}