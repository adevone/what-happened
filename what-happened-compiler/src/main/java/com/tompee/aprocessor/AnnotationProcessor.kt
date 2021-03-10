package com.tompee.aprocessor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import io.adev.whatHappened.Hide
import io.adev.whatHappened.HappenedEvent
import io.adev.whatHappened.Record
import io.adev.whatHappened.RecordingHolder
import io.adev.whatHappened.HappenedEventRecorder
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

@AutoService(Processor::class)
class AnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Record::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        roundEnv.getElementsAnnotatedWith(Record::class.java)
            .forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only classes can be annotated")
                    return true
                }
                processAnnotation(it)
            }
        return false
    }

//    steps.add(
//        InputStep.interact(
//            viewModelType = FrameworksViewModel::class,
//            methodName = "onItemClick",
//            arguments = listOf(
//                InputStep.Argument(
//                    name = "password",
//                    value = password,
//                    clazz = String::class,
//                    isHidden = true
//                ),
//                InputStep.Argument(
//                    name = "item",
//                    value = item,
//                    clazz = Basket.Item::class,
//                    isHidden = true
//                ),
//            )
//        )
//    )
    private fun processAnnotation(element: Element) {
        val className = element.simpleName.toString()
        val pack = processingEnv.elementUtils.getPackageOf(element).toString()

        val fileName = "${className}Recorder"
        val fileBuilder = FileSpec.builder(pack, fileName)
            .addImport(HappenedEvent::class.java.`package`.name, HappenedEvent::class.simpleName!!)
            .indent("    ")

        val classBuilder = TypeSpec.classBuilder(fileName)

        classBuilder.primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("recording", element.asType().asTypeName())
                .addParameter("happenedEventRecorder", HappenedEventRecorder::class.asTypeName())
                .build()
        )

        classBuilder.addProperty(
            PropertySpec.builder("recording", element.asType().asTypeName(), KModifier.PRIVATE)
                .initializer("recording")
                .build()
        )

        classBuilder.addProperty(
            PropertySpec.builder("happenedEventRecorder", HappenedEventRecorder::class.asTypeName(), KModifier.PRIVATE)
                .initializer("happenedEventRecorder")
                .build()
        )

        for (enclosed in element.enclosedElements) {
            if (enclosed.kind == ElementKind.METHOD &&
                enclosed is ExecutableElement &&
                enclosed.modifiers.any { modifier ->
                    modifier == Modifier.PUBLIC
                } &&
                enclosed.returnType.asTypeName().toString() == "kotlin.Unit"
            ) {
                val functionBuilder = FunSpec.builder(enclosed.simpleName.toString())
                enclosed.parameters.forEach { parameter ->
                    functionBuilder.addParameter(
                        parameter.simpleName.toString(),
                        parameter.asType().asTypeName().javaToKotlinType()
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
                functionBuilder.addCode("""
happenedEventRecorder.append(
    HappenedEvent.viewInteract(
        viewModelType = ${element.asType().asTypeName()}::class,
        methodName = "${enclosed.simpleName}",
        arguments = listOf(
$formattedArgs
        )
    )
)
""".trimStart())

                val passParameters = enclosed.parameters.joinToString { it.simpleName }
                functionBuilder.addStatement("recording.${enclosed.simpleName}(${passParameters})")
                classBuilder.addFunction(functionBuilder.build())
            }
        }

        fileBuilder.addType(classBuilder.build())

        fileBuilder.addFunction(
            FunSpec.builder("recorder")
                .receiver(
                    RecordingHolder::class.asTypeName()
                        .parameterizedBy(element.asType().asTypeName())
                )
                .returns(ClassName.bestGuess("$pack.$fileName"))
                .addCode("return $fileName(this.recording, this.happenedEventRecorder)")
                .build()
        )

        val file = fileBuilder.build()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.writeTo(File(kaptKotlinGeneratedDir))
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

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}