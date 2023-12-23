package com.example.taganroggo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogIn : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        auth = Firebase.auth
        val loginEditText: EditText = findViewById(R.id.loginfield)
        val passwordEditText: EditText = findViewById(R.id.passwordfield)
        val showPasswordCheckBox: CheckBox = findViewById(R.id.showPasswordCheckBox)
        val buttonsubmit: Button = findViewById(R.id.buttonReg)
        val buttonreg: Button = findViewById(R.id.buttonLogIn2)

        // Установка слушателя на чекбокс
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // В зависимости от состояния чекбокса, устанавливаем или снимаем видимость пароля
            val inputType = if (isChecked) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            passwordEditText.inputType = inputType
            // Необходимо установить курсор в конец текста после изменения типа ввода
            passwordEditText.text?.let { passwordEditText.setSelection(it.length) }
        }
        buttonreg.setOnClickListener {
            startActivity(Intent(this, RegAct::class.java))
        }
        buttonsubmit.setOnClickListener {

            val log = loginEditText.text.toString()
            val pas = passwordEditText.text.toString()


            if(log.isEmpty() || pas.isEmpty()){
                Toast.makeText(this, "Заполните все поля ", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(log, pas)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // успешный вход, переход в main activity
                        Toast.makeText(baseContext, "Authentication Success.",
                            Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}