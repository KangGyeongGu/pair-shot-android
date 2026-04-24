package com.pairshot.core.coupon.domain

sealed interface CouponDuration {
    val days: Long?

    sealed interface Preset : CouponDuration {
        data object Days7 : Preset {
            override val days: Long = PRESET_DAYS_7
        }

        data object Days30 : Preset {
            override val days: Long = PRESET_DAYS_30
        }

        data object Days60 : Preset {
            override val days: Long = PRESET_DAYS_60
        }

        data object Days90 : Preset {
            override val days: Long = PRESET_DAYS_90
        }

        data object Unlimited : Preset {
            override val days: Long? = null
        }
    }

    data class Custom(
        override val days: Long,
    ) : CouponDuration {
        init {
            require(days > 0) { "Custom duration must be positive" }
        }
    }

    companion object {
        fun fromDays(days: Long?): CouponDuration =
            when (days) {
                null -> {
                    Preset.Unlimited
                }

                PRESET_DAYS_7 -> {
                    Preset.Days7
                }

                PRESET_DAYS_30 -> {
                    Preset.Days30
                }

                PRESET_DAYS_60 -> {
                    Preset.Days60
                }

                PRESET_DAYS_90 -> {
                    Preset.Days90
                }

                else -> {
                    require(days > 0) { "Duration must be positive" }
                    Custom(days)
                }
            }

        internal const val PRESET_DAYS_7 = 7L
        internal const val PRESET_DAYS_30 = 30L
        internal const val PRESET_DAYS_60 = 60L
        internal const val PRESET_DAYS_90 = 90L
    }
}
