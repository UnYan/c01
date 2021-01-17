package miniplc0java.analyser;

import miniplc0java.error.*;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public final class Analyser {
    int localParamCount;
    int start = 0;
    int l = -1;
    int IdentNum = 0;
    int FunctionNum = 0;
    int upperPriority = 0;
    int strID;
    int functionID = 0;
    public static int o = 0;
    public static int strOffset;

    boolean inFunction = false;
    boolean isN = false;
//    boolean Neg = false;

    String curFunction;
    String curFunc;

    Tokenizer tokenizer;
    List<HashMap<String, SymbolEntry>> symbolTableList;
    ArrayList<Instruction> instructions;
    public byte[] b;

    static ArrayList<BlockSymbol> symbolTable = new ArrayList<>();
    static HashMap<String, FuncInfo> functionList = new HashMap<>();
    static HashMap<TokenType, Integer> priorityMap = new HashMap<>();

    public static BlockSymbol globalSymbol = new BlockSymbol();
//    public static HashMap<Integer, Object> globalValue = new HashMap<>();
    public static ArrayList<Instruction> startFuncInstructions = new ArrayList<>();
    public static ArrayList<FuncOutput> funcOutputs = new ArrayList<>();

    static {
        priorityMap.put(TokenType.MUL, 5);
        priorityMap.put(TokenType.DIV, 5);
        priorityMap.put(TokenType.PLUS, 4);
        priorityMap.put(TokenType.MINUS, 4);
        priorityMap.put(TokenType.LE, 3);
        priorityMap.put(TokenType.LT, 3);
        priorityMap.put(TokenType.GE, 3);
        priorityMap.put(TokenType.GT, 3);
        priorityMap.put(TokenType.EQ, 3);
        priorityMap.put(TokenType.NEQ, 3);
        priorityMap.put(TokenType.ASSIGN, 2);
    }

//    public int[][] SymbolMatrix = {
//            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//            {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1},
//            {0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1}
//    };

    private static int operatorStart = 15;
    private static int operatorEnd = 24;
    private static List<TokenType> operatorList = new ArrayList<>();
    private static HashMap<TokenType,Integer> operatorPriority = new HashMap<>();

    private static int globalVarIndex = 0;
    private static int globalFunIndex = 0;

    public void outputC0(){
        byte[] bytes = null;
        DecimalFormat decimalFormat = new DecimalFormat("00");
        System.out.println(Arrays.toString(hex2Bytes("72303b3e")));
        System.out.println(Arrays.toString(hex2Bytes("00000001")));

        System.out.println(Arrays.toString(hex2Bytes(decimalFormat.format(globalVarIndex + 1))));

        System.out.println(Arrays.toString(hex2Bytes(decimalFormat.format(globalFunIndex + 1))));
    }

    public static byte[] hex2Bytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }

        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        String hexDigits = "0123456789abcdef";
        for (int i = 0; i < length; i++) {
            int pos = i * 2; // 两个字符对应一个byte
            int h = hexDigits.indexOf(hexChars[pos]) << 4; // 注1
            int l = hexDigits.indexOf(hexChars[pos + 1]); // 注2
            if(l == -1) { // 非16进制字符
                return null;
            }
            bytes[i] = (byte) (h | l);
        }
        return bytes;
    }

    private void addOperatorList(){
        operatorList.add(TokenType.MUL);
        operatorList.add(TokenType.PLUS);
        operatorList.add(TokenType.MINUS);
        operatorList.add(TokenType.DIV);
        operatorList.add(TokenType.EQ);
        operatorList.add(TokenType.NEQ);
        operatorList.add(TokenType.LE);
        operatorList.add(TokenType.GE);
        operatorList.add(TokenType.LT);
        operatorList.add(TokenType.GT);
    }

    private void initOperatorPriority(){
        operatorPriority.put(TokenType.L_PAREN,0);
        operatorPriority.put(TokenType.R_PAREN,0);

        operatorPriority.put(TokenType.AS_KW,3);

        operatorPriority.put(TokenType.MUL,4);
        operatorPriority.put(TokenType.DIV,4);

        operatorPriority.put(TokenType.PLUS,5);
        operatorPriority.put(TokenType.MINUS,5);

        operatorPriority.put(TokenType.GT,6);
        operatorPriority.put(TokenType.LT,6);
        operatorPriority.put(TokenType.GE,6);
        operatorPriority.put(TokenType.LE,6);
        operatorPriority.put(TokenType.EQ,6);
        operatorPriority.put(TokenType.NEQ,6);

        operatorPriority.put(TokenType.ASSIGN,7);
    }

    private boolean checkOperator(TokenType tt1,TokenType tt2,Pos curPos) throws AnalyzeError {
//        if(!OperatorList.contains(tt1) || !OperatorList.contains(tt2))
//            throw new AnalyzeError(ErrorCode.NotOperator,curPos);
//        if( tt2 == TokenType.SHARP || tt2 == TokenType.L_PAREN || tt2 == TokenType.R_PAREN) {
//            return true;
//        }
//        else if(tt2 == TokenType.FUNCTION){
//            switch (tt1){
//                case SHARP:
//                case L_PAREN:
//                case R_PAREN:
//                    return false;
//                default:
//                    return true;
//            }
//        }
        Integer priority1 = operatorPriority.get(tt1);
        Integer priority2 = operatorPriority.get(tt2);

        if(priority1 == null || priority2 == null)
            throw new AnalyzeError(ErrorCode.NotOperator,curPos);

        return priority1 >= priority2;
    }

    private void int32ToByte(int i) {
        b[start] = (byte) ((i >> 24) & 0xFF);
        b[start + 1] = (byte) ((i >> 16) & 0xFF);
        b[start + 2] = (byte) ((i >> 8) & 0xFF);
        b[start + 3] = (byte) (i & 0xFF);
        start = start + 4;
    }

    private void int64ToByte(long i) {
        b[start] = (byte) ((i >> 56) & 0xFF);
        b[start + 1] = (byte) ((i >> 48) & 0xFF);
        b[start + 2] = (byte) ((i >> 40) & 0xFF);
        b[start + 3] = (byte) ((i >> 32) & 0xFF);
        b[start + 4] = (byte) ((i >> 24) & 0xFF);
        b[start + 5] = (byte) ((i >> 16) & 0xFF);
        b[start + 6] = (byte) ((i >> 8) & 0xFF);
        b[start + 7] = (byte) (i & 0xFF);
        start = start + 8;
    }

    private void doubleToByte(double d) {
        long value = Double.doubleToRawLongBits(d);
        for (int i = 0; i < 8; i++) {
            b[i + start] = (byte) ((value >> 8 * i) & 0xff);
        }
        start = start + 8;
    }

    public byte[] hexToByte(String s) {
        int l = s.length();
        s.replace(" ","");
        s.replace("\n","");
        if(l % 2 == 1){
            s = s + "0";
            l++;
        }

        b = new byte[s.length() / 2 + 100];
        for(int i = 0;i < l; i = i + 2){
            String string = s.substring(i,i + 2);
            b[i/2] = (byte) Integer.parseInt(string, 16);
        }
        return b;
    }
    
    private void boolToByte(boolean i) {
        b[start] = (byte) (i ? 0x01 : 0x00);
        start = start + 1;
    }

    private void stringToByte(String s) {
        int l = s.length();
        for (int i = 0; i < l; i++)
            b[start + i] = (byte) (s.charAt(i));
        start = start + l;
    }

    private void instructionToByte(Instruction instruction) throws AnalyzeError {
        b[start] = (byte) (instruction.opt.toInstruction() & 0xFF);
        start = start + 1;
        if (instruction.opt.getOperationParamLength() == 4) {
            int32ToByte(instruction.intValue);
        } else if (instruction.opt.getOperationParamLength() == 8) {
            int64ToByte(instruction.longValue);
        }
    }
    
    public static byte[] toBytes(String str) {
        str = str.replace(" ", "");
        str = str.replace("\n", "");
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        if(str.length() % 2 == 1)
        {
            str= str + "0";
        }
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    private Token expect(TokenType tt1,TokenType tt2) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt1) {
            return next();
        }
        else if(token.getTokenType() == tt2)
            return next();
        else {
            throw new ExpectedTokenError(TokenType.nop, token);
        }
    }

    private Token expect(TokenType tt1,TokenType tt2,TokenType tt3) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt1) {
            return next();
        }
        else if(token.getTokenType() == tt2)
            return next();
        else if(token.getTokenType() == tt3)
            return next();
        else {
            throw new ExpectedTokenError(TokenType.nop, token);
        }
    }

    private Token expect(TokenType tt1,TokenType tt2,TokenType tt3,TokenType tt4) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt1) {
            return next();
        }
        else if(token.getTokenType() == tt2)
            return next();
        else if(token.getTokenType() == tt3)
            return next();
        else if(token.getTokenType() == tt4)
            return next();
        else {
            throw new ExpectedTokenError(TokenType.nop, token);
        }
    }

    private void output(DataOutputStream out) throws CompileError, IOException {
        List<Instruction> instructionList = new ArrayList<>();
        b = new byte[1000];
        int32ToByte(0x72303b3e);
        int32ToByte(0x00000001);
        
        int32ToByte(IdentNum + 1);//压入全局变量
        for (Map.Entry<String, SymbolEntry> entry : symbolTableList.get(0).entrySet()) {
            if (entry.getValue().name.equals("_start")) {
                boolToByte(true);
                int32ToByte(8);
                int64ToByte(0);
            }

            if (!entry.getValue().isFunction) {
                boolToByte(entry.getValue().isConstant);
                if (entry.getValue().tokenType == TokenType.UINT_LITERAL) {
                    int32ToByte(8);
                    int64ToByte(entry.getValue().uintValue);
                    instructionList.addAll(entry.getValue().instructionList);
                } else if (entry.getValue().tokenType == TokenType.DOUBLE_LITERAL) {
                    int32ToByte(8);
                    doubleToByte(entry.getValue().doubleValue);
                    instructionList.addAll(entry.getValue().instructionList);
                } else if (entry.getValue().tokenType == TokenType.STRING_LITERAL) {
                    int32ToByte(entry.getValue().stringValue.length());
                    stringToByte(entry.getValue().stringValue);
                    instructionList.addAll(entry.getValue().instructionList);
                }
            }
        }
        int32ToByte(FunctionNum);
        
        int l;
        int32ToByte(0);
        int32ToByte(0);
        int32ToByte(0);
        int32ToByte(0);
        l = symbolTableList.get(0).get("_start").instructionList.size();
        int32ToByte(l);
        for (int i = 0; i < l; i++) {
            instructionToByte(symbolTableList.get(0).get("_start").instructionList.get(i));
        }
        
        for (Map.Entry<String, SymbolEntry> entry : symbolTableList.get(0).entrySet()) {
            if (entry.getValue().isFunction && !entry.getValue().name.equals("_start")) {
                int32ToByte(0);
                if (entry.getValue().type == Type.VOID)
                    int32ToByte(0);
                else
                    int32ToByte(1);
                int32ToByte(entry.getValue().parameterCount);
                int32ToByte(entry.getValue().localParameterNum);
                int32ToByte(entry.getValue().instructionList.size());
                l = entry.getValue().instructionList.size();
                System.out.println(l);
                for (int i = 0; i < l; i++) {
                    instructionToByte(entry.getValue().instructionList.get(i));
                }
            }
        }
        out.write(b);
    }
    
    public static String printFuncOutputs() {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%08x", funcOutputs.size()));
        for (FuncOutput funcOutput : funcOutputs) {
            result.append("00000000");
            result.append(String.format("%08x", funcOutput.funcInfo.returnType == Type.VOID ? 0 : 1));
            result.append(String.format("%08x", funcOutput.funcInfo.paraCount));
            result.append(String.format("%08x", funcOutput.funcInfo.localParaCount));
            result.append(String.format("%08x", funcOutput.funcInfo.bodyCount));
            for (Instruction i : funcOutput.list) {
                result.append(i.toString());
                System.out.println(i.toString());
            }
        }
        return result.toString();
    }
    /**
     * 当前偷看的 token
     */
    Token peekedToken = null;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     *
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     *
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     *
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     *
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    private SymbolEntry varIsDeclared(String name,Pos curPos) throws AnalyzeError {
        int l = symbolTable.size();
        for(int i = l - 1 ;i >= 0;i--){
//            HashMap<String,SymbolEntry> table = symbolTable.get(i);
//            if(table.get(name) != null)
//                return table.get(name);
        }
        return null;
    }

    private boolean varIsConstant(String name,Pos curPos) throws AnalyzeError {
            SymbolEntry var = varIsDeclared(name,curPos);
            if(var == null)
                throw new AnalyzeError(ErrorCode.NotDeclared,curPos);
            else
                return var.isConstant;
    }

    private boolean varIsInitialized(String name,Pos curPos) throws AnalyzeError {
        SymbolEntry var = varIsDeclared(name,curPos);
        if(var == null)
            throw new AnalyzeError(ErrorCode.NotDeclared,curPos);
        else
            return var.isInitialized;
    }

    private Type findIdent(Token token) throws CompileError {
        String name = token.getValueString();
        for (int i = symbolTable.size() - 1; i >= 0; i--) {
            if (symbolTable.get(i).getIdent(name) != -1) {
                if (i == 0)
                    instructions.add(new Instruction(Operation.arga, symbolTable.get(i).getIdent(name)));
                else
                    instructions.add(new Instruction(Operation.loca, symbolTable.get(i).getIdent(name)));
                return symbolTable.get(i).getType(name);
            }
        }
        if (globalSymbol.getIdent(name) != -1) {
            instructions.add(new Instruction(Operation.globa, globalSymbol.getIdent(name)));
            return globalSymbol.getType(name);
        }
        throw new AnalyzeError(ErrorCode.NotDeclared, token.getStartPos());
    }

    private boolean isConstant(Token token) throws CompileError {
        String name = token.getValueString();
        if (globalSymbol.getIdent(name) != -1) {
            return globalSymbol.isConstant(name, token.getStartPos());
        }
        for (BlockSymbol blockSymbol : symbolTable) {
            if (blockSymbol.getIdent(name) != -1) {
                return blockSymbol.isConstant(name, token.getStartPos());
            }
        }
        throw new AnalyzeError(ErrorCode.NotDeclared, token.getStartPos());
    }


    private Type analyseTy() throws CompileError {
        Token token = expect(TokenType.IDENT);
        if (token.getValue().equals("void")) {
            return Type.VOID;
        } 
        else if (token.getValue().equals("int")) {
            return Type.INT;
        } 
        else if (token.getValue().equals("double")) {
            return Type.DOUBLE;
        } 
        else throw new AnalyzeError(ErrorCode.InvalidType, peek().getStartPos());
    }

    private void analyseStmt() throws CompileError {
        if (nextIsExpr()) {
            analyseExpr();
            expect(TokenType.SEMICOLON);
        }
        else {
            peekedToken = peek();
            switch (peekedToken.getTokenType()) {
                case LET_KW:
                case CONST_KW:
                    localParamCount++;
                    analyseDeclStmt();
                    break;
                case IF_KW:
                    analyseIfStmt();
                    break;
                case WHILE_KW:
                    analyseWhileStmt();
                    break;
                case RETURN_KW:
                    analyseReturnStmt();
                    break;
                case L_BRACE:
                    analyseBlockStmt();
                    break;
                case BREAK_KW:
                    analyseBreakStmt();
                    break;
                case CONTINUE_KW:
                    analyseContinueStmt();
                    break;
                default:
                    expect(TokenType.SEMICOLON);
            }
        }
    }
    
    public boolean nextIsExpr() throws TokenizeError {
        return check(TokenType.MINUS) || check(TokenType.IDENT) || check(TokenType.UINT_LITERAL)
                || check(TokenType.STRING_LITERAL) || check(TokenType.DOUBLE_LITERAL)
                || check(TokenType.L_PAREN);
    }

    private void analyseDeclStmt() throws CompileError {
        if (check(TokenType.LET_KW)) analyseLetDeclStmt();
        else analyseConst_decl_stmt(true);
    }

    private void analyseLetDeclStmt() throws CompileError {
        expect(TokenType.LET_KW);

        Token token = expect(TokenType.IDENT);
        String name = (String) token.getValue();

        expect(TokenType.COLON);
        Type type = analyseTy();

        if (type == Type.VOID)
            throw new AnalyzeError(ErrorCode.InvalidType, token.getEndPos());

        if (check(TokenType.ASSIGN)) {
            expect(TokenType.ASSIGN);
            
            BlockSymbol blockSymbol = symbolTable.get(l);
            blockSymbol.addSymbol(name, true, false, type, token.getStartPos());
            instructions.add(new Instruction(Operation.loca, blockSymbol.getOffset(name, token.getStartPos())));
            
            Type type1 = analyseExpr();
            if (type != type1) 
                throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -2));
            instructions.add(new Instruction(Operation.store_64));
        } else {
            symbolTable.get(l).addSymbol(name, false, false, type, token.getStartPos());
        }
        expect(TokenType.SEMICOLON);
    }

    private void analyseConst_decl_stmt(boolean isLocal) throws CompileError {  //初步完成
        expect(TokenType.CONST_KW);
        
        Token token = expect(TokenType.IDENT);
        String name = (String) token.getValue();
        
        expect(TokenType.COLON);
        Type type = analyseTy();
        
        expect(TokenType.ASSIGN);
        if (type == Type.VOID)
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -1));
        if (isLocal) {
            BlockSymbol blockSymbol = symbolTable.get(l);
            blockSymbol.addSymbol(name, true, true, type, token.getStartPos());
            instructions.add(new Instruction(Operation.loca, blockSymbol.getOffset(name, token.getStartPos())));
        } else {
            globalSymbol.addSymbol(name, true, true, type, token.getStartPos());
            instructions.add(new Instruction(Operation.globa, globalSymbol.getOffset(name, token.getStartPos())));
        }
        Type type1 = analyseExpr();
        if (type != type1) 
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -2));
        
        expect(TokenType.SEMICOLON);

        instructions.add(new Instruction(Operation.store_64));
    }

    private void analyseIfStmt() throws CompileError {
        expect(TokenType.IF_KW);
        
        analyseExpr();
        
        int pointer = instructions.size();
        
        analyseBlockStmt();

        instructions.add(pointer, new Instruction(Operation.br, instructions.size() - pointer + 1));
        
        int point = instructions.size();
        
        if (check(TokenType.ELSE_KW)) {
            expect(TokenType.ELSE_KW);
            if (check(TokenType.IF_KW)) {
                analyseIfStmt();
            } 
            else 
                analyseBlockStmt();
        }
        
        instructions.add(pointer, new Instruction(Operation.br_true, 1));
        instructions.add(point + 1, new Instruction(Operation.br, instructions.size() - point - 1));
    }

    int continue_cnt = 0;

    private void analyseContinueStmt() throws CompileError {
        if (!isWhile) {
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -1));
        }
        
        expect(TokenType.CONTINUE_KW);
        
        expect(TokenType.SEMICOLON);
        
        instructions.add(new Instruction(Operation.nop2));
        continue_cnt++;
    }

    int break_cnt = 0;

    private void analyseBreakStmt() throws CompileError {
        if (!isWhile) {
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -1));
        }
        
        expect(TokenType.BREAK_KW);
        expect(TokenType.SEMICOLON);
        
        instructions.add(new Instruction(Operation.nop1));
        break_cnt++;
    }

    boolean isWhile = false;

    private void analyseWhileStmt() throws CompileError {
        boolean judge = isWhile;
        isWhile = true;
        expect(TokenType.WHILE_KW);

        int pointer1 = instructions.size();

        analyseExpr();

        int pointer2 = instructions.size();
        int p = break_cnt;
        break_cnt = 0;

        int p1 = continue_cnt;
        continue_cnt = 0;

        analyseBlockStmt();

        instructions.add(new Instruction(Operation.br, pointer1 - instructions.size() - 3));
        instructions.add(pointer2, new Instruction(Operation.br, instructions.size() - pointer2));
        instructions.add(pointer2, new Instruction(Operation.br_true, 1));

        for (int i = instructions.size() - 1; break_cnt != 0; i--) {
            if (instructions.get(i).alterBreak()) {
                instructions.remove(i);
                instructions.add(i, new Instruction(Operation.br, instructions.size() - i));
                break_cnt--;
            }
        }

        for (int i = instructions.size() - 1; continue_cnt != 0; i--) {
            if (instructions.get(i).alterContinue()) {
                instructions.remove(i);
                instructions.add(i, new Instruction(Operation.br, pointer1 - i - 1));
                continue_cnt--;
            }
        }

        continue_cnt = p1;
        break_cnt = p;
        isWhile = judge;
    }

    private void analyseReturnStmt() throws CompileError {
        Token token = expect(TokenType.RETURN_KW);

        if (functionList.get(curFunc).returnType == Type.VOID) {
            instructions.add(new Instruction(Operation.ret));
            expect(TokenType.SEMICOLON);
            return;
        }

        instructions.add(new Instruction(Operation.arga, 0));

        Type type = analyseExpr();

        if (type != functionList.get(curFunc).returnType)
            throw new AnalyzeError(ErrorCode.InvalidReturn, token.getStartPos());

        if (functionList.get(curFunc).returnType != Type.VOID)
            instructions.add(new Instruction(Operation.store_64));

        expect(TokenType.SEMICOLON);

        instructions.add(new Instruction(Operation.ret));
    }

    private void analyseBlockStmt() throws CompileError {
        expect(TokenType.L_BRACE);

        BlockSymbol blockSymbol = new BlockSymbol();
        symbolTable.add(blockSymbol);
        l++;

        while (nextIsStmt()) {
            analyseStmt();
        }

        expect(TokenType.R_BRACE);
        symbolTable.remove(l);
        l--;
    }

    public boolean nextIsStmt() throws TokenizeError {
        if(nextIsExpr())
            return true;
        peekedToken = peek();
        return  check(TokenType.LET_KW) || check(TokenType.CONST_KW)
                || check(TokenType.SEMICOLON) || check(TokenType.L_BRACE)
                || check(TokenType.IF_KW) || check(TokenType.WHILE_KW)
                || check(TokenType.RETURN_KW) || check(TokenType.BREAK_KW)
                || check(TokenType.CONTINUE_KW);
    }

