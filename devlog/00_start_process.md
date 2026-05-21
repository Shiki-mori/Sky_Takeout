# 前端启动

启动nginx，检查状态：

```bash
sudo systemctl start nginx
```

使用浏览器访问<http://localhost>

# 后端启动

## 编译

根目录执行：

```bash
mvn clean install
cd sky-server
mvn spring-boot:run
```
