package ru.tretyackov.todo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import kotlinx.coroutines.launch
import ru.tretyackov.todo.R
import ru.tretyackov.todo.data.toLocalToken
import ru.tretyackov.todo.data.updateYandexAuthToken
import ru.tretyackov.todo.theme.AppTheme

@Composable
@Preview
private fun LoginComponentPreview() {
    LoginComponent {}
}

@Composable
private fun LoginComponent(startLogin: () -> Unit) {
    AppTheme {
        Surface {
            val patternBackgroundImage = ImageBitmap.imageResource(R.drawable.pattern)
            val patternBackgroundBrush = remember(patternBackgroundImage) {
                ShaderBrush(
                    ImageShader(
                        patternBackgroundImage,
                        TileMode.Repeated,
                        TileMode.Repeated
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(patternBackgroundBrush, alpha = 0.7f)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.application_login),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = { startLogin() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            disabledContainerColor = Color.Black,
                            disabledContentColor = Color.White,
                        )
                    ) {
                        Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                            Image(
                                painter = painterResource(id = R.drawable.icon_yandex_id),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(
                                modifier = Modifier
                                    .width(10.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.login_by_yandex_id),
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

class LoginFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sdk = YandexAuthSdk.create(YandexAuthOptions(requireContext()))
        val launcher =
            registerForActivityResult(sdk.contract) { result -> handleAuthResult(result) }
        return ComposeView(requireContext()).apply {
            setContent {
                LoginComponent {
                    val loginOptions = YandexAuthLoginOptions()
                    launcher.launch(loginOptions)
                }
            }
        }
    }

    private fun handleAuthResult(yandexAuthResult: YandexAuthResult) {
        val message = getMessageForYandexAuthResult(yandexAuthResult)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        if (yandexAuthResult !is YandexAuthResult.Success)
            return
        lifecycleScope.launch {
            val yandexAuthToken = yandexAuthResult.token
            requireContext().updateYandexAuthToken(yandexAuthToken.toLocalToken())
            parentFragmentManager.commit {
                setCustomAnimations(R.anim.slide_in, 0, 0, 0)
                replace(
                    R.id.fragment_container_view,
                    ToDoListFragment::class.java,
                    null
                )
            }
        }
    }

    private fun getMessageForYandexAuthResult(result: YandexAuthResult): String {
        return when (result) {
            is YandexAuthResult.Success -> getString(R.string.yandex_login_success)
            is YandexAuthResult.Failure -> getString(R.string.yandex_login_failure)
            is YandexAuthResult.Cancelled -> getString(R.string.yandex_login_cancelled)
        }
    }
}