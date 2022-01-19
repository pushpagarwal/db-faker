package dbfaker.adaptor.memdb.query.planer

import dbfaker.adaptor.memdb.query.planer.dbfunctions.ArrayFunctions
import dbfaker.adaptor.memdb.query.planer.dbfunctions.StringFunctions
import dbfaker.memdb.BaseJsonValue
import dbfaker.memdb.jsonValueOf
import java.lang.reflect.Array
import java.util.stream.Collectors
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf

@Target(AnnotationTarget.FUNCTION)
annotation class DbFunction(val name: String)

interface DbFunctions

object FunctionResolver {
    private val functionHandlers = listOf(StringFunctions, ArrayFunctions).stream()
        .map { Pair(it, it::class) }
        .flatMap { (obj, cls) -> cls.members.stream().map { Pair(obj, it) } }
        .filter { (_, callable) -> callable.hasAnnotation<DbFunction>() }
        .map { (obj, callable) ->
            val name = callable.findAnnotation<DbFunction>()!!.name
            Triple(name, obj, callable)
        }
        .collect(Collectors.toUnmodifiableMap({ p -> p.first }, { p -> Pair(p.second, p.third) }))

    fun execute(name: String, arguments: List<BaseJsonValue>): BaseJsonValue {
        val (obj, callable) = functionHandlers[name]
            ?: throw IllegalArgumentException("Function $name is not supported.")
        val parameters = callable.parameters
        val args = mutableMapOf<KParameter, Any?>()
        for ((i, p) in parameters.withIndex()) {
            if (i == 0)
                args[p] = obj
            else if (i <= arguments.size) {
                if (p.isVararg) {
                    if (i != parameters.size - 1)
                        throw IllegalArgumentException("Var arg must be the last argument.")
                    val varArgType = p.type.arguments[0].type ?: Any::class.createType(nullable = true)
                    val varArgClass = (varArgType.classifier as KClass<*>).java
                    val arrSize = arguments.size - i + 1
                    args[p] = Array.newInstance(varArgClass, arrSize)
                    for (j in 0 until arrSize)
                        Array.set(args[p], j, resolveParameter(j + i - 1, arguments[j + i - 1], varArgType))
                } else
                    args[p] = (resolveParameter(i, arguments[i - 1], p.type))
            } else {
                if (!p.isOptional) {
                    if (p.type.isMarkedNullable) {
                        args[p] = null
                    } else
                        throw IllegalArgumentException(
                            "$name: Only ${arguments.size} parameters passed," +
                                    "Expected arguments: ${parameters.size - 1}"
                        )
                }
            }
        }
        val result = callable.callBy(args)
        return jsonValueOf(result)
    }

    private fun resolveParameter(index: Int, value: BaseJsonValue, type: KType): Any? {
        if (value::class.createType().isSubtypeOf(type))
            return value
        val v = value.value
        if (v == null) {
            if (type.isMarkedNullable)
                return null
            else
                throw IllegalArgumentException("Null value passed for non nullable argument #$index.")
        } else if (v::class.createType().isSubtypeOf(type))
            return v
        throw IllegalArgumentException("Passed value $v, Expected Type: $type")
    }

}
