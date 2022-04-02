package com.novikovpashka.projectkeeper

import java.text.NumberFormat

class Helpers {

    companion object {
        fun convert(double: Double, currency: CurrencyList, usd: Double, eur: Double): String {
            var formatter = NumberFormat.getInstance()
            formatter.maximumFractionDigits = 2
            return when(currency) {
                CurrencyList.RUB -> "₽" + formatter.format(double)
                CurrencyList.USD -> "\$" + formatter.format(double/usd)
                CurrencyList.EUR -> "€" + formatter.format(double/eur)
            }
        }
    }

}