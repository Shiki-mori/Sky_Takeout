# 新增员工

## 需求设计和分析

### 产品原型  

录入  

- 账号  唯一
- 员工姓名  
- 手机号  格式校验：11位数字
- 性别  男或女
- 身份证号  格式校验：18位数字
- 密码  默认：123456

### 接口设计

请求方式(Method)：POST  
请求路径(Path)：/admin/employee  
请求参数： json 格式  

Headers  

|参数名称|参数值|是否必须|
|---|---|---|
|Content-Type|application/json|是|

Body

|参数类型|参数名称|参数描述|是否必须|
|---|---|---|---|
|String|id|员工id|否|
|String|name|员工姓名|是|
|String|phone|手机号|是|
|String|sex|性别|是|
|String|idNumber|身份证号|是|
|String|username|用户名|是|

响应结果：json 格式，封装为result对象返回  
{
    code
    data
    msg
}

## 代码开发

### 创建DTO

设计DTO，根据新增员工接口  
封装前端提交的数据  

[EmployeeDTO](../sky-take-out/sky-pojo/src/main/java/com/sky/dto/EmployeeDTO.java)

在[EmployeeController.java](../sky-take-out/sky-server/src/main/java/com/sky/controller/admin/EmployeeController.java)中添加新增员工功能。

调用employeeservice的save方法。需要先在[EmployeeService.java](../sky-take-out/sky-server/src/main/java/com/sky/service/impl/EmployeeServiceImpl.java)实现save方法。

## 功能测试

- 前后端联调测试
- 接口文档测试

前后端开发进度可能不同步，主要以接口文档测试为主。

启动后端，访问<http://localhost:8080/doc.html>，点击新增员工，选择调试，填充请求参数：

```json
{
  "id": 0,
  "idNumber": "111111222233334444",
  "name": "张三",
  "phone": "11122223333",
  "sex": "1",
  "username": "zhangsan"
}
```

发送。由于缺少令牌，不能通过jwt验证，返回401错误。  
将在接口文档中统一加入一个令牌，方便后续调试。

从员工登录功能发送请求，获取一个返回的令牌token。

全局参数设置--添加参数。设置参数名称为token，参数类型为header，参数值为返回的令牌。  
> token的名称在[application.yml](../sky-take-out/sky-server/src/main/resources/application.yml)中配置。

## 代码完善

程序存在的问题：  

### SQL异常未处理

录入的用户名已存在，将抛出异常，但是没有进行处理。  
异常来源：数据库对用户名的唯一约束。  
报错信息:

  ```text
  2026-06-01 16:07:40.495 ERROR 19519 --- [nio-8080-exec-7] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.springframework.dao.DuplicateKeyException: 
  ### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'
  ### The error may exist in com/sky/mapper/EmployeeMapper.java (best guess)
  ### The error may involve com.sky.mapper.EmployeeMapper.insert-Inline
  ### The error occurred while setting parameters
  ### SQL: insert into employee (name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
  ### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'
  ; Duplicate entry 'zhangsan' for key 'idx_username'; nested exception is java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'] with root cause

  java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'zhangsan' for key 'idx_username'
  ```

使用全局异常处理器捕获异常。  
从报错日志中获取异常类型及其报错信息。

### 获取登录用户id

新增员工时，创建人id和修改人id设为固定值10L。TODO：修改为当前登录用户id。

实现思路：  
通过JWT令牌反向解析获取当前登录用户id。

解析出id后，如何传递给service层的save方法？

>ThreadLocal是Thread的局部变量。  
ThreadLocal为每个线程创建一个副本，每个线程的副本互不干扰。只有一个线程能修改自己的副本，其他线程的副本值不变。外部无法访问。  
客户端发起的每一次请求，tomcat服务器都为其分配一个线程。  
因此可以将当前登录用户id保存在ThreadLocal中。不会被其他请求线程所干扰。同时可在该请求的service调用中使用。  

>TheadLocal常用方法  
- public void set(T value)  设置当前线程的局部变量的值
- public T get()  获取当前线程的线程局部变量值
- public void remove()  删除当前线程的线程局部变量

创建一个ThreadLocal变量，保存当前登录用户id。