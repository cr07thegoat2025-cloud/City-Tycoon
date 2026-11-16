package com.citytycoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.floor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = 5894 or 1024 or 512
        setContent { CityTycoonApp() }
    }
}

private data class Plot(val x: Int, val y: Int, val kind: Int)
private val grass = Color(0xFF91B978)

@Composable
fun CityTycoonApp() {
    var cash by remember { mutableIntStateOf(12000) }
    var selectedTool by remember { mutableStateOf("Select") }
    var camera by remember { mutableStateOf(Offset.Zero) }
    val plots = remember { mutableStateListOf<Plot>() }
    var selected by remember { mutableStateOf<Plot?>(null) }
    MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFD9B66F), background = Color(0xFF17221F), surface = Color(0xFF26332E), onSurface = Color(0xFFF4F0E6))) {
        Column(Modifier.fillMaxSize().background(Color(0xFF17221F))) {
            Row(Modifier.fillMaxWidth().background(Color(0xFF202D28)).padding(horizontal = 18.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CITY TYCOON", style = MaterialTheme.typography.titleLarge, color = Color(0xFFE6C77A))
                Text("CASH  $" + cash, color = Color.White)
                Text("POPULATION  " + (plots.count { it.kind in 2..4 } * 12), color = Color.White)
                Text("CITY LEVEL  1", color = Color(0xFFE6C77A))
            }
            Row(Modifier.weight(1f).fillMaxWidth()) {
                Column(Modifier.width(142.dp).fillMaxHeight().background(Color(0xFF26332E)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("BUILD", color = Color(0xFFE6C77A), style = MaterialTheme.typography.titleMedium)
                    listOf("Select", "Road", "House", "Shop", "Factory", "Park").forEach { tool ->
                        Button(onClick = { selectedTool = tool; selected = null }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (selectedTool == tool) Color(0xFF8A7548) else Color(0xFF394740)), contentPadding = PaddingValues(4.dp)) { Text(tool) }
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Drag to move the map", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                }
                Box(Modifier.weight(1f).fillMaxHeight()) {
                    Canvas(Modifier.fillMaxSize()
                        .pointerInput(selectedTool, camera) {
                            detectTapGestures { p ->
                                val cell = 54f
                                val gx = floor((p.x - size.width / 2f - camera.x) / cell).toInt()
                                val gy = floor((p.y - size.height / 2f - camera.y) / cell).toInt()
                                val existing = plots.find { it.x == gx && it.y == gy }
                                if (selectedTool == "Select") selected = existing
                                else if (existing == null && cash >= cost(selectedTool)) {
                                    val kind = when (selectedTool) { "Road" -> 1; "House" -> 2; "Shop" -> 3; "Factory" -> 4; else -> 5 }
                                    plots.add(Plot(gx, gy, kind)); cash -= cost(selectedTool)
                                }
                            }
                        }
                        .pointerInput(Unit) { detectDragGestures { change, drag -> change.consume(); camera += drag } }
                    ) {
                        drawRect(grass)
                        val cell = 54f
                        val center = Offset(size.width / 2f + camera.x, size.height / 2f + camera.y)
                        val left = floor(-center.x / cell).toInt() - 1
                        val right = floor((size.width - center.x) / cell).toInt() + 1
                        val top = floor(-center.y / cell).toInt() - 1
                        val bottom = floor((size.height - center.y) / cell).toInt() + 1
                        for (x in left..right) for (y in top..bottom) {
                            val px = center.x + x * cell; val py = center.y + y * cell
                            drawRect(Color(0x22708058), Offset(px, py), androidx.compose.ui.geometry.Size(cell, cell))
                            drawRect(Color(0x33708058), Offset(px, py), androidx.compose.ui.geometry.Size(cell, cell), style = androidx.compose.ui.graphics.drawscope.Stroke(1f))
                        }
                        plots.forEach { plot ->
                            val px = center.x + plot.x * cell; val py = center.y + plot.y * cell
                            val c = when (plot.kind) { 1 -> Color(0xFF626C70); 2 -> Color(0xFFE8D8B8); 3 -> Color(0xFFB8A0C9); 4 -> Color(0xFF9B7770); else -> Color(0xFF4D8B5C) }
                            drawRoundRect(c, Offset(px + 3, py + 3), androidx.compose.ui.geometry.Size(cell - 6, cell - 6), androidx.compose.ui.geometry.CornerRadius(5f))
                            if (plot.kind in 2..4) {
                                drawRect(Color(0xFF3E4844), Offset(px + cell * .25f, py + cell * .18f), androidx.compose.ui.geometry.Size(cell * .5f, cell * .18f))
                                drawRect(Color(0xFF3E4844), Offset(px + cell * .25f, py + cell * .52f), androidx.compose.ui.geometry.Size(cell * .5f, cell * .18f))
                            }
                            if (selected == plot) drawRoundRect(Color(0xFFFFE28A), Offset(px + 1, py + 1), androidx.compose.ui.geometry.Size(cell - 2, cell - 2), androidx.compose.ui.geometry.CornerRadius(5f), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
                        }
                    }
                    selected?.let { plot ->
                        Surface(Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(14.dp), color = Color(0xEE26332E)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(when (plot.kind) { 1 -> "Road"; 2 -> "House"; 3 -> "Shop"; 4 -> "Factory"; else -> "Park" })
                                Text("Tile " + plot.x + ", " + plot.y, color = Color.LightGray)
                                TextButton(onClick = { plots.remove(plot); selected = null }) { Text("Demolish") }
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().background(Color(0xFF202D28)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("A quiet town is waiting to grow.", color = Color.LightGray)
                Text("DAY 1  •  NORMAL SPEED", color = Color(0xFFE6C77A))
            }
        }
    }
}

private fun cost(tool: String) = when (tool) { "Road" -> 40; "House" -> 500; "Shop" -> 850; "Factory" -> 1200; "Park" -> 300; else -> 0 }
