package com.haui.activity;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haui.fragment.LoginFragment;
import com.haui.fragment.ResetPassFragment;
import com.haui.fragment.SiginFragment;
import com.haui.log.Log;
import com.haui.object.Location;
import com.haui.object.SinhVien;
import com.haui.object.User;
import com.haui.task.ParserSinhVien;

import xyz.santeri.wvp.WrappingFragmentPagerAdapter;
import xyz.santeri.wvp.WrappingViewPager;

import static com.haui.activity.R.string.reset_pass_text;

/**
 * Created by Faker on 8/14/2016.
 */
public class LoginActivity extends AppCompatActivity implements ValueEventListener{
    private TabLayout tabLayout;
    private WrappingViewPager wrappingViewPager;
    private LoginFragment loginFragment;
    private SiginFragment siginFragment;
    private ResetPassFragment resetPassFragment;
    /**
     * Firebase
     */
    private DatabaseReference database;
    private Log log;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        database = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.login_sigin_layout);
        creatTabLoginSigin();
    }

    /**
     * khởi tạo mới một user và gửi lên database firebase
     * @param maSV
     * @param pass
     * @param tenSV
     * @param tenLopDL
     * @param soDT
     * @param img
     */
    public void writeNewUser( String maSV,String pass, String tenSV, String tenLopDL,
                             String soDT ,String img) {
        User user = new User(maSV,pass,tenSV,tenLopDL,soDT,img,new Location("",""),"...");
        database.child("users").child(maSV).setValue(user); // gửi một đối tượng lên firebase vói child là những node cha
    }
    @Override
    public void onBackPressed() {
        Intent returnIntent = new Intent(LoginActivity.this,NavigationActivity.class);
        setResult(RESULT_CANCELED,returnIntent);
        finish();
    }
    private void creatTabLoginSigin() {
        log=new Log(this);
        wrappingViewPager= (WrappingViewPager) findViewById(R.id.login_sigin_viewPager);
        loginFragment=new LoginFragment();
        siginFragment=new SiginFragment();
        resetPassFragment=new ResetPassFragment();
        wrappingViewPager.setAdapter(new WrappingFragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0:
                        return loginFragment;
                    case 1:
                        return siginFragment;
                    case 2:
                    default:
                        return resetPassFragment;
                }

            }
            @Override
            public int getCount() {
                return 3;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position){
                    case 0:
                        return getResources().getString(R.string.login_text);
                    case 1:
                        return getResources().getString(R.string. sigin_text);
                    case 2:
                    default:
                        return getResources().getString(reset_pass_text);
                }
            }
        });
        tabLayout = (TabLayout) findViewById(R.id.login_sigin_tablayout);
        if (tabLayout != null) {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
            tabLayout.setTabTextColors(Color.WHITE,getResources().getColor(R.color.colorPrimary));
            tabLayout.setSelectedTabIndicatorHeight(10);
            tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorPrimary));
            tabLayout.setupWithViewPager(wrappingViewPager);
            wrappingViewPager.setCurrentItem(1);
            onSelectTab(1);
            tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    onSelectTab(tab.getPosition());
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }
                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });
        }
    }
    private void onSelectTab(int i) {
        switch (i){
            case 0:
                tabLayout.getTabAt(0).setIcon(R.drawable.tab_login_1);
                tabLayout.getTabAt(1).setIcon(R.drawable.tab_register_0);
                tabLayout.getTabAt(2).setIcon(R.drawable.tab_refresh_0);
                break;
            case 1:
                tabLayout.getTabAt(0).setIcon(R.drawable.tab_login_0);
                tabLayout.getTabAt(1).setIcon(R.drawable.tab_register_1);
                tabLayout.getTabAt(2).setIcon(R.drawable.tab_refresh_0);
                break;
            case 2:
                tabLayout.getTabAt(0).setIcon(R.drawable.tab_login_0);
                tabLayout.getTabAt(1).setIcon(R.drawable.tab_register_0);
                tabLayout.getTabAt(2).setIcon(R.drawable.tab_refresh_1);
                break;
        }
    }

    private String msv;
    private  Handler handler;
    /**
     *  đăng khí tài khoản
     * @param id mã sinh viên
     * @param pass mật khẩu
     * @param soDT số diện thoại
     * @param btRegister nút đnăg kí
     * @param progressBar
     */
    public void register(final String id, final String pass, final String soDT, final AppCompatButton btRegister, final ProgressBar progressBar) {
        this.msv=id;
        database.child("users").child(id).addListenerForSingleValueEvent(this);
        /**
         * đối tượng trao đổi dữ liệu với 2 luồng
         */
         handler=new Handler(){
             /**
              * tin nhấn được gửi đến
              * và xử lý về dữ liệu truy vấn
              * @param msg tin nhấn được gửi đến
              */
            @Override
            public void handleMessage(Message msg) {

                if (msg.what==0){ // tin nhắn với mã code =0
                    SinhVien sinhVien= (SinhVien) msg.obj;
                    if (sinhVien!=null){
                        writeNewUser(sinhVien.getMaSV(),pass,sinhVien.getTenSV(),sinhVien.getLopDL(),soDT,"");
                        progressBar.setVisibility(View.GONE);
                        btRegister.setVisibility(View.VISIBLE);
                        loginFragment.setData(id,pass);
                        loginFragment.setTextNoti("Đăng ký thành công!");
                        wrappingViewPager.setCurrentItem(0);
                    }else{
                        siginFragment.textError("Mã sinh viên không đúng");
                        progressBar.setVisibility(View.GONE);
                        btRegister.setVisibility(View.VISIBLE);
                    }
                }
                if (msg.what==2){ // tin nhắn với mã code =1
                    siginFragment.textError("Mã sinh viên này đã được đăng ký");
                    progressBar.setVisibility(View.GONE);
                    btRegister.setVisibility(View.VISIBLE);
                }
            }
        };


    }
    /**
     * đăng nhập
     * */
    public  void login(final String id, final String pass, final AppCompatButton processButton, final ProgressBar progressBar, final AppCompatCheckBox animatedSwitch, final TextView tvNoti) {
        database.child("users").child(id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot){
                        User user = dataSnapshot.getValue(User.class);
                        try {
                            if (user.getPassWord().equals(pass)){
                                log.putID(id);
                                log.putPass(pass);
                                if (animatedSwitch.isChecked()){
                                    log.putID(id);
                                    log.putPass(pass);
                                }else{
//                                    log.remove();
                                }
                                processButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                Intent returnIntent = new Intent(LoginActivity.this,NavigationActivity.class);
                                returnIntent.putExtra(Log.LOG_PASS,pass);
                                returnIntent.putExtra(Log.LOG_ID,id);
                                setResult(RESULT_OK,returnIntent);
                                finish();
                            }else{
                                processButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                loginFragment.setTextNoti("* Sai mã sinh viên hoặc mật khẩu");
                            }
                        }catch (NullPointerException e){
                            processButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            loginFragment.setTextNoti("* Sai mã sinh viên hoặc mật khẩu");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        android.util.Log.e("faker","onCancelled");
                    }
                });

    }


    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        User user = dataSnapshot.getValue(User.class);
        try {

            if (user.getMaSV().equals(msv)&&!user.getPassWord().isEmpty()){  // kiểm tra mã sinh viên truy nhập có đúng là đã đnăg kí chưa và mạt khẩu đã tồn tại
                Message message=new Message();
                message.what=2;
                handler.sendMessage(message); // gửi 1 tin nhắn cho handler với mã là 2 để thông báo tài khảon đã tồn tại
            }else{ // nếu tài khoản k tồn lại
                ParserSinhVien parserSinhVien=new ParserSinhVien(handler); // lấy thêm thông tin của user từ hệ thống và gửi 1 sendMessage với mã code =0
                parserSinhVien.execute(msv); // khi có hệ thống
            }
            return;
        } catch (NullPointerException e) {
            ParserSinhVien parserSinhVien=new ParserSinhVien(handler);
            parserSinhVien.execute(msv);
            return;
        }
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
