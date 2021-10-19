package com.tompee.aprocessor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.adev.whatHappened.*
import org.jetbrains.annotations.Nullable
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.lang.model.util.AbstractTypeVisitor6
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Record::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val annotated = roundEnv.getElementsAnnotatedWith(Record::class.java)
        annotated.forEach { element ->
            val classElement = element as? TypeElement
            if (classElement == null) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Only classes can be annotated"
                )
                return true
            }
            processAnnotation(classElement, annotated)
        }
        return false
    }

    /**
     * Generates:
     *
     * steps.add(
     *     InputStep.interact(
     *         viewModelType = FrameworksViewModel::class,
     *         methodName = "onItemClick",
     *         arguments = listOf(
     *             InputStep.Argument(
     *                 name = "password",
     *                 value = password,
     *                 clazz = String::class,
     *                 isHidden = true
     *             ),
     *             InputStep.Argument(
     *                 name = "item",
     *                 value = item,
     *                 clazz = Basket.Item::class,
     *                 isHidden = true
     *             ),
     *         )
     *     )
     * )
     */
    private fun processAnnotation(element: TypeElement, annotated: Set<Element>) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val fileName = recorderName(className)
        val fileBuilder = FileSpec.builder(pack, fileName)
            .addImport(HappenedEvent::class.java.`package`.name, HappenedEvent::class.simpleName!!)
            .indent("    ")

        val recordingType = element.asType().asTypeName().anyGenerics()
        val classBuilder = TypeSpec.classBuilder(fileName)

        val superClassRawName = element.superclass.asTypeName().raw()
        val isSuperclassRecording = annotated
            .any { annotatedElement ->
                annotatedElement.asType().asTypeName().raw() == superClassRawName
            }
        classBuilder.addKdoc(
            """
            annotated=${annotated}
            superclass=${element.superclass}
            isSuperclassRecording=${isSuperclassRecording}
            annotationMirrors=${element.superclass.annotationMirrors}
        """.trimIndent()
        )

        val annotation = element.annotationMirrors.find {
            it.annotationType.toString() == Record::class.qualifiedName
        }!!

        val recordInterfaceTypes = annotation.elementValues
            .values.firstOrNull()
            ?.value as? List<AnnotationValue>

        val recordInterfaceTypeNames = recordInterfaceTypes
            ?.map { interfaze -> interfaze.value.toString() }
            ?: emptyList()

        val recordSuperinterfaces = element.interfaces.mapNotNull { mirror ->
            if (mirror.toString() in recordInterfaceTypeNames) {
                mirror.accept(FindElementVisitor(), null)
            } else {
                null
            }
        }

        val superInterfacesDeclarations = recordSuperinterfaces.flatMap { interfaze ->
            val interfaceSelfMethods = interfaze.enclosedElements.mapNotNull { enclosed ->
                if (enclosed is ExecutableElement &&
                    isNoReturnPublicMethod(enclosed) &&
                    isSelfMethod(enclosed)
                ) {
                    format(enclosed)
                } else {
                    null
                }
            }
            interfaceSelfMethods
        }.toSet()

        classBuilder
            .addModifiers(KModifier.OPEN)
            .apply {
                if (isSuperclassRecording) {
                    classBuilder.superclass(
                        ClassName.bestGuess(
                            recorderName(superClassRawName.toString())
                        )
                    )
                    classBuilder.superclassConstructorParameters.add(CodeBlock.of("recording"))
                    classBuilder.superclassConstructorParameters.add(CodeBlock.of("happenedEventRecorder"))
                }
                recordSuperinterfaces.forEach { interfaze ->
                    addSuperinterface(interfaze.asClassName())
                }
            }
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("recording", recordingType)
                    .addParameter(
                        "happenedEventRecorder",
                        HappenedEventRecorder::class.asTypeName()
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("recording", recordingType, KModifier.PUBLIC)
                    .apply {
                        if (isSuperclassRecording) {
                            addModifiers(KModifier.OVERRIDE)
                        } else {
                            addModifiers(KModifier.OPEN)
                        }
                    }
                    .initializer("recording")
                    .build()
            )
            .addProperty(
                PropertySpec
                    .builder(
                        "happenedEventRecorder",
                        HappenedEventRecorder::class.asTypeName(),
                        KModifier.PRIVATE
                    )
                    .initializer("happenedEventRecorder")
                    .build()
            )

        for (enclosed in element.enclosedElements) {
            if (enclosed !is ExecutableElement || !isNoReturnPublicMethod(enclosed)) continue
            val isRecordingInInterface = format(enclosed) in superInterfacesDeclarations
            if (!isSelfMethod(enclosed) && !isRecordingInInterface) continue

            val functionBuilder = FunSpec.builder(enclosed.simpleName.toString())
            if (isRecordingInInterface) {
                functionBuilder.addModifiers(KModifier.OVERRIDE)
            }
            enclosed.parameters.forEach { parameter ->
                functionBuilder.addParameter(
                    parameter.simpleName.toString(),
                    parameter.asType().asTypeName().javaToKotlinType()
                        .copy(
                            nullable = parameter.getAnnotation(Nullable::class.java) != null
                        )
                )
            }

            val formattedArgs = enclosed.parameters.joinToString(separator = ",\n") { parameter ->
                """
            HappenedEvent.Argument(
                name = "${parameter.simpleName}",
                value = ${parameter.simpleName},
                clazz = ${parameter.asType().asTypeName().javaToKotlinType()}::class,
                isHidden = ${parameter.getAnnotation(Hide::class.java) != null}
            )""".trimStart { it == '\n' }
            }
            functionBuilder.addCode(
                """
happenedEventRecorder.append(
    HappenedEvent.viewInteract(
        viewModelType = recording::class,
        methodName = "${enclosed.simpleName}",
        arguments = listOf(
$formattedArgs
        )
    )
)
""".trimStart()
            )

            val passParameters = enclosed.parameters.joinToString { it.simpleName }
            functionBuilder.addStatement("recording.${enclosed.simpleName}(${passParameters})")
            classBuilder.addFunction(functionBuilder.build())
        }

        fileBuilder.addType(classBuilder.build())

        fileBuilder.addFunction(
            FunSpec.builder("recorder")
                .receiver(
                    RecordingHolder::class.asTypeName()
                        .parameterizedBy(recordingType)
                )
                .returns(ClassName.bestGuess("$pack.$fileName"))
                .addCode("return $fileName(this.recording, this.happenedEventRecorder)")
                .build()
        )

        val file = fileBuilder.build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!
        file.writeTo(File(kaptKotlinGeneratedDir))
    }

    private fun recorderName(recordingName: String): String {
        return "${recordingName}Recorder"
    }

    private fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
        (rawType.javaToKotlinType() as ClassName).parameterizedBy(
            *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
        )
    } else {
        val className = JavaToKotlinClassMap.INSTANCE
            .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
        if (className == null) this
        else ClassName.bestGuess(className)
    }

    private fun TypeName.anyGenerics(): TypeName = if (this is ParameterizedTypeName) {
        rawType.parameterizedBy(
            typeArguments.map { STAR }
        )
    } else {
        this
    }

    private fun TypeName.raw(): TypeName {
        return if (this is ParameterizedTypeName)
            this.rawType
        else
            this
    }

    private fun isSelfMethod(element: ExecutableElement): Boolean {
        return element.getAnnotation(Override::class.java) == null
    }

    private fun isNoReturnPublicMethod(element: ExecutableElement): Boolean {
        return element.kind == ElementKind.METHOD
                && Modifier.PUBLIC in element.modifiers
                && element.returnType.asTypeName().toString() == "kotlin.Unit"
    }

    private fun format(element: ExecutableElement): String {
        return "${element.simpleName}(${element.parameters.joinToString { it.asType().toString() }})"
    }

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}

private class FindElementVisitor : AbstractTypeVisitor6<TypeElement?, Any?>() {
    override fun visitDeclared(t: DeclaredType?, p: Any?): TypeElement? {
        return t?.asElement() as? TypeElement
    }

    override fun visitPrimitive(t: PrimitiveType?, p: Any?): TypeElement? {
        return null
    }

    override fun visitNull(t: NullType?, p: Any?): TypeElement? {
        return null
    }

    override fun visitArray(t: ArrayType?, p: Any?): TypeElement? {
        return null
    }

    override fun visitError(t: ErrorType?, p: Any?): TypeElement? {
        return null
    }

    override fun visitTypeVariable(t: TypeVariable?, p: Any?): TypeElement? {
        return null
    }

    override fun visitWildcard(t: WildcardType?, p: Any?): TypeElement? {
        return null
    }

    override fun visitExecutable(t: ExecutableType?, p: Any?): TypeElement? {
        return null
    }

    override fun visitNoType(t: NoType?, p: Any?): TypeElement? {
        return null
    }
}