package com.rosstonovsky.skdeditor.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rosstonovsky.catbox.CatManager
import com.rosstonovsky.skdeditor.Const
import com.rosstonovsky.skdeditor.R
import com.rosstonovsky.skdeditor.ui.theme.SKDETheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.roundToInt


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
			runBlocking {
				if (withTimeoutOrNull(10_000) {
						CatManager.init(mainContext)
					} == null)
					Toast.makeText(mainContext, R.string.catbox_timeout, Toast.LENGTH_SHORT).show()
			}
		} catch (e: Exception) {
			e.printStackTrace()
			// If app_process runs without root permissions,
			// it won't run and catbox will end of stream because missing permissions to change dalvik-cache ownership.
			// But it can also mean app_process can't run on device
			e.message?.let {
				if (it.contains("catbox reached")) {
					Toast.makeText(
						this,
						R.string.catbox_failed,
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
			CardBox()
		}
	}
}

var cardWidth by mutableIntStateOf(0)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardBox() {
	val velocityThreshold = 50.dp.toPx().toFloat()
	val state = remember {
		AnchoredDraggableState(
			initialValue = 0,
			positionalThreshold = { distance: Float -> distance * 0.3f },
			velocityThreshold = { velocityThreshold },
			animationSpec = tween()
		).apply {
			updateAnchors(
				DraggableAnchors {
					0 at 0f
					1 at 0f
				}
			)
		}
	}
	val pad = 16.dp.toPx()
	Box(
		modifier = Modifier
			.wrapContentSize(align = Alignment.TopCenter)
			.fillMaxWidth()
			.onGloballyPositioned {
				state.apply {
					updateAnchors(
						DraggableAnchors {
							0 at 0f
							1 at -it.size.width.toFloat() + pad
						}
					)
				}
			}
	) {
		/*
		Notes:
		for some reason state.progress becomes 1 while not on any anchor
		fadeOut doesn't really work well, so I'm using different exit animation
		 */
		AnimatedVisibility(
			visible = (state.currentValue == 0 && ((state.requireOffset() < cardWidth) || cardWidth == 0)),
			enter = fadeIn(animationSpec = tween(150, 0, FastOutSlowInEasing)),
			exit = slideOutHorizontally(animationSpec = tween(100, 0, FastOutSlowInEasing))
		) {
			InfoCard(state = state)
		}
		SettingsCard(state = state)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoCard(state: AnchoredDraggableState<Int>) {
	val pad = 16.dp.toPx()
	Card(
		modifier = Modifier
			.wrapContentSize(align = Alignment.TopCenter)
			.padding(16.dp)
			.fillMaxWidth()
			.onGloballyPositioned {
				cardWidth = it.size.width + pad
			}
			.offset {
				IntOffset(
					x = state
						.requireOffset()
						.roundToInt(), y = 0
				)
			}
			.anchoredDraggable(state, Orientation.Horizontal),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors()
	) {
		Text(
			modifier = Modifier
				.padding(12.dp),
			fontSize = 13.sp,
			text = "SKDE version: " + LocalContext.current.packageManager
				.getPackageInfoCompat(LocalContext.current.packageName).versionName +
					"\nSoul Knight version: " + LocalContext.current.packageManager
				.getPackageInfoCompat(Const.pkg).versionName +
					//TODO
					"\nInformation version: "
		)
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsCard(state: AnchoredDraggableState<Int>) {
	Card(
		modifier = Modifier
			.padding(16.dp)
			.width(with(LocalDensity.current) { cardWidth.toDp() })
			.offset {
				IntOffset(
					x = state
						.requireOffset()
						.roundToInt() + cardWidth, y = 0
				)
			}
			.anchoredDraggable(state, Orientation.Horizontal),
		shape = RoundedCornerShape(8.dp),
		colors = CardDefaults.cardColors()
	) {
		Text(
			modifier = Modifier
				.padding(12.dp),
			fontSize = 13.sp,
			text = "Test"
		)
	}
}

fun PackageManager.getPackageInfoCompat(packageName: String): PackageInfo =
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
	} else {
		getPackageInfo(packageName, 0)
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