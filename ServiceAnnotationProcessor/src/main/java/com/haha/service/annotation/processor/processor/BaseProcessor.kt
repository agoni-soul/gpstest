package com.haha.service.annotation.processor.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.UUID
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 *
 * @author : haha
 * @date   : 2024-09-14
 * @desc   : BaseProcessor抽象类
 * @version: 1.0
 *
 */
abstract class BaseProcessor: AbstractProcessor() {
    protected val TAG: String = javaClass.simpleName

    companion object {

        fun getClassName(typeMirror: TypeMirror?): String {
            return typeMirror?.toString() ?: ""
        }
    }

    protected var mFiler: Filer? = null
    protected var mElementUtils: Elements? = null
    protected var mTypeUtils: Types? = null
    protected var mMessager: Messager? = null
    protected var mOptions: Map<String, String>? = null

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        mFiler = processingEnv?.filer
        mElementUtils = processingEnv?.elementUtils
        mTypeUtils = processingEnv?.typeUtils
        mMessager = processingEnv?.messager
        mOptions = processingEnv?.options
    }

    /**
     * 从字符串获取TypeElement对象
     */
    fun typeElement(className: String): TypeElement? {
        return mElementUtils?.getTypeElement(className)
    }

    /**
     * 从字符串获取TypeMirror对象
     */
    fun typeMirror(className: String): TypeMirror? {
        return typeElement(className)?.asType()
    }


    /**
     * 从字符串获取ClassName对象
     */
    fun className(className: String): ClassName? {
        val typeError = typeElement(className) ?: return null
        return ClassName.get(typeError)
    }

    /**
     * 从字符串获取TypeName对象，包含Class的泛型信息
     */
    fun typeName(className: String): TypeName? {
        val typeMirror = typeMirror(className) ?: return null
        return TypeName.get(typeMirror)
    }

    fun isSubType(typeMirror: TypeMirror?, className: String?): Boolean {
        typeMirror ?: return false
        className ?: return false
        return mTypeUtils?.isSubtype(typeMirror, typeMirror(className)) ?: false
    }

    fun isSubType(element: Element?, className: String?): Boolean {
        element ?: return false
        className ?: return false
        return isSubType(element.asType(), className)
    }

    fun isSubType(element: Element?, typeMirror: TypeMirror?): Boolean {
        element ?: return false
        typeMirror ?: return false
        return mTypeUtils?.isSubtype(element.asType(), typeMirror) ?: false
    }

    /**
     * 非抽象类
     */
    fun isConcreteType(element: Element?): Boolean {
        return element is TypeElement &&
                !element.getModifiers().contains(Modifier.ABSTRACT)
    }

    /**
     * 非抽象子类
     */
    fun isConcreteSubType(element: Element?, className: String?): Boolean {
        return isConcreteType(element) && isSubType(element, className)
    }

    /**
     * 非抽象子类
     */
    fun isConcreteSubType(element: Element?, typeMirror: TypeMirror?): Boolean {
        return isConcreteType(element) && isSubType(element, typeMirror)
    }

    fun isInterceptor(element: Element?): Boolean {
        return isConcreteSubType(element, ConstantUtils.URI_INTERCEPTOR_CLASS)
    }

    fun randomHash(): String {
        return hash(UUID.randomUUID().toString())
    }

    fun hash(str: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            md.update(str.toByteArray())
            return BigInteger(1, md.digest()).toString(16)
        } catch (e: NoSuchAlgorithmException) {
            return Integer.toHexString(str.hashCode())
        }
    }

    /**
     * 创建Handler。格式：`"com.demo.TestActivity"` 或 `new TestHandler()`
     */
    fun buildHandler(isActivity: Boolean, cls: TypeElement): CodeBlock {
        val b = CodeBlock.builder()
        if (isActivity) {
            b.add("\$S", cls.qualifiedName.toString())
        } else {
            b.add("new \$T()", cls)
        }
        return b.build()
    }

    /**
     * 创建Interceptors。格式：`, new Interceptor1(), new Interceptor2()`
     */
    fun buildInterceptors(interceptors: List<TypeMirror?>?): CodeBlock {
        val b = CodeBlock.builder()
        if (!interceptors.isNullOrEmpty()) {
            for (type in interceptors) {
                if (type is TypeElement) {
                    val e = type
                    if (isInterceptor(e)) {
                        b.add(", new \$T()", ClassName.bestGuess(e.asType().toString()))
                    }
                }
            }
        }
        return b.build()
    }


    /**
     * 辅助工具类，用于生成ServiceInitClass，格式如下：
     * <pre>
     * package com.midea.base.serviceloader.api.generated.service;
     *
     * import com.midea.base.serviceloader.api.service.ServiceLoader;
     *
     * public class &lt;ClassName&gt; {
     * public static void init() {
     * ServiceLoader.put(com.xxx.interface1.class, "key1", com.xxx.implementsA.class, false);
     * ServiceLoader.put(com.xxx.interface2.class, "key2", com.xxx.implementsB.class, false);
     * }
     * }
    </pre> *
     */
    inner class ServiceInitClassBuilder(private val className: String) {
        private val builder: CodeBlock.Builder = CodeBlock.builder()
        private val serviceLoaderClass: ClassName? = className(ConstantUtils.SERVICE_LOADER_CLASS)

        fun put(
            interfaceName: String?,
            key: String?,
            implementName: String?,
            singleton: Boolean
        ): ServiceInitClassBuilder {
            serviceLoaderClass ?: return this
            interfaceName ?: return this
            implementName ?: return this
            builder.addStatement(
                "\$T.put(\$T.class, \$S, \$T.class, \$L)",
                serviceLoaderClass,
                className(interfaceName),
                key,
                className(implementName),
                singleton
            )
            return this
        }

        fun putDirectly(
            interfaceName: String?,
            key: String?,
            implementName: String?,
            singleton: Boolean
        ): ServiceInitClassBuilder {
            serviceLoaderClass ?: return this
            interfaceName ?: return this
            implementName ?: return this
            // implementName是注解生成的类，直接用$L拼接原始字符串
            builder.addStatement(
                "\$T.put(\$T.class, \$S, \$L.class, \$L)",
                serviceLoaderClass,
                className(interfaceName),
                key,
                implementName,
                singleton
            )
            return this
        }

        fun build() {
            val methodSpec = MethodSpec.methodBuilder(ConstantUtils.INIT_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addCode(builder.build())
                .build()

            val typeSpec = TypeSpec.classBuilder(this.className)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(methodSpec)
                .build()
            try {
                JavaFile.builder(ConstantUtils.GEN_PKG_SERVICE, typeSpec)
                    .build()
                    .writeTo(mFiler)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }
}