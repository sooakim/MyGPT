package kr.ac.kw.mygpt

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kr.ac.kw.mygpt.network.NetworkService
import kr.ac.kw.mygpt.network.asEvents
import kr.ac.kw.mygpt.network.request.PostChatCompletionsRequest
import kr.ac.kw.mygpt.network.response.PostChatCompletionsResponse
import kr.ac.kw.mygpt.ui.theme.MyGPTTheme
import kr.ac.kw.mygpt.ui.theme.Purple40
import kr.ac.kw.mygpt.ui.theme.Purple80

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyGPTTheme {
                // A surface container using the 'background' color from the theme
                MainView()
            }
        }
    }
}


@Composable
private fun MainView() {
    Box {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        var messages by remember { mutableStateOf(emptyList<String>()) }
        var inputMessage by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(false) }

        MainContentView(
            isLoading = isLoading,
            messages = messages,
            inputMessage = inputMessage,
            inputMessageChanged = { inputMessage = it },
            onSendClick = {
                coroutineScope.launch {
                    try {
                        val nextIndex = messages.count()
                        getMessages(question = inputMessage)
                            .onStart { isLoading = true }
                            .onCompletion {
                                isLoading = false
                                inputMessage = ""
                            }
                            .collect {
                                val mutableMessages = messages.toMutableList()
                                if (messages.lastIndex != nextIndex) {
                                    mutableMessages.add("")
                                }

                                var lastMessage = mutableMessages.lastOrNull() ?: ""
                                lastMessage += it.choices.mapNotNull { it.delta?.content }
                                    .joinToString(separator = "")
                                mutableMessages[nextIndex] = lastMessage
                                messages = mutableMessages
                            }
                    } catch (e: Exception) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainContentView(
    isLoading: Boolean,
    messages: List<String>,
    inputMessage: String,
    inputMessageChanged: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Column(modifier = Modifier.background(Color.White)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) {
                Box(
                    modifier = Modifier
                        .background(Purple80)
                        .fillMaxWidth()
                        .padding(all = 8.dp)
                ) {
                    Text("MyGPT: $it", color = Purple40)
                }
            }
        }
        Row(
            modifier = Modifier.background(color = Purple80),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Purple40,
                    containerColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                value = inputMessage,
                onValueChange = {
                    inputMessageChanged(it)
                }, placeholder = {
                    Text("내용을 입력하세요")
                })
            Button(enabled = !isLoading, onClick = onSendClick) {
                Text("Send")
            }
        }
    }
}

@Composable
@Preview
private fun MainContentView_Preview() {
    MainContentView(
        isLoading = false,
        messages = listOf(
            "답변",
            "답변",
            "답변"
        ),
        inputMessage = "질문",
        inputMessageChanged = {},
        onSendClick = {}
    )
}

private fun getMessages(question: String): Flow<PostChatCompletionsResponse> {
    val requestBody = PostChatCompletionsRequest(
        "gpt-3.5-turbo",
        true,
        listOf(
            PostChatCompletionsRequest.Message(
                "system",
                "You are a helpful assistant."
            ),
            PostChatCompletionsRequest.Message(
                "user",
                question
            )
        )
    )
    val gson = Gson()
    return NetworkService.gptApi.postChatCompletions(requestBody).asEvents()
        .mapNotNull {
            it.data ?: return@mapNotNull null
            if (it.data == "[DONE]") {
                return@mapNotNull null
            }
            gson.fromJson(it.data, PostChatCompletionsResponse::class.java)
        }
}