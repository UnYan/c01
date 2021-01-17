package miniplc0java.tokenizer;

public enum TokenType {
    nop,
    FN_KW,
    LET_KW,
    CONST_KW,
    AS_KW,
    WHILE_KW,
    IF_KW,
    ELSE_KW,
    RETURN_KW,
    BREAK_KW,
    CONTINUE_KW,

    UINT_LITERAL,
    STRING_LITERAL,
    DOUBLE_LITERAL,
    CHAR_LITERAL,

    IDENT,

    PLUS     ,
    MINUS    ,
    MUL      ,
    DIV      ,
    ASSIGN   ,
    EQ       ,
    NEQ      ,
    LT       ,
    GT       ,
    LE       ,
    GE       ,
    L_PAREN  ,
    R_PAREN  ,
    L_BRACE  ,
    R_BRACE  ,
    ARROW    ,
    COMMA    ,
    COLON    ,
    SEMICOLON,

    COMMENT,

    EOF;

    @Override
    public String toString() {
        switch (this) {
            case nop:
                return "NullToken";
            case FN_KW:
                return "fn";
            case LET_KW:
                return "let";
            case CONST_KW:
                return "const";
            case AS_KW:
                return "as";
            case WHILE_KW:
                return "while";
            case IF_KW:
                return "if";
            case ELSE_KW:
                return "else";
            case RETURN_KW:
                return "return";
            case BREAK_KW:
                return "break";
            case CONTINUE_KW:
                return "continue";
            case PLUS:
                return "+";
            case MINUS:
                return "-";
            case IDENT:
                return "ident";
            case UINT_LITERAL:
                return "uint_literal";
            case STRING_LITERAL:
                return "string_literal";
//            case TYPE:
//                return "type";
            case MUL:
                return "*";
            case DIV:
                return "/";
            /** == */
            case EQ:
                return "==";
            /** != */
            case NEQ://20
                return "!=";
            /** < */
            case LT:
                return "<";
            /** > */
            case GT:
                return ">";
            /** <= */
            case LE:
                return "<=";
            /** >= */
            case GE://24
                return ">=";
            /** = */
            case ASSIGN:
                return "=";
            /** 左括号 */
            case L_PAREN:
                return "(";
            /** 右括号 */
            case R_PAREN:
                return ")";
            /** 左花括号 */
            case L_BRACE:
                return "{";
            /** 右花括号 */
            case R_BRACE:
                return "}";
            /** -> */
            case ARROW:
                return "->";
            /** , */
            case COMMA:
                return ",";
            /** : */
            case COLON:
                return ":";
//            /** int */
//            case INT:
//                return "int";
//            /** void */
//            case VOID:
//                return "void";
//            case STRING:
//                return "string";
//            case DOUBLE:
//                return "double";
            /** 分号 */
            case SEMICOLON:
                return ";";
            case DOUBLE_LITERAL:
                return "double";
            case EOF:
                return "EOF";
            default:
                return "InvalidToken";
        }
    }

//
//
//
//    /** 空 */
//    None,
//    /** 无符号整数 */
//    Uint,
//    /** 标识符 */
//    Ident,
//    /** Begin */
//    Begin,
//    /** End */
//    End,
//    /** Var */
//    Var,
//    /** Const */
//    Const,
//    /** Print */
//    Print,
//    /** 加号 */
//    Plus,
//    /** 减号 */
//    Minus,
//    /** 乘号 */
//    Mult,
//    /** 除号 */
//    Div,
//    /** 等号 */
//    Equal,
//    /** 分号 */
//    Semicolon,
//    /** 左括号 */
//    LParen,
//    /** 右括号 */
//    RParen,
//    /** 文件尾 */
//    EOF;

//    @Override
//    public String toString() {
//        switch (this) {
//            case None:
//                return "NullToken";
//            case Begin:
//                return "Begin";
//            case Const:
//                return "Const";
//            case Div:
//                return "DivisionSign";
//            case EOF:
//                return "EOF";
//            case End:
//                return "End";
//            case Equal:
//                return "EqualSign";
//            case Ident:
//                return "Identifier";
//            case LParen:
//                return "LeftBracket";
//            case Minus:
//                return "MinusSign";
//            case Mult:
//                return "MultiplicationSign";
//            case Plus:
//                return "PlusSign";
//            case Print:
//                return "Print";
//            case RParen:
//                return "RightBracket";
//            case Semicolon:
//                return "Semicolon";
//            case Uint:
//                return "UnsignedInteger";
//            case Var:
//                return "Var";
//            default:
//                return "InvalidToken";
//        }
//    }
}
