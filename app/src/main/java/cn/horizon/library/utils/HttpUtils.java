package cn.horizon.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.horizon.library.activity.MainActivity;
import cn.horizon.library.bean.BookDetailInfo;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by horizon on 4/3/2017.
 */

public class HttpUtils {

    //获取登录的authorization信息
    public static HashMap<String, String> getAuthorization(final String url, final String username,
                                                           final String password, final Handler mHandler) {
        final HashMap<String, String> map = new HashMap<String, String>();
        //网络请求是耗时操作，需要在子线程中执行
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setConnectTimeout(8000);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Accept", "application/json");
//                    connection.setRequestProperty("client_id","WXClientId");
                    DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                    //拼接链接，在用户输入正确的用户名和密码的情况下进行授权登录
                    out.writeBytes("grant_type=password&username=" + username + "&password=" + password + "&client_id=WXClientId");

                    //获取状态码
//                    MainActivity.RESPONSE_CODE = connection.getResponsecode();
                    Log.i("ResponseMessage", "run: " + connection.getResponseMessage().toString());
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    Message msg = new Message();
                    msg.what = MainActivity.SHOW_RESPONSE;
                    //将服务器返回的结果放到message中
                    String result = MainActivity.parseJSONWithJSONObiect(builder.toString());
                    if (result.length() < 10) {
                        //返回错误信息
                        msg.obj = result;
                        mHandler.sendMessage(msg);

                    } else {
                        //返回token_type
                        String[] results = result.split(" ");
                        MainActivity.ACCESS_TOKEN = results[0];
                        String token_type = results[1];

                        //将数据返回

                        map.put("access_token", results[0]);
                        map.put("token_type", results[1]);
//                    login(MainActivity.GET_RESPONSE_URL, access_token, mHandler);
//
                        msg.obj = token_type;
                        mHandler.sendMessage(msg);

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();

        return map;
    }


    public static void login(final String url, final String authorization, final Handler mHandler) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                HttpURLConnection connection = null;
                try {

                    connection = (HttpURLConnection) new URL(url).openConnection();     //实例化一个HttpURLConnection
                    connection.setRequestMethod("GET");                                 //设置请求方法
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
//                    connection.setRequestProperty("Accept","application/json");         //设置请求头
                    connection.setRequestProperty("Authorization", "Bearer " + authorization);       //设置认证权限
//                    connection.setRequestProperty("http","//122.112.226.254/api/Test");

                    InputStream in = connection.getInputStream();                       //获取响应流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder builder = new StringBuilder();

                    String line = " ";                                                  //从缓存器中将数据读出来
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    Message msg = new Message();
                    msg.what = MainActivity.GET_RESPONSE;
                    if (builder != null) {
                        msg.obj = builder.toString();
                        mHandler.sendMessage(msg);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (connection != null) {
                    connection.disconnect();
                }
            }
        }.start();

    }


