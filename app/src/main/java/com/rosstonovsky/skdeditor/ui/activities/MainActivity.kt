package com.rosstonovsky.skdeditor.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rosstonovsky.skdeditor.ui.theme.SKDETheme
import com.rosstonovsky.catbox.CatManager
import java.lang.Exception


class MainActivity : ComponentActivity() {
	companion object {
		@SuppressLint("StaticFieldLeak")
		lateinit var mainContext : Context
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		mainContext = this
		try {
			CatManager.init(this);
		} catch (e : Exception) {
			e.printStackTrace()
			// If app_process runs without root permissions,
			// it won't run and catbox will end of stream because missing permissions to change dalvik-cache ownership.
			// But it can also mean app_process can't run on device
			if (e.message != null && e.message!!.contains("catbox reached")) {
				Toast.makeText(this, "app_process can't run on this device. Did you grant root permissions?", Toast.LENGTH_LONG).show()
				finish()
				return
			}
			Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
		}

		setContent {
			SKDETheme {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colorScheme.background
				) {
					Greeting("Android")
				}
			}
		}
	}
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
	Text(
		text = "Hello $name!",
		modifier = modifier.padding(all = 8.dp)
	)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
	SKDETheme {
		Greeting("Android")
	}
}