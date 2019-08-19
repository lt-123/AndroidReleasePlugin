package xyz.liut.releaseplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.dsl.BuildType
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import xyz.liut.logcat.L
import xyz.liut.logcat.Logcat
import xyz.liut.logcat.handler.StdHandler

import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.function.Consumer

/**
 * plugin
 */
class ReleasePlugin implements Plugin<Project> {

    private Project project

    private AppExtension android

    private ReleaseExtension extension

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
        this.android = project.android
        // create Extension
        project.extensions.create('outputApk', ReleaseExtension)
        this.extension = project.outputApk

        Logcat.handlers.add(new StdHandler(false, false))

        L.d "======================"

        createTasks()
        test()

        project.gradle.taskGraph.whenReady { taskGraph ->
            L.d "whenReadywhenReadywhenReadywhenReadywhenReadywhenReadywhenReadywhenReady"
        }
    }

    def createTasks() {
//        project.task

        L.d android.toString()

        Utils.printlnObject(android)
        Utils.printlnObject(extension)

        L.d "bbbbbbbbbbbb"
        android.buildTypes.forEach(new Consumer<BuildType>() {
            @Override
            void accept(BuildType buildType) {
                L.d buildType.toString()
            }
        })

//        L.i "ffffffffffffff"
//        android.productFlavors.forEach(new Consumer<ProductFlavor>() {
//            @Override
//            void accept(ProductFlavor productFlavor) {
//                L.i productFlavor.toString()
//
//            }
//        })
//
//        android.applicationVariants.forEach(new Consumer<ApplicationVariant>() {
//            @Override
//            void accept(ApplicationVariant applicationVariant) {
//
//                L.d applicationVariant.toString()
//
//            }
//        })
//
//        L.e "333333333333333333"
//        L.e android.flavorDimensionList.forEach(new Consumer<String>() {
//            @Override
//            void accept(String t) {
//                L.e t
//
//            }
//        })

    }

    def test(){
        // 添加打包 task
        project.task('release', dependsOn: 'assembleRelease', group: 'deploy', description: 'assemble All and rename to ./output') {
            doLast {
                final DomainObjectSet<ApplicationVariant> variants = project.android.applicationVariants

//                ReleaseExtension releaseExtension = project.outputApk


                String outputPath = project.outputApk.outputPath
//                if (outputPath == null) {
//                    outputPath = DEFAULT_OUTPUT_DIR
//                }
                String fileName = project.outputApk.fileName
//                if (fileName == null) {
//                    fileName = DEFAULT_FILE_NAME_FORMAT
//                }

                Utils.checkAndDir(outputPath)

                L.d "outputPath = $outputPath"
                L.d "fileName = ${fileName}"


                // 遍历所有变种
                variants.forEach(new Consumer<ApplicationVariant>() {
                    @Override
                    void accept(ApplicationVariant variant) {
                        // 遍历所有输出
                        variant.getOutputs().forEach(new Consumer<BaseVariantOutput>() {
                            @Override
                            void accept(BaseVariantOutput output) {

                                def outputFile = output.outputFile

                                if (outputFile.exists()) {
                                    L.d outputFile

                                    // 根据模板生成文件名
                                    String finalFileName = fileName
                                            .replace('$app', project.name)
                                            .replace('$b', variant.buildType.name)
                                            .replace('$f', variant.flavorName)
                                            .replace('$vn', variant.versionName)
                                            .replace('$vc', variant.versionCode.toString()) + ".apk"

                                    Files.copy(
                                            outputFile.toPath(),
                                            new File(outputPath + finalFileName).absoluteFile.toPath(),
                                            StandardCopyOption.REPLACE_EXISTING)

//                                list.add(new File("./output/${fileName}.apk"))

                                }
                            }
                        })


                    }
                })

                String uname = Utils.uname()
                switch (uname) {
                    case "Darwin":  // mac
//                        Utils.execCommand("open $outputPath")
                        break
                }
            }
        }

    }


}

