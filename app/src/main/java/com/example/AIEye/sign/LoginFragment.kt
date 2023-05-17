package com.example.AIEye.sign

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.AIEye.R
import com.example.AIEye.WebViewActivity
import com.example.AIEye.roomDB.RoomDB
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var viewModel: SignViewModel
    private var dialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val loginView = inflater.inflate(R.layout.fragment_login, container, false)

        val emailText = loginView.findViewById<EditText>(R.id.signInEmail)
        val passwordText = loginView.findViewById<EditText>(R.id.signInPassword)
        val loginButton = loginView.findViewById<AppCompatButton>(R.id.loginButton)
        val goRegister = loginView.findViewById<AppCompatButton>(R.id.goRegister)
        viewModel = ViewModelProvider(this)[SignViewModel::class.java]


        //버튼 클릭 이벤트 처리
        //로그인 신청
        loginButton.setOnClickListener {
            lifecycleScope.launch {
                val msg = viewModel.login(emailText.text.toString(), passwordText.text.toString())
                    ?: return@launch

                //자동 로그인 시 다이얼로그 창 생성
                if (msg == "auto") {
                    //id값 에 대응하는 표를 보여주고 선택하면 그때 로그인 으로 넘어가게 수정
                    val setIds = HashSet<String>()
                    RoomDB.getInstance(requireContext())?.userDAO()!!.getAll()?.forEach {
                        it?.email?.let { email -> setIds.add(email) }
                    }

                    setIds.add("기기 전체 삭제하기")
                    alertDialog(setIds)

                    //로그인 실패 시 다이얼로그 창 생성
                } else if (msg != "Success") {
                    AlertDialog.Builder(context).apply {
                        setTitle("Error 발생!").setMessage(msg)
                    }.create().show()

                }//로그인 성공 시 웹뷰로 이동
                else {
                    val intent = Intent(context, WebViewActivity::class.java)
                    intent.putExtra("email", emailText.text.toString())
                    intent.putExtra("password", passwordText.text.toString())
                    startActivity(intent)
                    activity?.finish()
                }
            }
        }

        //회원가입 신청
        goRegister.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.replace(
                R.id.fragment_container_view, SignUpFragment()
            )?.commit()
        }

        return loginView
    }

    // 자동 로그인시 선택할 수 있는 다이얼로그 창
    private fun alertDialog(ids: HashSet<String>) {
        val builder = AlertDialog.Builder(context).apply {
            setTitle("기기 선택하기")
            val idArray = ids.toTypedArray<CharSequence>()
            setItems(idArray) { _, which ->
                val selectedEmail = idArray[which] as String
                RoomDB.getInstance(context)?.userDAO()!!.getAll()?.forEach {
                    if (selectedEmail == it?.email) {
                        val intent = Intent(context, WebViewActivity::class.java)
                        intent.putExtra("email", it.email)
                        intent.putExtra("password", it.password)
                        startActivity(intent)
                        activity?.finish()
                    }

                    if (selectedEmail == "기기 전체 삭제하기") {
                        if (it != null) {
                            RoomDB.getInstance(context)!!.userDAO().delete(it)
                        }
                    }
                }
            }
        }
        dialog = builder.create().also {
            it.show()
        }
    }

    override fun onDestroy() {
        dialog?.dismiss()
        super.onDestroy()
    }
}