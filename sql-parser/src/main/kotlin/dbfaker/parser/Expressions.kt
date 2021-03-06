package dbfaker.parser

sealed interface ScalarExpression

data class QueryExpression(val alias: String, val condition: ScalarExpression)

sealed interface PropertyExpression : ScalarExpression {
    val path: String
}

data class IdPropertyPath(val parent: PropertyExpression?, val name: PropertyExpression) : PropertyExpression {
    override val path: String get() = (parent?.path ?: "") + name.path

}

data class Property(val parent: ScalarExpression, val name: PropertyExpression) : ScalarExpression

data class Id(val name: String) : PropertyExpression {
    override val path: String get() = ".${name}"
}

data class NumberConst(val value: Double) : ScalarExpression

data class BooleanConst(val value: Boolean) : ScalarExpression

data class TextConst(val value: String) : ScalarExpression

sealed interface BinaryExpression : ScalarExpression {
    val left: ScalarExpression
    val right: ScalarExpression
}

data class Equality(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class NonEquality(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class LessThan(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class LessEqual(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class GreaterThan(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class GreaterEqual(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression