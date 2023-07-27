package com.rosstonovsky.skdeditor.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rosstonovsky.catbox.CatManager
import com.rosstonovsky.skdeditor.R
import com.rosstonovsky.skdeditor.ui.theme.SKDETheme


class MainActivity : ComponentActivity() {
	companion object {
		@SuppressLint("StaticFieldLeak")
		lateinit var mainContext: Context
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		installSplashScreen()
		super.onCreate(savedInstanceState)
		mainContext = this
		try {
			CatManager.init(this);
		} catch (e: Exception) {
			e.printStackTrace()
			// If app_process runs without root permissions,
			// it won't run and catbox will end of stream because missing permissions to change dalvik-cache ownership.
			// But it can also mean app_process can't run on device
			e.message?.let {
				if (it.contains("catbox reached")) {
					Toast.makeText(
						this,
						"app_process can't run on this device. Did you grant root permissions?",
						Toast.LENGTH_LONG
					).show()
				} else {
					Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
				}
			}
		}

		setContent {
			SKDETheme {
				SkdeSurface()
			}
		}
	}
}

@Composable
fun SkdeSurface() {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Row(
			modifier = Modifier
				.wrapContentSize(align = Alignment.TopCenter)
				.fillMaxWidth()
		) {
			AppIcon()
			InfoCard()
		}
	}
}

@Composable
fun InfoCard() {
	Card(
		modifier = Modifier
			.wrapContentSize(align = Alignment.TopCenter)
			.padding(16.dp)
			.fillMaxWidth(),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.secondary,
			contentColor = MaterialTheme.colorScheme.onSecondary
		)
	) {
		Text(text = "")
	}
}

@Composable
fun AppIcon() {
	Box(
		modifier = Modifier
			.size(128.dp)
			.height(128.dp)
			.height(128.dp)
			.padding(16.dp)
	) {
		LocalContext.current.getDrawable(R.drawable.ic_icon)?.toBitmap(
			128.dp.toPx(),
			128.dp.toPx(),
			null
		)?.let {
			Image(
				it.asImageBitmap(),
				contentDescription = "App icon",
				modifier = Modifier
					.size(128.dp)
					.width(128.dp)
					.height(128.dp)
					.clip(CircleShape),
				alignment = Alignment.TopStart,
				contentScale = ContentScale.FillBounds
			)
		}
	}
}

@Composable
fun Dp.toPx(): Int = with(LocalDensity.current) { this@toPx.toPx().toInt() }

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SkdePreview() {
	SKDETheme {
		SkdeSurface()
	}
}