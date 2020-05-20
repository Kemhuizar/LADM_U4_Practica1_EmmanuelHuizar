package mx.edu.ittepic.ladm_u4_practica1_emmanuelhuizar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*

class Main2Activity : AppCompatActivity() {
    var baseRemota= FirebaseFirestore.getInstance()
    var dataLista=ArrayList<String>()
    var listaID=ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        llenartabla()

        button.setOnClickListener {
            finish()
        }

        nono.setOnClickListener {
            llenartabla()
        }
    }

    private fun llenartabla() {
        var cuales=true
        if (nono.isChecked==true){
            cuales =false
        }
        baseRemota.collection("consulta").whereEqualTo("estatus",cuales)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException!=null){
                    Toast.makeText(this,"No se pude realizar busqueda", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                dataLista.clear()
                listaID.clear()
                for(document in querySnapshot!!){
                    var cadena = "Nombre: "+document.getString("nombre")+"\nTelefono: "+document.getString("telefono")+"\n"
                    dataLista.add(cadena)
                    listaID.add(document.id)
                }
                if(dataLista.size==0){
                    dataLista.add("No hay contactos no deseados")
                }
                var adaptador = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataLista)
                lista2.adapter=adaptador
            }
        lista2.setOnItemClickListener { parent, view, position, id ->
            if(listaID.size==0){
                return@setOnItemClickListener
            }
            entregar(position,cuales)
        }
    }

    private fun entregar(position: Int,comprobar:Boolean) {
        var quees=""
        if(comprobar==false){quees=" Deseados"}else{quees=" No deseados"}
        AlertDialog.Builder(this).setTitle("Atencion").setMessage("¿Se entregara el pedido \n ${dataLista[position]}?")
            .setPositiveButton("Eliminar"){d,w->
                elimarpedido(position)
            }
            .setNegativeButton("Cancelar"){d,w->
            }
            .setNeutralButton("Pasar a ${quees}"){dialog, which ->
                actcualizar(position,comprobar)
            }
            .show()
    }

    private fun actcualizar(position: Int,comprobar1:Boolean) {
        var scomprobar=comprobar1
        if (scomprobar==false){
            scomprobar=true
        }else{
            scomprobar=false
        }
        baseRemota.collection("consulta").document(listaID[position])
            .update(
                "estatus",scomprobar
            )
    }

    private fun elimarpedido(position: Int) {
        AlertDialog.Builder(this).setTitle("Atencion").setMessage("¿Desea eliminar al contacto \n ${dataLista[position]}?")
            .setPositiveButton("Eliminar"){d,w->
                baseRemota.collection("consulta").document(listaID[position]).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this,"Se elimino con exito",Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"No se pudo eliminar",Toast.LENGTH_LONG).show()
                    }
            }
            .setNeutralButton("Cancelar"){dialog, which ->
            }
            .show()
    }
}
