package dbfaker.parser.model

interface Operator {
    fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression
}

class EqOp : Operator {
    override fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression {
        return Equality(left, right)
    }
}

class NeOp : Operator {
    override fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression {
        return NonEquality(left, right)
    }
}

class LtOp : Operator {
    override fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression {
        return LessThan(left, right)
    }
}

class LeOp : Operator {
    override fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression {
        return LessEqual(left, right)
    }
}

class GtOp : Operator {
    override fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression {
        return GreaterThan(left, right)
    }
}

class GeOp : Operator {
    override fun compose(left: ScalarExpression, right: ScalarExpression): ScalarExpression {
        return GreaterEqual(left, right)
    }
}



