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
