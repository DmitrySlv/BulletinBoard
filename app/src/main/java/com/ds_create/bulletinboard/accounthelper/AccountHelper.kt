package com.ds_create.bulletinboard.accounthelper

import android.util.Log
import android.widget.Toast
import com.ds_create.bulletinboard.MainActivity
import com.ds_create.bulletinboard.R
import com.ds_create.bulletinboard.constants.FirebaseAuthConstants
import com.ds_create.bulletinboard.dialoghelper.GoogleAccConst
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*

class AccountHelper(val act: MainActivity) {
    private lateinit var signInClient: GoogleSignInClient

    fun signUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signUpWithEmailSuccessful(task.result.user!!)
                            } else {
                                signUpWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        act.uiUpdate(user)
    }

    private fun signUpWithEmailException(e: Exception, email: String, password: String) {
        //Toast.makeText(act, act.resources.getString(R.string.sign_up_error), Toast.LENGTH_LONG).show()
        //Log.d("MyLog", "Exception: + ${exception.errorCode}")
        //Log.d("MyLog", "Exception: ${e}")
        if (e is FirebaseAuthUserCollisionException) {
            val exception = e as FirebaseAuthUserCollisionException
            if (e.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                //Toast.makeText(act, FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE, Toast.LENGTH_LONG).show()
                linkEmailToG(email, password)
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            val exception =
                e as FirebaseAuthInvalidCredentialsException
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            // Log.d("MyLog", "Exception: + ${exception.errorCode}")
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WEAK_PASSWORD, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun linkEmailToG(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (act.mAuth.currentUser != null) {
            act.mAuth.currentUser?.linkWithCredential(credential)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        act,
                        act.resources.getString(R.string.link_done),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        } else {
            Toast.makeText(act, act.resources.getString(R.string.enter_to_g), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(act.getString(R.string.default_web_client_id)).requestEmail()
            .build()
        return GoogleSignIn.getClient(act, gso)
    }

    fun signInWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        act.googleSignInLauncher.launch(intent)
    }

    fun signOutG() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                act.mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(act, "Sign in done", Toast.LENGTH_LONG).show()
                        act.uiUpdate(task.result?.user)
                    } else {
                        Log.d("MyLog", "Google SignIn Exception: ${task.exception}")
                    }
                }
            }

        }
    }

    fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            act.mAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    act.mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                act.uiUpdate(task.result?.user)
                            } else {
                                signInWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }
        }
    }

    private fun signInWithEmailException(e: Exception, email: String, password: String) {
       // Log.d("MyLog", "Exception: ${e}")
        if (e is FirebaseAuthInvalidCredentialsException) {
            //Log.d("MyLog", "Exception: ${task.exception}")
            val exception =
                e as FirebaseAuthInvalidCredentialsException
            //Log.d("MyLog", "Exception 2 : ${exception.errorCode}")
            if (e.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_INVALID_EMAIL, Toast.LENGTH_LONG).show()
            } else if (e.errorCode == FirebaseAuthConstants.ERROR_WRONG_PASSWORD) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_WRONG_PASSWORD, Toast.LENGTH_LONG).show()
            }
        } else if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(act, FirebaseAuthConstants.ERROR_USER_NOT_FOUND, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    act,
                    act.resources.getString(R.string.send_verification_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun signInAnonymously(listener: Listener) {
        act.mAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(act, "Вы вошли как гость", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(act, "Не удалось войти как гость", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}