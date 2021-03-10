package com.zhang.emailsnapshot.pojo;

import java.util.Date;

public class EmailFileInfoForData {
    private int email_id;
    private int receive_id;
    private int send_id;
    private String updatetime;
    private String email_title;
    private String email_url;

    public int getEmail_id() {
        return email_id;
    }

    public void setEmail_id(int email_id) {
        this.email_id = email_id;
    }

    public int getReceive_id() {
        return receive_id;
    }

    public void setReceive_id(int receive_id) {
        this.receive_id = receive_id;
    }

    public int getSend_id() {
        return send_id;
    }

    public void setSend_id(int send_id) {
        this.send_id = send_id;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getEmail_title() {
        return email_title;
    }

    public void setEmail_title(String email_title) {
        this.email_title = email_title;
    }

    public String getEmail_url() {
        return email_url;
    }

    public void setEmail_url(String email_url) {
        this.email_url = email_url;
    }
}
