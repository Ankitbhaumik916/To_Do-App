package com.example.mya

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import com.example.mya.ui.theme.MyATheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyATheme {
                SignetUI()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignetUI() {
    val context = LocalContext.current
    var genuineUri by remember { mutableStateOf<Uri?>(null) }
    var forgedUri by remember { mutableStateOf<Uri?>(null) }
    var result by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val pickGenuineImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        genuineUri = it
    }

    val pickForgedImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        forgedUri = it
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signet - Signature Verifier") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Upload Genuine Signature:", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { pickGenuineImage.launch("image/*") }) {
                Text("Choose Genuine Signature")
            }

            genuineUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Genuine Signature",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(vertical = 8.dp)
                )
            }

            Text("Upload Forged Signature:", style = MaterialTheme.typography.titleMedium)
            Button(onClick = { pickForgedImage.launch("image/*") }) {
                Text("Choose Forged Signature")
            }

            forgedUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Forged Signature",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    isLoading = true
                    result = null
                    scope.launch {
                        delay(2000) // TODO: Replace with real model call
                        result = if ((0..1).random() == 1) "✅ Genuine Signature" else "❌ Forged Signature"
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && genuineUri != null && forgedUri != null
            ) {
                Text("Verify Signature")
            }

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LottieLoading()
            }

            AnimatedVisibility(
                visible = result != null,
                enter = expandIn(expandFrom = Alignment.Center) + fadeIn(),
                exit = fadeOut()
            ) {
                ResultCard(result!!)
            }
        }
    }
}

@Composable
fun LottieLoading() {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("loading.json"))
    val progress by animateLottieCompositionAsState(composition)

    LottieAnimation(
        composition,
        progress,
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth()
    )
}

@Composable
fun ResultCard(result: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (result.contains("Genuine")) Color(0xFFD1FAE5) else Color(0xFFFFE4E6)
        )
    ) {
        Text(
            text = result,
            style = MaterialTheme.typography.titleLarge,
            color = if (result.contains("Genuine")) Color(0xFF059669) else Color(0xFFDC2626),
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}
