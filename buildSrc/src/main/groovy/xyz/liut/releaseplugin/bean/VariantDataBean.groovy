package xyz.liut.releaseplugin.bean

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Project
import xyz.liut.logcat.L

/**
 * input variant
 */
class VariantDataBean implements Serializable{

    public VariantMetaData metaData

    public File apkFile


    /**
     * 变种元数据
     *
     * Create by liut on 12/11/20
     */
    static class VariantMetaData implements Serializable{

        public String name

        public String buildTypeName

        public String flavorName

        public String versionName

        public int versionCode

        public boolean signingReady


        /**
         * 根据模板生成文件名
         *
         * @return 文件名
         */
        String fileNameTemplate(String fileNameTemplate) {
            return fileNameTemplate
                    .replace('$app', this.name)
                    .replace('$b', this.buildTypeName)
                    .replace('$f', this.flavorName)
                    .replace('$vn', this.versionName)
                    .replace('$vc', this.versionCode.toString())
        }

    }

    VariantMetaData getMetaData() {
        return metaData
    }

    File getApkFile() {
        return apkFile
    }

    /**
     * new VariantDataBean
     */
    static VariantDataBean newInstance(Project project, ApplicationVariant variant) {
        VariantDataBean bean = new VariantDataBean()

        def outputs = variant.outputs
        if (outputs.size() != 1) {
            throw new IllegalArgumentException("outputs.size = $outputs.size()")
        }

        def output = outputs[0]

        def outputFile = output.outputFile

        L.d outputFile

        VariantMetaData metaData = new VariantMetaData()
        metaData.name = project.name
        metaData.buildTypeName = variant.buildType.name
        metaData.flavorName = variant.flavorName
        metaData.versionName = variant.versionName
        metaData.versionCode = variant.versionCode
        metaData.signingReady = variant.signingReady

        bean.apkFile = outputFile
        bean.metaData = metaData

        return bean
    }


}
