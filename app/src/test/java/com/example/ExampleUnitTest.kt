package com.example

import com.example.data.GovtProcurementData
import com.example.data.model.GovtTokenStatus
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testUpFarmerProfile_validLandRecords() {
        val upFarmer = GovtProcurementData.upFarmerProfile
        assertEquals("Uttar Pradesh", upFarmer.state)
        assertEquals("Hardoi", upFarmer.district)
        assertTrue(upFarmer.khatauniOrKhatianNo.contains("00412"))
        assertTrue(upFarmer.gataOrDagNo.contains("418/1"))
        assertTrue(upFarmer.isLandVerified)
        assertTrue(upFarmer.dbtLinked)
        assertEquals(4.5, upFarmer.totalLandAreaAcres, 0.01)
    }

    @Test
    fun testWbFarmerProfile_validBanglarBhumiRecords() {
        val wbFarmer = GovtProcurementData.wbFarmerProfile
        assertEquals("West Bengal", wbFarmer.state)
        assertEquals("Purba Bardhaman", wbFarmer.district)
        assertTrue(wbFarmer.khatauniOrKhatianNo.contains("1842"))
        assertTrue(wbFarmer.gataOrDagNo.contains("812"))
        assertTrue(wbFarmer.isLandVerified)
        assertTrue(wbFarmer.dbtLinked)
    }

    @Test
    fun testMspYieldNormCalculation_wheat() {
        val wheat = GovtProcurementData.mspCropsList.first { it.cropNameEn == "Wheat" }
        assertEquals(2425, wheat.mspPricePerQuintal)
        assertEquals(18.0, wheat.maxYieldNormQuintalPerAcre, 0.01)
        assertEquals(12.0, wheat.maxMoistureAllowedPercent, 0.01)

        val cultivatedAcres = 4.0
        val maxQuota = cultivatedAcres * wheat.maxYieldNormQuintalPerAcre
        assertEquals(72.0, maxQuota, 0.01)

        val totalValue = maxQuota * wheat.mspPricePerQuintal
        assertEquals(174600.0, totalValue, 0.01)
    }

    @Test
    fun testMspYieldNormCalculation_paddy() {
        val paddy = GovtProcurementData.mspCropsList.first { it.cropNameEn.contains("Paddy (Common)") }
        assertEquals(2300, paddy.mspPricePerQuintal)
        assertEquals(20.0, paddy.maxYieldNormQuintalPerAcre, 0.01)
        assertEquals(17.0, paddy.maxMoistureAllowedPercent, 0.01)

        val cultivatedAcres = 3.2
        val maxQuota = cultivatedAcres * paddy.maxYieldNormQuintalPerAcre
        assertEquals(64.0, maxQuota, 0.01)

        val totalValue = maxQuota * paddy.mspPricePerQuintal
        assertEquals(147200.0, totalValue, 0.01)
    }

    @Test
    fun testProcurementReceipt_calculations() {
        val receipt = GovtProcurementData.initialUpProcurementReceipt
        assertEquals(70.0, receipt.acceptedQuantityQuintals, 0.01)
        assertEquals(2425, receipt.mspRatePerQuintal)
        assertEquals(169750.0, receipt.netPayableInr, 0.01)
        assertTrue(receipt.paymentStatus.contains("PFMS"))
    }

    @Test
    fun testCropForecastService_generatesHistoricalAnd30DayPrediction() {
        val crop = com.example.data.InitialData.cropsList.first()
        val forecastService = com.example.service.CropForecastService()
        val analysis = forecastService.generateForecastAnalysis(crop, com.example.data.model.TrendDateRange.DAYS_30)

        assertEquals(crop.id, analysis.cropId)
        assertEquals(crop.currentPrice, analysis.currentOfficialPrice)
        assertTrue("Historical points should not be empty", analysis.historicalPoints.isNotEmpty())
        assertEquals("Predicted points should contain 30 days", 30, analysis.predictedPoints.size)
        assertTrue("Predicted highest price should be >= lowest price", analysis.predictedHighestPrice >= analysis.predictedLowestPrice)
        assertTrue("AI rationale should not be blank", analysis.aiRationaleEn.isNotBlank())
        assertTrue("Selling window should not be blank", analysis.optimalSellingWindow.isNotBlank())
        assertTrue("Should have driving factors", analysis.keyDrivingFactors.isNotEmpty())

        // Verify that historical points are marked as isPrediction = false
        analysis.historicalPoints.forEach { pt ->
            assertFalse(pt.isPrediction)
        }

        // Verify that predicted points are marked as isPrediction = true
        analysis.predictedPoints.forEach { pt ->
            assertTrue(pt.isPrediction)
        }
    }
}

