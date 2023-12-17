package kr.ac.kw.mygpt.network.response

import com.google.gson.annotations.SerializedName

data class PostChatCompletionsResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("obj")
    val obj: String,
    @SerializedName("created")
    val created: Int,
    @SerializedName("model")
    val model: String,
    @SerializedName("system_fingerprint")
    val systemFingerprint: String?,
    @SerializedName("choices")
    val choices: List<Choice>
) {
    data class Choice(
        @SerializedName("index")
        val index: Int,
        @SerializedName("delta")
        val delta: Delta?,
        @SerializedName("logprobs")
        val logProbs: String?,
        @SerializedName("finish_reason")
        val finishReason: String?
    ) {
        data class Delta(
            @SerializedName("role")
            val role: String?,
            @SerializedName("content")
            val content: String
        )
    }
}