package kr.ac.kumoh.s20140350.movienow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
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

public class ChangeRefActivity extends AppCompatActivity implements View.OnClickListener {

    final String userID=SharedPrefManager.getInstance(this).getUserID();

    private Button changePrefReqButton;

    private CheckBox recommendAllowBox;
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
    private ProgressDialog progressDialog;
    private Vector<Integer> genreValue=new Vector<Integer>();
    private Spinner genreValArr[]= new Spinner[20];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_ref);

        changePrefReqButton=(Button) findViewById(R.id.ChangeReq);

        recommendAllowBox=(CheckBox)  findViewById(R.id.Recommendation);
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

        //서버에서 사용자 선호 장르 요청해서 받아온 후 표시
        progressDialog.setMessage("잠시만 기다려 주세요..");
        progressDialog.show();

        StringRequest stringRequest= new StringRequest(
                Request.Method.POST,
                Constants.URL_PREFCHECK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try
                        {
                            JSONObject obj=new JSONObject(response);
                            //응답으로 넘어온 데이터 값 중 success field의 값을 비교해서
                            if(obj.getBoolean("success"))//성공했으면
                            {
                                //추천 여부 확인 후 표시
                                if (obj.getInt("recommendation")!=0)
                                {
                                    recommendAllowBox.setChecked(true);
                                }
                                //선호 장르들 확인 후 표시
                                for (int i=0; i<20; i++)
                                {
                                    genreValArr[i].setSelection(obj.getInt("value"+i));
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), "선호 장르와 추천 사용 여부 조회 중 문제가 발생했습니다", Toast.LENGTH_SHORT).show();
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
                return params;
            }
        };
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        changePrefReqButton.setOnClickListener(this);
    }

    private void ChangeRequest()
    {
        progressDialog.setMessage("잠시만 기다려 주세요..");
        progressDialog.show();

        final String recommendAllow=Boolean.toString(recommendAllowBox.isChecked());

        genreValue.removeAllElements();
        //선호 장르 얻어오기(가중치)

        for (int i=0; i<20; i++)
        {
            genreValue.add(genreValArr[i].getSelectedItemPosition());
            //Log.e(Integer.toString(i), Integer.toString(genreValArr[i].getSelectedItemPosition()));
        }

        StringRequest stringRequest= new StringRequest(Request.Method.POST,
                Constants.URL_PREFCHANGE,
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
                                    Toast.makeText(getApplication(), "선호 장르 업데이트 도중 문제 발생", Toast.LENGTH_SHORT).show();
                                    break;
                                case 1:
                                    Toast.makeText(getApplication(), "정상 처리되었습니다", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), FirstActivity.class));
                                    finish();
                                    break;
                                case 2:
                                    Toast.makeText(getApplication(), "추천 여부 업데이트 도중 문제 발생", Toast.LENGTH_SHORT).show();
                                    break;
                                case 3:
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

                //Vector -> Array
                Integer genreArr[]=genreValue.toArray(new Integer[genreValue.size()]);

                Log.e("userID", userID);
                params.put("userID", userID);//보내줄 데이터의 변수명에 맞춰서 적어줄 것. params.put("php에서 사용되는 변수명", "거기에 넣을 값")
                params.put("recommendation", recommendAllow);
                for (int i=0; i<genreArr.length; i++)
                {
                    params.put("value"+i, Integer.toString(genreArr[i]));
                }
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
        if (view==changePrefReqButton)
        {
            ChangeRequest();
        }
    }

}