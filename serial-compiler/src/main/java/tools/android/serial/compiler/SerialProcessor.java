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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import tools.android.serial.annotation.Serial;

@SupportedAnnotationTypes("tools.android.serial.annotation.Serial")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class SerialProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private StringBuilder comments = new StringBuilder();
    private ClassName STRING = ClassName.get("java.lang", "String");

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
                    if (enclosedElement.getKind() == ElementKind.FIELD ) {
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
                        String setStatement = createSetStatement(fieldType, fn.orig);
                        setMethodBuilder.addStatement("put(\"" + fn.orig + "\", " + setStatement + ")");


                        MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder(fn.getMethodCase)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(TypeName.get(fieldType));
                        String getStatement = createGetStatement(fieldType, fn.orig);
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

    private String createSetStatement(TypeMirror type, String value) {
        TypeName typeName = TypeName.get(type);

        String statement = value + ".toString()";
        if (typeName.isPrimitive()) {
            if (typeName.equals(TypeName.BOOLEAN)) {
                statement = value + " ? \"t\" : \"f\"";
            } else if (typeName.equals(TypeName.BYTE)) {
                statement = "Byte.toString(" + value + ")";
            } else if (typeName.equals(TypeName.SHORT)) {
                statement = "Short.toString(" + value + ")";
            } else if (typeName.equals(TypeName.INT)) {
                statement = "Integer.toString(" + value + ")";
            } else if (typeName.equals(TypeName.LONG)) {
                statement = "Long.toString(" + value + ")";
            } else if (typeName.equals(TypeName.CHAR)) {
                statement = "String.valueOf(" + value + ")";
            } else if (typeName.equals(TypeName.FLOAT)) {
                statement = "Float.toString(" + value + ")";
            } else if (typeName.equals(TypeName.DOUBLE)) {
                statement = "Double.toString(" + value + ")";
            } else {
                comments.append(">isPrimitive>" + typeName);
            }
        } else if (typeName.isBoxedPrimitive()) {
            if (typeName.equals(TypeName.BOOLEAN.box())) {
                statement = value + " ? \"t\" : \"f\"";
            } else if (typeName.equals(TypeName.BYTE.box())) {
                statement = "Byte.toString(" + value + ")";
            } else if (typeName.equals(TypeName.SHORT.box())) {
                statement = "Short.toString(" + value + ")";
            } else if (typeName.equals(TypeName.INT.box())) {
                statement = "Integer.toString(" + value + ")";
            } else if (typeName.equals(TypeName.LONG.box())) {
                statement = "Long.toString(" + value + ")";
            } else if (typeName.equals(TypeName.CHAR.box())) {
                statement = "String.valueOf(" + value + ")";
            } else if (typeName.equals(TypeName.FLOAT.box())) {
                statement = "Float.toString(" + value + ")";
            } else if (typeName.equals(TypeName.DOUBLE.box())) {
                statement = "Double.toString(" + value + ")";
            } else {
                comments.append(">isBoxedPrimitive>" + typeName);
            }
        } else if (TypeName.get(String.class).equals(typeName)) {
        } else {
            comments.append(">isUnknown>" + typeName);
        }
        return statement;
    }

    private String createGetStatement(TypeMirror type, String value) {
        TypeName typeName = TypeName.get(type);

        String statement = "get(\"" + value + "\")";
        if (typeName.isPrimitive()) {
            if (typeName.equals(TypeName.BOOLEAN)) {
                statement = "\"t\".equals(get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.BYTE)) {
                statement = "Byte.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.SHORT)) {
                statement = "Short.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.INT)) {
                statement = "Integer.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.LONG)) {
                statement = "Long.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.CHAR)) {
                statement = "get(\"" + value + "\") == null ? null : get(\"" + value + "\").charAt(0)";
            } else if (typeName.equals(TypeName.FLOAT)) {
                statement = "Float.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.DOUBLE)) {
                statement = "Double.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else {
                comments.append("<isPrimitive<" + typeName);
            }
        } else if (typeName.isBoxedPrimitive()) {
            if (typeName.equals(TypeName.BOOLEAN.box())) {
                statement = "\"t\".equals(get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.BYTE.box())) {
                statement = "Byte.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.SHORT.box())) {
                statement = "Short.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.INT.box())) {
                statement = "Integer.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.LONG.box())) {
                statement = "Long.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.CHAR.box())) {
                statement = "get(\"" + value + "\") == null ? null : get(\"" + value + "\").charAt(0)";
            } else if (typeName.equals(TypeName.FLOAT.box())) {
                statement = "Float.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else if (typeName.equals(TypeName.DOUBLE.box())) {
                statement = "Double.valueOf(get(\"" + value + "\") == null ? \"0\" : get(\"" + value + "\"))";
            } else {
                comments.append("<isBoxedPrimitive<" + typeName);
            }
        } else if (TypeName.get(String.class).equals(typeName)) {
        } else {
            comments.append("<isUnknown<" + typeName);
        }

        return statement;
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
