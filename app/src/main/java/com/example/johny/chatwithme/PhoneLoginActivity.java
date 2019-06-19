package com.example.johny.chatwithme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationCodeButton, verifyButton;
    private EditText inputPhoneNumber, inputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationCodeButton = (Button) findViewById(R.id.send_verification_code_button);
        verifyButton = (Button) findViewById(R.id.verify_button);
        inputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        inputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = inputPhoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number is Required!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone verification");
                    loadingBar.setMessage("Please wait while we are autheticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callBacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code first.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Verification code");
                    loadingBar.setMessage("Please wait while we are verifaing your code...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalide Phone Number. Please enter corect phone number for code.", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                loadingBar.dismiss();
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent.", Toast.LENGTH_SHORT).show();

                sendVerificationCodeButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations! You are LogedIn successfuly!", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
