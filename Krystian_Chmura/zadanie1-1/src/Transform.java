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
            MethodGen enrichedMethod = addCallInfoToMethod(currentMethod, clazz.getClassName(), constantPool);
            generateClass.replaceMethod(currentMethod, enrichedMethod.getMethod());
        }
        return generateClass.getJavaClass();
    }

    private static MethodGen addCallInfoToMethod(Method method, String className, ConstantPoolGen constantPool) {
        final MethodGen generateMethod = new MethodGen(method, className, constantPool);

        generateMethod.setMaxLocals(generateMethod.getMaxLocals() + 1);
        generateMethod.setMaxStack(generateMethod.getMaxStack() + 5);
        int tmpLocalIndex = generateMethod.getMaxLocals() - 1;

        final InstructionList instructions = generateMethod.getInstructionList();

        InstructionHandle nextIns = null;
        for (InstructionHandle ins = instructions.getStart(); ins != null; ins = nextIns) {
            nextIns = ins.getNext();
            Instruction instruction = ins.getInstruction();
            if (instruction instanceof InvokeInstruction && isInvokeReturnValue(instruction, constantPool)) {
                InvokeInstruction invokeInstruction = (InvokeInstruction) instruction;
                InstructionList beforeInvokeInstructions = methodInfoCommands(invokeInstruction, constantPool);
                instructions.insert(ins, beforeInvokeInstructions);

                InstructionList afterInvokeInstructions = methodResultInfo(instruction, tmpLocalIndex, constantPool);
                instructions.append(ins, afterInvokeInstructions);
            }
        }
        return generateMethod;
    }

    private static boolean isInvokeReturnValue(Instruction instruction, ConstantPoolGen constantPool) {
        final InvokeInstruction invokeInstruction = (InvokeInstruction) instruction;
        final String methodSignature = invokeInstruction.getSignature(constantPool);
        return !methodSignature.endsWith("V") && !methodSignature.endsWith(">");
    }

    private static InstructionList methodInfoCommands(InvokeInstruction invokeInstruction, ConstantPoolGen constantPool) {
        String methodName = invokeInstruction.getMethodName(constantPool);

        String args = "";
        for (Type argType : invokeInstruction.getArgumentTypes(constantPool)) {
            args += argType.getSignature();
        }

        String returnType = invokeInstruction.getReturnType(constantPool).getSignature();

        String methodToBeCalledMessage = String.format("Method to be called: %s(%s)%s", methodName, args, returnType);

        InstructionList instructions = new InstructionList();
        instructions.append(new GETSTATIC(getSysOutFromConstantPool(constantPool)));
        instructions.append(new PUSH(constantPool, methodToBeCalledMessage));
        instructions.append(new INVOKEVIRTUAL(getPrintStreamPrintFromConstantPool(constantPool)));
        return instructions;
    }

    private static InstructionList methodResultInfo(Instruction instruction, int tmpLocalIndex, ConstantPoolGen constantPool) {
        InvokeInstruction invokeInstruction = (InvokeInstruction) instruction;

        InstructionList instructions = new InstructionList();
        instructions.append(new DUP());
        Type returnType = invokeInstruction.getReturnType(constantPool);
        if (returnType instanceof BasicType) {
            instructions.append(new INVOKESTATIC(getStringValueOfFromConstantPool(returnType, constantPool)));
        }
        instructions.append(new ASTORE(tmpLocalIndex));
        instructions.append(new GETSTATIC(getSysOutFromConstantPool(constantPool)));
        instructions.append(new PUSH(constantPool, "Got result: %s%n"));
        instructions.append(new ICONST(1));
        instructions.append(new ANEWARRAY(getObjectClassFromConstantPool(constantPool)));
        instructions.append(new DUP());
        instructions.append(new ICONST(0));
        instructions.append(new ALOAD(tmpLocalIndex));
        instructions.append(new AASTORE());
        instructions.append(new INVOKEVIRTUAL(getPrintStreamPrintfFromConstantPool(constantPool)));
        instructions.append(new POP());
        return instructions;
    }
    private static int getSysOutFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addFieldref(
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
    }

    private static int getStringValueOfFromConstantPool(Type type, ConstantPoolGen constantPool) {
        type = (type == Type.SHORT || type == Type.BYTE) ? Type.INT : type;
        return constantPool.addMethodref(
                "java/lang/String",
                "valueOf",
                String.format("(%s)Ljava/lang/String;", type.getSignature()));
    }

    private static int getPrintStreamPrintFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addMethodref(
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V");
    }

    private static int getPrintStreamPrintfFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addMethodref(
                "java/io/PrintStream",
                "printf",
                "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/io/PrintStream;");
    }

    private static int getObjectClassFromConstantPool(ConstantPoolGen constantPool) {
        return constantPool.addClass(ObjectType.OBJECT);
    }
}