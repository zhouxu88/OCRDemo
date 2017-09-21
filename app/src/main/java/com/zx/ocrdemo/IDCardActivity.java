package com.zx.ocrdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.zx.ocrdemo.utils.FileUtils;
import com.zx.ocrdemo.utils.ToastUtils;

import java.io.File;

/**
 * 身份证识别
 */
public class IDCardActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "IDCardActivity";
    private static final int REQUEST_CODE_CAMERA = 102; //照相机扫描的请求码

    private TextView resultTv; //扫描读取的结果
    private TextView nameTv; //姓名
    private TextView idNumberTv; //身份证号码
    private TextView effectiveDateTv; //有效日期
    private Context mContext;
    private int idType; //身份证类型，0：正面，1：反面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idcard);

        mContext = this;
        initView();
    }

    //初始化View
    private void initView() {
        findViewById(R.id.id_card_front_btn).setOnClickListener(this);
        findViewById(R.id.id_card_front_btn_native).setOnClickListener(this);
        findViewById(R.id.id_card_back_btn).setOnClickListener(this);
        findViewById(R.id.id_card_back_btn_native).setOnClickListener(this);
        resultTv = (TextView) findViewById(R.id.info_text_view);
        nameTv = (TextView) findViewById(R.id.name_tv);
        idNumberTv = (TextView) findViewById(R.id.id_number_tv);
        effectiveDateTv = (TextView) findViewById(R.id.effective_date_tv);
    }

    //调用拍摄身份证正面（不带本地质量控制）activity
    private void scanFront() {
        Intent intent = new Intent(this, CameraActivity.class);
        // 设置临时存储
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplication()).getAbsolutePath());
        //设置扫描的身份证的类型（正面front还是反面back）
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    // 调用拍摄身份证正面（带本地质量控制）activity
    private void scanFrontWithNativeQuality() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplication()).getAbsolutePath());
        //使用本地质量控制能力需要授权
        intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN, OCR.getInstance().getLicense());
        //设置本地质量使用开启
        intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
        //设置扫描的身份证的类型（正面front还是反面back）
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    //调用拍摄身份证反面（不带本地质量控制）activity
    private void scanBack() {
        Intent intent = new Intent(IDCardActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    //调用拍摄身份证反面（带本地质量控制）activity
    private void scanBackWithNativeQuality() {
        Intent intent = new Intent(IDCardActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN,
                OCR.getInstance().getLicense());
        intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE,
                true);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    /**
     * 识别身份证
     *
     * @param idCardSide 正面（front）还是反面（back）
     * @param filePath   文件路径
     */
    private void recIDCard(String idCardSide, String filePath) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);
        // 调用身份证识别服务
        OCR.getInstance().recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(IDCardResult result) {
                // 调用成功，返回IDCardResult对象
                if (result != null) {
                    resultTv.setText(result.toString());
                    Log.i(TAG, "result: " + result.toString());
                    if (idType == 0) {
                        //正面
                        String name = result.getName().toString(); //姓名
                        String gender = result.getGender().toString(); //性别
                        String ethnic = result.getEthnic().toString(); //民族
                        String birthday = result.getBirthday().toString(); //出生日期
                        String address = result.getAddress().toString(); //居住地址
                        String idNumber = result.getIdNumber().toString(); //身份证号码
                        Log.i(TAG, "name:----------->" + name);
                        Log.i(TAG, "gender:----------->" + gender);
                        Log.i(TAG, "ethnic:----------->" + ethnic);
                        Log.i(TAG, "birthday:----------->" + birthday);
                        Log.i(TAG, "address:----------->" + address);
                        Log.i(TAG, "idNumber:----------->" + idNumber);
                        nameTv.setText(name);
                        idNumberTv.setText(idNumber);
                    } else {
                        //反面
                        String signDate = result.getSignDate().toString(); //签发日期
                        String expiryDate = result.getExpiryDate().toString(); //截止日期
                        String issueAuthority = result.getIssueAuthority().toString();//签发机关 
                        Log.i(TAG, "signDate:----------->" + signDate);
                        Log.i(TAG, "expiryDate:----------->" + expiryDate);
                        Log.i(TAG, "issueAuthority:----------->" + issueAuthority);
                        effectiveDateTv.setText(signDate + "~" + expiryDate);
                    }
                }
            }

            @Override
            public void onError(OCRError error) {
                // 调用失败，返回OCRError对象
                Log.i(TAG, "onError: " + error.getMessage());
                ToastUtils.showToast(mContext, error.getMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_card_front_btn:
                //身份证正面
                idType = 0;
                scanFront();
                break;
            case R.id.id_card_front_btn_native:
                //身份证正面(本地质量控制)
                idType = 0;
                scanFrontWithNativeQuality();
                break;
            case R.id.id_card_back_btn:
                //身份证反面
                idType = 1;
                scanBack();
                break;
            case R.id.id_card_back_btn_native:
                //身份证反面(本地质量控制)
                idType = 1;
                scanBackWithNativeQuality();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //如果拍摄类型是身份证
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath();
                if (!TextUtils.isEmpty(contentType)) {
                    //判断是身份证正面还是反面
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                    }
                }
            }
        }
    }
}
