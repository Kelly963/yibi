package com.yibi.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 图标名称到Compose图标的映射
 */
object IconMapper {

    fun getIcon(name: String): ImageVector {
        return when (name) {
            "restaurant" -> Icons.Default.Restaurant
            "directions_car" -> Icons.Default.DirectionsCar
            "shopping_cart" -> Icons.Default.ShoppingCart
            "sports_esports" -> Icons.Default.SportsEsports
            "home" -> Icons.Default.Home
            "local_hospital" -> Icons.Default.LocalHospital
            "school" -> Icons.Default.School
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "card_giftcard" -> Icons.Default.CardGiftcard
            "trending_up" -> Icons.Default.TrendingUp
            "work" -> Icons.Default.Work
            // 新增图标
            "phone_android" -> Icons.Default.PhoneAndroid
            "checkroom" -> Icons.Default.Checkroom
            "face" -> Icons.Default.Face
            "pets" -> Icons.Default.Pets
            "flight" -> Icons.Default.Flight
            "fitness_center" -> Icons.Default.FitnessCenter
            "local_cafe" -> Icons.Default.LocalCafe
            "volunteer_activism" -> Icons.Default.VolunteerActivism
            "redeem" -> Icons.Default.Redeem
            else -> Icons.Default.MoreHoriz
        }
    }
}
