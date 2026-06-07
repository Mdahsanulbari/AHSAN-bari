package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CalculatorApp(modifier = Modifier.fillMaxSize())
      }
    }
  }
}

@Composable
fun CalculatorApp(modifier: Modifier = Modifier) {
  var isDarkTheme by remember { mutableStateOf(true) }

  // Theme colors
  val backgroundColor = if (isDarkTheme) Color(0xFF121418) else Color(0xFFF6F8FA)
  val displayCardColor = if (isDarkTheme) Color(0xFF1E222A) else Color(0xFFEDF0F4)
  val onBackgroundColor = if (isDarkTheme) Color(0xFFFFFFFF) else Color(0xFF121418)
  val secondaryTextColor = if (isDarkTheme) Color(0xFF8E9BA8) else Color(0xFF6B7A87)

  val numberBtnBg = if (isDarkTheme) Color(0xFF262A33) else Color(0xFFE2E6EC)
  val numberBtnFg = if (isDarkTheme) Color(0xFFECEFF4) else Color(0xFF121418)

  val utilityBtnBg = if (isDarkTheme) Color(0xFF383D48) else Color(0xFFD3D8E0)
  val utilityBtnFg = if (isDarkTheme) Color(0xFFECEFF4) else Color(0xFF242933)

  val actionBtnBg = Color(0xFFFF9F0A) // Vibrant orange
  val actionBtnFg = Color.White

  // Calculator states
  val currentInput = remember { mutableStateOf("") }
  val previousInput = remember { mutableStateOf("") }
  val pendingOperation = remember { mutableStateOf<String?>(null) }
  val resultText = remember { mutableStateOf("") }
  val wasLastActionEquals = remember { mutableStateOf(false) }

  // Evaluates standard logic based on current inputs and operator
  fun evaluate(num1Str: String, num2Str: String, op: String): String {
    val n1 = num1Str.toDoubleOrNull() ?: return "Error"
    val n2 = num2Str.toDoubleOrNull() ?: return "Error"
    val resValue = when (op) {
      "+" -> n1 + n2
      "-" -> n1 - n2
      "×" -> n1 * n2
      "÷" -> {
        if (n2 == 0.0) return "Error"
        n1 / n2
      }
      else -> return "Error"
    }
    return formatResult(resValue)
  }

  // Key operations router
  fun onKeyClick(key: String) {
    if (wasLastActionEquals.value) {
      wasLastActionEquals.value = false
      if (key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".")) {
        currentInput.value = ""
        previousInput.value = ""
        pendingOperation.value = null
        resultText.value = ""
      }
    }

    when (key) {
      "C" -> {
        currentInput.value = ""
        previousInput.value = ""
        pendingOperation.value = null
        resultText.value = ""
      }
      "⌫" -> {
        if (currentInput.value.isNotEmpty()) {
          currentInput.value = currentInput.value.dropLast(1)
        } else if (pendingOperation.value != null) {
          pendingOperation.value = null
          currentInput.value = previousInput.value
          previousInput.value = ""
        }
      }
      "%" -> {
        if (currentInput.value.isNotEmpty()) {
          val inputDouble = currentInput.value.toDoubleOrNull() ?: 0.0
          currentInput.value = formatResult(inputDouble / 100.0)
        } else if (resultText.value.isNotEmpty() && resultText.value != "Error") {
          val resultDouble = resultText.value.toDoubleOrNull() ?: 0.0
          resultText.value = formatResult(resultDouble / 100.0)
          currentInput.value = resultText.value
        }
      }
      "+", "-", "×", "÷" -> {
        val op = key
        if (currentInput.value.isNotEmpty()) {
          if (previousInput.value.isNotEmpty() && pendingOperation.value != null) {
            val res = evaluate(previousInput.value, currentInput.value, pendingOperation.value!!)
            if (res == "Error") {
              resultText.value = "Error"
              previousInput.value = ""
              currentInput.value = ""
              pendingOperation.value = null
            } else {
              previousInput.value = res
              currentInput.value = ""
              pendingOperation.value = op
              resultText.value = res
            }
          } else {
            previousInput.value = currentInput.value
            currentInput.value = ""
            pendingOperation.value = op
          }
        } else if (previousInput.value.isNotEmpty()) {
          pendingOperation.value = op
        } else if (resultText.value.isNotEmpty() && resultText.value != "Error") {
          previousInput.value = resultText.value
          pendingOperation.value = op
          currentInput.value = ""
        }
      }
      "." -> {
        if (currentInput.value.isEmpty()) {
          currentInput.value = "0."
        } else if (!currentInput.value.contains(".")) {
          currentInput.value += "."
        }
      }
      "=" -> {
        if (previousInput.value.isNotEmpty() && pendingOperation.value != null && currentInput.value.isNotEmpty()) {
          val res = evaluate(previousInput.value, currentInput.value, pendingOperation.value!!)
          resultText.value = res
          if (res != "Error") {
            currentInput.value = res
          } else {
            currentInput.value = ""
          }
          previousInput.value = ""
          pendingOperation.value = null
          wasLastActionEquals.value = true
        }
      }
      else -> {
        if (currentInput.value == "0") {
          currentInput.value = key
        } else {
          currentInput.value += key
        }
      }
    }
  }

  Column(
    modifier = modifier
      .background(backgroundColor)
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Elegant Top Header with Theme Switcher
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "🧮",
          fontSize = 24.sp,
          modifier = Modifier.padding(end = 6.dp)
        )
        Text(
          text = "CalcFlow",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = onBackgroundColor,
          letterSpacing = 0.5.sp
        )
      }

      IconButton(
        onClick = { isDarkTheme = !isDarkTheme },
        modifier = Modifier.minimumInteractiveComponentSize()
      ) {
        Text(
          text = if (isDarkTheme) "☀️" else "🌙",
          fontSize = 22.sp
        )
      }
    }

    // Display Card - Top large input, formula and results section
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1.5f)
        .padding(vertical = 12.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = displayCardColor)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
      ) {
        // Formula tracker at top of display
        val formula = if (previousInput.value.isNotEmpty() && pendingOperation.value != null) {
          "${previousInput.value} ${pendingOperation.value} ${currentInput.value}"
        } else ""

        Text(
          text = formula,
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
          color = secondaryTextColor,
          maxLines = 1,
          textAlign = TextAlign.End,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("display_formula")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Large output value display
        val mainDisplay = if (currentInput.value.isNotEmpty()) {
          currentInput.value
        } else if (resultText.value.isNotEmpty()) {
          resultText.value
        } else {
          "0"
        }

        Text(
          text = mainDisplay,
          fontSize = if (mainDisplay.length > 8) 32.sp else 46.sp,
          fontWeight = FontWeight.Bold,
          color = onBackgroundColor,
          maxLines = 1,
          textAlign = TextAlign.End,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("display_value")
        )
      }
    }

    // Calculator button layout grid
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .weight(4f),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Row 1: C, ⌫, %, ÷
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CalculatorButton("C", { onKeyClick("C") }, utilityBtnBg, utilityBtnFg, testTag = "key_c")
        CalculatorButton("⌫", { onKeyClick("⌫") }, utilityBtnBg, utilityBtnFg, testTag = "key_backspace")
        CalculatorButton("%", { onKeyClick("%") }, utilityBtnBg, utilityBtnFg, testTag = "key_percentage")
        CalculatorButton("÷", { onKeyClick("÷") }, actionBtnBg, actionBtnFg, testTag = "key_divide")
      }

      // Row 2: 7, 8, 9, ×
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CalculatorButton("7", { onKeyClick("7") }, numberBtnBg, numberBtnFg, testTag = "key_7")
        CalculatorButton("8", { onKeyClick("8") }, numberBtnBg, numberBtnFg, testTag = "key_8")
        CalculatorButton("9", { onKeyClick("9") }, numberBtnBg, numberBtnFg, testTag = "key_9")
        CalculatorButton("×", { onKeyClick("×") }, actionBtnBg, actionBtnFg, testTag = "key_multiply")
      }

      // Row 3: 4, 5, 6, -
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CalculatorButton("4", { onKeyClick("4") }, numberBtnBg, numberBtnFg, testTag = "key_4")
        CalculatorButton("5", { onKeyClick("5") }, numberBtnBg, numberBtnFg, testTag = "key_5")
        CalculatorButton("6", { onKeyClick("6") }, numberBtnBg, numberBtnFg, testTag = "key_6")
        CalculatorButton("-", { onKeyClick("-") }, actionBtnBg, actionBtnFg, testTag = "key_subtract")
      }

      // Row 4: 1, 2, 3, +
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CalculatorButton("1", { onKeyClick("1") }, numberBtnBg, numberBtnFg, testTag = "key_1")
        CalculatorButton("2", { onKeyClick("2") }, numberBtnBg, numberBtnFg, testTag = "key_2")
        CalculatorButton("3", { onKeyClick("3") }, numberBtnBg, numberBtnFg, testTag = "key_3")
        CalculatorButton("+", { onKeyClick("+") }, actionBtnBg, actionBtnFg, testTag = "key_add")
      }

      // Row 5: 0, ., =
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        CalculatorButton("0", { onKeyClick("0") }, numberBtnBg, numberBtnFg, weight = 2f, testTag = "key_0")
        CalculatorButton(".", { onKeyClick(".") }, numberBtnBg, numberBtnFg, weight = 1f, testTag = "key_decimal")
        CalculatorButton("=", { onKeyClick("=") }, actionBtnBg, actionBtnFg, weight = 1f, testTag = "key_equals")
      }
    }
  }
}

@Composable
fun RowScope.CalculatorButton(
  text: String,
  onClick: () -> Unit,
  containerColor: Color,
  contentColor: Color,
  weight: Float = 1f,
  testTag: String
) {
  Box(
    modifier = Modifier
      .weight(weight)
      .fillMaxSize()
  ) {
    Button(
      onClick = onClick,
      shape = RoundedCornerShape(24.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor
      ),
      contentPadding = PaddingValues(0.dp),
      modifier = Modifier
        .fillMaxSize()
        .minimumInteractiveComponentSize()
        .testTag(testTag)
    ) {
      Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

// Formats the Double result nicely to a maximum configuration of decimal places and drops trailing zeros
fun formatResult(value: Double): String {
  if (value.isInfinite() || value.isNaN()) return "Error"
  val valueLong = value.toLong()
  if (value == valueLong.toDouble()) {
    return valueLong.toString()
  }
  val formatted = String.format(java.util.Locale.US, "%.10f", value)
  var trimmed = formatted.trimEnd('0')
  if (trimmed.endsWith(".")) {
    trimmed = trimmed.substring(0, trimmed.length - 1)
  }
  return trimmed
}