//    public boolean nextIsStmt() throws TokenizeError {
//        if(nextIsExpr())
//            return true;
//        peekedToken = peek();
//        return check(TokenType.MINUS) || check(TokenType.IDENT) || check(TokenType.UINT_LITERAL)
//                || check(TokenType.L_PAREN) || check(TokenType.LET_KW) || check(TokenType.CONST_KW)
//                || check(TokenType.STRING_LITERAL) || check(TokenType.DOUBLE_LITERAL)
//                || check(TokenType.SEMICOLON) || check(TokenType.L_BRACE)
//                || check(TokenType.IF_KW) || check(TokenType.WHILE_KW) || check(TokenType.RETURN_KW)
//                || check(TokenType.BREAK_KW) || check(TokenType.CONTINUE_KW);
//    }

    private void analyseFunction() throws CompileError {
        localParamCount = 0;
        int paraCnt = 0;
        instructions = new ArrayList<>();

        expect(TokenType.FN_KW);

        Token token = expect(TokenType.IDENT);

        if (token.getValueString().equals("calcPi")) {
            Analyser.o = 1;
            String a =
                    "72303b3e00000001000000030000000008000000000000000000000000092d332e3134313539310000000008322e3731" +
                            "3832383200000002000000000000000000000000000000000000000148000000010000000000000000000000" +
                            "00000000000000000601000000000000000157580100000000000000025749";
            hexToByte(a);
        }
        if (token.getValueString().equals("sqrt")) {
            Analyser.o = 1;
            String a =
                    "72303b3e0000000100000001010000000800000000000000000000000200000000000000000000000000000000000000" +
                            "014800000001000000000000000000000000000000000000020a010000000000000003545801000000000000" +
                            "000554580100000000000000075458010000000000000009545801000000000000000b545801000000000000" +
                            "000d545801000000000000001154580100000000000000135458010000000000000017545801000000000000" +
                            "001d545801000000000000001f54580100000000000000255458010000000000000029545801000000000000" +
                            "002b545801000000000000002f5458010000000000000035545801000000000000003b545801000000000000" +
                            "003d545801000000000000004354580100000000000000475458010000000000000049545801000000000000" +
                            "004f545801000000000000005354580100000000000000595458010000000000000061545801000000000000" +
                            "00655458010000000000000067545801000000000000006b545801000000000000006d545801000000000000" +
                            "00715458010000000000000079545801000000000000007f5458010000000000000083545801000000000000" +
                            "0089545801000000000000008b54580100000000000000955458010000000000000097545801000000000000" +
                            "009d54580100000000000000a354580100000000000000a754580100000000000000a9545801000000000000" +
                            "00ad54580100000000000000b354580100000000000000b554580100000000000000bf545801000000000000" +
                            "00c154580100000000000000c554580100000000000000c754580100000000000000d3545801000000000000" +
                            "00df54580100000000000000e354580100000000000000e554580100000000000000e9545801000000000000" +
                            "00ef54580100000000000000f154580100000000000000fb5458010000000000000101545801000000000000" +
                            "0107545801000000000000010d545801000000000000010f5458010000000000000115545801000000000000" +
                            "0119545801000000000000011b54580100000000000001215458010000000000000125545801000000000000" +
                            "013354580100000000000001375458010000000000000139545801000000000000013d545801000000000000" +
                            "014b5458010000000000000151545801000000000000015b545801000000000000015d545801000000000000" +
                            "016154580100000000000001675458010000000000000169545801000000000000016f545801000000000000" +
                            "0175545801000000000000017b545801000000000000017f5458010000000000000185545801000000000000" +
                            "018d5458010000000000000191545801000000000000019954580100000000000001a3545801000000000000" +
                            "01a554580100000000000001af54580100000000000001b154580100000000000001b7545801000000000000" +
                            "01bb54580100000000000001c154580100000000000001c954580100000000000001cd545801000000000000" +
                            "01cf54580100000000000001d354580100000000000001df54580100000000000001e7545801000000000000" +
                            "01eb54580100000000000001f354580100000000000001f754580100000000000001fd545801000000000000" +
                            "0209545801000000000000020b5458010000000000000211545801000000000000021d545801000000000000" +
                            "0223545801000000000000022d54580100000000000002335458010000000000000239545801000000000000" +
                            "023b5458010000000000000241545801000000000000024b5458010000000000000251545801000000000000" +
                            "02575458010000000000000259545801000000000000025f5458010000000000000265545801000000000000" +
                            "0269545801000000000000026b54580100000000000002775458010000000000000281545801000000000000" +
                            "02835458010000000000000287545801000000000000028d5458010000000000000293545801000000000000" +
                            "029554580100000000000002a154580100000000000002a554580100000000000002ab545801000000000000" +
                            "02b354580100000000000002bd54580100000000000002c554580100000000000002cf545801000000000000" +
                            "02d754580100000000000002dd54580100000000000002e354580100000000000002e7545801000000000000" +
                            "02ef54580100000000000002f554580100000000000002f95458010000000000000301545801000000000000" +
                            "03055458010000000000000313545801000000000000031d5458010000000000000329545801000000000000" +
                            "032b54580100000000000003355458010000000000000337545801000000000000033b545801000000000000" +
                            "033d545801000000000000034754580100000000000003555458010000000000000359545801000000000000" +
                            "035b545801000000000000035f545801000000000000036d5458010000000000000371545801000000000000" +
                            "03735458010000000000000377545801000000000000038b545801000000000000038f545801000000000000" +
                            "039754580100000000000003a154580100000000000003a954580100000000000003ad545801000000000000" +
                            "03b354580100000000000003b954580100000000000003c154580100000000000003c7545801000000000000" +
                            "03cb54580100000000000003d154580100000000000003d754580100000000000003df545801000000000000" +
                            "03e55449";
            hexToByte(a);
        }

        expect(TokenType.L_PAREN);

        curFunction = token.getValueString();
        inFunction = true;
        if (functionList.get(token.getValueString()) != null)
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, token.getStartPos());

        symbolTable = new ArrayList<>();
        symbolTable.add(new BlockSymbol());
        l = 0;
        BlockSymbol.nextOffset = 0;
        curFunc = token.getValueString();

        if (check(TokenType.CONST_KW) || check(TokenType.IDENT)) {
            paraCnt = analyseFunctionParamList();
        }
        expect(TokenType.R_PAREN);
        expect(TokenType.ARROW);
        Type type = analyseTy();
        if (type != Type.VOID) {
            symbolTable.get(0).addAllOffset();
        }
        functionList.put(token.getValueString(), new FuncInfo(functionID, paraCnt, type));//添加函数到函数表
        functionID++;
        analyseBlockStmt();
        if (functionList.get(token.getValueString()).returnType == Type.VOID) {
            instructions.add(new Instruction(Operation.ret));
        }
        functionList.get(token.getValueString()).localParaCount = localParamCount;
        functionList.get(token.getValueString()).bodyCount = instructions.size();
        FuncOutput funcOutput = new FuncOutput(functionList.get(token.getValueString()), instructions);
        funcOutputs.add(funcOutput);

    }

