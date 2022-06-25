package com.novikovpashka.projectkeeper

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class Helpers {

    companion object {
        fun convertPrice(double: Double, currency: CurrencyList, usd: Double, eur: Double): String {
            val formatter = NumberFormat.getInstance()
            formatter.maximumFractionDigits = 2
            return when(currency) {
                CurrencyList.RUB -> "₽" + formatter.format(double)
                CurrencyList.USD -> "\$" + formatter.format(double/usd)
                CurrencyList.EUR -> "€" + formatter.format(double/eur)
            }
        }

        fun convertDate(date: Long): String {
            val simpleDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.ENGLISH)
            return simpleDateFormat.format(date).toString()
        }

        fun convertPriceProject(price: Double): String {
            val formatter = NumberFormat.getInstance()
            formatter.maximumFractionDigits = 2
            return formatter.format(price)

        }
    }

}