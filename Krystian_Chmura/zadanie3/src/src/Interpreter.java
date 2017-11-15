import generated.NPJLexer;
import generated.NPJParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.FileInputStream;
import java.io.IOException;

public class Interpreter {
    public static void main(String[] args) throws IOException {
        final String file = args[0];

        final int heapSize = Integer.valueOf(System.getProperty("npj.heap.size"));
        if (heapSize % 8 != 1)
            throw new RuntimeException("heapSize % 8 != 1");

        final int[] heap = new int[heapSize];

        NPJParser parser = createParser(file);
        parser.addParseListener(new ProgramInterpreter(new Memory(heapSize), heap));
        parser.program();
    }

    private static NPJParser createParser(String file) throws IOException {
        NPJLexer lexer = new NPJLexer(new ANTLRInputStream(new FileInputStream(file)));
        return new NPJParser(new CommonTokenStream(lexer));
    }
}
