package com.zhang.emailsnapshot.mapper;

import com.zhang.emailsnapshot.pojo.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface MybatisMapper {

    @Select("select custname from user_info where phonenum=#{phoneNum}")
    public String quaryCustNameByPhoneNum(String phoneNum);

    @Select("select password from user_info where phonenum=#{phonenum}")
    public String quaryPasswordByPhoneNum(String phonenum);

    @Select("select * from user_info where phonenum=#{phonenum}")
    public UserInfo quaryUserInfoByPhoneNum(String phonenum);

    @Insert("insert into user_info(phonenum,realname,custname,password) values(#{phoneNum},#{realName},#{custName},#{password})")
    public void register(UserInfo userInfo);

    @Insert("insert into user_list(id,custname) values(#{id},#{custName})")
    public void register1(UserListMapper userListMapper);

    @Select("select id from user_info where phonenum=#{phoneNum}")
    public int quaryIdByPhoneNum(UserInfo userInfo);

    @Select("select * from user_list where id=#{id}")
    public UserListMapper checkExist(UserListMapper userListMapper);

    @Insert("insert into email_text_info(receive_id,send_id,email_title,email_text,updatetime) values(#{receiveId},#{sendId},#{emailTitle},#{emailText},#{updateTime})")
    public void SendEmail(EmailTextInfo emailTextInfo);

    @Select("select * from email_text_info where receive_id=#{id} order by updatetime desc")
    public List<EmailTextInfoForData> getEmailText(int id);

    @Select("select * from email_file_info where receive_id=#{id} order by updatetime desc")
    public List<EmailFileInfoForData> getEmailFile(int id);

    @Select("select * from user_list where custname=#{custName}")
    public List<UserListMapperForData> quaryUserByCustName(UserListMapper userListMapper);

    @Select("select * from user_info where phonenum=#{phoneNum}")
    public UserInfo quaryUserInfoByPhoneNum1(UserInfo userInfo);

    @Insert("insert into email_file_info(receive_id,send_id,email_title,email_url,updatetime) values(#{receiveId},#{sendId},#{emailTitle},#{emailUrl},#{updateTime})")
    public void sendEmailFile(EmailFileInfo emailFileInfo);

    @Update("update user_info set password=#{password} where id=#{id}")
    public void changePwd(UserInfo userInfo);

    @Select("select * from manager where id=#{id}")
    public Manager checkManager(int id);

    @Delete("delete from user_info where id=#{id}")
    public void deleteUser(int id);

    @Delete("delete from user_list where id=#{id}")
    public void deleteUser1(int id);
}
