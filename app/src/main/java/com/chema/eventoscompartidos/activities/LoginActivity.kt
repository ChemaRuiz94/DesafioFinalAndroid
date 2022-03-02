package com.chema.eventoscompartidos.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class LoginActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private var RC_SIGN_IN = 1

    private var usuarios = ArrayList<User>()

    private lateinit var btn_login : Button
    private lateinit var btn_login_google : Button
    private lateinit var btn_signUp : Button
    private lateinit var txt_email : EditText
    private lateinit var txt_pwd : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login = findViewById(R.id.bt_Login)
        btn_login_google = findViewById(R.id.bt_Google)
        btn_signUp = findViewById(R.id.bt_Registrar)
        txt_email = findViewById(R.id.ed_txt_Email_login)
        txt_pwd = findViewById(R.id.ed_txt_Pass_login)

        auth = Firebase.auth


//        runBlocking {
//            Log.e("preuba2","Prueba2")
//            val job2 : Job = launch(context = Dispatchers.Default) {
//                val datos2 : QuerySnapshot = getDataFromFireStore2() as QuerySnapshot //Obtenermos la colección
//                Log.e("preuba1",datos2.toString())
//                obtenerDatos2(datos2 as QuerySnapshot?)  //'Destripamos' la colección y la metemos en nuestro ArrayList
//            }
//            //Con este método el hilo principal de onCreate se espera a que la función acabe y devuelva la colección con los datos.
//            job2.join() //Esperamos a que el método acabe: https://dzone.com/articles/waiting-for-coroutines
//        }


        btn_signUp.setOnClickListener{
            var intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btn_login_google.setOnClickListener{
            check_login_google()
        }

        btn_login.setOnClickListener{
            check_login()
        }
    }

    private fun check_login(){
        if(chekc_campos_vacios()){

            val email = txt_email.text.toString()
            val pwd = txt_pwd.text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pwd).addOnCompleteListener {
                if (it.isSuccessful){

                    VariablesCompartidas.emailUsuarioActual = (it.result?.user?.email?:"")

                    findUserByEmail(email)

                } else {
                    showAlert()
                }
            }

        }else{
            Toast.makeText(this,getString(R.string.emptyCamps), Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkUserRol(user: User){
//        val roles = findAllRoles()
        if(user.rol.size > 1) {
            //es admin
            Toast.makeText(this, "USUARIO ADMIN", Toast.LENGTH_SHORT).show()
            var myIntent = Intent(this, ActivatedUserHomeActivity::class.java)
            startActivity(myIntent)
        }
        else {
            //vamos usuario activity
            var myIntent = Intent(this, ActivatedUserHomeActivity::class.java)
            startActivity(myIntent)
        }

    }

    private fun findAllRoles(): ArrayList<Rol>{
        var rolesBd : ArrayList<Rol> = ArrayList()
        db.collection("${Constantes.COLLECTION_ROL}")
            .get()
            .addOnSuccessListener { roles ->
                //Existe
                for (rol in roles) {
                    Log.d(TAG, "${rol.id} => ${rol.data}")
                    var rolBD = Rol(
                        rol.get("idRol").toString().toInt(),
                        rol.get("nombreRol").toString()
                    )
                    rolesBd.add(rolBD)
                }
            }
            .addOnFailureListener { exception ->
                //No existe
                Log.w(TAG, "Error getting documents: ", exception)
            }
        return rolesBd
    }

    private fun findUserByEmail(email: String){

        Toast.makeText(this, email, Toast.LENGTH_SHORT).show()
        db.collection("${Constantes.collectionUser}")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { users ->
                //Existe
                Log.d("login", "existen usuarios")
                for (user in users) {
                    var phone = 0;
                    if (user.get("phone").toString() != ""){
                        phone = user.get("phone").toString().toInt()
                    }
                    var us = User(
                        user.get("userId").toString(),
                        user.get("userName").toString(),
                        user.get("email").toString(),
                        phone,
                        user.get("rol") as ArrayList<Rol>,
                        user.get("activo") as Boolean,
                        user.get("img").toString(),
                        user.get("eventos") as ArrayList<Evento>
                    )
                    if(us.activo){
                        VariablesCompartidas.userActual = us
                        checkUserRol(us)
                    }
                    else {
                        //usuario no activo mostrar mensaje
                        var myIntent = Intent(this, HomeActivity::class.java)
                        startActivity(myIntent)
                    }
                }
            }
            .addOnFailureListener { exception ->
                //No existe
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    suspend fun getDataFromFireStore2()  : QuerySnapshot? {
        return try{
            val data = db.collection("${Constantes.collectionUser}")
                .get()
                .await()
            data
        }catch (e : Exception){
            null
        }
    }

    private fun obtenerDatos2(datos: QuerySnapshot?) {
        usuarios.clear()

        for(dc: DocumentChange in datos?.documentChanges!!){

            if (dc.type == DocumentChange.Type.ADDED){

                //var prov = dc.document.get("provider").toString()
                var al = User(
                    dc.document.get("userId").toString(),
                    dc.document.get("userName").toString(),
                    dc.document.get("email").toString(),
                    dc.document.get("phone").toString().toInt(),
                    dc.document.get("rol") as ArrayList<Rol>,
                    dc.document.get("activo") as Boolean,
                    dc.document.get("img").toString(),
                    dc.document.get("eventos") as ArrayList<Evento>
                )
                Log.e("CHE","${al.rol}")
                usuarios.add(al)
            }
        }
    }


    fun check_user_rol(){
        for (u in usuarios){
            if(u.email.equals(VariablesCompartidas.emailUsuarioActual)){
//                VariablesCompartidas.rolUsuarioActual = u.rol
                VariablesCompartidas.userActual = u
            }
        }

        if(VariablesCompartidas.rolUsuarioActual.equals("user")){

            //Toast.makeText(this, "USUARIO MINDUNDI", Toast.LENGTH_SHORT).show()
            var myIntent = Intent(this,HomeActivity::class.java)
            startActivity(myIntent)

        }else if(VariablesCompartidas.rolUsuarioActual.equals("activated_user")){

            //Toast.makeText(this, "USUARIO ACTIVADO", Toast.LENGTH_SHORT).show()
            var myIntent = Intent(this,ActivatedUserHomeActivity::class.java)
            startActivity(myIntent)

        }else if (VariablesCompartidas.rolUsuarioActual.equals("admin")){
            Toast.makeText(this, "USUARIO ADMIN", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "NINGUN TIPO DE ROL", Toast.LENGTH_SHORT).show()
        }
    }




    private fun chekc_campos_vacios():Boolean{
        var correcto = true

        if(txt_email.text.toString().trim().isEmpty()){
            correcto = false
        }

        if(txt_pwd.text.toString().trim().isEmpty()){
            correcto = false
        }

        return correcto
    }

    private fun check_login_google(){
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.request_id_token)) //Esto se encuentra en el archivo google-services.json: client->oauth_client -> client_id
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(this,googleConf) //Este será el cliente de autenticación de Google.
//        googleClient.signOut() //Con esto salimos de la posible cuenta  de Google que se encuentre logueada.
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
                    auth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){

                            Toast.makeText(this, " IR A HOME ", Toast.LENGTH_SHORT).show()
                            val user = auth.currentUser!!
                            VariablesCompartidas.emailUsuarioActual = user.email!!

                            findUserByEmail(user.email!!)
                            //irHome(account.email?:"")  //Esto de los interrogantes es por si está vacío el email.
                            //Toast.makeText(this, " IR A HOME ", Toast.LENGTH_SHORT).show()
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