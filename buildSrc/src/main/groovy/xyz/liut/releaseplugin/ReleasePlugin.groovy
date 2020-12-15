package xyz.liut.releaseplugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.dsl.ProductFlavor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import xyz.liut.logcat.L
import xyz.liut.logcat.Logcat
import xyz.liut.logcat.handler.StdHandler
import xyz.liut.releaseplugin.bean.VariantDataBean
import xyz.liut.releaseplugin.task.BaseTask
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

    // 是否是加固任务 并执行成功
    private boolean isJiaguTaskSuccess
    // 是否是 release 并执行成功
    private boolean isReleaseTaskSuccess

    @Override
    void apply(Project project) {
        // log
        if (Logcat.handlers.size() == 0) {
            // 避免 deamon 重复添加
            Logcat.handlers.add(new StdHandler(false, false))
        }
        L.i "rootProject.name=${project.rootProject.name}"

        this.project = project
        this.rootProject = project.rootProject

        project.extensions.create('outputApk', ReleaseExtension)
        releaseExtension = project.outputApk
        releaseExtension.setWorkDir(project.projectDir.toString())

        rootProject.gradle.projectsEvaluated {
            L.i "projectsEvaluated..."

            // 判断是否是 Android 项目
            def app = project.plugins.withType(AppPlugin)
            if (!app) {
                throw new IllegalStateException("本插件仅支持 Android 项目")
            }

            isJiaguTaskSuccess = false
            isReleaseTaskSuccess = false

            this.android = project.android

            // local.properties
            initLocalProperties()

            // init Extension
            initExtension()

            // task
            createTasks()
        }

        rootProject.gradle.buildFinished {
            if (isJiaguTaskSuccess && releaseExtension.openDir) {
                Utils.openPath(releaseExtension.jiaguOutputPath)
            } else if (isReleaseTaskSuccess && releaseExtension.openDir) {
                Utils.openPath(releaseExtension.outputPath)
            }
            L.i "buildFinished..."
        }
    }

    /**
     * 读取 local.properties 到 localPropertiesMap
     */
    def initLocalProperties() {
        localPropertiesMap = new HashMap<>()

        String localProperties = rootProject.projectDir.toString() + File.separator + "local.properties"
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
        Utils.checkDir(releaseExtension.outputPath)
        Utils.checkDir(releaseExtension.jiaguOutputPath)
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

        create360Task()
    }

    /**
     * 360 加固 相关
     */
    def create360Task() {

        // 加固程序登录
        Task task = project.task("login360Jiagu", type: BaseTask, group: '360Jiagu', description: "360Jiagu login") {
            doLast {
                String jiaguProgramDir = localPropertiesMap.get("jiaguPath")
                String account = localPropertiesMap.get("account360")
                String passwd = localPropertiesMap.get("passwd360")

                if (!jiaguProgramDir) {
                    throw new IllegalArgumentException("jiaguPath 为空, 请在项目根目录的 local.properties 中配置 jiaguPath")
                }
                if (!account) {
                    throw new IllegalArgumentException("account360 为空, 请在项目根目录的 local.properties 中配置 account360")
                }
                if (!passwd) {
                    throw new IllegalArgumentException("passwd360 为空, 请在项目根目录的 local.properties 中配置 passwd360")
                }

                // 登录账户
                Utils.execCommand("java -jar $jiaguProgramDir -login ${account} ${passwd}")
            }

        }

        // 加固初始化
        project.task("init360Jiagu", type: BaseTask, dependsOn: task, group: '360Jiagu', description: "360Jiagu init") {
            doLast {
                String jiaguProgramDir = localPropertiesMap.get("jiaguPath")
                String apkSigning = localPropertiesMap.get("apkSigning")
                if (!jiaguProgramDir) {
                    throw new IllegalArgumentException("jiaguPath 为空, 请在项目根目录的 local.properties 中配置 jiaguPath")
                }
                if (!apkSigning) {
                    throw new IllegalArgumentException("apkSigning 为空, 请在项目根目录的 local.properties 中配置 apkSigning")
                }

                def signing = android.signingConfigs.find { it.name == apkSigning }
                if (!signing) {
                    throw new IllegalArgumentException("找不到名为${apkSigning}的签名配置")
                }

                def jiaguPath = jiaguProgramDir
                def storeFile = signing.storeFile
                def keyAlias = signing.keyAlias
                def keyPassword = signing.keyPassword
                def storePassword = signing.storePassword

                // 导入证书 <keystore_path><keystore_password><alias><alias_password>
                Utils.execCommand("java -jar $jiaguPath -importsign ${storeFile} ${storePassword} ${keyAlias} ${keyPassword}")
                // 清空增值服务
                Utils.execCommand("java -jar $jiaguPath -config ''")
            }

        }

        // 启动 gui 程序
        project.task("launch360Jiagu", type: BaseTask, group: '360Jiagu', description: "launch 360Jiagu") {
            doLast {

                String jiaguProgramDir = localPropertiesMap.get("jiaguPath")
                if (!jiaguProgramDir) {
                    throw new IllegalArgumentException("jiaguPath 为空, 请在项目根目录的 local.properties 中配置 jiaguPath")
                }

                Utils.execCommand("java -jar $jiaguProgramDir")
            }

        }

    }

    /**
     * 创建 task
     *
     * @param buildTypeName -
     * @param flavorName -
     */
    def createTask(String buildTypeName, String flavorName) {
        if (buildTypeName != null)
            buildTypeName = buildTypeName.capitalize()
        else
            buildTypeName = ""
        if (flavorName != null)
            flavorName = flavorName.capitalize()
        else
            flavorName = ""

        def base = "$flavorName$buildTypeName"

        def releaseDependsOn = "assemble$base"
        def releaseName = "release$base"

        def jiaguTaskName = "jiagu$base"

        L.d "create task: ${releaseName}、 ${jiaguTaskName}"

        List<VariantDataBean> variantBeans = new ArrayList<>()

        // 所有变种
        android.applicationVariants
                .findAll { applicationVariant ->    // 找到指定名称的变种
                    if (buildTypeName == "" && flavorName == "") {
                        return true
                    } else if (buildTypeName == "" && flavorName != "") {
                        return flavorName.equalsIgnoreCase(applicationVariant.flavorName)
                    } else if (buildTypeName != "" && flavorName == "") {
                        return buildTypeName.equalsIgnoreCase(applicationVariant.buildType.name)
                    } else {
                        return (buildTypeName.equalsIgnoreCase(applicationVariant.buildType.name) && flavorName.equalsIgnoreCase(applicationVariant.flavorName))
                    }
                }
                .forEach(new Consumer<ApplicationVariant>() {   // 遍历，转换为 apkFile
                    @Override
                    void accept(ApplicationVariant variant) {
                        variantBeans.add(VariantDataBean.newInstance(project, variant))
                    }
                })


        // 生成 release task
        project.task(releaseName, type: ReleaseTask, dependsOn: releaseDependsOn, group: 'deploy', description: "assemble and rename to $releaseExtension.outputPath") {
            variantDataBeans = variantBeans
            fileNameTemplate = releaseExtension.fileNameTemplate
            outputDir = releaseExtension.outputPath
            outputFiles = new ArrayList<>()

            doLast {
                isReleaseTaskSuccess = success
            }

        }

        // 生成加固 task
        project.task(jiaguTaskName, type: JiaguTask, dependsOn: releaseDependsOn, group: 'jiagu', description: "jiagu and rename to $releaseExtension.outputPath") {
            jiaguProgram = JIAGU_360
            jiaguCmdParams = releaseExtension.jiaguCmdParams
            jiaguProgramDir = localPropertiesMap.get("jiaguPath")
            variantDataBeans = variantBeans
            fileNameTemplate = releaseExtension.jiaguFileNameTemplate
            outputDir = releaseExtension.jiaguOutputPath

            doLast {
                isJiaguTaskSuccess = success
            }

        }

    }

}

