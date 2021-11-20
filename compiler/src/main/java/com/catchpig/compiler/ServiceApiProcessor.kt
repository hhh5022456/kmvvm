package com.catchpig.compiler

import com.catchpig.annotation.ServiceApi
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.MirroredTypesException

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ServiceApiProcessor : BaseProcessor() {
    companion object {
        private const val TAG = "ServiceApiProcessor"
        private val CLASS_NAME_SERVICE_API_COMPILER =
            ClassName("com.catchpig.mvvm.apt.interfaces", "ServiceApiCompiler")
        private val CLASS_NAME_MAP = ClassName("kotlin.collections", "HashMap")
        private val CLASS_NAME_SERVICE_PARAM =
            ClassName("com.catchpig.mvvm.entity", "ServiceParam")
        private val CLASS_NAME_INTERCEPTOR =
            ClassName("okhttp3", "Interceptor")
    }


    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        var set = HashSet<String>()
        set.add(ServiceApi::class.java.canonicalName)
        return set
    }

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val elements = roundEnv.getElementsAnnotatedWith(ServiceApi::class.java)
        if (elements.isNotEmpty()) {
            val typeSpec = TypeSpec
                .classBuilder("ServiceApi_Compiler")
                .addModifiers(KModifier.FINAL, KModifier.PUBLIC)
                .addSuperinterface(CLASS_NAME_SERVICE_API_COMPILER)
                .primaryConstructor(initConstructor(elements))
                .addProperty(initServiceProperty())
                .addFunction(getServiceParamFun())
                .build()
            val fullPackageName = CLASS_NAME_SERVICE_API_COMPILER.packageName
            var fileSpecBuilder = FileSpec
                .builder(fullPackageName, typeSpec.name!!)
                .addType(typeSpec)
            fileSpecBuilder.build()
                .writeTo(filer)
        }
        return true
    }

    private fun initConstructor(serviceElements: Set<Element>): FunSpec {
        var constructorBuilder = FunSpec.constructorBuilder()
        serviceElements.map {
            it as TypeElement
        }.forEach {
            val className = it.asClassName().simpleName
            warning(TAG, "${className}被ServiceApi注解")
            val service = it.getAnnotation(ServiceApi::class.java)
            val factory = try {
                service.factory
            } catch (e: MirroredTypeException) {
                e.typeMirror
            }

            val inteceptors = try {
                service.interceptors.toList()
            } catch (e: MirroredTypesException) {
                e.typeMirrors
            }
            constructorBuilder =
                constructorBuilder.addStatement(
                    "val list = mutableListOf<%T>()",
                    CLASS_NAME_INTERCEPTOR
                )
            inteceptors.forEach { inteceptor ->
                constructorBuilder =
                    constructorBuilder.addStatement("list.add(%T())", inteceptor)
            }
            constructorBuilder = constructorBuilder.addStatement(
                "serviceMap.put(%S, %T(%S, %T.create(), %L, %L, list))",
                className,
                CLASS_NAME_SERVICE_PARAM,
                service.baseUrl,
                factory,
                service.connectTimeout,
                service.readTimeout,
            )
        }
        return constructorBuilder.build()
    }

    private fun initServiceProperty(): PropertySpec {
        var builder = PropertySpec
            .builder(
                "serviceMap",
                CLASS_NAME_MAP.parameterizedBy(
                    String::class.asClassName(),
                    CLASS_NAME_SERVICE_PARAM
                )
            )
            .addModifiers(KModifier.PRIVATE)
        builder.initializer("hashMapOf()")
        return builder.build()
    }

    private fun getServiceParamFun(): FunSpec {
        var funSpecBuilder = FunSpec
            .builder("getServiceParam")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .addParameter("className", String::class)
            .addStatement("return serviceMap.get(className)!!")
            .returns(CLASS_NAME_SERVICE_PARAM)
        return funSpecBuilder.build()
    }
}