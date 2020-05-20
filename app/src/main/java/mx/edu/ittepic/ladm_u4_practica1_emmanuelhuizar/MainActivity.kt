package mx.edu.ittepic.ladm_u4_practica1_emmanuelhuizar

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.agregar.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    var baseRemota= FirebaseFirestore.getInstance()
    var dataLista=ArrayList<String>()
    var listaID=ArrayList<String>()
    var hiloControl : HiloControl?=null
    val siPermiso = 1
    val siPermiso2 = 3
    val siPermiso3 = 5
    val siPermiso4 = 7
    var telefono=""
    var mensaje=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        baseRemota.collection("Mensajes")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException!=null){
                    Toast.makeText(this,"No se pude realizar busqueda",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                for(document in querySnapshot!!){
                    editText4.setText(document.getString("Nodeseado"))
                    editText.setText(document.getString("Deseado"))
                }
            }

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALL_LOG)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CALL_LOG), siPermiso)
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.MANAGE_OWN_CALLS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.MANAGE_OWN_CALLS), siPermiso2)
        }
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_PHONE_STATE), siPermiso4)
        }


        hiloControl = HiloControl(this)
        hiloControl?.start()

        button5.setOnClickListener {
            var v = Intent(this,Main2Activity::class.java)
            startActivity(v)
        }

        button4.setOnClickListener {
            var dialogo= Dialog(this)
            dialogo.setContentView(R.layout.agregar)

            var Nombre = dialogo.findViewById<EditText>(R.id.editText2)
            var Telefono = dialogo.findViewById<EditText>(R.id.editText3)
            var deseado = dialogo.findViewById<CheckBox>(R.id.deseado)
            var button2 = dialogo.findViewById<Button>(R.id.button2)
            var button3 = dialogo.findViewById<Button>(R.id.button3)


            button2.setOnClickListener {
                var status=true
                if(Telefono.text.toString().equals("") || Nombre.text.toString().equals("")){
                    Toast.makeText(this,"Campos vacios", Toast.LENGTH_LONG).show()
                }else{
                    if (deseado.isChecked==false){
                        status=false
                    }
                    var datosInsertar = hashMapOf(
                        "nombre" to Nombre.text.toString(),
                        "telefono" to Telefono.text.toString(),
                        "estatus" to status
                    )

                    baseRemota.collection("consulta").add(datosInsertar as Any)
                        .addOnSuccessListener {
                            Toast.makeText(this,"Se inserto correctamente", Toast.LENGTH_LONG).show()
                            dialogo.dismiss()
                        }
                        .addOnFailureListener{
                            Toast.makeText(this,"IMPORTANTE No se pudo insertar", Toast.LENGTH_LONG).show()
                        }

                }
                dialogo.dismiss()
            }

            button3.setOnClickListener {
                dialogo.dismiss()
            }

            dialogo.show()
        }

        button8.setOnClickListener {
            var dialogo= Dialog(this)
            dialogo.setContentView(R.layout.mensajes)

            var editText5 = dialogo.findViewById<EditText>(R.id.editText5)
            var editText6 = dialogo.findViewById<EditText>(R.id.editText6)
            var button6 = dialogo.findViewById<Button>(R.id.button6)
            var button7 = dialogo.findViewById<Button>(R.id.button7)

            baseRemota.collection("Mensajes")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException!=null){
                        Toast.makeText(this,"No se pude realizar busqueda",Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    for(document in querySnapshot!!){
                        editText5.setText(document.getString("Deseado"))
                        editText6.setText(document.getString("Nodeseado"))
                    }
                }

            button6.setOnClickListener {
                var valor1=editText5.text.toString()
                var valor2=editText6.text.toString()
                if(editText6.text.toString().equals("") || editText5.text.toString().equals("")){
                    Toast.makeText(this,"Agrege mensaje", Toast.LENGTH_LONG).show()
                }else{
                    baseRemota.collection("Mensajes").document("WgURdDqDcwiHgQesgqtq")
                        .update(
                            "Deseado",valor1,"Nodeseado",valor2
                        )
                        .addOnSuccessListener {
                            Toast.makeText(this,"Modifico correctamente",Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this,"Error no en la red", Toast.LENGTH_LONG).show()
                        }
                }
                dialogo.dismiss()
            }

            button7.setOnClickListener {
                dialogo.dismiss()
            }

            dialogo.show()
        }

    }

    private fun entregar(position: Int) {
        AlertDialog.Builder(this).setTitle("Atencion").setMessage("¿Se entregara el pedido \n ${dataLista[position]}?")
            .setPositiveButton("Eliminar"){d,w->
                //entregarpedido(listaID[position])
            }
            .setNegativeButton("Cancelar"){d,w->
                //actulizar(listaID[position])
            }
            .setNeutralButton("Pasar a no deseados"){dialog, which ->
            }
            .show()
    }

    private fun leerSMSentrada() {
        var cursor = contentResolver.query(Uri.parse("content://call_log/calls/"),null, null, null, null)

        var resultado=""

        if (cursor!!.moveToFirst()){
            var posCelularOrigen = cursor.getColumnIndex("NUMBER")
            var posColumnaMensaje = cursor.getColumnIndex("TYPE")
            var posColumnaFecha = cursor.getColumnIndex("DATE")
            do{
                if(cursor.getInt(posColumnaMensaje)==3){
                    val fechaMensaje = cursor.getString(posColumnaFecha)
                    resultado += "ORIGEN: "+cursor.getString(posCelularOrigen)+
                            "\nMensaje: "+cursor.getString(posColumnaMensaje)+
                            "\nFecha: "+ Date(fechaMensaje.toLong()) +"\n--------------------------------\n"
                }
            }while (cursor.moveToNext())
        }else{
            resultado = "No hay sms en la bandeja de entreda"
        }
        textView2.setText(resultado)
    }


    fun envioSMS() {

        baseRemota.collection("llamada")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException!=null){
                    Toast.makeText(this,"No se pude realizar busqueda",Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                for(document in querySnapshot!!){
                    telefono= document.getString("numero").toString()
                }
            }

        if (telefono==""){
        }else{
            baseRemota.collection("consulta").whereEqualTo("telefono", telefono)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    if (firebaseFirestoreException!=null){
                        Toast.makeText(this,"No se pude realizar busqueda", Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    for(document in querySnapshot!!) {
                        mensaje = document.get("estatus").toString()
                    }
                }
            if(mensaje=="false"){

                baseRemota.collection("Mensajes")
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        for (document in querySnapshot!!) {
                            SmsManager.getDefault().sendTextMessage(telefono,null,document.getString("Nodeseado"), null, null)
                            Toast.makeText(this, "se envio el SMS", Toast.LENGTH_LONG).show()
                        }
                    }


            }else{
                baseRemota.collection("Mensajes")
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        for (document in querySnapshot!!) {
                            SmsManager.getDefault().sendTextMessage(telefono,null,document.getString("Deseado"), null, null)
                            Toast.makeText(this, "se envio el SMS", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            baseRemota.collection("llamada").document("yryTj4apcmMAB2o97nxg")
                .update(
                    "numero",""
                )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==siPermiso){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.SEND_SMS), siPermiso3)
            }
        }
    }
}
