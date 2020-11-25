# ImageFileTools
初始化图片操作工具

#如果使用的是bufferedimage进行缩放，参考命令如下：
java -jar -Xms128m -Xmx512m ImageFileTools.jar D:\demo\20201113\ddd D:\demo\20201113\ddds 0.2 true
对应着BufferedImageZoomMain主函数入口的：srcDir tarDir scale isCopyOtherFile

#如果使用的是gdal命令进行缩放，参考命令如下：
java -jar -Xms128m -Xmx512m ImageFileTools.jar D:\demo\20201113\ddd D:\demo\20201113\ddds 20% true
对应着GdalZoomMain主函数入口的：srcDir tarDir scale isCopyOtherFile
注意，要注意根据gdal的安装地址，进行GdalComman.properties命令的修改


#关于gdal的windows安装方式：
1）源码安装，这个百度一下，大多数情况下服务器环境都是没有这个办法的；
2）使用http://download.gisinternals.com/sdk/downloads/release-1911-x64-gdal-3-0-4-mapserver-7-4-3/gdal-300-1911-x64-core.msi
3）支持32位的机器可以用http://fwtools.loskot.net/FWTools247.exe，但是一般windows server都不支持
