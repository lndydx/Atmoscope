package com.lnxteam.atmoscope.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnxteam.atmoscope.ui.theme.Purple
import com.lnxteam.atmoscope.ui.theme.PurpleLight

@Composable
fun AstroCalculatorPage() {
    var focalLengthText by remember { mutableStateOf("24") }
    var cropFactor by remember { mutableStateOf(1.0) }

    val focalLength = focalLengthText.toDoubleOrNull()
    val result = if (focalLength != null && focalLength > 0) 500.0 / (focalLength * cropFactor) else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📸 Kalkulator Astrofotografi", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        Text(
            "Aturan 500 — batas shutter speed agar bintang tidak blur",
            color = Color(0xFF8B949E),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = focalLengthText,
            onValueChange = { focalLengthText = it },
            label = { Text("Focal Length (mm)", color = Color(0xFF8B949E)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleLight,
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Tipe Sensor",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("Full Frame", 1.0, "FF"),
                Triple("APS-C", 1.5, "APS-C"),
                Triple("Smartphone", 6.0, "Phone")
            ).forEach { (label, factor, _) ->
                FilterChip(
                    selected = cropFactor == factor,
                    onClick = { cropFactor = factor },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Purple,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Waktu Maksimal Shutter", color = Color(0xFF8B949E), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (result != null) "%.1f detik".format(result) else "—",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PurpleLight
                )
            }
        }
    }
}