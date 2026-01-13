package be.t_ars.ultranetscribblestrip

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleBackground
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleBlack
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleBlue
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleCyan
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleGreen
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleMagenta
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleRed
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleWhite
import be.t_ars.ultranetscribblestrip.ui.theme.ScribbleYellow
import be.t_ars.ultranetscribblestrip.ui.theme.borderWidth
import be.t_ars.ultranetscribblestrip.ui.theme.outerPadding
import be.t_ars.ultranetscribblestrip.xr18.UltranetChannelInfo
import be.t_ars.ultranetscribblestrip.xr18.XR18OSCAPI
import be.t_ars.ultranetscribblestrip.xr18.getUltranetInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ColorInfo(val background: Color, val border: Color, val text: Color)

val colors = arrayOf(
    ColorInfo(ScribbleBlack, ScribbleWhite, ScribbleWhite),
    ColorInfo(ScribbleRed, ScribbleRed, ScribbleBlack),
    ColorInfo(ScribbleGreen, ScribbleGreen, ScribbleBlack),
    ColorInfo(ScribbleYellow, ScribbleYellow, ScribbleBlack),
    ColorInfo(ScribbleBlue, ScribbleBlue, ScribbleBlack),
    ColorInfo(ScribbleMagenta, ScribbleMagenta, ScribbleBlack),
    ColorInfo(ScribbleCyan, ScribbleCyan, ScribbleBlack),
    ColorInfo(ScribbleWhite, ScribbleWhite, ScribbleBlack),
    ColorInfo(ScribbleBlack, ScribbleWhite, ScribbleWhite),
    ColorInfo(ScribbleBlack, ScribbleRed, ScribbleRed),
    ColorInfo(ScribbleBlack, ScribbleGreen, ScribbleGreen),
    ColorInfo(ScribbleBlack, ScribbleYellow, ScribbleYellow),
    ColorInfo(ScribbleBlack, ScribbleBlue, ScribbleBlue),
    ColorInfo(ScribbleBlack, ScribbleMagenta, ScribbleMagenta),
    ColorInfo(ScribbleBlack, ScribbleCyan, ScribbleCyan),
    ColorInfo(ScribbleBlack, ScribbleWhite, ScribbleWhite)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateData(Array(XR18OSCAPI.ROUTING_COUNT) { UltranetChannelInfo("-", 0) })

        lifecycleScope.launch {
            val info = withContext(Dispatchers.IO) {
                getUltranetInfo()
            }
            info?.let { updateData(it) }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    private fun updateData(data: Array<UltranetChannelInfo>) {
        setContent {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(ScribbleBlack)
                ) {
                    repeat(4) { row ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(
                                    top = if (row == 0) outerPadding else 0.dp,
                                    start = outerPadding,
                                    end = outerPadding,
                                    bottom = outerPadding
                                )
                                .fillMaxWidth()
                                .background(ScribbleBackground)
                        ) {
                            repeat(4) { column ->
                                renderCell(row, column, data)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RowScope.renderCell(row: Int, column: Int, data: Array<UltranetChannelInfo>) {
        val index = row * 4 + column
        val channelData = data[index]
        val color = colors[channelData.color]

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    top = outerPadding,
                    start = if (column == 0) outerPadding else 0.dp,
                    end = outerPadding,
                    bottom = outerPadding
                )
                .background(ScribbleBackground)
                .fillMaxHeight()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        bottom = outerPadding
                    )
                    .background(color.background)
                    .border(width = borderWidth, color = color.border)
                    .fillMaxHeight()
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = channelData.name,
                    color = color.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(
                text = "${index + 1}",
                color = color.text,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}