    /**
     * 进行登录，并将获取的access_token保存
     *
     * @param etUsername
     * @param etPassword
     * @param context
     * @param mHandler
     */
    public static void okhttp_login(TextView etUsername, TextView etPassword, final Context context, final Handler mHandler) {
        final String url = "https://eighthundred.cn/OAuth/Token";
        final HashMap<String, String> map = new HashMap<String, String>();
        //'Accept: application/json' -d 'grant_type=password&username=admin&password=123qwe&client_id=WXClientId'
        final String username = etUsername.getText().toString();
        final String password = etPassword.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (username.equals("admin") && password.equals("123qwe")) {
                    OkHttpUtils.post().url(url)
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("Accept", "application/json")
                            .addParams("username", username)
                            .addParams("password", password)
                            .addParams("grant_type", "password")
                            .addParams("client_id", "WXClientId")
                            .build()
                            .execute(new StringCallback() {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    Toast.makeText(context, "无法获取响应", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onResponse(String response, int id) {
                                    if (!TextUtils.isEmpty(response)) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(response);
                                            String access_token = jsonObject.getString("access_token");
                                            String token_type = jsonObject.getString("token_type");
                                            int expires_in = jsonObject.getInt("expires_in");
                                            String refresh_token = jsonObject.getString("refresh_token");

//                                tv_hello.setText("access_token: " + access_token + '\n'
//                                        + "token_type: " + token_type + '\n'
//                                        + "expires_in: " + expires_in + '\n'
//                                        +"refresh_token: " + refresh_token + '\n');
                                    /*ActivityOptionsCompat oc2 = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                                    Intent i2 = new Intent(MainActivity.this,LoginSuccessActivity.class);
                                    context.startActivity(i2, oc2.toBundle());

                                    finish();*/

                                            map.put("access_token", access_token);
                                            map.put("refresh_token", refresh_token);

                                            Message msg = new Message();
                                            msg.what = 1;
                                            msg.obj = map;
                                            mHandler.sendMessage(msg);

                                            Toast.makeText(context, "登录成功", Toast.LENGTH_SHORT).show();

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            });
                } else {
                    //用户名或密码有误
                    Toast.makeText(context, "用户名或密码有误", Toast.LENGTH_SHORT).show();

                }
            }
        }).start();

    }

    /**
     * 使用获得的access_token进行之后操作的授权
     */
    public static void okhttp_authorization(final Context context, final String access_token, final String qrInfo, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtils.postString().url("https://eighthundred.cn/api/Borrow")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + access_token)
                        .mediaType(MediaType.parse("application/json; charset=utf-8"))
                        .content("[\"" + qrInfo + "\"]")
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e("Authorization", "onError: " + e.getMessage());

                            }

                            @Override
                            public void onResponse(String response, int id) {
                                if (!TextUtils.isEmpty(response)) {
                                    try {
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        JSONObject result = jsonObject.getJSONObject("result");


                                        String encryptedCacheKey = result.getString("encryptedCacheKey");
                                        if (!TextUtils.isEmpty(encryptedCacheKey)) {
                                            Message msg = new Message();
                                            HashMap<String, String> map = new HashMap<String, String>();
                                            msg.what = 2;
                                            map.put("encryptedCacheKey", encryptedCacheKey);
                                            msg.obj = map;
                                            mHandler.sendMessage(msg);
                                            //  Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                            MainActivity.ENCRYPTEDCACHEKEY = encryptedCacheKey;
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
            }
        }).start();

    }

    /**
     * 借书
     * 获取书籍的详细信息以及userId
     */
    public static void okhttp_get_book_details(final Context context, final String encryptedCacheKey, final String access_token, final Handler mHandler) {
        new Thread(new Runnable() {
            String url = "https://eighthundred.cn/api/Admin/Borrow/" + encryptedCacheKey;

            @Override
            public void run() {
                Log.i("url", "url: " + url);
                OkHttpUtils.get().url(url)
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + access_token)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e("UserId", "onError: " + e.toString());
                                Toast.makeText(context, "错误信息" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onResponse(String response, int id) {

                                if (!TextUtils.isEmpty(response)) {
                                    try {
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        List<BookDetailInfo> bookList = new ArrayList<BookDetailInfo>();
                                        JSONObject result = jsonObject.getJSONObject("result");
                                        JSONArray userBorrowList = result.getJSONArray("userBorrowList");

                                        //TODO 由于读者会一次性借阅多本书，所有需要处理多本书的详细信息，
                                        //TODO 需要使用BookDetailInfo 类，来保存每本书的详细信息，将BookDetailInfo
                                        //TODO 对象保存在list中。

                                        for (int i = 0; i < userBorrowList.length(); i++) {
                                            JSONObject borrowInfo = userBorrowList.getJSONObject(i);
                                            int state = borrowInfo.getInt("state");
                                            //需要
                                            //书籍id
                                            String bookId = borrowInfo.getString("id");
                                            String author = borrowInfo.getString("author");
                                            String title = borrowInfo.getString("title");
                                            double price = borrowInfo.getDouble("price");
                                            int deposit = borrowInfo.getInt("deposit");
                                            // String refresh_token = result.getString("refresh_token");

                                            //获得userId，需要
                                            String userId = result.getString("userId");

                                            BookDetailInfo bookDetailInfo = new BookDetailInfo();
                                            bookDetailInfo.setId(bookId);
                                            bookDetailInfo.setAuthor(author);
                                            bookDetailInfo.setTitle(title);
                                            bookDetailInfo.setPrice(price);
                                            bookDetailInfo.setDeposit(deposit);
                                            bookDetailInfo.setUserId(userId);
                                            bookDetailInfo.setState(state);

                                            bookList.add(bookDetailInfo);
                                            Log.i("Get_Book_Details", "onResponse: " + userId);
//                                            Toast.makeText(context, "书的数量： " + bookList.size(), Toast.LENGTH_SHORT).show();

                                        }

                                        Message msg = new Message();
                                        msg.what = 3;
                                        msg.obj = bookList;
                                        mHandler.sendMessage(msg);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                    Log.i("Get_Book_Details", "Success");
                                }
                            }
                        });
            }
        }).start();

    }

    /**
     * 借书
     * 获取支付二维码
     */
    public static void okhttp_get_payQRUrl(final String userId, final String bookIds,
                                           final String access_token, final Context context, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "https://eighthundred.cn/api/Admin/Borrow?" + "userId=" + userId;
                //https://eighthundred.cn/api/Admin/Borrow?userId=1
                Log.e("QR_url", "QR_url: " + url);
                OkHttpUtils.postString().url(url)
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + access_token)
                        .mediaType(MediaType.parse("application/json; charset=utf-8"))
                        .content("[" + bookIds + "]")
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Toast.makeText(context, "Failure: " + e.toString(), Toast.LENGTH_SHORT).show();
                                Log.e("QR_Url", "onError: " + e.getMessage());
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                if (!TextUtils.isEmpty(response)) {
                                    try {
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        JSONObject result = jsonObject.getJSONObject("result");

                                        boolean isSuccess = result.getBoolean("isSuccess");
                                        String payQRUrl = result.getString("payQRUrl");
                                        double payTotal = result.getDouble("payTotal");

//                                        tv_hello.setText("isSuccess: " + isSuccess + '\n'
//                                                + "payQRUrl: " + payQRUrl + '\n'
//                                                + "payTotal: " + payTotal + '\n'
//
//                                        );
//                                        Toast.makeText(context, "payQRUrl: " + payQRUrl, Toast.LENGTH_SHORT).show();
                                        Message msg = new Message();
                                        msg.what = 4;
                                        msg.obj = payQRUrl;

                                        mHandler.sendMessage(msg);


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }).start();


    }

    public static void okhttp_show_qrImage(final Context context, final String url, final ImageView iv_pay_image, final Handler mHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpUtils.get().url(url)
                        .build()
                        .execute(new BitmapCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {

                                Toast.makeText(context, "Failure: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.e("show_qr_image", "onError: " + e.getMessage());

                            }

                            @Override
                            public void onResponse(Bitmap response, int id) {
                                Message msg = new Message();
                                msg.what = 5;
                                msg.obj = response;
                                mHandler.sendMessage(msg);
                                Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        }).run();


    }

    /**
     * 借书
     * 将读者的借书信息保存
     *
     * @param context
     * @param bookIds      读者借阅书籍id
     * @param access_token 授权口令
     * @param userId       读者口令
     */
    public static void okhttp_put_borrow_info(final Context context, final String bookIds, final String access_token, String userId) {
        final String url = "https://eighthundred.cn/api/Admin/Borrow?userId=" + userId;
        new Thread(new Runnable() {
            MediaType MEDIA_TYPE_PLAIN = MediaType.parse("application/json;charset=utf-8");
            String bookList = "[" + bookIds + "]";

            @Override
            public void run() {
                OkHttpUtils.put().url(url)
//                        .addHeader("Content-Type", "application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + access_token)
                        .requestBody(RequestBody.create(MEDIA_TYPE_PLAIN, bookList))
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Toast.makeText(context, "错误信息：" + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onResponse(String response, int id) {
                                if (TextUtils.isEmpty(response)) {
                                    Toast.makeText(context, "借书成功！", Toast.LENGTH_SHORT).show();


                                }
                            }
                        });
            }
        }).start();


    }


    /**
     * 还书
     *
     * @param context
     * @param encryptedCacheKey
     * @param access_token
     * @param mHandler
     */
    public static void okhttp_get_borrow_info(final Context context, final String encryptedCacheKey, final String access_token, final Handler mHandler) {
        new Thread(new Runnable() {
            String url = "https://eighthundred.cn/api/Admin/Return/" + encryptedCacheKey;

            @Override
            public void run() {
                Log.i("url", "url: " + url);
                OkHttpUtils.get().url(url)
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + access_token)
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.e("UserId", "onError: " + e.toString());

                            }

                            @Override
                            public void onResponse(String response, int id) {

                                if (!TextUtils.isEmpty(response)) {
                                    try {
                                        JSONObject jsonObject = null;
                                        try {
                                            jsonObject = new JSONObject(response);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        Map<String, String> map = new HashMap<String, String>();
                                        JSONObject result = jsonObject.getJSONObject("result");
                                        JSONArray bookReturnList = result.getJSONArray("bookReturnList");

                                        //书籍详细信息
                                        List<BookDetailInfo> bookList = new ArrayList<BookDetailInfo>();
                                        for (int i = 0; i < bookReturnList.length(); i++) {
                                            JSONObject bookDetail = bookReturnList.getJSONObject(i);
                                            String bookTitle = bookDetail.getString("bookTitle");
                                            String bookBriefId = bookDetail.getString("bookBriefId");
                                            String bookId = bookDetail.getString("bookId");
                                            int deposit = bookDetail.getInt("deposit");
                                            String borrowTime = bookDetail.getString("borrowTime");

                                            //读者信息
                                            JSONObject userInfo = result.getJSONObject("userInfo");
                                            String userId = userInfo.getString("id");
                                            String nickName = userInfo.getString("nickName");

                                            BookDetailInfo bookDetailInfo = new BookDetailInfo();
                                            bookDetailInfo.setTitle(bookTitle);
                                            bookDetailInfo.setId(bookBriefId);
                                            bookDetailInfo.setSearchId(bookId);
                                            bookDetailInfo.setDeposit(deposit);
                                            bookDetailInfo.setBorrowTime(borrowTime);
                                            bookDetailInfo.setUserId(userId);
                                            bookDetailInfo.setNickName(nickName);

                                            bookList.add(bookDetailInfo);
                                        }

                                        Message msg = new Message();
                                        msg.what = 0;
                                        msg.obj = bookList;
                                        mHandler.sendMessage(msg);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
                                    Log.i("Get_Book_Details", "Success");
                                }
                            }
                        });
            }
        }).start();
    }

    /**
     * 还书
     *
     * @param context
     * @param bookIds
     * @param access_token
     * @param userId
     */
    public static void okhttp_put_return_info(final Context context, final String bookIds, final String access_token, final String userId) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                MediaType MEDIA_TYPE_PLAIN = MediaType.parse("application/json;charset=utf-8");
                String url = "https://eighthundred.cn/api/Admin/Return?uId=" + userId;
                String bookList = "[" + bookIds + "]";
                OkHttpUtils.put().url(url)
//                        .addHeader("Content-Type","application/json")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer " + access_token)
                        .requestBody(RequestBody.create(MEDIA_TYPE_PLAIN, bookList))
                        .build()
                        .execute(new StringCallback() {
                            @Override
                            public void onError(Call call, Exception e, int id) {
                                Log.i("okhttp_put_return_info", "onError: " + e.getMessage());
                                Toast.makeText(context, "错误信息：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(String response, int id) {
                                Log.i("okhttp_put_return_info", "onResponse: " + response);
//                                Toast.makeText(context, "response: " + response, Toast.LENGTH_SHORT).show();
                                JSONObject jsonObject = null;
                                try {

                                    jsonObject = new JSONObject(response);
                                    JSONObject result = jsonObject.getJSONObject("result");
                                    boolean isSuccess = result.getBoolean("isSuccess");
                                    if (isSuccess) {
                                        Toast.makeText(context, "还书成功！", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(context, "还书失败！" + isSuccess, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        });

            }
        }.start();

    }
}
