package com.example.AIEye.sign

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.AIEye.WebViewActivity
import com.example.AIEye.retrofit.Login
import com.example.AIEye.retrofit.LoginService
import com.example.AIEye.retrofit.Register
import com.example.AIEye.retrofit.RegisterService
import com.example.AIEye.roomDB.ID
import com.example.AIEye.roomDB.RoomDB
import com.example.AIEye.roomDB.UserDAO
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

class SignViewModel(application: Application) : AndroidViewModel(application) {
    private var token: String? = null
    private var tokenName: String = ""
    private val registerService: RegisterService
    private val loginService: LoginService
    private val userDAO: UserDAO

    init {
        //모든 인증서에 대한 수락
        @SuppressLint("CustomX509TrustManager")
        val trustAllCerts = object : X509TrustManager {
            @SuppressLint("TrustAllX509TrustManager")
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
            }

            @SuppressLint("TrustAllX509TrustManager")
            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }

        //ssl 프로토콜을 지정하고 새로은 SSL 인증서를 생성함.
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(trustAllCerts), SecureRandom())
        val sslFactory = sslContext.socketFactory
        // 만든 새로운 인증서에 대해 호스트 이름 검증을 무시
        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslFactory, trustAllCerts)
            .hostnameVerifier { _, _ -> true }.build()

        Retrofit.Builder().baseUrl("https://" + WebViewActivity.ADDRESS + ":8097")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client).build().apply {
                registerService = create(RegisterService::class.java)
                loginService = create(LoginService::class.java)
            }

        //FCM 토큰
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                Log.w("Firebase", "Fetching FCM registration token failed", it.exception)
                return@addOnCompleteListener
            }
            //토큰 확인
            token = it.result
            Log.d("토큰", token as String)
        }

        //기기 닉네임
        tokenName = getName()


        val context = getApplication<Application>().applicationContext
        userDAO = RoomDB.getInstance(context)!!.userDAO()
    }

    suspend fun signUp(email: String?, password: String?, confirmPassword: String?): String? {
        //email 형식인지 확인
        if (!isValidEmail(email) && email?.isNotEmpty() == true) {
            return "이메일 형식이 아닙니다!"
        }

        if (password != confirmPassword) {
            return "비밀 번호가 서로 다릅니다!"
        }

        if (email != null && password != null && confirmPassword != null && token != null) {
            val register = Register(email, password, confirmPassword, token!!, tokenName)
            return postSignUp(register)
        }

        return null
    }

    suspend fun login(email: String?, password: String?): String? {
        //email 형식인지 확인
        if (!isValidEmail(email) && email?.isNotEmpty() == true) {
            return "이메일 형식이 아닙니다!"
        }

        //직접 로그인을 하는 경우
        if (email?.isNotEmpty() == true && password != null && token != null) {
            Login(email, password, token!!, tokenName).also { login ->
                return postLogin(login)
            }
        }

        //자동 로그인을 하는 경우
        if (userDAO.getAll()!!.isNotEmpty() && (email == null || email.isEmpty())) {
            return "auto"
        }

        return null
    }

    private suspend fun postSignUp(register: Register): String {

        val response = withContext(Dispatchers.IO) {
            registerService.sendRegister(register).execute()
        }

        //post 성공
        if (response.isSuccessful) {
            // DB에 계정 정보 저장
            isNew(register.email, register.password)
            return "Success"
        }
        //실패
        else {
            if (response.errorBody() == null) {
                Log.e("response Error!", response.code().toString())
                return "response Error" + response.code().toString()
            }
            try {
                JsonParser().parse(response.errorBody()!!.string()).apply {
                    return this.asJsonObject.get("errors").asJsonArray.joinToString()
                }
            } catch (e: Exception) {
                return e.message.toString()
            }
        }
    }

    private suspend fun postLogin(login: Login): String {
        val response = withContext(Dispatchers.IO) {
            loginService.sendLogin(login).execute()
        }

        if (response.isSuccessful) {
            // DB에 계정 정보 저장
            isNew(login.email, login.password)
            return "Success"

        } else {
            //실패 한 경우 에 대한 로직
            if (response.errorBody() == null) {
                Log.e("response Error!", response.code().toString())
                return "response Error" + response.code().toString()
            }
            try {
                JsonParser().parse(response.errorBody()!!.string()).apply {
                    return this.asJsonObject.get("errorMessage").toString()
                }
            } catch (e: Exception) {
                return e.message.toString()
            }
        }
    }

    private fun isNew(email: String, password: String) {
        //DB에 저장된 값과 동일한 계정이 없다면, DB에 저장
        val saveIds = userDAO.getAll()

        // 저장된 값이 없다면, 새 값을 추가
        if (saveIds.isNullOrEmpty()) {
            val id = ID(email = email, password = password)
            userDAO.insert(id)
        } else {
            // 저장된 값이 있으면, 새 값과 비교 후 추가
            var isNewId = true
            for (saveId in saveIds) {
                if (saveId?.email == email) {
                    isNewId = false
                    break
                }
            }

            if (isNewId) {
                val id = ID(email = email, password = password)
                userDAO.insert(id)
            }
        }
    }

    private fun isValidEmail(email: String?): Boolean {
        if (email == null) {
            return false
        }
        val pattern = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        return pattern.matches(email)
    }

    @Throws(SecurityException::class)
    private fun getName(): String {
        //블루투스 연결 시 보이는 닉네임
        val context = getApplication<Application>().applicationContext
        val bluetoothAdapter =
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
                ?: return "기본 닉네임"

        return bluetoothAdapter.name

    }
}