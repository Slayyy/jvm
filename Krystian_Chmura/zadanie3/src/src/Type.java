public enum Type {

    S(-2, 2), T(-4, 4);
    public final int code;
    public final int baseSize;

    Type(final int code, final int baseSize) {
        this.code = code;
        this.baseSize = baseSize;
    }

    public static Type forCode(int code) {
        switch (code) {
            case -2:
                return S;
            case -4:
                return T;
            default:
                throw new RuntimeException("code not supported");
        }
    }
}
