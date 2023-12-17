package kr.ac.kw.mygpt.network.request

import com.google.gson.annotations.SerializedName

data class PostChatCompletionsRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("stream")
    val stream: Boolean,
    @SerializedName("messages")
    val messages: List<Message>
) {
    data class Message(
        @SerializedName("role")
        val role: String,
        @SerializedName("content")
        val content: String
    )
}