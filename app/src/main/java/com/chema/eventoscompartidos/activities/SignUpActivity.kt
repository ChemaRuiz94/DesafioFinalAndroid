package com.chema.eventoscompartidos.activities

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.model.Evento
import com.chema.eventoscompartidos.model.Rol
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.ProviderType
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var btn_confirmar_signUp: Button
    private lateinit var btn_google_signUp: Button
    private lateinit var ed_txt_userName_signUp: EditText
    private lateinit var ed_txt_email_signUp: EditText
    private lateinit var ed_txt_pwd_signUp: EditText
    private lateinit var ed_txt_pwd2_signUp: EditText
    private lateinit var ed_txt_phone_signUp: EditText
    private var RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        btn_confirmar_signUp = findViewById(R.id.btn_confirmar_signUp)
        btn_google_signUp = findViewById(R.id.btn_google_signUp)
        ed_txt_userName_signUp = findViewById(R.id.ed_txt_userName_signUp)
        ed_txt_email_signUp = findViewById(R.id.ed_txt_email_signUp)
        ed_txt_pwd_signUp = findViewById(R.id.ed_txt_pwd_signUp)
        ed_txt_pwd2_signUp = findViewById(R.id.ed_txt_pwd2_signUp)
        ed_txt_phone_signUp = findViewById(R.id.ed_txt_phone_signUp)


        btn_confirmar_signUp.setOnClickListener{
            check_signUp_correcto()
        }

        btn_google_signUp.setOnClickListener{
            reg_google()
        }
    }


    fun check_signUp_correcto(){
        //primero comprueba que los campos no esten vacios
        if(check_campos_vacios()){

            //Despues comprueba que las contraseñas sean iguales
            if(check_pwd_iguales()){

                //Despues se comprueba que el formato del telefono sea correcto
                if(check_movil(ed_txt_phone_signUp.text.trim())){

                    //Si lo anterior ha salido bien, se registra el usuario
                    check_firebase_auth()

                }else{
                    Toast.makeText(this, (getString(R.string.phoneNotCorrect)),Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, (getString(R.string.samePassword)),Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(this, (getString(R.string.emptyCamps)),Toast.LENGTH_SHORT).show()
        }
    }
    /*
    Funcion que comprueba que todos los campos esten rellenos
    Devuelve true en caso de que esten rellenos
     */
    fun check_campos_vacios():Boolean{
        var correcto = true

        if(ed_txt_userName_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_email_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_pwd_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_pwd2_signUp.text.trim().isEmpty()){
            correcto = false
        }

        if(ed_txt_phone_signUp.text.trim().isEmpty()){
            correcto = false
        }

        return correcto
    }

    /*
    Funcion que comprueba si las dos contraseñas son iguales
     */
    fun check_pwd_iguales():Boolean{
        if(ed_txt_pwd_signUp.text.trim().equals(ed_txt_pwd2_signUp.text.trim())){
            return true
        }
        return false
    }

    /*
    * Comprueba si el numero de telefono es correcto
     */
    private fun check_movil(target: CharSequence?): Boolean {
        return if (target == null) {
            false
        } else {
            if (target.length < 6 || target.length > 13) {
                false
            } else {
                Patterns.PHONE.matcher(target).matches()
            }
        }
    }

    /*
    Autenticacion Firebase
     */
    private fun check_firebase_auth(){
        val email = ed_txt_email_signUp.text.toString()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword( email,ed_txt_pwd_signUp.text.toString()).addOnCompleteListener {
            if (it.isSuccessful){
                reg_user(email,ed_txt_userName_signUp.text.toString()) //guardamos el usuario
                VariablesCompartidas.emailUsuarioActual = (it.result?.user?.email?:"")
                Toast.makeText(this, " IR A HOME ", Toast.LENGTH_SHORT).show()

            } else {
                showAlert()
            }
        }
    }

    /*
    Registrar un usuario en FireStore
     */
    private fun reg_user(email: String, name: String){
        val rol =  Rol(2 , "${Constantes.rolUser}")
        var listRoles : ArrayList<Rol> = ArrayList()
        listRoles.add(rol)
        var img : String? = null
        var eventos : ArrayList<Evento> = ArrayList()
        val id = UUID.randomUUID()
        var phone = ed_txt_phone_signUp.text.toString().toInt()

        //Se guardarán en modo HashMap (clave, valor).
        var user = hashMapOf(
            "userId" to id.toString(),
            "userName" to name,
            "email" to email,
            "phone" to phone,
            "rol" to listRoles,
            "activo" to false,
            "img" to img,
            "eventos" to eventos
        )

        db.collection("${Constantes.collectionUser}")
            .document(id.toString()) //Será la clave del documento.
            .set(user).addOnSuccessListener {
                var myIntent = Intent(this,HomeActivity::class.java)
                startActivity(myIntent)
                Toast.makeText(this, getString(R.string.SignUpSuscesfull), Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this, getString(R.string.ocurridoErrorAutenticacion), Toast.LENGTH_SHORT).show()
            }
    }

    private fun reg_google(){
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.request_id_token)) //Esto se encuentra en el archivo google-services.json: client->oauth_client -> client_id
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this,googleConf) //Este será el cliente de autenticación de Google.
        googleClient.signOut() //Con esto salimos de la posible cuenta  de Google que se encuentre logueada.
        val signInIntent = googleClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Si la respuesta de esta activity se corresponde con la inicializada es que viene de la autenticación de Google.
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!

                //Ya tenemos la id de la cuenta. Ahora nos autenticamos con FireBase.
                if (account != null) {
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){

                            VariablesCompartidas.emailUsuarioActual = account.email?:""
                            reg_user(account.email?:"", account.displayName?:"")
                        } else {
                            showAlert()
                        }
                    }
                }
                //firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately

                showAlert()
            }
        }
    }
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.ERROR))
        builder.setMessage(getString(R.string.ocurridoErrorAutenticacion))
        builder.setPositiveButton(getString(R.string.aceptar),null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}