package kr.ac.kumoh.s20140350.movienow;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by DH on 2017-04-27.
 */

public class SharedPrefManager {
    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private static final String SHARED_PREF_NAME="userInfo";
    private static final String KEY_USER_ID="userID";
    private static final String KEY_ACCESS_TOKEN="token";



    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    //로그인 시도
    public boolean userLogin(String username) {
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(KEY_USER_ID,username);
        editor.apply();
        return true;
    }

    //로그인 여부 확인
    public boolean isLoggedIn() {
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (sharedPreferences.getString(KEY_USER_ID,null)!=null){
            return true;
        }
        return false;
    }

    //로그아웃 버튼을 눌렀을 때 사용할 것
    public boolean logout() {
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_USER_ID);
        editor.apply();
        return true;
    }

    //사용자의 ID를 알아내는 부분. 나중에 영화 추천을 위한 요청을 보낼 때 사용할 것
    public String getUserID()
    {
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    //FCM 관련--------------------------------------------------------------------------------------------------------
    public boolean storeToken(String token) {
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
        return true;
    }

    public String getToken() {
        SharedPreferences sharedPreferences=mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }
}
