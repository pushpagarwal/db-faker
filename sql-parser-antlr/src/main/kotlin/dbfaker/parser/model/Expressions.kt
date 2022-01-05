package dbfaker.parser.model


sealed interface BaseQueryExpression

sealed interface ScalarExpression : BaseQueryExpression

data class QueryExpression(val alias: String, val condition: ScalarExpression) : BaseQueryExpression

sealed interface PropertyExpression : ScalarExpression {
    val path: String
}

data class IdPropertyPath(val parent: PropertyExpression?, val name: PropertyExpression) : PropertyExpression {
    override val path: String get() = (parent?.path ?: "") + name.path

}

data class Property(val parent: ScalarExpression, val name: PropertyExpression) : ScalarExpression

data class IndexExpression(val parent: ScalarExpression, val index: ScalarExpression) : ScalarExpression

data class Id(val name: String) : PropertyExpression {
    override val path: String get() = ".${name}"
}

interface ArrayExpression<T : ScalarExpression> {
    val elements: List<T>
}

interface ObjectExpression<T : ScalarExpression> {
    val properties: Map<Id, T>
}

sealed interface ConstExpression : ScalarExpression

data class NumberConst(val value: Double) : ConstExpression

data class LongConst(val value: Long) : ConstExpression

data class BooleanConst(val value: Boolean) : ConstExpression

data class TextConst(val value: String) : ConstExpression

data class ArrayConst(override val elements: List<ConstExpression>) : ConstExpression, ArrayExpression<ConstExpression>

data class ObjectConst(override val properties: Map<Id, ConstExpression>) : ConstExpression,
    ObjectExpression<ConstExpression>

object NullConst : ConstExpression

object UndefinedConst : ConstExpression


data class ArrayCreation(override val elements: List<ScalarExpression>) : ScalarExpression,
    ArrayExpression<ScalarExpression>

data class ObjectCreation(override val properties: Map<Id, ScalarExpression>) : ScalarExpression,
    ObjectExpression<ScalarExpression>

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

data class InExpression(
    val left: ScalarExpression,
    val right: List<ScalarExpression>,
) : ScalarExpression

data class Addition(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class Subtraction(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class Multiplication(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class Divide(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class ModExpression(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class BitAnd(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class BitOr(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class BitXor(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class And(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression

data class Or(
    override val left: ScalarExpression,
    override val right: ScalarExpression,
) : BinaryExpression


sealed interface UnaryExpression : ScalarExpression {
    val opd: ScalarExpression
}

data class Negate(override val opd: ScalarExpression) : UnaryExpression

data class Not(override val opd: ScalarExpression) : UnaryExpression

data class BitComplement(override val opd: ScalarExpression) : UnaryExpression


data class FunctionCall(
    val name: Id,
    val argumentList: List<ScalarExpression>
) : ScalarExpression

interface TemporaryExpression : BaseQueryExpression

data class TemporaryScalarExpressionList(val list: MutableList<ScalarExpression> = mutableListOf()) :
    TemporaryExpression

data class TemporaryConstExpressionList(val list: MutableList<ConstExpression> = mutableListOf()) :
    TemporaryExpression

data class TemporaryConstProperty(override val key: Id, override val value: ConstExpression) : TemporaryExpression,
    Map.Entry<Id, ConstExpression>

data class TemporaryConstPropertyList(val list: MutableList<TemporaryConstProperty> = mutableListOf()) :
    TemporaryExpression

data class TemporaryScalarProperty(override val key: Id, override val value: ScalarExpression) : TemporaryExpression,
    Map.Entry<Id, ScalarExpression>

data class TemporaryScalarPropertyList(val list: MutableList<TemporaryScalarProperty> = mutableListOf()) :
    TemporaryExpression