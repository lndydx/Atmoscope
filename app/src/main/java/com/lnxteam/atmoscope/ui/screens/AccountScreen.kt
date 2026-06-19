package com.lnxteam.atmoscope.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnxteam.atmoscope.ui.theme.Purple
import com.lnxteam.atmoscope.ui.theme.PurpleLight
import com.lnxteam.atmoscope.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoggedOut: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayName = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Pengguna"
    val email = currentUser?.email ?: "-"
    val initials = displayName.take(1).uppercase()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A0E1A), Color(0xFF0D1117))))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Akun Saya", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Purple, PurpleLight))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(displayName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(email, fontSize = 14.sp, color = Color(0xFF8B949E))

                Spacer(modifier = Modifier.height(40.dp))

                // Logout button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    TextButton(
                        onClick = {
                            authViewModel.logout()
                            onLoggedOut()
                        },
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = PurpleLight)
                            Text("Ganti Akun / Logout", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delete account button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A0D0D)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            Text("Hapus Akun", color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF161B22),
            title = { Text("Hapus Akun?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Akun kamu akan dihapus permanen. Tindakan ini tidak bisa dibatalkan.",
                    color = Color(0xFF8B949E)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.deleteAccount { onLoggedOut() }
                    showDeleteDialog = false
                }) {
                    Text("Hapus", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = PurpleLight)
                }
            }
        )
    }
}