import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilationEngineCHIHO {
    static SymbolTable symbolTable;
    static VMWriter vmWriter;

    PrintWriter fileWriter;
    BufferedReader bufferedReader;


    String currentCommand;
    String fileName;
    String funcName;

    private int argCounter;
    private int nameCounter;
    private int funcCounter;
    private Boolean flag;
    private String whiteSpace;
    private int numWhiteSpace;
    private int falseCounter,trueCounter ,expCounter, endCounter;
    private int labelCounter;
    private Map<String, String> convertKind = Map.of("var", "LOCAL", "arg", "ARG",
            "static", "STATIC", "field", "THIS");
    private String className;

    // constructor of class
    public CompilationEngineCHIHO(PrintWriter fileWriter, FileReader fileReader) throws IOException {
        whiteSpace = "";
        this.fileWriter = fileWriter;
        this.bufferedReader = new BufferedReader(fileReader);
        currentCommand = bufferedReader.readLine();
        symbolTable = new SymbolTable();
        falseCounter = 0;
        trueCounter = 0;
    }

    // it transltes class.
    public void compileClass() throws IOException {
        writeNext();
        className = getValueAndNext();
        writeNext();
        while(isVarDec())
            compileClassVarDec();
        while(isSubDec())
            compileSubroutine();

        bufferedReader.close();
        fileWriter.close();
    }


    // compiles a static declaration or a field declaration.
    public void compileClassVarDec() throws IOException {
        String kind, type, name;
        kind = getValueAndNext(); //field
        type = getValueAndNext(); // int
        name = getValueAndNext(); // x
        symbolTable.define(name,type,kind);
        while(isAnotherIden()) { // isAnotherIden() skips ","
            //may need some good idea,
            String name0;
            name0 = getValueAndNext();
            symbolTable.define(name0,type,kind);
        }
        writeNext(); // ;
    }


    // complies a complete method, function, or constructor.
    public void compileSubroutine() throws IOException {
        symbolTable.startSubroutine();
        writeNext(); // constructor
        writeNext(); // ClassName or what to return
        writeNext(); // new or funcName
        writeNext(); // (
        compileParameterList();
        writeNext(); //)
//        fileWriter.println(whiteSpace + "<subroutineBody>");
        writeNext(); // {
        while (isVar())
            compileVarDec();
        compileStatements();
        writeNext(); // }
    }

    // complies a parameter list, not including the enclosing "()"
    public void compileParameterList() throws IOException {
        String type, name;
        while(isParameterList()) { //need to skip ","
            type = getValueAndNext(); // int
            name = getValueAndNext(); // AX
            symbolTable.define(name, type, "arg");
        }
    }

    // compiles a var declaration
    public void compileVarDec() throws IOException {
        String kind, type, name;
        kind = getValueAndNext();
        type = getValueAndNext();
        name = getValueAndNext();
        symbolTable.define(name,type,kind);
        while(isAnotherIden()) {
            //may need some good idea
            String name0;
            name0 = getValueAndNext();
            symbolTable.define(name0,type,kind);
        }
        writeNext();
    }

    // compiles a sequence of statements.
    public void compileStatements() throws IOException {
        while(isStatement()){
            if(isPointOrBracket("if"))
                compileIf();
            if(isPointOrBracket("do"))
                compileDo();
            if(isPointOrBracket("return"))
                compileReturn();
            if(isPointOrBracket("while"))
                compileWhile();
            if(isPointOrBracket("let"))
                compileLet();
        }
    }

    // complies a do statement
    public void compileDo() throws IOException {
        funcName = "";
        writeNext(); // do
        compileExpression();
//        String className = getValueAndNext();
//        if(isPointOrBracket(".")){
//            writeNext();
//            funcName = getValueAndNext();
//        }
//        vmWriter.writePop(symbolTable.kindOf(className), symbolTable.indexOf(className));
//        writeNext(); //(
//        compileExpressionList();
//        writeNext();//)
//        writeNext(); //;
//        vmWriter.writeCall(className + funcName, 0);
        vmWriter.writePop("TEMP", 0);

    }

    // complies a let statement.
    public void compileLet() throws IOException {
        writeNext();
        String str = getValueAndNext();
        if(isPointOrBracket("[")){
            writeNext();//[
            compileExpression();
            writeNext();//]
        }
        writeNext();
        compileExpression();
        writeNext();
        if (symbolTable.kindOf(str).equals("field"))
            vmWriter.writePop("LOCAL", symbolTable.indexOf(str));
        else{
            vmWriter.writePop(symbolTable.kindOf(str).toUpperCase(), symbolTable.indexOf(str));
        }
    }

    // complies a while statement
    public void compileWhile() throws IOException {
        // need to find better solution.
        int expNum, endNum;
        expNum = expCounter;
        endNum = endCounter;
        expCounter += 1;
        endCounter += 1;
        writeNext(); //while
        writeNext(); //(
        vmWriter.writeLabel("WHILE_EXP"+expNum);
        compileExpression();
        writeNext();
        writeNext();
        vmWriter.writeArithmetic("~");
        vmWriter.writeIf("WHILE_END"+endNum);
        compileStatements();
        writeNext();
        vmWriter.writeGoto("WHILE_EXP"+expNum);
        vmWriter.writeLabel("WHILE_END"+endNum);
    }

    // complies a return statement
    public void compileReturn() throws IOException {
        writeNext();
        if(!isPointOrBracket(";"))
            compileExpression();
        writeNext();
        vmWriter.writeReturn();
    }

    // complies an if statement with else clause.
    public void compileIf() throws IOException {
        // not sure need to do this;
        int numFalse, numTrue;
        numFalse = falseCounter;
        numTrue = trueCounter;
        falseCounter += 1;
        trueCounter += 1;
        writeNext();//if
        writeNext();//(
        compileExpression();
        vmWriter.writeArithmetic("~");
        vmWriter.writeIf("IF_FALSE"+numFalse);
        writeNext();//)
        writeNext();//{
        compileStatements();
        writeNext();//}
        vmWriter.writeGoto("IF_TRUE"+numTrue);
        if(isPointOrBracket("else")){
            writeNext();
            writeNext();
            vmWriter.writeLabel("IF_FALSE"+numFalse);
            compileStatements();
            vmWriter.writeLabel("label"+numTrue);
            writeNext();
        }

    }

    // complies an expression
    public void compileExpression() throws IOException {
        compileTerm();
        Map<String, String> operations = Map.of("+","ADD","-","SUB", "=", "EQ",
                "&lt;","LT", "&gt;", "GT", "&amp;", "AND", "|", "OR");
        String op;
        while (isOperation()){
            op = getValueAndNext();
            compileTerm();
            if (operations.get(op) != null)
                vmWriter.writeArithmetic(operations.get(op));
            else if (op.equals("*"))
                vmWriter.writeCall("Math.multiply", 2);
            else if (op.equals("/"))
                vmWriter.writeCall("Math.divide", 2);
        }
    }

    // compiles a term.
    public void compileTerm() throws IOException {
        String op;
        if(isPointOrBracket("-") || isPointOrBracket("~")){
            op = getValueAndNext();
            compileTerm();
            vmWriter.writeArithmetic(op);
        }else if(isPointOrBracket("(")){
            writeNext();
            compileExpression();
            writeNext();
        }else if (isPointOrBracket("<integerConstant>"))
            vmWriter.writePush("CONST", Integer.parseInt(getValueAndNext()));
        else if ((isPointOrBracket("<stringConstant>")))
            compileStringConst();
        else if ((isPointOrBracket("<keyword>")))
            compileKeyword();
        else{
            String arrOrFuncName = getValueAndNext();
            if(isPointOrBracket("["))
                compileArray(arrOrFuncName);
            else if(isPointOrBracket("."))
                compileSubCallWithClassName(arrOrFuncName);
            else if(isPointOrBracket("("))
                compileSubCall(arrOrFuncName);
            else { // variable identifier
                String varKind;
                int varIndex;
                varKind  = convertKind.get(symbolTable.kindOf(arrOrFuncName));
                varIndex = symbolTable.indexOf(arrOrFuncName);
                vmWriter.writePush(varKind, varIndex);

            }
        }

    }

    // complies a comma separated list of expressions.
    public int compileExpressionList() throws IOException {
        argCounter = 0;

        if(!isPointOrBracket(")")){
            compileExpression();
            argCounter += 1;
        }

        while (!isPointOrBracket(")")){
            writeNext();
            compileExpression();
            argCounter += 1;
        }

        return argCounter;
    }

    private void compileStringConst() throws IOException {
        String string;
        string = getValueAndNext();

        vmWriter.writePush("CONST", string.length());
        vmWriter.writeCall("String.new", 1);

        for (char ch: string.toCharArray()) {
            vmWriter.writePush("CONST", ch);
            vmWriter.writeCall("String.appendChar", 2);
        }
    }

    private void compileKeyword() throws IOException {
        String keyword = getValueAndNext();

        if (keyword.equals("this"))
            vmWriter.writePush("POINTER", 0);
        else vmWriter.writePush("CONST", 0);
        if (keyword.equals("true"))
            vmWriter.writeArithmetic("~");
    }

    private void compileArray(String varArray) throws IOException {
        String arrayKind;
        int arrayIndex;
        writeNext();
        compileExpression();
        writeNext();

        arrayKind = symbolTable.kindOf(varArray);
        arrayIndex = symbolTable.indexOf(varArray);
        vmWriter.writePush(convertKind.get(arrayKind), arrayIndex);

        vmWriter.writeArithmetic("ADD");
        vmWriter.writePush("POINTER",1);
        vmWriter.writePop("THAT", 0);
    }

    private void compileSubCallWithClassName(String classOrObjName) throws IOException {
        writeNext(); // next "."
        String callSentence;
        int numArguments = 0;
        String funcName = getValueAndNext();
        String type = symbolTable.typeOf(classOrObjName);
        if (type != null) {
            String kind = symbolTable.kindOf(classOrObjName);
            int index = symbolTable.indexOf(classOrObjName);
            vmWriter.writePush(convertKind.get(kind), index);
            callSentence = type + "." + funcName;
            numArguments++;
        }else callSentence = classOrObjName + "." + funcName;
        writeNext();
        numArguments = numArguments + compileExpressionList();
        writeNext();
        vmWriter.writeCall(callSentence, numArguments);
    }

    private void compileSubCall(String funcName) throws IOException {
        int numArguments;
        writeNext();
        numArguments = compileExpressionList() + 1;
        writeNext();
        vmWriter.writeCall(className, numArguments);
    }

    // read one command and write in xml.
    private void writeNext() throws IOException {
        currentCommand = bufferedReader.readLine();
    }

    private String getValueAndNext() throws IOException{
        currentCommand = bufferedReader.readLine();
        String[] ArrayString = currentCommand.split("<.+>");
        return ArrayString[0].substring(1,ArrayString[0].length()-2);
    }


    // checks if there are static or field.
    private boolean isVarDec() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isVarDec = cur.contains("field") || cur.contains("static");
        bufferedReader.reset();
        return isVarDec;
    }

    // checks if there are constructor or function or method.
    private boolean isSubDec() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isSubDe = cur.contains("constructor") || cur.contains("function") || cur.contains("method");
        bufferedReader.reset();
        return isSubDe;
    }

    // checks if there is identifier.
    private boolean isAnotherIden() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isAnotherId = !cur.contains(";");
        boolean isCurContains = cur.contains(",");
        bufferedReader.reset();
        if (isCurContains)
            bufferedReader.readLine();
        return isAnotherId;
    }

    // checks if there is ).
    private boolean isParameterList() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isParameterL = !cur.contains(")");
        bufferedReader.reset();
        if (isParameterL)
            bufferedReader.readLine();
        return isParameterL;
    }

    // checks if there is var.
    private boolean isVar() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isVar = cur.contains("var");
        bufferedReader.reset();
        return isVar;
    }

    // checks if there is symbol.
    private boolean isPointOrBracket(String symbol) throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isP = cur.contains(symbol);
        bufferedReader.reset();
        return isP;
    }

    // checks if there are let or if or while or do or return.
    private boolean isStatement() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isState = cur.contains("let") || cur.contains("if") || cur.contains("while") || cur.contains("do") ||
                cur.contains("return");
        bufferedReader.reset();
        return isState;
    }

    // checks if there are operations.
    private boolean isOperation() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        Pattern patt = Pattern.compile(".*> / <.*");
        Matcher match = patt.matcher(cur);
        boolean isOp = match.matches();
        boolean isGLAND = cur.contains("&gt;") || cur.contains("&lt;") || cur.contains("&amp;") || cur.contains("+") ||
                cur.contains("-") || cur.contains("*") || cur.contains("|") || cur.contains("=");
        bufferedReader.reset();
        return isOp || isGLAND;
    }

}
