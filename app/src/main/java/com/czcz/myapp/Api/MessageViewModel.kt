package com.czcz.myapp.Api

import android.content.Context
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.Api.PostViewModel.*
import com.czcz.myapp.Room.Conversation
import com.czcz.myapp.Room.PostDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageViewModel : ViewModel() {
    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage
    fun resetErrorMessage() {
        _errorMessage.value = ""
    }
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading
    private val _currentPartner = MutableStateFlow(User.empty())
    val currentPartner = _currentPartner
    fun setCurrentPartner(partner: User) {
        _currentPartner.value = partner
    }
    private val _messageList = MutableStateFlow<List<Message>>(emptyList())
    val messageList = _messageList
    fun setMessageList(messages: List<Message>) {
        _messageList.value = messages
    }
    private val _isPutting = MutableStateFlow(false)
    val isPutting = _isPutting
    fun setIsPutting(value: Boolean) {
        _isPutting.value = value
    }

    private val _contentInput = MutableStateFlow("")
    val contentInput = _contentInput
    fun setContentInput(value: String) {
        _contentInput.value = value
    }
    fun loadMessages(context: Context){
        CoroutineScope(Dispatchers.IO).launch {
            val dao = PostDatabase.getDatabase(context).postDao()
            _messageList.value = dao.getMessagesByConservationId(currentConversationId.value)
            Log.d("MessageViewModel", "loadMessages: ${_messageList.value}")
        }
    }
    fun sendMessage(context: Context){
        if(_contentInput.value.isEmpty()) {
            _errorMessage.value = "不可发送空消息"
            return
        }
        CoroutineScope(Dispatchers.IO).launch{
            _isLoading.value = true
            val dao = PostDatabase.getDatabase(context).postDao()
            val user = DataStorePreference.getUser(context).first()
            val currentConversations = dao.getAllConversations()
            val token = DataStorePreference.getToken(context).first()
            Log.d("MessageViewModel", "sendMessage: $token")
            val call = ApiClient.apiService.sendMessage(
                MessageRequest(
                    receiverId = _currentPartner.value.id,
                    text = _contentInput.value
                ), token = "Bearer $token"
            )
            call.enqueue(object : Callback<ApiResponse<Message>> {
                override fun onResponse(
                    call: Call<ApiResponse<Message>>,
                    response: Response<ApiResponse<Message>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            if (body.data != null) {
                                _messageList.value = _messageList.value + body.data
                                CoroutineScope(Dispatchers.IO).launch {
                                    dao.insertMessage(body.data)
                                    if(!currentConversations.any { it.id == body.data.conversationId }){
                                        dao.insertConversation(
                                            Conversation(
                                                id = body.data.conversationId,
                                                partner = _currentPartner.value,
                                                lastMessage = body.data.text,
                                                lastMessageTime = body.data.time,
                                                unreadCount = 0,
                                                ownerId = user.id
                                            )
                                        )
                                    }
                                    else{
                                        dao.updateConversation(Conversation(
                                            id = body.data.conversationId,
                                            partner = _currentPartner.value,
                                            lastMessage = body.data.text,
                                            lastMessageTime = body.data.time,
                                            unreadCount = 0,
                                            ownerId = user.id
                                        ))
                                    }
                                }
                                _contentInput.value = ""
                                _errorMessage.value = "发送成功"
                            }
                        }
                    } else {
                        _contentInput.value = ""
                        _errorMessage.value =
                            JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                    _isLoading.value = false
                }

                override fun onFailure(call: Call<ApiResponse<Message>>, t: Throwable) {
                    _errorMessage.value = "网络错误：${t.message}"
                    _isLoading.value = false
                }
            })
        }
    }
    private val _currentConversationId = MutableStateFlow("")
    val currentConversationId = _currentConversationId
    fun getConversation(context: Context){
        CoroutineScope(Dispatchers.IO).launch{
            val user = DataStorePreference.getUser(context).first()
            val call = ApiClient.apiService.getConversationId(
                ConversationIdRequest(
                    user.id,
                    _currentPartner.value.id
                )
            )
            call.enqueue(object : Callback<ApiResponse<ConversationId>> {
                override fun onResponse(
                    call: Call<ApiResponse<ConversationId>>,
                    response: Response<ApiResponse<ConversationId>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null && body.data != null) {
                            _currentConversationId.value = body.data.conversationId
                        }
                    }
                    else{
                        _errorMessage.value = JSONObject(response.errorBody()?.string() ?: "").optString("message")
                    }
                }
                override fun onFailure(call: Call<ApiResponse<ConversationId>>, t: Throwable) {
                    _errorMessage.value = "网络错误：${t.message}"
                    Log.d("MessageViewModel", "getConversation: ${t.message}")
                }
            })
        }
    }
    fun reset(){
        _currentPartner.value = User.empty()
        _currentConversationId.value = ""
        _messageList.value = emptyList()
        _contentInput.value = ""
        stopConversationPolling()
    }
    fun resetConversationUnreadCount(conversation:Conversation,context: Context){

        CoroutineScope(Dispatchers.IO).launch{
            val dao = PostDatabase.getDatabase(context).postDao()
            dao.updateConversation(Conversation(
                id = conversation.id,
                partner = conversation.partner,
                lastMessage = conversation.lastMessage,
                lastMessageTime = conversation.lastMessageTime,
                ownerId = conversation.ownerId,
                unreadCount = 0
            ))
        }
    }
    fun getUnreadMessages(context: Context){
        CoroutineScope(Dispatchers.IO).launch{
            val token = DataStorePreference.getToken(context).first()
            val dao = PostDatabase.getDatabase(context).postDao()
            val user = DataStorePreference.getUser(context).first()
            val conversations = dao.getConversationByOwnerId(user.id)
            val call = ApiClient.apiService.getUnreadMessages(token = "Bearer $token")
            call.enqueue(object : Callback<ApiResponse<List<Message>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<Message>>>,
                    response: Response<ApiResponse<List<Message>>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            if (body.data != null) {
                                CoroutineScope(Dispatchers.IO).launch{
                                    dao.insertMessages(body.data)
                                        body.data.groupBy { it.conversationId }
                                            .forEach { (conversationId, messages) ->
                                                val latest = messages.last()   // 服务端按时间正序返回，最后一条最新
                                                val existing = conversations?.find { it.id == conversationId }
                                                val addCount = messages.size
                                                if(existing != null){
                                                    dao.updateConversation(
                                                        Conversation(
                                                            ownerId = user.id,
                                                            id = conversationId,
                                                            partner = latest.sender,
                                                            lastMessage = latest.text,
                                                            lastMessageTime = latest.time,
                                                            unreadCount = (existing.unreadCount
                                                                ) + addCount
                                                        )
                                                    )
                                                }else{
                                                    dao.insertConversation(
                                                        Conversation(
                                                            id = conversationId,
                                                            partner = latest.sender,
                                                            lastMessage = latest.text,
                                                            lastMessageTime = latest.time,
                                                            unreadCount = addCount,
                                                            ownerId = user.id
                                                        )
                                                    )
                                                }
                                            }

                                }
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<ApiResponse<List<Message>>>, t: Throwable) {
                    _errorMessage.value = "网络错误：${t.message}"
                    Log.d("MessageViewModel", "getUnreadMessages: ${t.message}")
                }
            })
        }
    }
    // 当前会话的定时刷新任务；进入聊天页启动，离开页面取消
    private var conversationPollingJob: Job? = null


    fun startConversationPolling(context: Context) {
        stopConversationPolling()
        conversationPollingJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                refreshCurrentConversation(context)
                delay(3000)
            }
        }
    }

    fun stopConversationPolling() {
        conversationPollingJob?.cancel()
        conversationPollingJob = null
    }


    private suspend fun refreshCurrentConversation(context: Context) {
        val partnerId = _currentPartner.value.id
        if (partnerId.isBlank()) return

        val dao = PostDatabase.getDatabase(context).postDao()
        val token = DataStorePreference.getToken(context).first()

        try {
            val response = ApiClient.apiService
                .getConversationMessages(partnerId, "Bearer $token")
                .execute()

            val body = response.body()
            if (response.isSuccessful && body != null && body.code == 200 && body.data != null) {
                dao.insertMessages(body.data)
                _messageList.value = dao.getMessagesByConservationId(currentConversationId.value)
            }
        } catch (e: Exception) {
            // 轮询失败不打断循环，只记日志，下一次继续
            Log.d("MessageViewModel", "refreshCurrentConversation: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopConversationPolling()
    }
}