import java.util.Map;

public class Memory implements Collector {

    private final int halfSize;
    private int currentHalfBeginPos;
    private int nextHalfBeginPos;
    private int nextAllocationPos;

    Memory(int heapSize) {
        halfSize = heapSize / 2;
        currentHalfBeginPos = 1;
        nextHalfBeginPos = halfSize + 1;
        nextAllocationPos = currentHalfBeginPos;
    }

    @Override
    public void collect(int[] heap, Map<Object, Object> params) {
        swapHeaps();

        nextAllocationPos = currentHalfBeginPos;

        for (VarInfo info : ((Map<String, VarInfo>) (Map) params).values())
            info.posAtHeap = copyWithChildsToNewHeap(heap, info.posAtHeap);

        int previousHeapBegin = nextHalfBeginPos;
        int previousHeapEnd = nextHalfBeginPos + halfSize;


        for (int i = previousHeapBegin; i < previousHeapEnd; i++)
            heap[i] = 0;

    }

    private int copyWithChildsToNewHeap(int[] heap, int idx) {
        if (Constants.isVarTypeCell(heap[idx])) {
            int newVarPos = nextAllocationPos;
            int varSize = getVarSize(heap, idx);
            nextAllocationPos += varSize;
            System.arraycopy(heap, idx, heap, newVarPos, varSize);
            heap[idx] = newVarPos;
            if (heap[newVarPos] == Constants.TCode) {
                int f1 = heap[idx + 1];
                int f2 = heap[idx + 2];
                heap[newVarPos + 1] = copyWithChildsToNewHeap(heap, f1);
                heap[newVarPos + 2] = copyWithChildsToNewHeap(heap, f2);
            }
            return newVarPos;
        } else {
            return heap[idx];
        }
    }

    int getCurrentHalfBeginPos() {
        return currentHalfBeginPos;
    }

    int allocateEmptyT(int[] heap, Map<Object, Object> objects) {
        int tPos = allocateRaw(heap, objects, Constants.TSize);
        heap[tPos] = Constants.TCode;
        heap[tPos + 1] = Constants.NULLPTR;
        heap[tPos + 2] = Constants.NULLPTR;
        heap[tPos + 3] = 0;
        return tPos;
    }

    int allocateEmptyS(int[] heap, Map<Object, Object> objects, int stringLength) {
        int sPos = allocateRaw(heap, objects, Constants.SSize + stringLength);
        heap[sPos] = Constants.SCode;
        heap[sPos + 1] = stringLength;
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


    private int getVarSize(int[] heap, int idx) {
        switch (heap[idx]) {
            case Constants.TCode:
                return Constants.TSize;
            case Constants.SCode:
                return Constants.SSize + heap[idx + 1];
        }
        throw new RuntimeException("Undefined variable size.");
    }

    private void swapHeaps() {
        int temp = nextHalfBeginPos;
        nextHalfBeginPos = currentHalfBeginPos;
        currentHalfBeginPos = temp;
    }

}
