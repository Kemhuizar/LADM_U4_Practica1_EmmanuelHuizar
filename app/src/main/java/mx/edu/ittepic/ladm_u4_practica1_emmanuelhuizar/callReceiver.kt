package mx.edu.ittepic.ladm_u4_practica1_emmanuelhuizar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.google.firebase.firestore.FirebaseFirestore

class callReceiver:BroadcastReceiver(){
    var baseRemota= FirebaseFirestore.getInstance()
    var cursor : Context ?= null
    var contesto = true

    override fun onReceive(context : Context, intent: Intent?) {
        try {
            cursor = context
            val tmgr = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val PhoneListener = MyPhoneStateListener()
            tmgr.listen(PhoneListener, PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
        }
    }

    private inner class MyPhoneStateListener : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if(state == 2){
                contesto = false
            }
            if (state == 0 && contesto == true) {
                val num = "$incomingNumber"
                try {
                    if(!num.isEmpty()) {
                        baseRemota.collection("llamada").document("yryTj4apcmMAB2o97nxg")
                            .update(
                                "numero",num
                            )
                    }
                } catch (err : Exception) {
                }
            }
        }
    }
}