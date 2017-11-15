import com.sun.org.apache.xpath.internal.SourceTree;
import generated.NPJBaseListener;
import generated.NPJParser;

import java.util.*;

public class ProgramInterpreter extends NPJBaseListener {

    private final Memory memory;
    private final int[] heap;
    private final HashMap<String, VariableInfo> nameToVariableInfo;
    private final String NULL_TEXT = "NULL";

    ProgramInterpreter(Memory memory, int[] heap) {
        this.memory = memory;
        this.heap = heap;
        nameToVariableInfo = new HashMap<>();
    }

    @Override
    public void exitVarDeclT(NPJParser.VarDeclTContext ctx) {
        final int idx = memory.allocateT(heap, (Map) nameToVariableInfo);
        final String name = ctx.STRING().getText();
        final VariableInfo info = new VariableInfo(idx, Type.T);
        nameToVariableInfo.put(name, info);
    }

    @Override
    public void exitVarDeclSConst(NPJParser.VarDeclSConstContext ctx) {
        final String name = ctx.STRING(0).getText();
        final String value = ctx.STRING(1).getText();
        allocateString(name, value);
    }

    @Override
    public void exitVarDeclSNull(NPJParser.VarDeclSNullContext ctx) {
        final String name = ctx.STRING().getText();
        final VariableInfo date = new VariableInfo(NPJConst.NULL_PTR, Type.S);
        nameToVariableInfo.put(name, date);
    }

    @Override
    public void exitPrintStringMessage(NPJParser.PrintStringMessageContext ctx) {
        System.out.println(ctx.STRING().getText());
    }

    @Override
    public void exitPrintQuoted(NPJParser.PrintQuotedContext ctx) {
        final String message = ctx.QUOTED().getText();
        String withoutQuotes = message.substring(1, message.length() - 1);
        System.out.println(withoutQuotes);
    }

    @Override
    public void exitPrintStringConst(NPJParser.PrintStringConstContext ctx) {
        final VariableInfo info = nameToVariableInfo.get(ctx.STRING().getText());
        if (info.type != Type.S) {
            throw new RuntimeException("Could not print non-string value.");
        }

        System.out.println(readString(info.idx));
    }

    @Override
    public void exitCollect(NPJParser.CollectContext ctx) {
        NPJ.collect(heap, memory, (Map) nameToVariableInfo);
    }

    @Override
    public void exitAssignment(NPJParser.AssignmentContext ctx) {
        final String lValue = ctx.lValue().getText();
        final String rValue = ctx.rValue().getText();
        final int leftIdx = getPosOfTReference(lValue);
        if (NULL_TEXT.equals(rValue)) {
            heap[leftIdx] = NPJConst.NULL_PTR;
            return;
        } else if (rValue.startsWith("\"")) {
            allocateString(lValue, rValue.substring(1, rValue.length() - 1));
            return;
        }
        try {
            heap[leftIdx] = Integer.parseInt(rValue);
        } catch (NumberFormatException nfe) {
            heap[leftIdx] = getPosOfTReference(rValue);
        }
    }

    @Override
    public void exitHeapAnalyze(NPJParser.HeapAnalyzeContext ctx) {
        final ArrayList<VarPosToValue> typeTVariables = new ArrayList<>();
        final ArrayList<VarPosToValue> typeSVariables = new ArrayList<>();

        final int toSpaceIdx = memory.getCurrentHalfBeginPos();
        int idx = toSpaceIdx;
        while (idx < (toSpaceIdx + (heap.length / 2))) {
            int code = heap[idx];
            Type type = code < 0 ? Type.forCode(code) : null;
            if (type != null) {
                switch (type) {
                    case S:
                        typeSVariables.add(new VarPosToValue(idx, readString(idx)));
                        idx += Type.S.baseSize + heap[idx + NPJConst.STRING_LENGTH_OFFSET] - 1;
                        break;
                    case T:
                        typeTVariables.add(new VarPosToValue(idx, String.valueOf(heap[idx])));
                        idx += 3;
                }
            }
            idx++;
        }
        NPJ.heapAnalyze(typeTVariables, typeSVariables);
    }

    private void allocateString(String name, String value) {
        final int idx = memory.allocateS(heap, (Map) nameToVariableInfo, value.length());
        for (int i = 0; i < value.length(); i++) {
            heap[idx + Type.S.baseSize + i] = value.charAt(i);
        }
        nameToVariableInfo.put(name, new VariableInfo(idx, Type.S));
    }

    private String readString(int idx) {
        if (heap[idx] == NPJConst.NULL_PTR) {
            return NULL_TEXT;
        }

        final int length = heap[idx + NPJConst.STRING_LENGTH_OFFSET];
        final StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++)
            builder.append((char) heap[idx + Type.S.baseSize + i]);

        return builder.toString();
    }

    private int getPosOfTReference(String value) {
        List<String> parts = Arrays.asList(value.split("\\."));
        Iterator<String> it = parts.iterator();
        int idx = nameToVariableInfo.get(it.next()).idx;
        while (it.hasNext()) {
            switch (it.next()) {
                case "f1":
                    idx += 1;
                    break;
                case "f2":
                    idx += 2;
                    break;
                case "data":
                    idx += 3;
                    break;
                default:
                    throw new RuntimeException("invalid argument after dot operator");
            }
            if (it.hasNext()) {
                idx = heap[idx];
            }
        }
        return idx;
    }
}
