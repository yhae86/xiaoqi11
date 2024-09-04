# sheishiwodibot

telegram 上的谁是卧底游戏机器人。根据此源代码，你可以稍加改动创建自己的机器人，同时定制 词语、国家语言等支持

# 配置需要
开发工具 idea

语言 java-15

数据库 mysql 8+

# 如何使用
1、在数据库中运行 src/main/resources/create.sql 中命令（可修改密码）

2、编辑 src/main/resources/db.properties （如果修改过src/main/resources/create.sql）

3、运行构建工具 Plgins/mybatis-generator

4、添加 src/main/resources/mapper/ 中2个文本文件中的内容分别至src/main/java/com/m/sql/mapper/中的4个文件（添加 函数 与 数据库语句节点）

5、编辑 src/main/resources/telegram.properties

# 自定义

src/main/resources/strings.xml ：包含了所有文字信息、词语、国家语言文本 

  <language> < language>国家语言支持
  
    <strings>国家语言文本 
      
      <string>语言文本
        
    <words>这个语言所支持的词语
      
      <word>一对词语
        
        </word1>词语1
      
        </word2>词语2
 </language>

# 授权
通过此源代码衍生的产品或作品不该用于商业用途，除非有开发者的授权
