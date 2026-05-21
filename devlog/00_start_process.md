# 前端启动

启动nginx，检查状态：

```bash
sudo systemctl start nginx
```

使用浏览器访问<http://localhost>

# 后端启动

## 编译

在vscode中显示Maven视图，右键sky-take-out(父工程，聚合了其他模块)，选择`run maven commands--compile`进行编译，显示`BUILD SUCCESS`表示编译成功。

在资源管理器的sky-server模块下，右键SkyApplication.java，选择`run Java`启动后端服务。