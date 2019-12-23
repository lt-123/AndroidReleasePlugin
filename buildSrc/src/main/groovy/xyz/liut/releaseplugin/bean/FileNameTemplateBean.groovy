package xyz.liut.releaseplugin.bean

import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.BuildType
import org.gradle.api.Project

/**
 * 文件模板
 */
class FileNameTemplateBean {

    private String name

    private BuildType buildType

    private String flavorName

    private String versionName

    private int versionCode

    private File outputFile

    /**
     * @param project 项目
     * @param applicationVariant 安卓变种
     */
    FileNameTemplateBean(Project project, ApplicationVariant applicationVariant) {
        this.name = project.name
        this.buildType = applicationVariant.buildType
        this.flavorName = applicationVariant.flavorName
        this.versionName = applicationVariant.versionName
        this.versionCode = applicationVariant.versionCode
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    BuildType getBuildType() {
        return buildType
    }

    void setBuildType(BuildType buildType) {
        this.buildType = buildType
    }

    String getFlavorName() {
        return flavorName
    }

    void setFlavorName(String flavorName) {
        this.flavorName = flavorName
    }

    String getVersionName() {
        return versionName
    }

    void setVersionName(String versionName) {
        this.versionName = versionName
    }

    int getVersionCode() {
        return versionCode
    }

    void setVersionCode(int versionCode) {
        this.versionCode = versionCode
    }

    File getOutputFile() {
        return outputFile
    }

    void setOutputFile(File outputFile) {
        this.outputFile = outputFile
    }

    /**
     * 根据模板生成文件名
     *
     * @return 文件名
     */
    String fileNameTemplate(String fileNameTemplate) {
        return fileNameTemplate
                .replace('$app', this.name)
                .replace('$b', this.buildType.name)
                .replace('$f', this.flavorName)
                .replace('$vn', this.versionName)
                .replace('$vc', this.versionCode.toString())
    }


}
