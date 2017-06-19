package kr.ac.kumoh.s20140350.movienow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
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

public class checkPasswordActivity extends AppCompatActivity implements View.OnClickListener{
//withdrwalReadyActivity랑 동일한 구성. 이 다음으로 넘어가는 activity만 다를 뿐

    private Button checkPasswordButton;
    private EditText passwordText;
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
        setContentView(R.layout.activity_check_password);

        checkPasswordButton=(Button)findViewById(R.id.passCheckButton);
        passwordText=(EditText) findViewById(R.id.password);
        progressDialog=new ProgressDialog(this);
        passwordText.setFilters(new InputFilter[] {filterPass});
        checkPasswordButton.setOnClickListener(this);
    }

    private void checkPassword()
    {
        final String userID=SharedPrefManager.getInstance(this).getUserID();
        final String userPW=passwordText.getText().toString().trim();
        progressDialog.setMessage("잠시만 기다려 주세요..");
        progressDialog.show();
        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                Constants.URL_PASSCHECK,
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
                                    Toast.makeText(getApplication(), "비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(getApplication(), "회원 확인 완료", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), changePasswordActivity.class));
                                    finish();
                                    break;
                                case 2:
                                    Toast.makeText(getApplication(), "올바르지 않은 요청", Toast.LENGTH_SHORT).show();
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
                Log.e("id", userID);
                Log.e("pw", userPW);
                params.put("userID", userID);
                params.put("userPW", userPW);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }

    public void onClick(View view)
    {
        if (view==checkPasswordButton)
            checkPassword();
    }
}
