package com.example.data.db

import com.example.data.InitialData
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KisanRepository(private val dao: KisanDao) {

    suspend fun seedDatabaseIfEmpty() {
        // Only seed initial data if the database is actually empty
        val existingCropsCount = dao.getCropsCount()
        if (existingCropsCount > 0) {
            return
        }

        // Map initial crops to entities
        val cropEntities = InitialData.cropsList.map { crop ->
            CropEntity(
                id = crop.id,
                nameEn = crop.nameEn,
                nameMr = crop.nameMr,
                nameHi = crop.nameHi,
                nameGu = crop.nameGu,
                category = crop.category.name,
                currentPrice = crop.currentPrice,
                minPrice = crop.minPrice,
                maxPrice = crop.maxPrice,
                modalPrice = crop.modalPrice,
                mandiName = crop.mandiName,
                district = crop.district,
                state = crop.state,
                arrivalVolumeQuintals = crop.arrivalVolumeQuintals,
                trendPercent = crop.trendPercent,
                recommendationEn = crop.recommendationEn,
                recommendationMr = crop.recommendationMr,
                recommendationHi = crop.recommendationHi,
                recommendationGu = crop.recommendationGu,
                priceHistoryJson = crop.priceHistory.joinToString(";") { "${it.dayLabel},${it.price},${it.volumeTonnes}" },
                emoji = crop.emoji
            )
        }
        dao.insertCrops(cropEntities)

        val lotEntities = InitialData.sampleLots.map { lot ->
            LotEntity(
                id = lot.id,
                cropName = lot.cropName,
                variety = lot.variety,
                qualityGrade = lot.qualityGrade,
                quantityQuintals = lot.quantityQuintals,
                expectedPricePerQuintal = lot.expectedPricePerQuintal,
                locationDistrict = lot.locationDistrict,
                locationTaluka = lot.locationTaluka,
                storageType = lot.storageType,
                harvestDate = lot.harvestDate,
                status = lot.status.name,
                offersCount = lot.offersCount,
                dateCreated = lot.dateCreated
            )
        }
        lotEntities.forEach { dao.insertLot(it) }

        val offerEntities = InitialData.sampleBuyerOffers.map { offer ->
            OfferEntity(
                id = offer.id,
                lotId = offer.lotId,
                buyerName = offer.buyerName,
                buyerCompany = offer.buyerCompany,
                buyerRating = offer.buyerRating,
                isVerified = offer.isVerified,
                offeredPricePerQuintal = offer.offeredPricePerQuintal,
                pickupDate = offer.pickupDate,
                paymentTerms = offer.paymentTerms,
                status = offer.status.name
            )
        }
        dao.insertOffers(offerEntities)

        val paymentEntities = InitialData.samplePayments.map { pay ->
            PaymentEntity(
                id = pay.id,
                lotId = pay.lotId,
                cropName = pay.cropName,
                amountInr = pay.amountInr,
                razorpayOrderId = pay.razorpayOrderId,
                paymentId = pay.paymentId,
                utrNumber = pay.utrNumber,
                buyerName = pay.buyerName,
                farmerKisanCard = pay.farmerKisanCard,
                status = pay.status.name,
                timestamp = pay.timestamp,
                disputeReason = pay.disputeReason
            )
        }
        paymentEntities.forEach { dao.insertPayment(it) }
    }

    suspend fun updateCrops(crops: List<CropItem>) {
        val entities = crops.map { crop ->
            CropEntity(
                id = crop.id,
                nameEn = crop.nameEn,
                nameMr = crop.nameMr,
                nameHi = crop.nameHi,
                nameGu = crop.nameGu,
                category = crop.category.name,
                currentPrice = crop.currentPrice,
                minPrice = crop.minPrice,
                maxPrice = crop.maxPrice,
                modalPrice = crop.modalPrice,
                mandiName = crop.mandiName,
                district = crop.district,
                state = crop.state,
                arrivalVolumeQuintals = crop.arrivalVolumeQuintals,
                trendPercent = crop.trendPercent,
                recommendationEn = crop.recommendationEn,
                recommendationMr = crop.recommendationMr,
                recommendationHi = crop.recommendationHi,
                recommendationGu = crop.recommendationGu,
                priceHistoryJson = crop.priceHistory.joinToString(";") { "${it.dayLabel},${it.price},${it.volumeTonnes}" },
                emoji = crop.emoji
            )
        }
        dao.insertCrops(entities)
    }

    val allCrops: Flow<List<CropItem>> = dao.getAllCrops().map { entities ->
        if (entities.isEmpty()) {
            InitialData.cropsList
        } else {
            entities.map { entity ->
                val pricePoints = entity.priceHistoryJson.split(";").filter { it.isNotBlank() }.map { part ->
                    val pieces = part.split(",")
                    PricePoint(
                        dayLabel = pieces.getOrNull(0) ?: "Day",
                        price = pieces.getOrNull(1)?.toIntOrNull() ?: entity.currentPrice,
                        volumeTonnes = pieces.getOrNull(2)?.toIntOrNull() ?: 1000
                    )
                }
                CropItem(
                    id = entity.id,
                    nameEn = entity.nameEn,
                    nameMr = entity.nameMr,
                    nameHi = entity.nameHi,
                    nameGu = entity.nameGu,
                    category = try { CropCategory.valueOf(entity.category) } catch (e: Exception) { CropCategory.ALL },
                    currentPrice = entity.currentPrice,
                    minPrice = entity.minPrice,
                    maxPrice = entity.maxPrice,
                    modalPrice = entity.modalPrice,
                    mandiName = entity.mandiName,
                    district = entity.district,
                    state = entity.state,
                    arrivalVolumeQuintals = entity.arrivalVolumeQuintals,
                    trendPercent = entity.trendPercent,
                    recommendationEn = entity.recommendationEn,
                    recommendationMr = entity.recommendationMr,
                    recommendationHi = entity.recommendationHi,
                    recommendationGu = entity.recommendationGu,
                    priceHistory = pricePoints.ifEmpty { InitialData.cropsList.first().priceHistory },
                    emoji = entity.emoji
                )
            }
        }
    }

    val allLots: Flow<List<FarmerLot>> = dao.getAllLots().map { entities ->
        if (entities.isEmpty()) {
            InitialData.sampleLots
        } else {
            entities.map {
                FarmerLot(
                    id = it.id,
                    cropName = it.cropName,
                    variety = it.variety,
                    qualityGrade = it.qualityGrade,
                    quantityQuintals = it.quantityQuintals,
                    expectedPricePerQuintal = it.expectedPricePerQuintal,
                    locationDistrict = it.locationDistrict,
                    locationTaluka = it.locationTaluka,
                    storageType = it.storageType,
                    harvestDate = it.harvestDate,
                    status = try { LotStatus.valueOf(it.status) } catch (e: Exception) { LotStatus.ACTIVE },
                    offersCount = it.offersCount,
                    dateCreated = it.dateCreated
                )
            }
        }
    }

    val allOffers: Flow<List<BuyerOffer>> = dao.getAllOffers().map { entities ->
        if (entities.isEmpty()) {
            InitialData.sampleBuyerOffers
        } else {
            entities.map {
                BuyerOffer(
                    id = it.id,
                    lotId = it.lotId,
                    buyerName = it.buyerName,
                    buyerCompany = it.buyerCompany,
                    buyerRating = it.buyerRating,
                    isVerified = it.isVerified,
                    offeredPricePerQuintal = it.offeredPricePerQuintal,
                    pickupDate = it.pickupDate,
                    paymentTerms = it.paymentTerms,
                    status = try { OfferStatus.valueOf(it.status) } catch (e: Exception) { OfferStatus.PENDING },
                    counteredPrice = it.counteredPrice
                )
            }
        }
    }

    val allPayments: Flow<List<RazorpayPayment>> = dao.getAllPayments().map { entities ->
        if (entities.isEmpty()) {
            InitialData.samplePayments
        } else {
            entities.map {
                RazorpayPayment(
                    id = it.id,
                    lotId = it.lotId,
                    cropName = it.cropName,
                    amountInr = it.amountInr,
                    razorpayOrderId = it.razorpayOrderId,
                    paymentId = it.paymentId,
                    utrNumber = it.utrNumber,
                    buyerName = it.buyerName,
                    farmerKisanCard = it.farmerKisanCard,
                    status = try { PaymentStatus.valueOf(it.status) } catch (e: Exception) { PaymentStatus.ESCROW_LOCKED },
                    timestamp = it.timestamp,
                    disputeReason = it.disputeReason
                )
            }
        }
    }

    suspend fun createLot(lot: FarmerLot) {
        dao.insertLot(
            LotEntity(
                id = lot.id,
                cropName = lot.cropName,
                variety = lot.variety,
                qualityGrade = lot.qualityGrade,
                quantityQuintals = lot.quantityQuintals,
                expectedPricePerQuintal = lot.expectedPricePerQuintal,
                locationDistrict = lot.locationDistrict,
                locationTaluka = lot.locationTaluka,
                storageType = lot.storageType,
                harvestDate = lot.harvestDate,
                status = lot.status.name,
                offersCount = lot.offersCount,
                dateCreated = lot.dateCreated
            )
        )
    }

    suspend fun updateOfferStatus(
        offerId: String,
        newStatus: OfferStatus,
        lotId: String,
        counteredPrice: Int? = null
    ) {
        val existingEntity = dao.getOfferById(offerId)
        if (existingEntity != null) {
            val updated = existingEntity.copy(
                status = newStatus.name,
                counteredPrice = counteredPrice ?: existingEntity.counteredPrice
            )
            dao.updateOffer(updated)
        } else {
            val target = InitialData.sampleBuyerOffers.find { it.id == offerId }
            if (target != null) {
                val updated = target.copy(
                    status = newStatus,
                    counteredPrice = counteredPrice ?: target.counteredPrice
                )
                dao.insertOffer(
                    OfferEntity(
                        id = updated.id,
                        lotId = updated.lotId,
                        buyerName = updated.buyerName,
                        buyerCompany = updated.buyerCompany,
                        buyerRating = updated.buyerRating,
                        isVerified = updated.isVerified,
                        offeredPricePerQuintal = updated.offeredPricePerQuintal,
                        pickupDate = updated.pickupDate,
                        paymentTerms = updated.paymentTerms,
                        status = updated.status.name,
                        counteredPrice = updated.counteredPrice
                    )
                )
            }
        }
    }

    suspend fun addPayment(payment: RazorpayPayment) {
        dao.insertPayment(
            PaymentEntity(
                id = payment.id,
                lotId = payment.lotId,
                cropName = payment.cropName,
                amountInr = payment.amountInr,
                razorpayOrderId = payment.razorpayOrderId,
                paymentId = payment.paymentId,
                utrNumber = payment.utrNumber,
                buyerName = payment.buyerName,
                farmerKisanCard = payment.farmerKisanCard,
                status = payment.status.name,
                timestamp = payment.timestamp,
                disputeReason = payment.disputeReason
            )
        )
    }
}
