package com.demo.gradle.bytecode

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


class ClassAdapter extends ClassVisitor {
    def className
    ClassAdapter(ClassVisitor classVisitor, String className) {
        super(Opcodes.ASM7, classVisitor)
        this.className = className
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        def methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        return new GenMethodDurationVisitor(api, methodVisitor, access, name, descriptor, className)
    }
}
