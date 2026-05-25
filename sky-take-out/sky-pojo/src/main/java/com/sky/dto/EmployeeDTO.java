package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
//为类的字段自动生成 getter、setter、toString、equals 和 hashCode 方法
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
