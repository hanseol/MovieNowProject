package kr.ac.kumoh.s20140350.movienow;

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

public class changePasswordActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText password;
    private EditText checkpass;
    private Button changeRequestButton;
    private ProgressDialog progressDialog;

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
        setContentView(R.layout.activity_change_password);

        password=(EditText) findViewById(R.id.Password);
        checkpass=(EditText) findViewById(R.id.checkPassword);
        changeRequestButton=(Button)findViewById(R.id.changePassRequest);

        //필터 설정
        password.setFilters(new InputFilter[] {filterPass});
        checkpass.setFilters(new InputFilter[] {filterPass});

        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("잠시만 기다려 주세요....");
        changeRequestButton.setOnClickListener(this);
    }

    private void changePassword()
    {
        //비밀번호 길이 검사
        if ((password.getText().toString().length()<4) || (checkpass.getText().toString().length()>10))
        {
            Toast.makeText(getApplicationContext(), "비밀번호는 4~10자여야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }
        //비밀번호와 비밀번호 확인 체크
        if (!password.getText().toString().trim().equals(checkpass.getText().toString().trim()))
        {
            Toast.makeText(getApplicationContext(), "비밀번호와 비밀번호 확인이 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        final String userID= SharedPrefManager.getInstance(this).getUserID();
        final String userPW=password.getText().toString().trim();

        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                Constants.URL_CHANGEPASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        //응답이 json 형식으로 넘어오므로 이에 대한 처리
                        try
                        {
                            JSONObject jsonObject=new JSONObject(response);
                            switch (jsonObject.getInt("rescode"))
                            {
                                case 0:
                                    Toast.makeText(getApplicationContext(), "비밀번호 변경 도중 오류가 발생했습니다", Toast.LENGTH_LONG).show();
                                    break;
                                case 1:
                                    Toast.makeText(getApplicationContext(), "정상적으로 변경되었습니다. 다시 로그인하세요", Toast.LENGTH_LONG).show();
                                    SharedPrefManager.getInstance(getApplicationContext()).logout();
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();
                                    break;
                                case 2:
                                    Toast.makeText(getApplicationContext(), "올바르지 않은 요청입니다", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError { //지정한 url로 데이터를 넘기기 위해 사용
                Map<String, String> params=new HashMap<>();
                params.put("userID", userID);//보내줄 데이터의 변수명에 맞춰서 적어줄 것. params.put("php에서 사용되는 변수명", "거기에 넣을 값")
                params.put("userPW", userPW);

                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void onClick(View view)
    {
        if (view==changeRequestButton)
            changePassword();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }
}
