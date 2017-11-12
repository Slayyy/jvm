import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.AASTORE;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ANEWARRAY;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.CHECKCAST;
import org.apache.bcel.generic.DUP;
import org.apache.bcel.generic.DUP2;
import org.apache.bcel.generic.GETSTATIC;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IADD;
import org.apache.bcel.generic.IFNONNULL;
import org.apache.bcel.generic.IF_ICMPEQ;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.INVOKEINTERFACE;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKESTATIC;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.NEW;
import org.apache.bcel.generic.POP;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.PUTSTATIC;
import org.apache.bcel.generic.RETURN;
import org.apache.bcel.generic.SWAP;

import java.io.IOException;
import java.util.Map;

import static org.apache.bcel.Constants.*;

public class Transform {

    private static final int LOCAL_MAP = 0;
    private static final int LOCAL_ITER = 1;
    private static final int LOCAL_KEY = 2;
    private static final int LOCAL_VALUE = 3;

    private static final int COUNT_THRESHOLD = 5;

    private static final String EXECUTION_STATS_FIELD_NAME = "$executionStats";
    private static final String CONSTRUCTOR = "<init>";
    private static final String STATIC_CONSTRUCTOR = "<clinit>";
    private static final String STATS = "$stats";
    private static final String INCREMENT = "$increment";

    private ConstantPoolGen constantPoolGen;
    private ConstantPoolGen counterClassConstantPoolGen;

    public static void main(String[] args) throws IOException {
        String file = args[0];
        JavaClass rewrittenClass = new ClassParser(file).parse();

        ClassGen counterClass = new ClassGen("$Counter",
                "java/lang/Object",
                "$Counter.java",
                ACC_PUBLIC,
                new String[0]);


        new Transform()
                .addStats(rewrittenClass, counterClass)
                .dump(file);

        counterClass.getJavaClass().dump("$Counter.class");
    }

    private JavaClass addStats(JavaClass oldClass, ClassGen counterClass) {
        ClassGen newClass = new ClassGen(oldClass);
        constantPoolGen = newClass.getConstantPool();
        counterClassConstantPoolGen = counterClass.getConstantPool();

        for (Method method : oldClass.getMethods()) {
            if (isStaticOrNormalConstructor(method)) {
                continue;
            }
            MethodGen newMethod = new MethodGen(method, newClass.getClassName(), constantPoolGen);
            enchanceClassWithIncrementCalls(newClass.getClassName(), counterClass.getClassName(), newMethod);
            newMethod.setMaxStack();
            newMethod.setMaxLocals();
            newClass.replaceMethod(method, newMethod.getMethod());
        }

        counterClass.addField(createStatsMap());
        counterClass.addInterface("java.lang.Runnable");
        counterClass.addMethod(createRunMethod(counterClass));
        counterClass.addMethod(defaultConstructorForCounter(counterClass));
        createStaticInitializer(counterClass);
        counterClass.addMethod(createInrecmentMethod(counterClass.getClassName()));
        counterClass.addMethod(createStatisticsPrintingMethod(counterClass));

        return newClass.getJavaClass();
    }

    private Method defaultConstructorForCounter(ClassGen counterClass) {
        InstructionList instructionList = new InstructionList();
        instructionList.append(new ALOAD(0));
        instructionList.append(new INVOKESPECIAL(
                counterClassConstantPoolGen.addMethodref(
                        "java/lang/Object",
                        CONSTRUCTOR,
                        "()V")));
        instructionList.append(new RETURN());

        MethodGen methodGen = new MethodGen(ACC_PUBLIC,
                Type.VOID,
                new Type[0],
                new String[0],
                CONSTRUCTOR,
                counterClass.getClassName(),
                instructionList,
                counterClassConstantPoolGen);

        methodGen.setMaxStack();
        methodGen.setMaxLocals();
        return methodGen.getMethod();
    }

