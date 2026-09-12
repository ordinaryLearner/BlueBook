package com.czcz.myapp.Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.czcz.myapp.Api.MessageViewModel
import com.czcz.myapp.Api.Models.*
import com.czcz.myapp.DataStorePreference
import com.czcz.myapp.Room.Conversation
import com.czcz.myapp.Room.PostDatabase
import com.czcz.myapp.ui.Avatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController, messageViewModel: MessageViewModel) {
    val context = LocalContext.current
    val dao = PostDatabase.getDatabase(context).postDao()
    val user = DataStorePreference.getUser(context).collectAsState(User.empty())
    val conversations = remember { mutableStateListOf<Conversation>() }
    LaunchedEffect(user.value.id) {
        conversations.clear()
        messageViewModel.getUnreadMessages(context)
        conversations.addAll(dao.getConversationByOwnerId(user.value.id) ?: emptyList())
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("会话列表") }
            )
        }
    ) {PaddingValue ->
        if(conversations.isNotEmpty()){
            LazyColumn(modifier = Modifier.padding(PaddingValue)) {
                items(conversations.size) { index ->
                    ChatItem(conversations[index], navController, messageViewModel)
                }
            }
        }else{
            Box(modifier = Modifier.fillMaxSize().padding(PaddingValue),
            ){
                Text("暂无会话",modifier = Modifier.align(Alignment.Center))
            }
        }

    }
}
@Composable
fun ChatItem(conversation: Conversation,navController: NavController, messageViewModel: MessageViewModel){
    val context = LocalContext.current
    Row(modifier = Modifier
        .padding(4.dp)
        .clickable {
            messageViewModel.resetConversationUnreadCount(conversation, context)
            messageViewModel.setCurrentPartner(conversation.partner)
            messageViewModel.getConversation(context)
            navController.navigate("ChatScreen")
        }) {
        Avatar(
            conversation.partner.avatar,
            size = 40.dp
        )
        Column() {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Text(conversation.partner.username ?: "BB用户", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(conversation.lastMessageTime)
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                Text(conversation.lastMessage, fontSize = 14.sp)
                if(conversation.unreadCount > 0){
                Button(
                    modifier = Modifier.size(20.dp),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                        )
                    ) {
                        Text(conversation.unreadCount.toString())
                    }
                }
            }
            Divider(modifier = Modifier.padding(4.dp))
        }
    }
}