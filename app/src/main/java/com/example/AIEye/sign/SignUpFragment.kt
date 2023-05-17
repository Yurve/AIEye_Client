package com.example.AIEye.sign

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.AIEye.R
import kotlinx.coroutines.launch

class SignUpFragment : Fragment() {
    private lateinit var viewModel: SignViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val signUpView = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val email = signUpView.findViewById<EditText>(R.id.signUpEmail)
        val password = signUpView.findViewById<EditText>(R.id.signUpPassword)
        val confirmPassword = signUpView.findViewById<EditText>(R.id.signUpConfirmPassword)
        val signUpButton = signUpView.findViewById<AppCompatButton>(R.id.signUpButton)
        val goSingIn = signUpView.findViewById<AppCompatButton>(R.id.goLogin)
        viewModel = ViewModelProvider(this)[SignViewModel::class.java]


        //회원가입 신청
        signUpButton.setOnClickListener {
            lifecycleScope.launch {

                val msg = viewModel.signUp(
                    email.text.toString(),
                    password.text.toString(),
                    confirmPassword.text.toString()
                ) ?: return@launch

                //회원가입 성공 시
                if (msg == "Success") {
                    Toast.makeText(context, "회원 가입 성공! 로그인 버튼을 눌러 주세요!", Toast.LENGTH_SHORT).show()

                    activity?.supportFragmentManager?.beginTransaction()?.replace(
                        R.id.fragment_container_view,
                        LoginFragment()
                    )?.commit()

                    //회원가입 실패 시 다이얼로그 창 생성
                } else {
                    AlertDialog.Builder(context).apply {
                        setTitle("Error 발생!").setMessage(msg)
                    }.create().show()
                }
            }
        }

        //로그인 신청
        goSingIn.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.fragment_container_view,
                LoginFragment()
            )?.commit()
        }

        return signUpView
    }

}