    private Method createInrecmentMethod(String counterClassName) {
        InstructionList instructions = new InstructionList();

        instructions.append(new ALOAD(0));
        instructions.append(new GETSTATIC(statsMapFieldId(counterClassName)));
        instructions.append(new SWAP());
        instructions.append(new DUP2());
        instructions.append(new INVOKEINTERFACE(mapGetId(), 2));
        instructions.append(new DUP());
        BranchHandle ifNonNullJump = instructions.append(new IFNONNULL(null));
        instructions.append(new POP());
        instructions.append(new ICONST(0));
        instructions.append(new INVOKESTATIC(integerValueOfId()));
        InstructionHandle ifNonNullDest = instructions.append(new CHECKCAST(integerClassId()));
        instructions.append(new INVOKEVIRTUAL(integerIntValueId()));
        ifNonNullJump.setTarget(ifNonNullDest);
        instructions.append(new ICONST(1));
        instructions.append(new IADD());
        instructions.append(new INVOKESTATIC(integerValueOfId()));

        instructions.append(new INVOKEINTERFACE(mapPutId(), 3));
        instructions.append(new POP());

        instructions.append(new RETURN());
        MethodGen runMethod = new MethodGen(ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC,
                Type.VOID,
                new Type[]{Type.STRING},
                new String[]{"instrName"},
                INCREMENT, counterClassName,
                instructions,
                counterClassConstantPoolGen);

        runMethod.setMaxStack();
        return runMethod.getMethod();
    }

    private void createStaticInitializer(ClassGen counterClass) {
        String className = counterClass.getClassName();
        InstructionList instructions = new InstructionList();

        instructions.append(new NEW(treeMapId()));
        instructions.append(new DUP());
        instructions.append(new INVOKESPECIAL(treeMapConstructorId()));
        instructions.append(new PUTSTATIC(statsMapFieldId(className)));

        instructions.append(new INVOKESTATIC(runtimeGetRuntimeId()));
        instructions.append(new NEW(threadTypeId()));
        instructions.append(new DUP());
        instructions.append(new NEW(counterClassConstantPoolGen.addClass(className)));
        instructions.append(new DUP());
        instructions.append(new INVOKESPECIAL(counterClassConstructorId(className)));
        instructions.append(new INVOKESPECIAL(threadConstructorWithRunnableId()));
        instructions.append(new INVOKEVIRTUAL(addShutdownHookId()));
        instructions.append(new RETURN());

        MethodGen methodGen = new MethodGen(ACC_STATIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[0],
                STATIC_CONSTRUCTOR,
                className,
                instructions,
                counterClassConstantPoolGen);
        methodGen.setMaxStack();
        counterClass.addMethod(methodGen.getMethod());
    }

    private void enchanceClassWithIncrementCalls(String className, String counterClassName, MethodGen method) {
        InstructionList methodInstructions = method.getInstructionList();
        InstructionHandle inspectedInstructionHandle = methodInstructions.getStart();
        do {
            InstructionHandle nextHandle = inspectedInstructionHandle.getNext();

            Instruction instruction = inspectedInstructionHandle.getInstruction();

            if (isNotCallToExternalMethodOrOneStartingWithM(className, instruction)) {
                injectCounterBeforeInstruction(counterClassName, methodInstructions, inspectedInstructionHandle, method.getExceptionHandlers());
            }

            inspectedInstructionHandle = nextHandle;
        } while (inspectedInstructionHandle != null);
        methodInstructions.setPositions();
    }

    private void injectCounterBeforeInstruction(String counterClassName,
                                                InstructionList methodInstructions,
                                                InstructionHandle inspectedInstructionHandle,
                                                CodeExceptionGen[] exceptionHandlers) {
        InstructionList bumpingInstructions = new InstructionList();
        Instruction instruction = inspectedInstructionHandle.getInstruction();
        bumpingInstructions.append(new PUSH(constantPoolGen, instruction.getName()));
        bumpingInstructions.append(new INVOKESTATIC(incrementMethodId(counterClassName)));
        InstructionHandle newBranchTarget = methodInstructions.insert(inspectedInstructionHandle, bumpingInstructions);

        methodInstructions.redirectBranches(inspectedInstructionHandle, newBranchTarget);

        for (CodeExceptionGen exceptionHandler : exceptionHandlers) {
            if (exceptionHandler.getStartPC() == inspectedInstructionHandle) {
                exceptionHandler.setStartPC(newBranchTarget);
            }
            if (exceptionHandler.getHandlerPC() == inspectedInstructionHandle) {
                exceptionHandler.setHandlerPC(newBranchTarget);
            }
        }
    }

