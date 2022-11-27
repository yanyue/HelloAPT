package com.example.apt_processor;

import com.example.apt_annotation.Executor;
import com.example.apt_annotation.SpeechExecutor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class MyProcessor extends AbstractProcessor {

    private Messager mMessager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(Executor.class.getCanonicalName());
        return hashSet;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Hello APT");
    }

    public static CodeBlock generateIfClause(Set<? extends Element> elements, String str) {
        Builder mainBuilder = CodeBlock.builder();
        int i = 0;
        for (Element element : elements) {
            Executor[] es = element.getAnnotationsByType(Executor.class);
            if (i == 0) {
                CodeBlock cb = CodeBlock.builder()
//                    .beginControlFlow("if ($L == $S)", str, es[0].name())
                    .addStatement("return new $T()", element.asType())
//                    .endControlFlow()
                    .build();
                return cb;
            }
            i++;
        }

        return mainBuilder.build();
    }

    private void generateCode(Set<? extends Element> elements) {
        //生成类
        TypeSpec.Builder classBuilder = TypeSpec
            .classBuilder("ExecutorFactory")

            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        //生成方法
        MethodSpec method = MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(String.class, "string")
            .returns(SpeechExecutor.class)
//            .returns(void.class)
            .addCode(generateIfClause(elements, "string"))
            .build();

        classBuilder.addMethod(method);

        //包
        JavaFile javaFile = JavaFile
            .builder("com.example.helloapt", classBuilder.build())
            .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mMessager.printMessage(Diagnostic.Kind.NOTE, "Hello APT in process");

        //拿到所有添加Print注解的成员变量
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Executor.class);
        for (Element element : elements) {
            //拿到成员变量名
            Name simpleName = element.getSimpleName();
            //输出成员变量名
            mMessager.printMessage(Diagnostic.Kind.NOTE, element.toString());
        }

        // 产生代码
        generateCode(elements);

        return false;
    }
}
