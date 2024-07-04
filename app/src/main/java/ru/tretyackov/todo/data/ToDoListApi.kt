package ru.tretyackov.todo.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

const val token = "ТОКЕН"

interface ToDoListApi {
    @GET("list")
    suspend fun getToDoList() : ToDoListDto
    @POST("list")
    suspend fun add(@Header("X-Last-Known-Revision") revision: Int,
                    @Query("id") id: String,
                    @Body createToDoItemDto: CreateToDoItemDto) : OperatedToDoItemDto
    @PUT("list/{id}")
    suspend fun update(@Header("X-Last-Known-Revision") revision: Int, @Path("id") id: String, @Body updateToDoItemDto: UpdateToDoItemDto) : OperatedToDoItemDto
    @DELETE("list/{id}")
    suspend fun delete(@Header("X-Last-Known-Revision") revision: Int, @Path("id") id: String) : OperatedToDoItemDto
}

object ToDoListApiHelper {
    private const val BASE_URL = "https://hive.mrdekk.ru/todo/"
    private var api: ToDoListApi? = null
       fun getInstance(): ToDoListApi {
        if(api != null)
        {
            return api as ToDoListApi
        }
       val client = OkHttpClient().newBuilder().addInterceptor { chain ->
           val original = chain.request()
           val request = original.newBuilder()
               .header("Authorization", "Bearer $token")
               .build()
           chain.proceed(request) }.build()
        api = Retrofit.Builder().baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ToDoListApi::class.java)
        return api as ToDoListApi
    }
}