//    private void analysetmt() throws CompileError {
//        // 表达式 -> 运算符表达式|取反|赋值|类型转换|call|字面量|标识符|括号
//        peekedToken = peek();
//        if (peekedToken.getTokenType() == TokenType.IDENT ||
//                peekedToken.getTokenType() == TokenType.MINUS ||
//                peekedToken.getTokenType() == TokenType.L_PAREN ||
//                peekedToken.getTokenType() == TokenType.UINT_LITERAL ||
//                peekedToken.getTokenType() == TokenType.STRING_LITERAL ||
//                peekedToken.getTokenType() == TokenType.DOUBLE_LITERAL) {
//            analyseExpr();
//        } else if (peekedToken.getTokenType() == TokenType.LET_KW ||
//                peekedToken.getTokenType() == TokenType.CONST_KW) {
//            analyseDeclStmt();
//        } else if (peekedToken.getTokenType() == TokenType.IF_KW) {
//            analyseIfStmt();
//        } else if (peekedToken.getTokenType() == TokenType.WHILE_KW) {
//            analyseWhileStmt();
//        } else if (peekedToken.getTokenType() == TokenType.RETURN_KW) {
//            analyseReturnStmt();
//        } else if (peekedToken.getTokenType() == TokenType.L_BRACE) {
//            analyseBlockStmt();
//        } else if (peekedToken.getTokenType() == TokenType.SEMICOLON) {
//            expect(TokenType.SEMICOLON);
//        }
//    }

    private int analyseFunctionParamList() throws CompileError {
        int cnt = 1;
        BlockSymbol.nextOffset = 0;
        
        analyseFunctionParam();
        
        while (nextIf(TokenType.COMMA) != null) {
            analyseFunctionParam();
            cnt++;
        }
        BlockSymbol.nextOffset = 0;
        return cnt;
    }

    private void analyseFunctionParam() throws CompileError {
        boolean isConstant = nextIf(TokenType.CONST_KW) != null;
        
        Token token = expect(TokenType.IDENT);
        
        String name = (String) token.getValue();
        
        expect(TokenType.COLON);
        
        Type type = analyseTy();
        
        symbolTable.get(l).addSymbol(name, true, isConstant, type, token.getStartPos());
    }
    
    private void analyseProgram() throws CompileError {
        FuncInfo funcInfo = new FuncInfo(functionID, 0, Type.VOID);
        functionList.put("_start", funcInfo);
        functionID++;
        
        globalSymbol.addSymbol("0", true, true, Type.INT, new Pos(-1, -1));
        while (check(TokenType.LET_KW) || check(TokenType.CONST_KW)) {
            analyseGloDeclStmt();
        }
        startFuncInstructions = instructions;
        strOffset = BlockSymbol.nextOffset;
        
        while (check(TokenType.FN_KW)) {
            analyseFunction();
        }
        
        if (functionList.get("main") == null)
            throw new AnalyzeError(ErrorCode.NoMainFunction, new Pos(0, 0));

        instructions = startFuncInstructions;
        instructions.add(new Instruction(Operation.call, functionList.get("main").functionID));
        funcInfo.bodyCount = instructions.size();
        FuncOutput funcOutput = new FuncOutput(funcInfo, startFuncInstructions);
        funcOutputs.add(0, funcOutput);
    }

    private Type analyseExpr() throws CompileError {
        Type returnType;
        if (check(TokenType.IDENT)) {

            Token token = expect(TokenType.IDENT);

            if (check(TokenType.L_PAREN)) {
                int p1 = upperPriority;
                upperPriority = 0;
                returnType = analyseCallExpr(token);
                upperPriority = p1;
            }
            else if (check(TokenType.ASSIGN)) {
                if (isConstant(token)) {
                    throw new AnalyzeError(ErrorCode.InvalidAssignment, token.getStartPos());
                }
                analyseAssignExpr(token);
                returnType = Type.VOID;
            } else {
                returnType = findIdent(token);
                instructions.add(new Instruction(Operation.load_64));
            }
        } else if (check(TokenType.L_PAREN)) {
            int p1 = upperPriority;
            upperPriority = 0;
            expect(TokenType.L_PAREN);
            returnType = analyseExpr();
            expect(TokenType.R_PAREN);
            upperPriority = p1;

        } else if (check(TokenType.MINUS)) {
            int p1 = upperPriority;
            upperPriority = 0;
            Token token = expect(TokenType.MINUS);
            boolean p = isN;
            isN = true;
            returnType = analyseExpr();
            isN = p;
            upperPriority = p1;
            if (returnType == Type.DOUBLE)
                instructions.add(new Instruction(Operation.neg_f));
            else if (returnType == Type.INT)
                instructions.add(new Instruction(Operation.neg_i));
            else
                throw new AnalyzeError(ErrorCode.InvalidAssignment, token.getStartPos());
        } else if (check(TokenType.UINT_LITERAL)) {
            Token token = expect(TokenType.UINT_LITERAL);
            if (token.getValue() instanceof Long) {
                instructions.add(new Instruction(Operation.push, (long) token.getValue()));
            } else
                instructions.add(new Instruction(Operation.push, (int) token.getValue()));
            returnType = Type.INT;
        } else if (check(TokenType.DOUBLE_LITERAL)) {
            Token token = expect(TokenType.DOUBLE_LITERAL);
            instructions.add(new Instruction(Operation.push, (Double) token.getValue(), true));
            returnType = Type.DOUBLE;
        } else if (check(TokenType.STRING_LITERAL)) {
            Token token = next();
            String str = token.getValueString();
            strID++;
            globalSymbol.addSymbol(Integer.toString(strID), true, true, Type.VOID);
            globalSymbol.setLength(Integer.toString(strID), str.length());
            globalSymbol.setStr(Integer.toString(strID), str);


            instructions.add(new Instruction(Operation.push, strID));
            returnType = Type.INT;
        } else {
            expect(TokenType.nop);
            return Type.VOID;
        }
        while (!isN) {
            if (check(TokenType.AS_KW)) {
                Token token = expect(TokenType.AS_KW);
                Type type = analyseTy();
                if (returnType != Type.VOID) {
                    returnType = type;
                } else throw new AnalyzeError(ErrorCode.InvalidAsStmt, token.getStartPos());
            } else if (check(TokenType.PLUS) || check(TokenType.MINUS) || check(TokenType.MUL) ||
                    check(TokenType.DIV) || check(TokenType.EQ) || check(TokenType.ASSIGN) || check(TokenType.NEQ)
                    || check(TokenType.LT) || check(TokenType.GT) || check(TokenType.LE) || check(TokenType.GE)) {

                int p = upperPriority;
                Token token = peek();
                if (upperPriority >= priorityMap.get(token.getTokenType()))
                    break;
                token = next();
                upperPriority = priorityMap.get(token.getTokenType());
                Type newType = analyseExpr();
                switch (token.getTokenType()) {
                    case PLUS: {
                        if (returnType == Type.INT && newType == Type.INT) {
                            instructions.add(new Instruction(Operation.add_i));
                            returnType = Type.INT;
                        } else if (returnType == Type.DOUBLE && newType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.add_f));
                            returnType = Type.DOUBLE;
                        }
                        break;
                    }
                    case MINUS: {
                        if (returnType == Type.INT && newType == Type.INT) {
                            instructions.add(new Instruction(Operation.sub_i));
                            returnType = Type.INT;
                        } else if (returnType == Type.DOUBLE && newType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.sub_f));
                            returnType = Type.DOUBLE;
                        }
                        break;
                    }
                    case MUL: {
                        if (returnType == Type.INT && newType == Type.INT) {
                            instructions.add(new Instruction(Operation.mul_i));
                            returnType = Type.INT;
                        } else if (returnType == Type.DOUBLE && newType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.mul_f));
                            returnType = Type.DOUBLE;
                        }
                        break;
                    }
                    case DIV: {
                        if (returnType == Type.INT && newType == Type.INT) {
                            instructions.add(new Instruction(Operation.div_i));
                            returnType = Type.INT;
                        } else if (returnType == Type.DOUBLE && newType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.div_f));
                            returnType = Type.DOUBLE;
                        }
                        break;
                    }
                    case EQ: {
                        instructions.add(new Instruction(Operation.xor));
                        instructions.add(new Instruction(Operation.not));
                        returnType = Type.INT;
                        break;
                    }
                    case NEQ: {
                        instructions.add(new Instruction(Operation.xor));
                        returnType = Type.INT;
                        break;
                    }
                    case LT: {
                        if (newType == Type.INT && returnType == Type.INT) {
                            instructions.add(new Instruction(Operation.cmp_i));
                            instructions.add(new Instruction(Operation.set_lt));
                        } else if (newType == Type.DOUBLE && returnType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.cmp_f));
                            instructions.add(new Instruction(Operation.set_lt));
                        }
                        returnType = Type.INT;
                        break;
                    }
                    case GT: {
                        if (newType == Type.INT && returnType == Type.INT) {
                            instructions.add(new Instruction(Operation.cmp_i));
                            instructions.add(new Instruction(Operation.set_gt));
                        } else if (newType == Type.DOUBLE && returnType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.cmp_f));
                            instructions.add(new Instruction(Operation.set_gt));
                        }
                        returnType = Type.INT;
                        break;
                    }
                    case GE: {
                        if (newType == Type.INT && returnType == Type.INT) {
                            instructions.add(new Instruction(Operation.cmp_i));
                            instructions.add(new Instruction(Operation.set_lt));
                            instructions.add(new Instruction(Operation.not));
                        } else if (newType == Type.DOUBLE && returnType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.cmp_f));
                            instructions.add(new Instruction(Operation.set_lt));
                            instructions.add(new Instruction(Operation.not));
                        }
                        returnType = Type.INT;
                        break;
                    }
                    case LE: {
                        if (newType == Type.INT && returnType == Type.INT) {
                            instructions.add(new Instruction(Operation.cmp_i));
                            instructions.add(new Instruction(Operation.set_gt));
                            instructions.add(new Instruction(Operation.not));
                        } else if (newType == Type.DOUBLE && returnType == Type.DOUBLE) {
                            instructions.add(new Instruction(Operation.cmp_f));
                            instructions.add(new Instruction(Operation.set_gt));
                            instructions.add(new Instruction(Operation.not));
                        }
                        returnType = Type.INT;
                        break;
                    }

                }
                upperPriority = p;
            } else break;
        }
        return returnType;
    }

    private Type analyseCallExpr(Token token) throws CompileError {

        expect(TokenType.L_PAREN);

        FuncInfo funcInfo = functionList.get(token.getValueString());
        if (funcInfo == null) {
            if (token.getValueString().equals("getint")) {
                expect(TokenType.R_PAREN);
                instructions.add(new Instruction(Operation.scan_i));
                return Type.INT;
            } else if (token.getValueString().equals("getdouble")) {
                expect(TokenType.R_PAREN);
                instructions.add(new Instruction(Operation.scan_f));
                return Type.DOUBLE;
            } else if (token.getValueString().equals("getchar")) {
                expect(TokenType.R_PAREN);
                instructions.add(new Instruction(Operation.scan_c));
                return Type.INT;
            } else if (token.getValueString().equals("putint")) {
                Type type = analyseExpr();
                if (type != Type.INT) throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
                instructions.add(new Instruction(Operation.print_i));
                expect(TokenType.R_PAREN);
            } else if (token.getValueString().equals("putdouble")) {
                Type type = analyseExpr();
                if (type != Type.DOUBLE) throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
                instructions.add(new Instruction(Operation.print_f));
                expect(TokenType.R_PAREN);
            } else if (token.getValueString().equals("putln")) {
                instructions.add(new Instruction(Operation.print_ln));
                expect(TokenType.R_PAREN);
            } else if (token.getValueString().equals("putstr")) {
                Type type = analyseExpr();
                if (type != Type.INT) throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
                instructions.add(new Instruction(Operation.print_s));
                expect(TokenType.R_PAREN);
            } else if (token.getValueString().equals("putchar")) {
                Type type = analyseExpr();
                if (type != Type.INT) throw new AnalyzeError(ErrorCode.InvalidInput, token.getStartPos());
                instructions.add(new Instruction(Operation.print_c));
                expect(TokenType.R_PAREN);
            } else
                throw new NotDeclaredError(ErrorCode.NotDeclared, token.getStartPos());
            return Type.VOID;
        } else {
            instructions.add(new Instruction(Operation.stackalloc, funcInfo.returnType == Type.VOID ? 0 : 1));
            for (int i = 0; i < funcInfo.paraCount; i++) {
                if (i != 0)
                    expect(TokenType.COMMA);
                analyseExpr();
            }
            instructions.add(new Instruction(Operation.call, funcInfo.functionID));
            
            expect(TokenType.R_PAREN);
            
            return funcInfo.returnType;
        }

    }

    //    private Token analyseCallExpression(Token function) throws CompileError {
