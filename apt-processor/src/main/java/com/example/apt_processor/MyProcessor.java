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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

    public static CodeBlock generateInnerClause(List<? extends Element> elements, String str) {
        Builder mainBuilder = CodeBlock.builder();
        if (elements.size() == 0) {
            return mainBuilder.addStatement("return null").build();
        }

        int i = 0;
        for (Element element : elements) {
            Executor[] es = element.getAnnotationsByType(Executor.class);
            if (i == 0) {
                mainBuilder.beginControlFlow("if ($L == $S)", str, es[0].name());
            } else {
                mainBuilder.nextControlFlow("else if ($L == $S)", str, es[0].name());
            }
            mainBuilder.addStatement("return new $T()", element.asType());
            i++;
        }
        mainBuilder.nextControlFlow("else");
        mainBuilder.addStatement("return null");
        mainBuilder.endControlFlow();

        return mainBuilder.build();
    }

    private void generateCode(Set<? extends Element> elements) {
        //生成类
        TypeSpec.Builder classBuilder = TypeSpec
            .classBuilder("ExecutorFactory")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        List<Element> list = new ArrayList<>();
        for (Element element : elements) {
//            if (element.getClass().getSuperclass() != SpeechExecutor.class) {
//                continue;
//            }
            list.add(element);
        }

        //生成方法
        MethodSpec method = MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(String.class, "sName")
            .returns(SpeechExecutor.class)
            .addCode(generateInnerClause(list, "sName")).build();

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
        mMessager.printMessage(Diagnostic.Kind.NOTE, "process size=" + annotations.size());

        //拿到所有添加Print注解的成员变量
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Executor.class);
        for (Element element : elements) {
            //拿到成员变量名
            Name simpleName = element.getSimpleName();
            //输出成员变量名
            mMessager.printMessage(Diagnostic.Kind.NOTE,
                "simpleName =" + simpleName
                + " superName ="
                + element.asType().getClass());
//                + Arrays.toString(element.asType().getKind().getInterfaces()));
        }

        if (annotations.size() > 0) {
            // 产生代码
            generateCode(elements);
        }

        return false;
    }
}
