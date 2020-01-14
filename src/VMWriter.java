import java.io.PrintWriter;

public class VMWriter {
    private PrintWriter fileWriter;

    public VMWriter(PrintWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    public void writePush(String Segment, int Index) {
        switch (Segment){
            case "CONST":
                fileWriter.println("push const "+ Index);
                break;
            case "ARG":
                fileWriter.println("push arg "+ Index);
                break;
            case "LOCAL":
                fileWriter.println("push local "+ Index);
                break;
            case "STATIC":
                fileWriter.println("push static "+ Index);
                break;
            case "THIS":
                fileWriter.println("push this "+ Index);
                break;
            case "THAT":
                fileWriter.println("push that "+ Index);
                break;
            case "POINTER":
                fileWriter.println("push pointer "+ Index);
                break;
            case "TEMP":
                fileWriter.println("push temp "+ Index);
                break;
        }
    }

    public void writePop(String Segment, int Index) {
        switch (Segment){
            case "CONST":
                fileWriter.println("pop const "+ Index);
                break;
            case "ARG":
                fileWriter.println("pop arg "+ Index);
                break;
            case "LOCAL":
                fileWriter.println("pop local "+ Index);
                break;
            case "STATIC":
                fileWriter.println("pop static "+ Index);
                break;
            case "THIS":
                fileWriter.println("pop this "+ Index);
                break;
            case "THAT":
                fileWriter.println("pop that "+ Index);
                break;
            case "POINTER":
                fileWriter.println("pop pointer "+ Index);
                break;
            case "TEMP":
                fileWriter.println("pop temp "+ Index);
                break;
        }
    }

    public void writeArithmetic(String command){
        switch (command){
            case "ADD":
                fileWriter.println("add");
                break;
            case "SUB":
                fileWriter.println("sub");
                break;
            case "-":
                fileWriter.println("neg");
                break;
            case "EQ":
                fileWriter.println("eq");
                break;
            case "GT":
                fileWriter.println("gt");
                break;
            case "LT":
                fileWriter.println("lt");
                break;
            case "AND":
                fileWriter.println("and");
                break;
            case "OR":
                fileWriter.println("or");
                break;
            case "~":
                fileWriter.println("not");
                break;
        }
    }

    public void writeLabel(String label){
        fileWriter.println("label"+ label);
    }

    public void writeGoto(String label){
        fileWriter.println("goto "+ label);
    }

    public void writeIf(String label){
        fileWriter.println("if-goto "+ label);
    }

    public void writeCall(String name, int nArgs){
        fileWriter.println("call "+ name+ " " + nArgs );
    }

    public void writeFunction(String name, int nLocals){
        fileWriter.println("function "+ name + nLocals);
    }

    public void writeReturn(){fileWriter.println("return");}

    public void close(){fileWriter.close();}
}
