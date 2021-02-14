import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.runtime.CharStreams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class cal {

    public static void writeToFile(String fileName, String value) throws IOException {
        String fn = Paths.get(fileName).getFileName().toString().split("\\.")[0] + ".ir";

        Files.write(Paths.get(fn), Collections.singleton(value));
    }

    public static void main(String[] args) throws IOException {

        String inputFile = (args.length > 0) ? args[0] : null;

        if (inputFile == null) {
            System.out.println("Please provide an input file");
            System.exit(0);
        }

        InputStream is = new FileInputStream(inputFile);
        CALLexer lexer = new CALLexer(CharStreams.fromStream(is));

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CALParser parser = new CALParser(tokens);

        ParseTree tree = parser.prog();

        EvalVisitor eval = new EvalVisitor();
        eval.visit(tree);

        IRGeneratorVisitor ir = new IRGeneratorVisitor();
        String irCode = ir.visit(tree);

        writeToFile(inputFile, irCode);

    }
}