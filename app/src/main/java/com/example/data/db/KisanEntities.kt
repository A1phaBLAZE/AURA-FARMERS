package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_crops")
data class CropEntity(
    @PrimaryKey val id: String,
    val nameEn: String,
    val nameMr: String,
    val nameHi: String,
    val nameGu: String,
    val category: String,
    val currentPrice: Int,
    val minPrice: Int,
    val maxPrice: Int,
    val modalPrice: Int,
    val mandiName: String,
    val district: String,
    val state: String,
    val arrivalVolumeQuintals: Int,
    val trendPercent: Double,
    val recommendationEn: String,
    val recommendationMr: String,
    val recommendationHi: String,
    val recommendationGu: String,
    val priceHistoryJson: String,
    val emoji: String
)

@Entity(tableName = "farmer_lots")
data class LotEntity(
    @PrimaryKey val id: String,
    val cropName: String,
    val variety: String,
    val qualityGrade: String,
    val quantityQuintals: Double,
    val expectedPricePerQuintal: Int,
    val locationDistrict: String,
    val locationTaluka: String,
    val storageType: String,
    val harvestDate: String,
    val status: String,
    val offersCount: Int,
    val dateCreated: String
)

@Entity(tableName = "buyer_offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val lotId: String,
    val buyerName: String,
    val buyerCompany: String,
    val buyerRating: Double,
    val isVerified: Boolean,
    val offeredPricePerQuintal: Int,
    val pickupDate: String,
    val paymentTerms: String,
    val status: String,
    val counteredPrice: Int? = null
)

@Entity(tableName = "razorpay_payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val lotId: String,
    val cropName: String,
    val amountInr: Double,
    val razorpayOrderId: String,
    val paymentId: String,
    val utrNumber: String,
    val buyerName: String,
    val farmerKisanCard: String,
    val status: String,
    val timestamp: String,
    val disputeReason: String? = null
)
