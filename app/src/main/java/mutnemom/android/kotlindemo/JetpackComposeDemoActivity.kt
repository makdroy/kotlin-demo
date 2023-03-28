package mutnemom.android.kotlindemo

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// class JetpackComposeDemoActivity : AppCompatActivity() {
class JetpackComposeDemoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Greeting("World")
            JetKiteButton(text = "JetKite") {
                Toast.makeText(
                    this,
                    "click JetKite button",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @Composable
    fun Greeting(name: String) {
        Text(text = "Hello $name!")
    }

    @Preview(name = "pre1")
    @Composable
    fun DefaultPreview() {
        Text("Hello World")
    }

    @Preview
    @Composable
    fun JetKiteButtonPreview() {
        MaterialTheme {
            Surface {
                JetKiteButton(text = "Test Button") {
                    Toast.makeText(
                        this,
                        "click JetKite button",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @Composable
    fun JetKiteButton(
        modifier: Modifier = Modifier,
        text: String,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onSecondary
            ),
            shape = RectangleShape,
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 4.dp
            ),
            enabled = enabled
        ) {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.button
            )
        }
    }

}
