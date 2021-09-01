package com.example.eam.managers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SessionManager {
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "LOGIN";
    private static final String LOGIN = "IS_LOGIN";
    public static final String COMPANYID = "COMPANYID";
    /*public static final String NAME = "NAME";
    public static final String EMAIL = "EMAIL";
    public static final String ID = "ID";*/

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createSession(String companyID){
        editor.putBoolean(LOGIN, true);
        editor.putString(COMPANYID, companyID);
        /*editor.putString(NAME, name);
        editor.putString(EMAIL, email);
        editor.putString(ID, id);*/
        editor.apply();
        //editor.commit();
    }

    public boolean isLoggin(){
        return sharedPreferences.getBoolean(LOGIN, false);
    }

    public boolean checkLogin(){
        if(!this.isLoggin()){
            /*Intent i = new Intent(context, PhoneLoginActivity.class);
            context.startActivity(i);
            ((MainActivity)context).finish();*/
            return false;
        }
        else{
            return true;
        }
    }

    public HashMap<String, String> getUserDetail(){
        HashMap<String, String> user = new HashMap<>();
        user.put(COMPANYID, sharedPreferences.getString(COMPANYID, null));

        /*user.put(NAME, sharedPreferences.getString(NAME, null));
        user.put(EMAIL, sharedPreferences.getString(EMAIL, null));
        user.put(ID, sharedPreferences.getString(ID, null));*/

        return user;
    }

    public void logout(){
        editor.clear();
        editor.commit();
        /*Intent i = new Intent(context, PhoneLoginActivity.class);
        context.startActivity(i);
        ((MainActivity)context).finish();*/
    }
}
