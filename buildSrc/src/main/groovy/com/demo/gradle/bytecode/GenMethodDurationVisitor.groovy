package com.demo.gradle.bytecode

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class GenMethodDurationVisitor extends AdviceAdapter {

    def typeName = "com/demo/app/utils/MethodDurationImpl"
    def varLocal = newLocal(Type.getType("Lcom/demo/app/utils/MethodDurationImpl;"))
    def className
    protected GenMethodDurationVisitor(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, String className) {
        super(api, methodVisitor, access, name, descriptor)
        this.className = className
    }

    @Override
    protected void onMethodEnter() {
        def methodName = name
        def className = className

        mv.visitTypeInsn(NEW, typeName)
        mv.visitInsn(DUP)
        mv.visitLdcInsn(className)
        mv.visitLdcInsn(methodName)
        mv.visitMethodInsn(INVOKESPECIAL, typeName, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false)
        mv.visitVarInsn(ASTORE, varLocal)

        mv.visitVarInsn(ALOAD, varLocal)
        mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "onBefore", "()V", false)

    }

    @Override
    protected void onMethodExit(int opcode) {
        mv.visitVarInsn(ALOAD, varLocal)
        mv.visitMethodInsn(INVOKEVIRTUAL, typeName, "onAfter", "()V", false)
    }
}
