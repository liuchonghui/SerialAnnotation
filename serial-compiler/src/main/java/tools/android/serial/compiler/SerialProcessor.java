package tools.android.serial.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

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
            StringBuilder comments = new StringBuilder();
            ElementKind elementKind = element.getKind();
            if (elementKind == ElementKind.CLASS) {
                String packageName;
                TypeElement element4Class = (TypeElement) element;
                String clazzName = element4Class.getSimpleName().toString();
                String CLASSNAME = clazzName + "Serial";
                packageName = elementUtils.getPackageOf(element4Class).toString();

                ClassName hashMap = ClassName.get("java.util", "HashMap");
                ClassName key = ClassName.get("java.lang", "String");
                ClassName value = ClassName.get("java.lang", "String");
                TypeName listOfHoverboards = ParameterizedTypeName.get(hashMap, key, value);

                TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASSNAME)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(ClassName.bestGuess("java.io.Serializable"))
                        .superclass(listOfHoverboards);

                FieldSpec.Builder serialVersionUID = FieldSpec.builder(TypeName.LONG, "serialVersionUID")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("1L");
                classBuilder.addField(serialVersionUID.build());
                TypeSpec classTypeSpec = classBuilder.build();

                for (Element enclosedElement : element.getEnclosedElements()) {
                    if (enclosedElement.getKind() == ElementKind.FIELD) {
                        TypeMirror fieldType = enclosedElement.asType();
                        FieldName fn = new FieldName();
                        fn.orig = enclosedElement.getSimpleName().toString();
                        fn.underLineCase = toUnderlineCase(fn.orig);
                        fn.camelCase = toCamelCase(fn.underLineCase);
                        fn.bigCamelCase = toFirstUpperCase(fn.camelCase);
                        fn.setMethodCase = "set" + fn.bigCamelCase;
                        fn.getMethodCase = "get" + fn.bigCamelCase;

                        FieldSpec.Builder fieldBuilder = FieldSpec.builder(TypeName.get(fieldType), fn.orig)
                                .addModifiers(Modifier.PRIVATE);

                        MethodSpec.Builder setMethodBuilder = MethodSpec.methodBuilder(fn.setMethodCase)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(void.class)
                                .addParameter(TypeName.get(fieldType), fn.orig);
                        String setStatement = createSetStatement(fieldType.getKind(), fn.orig);
                        setMethodBuilder.addStatement("put(\"" + fn.orig + "\", " + setStatement + ")");


                        MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder(fn.getMethodCase)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.get(fieldType));
                        String getStatement = createGetStatement(fieldType.getKind(), fn.orig);
                        getMethodBuilder.addStatement("return " + getStatement);


                        classTypeSpec = classBuilder
                                .addField(fieldBuilder.build())
                                .addMethod(setMethodBuilder.build())
                                .addMethod(getMethodBuilder.build())
                                .build();
                    }
                }
                try {
                    JavaFile javaFile = JavaFile.builder(packageName, classTypeSpec)
                            .addFileComment(comments.toString())
                            .build();
                    javaFile.writeTo(filer);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
        return true;
    }

    private String createSetStatement(TypeKind kind, String value) {
        switch(kind) {
            case BOOLEAN:
                return value + " ? \"t\" : \"f\"";
            case BYTE:
                return "Byte.toString(" + value + ")";
            case SHORT:
                return "Short.toString(" + value + ")";
            case INT:
                return "Integer.toString(" + value + ")";
            case LONG:
                return "Long.toString(" + value + ")";
            case CHAR:
                return "Char.toString(" + value + ")";
            case FLOAT:
                return "Float.toString(" + value + ")";
            case DOUBLE:
                return "Double.toString(" + value + ")";
            default:
                return value + ".toString()";
        }
    }

    private String createGetStatement(TypeKind kind, String value) {
        switch(kind) {
            case BOOLEAN:
                return "\"t\".equals(get(\"" + value + "\"))";
            case BYTE:
                return "Byte.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            case SHORT:
                return "Short.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            case INT:
                return "Integer.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            case LONG:
                return "Long.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            case CHAR:
                return "Char.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            case FLOAT:
                return "Float.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            case DOUBLE:
                return "Double.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            default:
                return "get(\"" + value + "\")";
        }
    }

    public static String toFirstUpperCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (input.length() == 1) {
            return input.toUpperCase();
        } else {
            return input.substring(0, 1).toUpperCase() + input.substring(1);
        }
    }

    public static String toUnderlineCase(String input) {
        StringBuilder result = new StringBuilder();
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (input != null && input.length() > 0) {
            result.append(input.substring(0, 1).toUpperCase());
            for (int i = 1; i < input.length(); i++) {
                String s = input.substring(i, i + 1);
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append("_");
                }
                result.append(s);
            }
        }
        return result.toString().replaceAll("__", "_").toLowerCase();
    }

    public static String toCamelCase(String input) {
        StringBuilder result = new StringBuilder();
        if (input == null || input.isEmpty()) {
            return "";
        } else if (!input.contains("_")) {
            return input.substring(0, 1).toLowerCase() + input.substring(1);
        }
        String camels[] = input.split("_");
        for (String camel :  camels) {
            if (camel.isEmpty()) {
                continue;
            }
            if (result.length() == 0) {
                result.append(camel.toLowerCase());
            } else {
                result.append(camel.substring(0, 1).toUpperCase());
                result.append(camel.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    private class FieldName {
        String orig;
        String underLineCase;
        String camelCase;
        String bigCamelCase;
        String setMethodCase;
        String getMethodCase;
    }
}
