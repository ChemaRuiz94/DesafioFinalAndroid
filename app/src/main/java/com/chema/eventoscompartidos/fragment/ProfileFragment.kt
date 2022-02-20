package com.chema.eventoscompartidos.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chema.eventoscompartidos.R
import com.chema.eventoscompartidos.activities.ui.home.HomeViewModel
import com.chema.eventoscompartidos.databinding.FragmentHomeBinding
import com.chema.eventoscompartidos.databinding.FragmentProfileBinding
import com.chema.eventoscompartidos.model.User
import com.chema.eventoscompartidos.utils.Constantes
import com.chema.eventoscompartidos.utils.VariablesCompartidas
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.io.FileNotFoundException
import java.io.InputStream

class ProfileFragment: Fragment() {

    lateinit var btn_change_pwd_profile : Button
    lateinit var flt_btn_edit_profile : FloatingActionButton
    lateinit var imgUsuarioPerfil : ImageView
    lateinit var ed_txt_userName_profile : EditText
    lateinit var ed_txt_email_profile : EditText
    lateinit var ed_txt_phone_profile : EditText

    lateinit var userAct : User
    private var photo: Bitmap? = null
    private var editMode: Boolean = false

    private lateinit var homeViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

         */

        return root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_change_pwd_profile = view.findViewById(R.id.btn_change_pwd_profile)
        flt_btn_edit_profile = view.findViewById(R.id.flt_btn_edit_profile)
        imgUsuarioPerfil = view.findViewById(R.id.imgUsuarioPerfil)
        ed_txt_userName_profile = view.findViewById(R.id.ed_txt_userName_profile)
        ed_txt_email_profile = view.findViewById(R.id.ed_txt_email_profile)
        ed_txt_phone_profile = view.findViewById(R.id.ed_txt_phone_profile)

        imgUsuarioPerfil.setOnClickListener{
            cambiarFoto()
        }

        btn_change_pwd_profile.setOnClickListener{
            cambiarContraseña()
        }

        flt_btn_edit_profile.setOnClickListener{
            //change_to_edit_mode()
        }

        cargarDatosUser()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun cargarDatosUser(){
        userAct = VariablesCompartidas.userActual as User

        ed_txt_userName_profile.apply {
            text.clear()
            append(userAct.userName)
        }
        ed_txt_email_profile.apply {
            text.clear()
            append(userAct.email)
        }
        ed_txt_phone_profile.apply {
            text.clear()
            append(userAct.phone.toString())
        }

        //ed_txt_userName_profile.setText(userAct.userName)
        //ed_txt_email_profile.setText(userAct.email)
        //ed_txt_phone_profile.setText(userAct.phone)
    }


    fun cambiarContraseña() {
        val dialog = layoutInflater.inflate(R.layout.password_changer, null)
        val pass1 = dialog.findViewById<EditText>(R.id.edPassChanger)
        val pass2 = dialog.findViewById<EditText>(R.id.edPass2Changer)
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.changePassword))
            .setView(dialog)
            .setPositiveButton("OK") { view, _ ->
                val p1 = pass1.text.toString()
                val p2 = pass2.text.toString()
                if (p1 == p2) {
                    //usuario.passwd = p1
                    Toast.makeText(
                        context,
                        getString(R.string.Suscesfull),
                        Toast.LENGTH_SHORT
                    ).show()
                } else Toast.makeText(
                    context,
                    getString(R.string.ERROR),
                    Toast.LENGTH_SHORT
                ).show()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.cancelar)) { view, _ ->
                view.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    fun cambiarFoto() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.chosePhoto))
            .setMessage(getString(R.string.strMensajeElegirFoto))
            .setPositiveButton(getString(R.string.strCamara)) { view, _ ->
                hacerFoto()
                view.dismiss()
            }
            .setNegativeButton(getString(R.string.strGaleria)) { view, _ ->
                elegirDeGaleria()
                view.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    private fun elegirDeGaleria() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(intent, "Seleccione una imagen"),
            Constantes.CODE_GALLERY
        )
    }

    private fun hacerFoto() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        )
            ActivityCompat.requestPermissions(
                context as AppCompatActivity,
                arrayOf(Manifest.permission.CAMERA),
                Constantes.CODE_CAMERA
            )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, Constantes.CODE_CAMERA)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constantes.CODE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    photo = data?.extras?.get("data") as Bitmap
                    imgUsuarioPerfil.setImageBitmap(photo)
                }
            }
            Constantes.CODE_GALLERY -> {
                if (resultCode === Activity.RESULT_OK) {
                    val selectedImage = data?.data
                    val selectedPath: String? = selectedImage?.path
                    if (selectedPath != null) {
                        var imageStream: InputStream? = null
                        try {
                            imageStream = selectedImage.let {
                                requireContext().contentResolver.openInputStream(
                                    it
                                )
                            }
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        }
                        val bmp = BitmapFactory.decodeStream(imageStream)
                        photo = Bitmap.createScaledBitmap(bmp, 200, 300, true)
                        imgUsuarioPerfil.setImageBitmap(photo)
                    }
                }
            }
        }
    }
}