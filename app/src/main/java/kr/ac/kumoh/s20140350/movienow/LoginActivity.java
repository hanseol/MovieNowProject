package kr.ac.kumoh.s20140350.movienow;

/**
 * Created by SEOL on 2017-05-17.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextUserID, editTextUserPW;
    private Button buttonLogin;
    private ProgressDialog progressDialog;
    private Button changeSignup;

    protected InputFilter filter= new InputFilter() {

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]*$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };

    protected InputFilter filterPass= new InputFilter() {

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            Pattern ps = Pattern.compile("^[a-zA-Z0-9]*[?=.*/!@#$%^*+=-]*$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        if (SharedPrefManager.getInstance(this).isLoggedIn())
        {
            startActivity(new Intent(this, FirstActivity.class));
            Toast.makeText(getApplicationContext(), "이미 로그인 되어 있습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextUserID=(EditText) findViewById(R.id.UserName);
       // editTextUserID.requestFocus();
        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
       // imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        editTextUserPW=(EditText)findViewById(R.id.Password);
        buttonLogin=(Button)findViewById(R.id.LoginRequest);

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("잠시만 기다려 주세요....");

        changeSignup=(Button) findViewById(R.id.changeSignup);


        buttonLogin.setOnClickListener(this);
        changeSignup.setOnClickListener(this);
        //아이디는 영어와 숫자만, 비밀번호는 영어와 숫자와 특문만 가능하게 필터 설정
        editTextUserID.setFilters(new InputFilter[] {filter});;
        editTextUserPW.setFilters(new InputFilter[] {filterPass});
    }

    private void userLogin() {
        //입력한 값의 길이 확인
        if ((editTextUserID.getText().toString().length()<5) || (editTextUserID.getText().toString().length()>10))
        {
            Toast.makeText(getApplicationContext(), "아이디는 5~10자여야 합니다", Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.show();
        final String userID=editTextUserID.getText().toString().trim();
        final String userPW=editTextUserPW.getText().toString().trim();

        StringRequest stringRequest= new StringRequest(
                Request.Method.POST,
                Constants.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            progressDialog.dismiss();
                            JSONObject obj=new JSONObject(response);
                            //응답으로 넘어온 데이터 값 중 success field의 값을 비교해서
                            if(obj.getBoolean("success"))//성공했으면
                            {
                                SharedPrefManager.getInstance(getApplicationContext()).userLogin(obj.getString("userID"));
                                Toast.makeText(getApplicationContext(), "환영합니다, "+obj.getString("userID") + "님!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), FirstActivity.class));
                                finish();
                            }
                            else
                            {
                                switch(obj.getInt("rescode"))
                                {
                                    case 1:
                                        Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 다시 확인하세요", Toast.LENGTH_LONG).show();
                                        break;
                                }

                            }
                        }
                        catch(JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })//parameter list end
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params=new HashMap<>();
                //만약 php에서 문제가 생기면 이 변수와 제대로 짝을 맞추었는지 확인할 것
                params.put("userID", userID);
                params.put("userPW", userPW);

                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void onClick(View view)
    {
        if (view==buttonLogin)
            userLogin();
        if (view==changeSignup)
        {
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }

}