    private boolean isNotCallToExternalMethodOrOneStartingWithM(String className, Instruction instruction) {
        if (!(instruction instanceof InvokeInstruction)) {
            return true;
        }

        InvokeInstruction invokeInstruction = (InvokeInstruction) instruction;
        ObjectType calledMethodClassType = invokeInstruction.getLoadClassType(constantPoolGen);
        if (!className.equals(calledMethodClassType.getClassName())) {
            return false;
        }

        String methodName = invokeInstruction.getMethodName(constantPoolGen);
        return !methodName.startsWith("m");
    }

    private int incrementMethodId(String counterClassName) {
        return constantPoolGen.addMethodref(counterClassName, INCREMENT, "(Ljava/lang/String;)V");
    }

    private boolean isStaticOrNormalConstructor(Method method) {
        return CONSTRUCTOR.equals(method.getName()) || STATIC_CONSTRUCTOR.equals(method.getName());
    }


    private int threadTypeId() {
        return counterClassConstantPoolGen.addClass(new ObjectType("java.lang.Thread"));
    }

    private int treeMapId() {
        return counterClassConstantPoolGen.addClass(new ObjectType("java.util.TreeMap"));
    }

    private int mapPutId() {
        return counterClassConstantPoolGen.addInterfaceMethodref("java/util/Map",
                "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    }

    private int addShutdownHookId() {
        return counterClassConstantPoolGen.addMethodref("java/lang/Runtime",
                "addShutdownHook",
                "(Ljava/lang/Thread;)V");
    }

    private int threadConstructorWithRunnableId() {
        return counterClassConstantPoolGen.addMethodref("java/lang/Thread"
                , CONSTRUCTOR,
                "(Ljava/lang/Runnable;)V");
    }

    private int counterClassConstructorId(String className) {
        return counterClassConstantPoolGen.addMethodref(className,
                CONSTRUCTOR,
                "()V");
    }

    private int runtimeGetRuntimeId() {
        return counterClassConstantPoolGen.addMethodref("java/lang/Runtime",
                "getRuntime",
                "()Ljava/lang/Runtime;");
    }

    private int objectClassId() {
        return counterClassConstantPoolGen.addClass(ObjectType.OBJECT);
    }

    private int integerClassId() {
        return counterClassConstantPoolGen.addClass(new ObjectType("java.lang.Integer"));
    }

    private int integerIntValueId() {
        return counterClassConstantPoolGen.addMethodref(
                "java/lang/Integer",
                "intValue",
                "()I");
    }

    private int integerValueOfId() {
        return counterClassConstantPoolGen.addMethodref("java/lang/Integer",
                "valueOf",
                "(I)Ljava/lang/Integer;");
    }

    private Field createStatsMap() {
        return new FieldGen(ACC_PUBLIC | ACC_STATIC,
                Type.getType(Map.class),
                EXECUTION_STATS_FIELD_NAME,
                counterClassConstantPoolGen).getField();
    }

    private int treeMapConstructorId() {
        return counterClassConstantPoolGen.addMethodref("java/util/TreeMap",
                CONSTRUCTOR,
                "()V");
    }

    private int printfStatsId(ClassGen counterClass) {
        return counterClassConstantPoolGen.addMethodref(counterClass.getClassName(),
                STATS,
                "()V");
    }

    private Method createRunMethod(ClassGen counterClass) {
        InstructionList instructions = new InstructionList();
        instructions.append(new INVOKESTATIC(printfStatsId(counterClass)));
        instructions.append(new RETURN());
        MethodGen runMethod = new MethodGen(ACC_PUBLIC | ACC_SYNTHETIC,
                Type.VOID, new Type[0],
                new String[0],
                "run",
                counterClass.getClassName(),
                instructions, counterClassConstantPoolGen);

        runMethod.setMaxStack();
        return runMethod.getMethod();
    }

    private int statsMapFieldId(String className) {
        return counterClassConstantPoolGen.addFieldref(className,
                EXECUTION_STATS_FIELD_NAME,
                "Ljava/util/Map;");
    }

    private int mapKeySetId() {
        return counterClassConstantPoolGen.addInterfaceMethodref("java/util/Map",
                "keySet",
                "()Ljava/util/Set;");
    }

    private int collectionIteratorId() {
        return counterClassConstantPoolGen.addInterfaceMethodref("java/util/Set",
                "iterator",
                "()Ljava/util/Iterator;");
    }

    private int iteratorHasNextId() {
        return counterClassConstantPoolGen.addInterfaceMethodref("java/util/Iterator",
                "hasNext",
                "()Z");
    }

    private int iteratorNextId() {
        return counterClassConstantPoolGen.addInterfaceMethodref("java/util/Iterator",
                "next",
                "()Ljava/lang/Object;");
    }

    private int mapGetId() {
        return counterClassConstantPoolGen.addInterfaceMethodref("java/util/Map",
                "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;");
    }

    private int getSysOutConstIndexId() {
        return counterClassConstantPoolGen.addFieldref("java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
    }

    private int printStreamPrintfId() {
        return counterClassConstantPoolGen.addMethodref("java/io/PrintStream",
                "printf",
                "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;");
    }

    private Method createStatisticsPrintingMethod(ClassGen newClass) {

        InstructionList instructions = new InstructionList();

        instructions.append(new GETSTATIC(statsMapFieldId(newClass.getClassName())));
        instructions.append(new DUP());
        instructions.append(new ASTORE(LOCAL_MAP));

        instructions.append(new INVOKEINTERFACE(mapKeySetId(), 1));
        instructions.append(new INVOKEINTERFACE(collectionIteratorId(), 1));
        instructions.append(new DUP());
        instructions.append(new ASTORE(LOCAL_ITER));

        InstructionHandle loopEntry = instructions.append(new INVOKEINTERFACE(iteratorHasNextId(), 1));
        instructions.append(new ICONST(0));
        BranchHandle loopExitJump = instructions.append(new IF_ICMPEQ(null));

        instructions.append(new ALOAD(LOCAL_ITER));
        instructions.append(new ALOAD(LOCAL_MAP));
        instructions.append(new ALOAD(LOCAL_ITER));
        instructions.append(new INVOKEINTERFACE(iteratorNextId(), 1));
        instructions.append(new DUP());
        instructions.append(new ASTORE(LOCAL_KEY));
        instructions.append(new INVOKEINTERFACE(mapGetId(), 2));
        instructions.append(new DUP());
        instructions.append(new ASTORE(LOCAL_VALUE));


        instructions.append(new CHECKCAST(integerClassId()));
        instructions.append(new INVOKEVIRTUAL(integerIntValueId()));
        instructions.append(new ICONST(COUNT_THRESHOLD));

        instructions.append(new IF_ICMPLT(loopEntry));

        instructions.append(new GETSTATIC(getSysOutConstIndexId()));
        instructions.append(new PUSH(counterClassConstantPoolGen, "%s    %d%n"));
        instructions.append(new ICONST(2));
        instructions.append(new ANEWARRAY(objectClassId()));
        instructions.append(new DUP());
        instructions.append(new ICONST(0));
        instructions.append(new ALOAD(LOCAL_KEY));
        instructions.append(new AASTORE());

        instructions.append(new DUP());
        instructions.append(new ICONST(1));
        instructions.append(new ALOAD(LOCAL_VALUE));
        instructions.append(new AASTORE());

        instructions.append(new INVOKEVIRTUAL(printStreamPrintfId()));
        instructions.append(new POP());
        instructions.append(new GOTO(loopEntry));

        InstructionHandle loopExit = instructions.append(new RETURN());
        loopExitJump.setTarget(loopExit);

        MethodGen methodGen = new MethodGen(ACC_STATIC | ACC_PUBLIC,
                Type.VOID,
                Type.NO_ARGS,
                new String[0],
                STATS,
                newClass.getClassName(),
                instructions,
                counterClassConstantPoolGen);

        methodGen.setMaxLocals();
        methodGen.setMaxStack();
        return methodGen.getMethod();
    }
}