package xyz.liut.releaseplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.utils.StringHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import xyz.liut.logcat.L
import xyz.liut.logcat.Logcat
import xyz.liut.logcat.handler.StdHandler
import xyz.liut.releaseplugin.task.JiaguTask
import xyz.liut.releaseplugin.task.ReleaseTask

import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * plugin
 */
class ReleasePlugin implements Plugin<Project> {

    private Project rootProject

    private Project project

    private AppExtension android

    private ReleaseExtension releaseExtension

    private Map<String, String> localPropertiesMap

    // 是否是加固任务
    private boolean isJiaguTask

    @Override
    void apply(Project project) {
        // log
        if (Logcat.handlers.size() == 0) {
            // 避免 deamon 重复添加
            Logcat.handlers.add(new StdHandler(false, false))
        }
        L.i "=====${project.rootProject.name}====="

        // 判断是否是 Android 项目
        def app = project.plugins.withType(AppPlugin)
        if (!app) {
            throw new IllegalStateException("本插件仅支持 Android 项目")
        }

        isJiaguTask = false

        this.project = project
        this.rootProject = project.rootProject
        this.android = project.android
        project.extensions.create('outputApk', ReleaseExtension)
        this.releaseExtension = project.outputApk

        rootProject.gradle.projectsEvaluated {
            L.i "projectsEvaluated"

            // local.properties
            initLocalProperties()

            // init Extension
            initExtension()

            // task
            createTasks()
        }
    }

    /**
     * 读取 local.properties 到 localPropertiesMap
     */
    def initLocalProperties() {
        localPropertiesMap = new HashMap<>()

        String localProperties = rootProject.projectDir.toString() + "/local.properties"
        Properties properties = new Properties()
        properties.load(new FileInputStream(new File(localProperties)))
        properties.forEach(new BiConsumer<String, String>() {
            @Override
            void accept(String key, String value) {
                localPropertiesMap.put(key, value)
                L.d "localProperties $key = $value"
            }
        })


    }


    /**
     * 扩展参数
     */
    def initExtension() {
        L.i "createExtension"

        def outputDir = project.rootDir.toString() + File.separator + releaseExtension.outputPath

        Utils.checkDir(outputDir)

        releaseExtension.outputPath = outputDir
        L.d releaseExtension.outputPath
    }

    /**
     * 创建 tasks
     */
    def createTasks() {
        // assemble All
        createTask(null, null)

        android.buildTypes.forEach(new Consumer<BuildType>() {
            @Override
            void accept(BuildType buildType) {
                def buildTypeName = buildType.name

                // assemble buildType
                createTask(buildTypeName, null)

                android.productFlavors.forEach(new Consumer<ProductFlavor>() {
                    @Override
                    void accept(ProductFlavor productFlavor) {
                        def flavorName = productFlavor.name

                        createTask(buildTypeName, flavorName)
                    }
                })
            }
        })
    }

    /**
     * 创建 task
     *
     * @param buildTypeName -
     * @param flavorName -
     */
    def createTask(String buildTypeName, String flavorName) {
        if (buildTypeName != null)
            buildTypeName = StringHelper.capitalize(buildTypeName)
        else
            buildTypeName = ""
        if (flavorName != null)
            flavorName = StringHelper.capitalize(flavorName)
        else
            flavorName = ""

        def base = "$flavorName$buildTypeName"

        def dependsOn = "assemble$base"
        def taskName = "release$base"

        def jiaguTaskName = "jiagu$base"

        L.i "create task: $taskName"

        // 所有变种
        Set<ApplicationVariant> variants = android.applicationVariants.findAll { applicationVariant ->
            return buildTypeName == "" || flavorName == "" || (buildTypeName.equalsIgnoreCase(applicationVariant.buildType.name) && flavorName.equalsIgnoreCase(applicationVariant.flavorName))
        }

        // 生成 release task
        def releaseTask = project.task(taskName, type: ReleaseTask, dependsOn: dependsOn, group: 'deploy', description: "assemble and rename to $releaseExtension.outputPath") {
            inputVariants = variants
            fileNameTemplate = releaseExtension.fileNameTemplate
            outputDir = releaseExtension.outputPath
            outputFiles = new HashSet<>()

            doLast {
                if (!isJiaguTask && releaseExtension.openDir && success) {
                    Utils.openPath(releaseExtension.outputPath)
                }
            }

        }

        // 生成加固 task
        project.task(jiaguTaskName, type: JiaguTask, dependsOn: releaseTask, group: 'jiagu', description: "jiagu and rename to $releaseExtension.outputPath") {
            jiaguProgram = JIAGU_360
            jiaguProgramDir = localPropertiesMap.get("jiaguPath")
            apkFiles = releaseTask.outputFiles
            outputDir = releaseExtension.jiaguOutputPath

            doFirst {
                isJiaguTask = true
            }

            doLast {
                if (releaseExtension.openDir && success) {
                    Utils.openPath(releaseExtension.jiaguOutputPath)
                }
            }

        }

    }

}

