package com.example.mystoryapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mystoryapp.customview.EmailEditText
import com.example.mystoryapp.customview.NameEditText
import com.example.mystoryapp.customview.PasswordEditText
import com.example.mystoryapp.customview.SignupButton
import com.example.mystoryapp.databinding.ActivitySignUpBinding
import com.example.mystoryapp.response.RegisterResponse
import com.example.mystoryapp.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private var _signupBinding: ActivitySignUpBinding? = null
    private val binding get() = _signupBinding

    private lateinit var nameEditText: NameEditText
    private lateinit var emailEditText: EmailEditText
    private lateinit var passwordEditText: PasswordEditText
    private lateinit var signupButton: SignupButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _signupBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        showLoading(false)

        nameEditText = binding!!.nameEditText
        emailEditText = binding!!.emailEditText
        passwordEditText = binding!!.passwordEditText
        signupButton = binding!!.signupButton

        val showPasswordCheckbox = binding!!.passwordShowCheckbox

        showPasswordCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                Toast.makeText(this, "Password Ditampilkan", Toast.LENGTH_SHORT).show()
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }else{
                Toast.makeText(this, "Password Disembunyikan", Toast.LENGTH_SHORT).show()
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        setLoginButtonEnable()

        init()

        signupButton.setOnClickListener {
            register()
        }
    }

    private fun register(){
        showLoading(true)
        ApiConfig.getApiService().registerUser(
            nameEditText.text.toString().trim(),
            emailEditText.text.toString().trim(),
            passwordEditText.text.toString().trim()
        ).enqueue(object  : Callback<RegisterResponse>{
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                showLoading(false)
                val signupResponse = response.body()
                signupResponse?.message?.let { Log.d("SignupActivity", it) }

                if (signupResponse != null) {
                    if (!signupResponse.error){
                        val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                        intent.putExtra(MainActivity.EXTRA_RESPONSE_SIGNUP, signupResponse.message)
                        startActivity(intent)
                    } else{
                        Toast.makeText(this@SignUpActivity, signupResponse.message, Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this@SignUpActivity, "Email Sudah Digunakan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@SignUpActivity, "Registrasi Gagal", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun init(){
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })

        passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setLoginButtonEnable()
            }
            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun setLoginButtonEnable() {
        val name = nameEditText.text
        val email = emailEditText.text
        val password = passwordEditText.text
        signupButton.isEnabled = name != null && name.toString().isNotEmpty() &&
                password != null && password.toString().length >= 6 &&
                email != null && email.toString().isNotEmpty()
    }

    private fun showLoading(isLoading: Boolean){
        if (isLoading){
            binding?.progressBar?.visibility  = View.VISIBLE
        } else {
            binding?.progressBar?.visibility  = View.GONE
        }
    }
}