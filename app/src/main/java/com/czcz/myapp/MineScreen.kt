package com.czcz.myapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MineScreen() {
    val user = remember {
        Model.User(
            account = 10001,
            name = "创作者小C",
            email = "xiaoc@example.com",
            password = "",
            profilePicture = 0,
//            birthday = "2000-06-15",
//            qq = "123456789",
//            phone = "138****8888",
//            joinTime = "2024-03-20",
//            level = 5,
            postCount = 128,
            likeCount = 9600,
//            followCount = 256,
//            followerCount = 1024,
            introduction = "分享生活的美好，记录每一个精彩瞬间 ✨",
            posts = mutableListOf(),
            likes = mutableListOf(),
        )
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("我的帖子", "点赞帖子")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            ProfileHeader(user = user)
        }
        item {
            StatsRow(
                postCount = user.postCount,
//                followCount = user.followCount,
//                followerCount = user.followerCount,
                likeCount = user.likeCount,
            )
        }
        item {
            UserInfoSection(user = user)
        }
        item {
            Spacer(Modifier.height(8.dp))
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                    )
                }
            }
        }
        item {
            PostListPlaceholder(
                tabTitle = tabTitles[selectedTabIndex],
                count = if (selectedTabIndex == 0) user.postCount else user.likes.size,
            )
        }
    }
}

@Composable
private fun ProfileHeader(user: Model.User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (user.profilePicture != 0) {
                    // TODO: 使用 Coil/Glide 加载头像
                    // Image(painter = painterResource(user.profilePicture), ...)
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                //LevelBadge(level = user.level)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = user.introduction,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { /* 编辑资料 */ }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑资料")
            }
            IconButton(onClick = { /* 设置 */ }) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        }
    }
}

@Composable
private fun LevelBadge(level: Int) {
    val levelColors = listOf(
        Color(0xFF9E9E9E), Color(0xFF4CAF50), Color(0xFF2196F3),
        Color(0xFFFF9800), Color(0xFFE91E63), Color(0xFF9C27B0),
    )
    val color = levelColors.getOrElse(level - 1) { levelColors.last() }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = "Lv.$level",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StatsRow(
    postCount: Int,
//    followCount: Int,
//    followerCount: Int,
    likeCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(label = "作品", count = postCount)
//        StatItem(label = "关注", count = followCount)
//        StatItem(label = "粉丝", count = followerCount)
        StatItem(label = "获赞", count = formatCount(likeCount))
    }
}

@Composable
private fun StatItem(label: String, count: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { },
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StatItem(label: String, count: Int) {
    StatItem(label = label, count = count.toString())
}

@Composable
private fun UserInfoSection(user: Model.User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
//        InfoRow(icon = Icons.Default.Person, label = "加入时间", value = user.joinTime)
//        if (user.birthday.isNotBlank()) {
//            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
//            InfoRow(icon = Icons.Default.Favorite, label = "生日", value = user.birthday)
//        }
//        if (user.qq.isNotBlank()) {
//            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
//            InfoRow(icon = Icons.Default.Person, label = "QQ", value = user.qq)
//        }
//        if (user.phone.isNotBlank()) {
//            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
//            InfoRow(icon = Icons.Default.Person, label = "手机", value = user.phone)
//        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PostListPlaceholder(tabTitle: String, count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 0) "$tabTitle 列表（共 $count 条）" else "暂无$tabTitle",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 10000 -> String.format("%.1fw", count / 10000.0)
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}
