package com.example.taganroggo

import android.content.Context
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.io.PipedInputStream

class RegAct : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)

        auth = Firebase.auth

        val nameEditText: EditText = findViewById(R.id.namefield)
        val surnameEditText: EditText = findViewById(R.id.surnamefield)
        val loginEditText: EditText = findViewById(R.id.loginfield)
        val passwordEditText: EditText = findViewById(R.id.passwordfield)
        val showPasswordCheckBox: CheckBox = findViewById(R.id.showPasswordCheckBox)
        val buttonsubmit: Button = findViewById(R.id.buttonReg)

        // Установка слушателя на чекбокс
        showPasswordCheckBox.setOnCheckedChangeListener { _, isChecked ->
            // В зависимости от состояния чекбокса, устанавливаем или снимаем видимость пароля
            val inputType = if (isChecked) InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            passwordEditText.inputType = inputType
            // Необходимо установить курсор в конец текста после изменения типа ввода
            passwordEditText.text?.let { passwordEditText.setSelection(it.length) }
        }
        buttonsubmit.setOnClickListener {

            val nam = nameEditText.text.toString()
            val sur = surnameEditText.text.toString()
            val log = loginEditText.text.toString()
            val pas = passwordEditText.text.toString()

            if(log.isEmpty() || pas.isEmpty() || nam.isEmpty() || sur.isEmpty()){
                Toast.makeText(this, "Заполните все поля ", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(log, pas)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        //успешный ввод, переход к main activity и сохранение в ShaderPref
                        val shaderPref = getSharedPreferences("SPDB", Context.MODE_PRIVATE)
                        val editor = shaderPref.edit()

                        FirebaseAPI().writeUser(nam, log, sur){ id ->
                            editor.apply{
                                putString("login", log)
                                putString("password", pas)
                                putInt("id", id)
                                apply()
                            }
                        }

                        Toast.makeText(baseContext, "Authentication Success.",
                            Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegAct, MainActivity::class.java))

                    } else {
                        // If sign in fails, display a message to the user.

                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(this, "Error occurred ${it.localizedMessage}", Toast.LENGTH_SHORT)
                        .show()
                }

        }
    }
}