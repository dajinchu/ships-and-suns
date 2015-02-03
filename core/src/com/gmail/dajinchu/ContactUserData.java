package com.gmail.dajinchu;

/**
 * Created by Da-Jin on 2/1/2015.
 */
//Basically Box2d's contact class, except it stores the userdata,
//allowing us to manipulate the fixtures without screwing getUserData() up
public class ContactUserData {
    public final Object a,b;

    public ContactUserData(Object adata, Object bdata){
        this.a = adata;
        this.b = bdata;
    }

    public boolean isDataNull(){
        if(a==null||b==null)return true;

        return false;
    }
}