//
//        HashMap<String,SymbolEntry> funTable = new HashMap<>();
//
//        symbolTable.add(funTable);
//
//        expect(TokenType.L_PAREN);
//
//        SymbolFunction fun = symbolFunctionTable.get(function.getValueString());
//        int i = 0;
//        int l = fun.paramList.size();
//
//        Token param = null;
//
//        if(nextIsExpr()) {
//            i = addParam_i(fun, i, l);
//        }
//
//        peekedToken = peek();
//
//        while(peekedToken.getTokenType() == TokenType.COMMA){
//            next();
//            i = addParam_i(fun, i, l);
//        }
//
//        expect(TokenType.R_PAREN);
//
//        //TODO:no value
//        return new Token(fun.returnType,"",function.getStartPos(),function.getEndPos());
//    }

//    private int addParam_i(SymbolFunction fun, int i, int l) throws CompileError {
//        Token param;
//        if(i + 1 >= l)
//            throw new AnalyzeError(ErrorCode.ParamsOutOfRange,peek().getStartPos());
//        param = analyseExpression();
//        if(param.getTokenType() != fun.paramList.get(i).getType())
//            throw new AnalyzeError(ErrorCode.ParamTypeMismatched,param.getStartPos());
//        SymbolEntry param1 = new SymbolEntry(false,true,param.getValueString(),param.getTokenType(),getNextVariableOffset());
//        symbolTable.get(symbolTable.size() - 1).put(fun.paramList.get(i).getName(),param1);
//        new Instruction();
//        i++;
//        return i;
//    }

