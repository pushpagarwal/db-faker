lexer grammar CosmosDBSqlLexer;

// keywords

AND:                          A N D;
ARRAY:                        A R R A Y;
AS:                           A S;
ASC:                          A S C;
BETWEEN:                      B E T W E E N;
BY:                           B Y;
CASE:                         C A S E;
CAST:                         C A S T;
CONVERT:                      C O N V E R T;
CROSS:                        C R O S S;
DESC:                         D E S C;
DISTINCT:                     D I S T I N C T;
ELSE:                         E L S E;
END:                          E N D;
ESCAPE:                       E S C A P E;
EXISTS:                       E X I S T S;
K_false:                      'false';        // case sensitive
FOR:                          F O R;
FROM:                         F R O M ;
GROUP:                        G R O U P;
HAVING:                       H A V I N G;
IN:                           I N;
INNER:                        I N N E R;
INSERT:                       I N S E R T;
INTO:                         I N T O;
IS:                           I S;
JOIN:                         J O I N;
LEFT:                         L E F T;
LIKE:                         L I K E;
LIMIT:                        L I M I T;
NOT:                          N O T;
K_null:                       'null';         // case sensitive
OFFSET:                       O F F S E T;
ON:                           O N;
OR:                           O R;
ORDER:                        O R D E R;
OUTER:                        O U T E R;
OVER:                         O V E R;
RIGHT:                        R I G H T;
SELECT:                       S E L E C T;
SET:                          S E T;
THEN:                         T H E N;
TOP:                          T O P;
K_true:                       'true';         // case sensitive
K_udf:                        'udf';          // case sensitive
K_undefined:                  'undefined';    // case sensitive
UPDATE:                       U P D A T E;
VALUE:                        V A L U E;
WHEN:                         W H E N;
WHERE:                        W H E R E;
WITH:                         W I T H;
Infinity:                     'Infinity';     // case sensitive
NaN:                          'NaN';          // case sensitive

// others

SPACE:                        [ \t\r\n]+                       -> skip;
COMMENTS:                     '-' '-' ~[\t\r\n]+ [\t\r\n]     -> skip;

// keywords type groups
PARAM_NAME:                   '@'[a-zA-Z_][a-zA-Z_0-9]*;
ID:                           [a-zA-Z_][a-zA-Z_0-9]*;
INTEGER:                      Digits;

// operators
COL:                          'C';
COMMA:                        ',';
DOT:                          '.';
ADD:                          '+';
SUB:                          '-';
MUL:                          '*';
DIV:                          '/';
MOD:                          '%';
COLON:                        ':';
EQUAL:                        '=';
GT :                          '>';
LT :                          '<';
LE :                          '<=';
GE :                          '>=';
NOTEQUAL :                    '!=';
BIT_NOT_OP:                   '~';
BIT_OR_OP:                    '|';
BIT_AND_OP:                   '&';
BIT_XOR_OP:                   '^';
QUEST:                        '?';
LEFT_BRACE:                   '{';
RIGHT_BRACE:                  '}';
LEFT_BRACKET:                 '[';
RIGHT_BRACKET:                ']';
LEFT_PARENTHESIS:             '(';
RIGHT_PARENTHESIS:            ')';
QUOTE:                        '"'|'\'';

NUMBER:
    Digits '.' Digits? ExponentPart?
        |	'.' Digits ExponentPart?
        |	Digits ExponentPart
        ;

fragment
ExponentPart
	:	ExponentIndicator SignedInteger
	;

fragment
ExponentIndicator
	:
	;
fragment
SignedInteger
	:	Sign? Digits
	;

fragment
Sign
	:	[+-]
	;

fragment
Digits
 : [1-9][0-9]*;

StringLiteral
	:	'"' StringCharacters? '"'
	;
fragment
StringCharacters
	:	StringCharacter+
	;
fragment
StringCharacter
	:	~["\\\r\n]
	|	'\\' [btnfr"'\\]
	;

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];