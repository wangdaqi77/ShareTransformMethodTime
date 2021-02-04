package com.demo.gradle.bytecode;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.println;


public class MethodDurationTransform extends Transform {


    public MethodDurationTransform(){
    }


    @Override
    public String getName() {
        return "MethodDurationTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        //需要处理的数据类型,这里表示class文件
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        //作用范围
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        //是否支持增量编译
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        outputProvider.deleteAll();

        for (TransformInput input : inputs) {

            for (JarInput jarInput : input.getJarInputs()) {
                String originJarFileName = jarInput.getFile().getName();
                String md5 = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (originJarFileName.endsWith(".jar")) {
                    originJarFileName = originJarFileName.substring(0, originJarFileName.length() - 4);
                }
                File destFile = outputProvider.getContentLocation(
                        originJarFileName + md5,
                        jarInput.getContentTypes(),
                        jarInput.getScopes(),
                        Format.JAR);

                // jar源复制到目标处
                FileUtils.copyFile(jarInput.getFile(), destFile);
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dest = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.getFile().getName(),
                        directoryInput.getContentTypes(),
                        directoryInput.getScopes(),
                        Format.DIRECTORY);

                transform(directoryInput.getFile(), dest);
            }
        }

    }

    private void transform(File input, File dest) throws IOException {

        FileUtils.forceMkdir(dest);
        String srcDirPath = input.getAbsolutePath();
        String destDirPath = dest.getAbsolutePath();
        for (File file : Objects.requireNonNull(input.listFiles())) {
            String destFilePath = file.getAbsolutePath().replace(srcDirPath, destDirPath);
            File destFile = new File(destFilePath);
            if (file.isDirectory()) {
                transform(file, destFile);
            } else if (file.isFile()) {
                if (file.getAbsolutePath().endsWith(".class") && !file.getName().equals("MethodDurationImpl.class")) {
                    FileUtils.touch(destFile);
                    addMethodDuration(file, destFile);
                }else {
                    FileUtils.copyFile(file, destFile);
                }
            }
        }
    }


    private void addMethodDuration(File input, File output) {
        String inputPath = input.getAbsolutePath();
        String outputPath = output.getAbsolutePath();
        println(this, "inputPath:" + inputPath + " , outputPath:" + outputPath);
        try {
            FileInputStream is = new FileInputStream(inputPath);
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassAdapter adapter = new ClassAdapter(cw, cr.getClassName());
            cr.accept(adapter, ClassReader.SKIP_FRAMES);
            FileOutputStream fos = new FileOutputStream(outputPath);
            fos.write(cw.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}