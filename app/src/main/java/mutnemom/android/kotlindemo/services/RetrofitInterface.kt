package mutnemom.android.kotlindemo.services

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming

interface RetrofitInterface {

    @GET("storage/fe6011afaa624cd00a160ee/2017/10/file-example_PDF_1MB.pdf")
    @Streaming
    fun downloadFile(): Call<ResponseBody>

}
