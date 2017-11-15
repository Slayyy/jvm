// Generated from NPJ.g4 by ANTLR 4.5.3
package generated;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NPJLexer extends Lexer {
    static {
        RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION);
    }

    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            NULL = 10, INT = 11, STRING = 12, QUOTED = 13, WS = 14;
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    public static final String[] ruleNames = {
            "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
            "NULL", "INT", "STRING", "QUOTED", "WS"
    };

    private static final String[] _LITERAL_NAMES = {
            null, "';'", "'VarDeclT'", "'VarDeclS'", "'\"'", "'='", "'.'", "'Print'",
            "'HeapAnalyze'", "'Collect'", "'NULL'"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, null, null, null, null, null, null, null, null, null, "NULL", "INT",
            "STRING", "QUOTED", "WS"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }


    public NPJLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @Override
    public String getGrammarFileName() {
        return "NPJ.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public static final String _serializedATN =
            "\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\20\u0084\b\1\4\2" +
                    "\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4" +
                    "\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\3\3\3\3\3\3\3\3" +
                    "\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\6\3\6" +
                    "\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3" +
                    "\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3" +
                    "\f\3\f\3\f\7\f\\\n\f\f\f\16\f_\13\f\5\fa\n\f\3\r\3\r\7\re\n\r\f\r\16\r" +
                    "h\13\r\3\16\3\16\7\16l\n\16\f\16\16\16o\13\16\3\16\6\16r\n\16\r\16\16" +
                    "\16s\3\16\7\16w\n\16\f\16\16\16z\13\16\3\16\3\16\3\17\6\17\177\n\17\r" +
                    "\17\16\17\u0080\3\17\3\17\2\2\20\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23" +
                    "\13\25\f\27\r\31\16\33\17\35\20\3\2\t\3\2\63;\3\2\62;\4\2C\\c|\5\2\62" +
                    ";C\\c|\3\2\"\"\6\2\"\"\62;C\\c|\5\2\13\f\17\17\"\"\u008a\2\3\3\2\2\2\2" +
                    "\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2" +
                    "\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2" +
                    "\33\3\2\2\2\2\35\3\2\2\2\3\37\3\2\2\2\5!\3\2\2\2\7*\3\2\2\2\t\63\3\2\2" +
                    "\2\13\65\3\2\2\2\r\67\3\2\2\2\179\3\2\2\2\21?\3\2\2\2\23K\3\2\2\2\25S" +
                    "\3\2\2\2\27`\3\2\2\2\31b\3\2\2\2\33i\3\2\2\2\35~\3\2\2\2\37 \7=\2\2 \4" +
                    "\3\2\2\2!\"\7X\2\2\"#\7c\2\2#$\7t\2\2$%\7F\2\2%&\7g\2\2&\'\7e\2\2\'(\7" +
                    "n\2\2()\7V\2\2)\6\3\2\2\2*+\7X\2\2+,\7c\2\2,-\7t\2\2-.\7F\2\2./\7g\2\2" +
                    "/\60\7e\2\2\60\61\7n\2\2\61\62\7U\2\2\62\b\3\2\2\2\63\64\7$\2\2\64\n\3" +
                    "\2\2\2\65\66\7?\2\2\66\f\3\2\2\2\678\7\60\2\28\16\3\2\2\29:\7R\2\2:;\7" +
                    "t\2\2;<\7k\2\2<=\7p\2\2=>\7v\2\2>\20\3\2\2\2?@\7J\2\2@A\7g\2\2AB\7c\2" +
                    "\2BC\7r\2\2CD\7C\2\2DE\7p\2\2EF\7c\2\2FG\7n\2\2GH\7{\2\2HI\7|\2\2IJ\7" +
                    "g\2\2J\22\3\2\2\2KL\7E\2\2LM\7q\2\2MN\7n\2\2NO\7n\2\2OP\7g\2\2PQ\7e\2" +
                    "\2QR\7v\2\2R\24\3\2\2\2ST\7P\2\2TU\7W\2\2UV\7N\2\2VW\7N\2\2W\26\3\2\2" +
                    "\2Xa\7\62\2\2Y]\t\2\2\2Z\\\t\3\2\2[Z\3\2\2\2\\_\3\2\2\2][\3\2\2\2]^\3" +
                    "\2\2\2^a\3\2\2\2_]\3\2\2\2`X\3\2\2\2`Y\3\2\2\2a\30\3\2\2\2bf\t\4\2\2c" +
                    "e\t\5\2\2dc\3\2\2\2eh\3\2\2\2fd\3\2\2\2fg\3\2\2\2g\32\3\2\2\2hf\3\2\2" +
                    "\2im\7$\2\2jl\t\5\2\2kj\3\2\2\2lo\3\2\2\2mk\3\2\2\2mn\3\2\2\2nq\3\2\2" +
                    "\2om\3\2\2\2pr\t\6\2\2qp\3\2\2\2rs\3\2\2\2sq\3\2\2\2st\3\2\2\2tx\3\2\2" +
                    "\2uw\t\7\2\2vu\3\2\2\2wz\3\2\2\2xv\3\2\2\2xy\3\2\2\2y{\3\2\2\2zx\3\2\2" +
                    "\2{|\7$\2\2|\34\3\2\2\2}\177\t\b\2\2~}\3\2\2\2\177\u0080\3\2\2\2\u0080" +
                    "~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0082\3\2\2\2\u0082\u0083\b\17\2\2" +
                    "\u0083\36\3\2\2\2\n\2]`fmsx\u0080\3\b\2\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }
}