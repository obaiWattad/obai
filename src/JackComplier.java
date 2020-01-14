import java.io.*;

public class JackComplier {
    private static CompilationEngine compilationEngine;
    private static PrintWriter fileWriter;
    private static PrintWriter fileWriterT,fileWriterCom;
    private static BufferedReader bufferedReader;

    public static void main(String[] args) throws IOException {
        File files = new File(args[0]);
        String fileName = "";
        if (files.isDirectory()){
            String[] fileNameA = files.getPath().split("[/]");
            fileName += "/" + fileNameA[0];
            for (int i=1; i<fileNameA.length; i++)
                fileName += "/" + fileNameA[i];
            File[] filesArray = files.listFiles();
            for (File file : filesArray){
                String[] fileS = file.getName().split("\\.");
                if (fileS.length > 1)
                    if (fileS[fileS.length - 1].equals("jack")){
                        translateFile(file, fileName,fileS[fileS.length-2]);
                    }
            }
        }else{
            String[] fileNameA = files.getPath().split("[/|\\.]");
            for(int i=1; i<fileNameA.length-2;i++)
                fileName += "/" + fileNameA[i];
            String[] pathList = files.getName().split("\\.");
            translateFile(files, fileName, pathList[0]);
        }

    }



    private static void translateFile(File file, String filePath, String fileName) throws IOException{
        FileReader fileReader = new FileReader(file);
        bufferedReader = new BufferedReader(fileReader);
        fileWriterT = new PrintWriter(filePath + "/" + fileName + "T.xml");
        JackTokenizer jackTokenizer = new JackTokenizer(fileWriterT, bufferedReader);
        while(jackTokenizer.hasMoreTokens()){
            jackTokenizer.advance();
            jackTokenizer.tokenType();
        }
        jackTokenizer.Close();
        File fileC = new File(filePath + "/" + fileName + "T.xml");
        FileReader fileReaderC = new FileReader(fileC);
        fileWriter = new PrintWriter( filePath + "/" + fileName + ".xml");
        compilationEngine = new CompilationEngine(fileWriter, fileReaderC);
        compilationEngine.compileClass();
        fileC.delete();
        File fileXml = new File(filePath + "/" + fileName + ".xml");
        FileReader fileReaderXml = new FileReader(fileXml);
        fileWriterCom = new PrintWriter(filePath + "/" + fileName + ".vm");
        VMWriter vmWriter = new VMWriter(fileWriterCom);




    }


    private static void codeWriteExpression(VMWriter vmWriter,SymbolTable symbolTable , String exp){
        if (isInteger(exp)){
            vmWriter.writePush("CONST", Integer.parseInt(exp));
        }
        if (symbolTable.kindOf(exp).equals("VAR")){
            vmWriter.writePush("VAR", symbolTable.indexOf(exp));
        }
        //not sure need to check
        String[] expList = exp.split("[\\+|\\*|\\-|\\/]",2);
        if (expList.length == 2) {
            codeWriteExpression(vmWriter, symbolTable, expList[0]);
            codeWriteExpression(vmWriter, symbolTable, expList[1]);
            //need to consider
            vmWriter.writeArithmetic("ADD");
        }
        if (expList.length == 1) {
            codeWriteExpression(vmWriter, symbolTable, expList[0]);
            vmWriter.writeArithmetic("SUB");
        }


    }


    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }
}
