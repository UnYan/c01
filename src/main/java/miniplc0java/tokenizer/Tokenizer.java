package miniplc0java.tokenizer;

import miniplc0java.error.ErrorCode;
import miniplc0java.error.TokenizeError;
import miniplc0java.util.Pos;

public class Tokenizer {
    private static int identStart = 1;
    private static int identEnd = 10;
    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();

        if (peek=='\''){
            return lexCharLiteral();
        }
        else if(peek=='\"'){
            return lexStringLiteral();
        }
        else if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        }
        else if (Character.isLetter(peek)||peek=='_') {
            return lexIdentOrKeyword();
        }
        else {
            return lexOperatorOrUnknown();
        }
    }
    private Token lexCharLiteral() throws TokenizeError{
        Pos st = it.currentPos();
        Token token;
        it.nextChar();
        if (it.peekChar()=='\\'){
            it.nextChar();
            switch (it.nextChar()){
                case 'n':
                    token = new Token(TokenType.UINT_LITERAL,(int)'\n',st, it.currentPos());
                    break;
                case 't':
                    token = new Token(TokenType.UINT_LITERAL,(int)'\t',st, it.currentPos());
                    break;
                case 'r':
                    token =  new Token(TokenType.UINT_LITERAL,(int)'\r',st, it.currentPos());
                    break;
                case '\'':
                    token =  new Token(TokenType.UINT_LITERAL,(int)'\'',st, it.currentPos());
                    break;
                case '\\':
                    token =  new Token(TokenType.UINT_LITERAL,(int)'\\',st, it.currentPos());
                    break;
                case '\"':
                    token =  new Token(TokenType.UINT_LITERAL,(int)'\"',st, it.currentPos());
                    break;
                default:
                    throw new TokenizeError(ErrorCode.InvalidChar, it.currentPos());
            }
        }
        else {
            token =  new Token(TokenType.UINT_LITERAL,(int)it.nextChar(),st, it.currentPos());
        }
        if (it.nextChar()!='\''){
            throw new TokenizeError(ErrorCode.InvalidChar, it.currentPos());
        }
        return token;
    }

    private Token lexStringLiteral() throws TokenizeError {
        StringBuilder stringBuilder = new StringBuilder();
        Pos start = it.currentPos();
        it.nextChar();
        while(!it.isEOF()) {
            char peek = it.peekChar();
            if (peek == '\\') {
//                stringBuilder.append(it.nextChar());
                it.nextChar();
                peek = it.peekChar();
                switch (peek) {
                    case '\'':
                        stringBuilder.append('\'');
                        break;
                    case '\"':
                        stringBuilder.append('\"');
                        break;
                    case '\\':
                        stringBuilder.append('\\');
                        break;
                    case 'n':
                        stringBuilder.append('\n');
                        break;
                    case 't':
                        stringBuilder.append('\t');
                        break;
                    case 'r':
                        stringBuilder.append('\r');
                        break;
                    default:
                        throw new TokenizeError(ErrorCode.InvalidStringLiteral ,it.currentPos());
                }
                it.nextChar();
            } else if (peek == '\"') {
                it.nextChar();
                break;
            } else {
                stringBuilder.append(it.nextChar());
            }
        }
        String result = stringBuilder.toString();
        Pos end = it.currentPos();
        return new Token(TokenType.STRING_LITERAL, result, start, end);
    }

    private Token lexUIntOrDouble() throws TokenizeError {
        boolean isDouble = false;
        StringBuilder stringBuilder = new StringBuilder();
        Pos start = it.currentPos();
        while(!it.isEOF()) {
            char peek = it.peekChar();
            if (Character.isDigit(peek)) {
                stringBuilder.append(it.nextChar());
            } else if (peek == '.' && !isDouble) {
                stringBuilder.append(it.nextChar());
                if (Character.isDigit(it.peekChar())) {
                    isDouble = true;
                } else {
                    throw new TokenizeError(ErrorCode.InvalidDouble ,it.currentPos());
                }
            } else if ((peek == 'e' || peek == 'E') && isDouble) {
                stringBuilder.append(it.nextChar());
                peek = it.peekChar();
                if (peek == '+' || peek == '-') {
                    stringBuilder.append(it.nextChar());
                }
                if (Character.isDigit(it.peekChar())) {
                    isDouble = true;
                } else {
                    throw new TokenizeError(ErrorCode.InvalidDouble ,it.currentPos());
                }
            } else {
                break;
            }
        }
        String result = stringBuilder.toString();
        Pos end = it.currentPos();
        if (isDouble) {
            try {
                return new Token(TokenType.DOUBLE_LITERAL, Double.parseDouble(result), start, end);
            } catch (NumberFormatException e) {
                throw new TokenizeError(ErrorCode.DoubleOverflow ,it.currentPos());
            }
        } else {
            try {
                return new Token(TokenType.UINT_LITERAL, Integer.parseInt(result), start, end);
            } catch (NumberFormatException e) {
                return new Token(TokenType.UINT_LITERAL, Long.parseLong(result), start, end);
            }

        }}

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串

        Pos start = new Pos(it.currentPos().row,it.currentPos().col);
        StringBuilder s = new StringBuilder();
        char c;
        do {
            s.append(it.nextChar());
            c = it.peekChar();
        }while (Character.isLetterOrDigit(c)||c=='_');
//        if("int".equals(token.toString()))
//            return new Token(TokenType.INT, token, start, end);
//        if("void".equals(token.toString()))
//            return new Token(TokenType.VOID, token, start, end);
//        if("string".equals(token.toString()))
//            return new Token(TokenType.STRING, token, start, end);
//        if("double".equals(token.toString()))
//            return new Token(TokenType.DOUBLE, token, start, end);
        TokenType[] KW = TokenType.values();
        for(int i = identStart; i <= identEnd;i++){
            if(KW[i].toString().equalsIgnoreCase(s.toString())) {
                return new Token(KW[i], s.toString(), start, it.currentPos());
            }
        }
            return new Token(TokenType.IDENT, s.toString(),start, it.currentPos());

    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                if(it.peekChar() == '>'){
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                else
                    return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());
//                throw new Error("Not implemented");

            case '*':
                // 填入返回语句
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            case '/':
                if (it.peekChar()=='/'){
                    while (it.nextChar()!='\n'){
                    }
                    return nextToken();
                }
                // 填入返回语句
                return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());
//                throw new Error("Not implemented");

            // 填入更多状态和返回语句
            case '=':
                if(it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                else
                    return new Token(TokenType.ASSIGN, "=", it.previousPos(), it.currentPos());
            case '!':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                else
                    throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());

            case '<':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.LE, "<=",it.previousPos(), it.currentPos());
                }
                else
                    return new Token(TokenType.LT, '<',it.previousPos(), it.currentPos());

            case '>':
                if(it.peekChar() == '='){
                    it.nextChar();
                    return new Token(TokenType.GE, ">=",it.previousPos(), it.currentPos());
                }
                else
                    return new Token(TokenType.GT, '>',it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(',it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')',it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{',it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}',it.previousPos(), it.currentPos());

            case ',':
                return new Token(TokenType.COMMA, ',',it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON, ':',it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';',it.previousPos(), it.currentPos());

            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
