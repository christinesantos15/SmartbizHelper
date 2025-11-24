package com.example.smartbizhelper.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.smartbizhelper.EditProfileActivity
import com.example.smartbizhelper.LoginActivity
import com.example.smartbizhelper.R
import com.example.smartbizhelper.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        setupClickListeners()
        updateUI(auth.currentUser)
        loadPreferences()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateUI(auth.currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.profileName.text = user.displayName ?: "No Name"
            binding.profileEmail.text = user.email
        } else {
            binding.profileName.text = "User Name"
            binding.profileEmail.text = "user.email@example.com"
        }
    }

    private fun setupClickListeners() {
        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.editProfileButton.setOnClickListener {
            startActivity(Intent(activity, EditProfileActivity::class.java))
        }

        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference(isChecked)
        }

        binding.themeSetting.setOnClickListener {
            showThemeDialog()
        }

        binding.languageSetting.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun loadPreferences() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val notificationsEnabled = sharedPref.getBoolean("notifications_enabled", true)
        binding.notificationsSwitch.isChecked = notificationsEnabled
    }

    private fun saveNotificationPreference(isEnabled: Boolean) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putBoolean("notifications_enabled", isEnabled)
            apply()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val currentTheme = sharedPref.getInt("theme_mode", 2)

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                val selectedMode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(selectedMode)
                saveThemePreference(which)
                dialog.dismiss()
            }
            .show()
    }

    private fun saveThemePreference(themeMode: Int) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putInt("theme_mode", themeMode)
            apply()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Filipino")
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val currentLanguage = sharedPref.getString("language", "en")
        val currentLangIndex = if (currentLanguage == "en") 0 else 1

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Language")
            .setSingleChoiceItems(languages, currentLangIndex) { dialog, which ->
                val selectedLanguage = if (which == 0) "en" else "fil"
                setLocale(selectedLanguage)
                saveLanguagePreference(selectedLanguage)
                dialog.dismiss()
                activity?.recreate()
            }
            .show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = resources
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun saveLanguagePreference(language: String) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("language", language)
            apply()
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
