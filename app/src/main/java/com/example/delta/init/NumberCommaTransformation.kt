package com.example.delta.init

import android.content.Context
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.delta.R
import java.text.NumberFormat
import java.util.Locale

class NumberCommaTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formatted = originalText
            .filter { it.isDigit() }
            .toLongOrNull()
            ?.let { NumberFormat.getNumberInstance(Locale.US).format(it) }
            ?: ""

        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return formatted.take(offset + formatted.count { it == ',' })
                        .length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return formatted.take(offset).filter { it.isDigit() }.length
                }
            }
        )
    }

    // Utility function to convert numbers to English words
    fun numberToWords(context: Context, number: Long): String {
        if (number == 0L) return context.getString(R.string.zero_persian)

        val units = arrayOf(
            "",
            context.getString(R.string.one_persian),
            context.getString(R.string.two_persian),
            context.getString(R.string.three_persian),
            context.getString(R.string.four_persian),
            context.getString(R.string.five_persian),
            context.getString(R.string.six_persian),
            context.getString(R.string.seven_persian),
            context.getString(R.string.eight_persian),
            context.getString(R.string.nine_persian),
            context.getString(R.string.ten_persian),
            context.getString(R.string.eleven_persian),
            context.getString(R.string.twelve_persian),
            context.getString(R.string.thirteen_persian),
            context.getString(R.string.fourteen_persian),
            context.getString(R.string.fifteen_persian),
            context.getString(R.string.sixteen_persian),
            context.getString(R.string.seventeen_persian),
            context.getString(R.string.eighteen_persian),
            context.getString(R.string.nineteen_persian)
        )

        val tens = arrayOf(
            "",
            "",
            context.getString(R.string.twenty_persian),
            context.getString(R.string.thirty_persian),
            context.getString(R.string.forty_persian),
            context.getString(R.string.fifty_persian),
            context.getString(R.string.sixty_persian),
            context.getString(R.string.seventy_persian),
            context.getString(R.string.eighty_persian),
            context.getString(R.string.ninety_persian)
        )

        fun convert(n: Long): String {
            return when {
                n < 20 -> units[n.toInt()]

                n < 100 -> tens[(n / 10).toInt()] +
                        (if (n % 10 > 0) " و " + units[(n % 10).toInt()] else "")

                n < 1000 -> when (n / 100) {
                    1L -> context.getString(R.string.one_hundred_persian)
                    2L -> context.getString(R.string.two_hundred_persian)
                    3L -> context.getString(R.string.three_hundred_persian)
                    5L -> context.getString(R.string.five_hundred_persian)
                    else -> units[(n / 100).toInt()] + " " + context.getString(R.string.hundred_persian)
                } + (if (n % 100 > 0) " و " + convert(n % 100) else "")

                n < 1_000_000 -> convert(n / 1000) + " " +
                        context.getString(R.string.thousand_persian) +
                        (if (n % 1000 > 0) " و " + convert(n % 1000) else "")

                n < 1_000_000_000 -> convert(n / 1_000_000) + " " +
                        context.getString(R.string.million_persian) +
                        (if (n % 1_000_000 > 0) " و " + convert(n % 1_000_000) else "")

                else -> convert(n / 1_000_000_000) + " " +
                        context.getString(R.string.billion_persian) +
                        (if (n % 1_000_000_000 > 0) " و " + convert(n % 1_000_000_000) else "")
            }
        }

        return convert(number)
    }

}
