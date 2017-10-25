package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.ModelSchema;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.pugfish1992.javario.annotation.ModelSchema")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JavarioProcessor extends AbstractProcessor {

    private static final String METHOD_PREFIX = "start";
    private static final ClassName classIntent = ClassName.get("android.content", "Intent");
    private static final ClassName classContext = ClassName.get("android.content", "Context");

    private Filer mFiler;
    private Messager mMessager;
    private Elements mElements;
    private Map<String, String> mActivitiesWithPackage;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
        mElements = processingEnvironment.getElementUtils();
        mActivitiesWithPackage = new HashMap<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        /**
         * 1- Find all annotated element
         */
        for (Element element : roundEnvironment.getElementsAnnotatedWith(ModelSchema.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            TypeElement typeElement = (TypeElement) element;
            mActivitiesWithPackage.put(
                    typeElement.getSimpleName().toString(),
                    mElements.getPackageOf(typeElement).getQualifiedName().toString());
        }


        /**
         * 2- Generate a class
         */
        TypeSpec.Builder navigatorClass = TypeSpec
                .classBuilder("Navigator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (Map.Entry<String, String> element : mActivitiesWithPackage.entrySet()) {
            String activityName = element.getKey();
            String packageName = element.getValue();
            ClassName activityClass = ClassName.get(packageName, activityName);
            MethodSpec intentMethod = MethodSpec
                    .methodBuilder(METHOD_PREFIX + activityName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(classIntent)
                    .addParameter(classContext, "context")
                    .addStatement("return new $T($L, $L)", classIntent, "context", activityClass + ".class")
                    .build();
            navigatorClass.addMethod(intentMethod);
        }

        try {
            /**
             * 3- Write generated class to a file
             */
            JavaFile.builder("com.pugfish1992.plumber", navigatorClass.build()).build().writeTo(mFiler);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
