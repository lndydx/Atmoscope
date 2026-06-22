package com.lnxteam.atmoscope.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lnxteam.atmoscope.viewmodel.AstroViewModel
import com.lnxteam.atmoscope.viewmodel.UiState

@Composable
fun AstroApodPage(astroViewModel: AstroViewModel) {
    val apodState by astroViewModel.apodState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) { astroViewModel.loadApod() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        when (val state = apodState) {
            is UiState.Loading, is UiState.Idle -> LoadingIndicator("Memuat foto hari ini")
            is UiState.Error -> ErrorCard(state.message)
            is UiState.Success -> {
                val apod = state.data
                Text(apod.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(apod.date, color = Color(0xFF8B949E), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))

                if (apod.mediaType == "image" && apod.url != null) {
                    AsyncImage(
                        model = apod.url,
                        contentDescription = apod.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎬", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Konten hari ini berupa video", color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = {
                                apod.url?.let {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                                }
                            }) {
                                Text("Tonton di Browser")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    apod.explanation,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}