package id.infokini.news

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

/**
 * Original, locally-rendered concept portrait for InfoKini.
 * This is an illustration preview, not a photorealistic avatar or lip-synced presenter.
 */
@Composable
fun InfoKiniPresenterPortrait(style: String) {
    val female = style == "Presenter Wanita"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101B3D))
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(188.dp)) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            // Studio backdrop
            drawCircle(Color(0xFF172B55), radius = h * 0.52f, center = Offset(cx, h * 0.43f))
            drawRoundRect(Color(0xFF0B1230), topLeft = Offset(w * 0.08f, h * 0.77f), size = Size(w * 0.84f, h * 0.28f))
            // Shoulders and formal jacket
            val jacket = Path().apply {
                moveTo(w * 0.25f, h)
                lineTo(w * 0.31f, h * 0.69f)
                quadraticTo(cx, h * 0.57f, w * 0.69f, h * 0.69f)
                lineTo(w * 0.76f, h)
                close()
            }
            drawPath(jacket, Color(0xFF080D25))
            // Shirt and collar
            val shirt = Path().apply {
                moveTo(w * 0.43f, h * 0.68f)
                lineTo(cx, h * 0.88f)
                lineTo(w * 0.57f, h * 0.68f)
                close()
            }
            drawPath(shirt, Color(0xFFF4F5F8))
            // Neck
            drawRoundRect(Color(0xFFB97855), Offset(w * 0.455f, h * 0.58f), Size(w * 0.09f, h * 0.17f))
            // Hair behind face
            drawOval(Color(0xFF211813), Offset(w * 0.365f, h * 0.105f), Size(w * 0.27f, h * 0.52f))
            // Face
            drawOval(Color(0xFFC98763), Offset(w * 0.39f, h * 0.16f), Size(w * 0.22f, h * 0.43f))
            // Hairline / hairstyle
            if (female) {
                drawOval(Color(0xFF211813), Offset(w * 0.35f, h * 0.10f), Size(w * 0.30f, h * 0.30f))
                drawOval(Color(0xFFC98763), Offset(w * 0.395f, h * 0.20f), Size(w * 0.21f, h * 0.39f))
                drawOval(Color(0xFF211813), Offset(w * 0.345f, h * 0.23f), Size(w * 0.055f, h * 0.39f))
                drawOval(Color(0xFF211813), Offset(w * 0.60f, h * 0.23f), Size(w * 0.055f, h * 0.39f))
            } else {
                drawOval(Color(0xFF211813), Offset(w * 0.39f, h * 0.115f), Size(w * 0.22f, h * 0.18f))
            }
            // Eyes, nose and smile
            drawLine(Color(0xFF34221B), Offset(w * 0.435f, h * 0.365f), Offset(w * 0.465f, h * 0.365f), strokeWidth = 2.4f)
            drawLine(Color(0xFF34221B), Offset(w * 0.535f, h * 0.365f), Offset(w * 0.565f, h * 0.365f), strokeWidth = 2.4f)
            drawLine(Color(0xFF9C6048), Offset(cx, h * 0.37f), Offset(cx - w * 0.008f, h * 0.435f), strokeWidth = 2f)
            drawLine(Color(0xFF7B352F), Offset(w * 0.465f, h * 0.49f), Offset(w * 0.535f, h * 0.49f), strokeWidth = 2f)
            // Tie and lapels
            val tie = Path().apply {
                moveTo(w * 0.48f, h * 0.70f)
                lineTo(w * 0.52f, h * 0.70f)
                lineTo(w * 0.535f, h * 0.93f)
                lineTo(cx, h * 0.98f)
                lineTo(w * 0.465f, h * 0.93f)
                close()
            }
            drawPath(tie, Color(0xFF13BDEB))
            drawLine(Color(0xFF13BDEB), Offset(w * 0.33f, h * 0.72f), Offset(w * 0.43f, h * 0.85f), strokeWidth = 3f)
            drawLine(Color(0xFF13BDEB), Offset(w * 0.67f, h * 0.72f), Offset(w * 0.57f, h * 0.85f), strokeWidth = 3f)
            drawCircle(Color(0xFF13BDEB), radius = 3.5f, center = Offset(w * 0.78f, h * 0.16f))
        }
    }
}
