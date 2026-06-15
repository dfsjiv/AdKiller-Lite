package io.github.adkillerlite

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.adkillerlite.ui.AdKillerApp

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{AdKillerApp(application as AdKillerApplication){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))}}}}
