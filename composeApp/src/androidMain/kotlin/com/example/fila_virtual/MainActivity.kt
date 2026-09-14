package com.example.fila_virtual

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {


    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var invitationToken by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invitationToken = savedInstanceState?.getString("invitation_token")
            ?: intent.getStringExtra("invitation_token")
            ?: intent.data?.getQueryParameter("token")
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("385041919843-v9f3p7kntedtho0612ivkvcdgjsse0jh.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = true

            App(
                onGoogleSignIn = { startGoogleSignIn() },
                onSignOut = { signOut() },
                invitationToken = invitationToken
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        invitationToken = intent.getStringExtra("invitation_token")
            ?: intent.data?.getQueryParameter("token")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("invitation_token", invitationToken)
        super.onSaveInstanceState(outState)
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            }  catch (e: ApiException) {
            // ESTO es lo que verás en el Logcat ahora:
            android.util.Log.e("GOOGLE_DEBUG", "Error Code: ${e.statusCode}")
            android.util.Log.e("GOOGLE_DEBUG", "Causa: ${android.util.Log.getStackTraceString(e)}")

                Toast.makeText(this, "ERROR GOOGLE: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.let {
                        saveUserToFirestore(it.uid, it.displayName, it.email, it.photoUrl?.toString())
                    }
                    Toast.makeText(this, "Sesión iniciada con Google", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error en Firebase: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Archivo: /home/mhuitron/Documentos/git/Fila_Virtual/composeApp/src/androidMain/kotlin/com/example/fila_virtual/MainActivity.kt

    private fun saveUserToFirestore(uid: String, name: String?, email: String?, photoUrl: String?) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("usuarios").document(uid)

        userRef.get().addOnSuccessListener { document ->
            val now = System.currentTimeMillis() // Usamos Long para createdAt/updatedAt

            if (!document.exists()) {
                // Creamos el documento con la estructura exacta de la data class Usuario
                val userData = hashMapOf(
                    "nombre" to (name ?: "Usuario Google"),
                    "email" to (email ?: ""),
                    "telefono" to "",
                    "fotoUrl" to (photoUrl ?: ""),
                    "rolGlobal" to "cliente", // Coincide con Roles.CLIENTE
                    "metodosPago" to listOf<String>(),
                    "verificado" to true,    // Los usuarios de Google ya están verificados
                    "activo" to true,
                    "createdAt" to now,
                    "updatedAt" to now
                )
                userRef.set(userData)
            } else {
                // Si el usuario ya existe, solo actualizamos la foto y la fecha de última conexión
                val updates = mutableMapOf<String, Any>()
                updates["updatedAt"] = now
                if (photoUrl != null) {
                    updates["fotoUrl"] = photoUrl
                }
                userRef.update(updates)
            }
        }
    }
}