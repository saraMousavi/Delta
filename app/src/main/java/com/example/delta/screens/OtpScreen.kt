package com.example.delta.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.delta.volley.OTPApi
import com.example.delta.R
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDirection


@Composable
fun OtpScreen(
    phone: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var code by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var verifying by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    fun send() {
        sending = true
        OTPApi().sendOtp(
            context = ctx,
            phone = phone,
            onSuccess = {
                sending = false
                secondsLeft = 60
                Toast.makeText(ctx, ctx.getString(R.string.code_sent), Toast.LENGTH_SHORT).show()
            },
            onError = {
                sending = false
                Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    LaunchedEffect(Unit) { send() }

    LaunchedEffect(secondsLeft) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(112.dp))
        Text("کد تایید برای $phone ارسال شد", style = MaterialTheme.typography.bodyLarge)

        Image(
            painter = painterResource(R.drawable.delta_logo),
            contentDescription = null,
            modifier = Modifier.size(250.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))
        OtpFields(
            value = code,
            length = 6,
            onValueChange = { code = it },
            onComplete = { code = it },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                verifying = true
                OTPApi().verifyOtp(
                    context = ctx,
                    phone = phone,
                    code = code,
                    onSuccess = {
                        verifying = false
                        Toast.makeText(ctx, ctx.getString(R.string.confirmed), Toast.LENGTH_SHORT).show()
                        onVerified()
                    },
                    onError = {
                        verifying = false
                        Toast.makeText(ctx, it, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = code.length == 6 && !verifying,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (verifying) CircularProgressIndicator(strokeWidth = 2.dp) else Text(ctx.getString(R.string.confirm), style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { if (secondsLeft == 0 && !sending) send() },
            enabled = secondsLeft == 0 && !sending,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text(if (secondsLeft == 0) "ارسال مجدد کد" else "ارسال مجدد تا $secondsLeft ثانیه", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text(ctx.getString(R.string.returned), style = MaterialTheme.typography.bodyLarge) }
    }
}

@Composable
fun OtpFields(
    value: String,
    onValueChange: (String) -> Unit,
    onComplete: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier,
) {
    val focusRequesters = remember { List(length) { FocusRequester() } }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var cells by remember { mutableStateOf(List(length) { "" }) }

    LaunchedEffect(value) {
        val digits = value.filter { it.isDigit() }.take(length)
        val list = MutableList(length) { "" }
        digits.forEachIndexed { index, c -> list[index] = c.toString() }
        cells = list
    }

    fun currentCode(): String = cells.joinToString("")

    fun pasteAt(index: Int, text: String) {
        val digits = text.filter { it.isDigit() }
        if (digits.isEmpty()) return
        val list = cells.toMutableList()
        var ptr = index
        for (c in digits) {
            if (ptr >= length) break
            list[ptr] = c.toString()
            ptr++
        }
        cells = list
        val newCode = currentCode()
        onValueChange(newCode)
        if (newCode.length == length) {
            onComplete(newCode)
            focusManager.clearFocus()
        } else if (ptr <= length - 1) {
            focusRequesters[ptr].requestFocus()
        }
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0 until length) {
            OutlinedTextField(
                value = cells[i],
                onValueChange = { new ->
                    if (new.length > 1) {
                        pasteAt(i, new)
                        return@OutlinedTextField
                    }
                    val digit = new.take(1).filter { it.isDigit() }
                    val list = cells.toMutableList()
                    list[i] = digit
                    cells = list
                    val codeNow = currentCode()
                    onValueChange(codeNow)
                    if (digit.isNotEmpty()) {
                        if (i < length - 1) {
                            focusManager.moveFocus(FocusDirection.Next)
                        } else if (codeNow.length == length) {
                            onComplete(codeNow)
                            focusManager.clearFocus()
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center,textDirection = TextDirection.Ltr),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (i == length - 1) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    onDone = {
                        val codeNow = currentCode()
                        if (codeNow.length == length) onComplete(codeNow)
                    }
                ),
                modifier = Modifier
                    .width(52.dp)
                    .height(64.dp)
                    .focusRequester(focusRequesters[i])
                    .onPreviewKeyEvent { event ->
                        if (
                            event.type == KeyEventType.KeyDown &&
                            event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DEL &&
                            cells[i].isEmpty()
                        ) {
                            if (i > 0) focusManager.moveFocus(FocusDirection.Previous)
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text("", textAlign = TextAlign.Center) }
            )
        }
    }
}
}
