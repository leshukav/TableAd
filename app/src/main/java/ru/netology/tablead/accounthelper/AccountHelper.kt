package ru.netology.tablead.accounthelper

import android.util.Log
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import ru.netology.tablead.MainActivity
import ru.netology.tablead.R
import ru.netology.tablead.constants.FirebaseAuthConstants
import ru.netology.tablead.dialoghelper.GoogleAccountConst

class AccountHelper(act: MainActivity) {
    private val activity = act
    private lateinit var signInClient: GoogleSignInClient

    fun sinUpWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    activity.myAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                signUpWithEmailSuccessful(task.result.user!!)
                            } else {
                                signUpWithEmailExeption(task.exception!!, email, password)
                            }
                        }
                }
            }

        }
    }

    private fun signUpWithEmailExeption(e: Exception, email: String, password: String) {
        Log.d("MyLod", "Ex  ${e}")
        if (e is FirebaseAuthUserCollisionException) {
            val exception = e as FirebaseAuthUserCollisionException
            //    Log.d("MyLog", " ${exception.errorCode}" )
            if (exception.errorCode == FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE) {
                Toast.makeText(
                    activity,
                    FirebaseAuthConstants.ERROR_EMAIL_ALREADY_IN_USE,
                    Toast.LENGTH_LONG
                ).show()

                linkEmailToGoogle(email, password)  //  Link Email to Google
            }
        } else if (e is FirebaseAuthInvalidCredentialsException) {
            val exception =
                e as FirebaseAuthInvalidCredentialsException
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {
                Toast.makeText(
                    activity,
                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        if (e is FirebaseAuthWeakPasswordException) {
            //   val exception = task.exception as FirebaseAuthWeakPasswordException
            if (e.errorCode == FirebaseAuthConstants.ERROR_WEAK_PASSWORD) {
                Toast.makeText(
                    activity,
                    FirebaseAuthConstants.ERROR_WEAK_PASSWORD,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun signUpWithEmailSuccessful(user: FirebaseUser) {
        sendEmailVerification(user)
        activity.uiUpdate(user)

    }

    fun sinInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            activity.myAuth.currentUser?.delete()?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    activity.myAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                activity.uiUpdate(task.result?.user)
                                Log.d("MyLog", " ${task.result}")
                            } else {
                                signInWithEmailException(task.exception!!, email, password)
                            }
                        }
                }
            }

        }
    }

    private fun signInWithEmailException(e: java.lang.Exception, email: String, password: String) {
        Log.d("MyLog", " ${e}")
        if (e is FirebaseAuthInvalidCredentialsException) {
            val exception =
                e as FirebaseAuthInvalidCredentialsException
            if (exception.errorCode == FirebaseAuthConstants.ERROR_INVALID_EMAIL) {

                Toast.makeText(
                    activity,
                    FirebaseAuthConstants.ERROR_INVALID_EMAIL,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        if (e is FirebaseAuthInvalidUserException) {
            if (e.errorCode == FirebaseAuthConstants.ERROR_USER_NOT_FOUND) {
                Toast.makeText(
                    activity,
                    FirebaseAuthConstants.ERROR_USER_NOT_FOUND,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun linkEmailToGoogle(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        if (activity.myAuth.currentUser != null) {
            activity.myAuth.currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            activity,
                            activity.resources.getString(R.string.link_is_successful),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                activity,
                activity.resources.getString(R.string.enter_google_account_to_register_email),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id)).requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }

    fun signWithGoogle() {
        signInClient = getSignInClient()
        val intent = signInClient.signInIntent
        activity.googleSignInLauncher.launch(intent)
    }

    fun signOutGoogle() {
        getSignInClient().signOut()
    }

    fun signInFirebaseWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        activity.myAuth.currentUser?.delete()?.addOnCompleteListener {
            if (it.isSuccessful) {
                activity.myAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        activity.uiUpdate(task.result?.user)
                        Toast.makeText(
                            activity,
                            activity.resources.getString(R.string.sign_with_google_account),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Log.d("MyLog", "Exeption ${task.exception}")
                    }
                }
            }
        }
    }

    private fun sendEmailVerification(user: FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.send_verification_done),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.send_verification_email_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    }

    fun signInAnonimysly(listener: Listener) {
        activity.myAuth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                listener.onComplete()
                Toast.makeText(activity, "You signIn of anonimus", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "You can't signIn of anonimus", Toast.LENGTH_LONG).show()
            }
        }
    }

    interface Listener {
        fun onComplete()
    }
}