class Constants {
    static final int NULLPTR = 0;

    static final int TCode = -100;
    static final int TSize = 4;

    static final int SCode = -200;
    static final int SSize = 2;

    static boolean isVarTypeCell(int i) {
        return i < 0;
    }
}
