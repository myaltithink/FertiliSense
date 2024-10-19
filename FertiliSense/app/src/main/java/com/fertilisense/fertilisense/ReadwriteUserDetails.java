package com.fertilisense.fertilisense;

public class ReadwriteUserDetails {

    //For Registration of User Account
    public String username, date, gender, phone, age;
    public ReadwriteUserDetails(){};
    public ReadwriteUserDetails(String textUsername, String textDate, String textGender, String textPhone, String textAge){
        this.username = textUsername;
        this.date = textDate;
        this.gender = textGender;
        this.phone = textPhone;
        this.age = textAge;
    }
}
