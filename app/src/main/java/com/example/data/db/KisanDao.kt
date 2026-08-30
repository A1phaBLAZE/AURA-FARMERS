package com.example.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KisanDao {

    @Query("SELECT * FROM cached_crops")
    fun getAllCrops(): Flow<List<CropEntity>>

    @Query("SELECT COUNT(*) FROM cached_crops")
    suspend fun getCropsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrops(crops: List<CropEntity>)

    @Query("SELECT * FROM farmer_lots ORDER BY dateCreated DESC")
    fun getAllLots(): Flow<List<LotEntity>>

    @Query("SELECT COUNT(*) FROM farmer_lots")
    suspend fun getLotsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLot(lot: LotEntity)

    @Update
    suspend fun updateLot(lot: LotEntity)

    @Query("DELETE FROM farmer_lots WHERE id = :lotId")
    suspend fun deleteLot(lotId: String)

    @Query("SELECT * FROM buyer_offers WHERE lotId = :lotId")
    fun getOffersForLot(lotId: String): Flow<List<OfferEntity>>

    @Query("SELECT * FROM buyer_offers")
    fun getAllOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM buyer_offers WHERE id = :id LIMIT 1")
    suspend fun getOfferById(id: String): OfferEntity?

    @Query("SELECT COUNT(*) FROM buyer_offers")
    suspend fun getOffersCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: OfferEntity)

    @Update
    suspend fun updateOffer(offer: OfferEntity)

    @Query("SELECT * FROM razorpay_payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT COUNT(*) FROM razorpay_payments")
    suspend fun getPaymentsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)
}
