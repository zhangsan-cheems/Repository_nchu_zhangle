package com.zhang.emailsnapshot.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhang.emailsnapshot.mapper.MybatisMapper;
import com.zhang.emailsnapshot.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class CommonController {

    @Autowired
    private MybatisMapper mybatisMapper;

    /**
     * 处理注册信息
     * 注册信息的校验都在这里
     * @param userInfo
     * @return
     */
    @ResponseBody
    @RequestMapping("/register")
    public String register(UserInfo userInfo){
        //先在数据库中查询手机号码，若存在，则不让注册
        UserInfo userInfo1=mybatisMapper.quaryUserInfoByPhoneNum1(userInfo);
        String phonenum1=userInfo1.getPhoneNum();
        if(phonenum1!=null){
            return "erro06";
        }
        //检验电话号码
        //电话号码必须为11位数字
        String phonenum=userInfo.getPhoneNum();
        String pattern="^\\d{11}$";
        if(!phonenum.matches(pattern)){
            return "erro01";
        }
        //检验真实姓名
        //真名必须为汉字
        String realname=userInfo.getRealName();
        pattern="^[\\u4e00-\\u9fa5]{0,}$";
        if(!realname.matches(pattern)){
            return "erro02";
        }
        //检验密码
        //密码格式为6到12位的任意字符
        String password=userInfo.getPassword();
        pattern="^.{6,12}$";
        if(!password.matches(pattern)){
            return "erro03";
        }
        //通过所有检验条件即可入库
        mybatisMapper.register(userInfo);
        //进入user_info后，再获取其id
        int id=mybatisMapper.quaryIdByPhoneNum(userInfo);
        userInfo.setId(id);
        //注入user_list表
        UserListMapper userListMapper=new UserListMapper();
        userListMapper.setId(userInfo.getId());
        userListMapper.setCustName(userInfo.getCustName());
        mybatisMapper.register1(userListMapper);
        return "success";
    }

    @ResponseBody
    @RequestMapping("/login")
    public String login(UserInfo userInfo, HttpSession session){
        String phonenum=userInfo.getPhoneNum();
        String password=mybatisMapper.quaryPasswordByPhoneNum(phonenum);
        String pwd=userInfo.getPassword();
        if(!password.equals(pwd)){
            return "fail";
        }
        //密码验证成功，将数据存入session
        userInfo=mybatisMapper.quaryUserInfoByPhoneNum(phonenum);
        session.setAttribute("id",userInfo.getId());
        session.setAttribute("phonenum",userInfo.getPhoneNum());
        session.setAttribute("realname",userInfo.getRealName());
        session.setAttribute("custname",userInfo.getCustName());
        session.setAttribute("password",userInfo.getPassword());
        return "success";
    }

    @ResponseBody
    @RequestMapping("/getSession")
    public String getSession(HttpSession session){
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("id",(int)session.getAttribute("id"));
        jsonObject.put("phonenum",(String)session.getAttribute("phonenum"));
        jsonObject.put("realname",(String)session.getAttribute("realname"));
        jsonObject.put("custname",(String)session.getAttribute("custname"));
        jsonObject.put("password",(String)session.getAttribute("password"));
        String str=jsonObject.toJSONString();
        return str;
    }

    @ResponseBody
    @RequestMapping("/getPhoneNum")
    public String getPhoneNum(HttpSession session){
        return (String) session.getAttribute("phonenum");
    }

    /**
     * 获取收件信息
     * @param session
     * @return
     */
    @ResponseBody
    @RequestMapping("/getReceiveEmailText")
    public String getReceiveEmailText(HttpSession session){
        int Id=(int)session.getAttribute("id");
        List<EmailTextInfoForData> emailTextInfos = mybatisMapper.getEmailText(Id);
        String json=JSON.toJSONString(emailTextInfos);
        System.out.print("收件信息为：");
        System.out.println(json);
        return json;
    }

    /**
     * 动态获取用户名
     */
    @ResponseBody
    @RequestMapping("/getCustName")
    public String getCustName(UserInfo userInfo){
        String phonenum=userInfo.getPhoneNum();
        String custName=mybatisMapper.quaryCustNameByPhoneNum(phonenum);
        return custName;
    }

    /**
     * 处理发送邮件
     */
    @ResponseBody
    @RequestMapping("/handleSendForText")
    public String handleSendForText(EmailTextInfo emailTextInfo, HttpSession session){
        emailTextInfo.setSendId((int)session.getAttribute("id"));
        //先做校验，看看收件人id是否在库中
        UserListMapper userListMapper=new UserListMapper();
        userListMapper.setId(emailTextInfo.getReceiveId());
        userListMapper=mybatisMapper.checkExist(userListMapper);
        if(userListMapper==null){
            return "erro04";
        }
        //如果没错，直接入库
        emailTextInfo.setUpdateTime(new Date());
        mybatisMapper.SendEmail(emailTextInfo);
        return "success";
    }

    @ResponseBody
    @RequestMapping("/getIdByCustName")
    public String getIdByCustName(UserListMapper userListMapper){
        List<UserListMapperForData> list=mybatisMapper.quaryUserByCustName(userListMapper);
        String json=JSON.toJSONString(list);
        if(list.size()==0){
            return "erro05";
        }
        return json;
    }

    @ResponseBody
    @RequestMapping("/quitSys")
    public String quitSys(HttpSession session){
        session.removeAttribute("id");
        session.removeAttribute("phonenum");
        session.removeAttribute("realname");
        session.removeAttribute("custname");
        session.removeAttribute("password");
        return "ok";
    }

    /**
     * 发送文件
     * @param session
     * @param emailFileInfo
     * @param upload
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/sendFile")
    public String sendFile(HttpSession session, EmailFileInfo emailFileInfo, MultipartFile upload, HttpServletRequest request){
        //检查一下receiveId
        UserListMapper userListMapper=new UserListMapper();
        userListMapper.setId(emailFileInfo.getReceiveId());
        userListMapper=mybatisMapper.checkExist(userListMapper);
        if(userListMapper==null){
            return "请输入正确的用户ID！";
        }

        emailFileInfo.setSendId((int)session.getAttribute("id"));
        emailFileInfo.setUpdateTime(new Date());
        //转存文件
        //给文件名加个时间戳，避免文件重名
        String name=upload.getOriginalFilename();
        Date date=new Date();
//        name=date+name;//不能直接加时间
        //加时间戳
        name=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+name;
//        String path=request.getSession().getServletContext().getRealPath("/email-file/");
        String path="C:\\Users\\zhang\\Desktop\\email-file";
        String filepath=path+ File.separator+name;
        try {
            //同名不能传入
            upload.transferTo(new File(path,name));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        }
        emailFileInfo.setEmailUrl(filepath);
        mybatisMapper.sendEmailFile(emailFileInfo);
        return "发送成功！";
    }

    @ResponseBody
    @RequestMapping("/getReceiveEmailFile")
    public String getReceiveEmailFile(HttpSession session){
        int Id=(int)session.getAttribute("id");
        List<EmailFileInfoForData> emailFileInfos = mybatisMapper.getEmailFile(Id);
        String json=JSON.toJSONString(emailFileInfos);
        System.out.print("收件信息为：");
        System.out.println(json);
        return json;
    }

    /**
     * 下载附件
     * @param filePath
     * @return
     * @throws IOException
     */
    @ResponseBody
    @RequestMapping("/downLoad")
    public String downLoad(String filePath) throws IOException {
        File file=new File(filePath);
        InputStream inputStream=new FileInputStream(file);
        System.out.println("文件大小为："+file.length());
        String fileType = filePath.substring(filePath.lastIndexOf("."));
        System.out.println("文件后缀为："+fileType);
        byte bytes[]=new byte[(int)file.length()];
        inputStream.read(bytes);
        String name="";
        name=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+name;
        OutputStream outputStream=new FileOutputStream("C:\\Users\\zhang\\Desktop\\file-receive\\"+name+fileType);
        outputStream.write(bytes);
        return "ok";
    }

    /**
     * 修改密码
     */
    @ResponseBody
    @RequestMapping("/changePwd")
    public String changePwd(UserInfo userInfo,HttpSession session){
        userInfo.setId((int)session.getAttribute("id"));
        String newPwd=userInfo.getPassword();
        String pattern="^.{6,12}$";
        if(!newPwd.matches(pattern)){
            return "erro03";
        }
        mybatisMapper.changePwd(userInfo);
        return "success";
    }

    /**
     * 查询管理员身份
     */
    @ResponseBody
    @RequestMapping("/checkManager")
    public String checkManager(HttpSession session){
        Manager manager=mybatisMapper.checkManager((int)session.getAttribute("id"));
        if(manager==null){
            return "no";
        }
        return "yes";
    }

    /**
     * 用户Delete
     */
    @ResponseBody
    @RequestMapping("/deleteUser")
    public String deleteUser(int id){
        mybatisMapper.deleteUser(id);
        mybatisMapper.deleteUser1(id);
        return "success";
    }
}
