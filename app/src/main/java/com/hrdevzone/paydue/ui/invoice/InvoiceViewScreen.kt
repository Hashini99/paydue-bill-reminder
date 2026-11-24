package com.hrdevzone.paydue.ui.invoice

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hrdevzone.paydue.data.local.InvoiceRecord
import com.hrdevzone.paydue.ui.LocalAppContainer
import com.hrdevzone.paydue.util.invoicePdfFile
import com.hrdevzone.paydue.util.shareInvoicePdf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceViewScreen(
    invoiceId: Long,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val container = LocalAppContainer.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var invoice by remember { mutableStateOf<InvoiceRecord?>(null) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(invoiceId) {
        invoice = container.invoiceRepository.getById(invoiceId)
    }

    LaunchedEffect(invoice) {
        val current = invoice ?: return@LaunchedEffect
        val file = invoicePdfFile(context, current)
        if (!file.exists()) return@LaunchedEffect
        runCatching {
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                PdfRenderer(pfd).use { renderer ->
                    val page = renderer.openPage(0)
                    val bitmap = Bitmap.createBitmap(
                        page.width * 2,
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    pageBitmap = bitmap
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice?.invoiceNumber ?: "Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        invoice?.let { shareInvoicePdf(context, it) }
                    }) {
                        Icon(Icons.Filled.IosShare, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(595f / 842f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bitmap = pageBitmap
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Invoice preview",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    CircularProgressIndicator()
                }
            }

            Button(
                onClick = { invoice?.let { shareInvoicePdf(context, it) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.IosShare, contentDescription = null)
                Text(" Share invoice", modifier = Modifier.padding(start = 8.dp))
            }

            OutlinedButton(
                onClick = {
                    val current = invoice
                    if (current != null) {
                        scope.launch {
                            container.invoiceRepository.delete(current)
                            onDeleted()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text(" Delete invoice", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
