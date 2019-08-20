package xyz.liut.releaseplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import com.android.utils.StringHelper
import org.gradle.api.Plugin
import org.gradle.api.Project
import xyz.liut.logcat.L
import xyz.liut.logcat.Logcat
import xyz.liut.logcat.handler.StdHandler
import xyz.liut.releaseplugin.task.JiaguTask

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Consumer

/**
 * plugin
 */
class ReleasePlugin implements Plugin<Project> {

    private Project rootProject

    private Project project

    private AppExtension android

    private ReleaseExtension releaseExtension

//    private static String DEFAULT_OUTPUT_DIR = './output/'
//    private static String DEFAULT_FILE_NAME_FORMAT = '$app-$b-$f_$vn.$vc'

    @Override
    void apply(Project project) {
        // 判断是否是 Android 项目
        def app = project.plugins.withType(AppPlugin)
        if (!app) {
            throw new IllegalStateException("本插件仅支持 Android 项目")
        }

        this.project = project
        this.rootProject = project.rootProject
        this.android = project.android
        project.extensions.create('outputApk', ReleaseExtension)
        this.releaseExtension = project.outputApk

        // log
        Logcat.handlers.add(new StdHandler(false, false))

        project.gradle.beforeProject {
            // init Extension
            initExtension()

            // task
            createTasks()
        }
    }

    /**
     * 扩展参数
     */
    def initExtension() {
        L.i "createExtension"

        def outputDir = project.rootDir.toString() + File.separator + releaseExtension.outputPath

        Utils.checkAndDir(outputDir)

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

        def taskName = "release$base"
        def dependsOn = "assemble$base"

        L.i "create task: $taskName"

        def releaseTask = project.task(taskName, dependsOn: dependsOn, group: 'deploy', description: "assemble and rename to $releaseExtension.outputPath") {
            doLast {
                L.e "release$buildTypeName$flavorName"

                List<File> files = new ArrayList<>()
//                outputs.files = files

                android.applicationVariants.forEach(new Consumer<ApplicationVariant>() {
                    @Override
                    void accept(ApplicationVariant applicationVariant) {
                        applicationVariant.outputs.forEach(new Consumer<BaseVariantOutput>() {
                            @Override
                            void accept(BaseVariantOutput baseVariantOutput) {

                                def outputFile = baseVariantOutput.outputFile
                                if (!outputFile.exists()) {
                                    return
                                }
                                L.d outputFile

                                boolean b = buildTypeName == "" || flavorName == "" || (buildTypeName.equalsIgnoreCase(applicationVariant.buildType.name) && flavorName.equalsIgnoreCase(applicationVariant.flavorName))

                                if (b) {
                                    // 根据模板生成文件名
                                    String finalFileName = releaseExtension.fileName
                                            .replace('$app', project.name)
                                            .replace('$b', applicationVariant.buildType.name)
                                            .replace('$f', applicationVariant.flavorName)
                                            .replace('$vn', applicationVariant.versionName)
                                            .replace('$vc', applicationVariant.versionCode.toString())

                                    if (applicationVariant.signingReady) {
                                        finalFileName = finalFileName + ".apk"
                                    } else {
                                        finalFileName = finalFileName + "-unsigned.apk"
                                    }

                                    Files.copy(
                                            outputFile.toPath(),
                                            new File(releaseExtension.outputPath + File.separator + finalFileName).absoluteFile.toPath(),
                                            StandardCopyOption.REPLACE_EXISTING)

                                    files.add(new File(releaseExtension.outputPath + File.separator + finalFileName))
                                }
                            }
                        })

                    }
                })

            }
        }

        def jiaguTaskName = "jiagu$base"

        project.task(jiaguTaskName, type: JiaguTask, dependsOn: releaseTask, group: 'deploy', description: "jiagu and rename to $releaseExtension.outputPath") {
//            doLast {
//
//                L.e jiaguTaskName
//
//            }
        }

    }

}

