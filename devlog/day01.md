# 开发环境搭建

## 前端环境搭建

### 安装nginx

```bash
sudo zypper install nginx
```

### 配置文件

在提供的前端运行环境文件夹中，有一个目录`nginx-1.20.2`。  
目录下有html目录，里面是前端页面文件。目录下的nginx.exe只是启动一个web服务器，作用是把html目录发布出去。

查找opensuse的默认网页目录：

```bash
grep root /etc/nginx/nginx.conf
```

输出：

```text
	    root /srv/www/htdocs;
	#	root /srv/www/htdocs;
	#	root /srv/www/htdocs;
#	    root /srv/www/htdocs;
#		root /srv/www/htdocs;
```

说明opensuse nginx默认网站目录是`/srv/www/htdocs`。

将sky_takeout的html目录内容复制到该目录下:

```bash
sudo cp -r <file-path>/html/sky/* /srv/www/htdocs/
```

启动nginx，检查状态：

```bash
sudo systemctl start nginx
sudo systemctl status nginx
```

将conf文件迁移到nginx的conf目录下：

```bash
sudo mv nginx.conf nginx_backup.conf
sudo cp <file-path>/nginx-1.20.2/conf/nginx.conf /etc/nginx/
```

使用浏览器访问<http://localhost>，现在是404 Not Found。

### 问题定位

nginx.conf文件中：

```conf
location / {
    root   html/sky;
    index  index.html index.htm;
}
```

html/sky是一个相对路径，将作为`/usr/sbin/nginx/html/sky`被使用。但是opensuse的nginx默认网站目录是`/srv/www/htdocs`。

将本段修改为：

```conf
location / {
    root   /srv/www/htdocs;
    index  index.html;
    try_files $uri $uri/ /index.html;
}
```

重启nginx：

```bash
sudo nginx -t
sudo systemctl restart nginx
```

修改后，访问`localhost`显示nginx的默认页面`Welcome to nginx!`。  

这说明更改的nginx配置没有生效。

```bash
sudo nginx -T | grep -n "server_name" 
grep -R "root" /etc/nginx/nginx.conf
```

输出：

```text
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
48: server_name localhost;
114: # server_name somename alias another.alias;
127: # server_name localhost;
```

输出：

```text
    root /srv/www/htdocs; 
    root html; 
    # root html; 
    # deny access to .htaccess files, if Apache's document root 
    # root html; 
    # root html;
```

需要将server设置为默认server。

打开nginx.conf文件，在listen 80后前添加default_server：

```conf
server {
    listen 80 default_server;
    server_name localhost;

    ...
}
```

搞半天复制错目录了。应该复制的是`sky`目录下的内容。

访问localhost，显示正确页面。

### nginx反向代理

前端向nginx服务器发送请求，nginx服务器再将请求转发给后端tomcat。  

#### 优势

- 隐藏后端服务器真实地址，保证后端服务安全
- 负载均衡，将请求分配给不同的后端服务器
- 缓存静态资源，提高访问速度

#### 配置方法

在文件nginx.conf中添加如下配置：

```conf
server {
    listen 80;
    server_name localhost;
    
    location /api/ {
        proxy_pass http://localhost:8080/admin/; # 反向代理
    }
}
```

```conf
upstream webservers {
    server 192.168.100.128:8080;
    server 192.168.100.129:8080;
}
server {
    listen 8080;
    server_name localhost;

    location /api/ {
        proxy_pass http://webservers/admin/; # 负载均衡
    }
}

## 后端环境搭建

后端工程基于maven进行项目构建。

### 项目结构

sky-take-out：maven父工程，统一管理依赖版本，聚合其他子模块  
sky-common：子模块，存放公共类，如工具类，常量类，异常类  
sky-pojo：子模块，存放实体类，如VO、DTO等  
sky-server：子模块，后端服务，存放配置文件、Controller、Service、Mapper等

### 数据库环境搭建

进入mariadb，执行：

```sql
source path/sky.sql
```

导入成功后，为该数据库创建专用用户并授权：

```sql
CREATE USER 'sky_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON sky_take_out.* TO 'sky_user'@'localhost';
FLUSH PRIVILEGES;
```

连接dbeaver。启动dbeaver，创建新的连接，填写数据库名称，username及password，测试连接，下载驱动，完成。

## 前后端联调

前端页面-Controller-Service-Mapper-数据库

- Controller：接受并封装参数，调用service方法查询数据库，封装结果并响应  
- Service：调用mapper方法查询数据库，密码比对，返回结果
- Mapper：调用数据库，返回结果

### 后端启动

在sky-server模块中的resources目录下，找到application.yml文件，修改数据库连接信息。

在根目录执行`mvn clean package`进行编译。  
`mvn clean install`进行安装。

在控制台进入sky-take-out/sky-server目录，执行`mvn spring-boot:run`启动后端服务。

启动成功，访问<http://localhost>，点击登录，进入初始页面。

## 导入接口文档

使用apifox：<https://apifox.com/>，下载AppImage。

创建两个项目。  
将`管理端接口``用户端接口`两个json文件导入。

选择导入，格式选择yapi。选择文件导入->创建新模块。

## Swagger

使用swagger帮助后端生成接口文档，并进行接口测试。

按照规范定义接口及接口相关的信息，即可生成接口文档，及在线接口调试页面。  

相比postman的优势：  
postman在测试接口时，需要手动输入参数，并且返回结果为json格式。当参数数量过多时操作不便。

swagger:<https://swagger.io/>

Knife4j 是为 java MVC 框架集成swagger生成api文档的增强解决方案。  
本项目中使用knife4j以简化操作。

使用方法：  

在pom.xml中导入swagger配置（maven依赖）。  
在`sky-server/src/main/java/com/sky/config/WebMvcConfiguration.java`编写生成接口文档（指定扫描包）以及静态资源映射。  
api文档在<http://localhost:8080/doc.html>访问。  
该页面下选择调试，在请求参数中直接输入数据并发送，可查看返回。