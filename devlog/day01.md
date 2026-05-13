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

## 后端环境搭建

后端工程基于maven进行项目构建。

### 项目结构

sky-take-out：maven父工程，统一管理依赖版本，聚合其他子模块  
sky-common：子模块，存放公共类，如工具类，常量类，异常类  
sky-pojo：子模块，存放实体类，如VO、DTO等  
sky-server：子模块，后端服务，存放配置文件、Controller、Service、Mapper等