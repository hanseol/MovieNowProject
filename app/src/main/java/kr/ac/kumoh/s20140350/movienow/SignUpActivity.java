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
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.util.Vector;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{


    private EditText editTextUsername, editTextPassword, editTextCheckPassword;
    private Button buttonRegister, buttonDupIDCheck;
    private ProgressDialog progressDialog;
    private int isCheckDupID=0;
    private TextView changeLogin;

    private Spinner actionSpinner;
    private Spinner adultSpinner;
    private Spinner adventureSpinner;
    private Spinner animationSpinner;
    private Spinner comedySpinner;
    private Spinner crimeSpinner;
    private Spinner musicalSpinner;
    private Spinner documentarySpinner;
    private Spinner dramaSpinner;
    private Spinner familySpinner;
    private Spinner fantasySpinner;
    private Spinner historySpinner;
    private Spinner horrorSpinner;
    private Spinner melo_romanceSpinner;
    private Spinner mysterySpinner;
    private Spinner sfSpinner;
    private Spinner thrillerSpinner;
    private Spinner warSpinner;
    private Spinner westernSpinner;
    private Spinner etcSpinner;
    private CheckBox recommendAllowBox;


    private Vector<Integer> genreValue=new Vector<Integer>();
    private Spinner genreValArr[]= new Spinner[20];

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
        setContentView(R.layout.activity_signup);

        if (SharedPrefManager.getInstance(this).isLoggedIn())
        {
            startActivity(new Intent(this, FirstActivity.class));
            Toast.makeText(getApplicationContext(), "이미 로그인 되어 있습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editTextUsername=(EditText) findViewById(R.id.UserName);
        editTextPassword=(EditText) findViewById(R.id.Password);
        editTextCheckPassword=(EditText) findViewById(R.id.checkPassword);
        buttonRegister=(Button) findViewById(R.id.RegisterRequest);
        buttonDupIDCheck=(Button) findViewById(R.id.DupIDCheck);
        recommendAllowBox=(CheckBox)  findViewById(R.id.Recommendation);

        changeLogin=(TextView) findViewById(R.id.changeLogin);

        //선호장르 박스들 뷰 저장

        actionSpinner=(Spinner)findViewById(R.id.spinner_Action);
        adultSpinner=(Spinner)findViewById(R.id.spinner_Adult);
        adventureSpinner=(Spinner)findViewById(R.id.spinner_Adventure);
        animationSpinner=(Spinner)findViewById(R.id.spinner_Animation);
        comedySpinner=(Spinner)findViewById(R.id.spinner_Comedy);
        crimeSpinner=(Spinner)findViewById(R.id.spinner_Crime);
        musicalSpinner=(Spinner)findViewById(R.id.spinner_Musical);
        documentarySpinner=(Spinner)findViewById(R.id.spinner_Documentary);
        dramaSpinner=(Spinner)findViewById(R.id.spinner_Drama);
        familySpinner=(Spinner)findViewById(R.id.spinner_Family);
        fantasySpinner=(Spinner)findViewById(R.id.spinner_Fantasy);
        historySpinner=(Spinner)findViewById(R.id.spinner_History);
        horrorSpinner=(Spinner)findViewById(R.id.spinner_Horror);
        melo_romanceSpinner=(Spinner)findViewById(R.id.spinner_Melo_romance);
        mysterySpinner=(Spinner)findViewById(R.id.spinner_Mystery);
        sfSpinner=(Spinner)findViewById(R.id.spinner_SF);
        thrillerSpinner=(Spinner)findViewById(R.id.spinner_Thriller);
        warSpinner=(Spinner)findViewById(R.id.spinner_War);
        westernSpinner=(Spinner)findViewById(R.id.spinner_Western);
        etcSpinner=(Spinner)findViewById(R.id.spinner_Etc);


        genreValArr[0]=sfSpinner;
        genreValArr[1]=crimeSpinner;
        genreValArr[2]=actionSpinner;
        genreValArr[3]=melo_romanceSpinner;
        genreValArr[4]=dramaSpinner;
        genreValArr[5]=comedySpinner;
        genreValArr[6]=animationSpinner;
        genreValArr[7]=fantasySpinner;
        genreValArr[8]=adventureSpinner;
        genreValArr[9]=thrillerSpinner;
        genreValArr[10]=familySpinner;
        genreValArr[11]=documentarySpinner;
        genreValArr[12]=warSpinner;
        genreValArr[13]=mysterySpinner;
        genreValArr[14]=adultSpinner;
        genreValArr[15]=horrorSpinner;
        genreValArr[16]=musicalSpinner;
        genreValArr[17]=historySpinner;
        genreValArr[18]=westernSpinner;
        genreValArr[19]=etcSpinner;

        //아답터 생성
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.value_arr, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner

        //아답터 적용
        for (int i=0; i<20; i++)
        {
            genreValArr[i].setAdapter(adapter);
        }

        progressDialog=new ProgressDialog(this);

        buttonRegister.setOnClickListener(this);
        buttonDupIDCheck.setOnClickListener(this);
        changeLogin.setOnClickListener(this);

        //아이디는 영어와 숫자만, 비밀번호는 영어와 숫자와 특문만 가능하게 필터 설정
        editTextUsername.setFilters(new InputFilter[] {filter});;
        editTextPassword.setFilters(new InputFilter[] {filterPass});
        editTextCheckPassword.setFilters(new InputFilter[] {filterPass});

        //etcSpinner.setSelection(2);

        //Toast.makeText(this, SharedPrefManager.getInstance(this).getToken(), Toast.LENGTH_SHORT).show();

        recommendAllowBox.setChecked(true);
    }

    //회원가입 버튼 눌렀을 시 실행되는 함수--------------------------------------------------------------
    private void registerUser() {

        //입력한 값들의 길이 확인
        if ((editTextUsername.getText().toString().length()<5) || (editTextUsername.getText().toString().length()>10))
        {
            Toast.makeText(getApplicationContext(), "아이디는 5~10자여야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((editTextPassword.getText().toString().length()<4) || (editTextPassword.getText().toString().length()>10))
        {
            Toast.makeText(getApplicationContext(), "비밀번호는 4~10자여야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        if ((editTextCheckPassword.getText().toString().length()<4) || (editTextCheckPassword.getText().toString().length()>10))
        {
            Toast.makeText(getApplicationContext(), "비밀번호는 4~10자여야 합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        //비밀번호와 비밀번호 확인 체크

        if (!editTextPassword.getText().toString().trim().equals(editTextCheckPassword.getText().toString().trim()))
        {
            Toast.makeText(getApplicationContext(), "비밀번호와 비밀번호 확인이 일치하지 않습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        String tempUserToken="null";
        //토큰 생성 여부 확인---------------------------------------
        if (SharedPrefManager.getInstance(this).getToken()==null)
        {
            Toast.makeText(this, "토큰이 생성되지 않았습니다", Toast.LENGTH_SHORT).show();

        }
        else
        {
            tempUserToken=SharedPrefManager.getInstance(getApplicationContext()).getToken();
        }
        final String userToken=tempUserToken;
        final String userID=editTextUsername.getText().toString().trim();
        final String userPW=editTextPassword.getText().toString().trim();

        //중복 아이디 체크를 하지 않았다면
        if (isCheckDupID==0)
        {
            Toast.makeText(getApplicationContext(), "아이디 중복 체크를 해주세요", Toast.LENGTH_SHORT).show();
        }
        else if (isCheckDupID==1)
        {
            //중복 체크는 했는데 이게 중복된 아이디라면
            Toast.makeText(getApplicationContext(), "다른 아이디를 사용하세요. 중복체크는 하셨나요?", Toast.LENGTH_SHORT).show();
        }
        else if (isCheckDupID==2)//중복되지 않은 ID라면
        {
            final String recommendAllow=Boolean.toString(recommendAllowBox.isChecked());
            progressDialog.setMessage("잠시만 기다려 주세요..");
            progressDialog.show();

            //회원가입 재시도 시 기존의 벡터를 지우지 않으면 크기가 계속 늘어나는 단점
            genreValue.removeAllElements();
            //선호 장르 얻어오기(가중치)

            for (int i=0; i<20; i++)
            {
                genreValue.add(genreValArr[i].getSelectedItemPosition());
                //Log.e(Integer.toString(i), Integer.toString(genreValArr[i].getSelectedItemPosition()));
            }

            StringRequest stringRequest= new StringRequest(Request.Method.POST,
                    Constants.URL_REGISTER,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            String responseMsg;
                            //응답이 json 형식으로 넘어오므로 이에 대한 처리
                            try//회원가입 스크립트에서는 응답 코드 형식으로 데이터를 구성하였으므로 코드에 맞는 응답으로 변환
                            {
                                JSONObject jsonObject=new JSONObject(response);
                                switch (jsonObject.getInt("rescode"))
                                {
                                    case 1:
                                        responseMsg=new String("정상적으로 가입되었습니다");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
                                        finish();
                                        break;
                                    case 2:
                                        responseMsg=new String("회원 가입 도중 오류 발생. 다시 시도해 주세요");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        break;
                                    case 3:
                                        responseMsg=new String("이미 동일한 아이디가 있습니다");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        break;
                                    case 4:
                                        responseMsg=new String("아이디와 비밀번호를 모두 채워주세요");
                                        responseMsg+=" "+jsonObject.getString("id") + " "+ jsonObject.getString("pw");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        break;
                                    case 5:
                                        responseMsg=new String("잘못된 요청입니다");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        break;
                                    case 6:
                                        responseMsg=new String("선호 장르 생성 도중 오류가 발생했습니다");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        break;
                                    case 7:
                                        responseMsg=new String("예매 기록 생성 도중 오류가 발생했습니다");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                        break;
                                    case 8:
                                        responseMsg=new String("선호 장르 업데이트 도중 오류가 발생했습니다");
                                        Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
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

                    //Vector -> Array
                    Integer genreArr[]=genreValue.toArray(new Integer[genreValue.size()]);
                    Log.e("genreCount", Integer.toString(genreArr.length));
                    params.put("userID", userID);//보내줄 데이터의 변수명에 맞춰서 적어줄 것. params.put("php에서 사용되는 변수명", "거기에 넣을 값")
                    params.put("userPW", userPW);
                    params.put("token", userToken);
                    params.put("recommendation", recommendAllow);
                    for (int i=0; i<genreArr.length; i++)
                    {
                        params.put("value"+i, Integer.toString(genreArr[i]));
                        Log.e("value"+i, Integer.toString(genreArr[i]));
                    }
                    return params;
                }
            };
            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        }

    }


    //중복 아이디 체크
    private void checkDupID()
    {
        if ((editTextUsername.getText().toString().length()<5) || (editTextUsername.getText().toString().length()>10))
        {
            Toast.makeText(getApplicationContext(), "아이디는 5~10자여야 합니다", Toast.LENGTH_LONG).show();
            return;
        }
        final String userID=editTextUsername.getText().toString().trim();
        Log.e("check", userID);
        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                Constants.URL_ID_CHECK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        String responseMsg;
                        //응답이 json 형식으로 넘어오므로 이에 대한 처리
                        try//회원가입 스크립트에서는 응답 코드 형식으로 데이터를 구성하였으므로 코드에 맞는 응답으로 변환
                        {
                            JSONObject jsonObject=new JSONObject(response);
                            switch (jsonObject.getInt("rescode"))
                            {
                                case 1:
                                    responseMsg=new String("사용할 수 있는 아이디입니다");
                                    Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                    isCheckDupID=2;
                                    break;
                                case 2:
                                    responseMsg=new String("이미 사용중인 아이디입니다");
                                    Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                    isCheckDupID=1;
                                    break;
                                case 3:
                                    responseMsg=new String("아이디를 입력하세요");
                                    Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
                                    break;
                                case 4:
                                    responseMsg=new String("유효하지 않은 요청입니다");
                                    Toast.makeText(getApplicationContext(), responseMsg, Toast.LENGTH_LONG).show();
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
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void onClick(View view)
    {
        //등록 버튼을 누르면
        if (view==buttonRegister){
            registerUser(); //회원가입 함수 실행
        }
        else if (view==buttonDupIDCheck)//중복 확인 버튼을 클릭하면
            checkDupID(); //중복 확인 함수 실행
        else if (view==changeLogin)//로그인 텍스트를 누르면
        {
            startActivity(new Intent(this, LoginActivity.class));//로그인 화면으로 이동
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), FirstActivity.class));
        finish();
    }
}