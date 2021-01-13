## 展示md或txt格式的文章，纯静态网页。
### 使用方法
将文章放到myarticle目录下，然后命名为<数字-文章名>,例如：
> 02-第一篇.md 

然后执行sh runGen.sh,将会在本目录生成articleIndexes.js。里面包含了所有文章的索引。
然后刷新网页即可。
索引的组织层次与myarticle的子目录的层次相同。

### You Need Know
1. 所有文件请使用UTF-8。
2. HTML格式的页面因为head部分有<meta charset="utf-8">，所以可以正常显示。
3. 纯文本方式打开的网页，中文可能会显示乱码。只有把浏览器的网页默认编码设置为UTF-8就可以了。
4. Chrome从55版本开始，不能设置网页默认编码了。在Chrome网上应用店搜索Set Character Encoding，安装。然后在你的页面 右键-Set Character Encoding，选择Unicode(UTF-8)。
