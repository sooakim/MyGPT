package kr.ac.kw.mygpt.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kr.ac.kw.mygpt.network.request.PostChatCompletionsRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface GPTApi {
    @POST("/v1/chat/completions")
    @Streaming
    fun postChatCompletions(
        @Body requestBody: PostChatCompletionsRequest
    ): Call<ResponseBody>
}

data class Event(
    val name: String? = null,
    val data: String? = null
)

fun Call<ResponseBody>.asEvents() = flow {
    val response = await()
    if (!response.isSuccessful) return@flow
    val input = response.body()?.byteStream()?.bufferedReader() ?: return@flow
    var event = Event()
    while (currentCoroutineContext().isActive) {
        val line = withContext(Dispatchers.IO) {
            input.readLine()
        } ?: break

        when {
            line.startsWith("event:") -> {
                event = event.copy(name = line.substring(6).trim())
            }

            line.startsWith("data:") -> {
                event = event.copy(data = line.substring(5).trim())
            }

            line.isEmpty() -> {
                emit(event)
                Event()
            }
        }
    }
}

suspend fun <T> Call<T>.await() = suspendCancellableCoroutine {
    it.invokeOnCancellation { cancel() }
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            it.resume(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            it.resumeWithException(t)
        }
    })
}