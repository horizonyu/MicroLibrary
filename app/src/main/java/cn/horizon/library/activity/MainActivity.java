package cn.horizon.library.activity;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import cn.horizon.library.R;
import cn.horizon.library.utils.HttpUtils;

//import cn.fanrunqi.materiallogin.R;

public class MainActivity extends AppCompatActivity {

    public static final int SHOW_RESPONSE = 0;
    public static final int GET_RESPONSE = 1;
    public static String ACCESS_TOKEN = "";
    public static String ENCRYPTEDCACHEKEY = "";
    private static String access_token;
    private static String token_type;
    private static Map<String, String> map = new HashMap<>();
    private Context mContext;


    private Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    map = (Map<String, String>) msg.obj;
                    handle_response_login(map, mContext);
                    break;
                default:
                    break;
            }

        }
    };


    private void handle_response_login(Map<String, String> map, Context mContext) {
        if (map.size() > 0) {
            //实现跳转
            String refresh_token = map.get("refresh_token");
            String access_token = map.get("access_token");
            ACCESS_TOKEN = access_token;

            Explode explode = new Explode();
            explode.setDuration(500);

            getWindow().setExitTransition(explode);
            getWindow().setEnterTransition(explode);
            ActivityOptionsCompat oc2 = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            Intent i2 = new Intent(mContext.getApplicationContext(), LoginSuccessActivity.class);
            startActivity(i2, oc2.toBundle());
//            startActivity(i2);

            finish();
            //保存登录状态
            saveLoginState();


//            Toast.makeText(getApplicationContext(), access_token + "!", Toast.LENGTH_SHORT).show();
        } else {
            //没有响应结果，不实现跳转

        }
    }

    private void saveLoginState() {
        SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("userName","admin");
        edit.putString("password", "123qwe");
        edit.commit();
    }

    @InjectView(R.id.et_username)
    EditText etUsername;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.bt_go)
    Button btGo;
    @InjectView(R.id.cv)
    CardView cv;
    @InjectView(R.id.fab)
    FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mContext = this;

        //获取登录状态
//        getLoginState();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getLoginState() {
        SharedPreferences sp = getSharedPreferences("user",Context.MODE_PRIVATE);
        String userName = sp.getString("userName", "");
        //如果已经登录过，则直接进入，不用再次登录
        if (!TextUtils.isEmpty(userName)){
            Explode explode = new Explode();
            explode.setDuration(500);

            getWindow().setExitTransition(explode);
            getWindow().setEnterTransition(explode);
            ActivityOptionsCompat oc2 = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            Intent i2 = new Intent(mContext.getApplicationContext(), LoginSuccessActivity.class);
            startActivity(i2, oc2.toBundle());
        }
    }


    // 设置按钮的点击事件
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @OnClick({R.id.bt_go, R.id.fab})
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fab:
                getWindow().setExitTransition(null);
                getWindow().setEnterTransition(null);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options =
                            ActivityOptions.makeSceneTransitionAnimation(this, fab, fab.getTransitionName());
                    startActivity(new Intent(this, RegisterActivity.class), options.toBundle());
                } else {
                    startActivity(new Intent(this, RegisterActivity.class));
                }
                break;
            case R.id.bt_go:
                HttpUtils.okhttp_login(etUsername, etPassword, getApplicationContext(), mHandler);
                break;
        }
    }
    /**
     * @param response 解析Json数据，并将指定的数据返回
     * @return
     */
    public static String parseJSONWithJSONObiect(String response) {

        try {

            JSONObject jsonObect = new JSONObject(response);
//            error = jsonObect.getString("error");

            //如果出现错误信息，则直接返回错误提示
//            if(!error.isEmpty()){
//                errorDescription = jsonObect.getString("error_description");
//                return errorDescription;
//            }
//            //否则，获取需要的信息
//            else {
            //否则，获取需要的信息
            access_token = jsonObect.getString("access_token");
            token_type = jsonObect.getString("token_type");

            String expires_in = jsonObect.getString("expires_in");
            String refresh_token = jsonObect.getString("refresh_token");

            Log.d("MainActivity", "access_token: " + access_token);
            Log.d("MainActivity", "token_type: " + token_type);
            Log.d("MainActivity", "expires_in: " + expires_in);
            Log.d("MainActivity", "refresh_token: " + refresh_token);
//            }

        } catch (JSONException e) {
            e.printStackTrace();

        }
        String result = access_token + " " + token_type;
        return result;
    }
}
