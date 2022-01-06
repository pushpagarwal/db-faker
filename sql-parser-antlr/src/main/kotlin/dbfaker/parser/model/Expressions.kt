package dbfaker.parser.model


sealed interface BaseQueryExpression

sealed interface ScalarExpression : BaseQueryExpression

data class QueryExpression(
    val alias: String?,
    val condition: ScalarExpression?,
    val orderBy: OrderByItemList?,
    val selectExpression: SelectExpression,
) : BaseQueryExpression

data class OrderByItem(
    val property: PropertyExpression,
    val descending: Boolean = false
) : BaseQueryExpression

data class OrderByItemList(
    override val elements: List<OrderByItem>
) : BaseQueryExpression, ArrayExpression<OrderByItem> {
    constructor(list: TemporaryOrderByItemList) : this(list.toList())
}

sealed interface SelectExpression : BaseQueryExpression

data class SelectItem(
    val expression: ScalarExpression,
    val alias: String?
) : BaseQueryExpression

data class SelectItemList(
    override val elements: List<SelectItem>
) : SelectExpression, ArrayExpression<SelectItem> {
    constructor(list: TemporarySelectItemList) : this(list.toList())
}

data class SelectValueExpression(val expression: ScalarExpression) : SelectExpression

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

interface ArrayExpression<T : BaseQueryExpression> {
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

data class ArrayConst(override val elements: List<ConstExpression>) : ConstExpression,
    ArrayExpression<ConstExpression> {
    constructor(list: TemporaryConstExpressionList) : this(list.toList())
}

data class ObjectConst(override val properties: Map<Id, ConstExpression>) : ConstExpression,
    ObjectExpression<ConstExpression>

object NullConst : ConstExpression

object UndefinedConst : ConstExpression


data class ArrayCreation(override val elements: List<ScalarExpression>) : ScalarExpression,
    ArrayExpression<ScalarExpression> {
    constructor(list: TemporaryScalarExpressionList) : this(list.toList())
}

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

interface TemporaryList<T> {
    val items: MutableList<T>
}

inline fun <reified T> TemporaryList<T>.toList(): List<T> {
    return items.toList()
}

data class TemporaryScalarExpressionList(override val items: MutableList<ScalarExpression> = mutableListOf()) :
    TemporaryExpression, TemporaryList<ScalarExpression>

data class TemporaryConstExpressionList(override val items: MutableList<ConstExpression> = mutableListOf()) :
    TemporaryExpression, TemporaryList<ConstExpression>

data class TemporaryConstProperty(override val key: Id, override val value: ConstExpression) : TemporaryExpression,
    Map.Entry<Id, ConstExpression>

data class TemporaryConstPropertyList(override val items: MutableList<TemporaryConstProperty> = mutableListOf()) :
    TemporaryExpression, TemporaryList<TemporaryConstProperty>

data class TemporaryScalarProperty(override val key: Id, override val value: ScalarExpression) : TemporaryExpression,
    Map.Entry<Id, ScalarExpression>

data class TemporaryScalarPropertyList(override val items: MutableList<TemporaryScalarProperty> = mutableListOf()) :
    TemporaryExpression, TemporaryList<TemporaryScalarProperty>

data class TemporaryOrderByItemList(override val items: MutableList<OrderByItem> = mutableListOf()) :
    TemporaryExpression, TemporaryList<OrderByItem>

data class TemporarySelectItemList(override val items: MutableList<SelectItem> = mutableListOf()) :
    TemporaryExpression, TemporaryList<SelectItem>