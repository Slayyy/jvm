import generated.NPJBaseListener;
import generated.NPJParser;

import java.util.*;

public class ProgramInterpreter extends NPJBaseListener {

    private final Memory memory;
    private final int[] heap;
    private final HashMap<String, VarInfo> varNameToInfo;
    private final String NULL_TEXT = "NULL";

    ProgramInterpreter(Memory memory, int[] heap) {
        this.memory = memory;
        this.heap = heap;
        varNameToInfo = new HashMap<>();
    }

    @Override
    public void exitVarDeclT(NPJParser.VarDeclTContext ctx) {
        final int idx = memory.allocateEmptyT(heap, (Map) varNameToInfo);
        final String name = ctx.STRING().getText();
        final VarInfo info = new VarInfo(idx, Constants.TCode);
        varNameToInfo.put(name, info);
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
        final VarInfo date = new VarInfo(Constants.NULLPTR, Constants.SCode);
        varNameToInfo.put(name, date);
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
        final VarInfo info = varNameToInfo.get(ctx.STRING().getText());
        if (info.code != Constants.SCode) {
            throw new RuntimeException("Could not print non-string value.");
        }

        System.out.println(readStringFromVarOnHeap(info.posAtHeap));
    }

    @Override
    public void exitCollect(NPJParser.CollectContext ctx) {
        NPJ.collect(heap, memory, (Map) varNameToInfo);
    }

    @Override
    public void exitAssignment(NPJParser.AssignmentContext ctx) {
        final String lhs = ctx.lValue().getText();
        final String rhs = ctx.rValue().getText();
        final int lPos = getPosOfTReference(lhs);
        if (NULL_TEXT.equals(rhs)) {
            heap[lPos] = Constants.NULLPTR;
            return;
        } else if (rhs.startsWith("\"")) {
            allocateString(lhs, rhs.substring(1, rhs.length() - 1));
            return;
        }
        try {
            heap[lPos] = Integer.parseInt(rhs);
        } catch (NumberFormatException nfe) {
            heap[lPos] = getPosOfTReference(rhs);
        }
    }

    @Override
    public void exitHeapAnalyze(NPJParser.HeapAnalyzeContext ctx) {
        final ArrayList<VarPosToValue> typeTVariables = new ArrayList<>();
        final ArrayList<VarPosToValue> typeSVariables = new ArrayList<>();

        int pos = memory.getCurrentHalfBeginPos();
        while (pos < (memory.getCurrentHalfBeginPos() + (heap.length / 2))) {
            switch (heap[pos]) {
                case Constants.SCode:
                    typeSVariables.add(new VarPosToValue(pos, readStringFromVarOnHeap(pos)));
                    pos += Constants.SSize + heap[pos + 1] - 1;
                    break;
                case Constants.TCode:
                    typeTVariables.add(new VarPosToValue(pos, String.valueOf(heap[pos + 3])));
                    pos += 3;
                    break;
                default:
            }
            pos++;
        }
        NPJ.heapAnalyze(typeTVariables, typeSVariables);
    }

    private void allocateString(String name, String value) {
        final int pos = memory.allocateEmptyS(heap, (Map) varNameToInfo, value.length());
        for (int i = 0; i < value.length(); i++) {
            heap[pos + Constants.SSize + i] = value.charAt(i);
        }
        varNameToInfo.put(name, new VarInfo(pos, Constants.SCode));
    }

    private String readStringFromVarOnHeap(int pos) {
        if (heap[pos] == Constants.NULLPTR) {
            return NULL_TEXT;
        }

        final int length = heap[pos + 1];
        final StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++)
            builder.append((char) heap[pos + Constants.SSize + i]);

        return builder.toString();
    }

    private int getPosOfTReference(String value) {
        List<String> parts = Arrays.asList(value.split("\\."));
        Iterator<String> it = parts.iterator();
        int pos = varNameToInfo.get(it.next()).posAtHeap;
        while (it.hasNext()) {
            switch (it.next()) {
                case "f1":
                    pos += 1;
                    break;
                case "f2":
                    pos += 2;
                    break;
                case "data":
                    pos += 3;
                    break;
                default:
                    throw new RuntimeException("invalid argument after dot operator");
            }
            if (it.hasNext()) {
                pos = heap[pos];
            }
        }
        return pos;
    }
}
