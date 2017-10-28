package com.pugfish1992.javario;

import com.pugfish1992.javario.annotation.ModelSchema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes({
        "com.pugfish1992.javario.annotation.ModelSchema",
        "com.pugfish1992.javario.annotation.ModelSchemaOption",
        "com.pugfish1992.javario.annotation.FieldOption",
        "com.pugfish1992.javario.annotation.PrimaryKey"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JavarioProcessor extends AbstractProcessor {

    private static final String GENERATED_CLASS_PACKAGE = "com.pugfish1992.javario";

    private Filer mFiler;
    private Messager mMessager;
//    private Elements mElements;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mMessager = processingEnvironment.getMessager();
//        mElements = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        List<String> writtenModelClassNames = new ArrayList<>();
        ModelClassWriter modelClassWriter = new ModelClassWriter(mMessager, mFiler);

        // 1- Generate model classes from schema classes

        for (Element element : roundEnvironment.getElementsAnnotatedWith(ModelSchema.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }

            try {
                String writtenClassName = modelClassWriter.write((TypeElement) element, GENERATED_CLASS_PACKAGE);
                if (writtenClassName != null) {
                    writtenModelClassNames.add(writtenClassName);
                } else {
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 2- Generate a Javario class which initialize the javario

        try {
            JavarioClassWriter.write(mFiler, GENERATED_CLASS_PACKAGE, writtenModelClassNames);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
