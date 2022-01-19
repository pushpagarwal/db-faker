parser grammar CosmosDBSqlParser;

options {
    tokenVocab = CosmosDBSqlLexer;
}

root
    : sql_query
    ;

sql_query
    : select_clause from_clause? where_clause? orderby_clause?
    ;

select_clause
    : SELECT top_spec? selection
    ;

top_spec
    : TOP NUMBER
    ;

from_clause
    : FROM from_specification
    ;

where_clause
    : WHERE scalar_expression
    ;

orderby_clause
    : ORDER BY orderby_item_list
    ;

selection
    : select_list
    | select_value_spec
    | MUL // FIXME 'SELECT *<EOF>' is not supported actually
    ;

select_value_spec
    : VALUE scalar_expression
    ;

select_list
    : select_item
    | select_list COMMA select_item
    ;

select_item
    : scalar_expression
    | scalar_expression select_alias
    ;

select_alias
    : ID
    | AS ID
    ;

orderby_item_list
    : orderby_item
    | orderby_item_list COMMA orderby_item
    ;

orderby_item:
    scalar_expression
    | scalar_expression ASC
    | scalar_expression DESC
    ;

from_specification
    : primary_from_specification
    | from_specification JOIN primary_from_specification
    ;

primary_from_specification
    : input_collection
    | input_collection input_alias
    | ID IN input_collection
    ;

input_alias
    : ID
    | AS ID
    ;

input_collection
    : relative_path
    | LEFT_PARENTHESIS sql_query RIGHT_PARENTHESIS
    ;

relative_path
    : relative_path_segment
    | relative_path DOT relative_path_segment
    | relative_path LEFT_BRACKET NUMBER RIGHT_BRACKET
    | relative_path LEFT_BRACKET QUOTE relative_path_segment QUOTE RIGHT_BRACKET
    ;

relative_path_segment
    : ID
    ;

array_item_list
    : scalar_expression
    | array_item_list COMMA scalar_expression
    ;

array_create_expression
    : LEFT_BRACKET array_item_list? RIGHT_BRACKET
    ;

property_name
    : ID
    ;

object_property
    : property_name COLON scalar_expression
    ;

object_property_list:
    object_property
    | object_property_list COMMA object_property
    ;

object_create_expression
    : LEFT_BRACE object_property_list? RIGHT_BRACE
    ;

function_arg_list:
    scalar_expression
    | function_arg_list COMMA scalar_expression
    ;

udf_function_name
    : ID
    ;

function_call_expression
    : sys_function_name LEFT_PARENTHESIS function_arg_list? RIGHT_PARENTHESIS
    | K_udf DOT udf_function_name LEFT_PARENTHESIS function_arg_list? RIGHT_PARENTHESIS
    ;

scalar_expression
    : logical_scalar_expression
    | between_scalar_expression
    ;

logical_scalar_expression
    : binary_expression # logicalBinaryExpression
    | in_scalar_expression # logicalScalarExpression
    | logical_scalar_expression AND logical_scalar_expression # logicalANDExpression
    | logical_scalar_expression OR logical_scalar_expression # logicalOrExpression
    ;

between_scalar_expression
    : binary_expression BETWEEN binary_expression AND binary_expression
    | binary_expression NOT BETWEEN binary_expression AND binary_expression
    ;

in_scalar_expression
    : binary_expression IN LEFT_PARENTHESIS in_scalar_expression_item_list RIGHT_PARENTHESIS
    | binary_expression NOT IN LEFT_PARENTHESIS in_scalar_expression_item_list RIGHT_PARENTHESIS
    ;

exists_scalar_expression
    : EXISTS LEFT_PARENTHESIS sql_query RIGHT_PARENTHESIS
    ;

array_scalar_expression
    : ARRAY LEFT_PARENTHESIS sql_query RIGHT_PARENTHESIS
    ;

in_scalar_expression_item_list
    : scalar_expression
    | in_scalar_expression_item_list COMMA scalar_expression
    ;

binary_expression:
    unary_expression # UnaryExpression
    | binary_expression multiplication = (MUL| DIV| MOD) binary_expression # Multiplication
    | binary_expression addition  = (ADD |SUB | BIT_AND_OP | BIT_OR_OP | BIT_XOR_OP) binary_expression # Addition
    | binary_expression compare = (EQUAL| NOTEQUAL | LT | LE | GT | GE) binary_expression # Comparison
    ;

unary_expression:
    primary_expression
    | unary_operator primary_expression
    ;

unary_operator:
    SUB
    | ADD
    | BIT_NOT_OP
    | NOT
    ;

primary_expression
    : constant # constantExpression
    | function_call_expression # functionCallExpression
    | ID # inputAliasExpression
    | PARAM_NAME # parameterName
    | array_create_expression # arrayCreateExpression
    | object_create_expression # objectCreateExpression
    | LEFT_PARENTHESIS scalar_expression RIGHT_PARENTHESIS # parenthesisScalarExpression
    | LEFT_PARENTHESIS sql_query RIGHT_PARENTHESIS # parenthesisSqlExpression
    | primary_expression DOT property_name # propertyPath
    | primary_expression LEFT_BRACKET scalar_expression RIGHT_BRACKET # arrayIndexed
    | exists_scalar_expression # existsScalarExpression
    | array_scalar_expression # arrayScalarExpression
    ;

constant
    : K_undefined # constUndefined
    | K_null      # constNull
    | K_true      # constTrue
    | K_false     # constFalse
    | NUMBER      # constNumber
    | INTEGER     # constInteger
    | StringLiteral # constText
    | array_constant # constArray
    | object_constant # constObject
    ;

array_constant
    : LEFT_BRACKET array_constant_list? RIGHT_BRACKET
    ;

array_constant_list
    : constant
    | array_constant_list COMMA constant
    ;

object_constant
    : LEFT_BRACE object_constant_items? RIGHT_BRACE
    ;

object_constant_item
    : property_name COLON constant
    ;

object_constant_items
    : object_constant_item
    | object_constant_items COMMA object_constant_item
    ;

sys_function_name
    : ID
    ;