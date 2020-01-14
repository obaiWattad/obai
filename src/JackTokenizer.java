import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This class change jack file in to token file
public class JackTokenizer {

    HashMap<String, String> dic = new HashMap<>();
    public String currentCommend;
    public String currentToken;
    private int currentTokenLine = -1;
    private String[] arr;
    private PrintWriter fileWriter;
    private BufferedReader bufferedReader;

    //constructer of class
    public JackTokenizer(PrintWriter fileWriter, BufferedReader bufferedReader) throws FileNotFoundException {
        this.bufferedReader = bufferedReader;
        this.fileWriter = fileWriter;
        fileWriter.println("<tokens>");
        this.dic.put("(","symbol");
        this.dic.put("{","symbol");
        this.dic.put("}","symbol");
        this.dic.put("[","symbol");
        this.dic.put("]","symbol");
        this.dic.put("*","symbol");
        this.dic.put("/","symbol");
        this.dic.put("&","symbol");
        this.dic.put("|","symbol");
        this.dic.put(">","symbol");
        this.dic.put("~","symbol");
        this.dic.put(")","symbol");
        this.dic.put(";","symbol");
        this.dic.put(",","symbol");
        this.dic.put(".","symbol");
        this.dic.put("=","symbol");
        this.dic.put("+","symbol");
        this.dic.put("-","symbol");
        this.dic.put("<","symbol");
        this.dic.put("class","keyword");
        this.dic.put("function","keyword");
        this.dic.put("method","keyword");
        this.dic.put("field","keyword");
        this.dic.put("static","keyword");
        this.dic.put("var","keyword");
        this.dic.put("int","keyword");
        this.dic.put("char","keyword");
        this.dic.put("boolean","keyword");
        this.dic.put("void","keyword");
        this.dic.put("true","keyword");
        this.dic.put("false","keyword");
        this.dic.put("null","keyword");
        this.dic.put("this","keyword");
        this.dic.put("let","keyword");
        this.dic.put("do","keyword");
        this.dic.put("if","keyword");
        this.dic.put("else","keyword");
        this.dic.put("while","keyword");
        this.dic.put("return","keyword");
        this.dic.put("constructor","keyword");
    }

    // This check if there is more tokens in jack file.
    public Boolean hasMoreTokens(){
        try {
            if(arr != null)
                return true;
            currentCommend = bufferedReader.readLine();
            if(currentCommend == null)
                return false;
            Pattern patt = Pattern.compile("[ |\t]*");
            Pattern patt2 = Pattern.compile("[ |\t]*[/]+.*");
            Pattern patt3 = Pattern.compile("[ |\t]*[/**]+.*");
            Matcher match = patt.matcher(currentCommend);
            Matcher match2 = patt2.matcher(currentCommend);
            Matcher match3 = patt3.matcher(currentCommend);
            while ((match.matches()||match2.matches()||match3.matches()) && currentCommend!=null) {
                currentCommend = bufferedReader.readLine();
                if(currentCommend == null)
                    break;
                match = patt.matcher(currentCommend);
                match2 = patt2.matcher(currentCommend);
                match3 = patt3.matcher(currentCommend);
            }
            return  currentCommend != null;
        } catch (IOException e) {
            return false;
        }
    }


    // This moves the tokens.
    public void advance() {
        char[] chars;
        String cur = "";
        if (arr != null && currentTokenLine != arr.length - 1) {
            currentTokenLine++;
            currentToken = arr[currentTokenLine];
            if (currentTokenLine == (arr.length - 1))
                arr = null;
        } else {
            currentTokenLine = -1;
            chars = currentCommend.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '/' && chars[i + 1] == '/')
                    break;
                if (dic.get(String.valueOf(chars[i])) != null)
                    cur += " " + chars[i] + " ";
                else
                    cur += chars[i];
            }
            arr = cur.split("\\s+");
            currentTokenLine++;
            if(arr[0].equals("")) {
                currentTokenLine++;
                currentToken = arr[1];
                if(arr.length == 2)
                    arr = null;
            }else {
                currentToken = arr[0];
                if(arr.length == 1)
                    arr = null;
            }
        }
        if(currentToken.contains("\"")){
            currentTokenLine++;
            currentToken += " " + arr[currentTokenLine];
            while(!arr[currentTokenLine].contains("\"")){
                currentTokenLine++;
                currentToken += " " + arr[currentTokenLine];
            }
            if (currentTokenLine == (arr.length - 1))
                arr = null;
        }
    }

    // This returns the token type.
    public String tokenType(){
        String type;
        type = dic.get(currentToken);
        if(type != null) {
            if(type.equals("symbol")) {
                if (currentToken.equals("<"))
                    fileWriter.println("<" + type + "> &lt; </" + type + ">");
                else if(currentToken.equals(">"))
                    fileWriter.println("<" + type + "> &gt; </" + type + ">");
                else if (currentToken.equals("&"))
                    fileWriter.println("<" + type + "> &amp; </" + type + ">");
                else fileWriter.println("<" + type + "> " + currentToken + " </" + type + ">");

            }
            else fileWriter.println("<" + type + "> " + currentToken + " </" + type + ">");
            return type.toUpperCase();
        }
        if(currentToken.contains("\"")) {
            String str = "";
            for(char c: currentToken.toCharArray()){
                if(c != '"')
                    str += c;
            }
            fileWriter.println("<stringConstant> " + str + " </stringConstant>");
            return "STRING_CONST";
        }
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if(pattern.matcher(currentToken).matches()) {
            fileWriter.println("<integerConstant> " + currentToken + " </integerConstant>");
            return "INT_CONST";
        }
        fileWriter.println("<identifier> " + currentToken + " </identifier>");
        return "IDENTIFIER";
    }

    //this returns upper letters of keyword.
    public String keyWord(){
        return currentToken.toUpperCase();
    }

    // this returns itself.
    public String identifier(){
        return currentToken;
    }

    // this returns symbol.
    public char symbol(){
        return currentToken.charAt(0);
    }

    // this returns string value.
    public String stringVal(){
        return currentToken;
    }

    // this returns int value.
    public int intVal(){
        return Integer.parseInt(currentToken);
    }

    // this close the token file.
    public void Close(){
        fileWriter.println("</tokens>");
        fileWriter.close();
    }

}
