package dbfaker.adaptor.memdb.query.planer.dbfunctions

import dbfaker.adaptor.memdb.query.planer.DbFunction
import dbfaker.adaptor.memdb.query.planer.DbFunctions
import java.util.regex.Pattern

object StringFunctions : DbFunctions {

    @DbFunction("RegexMatch")
    fun regexMatch(str: String, pattern: String, options: String?): Boolean {
        var flags = 0
        if (options != null) {
            for (c in options) {
                flags = when (c) {
                    'm' -> flags or Pattern.MULTILINE
                    'i' -> flags or Pattern.CASE_INSENSITIVE or Pattern.UNICODE_CASE
                    's' -> flags or Pattern.DOTALL
                    'x' -> flags or Pattern.COMMENTS
                    else -> flags
                }
            }
        }
        return Pattern.compile(pattern, flags).matcher(str).find()
    }

    @DbFunction(name = "CONCAT")
    fun concat(vararg strs: String): String {
        return strs.joinToString(separator = "") { s -> s }
    }

    @DbFunction(name = "CONTAINS")
    fun contains(s1: String, s2: String, ignoreCase: Boolean = false) = s1.contains(s2, ignoreCase)

    @DbFunction(name = "ENDSWITH")
    fun endsWith(s1: String, s2: String, ignoreCase: Boolean = false) = s1.endsWith(s2, ignoreCase)

    @DbFunction(name = "STRINGEQUALS")
    fun stringEquals(s1: String, s2: String, ignoreCase: Boolean = false) = s1.equals(s2, ignoreCase)

}