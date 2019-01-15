package tools.android.serial.compiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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

import tools.android.serial.annotation.Serial;

@SupportedAnnotationTypes("tools.android.serial.annotation.Serial")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SerialProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Serial.class);
        for (Element element : elements) {
            ElementKind elementKind = element.getKind();
            if (elementKind == ElementKind.CLASS) {
                String packageName;
                TypeElement element4Class = (TypeElement) element;
                String clazzName = element4Class.getSimpleName().toString();
                String CLASSNAME = clazzName + "Serial";
                packageName = elementUtils.getPackageOf(element4Class).toString();

                String message = "annotation found in " + clazzName + " with Serial " + CLASSNAME;
                messager.printMessage(Diagnostic.Kind.NOTE, message);

                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASSNAME)
                        .addModifiers(Modifier.PUBLIC);

//                MethodSpec.Builder build = MethodSpec.methodBuilder("getMaps").addModifiers(Modifier.PUBLIC);
//                TypeName listOfHoverboards = ParameterizedTypeName.get(hashMap, key, value);
//                build.addStatement("$T result = new $T<>()", listOfHoverboards, hashMap);
//                build.returns(listOfHoverboards);
//                for (TypeElement e : map) {
//
//
//                    String classname = e.getQualifiedName().toString();
//                    Serial annotation = e.getAnnotation(Serial.class);
//                    String path = annotation.path();
//                    build.addStatement("result.put($S,$S)", path, classname);
//
//                }
//
//                build.addStatement("return result");
//
//                MethodSpec printNameMethodSpec = build.build();



//                TypeSpec classTypeSpec = classBuilder.addMethod(printNameMethodSpec).build();

                TypeSpec classTypeSpec = classBuilder.build();

                try {
                    JavaFile javaFile = JavaFile.builder(packageName, classTypeSpec)
                            .addFileComment(" Do not modify, created by annotation processor !")
                            .build();
                    javaFile.writeTo(filer);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }



        return true;
    }
}
