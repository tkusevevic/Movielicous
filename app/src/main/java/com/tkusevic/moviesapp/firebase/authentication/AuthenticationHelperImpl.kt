package com.tkusevic.moviesapp.firebase.authentication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.tkusevic.moviesapp.commons.extensions.mapToUser
import com.tkusevic.moviesapp.data.model.User
import com.tkusevic.moviesapp.firebase.RequestListener
import com.tkusevic.moviesapp.firebase.UserRequestListener
import com.tkusevic.moviesapp.firebase.database.DatabaseHelper
import javax.inject.Inject

/**
 * Created by tkusevic on 14.02.2018..
 */
class AuthenticationHelperImpl @Inject constructor(private val firebaseAuth: FirebaseAuth, private val databaseHelper: DatabaseHelper) : AuthenticationHelper {

    override fun editUser(user: User, listener: UserRequestListener) {
        databaseHelper.saveUser(user)
        listener.onSuccessfulRequest(user)
    }

    override fun attemptToRegisterTheUser(email: String, password: String, name: String, listener: RequestListener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                firebaseAuth.currentUser?.run {
                    val mappedUser = user?.mapToUser()
                    mappedUser?.userDisplayName = name
                    mappedUser?.let { databaseHelper.saveUser(it) }
                    listener.onSuccessfulRequest()
                }
            } else {
                listener.onFailedRequest()
            }
        }
    }

    override fun logTheUserIn(email: String, password: String, listener: UserRequestListener) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseAuth.currentUser?.run {
                    databaseHelper.getUser(uid, { listener.onSuccessfulRequest(it) })
                }
            } else {
                listener.onFailedRequest()
            }
        }
    }

    override fun setUserDisplayName(username: String) {
        var username = username
        val currentUser = firebaseAuth.currentUser
        if (username.isEmpty()) {
            username = "randomUsername"
        }
        if (currentUser != null) {
            val request = UserProfileChangeRequest.Builder().setDisplayName(username).build()
            currentUser.updateProfile(request).addOnCompleteListener { }
        }
    }

    override fun logTheUserOut() {
        firebaseAuth.signOut()
    }

    override fun checkIfUserIsLoggedIn(): Boolean {
        return (firebaseAuth.currentUser != null)
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    override fun getCurrentUser(): FirebaseUser? {
        return (firebaseAuth.currentUser)
    }

    override val currentUserDisplayName: String = firebaseAuth.currentUser?.displayName.toString()
}