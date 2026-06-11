package com.example.budgettracker.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BeachAccess
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Chair
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.automirrored.outlined.DirectionsBike
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.LocalHospital
import androidx.compose.material.icons.outlined.LocalLaundryService
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.LocalPizza
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Train
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Work
import androidx.compose.ui.graphics.vector.ImageVector

/** A pickable category icon. [key] is the stable value stored in `Category.icon`. */
data class CategoryIcon(val key: String, val label: String, val vector: ImageVector)

/** A titled section in the icon picker. */
data class IconSection(val title: String, val icons: List<CategoryIcon>)

val CATEGORY_ICON_SECTIONS: List<IconSection> = listOf(
    IconSection(
        "Food & Drink",
        listOf(
            CategoryIcon("restaurant", "Restaurant dining out", Icons.Outlined.Restaurant),
            CategoryIcon("lunch_dining", "Lunch burger fast food", Icons.Outlined.LunchDining),
            CategoryIcon("fastfood", "Fast food snacks", Icons.Outlined.Fastfood),
            CategoryIcon("local_pizza", "Pizza", Icons.Outlined.LocalPizza),
            CategoryIcon("local_cafe", "Coffee cafe tea", Icons.Outlined.LocalCafe),
            CategoryIcon("local_bar", "Bar drinks alcohol", Icons.Outlined.LocalBar),
            CategoryIcon("cake", "Cake dessert sweets", Icons.Outlined.Cake),
        ),
    ),
    IconSection(
        "Home & Bills",
        listOf(
            CategoryIcon("home", "Home house rent", Icons.Outlined.Home),
            CategoryIcon("bolt", "Electricity power energy", Icons.Outlined.Bolt),
            CategoryIcon("water_drop", "Water utilities", Icons.Outlined.WaterDrop),
            CategoryIcon("wifi", "Internet wifi broadband", Icons.Outlined.Wifi),
            CategoryIcon("phone", "Phone mobile", Icons.Outlined.Phone),
            CategoryIcon("receipt_long", "Bills receipt", Icons.AutoMirrored.Outlined.ReceiptLong),
            CategoryIcon("cleaning_services", "Cleaning maintenance", Icons.Outlined.CleaningServices),
            CategoryIcon("local_laundry_service", "Laundry", Icons.Outlined.LocalLaundryService),
            CategoryIcon("chair", "Furniture home goods", Icons.Outlined.Chair),
        ),
    ),
    IconSection(
        "Transport",
        listOf(
            CategoryIcon("directions_car", "Car driving transport", Icons.Outlined.DirectionsCar),
            CategoryIcon("directions_bus", "Bus public transport", Icons.Outlined.DirectionsBus),
            CategoryIcon("train", "Train metro rail", Icons.Outlined.Train),
            CategoryIcon("directions_bike", "Bike cycling", Icons.AutoMirrored.Outlined.DirectionsBike),
            CategoryIcon("two_wheeler", "Scooter motorbike", Icons.Outlined.TwoWheeler),
            CategoryIcon("local_taxi", "Taxi cab ride", Icons.Outlined.LocalTaxi),
            CategoryIcon("local_gas_station", "Fuel petrol gas", Icons.Outlined.LocalGasStation),
            CategoryIcon("flight", "Flight travel plane", Icons.Outlined.Flight),
        ),
    ),
    IconSection(
        "Shopping",
        listOf(
            CategoryIcon("shopping_cart", "Groceries shopping cart", Icons.Outlined.ShoppingCart),
            CategoryIcon("shopping_bag", "Shopping bag retail", Icons.Outlined.ShoppingBag),
            CategoryIcon("local_mall", "Mall shopping", Icons.Outlined.LocalMall),
            CategoryIcon("storefront", "Store shop", Icons.Outlined.Storefront),
            CategoryIcon("checkroom", "Clothing apparel", Icons.Outlined.Checkroom),
            CategoryIcon("devices", "Electronics gadgets", Icons.Outlined.Devices),
            CategoryIcon("card_giftcard", "Gifts presents", Icons.Outlined.CardGiftcard),
        ),
    ),
    IconSection(
        "Money",
        listOf(
            CategoryIcon("payments", "Salary income payments", Icons.Outlined.Payments),
            CategoryIcon("attach_money", "Cash money", Icons.Outlined.AttachMoney),
            CategoryIcon("account_balance", "Bank account", Icons.Outlined.AccountBalance),
            CategoryIcon("savings", "Savings piggy bank", Icons.Outlined.Savings),
            CategoryIcon("credit_card", "Credit card debt", Icons.Outlined.CreditCard),
            CategoryIcon("trending_up", "Investment growth", Icons.AutoMirrored.Outlined.TrendingUp),
            CategoryIcon("currency_exchange", "Exchange transfer", Icons.Outlined.CurrencyExchange),
        ),
    ),
    IconSection(
        "Health",
        listOf(
            CategoryIcon("favorite", "Health wellbeing", Icons.Outlined.Favorite),
            CategoryIcon("local_hospital", "Hospital medical", Icons.Outlined.LocalHospital),
            CategoryIcon("medication", "Medicine pharmacy", Icons.Outlined.Medication),
            CategoryIcon("fitness_center", "Gym fitness workout", Icons.Outlined.FitnessCenter),
            CategoryIcon("spa", "Spa wellness", Icons.Outlined.Spa),
            CategoryIcon("self_improvement", "Meditation yoga", Icons.Outlined.SelfImprovement),
        ),
    ),
    IconSection(
        "Leisure",
        listOf(
            CategoryIcon("movie", "Movies cinema", Icons.Outlined.Movie),
            CategoryIcon("music_note", "Music streaming", Icons.Outlined.MusicNote),
            CategoryIcon("sports_esports", "Games gaming", Icons.Outlined.SportsEsports),
            CategoryIcon("sports_soccer", "Sports football", Icons.Outlined.SportsSoccer),
            CategoryIcon("travel_explore", "Travel explore", Icons.Outlined.TravelExplore),
            CategoryIcon("beach_access", "Vacation holiday beach", Icons.Outlined.BeachAccess),
            CategoryIcon("menu_book", "Books reading", Icons.AutoMirrored.Outlined.MenuBook),
        ),
    ),
    IconSection(
        "Work & Other",
        listOf(
            CategoryIcon("work", "Work job office", Icons.Outlined.Work),
            CategoryIcon("business_center", "Business briefcase", Icons.Outlined.BusinessCenter),
            CategoryIcon("badge", "ID badge work", Icons.Outlined.Badge),
            CategoryIcon("laptop", "Laptop computer", Icons.Outlined.Laptop),
            CategoryIcon("school", "Education school tuition", Icons.Outlined.School),
            CategoryIcon("volunteer_activism", "Donation charity", Icons.Outlined.VolunteerActivism),
            CategoryIcon("child_care", "Kids childcare", Icons.Outlined.ChildCare),
            CategoryIcon("pets", "Pets animals", Icons.Outlined.Pets),
            CategoryIcon("star", "Other favourite", Icons.Outlined.Star),
            CategoryIcon("category", "Miscellaneous other", Icons.Outlined.Category),
        ),
    ),
)

private val byKey: Map<String, CategoryIcon> =
    CATEGORY_ICON_SECTIONS.flatMap { it.icons }.associateBy { it.key }

/** Resolve a stored key to its vector; null for a null or unrecognized key (forward-compatible). */
fun iconVectorForKey(key: String?): ImageVector? = key?.let { byKey[it]?.vector }

/** Short display label for a stored key (first word of its search label); null for null/unknown. */
fun iconLabelFor(key: String?): String? = key?.let { byKey[it]?.label?.substringBefore(' ') }

/** Filter the full set by a case-insensitive substring of label or key; blank query returns all. */
fun searchIcons(query: String): List<CategoryIcon> {
    val q = query.trim()
    val all = CATEGORY_ICON_SECTIONS.flatMap { it.icons }
    if (q.isEmpty()) return all
    return all.filter { it.label.contains(q, ignoreCase = true) || it.key.contains(q, ignoreCase = true) }
}
