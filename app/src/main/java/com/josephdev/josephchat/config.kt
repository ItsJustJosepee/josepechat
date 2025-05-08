package com.josephdev.josephchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.josephdev.josephchat.ActivityUtils.openActivityAndClear
import kotlinx.coroutines.launch

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [config.newInstance] factory method to
 * create an instance of this fragment.
 */
class config : Fragment() {
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí ya puedes usar context sin problemas
        googleAuthClient = GoogleAuthClient(requireContext())

        val logoutButton = view.findViewById<Button>(R.id.logout)
        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                logout()
            }
        }
    }

    private suspend fun logout() {
        googleAuthClient.signOut()
        openActivityAndClear(requireActivity(), jchatlogin::class.java)
    }
}
