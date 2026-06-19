package com.lnxteam.atmoscope.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lnxteam.atmoscope.data.ConstellationData
import com.lnxteam.atmoscope.data.ConstellationInfo
import java.time.LocalDate

@Composable
fun AstroConstellationPage() {
    val currentMonth = LocalDate.now().monthValue
    val constellations = ConstellationData.forMonth(currentMonth)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Rasi bintang terlihat bulan ini", color = Color(0xFF8B949E), fontSize = 13.sp)
        if (constellations.isEmpty()) {
            Text("Belum ada data rasi untuk bulan ini", color = Color.White)
        } else {
            constellations.forEach { c -> ConstellationCard(c) }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ConstellationCard(c: ConstellationInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(c.emoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(c.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text(c.latinName, color = Color(0xFF8B949E), fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width; val h = size.height
                c.lines.forEach { (a, b) ->
                    val pa = c.starPoints[a]; val pb = c.starPoints[b]
                    drawLine(
                        color = Color(0xFFA78BFA).copy(alpha = 0.6f),
                        start = Offset(pa.first * w, pa.second * h),
                        end = Offset(pb.first * w, pb.second * h),
                        strokeWidth = 2f
                    )
                }
                c.starPoints.forEach { (x, y) ->
                    drawCircle(color = Color.White, radius = 4f, center = Offset(x * w, y * h))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(c.description, color = Color(0xFF8B949E), fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}