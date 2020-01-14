import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// this class change token file into xml file.
public class CompilationEngine {

    PrintWriter fileWriter;
    BufferedReader bufferedReader;
    String currentCommand;
    String fileName;
    private int nameCounter;
    private int funcCounter;
    private Boolean flag;
    private String whiteSpace;
    private int numWhiteSpace;

    // constructor of class
    public CompilationEngine(PrintWriter fileWriter, FileReader fileReader) throws IOException {
        whiteSpace = "";
        this.fileWriter = fileWriter;
        this.bufferedReader = new BufferedReader(fileReader);
        currentCommand = bufferedReader.readLine();

    }

    // it transltes class.
    public void compileClass() throws IOException {
        fileWriter.println("<class>");
        whiteSpace+="  ";
        writeNext();
        writeNext();
        writeNext();

        while(isVarDec())
            compileClassVarDec();
        while(isSubDec())
            compileSubroutine();

        writeNext();
        fileWriter.println("</class>");
        bufferedReader.close();
        fileWriter.close();
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
        boolean isAnotherId = cur.contains(",") || cur.contains("identifier");
        bufferedReader.reset();
        return isAnotherId;
    }

    // checks if there is ).
    private boolean isParameterList() throws IOException {
        bufferedReader.mark(1000);
        String cur;
        cur = bufferedReader.readLine();
        boolean isParameterL = !cur.contains(")");
        bufferedReader.reset();
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

    // compiles a static declaration or a field declaration.
    public void compileClassVarDec() throws IOException {
        fileWriter.println(whiteSpace + "<classVarDec>");
        whiteSpace+="  ";

        writeNext();
        writeNext();
        writeNext();
        while(isAnotherIden())
            writeNext();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</classVarDec>");
    }

    // complies a complete method, function, or constructor.
    public void compileSubroutine() throws IOException {
        fileWriter.println(whiteSpace + "<subroutineDec>");
        whiteSpace += "  ";
        writeNext();
        writeNext();
        writeNext();
        writeNext();
        compileParameterList();
        writeNext();
        fileWriter.println(whiteSpace + "<subroutineBody>");
        whiteSpace += "  ";
        writeNext();
        while (isVar())
            compileVarDec();
        compileStatements();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</subroutineBody>");
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</subroutineDec>");
    }

    // complies a parameter list, not including the enclosing "()"
    public void compileParameterList() throws IOException {
        fileWriter.println(whiteSpace + "<parameterList>");
        whiteSpace += "  ";
        while(isParameterList())
            writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</parameterList>");
    }

    // compiles a var declaration
    public void compileVarDec() throws IOException {
        fileWriter.println(whiteSpace + "<varDec>");
        whiteSpace += "  ";
        writeNext();
        writeNext();
        writeNext();
        while(isAnotherIden())
            writeNext();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</varDec>");
    }

    // compiles a sequence of statements.
    public void compileStatements() throws IOException {
        fileWriter.println(whiteSpace + "<statements>");
        whiteSpace += "  ";
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
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</statements>");
    }

    // complies a do statement
    public void compileDo() throws IOException {
        fileWriter.println(whiteSpace + "<doStatement>");
        whiteSpace += "  ";
        writeNext();
        writeNext();

        if(isPointOrBracket(".")){
            writeNext();
            writeNext();
        }

        writeNext();
        compileExpressionList();
        writeNext();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</doStatement>");
    }

    // complies a let statement.
    public void compileLet() throws IOException {
        fileWriter.println(whiteSpace + "<letStatement>");
        whiteSpace += "  ";
        writeNext();
        writeNext();
        if(isPointOrBracket("[")){
            writeNext();
            compileExpression();
            writeNext();
        }
        writeNext();
        compileExpression();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</letStatement>");
    }

    // complies a while statement
    public void compileWhile() throws IOException {
        fileWriter.println(whiteSpace + "<whileStatement>");
        whiteSpace += "  ";
        writeNext();
        writeNext();
        compileExpression();
        writeNext();
        writeNext();
        compileStatements();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</whileStatement>");
    }

    // complies a return statement
    public void compileReturn() throws IOException {
        fileWriter.println(whiteSpace + "<returnStatement>");
        whiteSpace += "  ";
        writeNext();
        if(!isPointOrBracket(";"))
            compileExpression();
        writeNext();
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</returnStatement>");
    }

    // complies an if statement with else clause.
    public void compileIf() throws IOException {
        fileWriter.println(whiteSpace + "<ifStatement>");
        whiteSpace += "  ";
        writeNext();
        writeNext();
        compileExpression();
        writeNext();
        writeNext();
        compileStatements();
        writeNext();
        if(isPointOrBracket("else")){
            writeNext();
            writeNext();
            compileStatements();
            writeNext();
        }
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</ifStatement>");
    }

    // complies an expression
    public void compileExpression() throws IOException {
        fileWriter.println(whiteSpace + "<expression>");
        whiteSpace += "  ";
        compileTerm();
        while (isOperation()){
            writeNext();
            compileTerm();
        }
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</expression>");
    }

    // compiles a term.
    public void compileTerm() throws IOException {
        fileWriter.println(whiteSpace + "<term>");
        whiteSpace += "  ";
        if(isPointOrBracket("-") || isPointOrBracket("~")){
            writeNext();
            compileTerm();
        }else if(isPointOrBracket("(")){
            writeNext();
            compileExpression();
            writeNext();
        }else{
            writeNext();
            if(isPointOrBracket("[")){
                writeNext();
                compileExpression();
                writeNext();
            }
            else if(isPointOrBracket(".")){
                writeNext();
                writeNext();
                writeNext();
                compileExpressionList();
                writeNext();
            }else if(isPointOrBracket("(")){
                writeNext();
                compileExpressionList();
                writeNext();
            }
        }
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</term>");
    }

    // complies a comma separated list of expressions.
    public void compileExpressionList() throws IOException {
        fileWriter.println(whiteSpace + "<expressionList>");
        whiteSpace += "  ";
        if(!isPointOrBracket(")"))
            compileExpression();
        while (!isPointOrBracket(")")){
            writeNext();
            compileExpression();
        }
        whiteSpace = whiteSpace.substring(2);
        fileWriter.println(whiteSpace + "</expressionList>");
    }

    // read one command and write in xml.
    private void writeNext() throws IOException {
        currentCommand = bufferedReader.readLine();
        fileWriter.println(currentCommand);
    }

}
