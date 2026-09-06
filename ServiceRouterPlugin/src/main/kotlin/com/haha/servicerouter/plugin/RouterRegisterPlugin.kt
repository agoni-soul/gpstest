package com.haha.servicerouter.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 对齐 ARouter arouter-register：
 * 打包期扫描 RouteLoader / InterceptorLoader / IServiceInit，
 * ASM 注入 Router.loadRouterMap 与 ServiceLoaderInit.loadServiceMap。
 */
class RouterRegisterPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val androidComponents =
            project.extensions.findByType(AndroidComponentsExtension::class.java)
        if (androidComponents == null) {
            project.logger.warn("[DOFRouter] skip register plugin: AndroidComponentsExtension not found")
            return
        }
        androidComponents.onVariants { variant ->
            val taskName = "dofRouterRegister${variant.name.replaceFirstChar { it.uppercase() }}"
            val taskProvider =
                project.tasks.register(taskName, RouterRegisterTask::class.java) { task ->
                    task.group = "dofrouter"
                    task.description = "Inject DOFRouter auto-register for ${variant.name}"
                }
            variant.artifacts
                .forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    RouterRegisterTask::allJars,
                    RouterRegisterTask::allDirectories,
                    RouterRegisterTask::output
                )
        }
    }
}
