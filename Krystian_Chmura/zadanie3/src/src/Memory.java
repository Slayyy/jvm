import java.util.Map;

public class Memory implements Collector {

    private final int halfSize;
    private int currentHalfBeginPos = 1;
    private int nextHalfBeginPos;
    private int nextAllocationPos;

    Memory(int heapSize) {
        halfSize = heapSize / 2;
        nextHalfBeginPos = halfSize + 1;
        nextAllocationPos = currentHalfBeginPos;
    }

     int getCurrentHalfBeginPos() {
        return currentHalfBeginPos;
    }

     int allocateT(int[] heap, Map<Object, Object> objects) {
        int tPos = allocateRaw(heap, objects, Type.T.baseSize);
        heap[tPos] = Type.T.code;
        heap[tPos + 1] = NPJConst.NULL_PTR;
        heap[tPos + 2] = NPJConst.NULL_PTR;
        heap[tPos + 3] = 0;
        return tPos;
    }

    int allocateS(int[] heap, Map<Object, Object> objects, int stringLength) {
        int sPos = allocateRaw(heap, objects, Type.S.baseSize + stringLength);
        heap[sPos] = Type.S.code;
        heap[sPos + NPJConst.STRING_LENGTH_OFFSET] = stringLength;
        return sPos;
    }

    private boolean isNoLeftMemoryForAllocation(int size) {
        return nextAllocationPos + size > currentHalfBeginPos + halfSize;
    }

    private int allocateRaw(int[] heap, Map<Object, Object> objects, int size) {
        if (isNoLeftMemoryForAllocation(size)) {
            collect(heap, objects);
        }

        if (isNoLeftMemoryForAllocation(size)) {
            throw new RuntimeException("out of memory");
        }

        int newMemoryPos = nextAllocationPos;
        nextAllocationPos += size;
        return newMemoryPos;
    }

    @Override
    public void collect(int[] heap, Map<Object, Object> params) {
        swapHeaps();

        nextAllocationPos = currentHalfBeginPos;

        for (VariableInfo info : ((Map<String, VariableInfo>) (Map) params).values())
            info.idx = copyToNewHeapIfAlive(heap, info.idx);

        int zeroEndIdx = nextHalfBeginPos + halfSize;

        for (int i = nextHalfBeginPos; i < zeroEndIdx; i++)
            heap[i] = 0;

    }

    private int copyToNewHeapIfAlive(int[] heap, int idx) {
        if (isVarTypeCell(heap[idx])) {
            int newIdx = nextAllocationPos;
            int variableSize = getVarSize(heap, idx);
            nextAllocationPos += variableSize;
            System.arraycopy(heap, idx, heap, newIdx, variableSize);
            heap[idx] = newIdx;
            if (Type.forCode(heap[newIdx]) == Type.T) {
                int f1 = heap[idx + 1];
                int f2 = heap[idx + 2];
                heap[newIdx + 1] = copyToNewHeapIfAlive(heap, f1);
                heap[newIdx + 2] = copyToNewHeapIfAlive(heap, f2);
            }
            return newIdx;
        } else {
            return heap[idx];
        }
    }

    private int getVarSize(int[] heap, int idx) {
        Type type = Type.forCode(heap[idx]);
        switch (type) {
            case T:
                return type.baseSize;
            case S:
                return type.baseSize + heap[idx + NPJConst.STRING_LENGTH_OFFSET];
        }
        throw new RuntimeException("Undefined variable size.");
    }

    private void swapHeaps() {
        int temp = nextHalfBeginPos;
        nextHalfBeginPos = currentHalfBeginPos;
        currentHalfBeginPos = temp;
    }

    static private boolean isVarTypeCell(int i)
    {
        return i < 0;
    }
}