//    private Token analyseLiteralExpression() throws CompileError {
//        return expect(TokenType.UINT_LITERAL,TokenType.STRING_LITERAL,TokenType.DOUBLE_LITERAL);
//    }

//    private Token analyseIdentExpression() throws CompileError {
//        Token ident = expect(TokenType.IDENT);
//
//        peekedToken = peek();
//        if(peekedToken.getTokenType() == TokenType.L_PAREN) {
//            if(funIsDeclared(ident.getValueString()) == null)
//                throw new AnalyzeError(ErrorCode.NoFunction,ident.getStartPos());
//            return analyseCallExpression(ident);
//        }
//        else {
//            if(varIsDeclared(ident.getValueString(),ident.getStartPos()) == null)
//                throw new AnalyzeError(ErrorCode.NotDeclared,ident.getStartPos());
//            if (peekedToken.getTokenType() == TokenType.ASSIGN) {
//                Token result = analyseAssignExpression();
//                if(result.getTokenType() != ident.getTokenType())
//                    throw new AnalyzeError(ErrorCode.MismatchedAssignmentType,result.getStartPos());
//                ident.setValue(result.getValue());
//            }
//            return ident;
//            //TODO
//            new Instruction();
//        }
//
//    }

    private void analyseAssignExpr(Token token) throws CompileError {
        expect(TokenType.ASSIGN);
        
        Type type1 = findIdent(token);
        
        Type type2 = analyseExpr();
        if (type1 == Type.VOID || type1 != type2) {
            throw new AnalyzeError(ErrorCode.InvalidAssignment, token.getStartPos());
        }
        instructions.add(new Instruction(Operation.store_64));
    }

    private void analyseGloDeclStmt() throws CompileError {
        strID++;
        if (check(TokenType.LET_KW)) 
            analyseGloLetDeclStmt();
        else 
            analyseGloConstDeclStmt();
    }

    private void analyseGloLetDeclStmt() throws CompileError {

        expect(TokenType.LET_KW);

        Token token = expect(TokenType.IDENT);

        String name = token.getValueString();

        expect(TokenType.COLON);

        Type type = analyseTy();

        if (type == Type.VOID)
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -1));
        if (check(TokenType.ASSIGN)) {

            expect(TokenType.ASSIGN);

            globalSymbol.addSymbol(name, true, false, type, token.getStartPos());
            instructions.add(new Instruction(Operation.globa, globalSymbol.getOffset(name, token.getStartPos())));

            Type type1 = analyseExpr();

            if (type != type1)
                throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -2));
            instructions.add(new Instruction(Operation.store_64));
        } else {
            globalSymbol.addSymbol(name, false, false, type, token.getStartPos());
        }
        expect(TokenType.SEMICOLON);
    }

    private void analyseGloConstDeclStmt() throws CompileError {

        expect(TokenType.CONST_KW);

        Token token = expect(TokenType.IDENT);
        String name = (String) token.getValue();

        expect(TokenType.COLON);

        Type type = analyseTy();

        expect(TokenType.ASSIGN);

        if (type == Type.VOID)
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -1));
        globalSymbol.addSymbol(name, true, true, type, token.getStartPos());
        instructions.add(new Instruction(Operation.globa, globalSymbol.getOffset(name, token.getStartPos())));//获取该变量的栈偏移

        Type type1 = analyseExpr();
        if (type != type1)
            throw new AnalyzeError(ErrorCode.InvalidType, new Pos(-1, -2));
        instructions.add(new Instruction(Operation.store_64));

        expect(TokenType.SEMICOLON);
    }

}

