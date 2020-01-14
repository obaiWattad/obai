import java.io.*;

public class JackAnalyzer {

    private static CompilationEngine compilationEngine;
    private static PrintWriter fileWriter;
    private static PrintWriter fileWriterT, fileWriterCom;
    private static BufferedReader bufferedReader;
    private static BufferedReader bufferedReaderC;


    // This is the main function of program.
    public static void main(String[] args) throws IOException {
        File files = new File(args[0]);
        System.out.println(files);
        String fileName = "";
        System.out.println(files.isDirectory());
        if (files.isDirectory()) {
            String[] fileNameA = files.getPath().split("[/]");
            fileName += fileNameA[0];
            for(int i=1;i<fileNameA.length;i++)
                fileName += "/" + fileNameA[i];
            System.out.println(fileName);
            File[] filesArray = files.listFiles();
            for (File file : filesArray) {
                String[] fileS = file.getName().split("\\.");
                System.out.println(file.getName());
                if (fileS.length > 1)
                    if (fileS[fileS.length - 1].equals("jack")) {
                        translateFile(file, fileName, fileS[fileS.length - 2]);
                    }
            }
        }else {
            String[] fileNameA = files.getPath().split("[/|\\.]");
            for(int i=1;i<fileNameA.length-2;i++)
            fileName += "/" + fileNameA[i];
            String[] pathList = files.getName().split("\\.");
            translateFile(files, fileName, pathList[0]);
        }

    }

    // This translate file from jack to xml.
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
        fileWriter = new PrintWriter( filePath + "/" + fileName + ".vm");
        compilationEngine = new CompilationEngine(fileWriter, fileReaderC);
        compilationEngine.compileClass();
        fileC.delete();
    }
}
