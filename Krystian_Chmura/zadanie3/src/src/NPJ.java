import java.util.List;
import java.util.Map;

class NPJ {
    static void collect(int[] heap, Collector collector, Map<Object, Object> params) {
        collector.collect(heap, params);
    }

    static void heapAnalyze(List<VarPosToValue> tValues, List<VarPosToValue> sValues) {
        System.out.println("-----------------HEAP ANALYZE-----------------");
        System.out.println("T:");
        for (VarPosToValue posToValue : tValues) {
            System.out.format("\t%5d: %s\n", posToValue.pos, posToValue.val);
        }
        System.out.println("S:");
        for (VarPosToValue posToValue : sValues) {
            System.out.format("\t%5d: %s\n", posToValue.pos, posToValue.val);
        }
    }
}