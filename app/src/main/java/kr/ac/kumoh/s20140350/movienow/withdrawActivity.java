package kr.ac.kumoh.s20140350.movienow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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

public class withdrawActivity extends AppCompatActivity implements View.OnClickListener{
//회원가입 실제 실행하는 부분
    private Button withdrawalOK;
    private Button withdrawalCancel;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdraw);
        withdrawalCancel=(Button) findViewById(R.id.Cancel);
        withdrawalOK=(Button) findViewById(R.id.OK);
        withdrawalOK.setOnClickListener(this);
        withdrawalCancel.setOnClickListener(this);
        progressDialog=new ProgressDialog(this);
    }

    private void Withdrawal()
    {
        final String userID=SharedPrefManager.getInstance(this).getUserID();
        progressDialog.setMessage("잠시만 기다려 주세요..");
        progressDialog.show();
        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                Constants.URL_WITHDRAWAL,
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
                                    Toast.makeText(getApplication(), "회원 탈퇴 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(getApplication(), "회원 탈퇴가 정상적으로 완료되었습니다 ㅠ_ㅠ.", Toast.LENGTH_SHORT).show();
                                    SharedPrefManager.getInstance(getApplicationContext()).logout();
                                    startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                                    finish();
                                    break;
                                case 2:
                                    Toast.makeText(getApplication(), "올바르지 않은 요청입니다", Toast.LENGTH_SHORT).show();
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
                params.put("userID", userID);
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }

    public void onClick(View view)
    {
        if (view==withdrawalOK)
        {
            Withdrawal();

        }
        else if (view==withdrawalCancel)
        {
            Toast.makeText(getApplication(), "잘 선택하셨어요!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), FirstActivity.class));
            finish();
        }
    }
}
