package com.example.pallab.travelgoapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.pallab.travelgoapp.Common.Common;
import com.example.pallab.travelgoapp.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserActivity extends AppCompatActivity {
    Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    TextView txt_forgot_pwd;


    private  final static int PERMISSION = 1000;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //before setContentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.layout_user);

        //init paperdb
        Paper.init(this);

        //Init Firebase
        auth = FirebaseAuth.getInstance();
        db   = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tb1);

        //Init View
        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnSignIn   = (Button)findViewById(R.id.btnSignIn);
        rootLayout  = (RelativeLayout)findViewById(R.id.rootLayout);
        txt_forgot_pwd = (TextView)findViewById(R.id.txt_forgot_password);

        txt_forgot_pwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialogForgotPwd();
                return false;
            }
        });

        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });

        //Auto login System
        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);
        if (user != null && pwd != null){
            if (!TextUtils.isEmpty(user) &&
                    !TextUtils.isEmpty(pwd)){

                autoLogin(user,pwd);
            }
        }


    }

    private void autoLogin(String user, String pwd) {


        final AlertDialog waitingDialog = new SpotsDialog(UserActivity.this);
        waitingDialog.show();

        //Login
        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        waitingDialog.dismiss();

                        FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        //After assigned value
                                        Common.currentUser = dataSnapshot.getValue(User.class);
                                        //Start new activity
                                        startActivity(new Intent(UserActivity.this,DriverHome.class));
                                        waitingDialog.dismiss();
                                        finish();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Snackbar.make(rootLayout, "Failed"+e.getMessage(), Snackbar.LENGTH_SHORT)
                        .show();
                //active sign in button
                btnSignIn.setEnabled(true);
            }
        });


    }

    private void showDialogForgotPwd() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(UserActivity.this);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Please Enter Your Email Address");

        LayoutInflater inflater = LayoutInflater.from(UserActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pwd,null);


        final MaterialEditText edtEmail = (MaterialEditText)forgot_pwd_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_pwd_layout);

        //set button
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                    final AlertDialog waitingDialog = new SpotsDialog(UserActivity.this);
                    waitingDialog.show();

                    auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                        dialogInterface.dismiss();
                                        waitingDialog.dismiss();

                                        Snackbar.make(rootLayout,"Reset Password Link Has Been Sent",Snackbar.LENGTH_LONG)
                                                .show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialogInterface.dismiss();
                            waitingDialog.dismiss();

                            Snackbar.make(rootLayout,""+e.getMessage(),Snackbar.LENGTH_LONG)
                                    .show();
                        }
                    });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
            }
        });
        alertDialog.show();

    }

    private void showLoginDialog() {
        final AlertDialog.Builder dialog  = new AlertDialog.Builder(UserActivity.this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use your email to Sign In");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout    = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail  = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword  = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("SIGN IN ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //set disable button sign in if is processing
                btnSignIn.setEnabled(false);


                //check validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short", Snackbar.LENGTH_SHORT)
                            .show();
                    return;
                }

                final AlertDialog waitingDialog = new SpotsDialog(UserActivity.this);
                waitingDialog.show();

                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {

                                waitingDialog.dismiss();

                                FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                //save value
                                                Paper.book().write(Common.user_field,edtEmail.getText().toString());
                                                Paper.book().write(Common.pwd_field,edtPassword.getText().toString());


                                                //After assigned value
                                                Common.currentUser = dataSnapshot.getValue(User.class);
                                                //Start new activity
                                                startActivity(new Intent(UserActivity.this,DriverHome.class));
                                                finish();

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout, "Failed"+e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                        //active sign in button
                        btnSignIn.setEnabled(true);
                    }
                });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }


    private void showRegisterDialog() {
        final AlertDialog.Builder dialog  = new AlertDialog.Builder(UserActivity.this);
        dialog.setTitle("SIGN UP");
        dialog.setMessage("Please use your email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout    = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail  = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword  = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName  = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone  = register_layout.findViewById(R.id.edtPhone);


        dialog.setView(register_layout);

        //Set Button
        dialog.setPositiveButton("SIGN UP ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                //check validation
                if (TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter phone number",Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                if (edtPassword.getText().toString().length() < 6 ){
                    Snackbar.make(rootLayout,"Password too short",Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                //Register New User

                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                //save user to db
                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());
                                user.setPassword(edtPassword.getText().toString());

                                //using email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Register successfull",Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        });

            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

}
