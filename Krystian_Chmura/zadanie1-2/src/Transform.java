import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.IOException;

public class Transform {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.exit(1);
        }

        final String classFile = args[0];
        final JavaClass clazz = new ClassParser(classFile).parse();
        final JavaClass resultClass = transformClass(clazz);
        resultClass.dump(classFile);
    }

    private static JavaClass transformClass(JavaClass clazz) {
        final ClassGen generateClass = new ClassGen(clazz);
        final ConstantPoolGen constantPool = generateClass.getConstantPool();

        for (Method currentMethod : generateClass.getMethods()) {
            MethodGen enrichedMethod = addGetFieldInfoToMethod(currentMethod, clazz.getClassName(), constantPool);
            generateClass.replaceMethod(currentMethod, enrichedMethod.getMethod());
        }
        return generateClass.getJavaClass();
    }

    private static MethodGen addGetFieldInfoToMethod(Method method, String className, ConstantPoolGen constantPool) {
        final MethodGen generateMethod = new MethodGen(method, className, constantPool);

        generateMethod.setMaxStack(generateMethod.getMaxStack() + 6);
        generateMethod.setMaxLocals(generateMethod.getMaxLocals() + 1);
        int tmpLocalVariableIndex = generateMethod.getMaxLocals() - 1;

        final InstructionList instructions = generateMethod.getInstructionList();

        InstructionHandle nextIns;
        for (InstructionHandle ins = instructions.getStart(); ins != null; ins = nextIns) {
            nextIns = ins.getNext();
            Instruction instruction = ins.getInstruction();

            boolean isPrimitive =
                    instruction instanceof GETFIELD && ((GETFIELD) instruction).getFieldType(constantPool) instanceof BasicType;
            if (isPrimitive) {
                GETFIELD getFieldInstruction = (GETFIELD) instruction;
                Type accessedFieldType = getFieldInstruction.getFieldType(constantPool);

                InstructionList instructionsToPrepend = printAccessedFieldInfo(tmpLocalVariableIndex, getFieldInstruction, accessedFieldType, constantPool);
                instructions.insert(instruction, instructionsToPrepend);

                if (isNumeric(accessedFieldType)) {
                    InstructionList thresholdCheck = checkIfResultIsLargerThanThreshold(ins, accessedFieldType, constantPool);
                    instructions.append(instruction, thresholdCheck);
                }
            }
        }
        instructions.setPositions();

        return generateMethod;
    }

    private static InstructionList printAccessedFieldInfo(int tmpLocalVariableIndex, GETFIELD getFieldInstruction, Type accessedFieldType, ConstantPoolGen constantPool) {
        InstructionList instructionsToPrepend = new InstructionList();

        String targetType = getFieldInstruction.getLoadClassType(constantPool).getClassName();
        String fieldType = accessedFieldType.toString();
        String fieldName = getFieldInstruction.getFieldName(constantPool);

        instructionsToPrepend.append(new DUP());
        instructionsToPrepend.append(new ASTORE(tmpLocalVariableIndex));
        instructionsToPrepend.append(new GETSTATIC(getSysOutFromConstantPool(constantPool)));
        instructionsToPrepend.append(new PUSH(constantPool, "Before getfield:%n    %s%n    %s%n    %s%n    %s%n"));
        createNewArray(instructionsToPrepend, constantPool, getObjectClassFromConstantPool(constantPool), 4);
        insertStringIntoArray(instructionsToPrepend, constantPool, 0, targetType);
        insertStringIntoArray(instructionsToPrepend, constantPool, 1, fieldType);
        insertStringIntoArray(instructionsToPrepend, constantPool, 2, fieldName);
        insertFieldValueObjectIntoArray(instructionsToPrepend, constantPool, 3, tmpLocalVariableIndex, getFieldInstruction);
        instructionsToPrepend.append(new INVOKEVIRTUAL(getPrintStreamPrintfFromConstantPool(constantPool)));
        instructionsToPrepend.append(new POP());
        return instructionsToPrepend;
    }

    private static InstructionList checkIfResultIsLargerThanThreshold(InstructionHandle instructionHandle, Type accessedFieldType, ConstantPoolGen constantPool) {
        InstructionList instructionsToAppend = new InstructionList();

        instructionsToAppend.append(InstructionFactory.createDup(accessedFieldType.getSize()));
        if (accessedFieldType.equals(Type.FLOAT)) {
            instructionsToAppend.append(new PUSH(constantPool, 30.0f));
            instructionsToAppend.append(new FCMPL());
            instructionsToAppend.append(new ICONST(1));
            instructionsToAppend.append(new IF_ICMPLT(instructionHandle.getNext()));
        } else if (accessedFieldType.equals(Type.DOUBLE)) {
            instructionsToAppend.append(new PUSH(constantPool, 30.0));
            instructionsToAppend.append(new DCMPL());
            instructionsToAppend.append(new ICONST(1));
            instructionsToAppend.append(new IF_ICMPLT(instructionHandle.getNext()));
        } else if (accessedFieldType.equals(Type.LONG)) {
            instructionsToAppend.append(new PUSH(constantPool, 30L));
            instructionsToAppend.append(new LCMP());
            instructionsToAppend.append(new ICONST(1));
            instructionsToAppend.append(new IF_ICMPLT(instructionHandle.getNext()));
        } else {
            instructionsToAppend.append(new PUSH(constantPool, 30));
            instructionsToAppend.append(new IF_ICMPLE(instructionHandle.getNext()));
        }
        instructionsToAppend.append(new GETSTATIC(getSysOutFromConstantPool(constantPool)));
        instructionsToAppend.append(new PUSH(constantPool, "    !the value is greater than 30!"));
        instructionsToAppend.append(new INVOKEVIRTUAL(getPrintStreamPrintlnFromConstantPool(constantPool)));
        return instructionsToAppend;
    }

    private static void createNewArray(InstructionList instructionList, ConstantPoolGen constantPool, int elementClassFromConstantPool, int size) {
        instructionList.append(new PUSH(constantPool, size));
        instructionList.append(new ANEWARRAY(elementClassFromConstantPool));
    }

    private static void insertFieldValueObjectIntoArray(InstructionList instructionList, ConstantPoolGen constantPool, int index, int targetObjectLocalIndex, GETFIELD getFieldInstruction) {
        instructionList.append(new DUP());
        instructionList.append(new PUSH(constantPool, index));
        instructionList.append(new ALOAD(targetObjectLocalIndex));
        instructionList.append(getFieldInstruction);
        stringValueOfPrimitiveType(instructionList, constantPool, getFieldInstruction);
        instructionList.append(new AASTORE());
    }

    private static void insertStringIntoArray(InstructionList instructionList, ConstantPoolGen constantPool, int index, String value) {
        instructionList.append(new DUP());
        instructionList.append(new PUSH(constantPool, index));
        instructionList.append(new PUSH(constantPool, value));
        instructionList.append(new AASTORE());
    }

    private static void stringValueOfPrimitiveType(InstructionList instructionList, ConstantPoolGen constantPool, GETFIELD getFieldInstruction) {
        Type printedType = getFieldInstruction.getFieldType(constantPool);
        if (printedType.equals(Type.BYTE) || printedType.equals(Type.SHORT)) {
            printedType = Type.INT;
        }
        instructionList.append(new INVOKESTATIC(getStringValueOfFromConstantPool(printedType, constantPool)));
    }

    private static boolean isNumeric(Type fieldType) {
        return fieldType.equals(Type.BYTE)
                || fieldType.equals(Type.SHORT)
                || fieldType.equals(Type.INT)
                || fieldType.equals(Type.LONG)
                || fieldType.equals(Type.FLOAT)
                || fieldType.equals(Type.DOUBLE);
    }

    private static int getSysOutFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addFieldref("java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
    }

    private static int getPrintStreamPrintlnFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addMethodref("java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V");
    }

    private static int getPrintStreamPrintfFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addMethodref(
                "java/io/PrintStream",
                "printf",
                "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;");
    }

    private static int getStringValueOfFromConstantPool(Type argType, ConstantPoolGen constantPool) {
        return constantPool.addMethodref(
                "java/lang/String",
                "valueOf",
                String.format("(%s)Ljava/lang/String;",
                        argType.getSignature()));
    }

    private static int getObjectClassFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addClass(ObjectType.OBJECT);
    }
}