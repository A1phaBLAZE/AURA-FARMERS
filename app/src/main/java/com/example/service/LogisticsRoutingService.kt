package com.example.service

import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class LogisticsRoutingService {

    /**
     * Computes Haversine distance in Kilometers with realistic road tortuosity/winding factor (1.28x).
     */
    fun calculateRoadDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val crowFlyKm = r * c
        // Road tortuosity factor in Indian urban/semi-rural topography is ~1.28x
        val roadKm = (crowFlyKm * 1.28)
        return (roadKm * 10.0).roundToInt() / 10.0
    }

    /**
     * Estimated travel duration in minutes based on distance and average urban-rural speed (28 km/h).
     */
    fun estimateTravelDurationMinutes(distanceKm: Double): Int {
        val avgSpeedKmh = 28.0
        val travelMins = ((distanceKm / avgSpeedKmh) * 60.0).roundToInt()
        return travelMins.coerceAtLeast(10)
    }

    /**
     * Matches a consumer's requested produce order to the closest fulfilling farmer or FPO within delivery radius.
     */
    fun findNearestFulfillingListing(
        consumerLat: Double,
        consumerLon: Double,
        cropName: String,
        requiredKg: Double,
        allListings: List<D2CProduceListing>
    ): Pair<D2CProduceListing, Double>? {
        val candidates = allListings.filter { listing ->
            listing.cropName.contains(cropName, ignoreCase = true) &&
                    listing.availableStockKg >= requiredKg
        }

        if (candidates.isEmpty()) return null

        val withDistances = candidates.map { listing ->
            val dist = calculateRoadDistanceKm(consumerLat, consumerLon, listing.latitude, listing.longitude)
            listing to dist
        }

        // Filter within farmer's delivery radius or closest
        val withinRadius = withDistances.filter { (listing, dist) -> dist <= listing.deliveryRadiusKm }
        return if (withinRadius.isNotEmpty()) {
            withinRadius.minByOrNull { it.second }
        } else {
            withDistances.minByOrNull { it.second }
        }
    }

    /**
     * AI Multi-Stop Route Optimizer (Vehicle Routing Problem with Pickup & Delivery precedence constraints).
     * Solves for 1 delivery vehicle executing multiple farmer pickups and multiple consumer drop-offs.
     */
    fun computeOptimizedMultiStopRoute(
        orders: List<D2COrder>,
        depotLat: Double = 19.9975, // Central FPO Hub / Aggregation Center (Nashik APMC Gateway)
        depotLon: Double = 73.7898,
        depotName: String = "Nashik Central FPO Agri-Logistics Hub"
    ): LogisticsMultiStopRoute {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val routeDate = sdf.format(Date())

        if (orders.isEmpty()) {
            return LogisticsMultiStopRoute(
                routeDate = routeDate,
                totalPickups = 0,
                totalDeliveries = 0,
                totalCargoKg = 0.0,
                totalDistanceKm = 0.0,
                estimatedDurationMinutes = 0,
                fuelOrPowerCostInr = 0.0,
                co2SavedKg = 0.0,
                stops = emptyList()
            )
        }

        // 1. Group unique farmer pickup points
        val farmerGroups = orders.groupBy { it.farmerId }
        val pickupStops = farmerGroups.map { (_, orderList) ->
            val firstOrder = orderList.first()
            val totalKgFromFarm = orderList.sumOf { it.quantityKg }
            val cropSummary = orderList.joinToString(", ") { "${it.cropName} (${it.quantityKg.toInt()}kg)" }

            RouteStop(
                stopSequence = 0, // Assigned after optimization
                stopType = RouteStopType.PICKUP,
                partyName = "${firstOrder.farmerName} (${firstOrder.farmNameOrFpo})",
                contactNumber = "+91 98221 ${((10000..99999).random())}",
                address = "${firstOrder.farmVillage}, ${firstOrder.farmerDistrict}",
                district = firstOrder.farmerDistrict,
                latitude = firstOrder.farmerLatitude,
                longitude = firstOrder.farmerLongitude,
                cropItem = cropSummary,
                quantityKg = totalKgFromFarm,
                etaTime = "",
                cumulativeDistanceKm = 0.0,
                currentVehiclePayloadKg = 0.0,
                isCompleted = orderList.all { it.status != DeliveryStatus.BOOKED },
                orderId = null
            )
        }

        // 2. Prepare consumer delivery drop-offs
        val deliveryStops = orders.map { order ->
            RouteStop(
                stopSequence = 0,
                stopType = RouteStopType.DELIVERY,
                partyName = order.consumerName,
                contactNumber = order.consumerMobile,
                address = "${order.deliveryAddress}, ${order.deliveryDistrict} - ${order.deliveryPincode}",
                district = order.deliveryDistrict,
                latitude = order.deliveryLatitude,
                longitude = order.deliveryLongitude,
                cropItem = "${order.cropName} (${order.variety})",
                quantityKg = order.quantityKg,
                etaTime = "",
                cumulativeDistanceKm = 0.0,
                currentVehiclePayloadKg = 0.0,
                isCompleted = order.status == DeliveryStatus.DELIVERED,
                orderId = order.orderId
            )
        }

        // 3. AI Heuristic Route Ordering (Nearest Neighbor on Pickups, then Nearest Neighbor on Drop-offs)
        val orderedStops = mutableListOf<RouteStop>()
        var currentLat = depotLat
        var currentLon = depotLon

        // A. Optimize Pickups
        val remainingPickups = pickupStops.toMutableList()
        while (remainingPickups.isNotEmpty()) {
            val nearest = remainingPickups.minByOrNull {
                calculateRoadDistanceKm(currentLat, currentLon, it.latitude, it.longitude)
            }!!
            orderedStops.add(nearest)
            remainingPickups.remove(nearest)
            currentLat = nearest.latitude
            currentLon = nearest.longitude
        }

        // B. Optimize Deliveries
        val remainingDeliveries = deliveryStops.toMutableList()
        while (remainingDeliveries.isNotEmpty()) {
            val nearest = remainingDeliveries.minByOrNull {
                calculateRoadDistanceKm(currentLat, currentLon, it.latitude, it.longitude)
            }!!
            orderedStops.add(nearest)
            remainingDeliveries.remove(nearest)
            currentLat = nearest.latitude
            currentLon = nearest.longitude
        }

        // 4. Calculate Sequential ETAs, Distances, and Cargo Payload Progress
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7) // Morning Run starts 07:00 AM
            set(Calendar.MINUTE, 0)
        }

        var cumulativeDist = 0.0
        var currentPayload = 0.0
        var prevLat = depotLat
        var prevLon = depotLon

        val finalizedStops = orderedStops.mapIndexed { index, stop ->
            val legDist = calculateRoadDistanceKm(prevLat, prevLon, stop.latitude, stop.longitude)
            cumulativeDist += legDist
            val transitMins = estimateTravelDurationMinutes(legDist)
            val serviceMins = if (stop.stopType == RouteStopType.PICKUP) 15 else 8 // 15 mins loading at farm, 8 mins unloading at consumer
            calendar.add(Calendar.MINUTE, transitMins + serviceMins)

            if (stop.stopType == RouteStopType.PICKUP) {
                currentPayload += stop.quantityKg
            } else {
                currentPayload = (currentPayload - stop.quantityKg).coerceAtLeast(0.0)
            }

            prevLat = stop.latitude
            prevLon = stop.longitude

            stop.copy(
                stopSequence = index + 1,
                etaTime = timeFormat.format(calendar.time),
                cumulativeDistanceKm = (cumulativeDist * 10).roundToInt() / 10.0,
                currentVehiclePayloadKg = (currentPayload * 10).roundToInt() / 10.0
            )
        }

        val totalCargo = orders.sumOf { it.quantityKg }
        val totalMins = finalizedStops.size * 12 + estimateTravelDurationMinutes(cumulativeDist)
        // Electric EV calculation: ~₹1.8 per km vs Diesel ₹7.5 per km
        val powerCost = (cumulativeDist * 1.8).roundToInt().toDouble()
        // CO2 saved: (Individual fragmented consumer trips vs 1 combined EV route) ~ 0.18 kg CO2 per km saved
        val co2Saved = ((cumulativeDist * 1.4) * 0.18 * 10.0).roundToInt() / 10.0

        return LogisticsMultiStopRoute(
            routeDate = routeDate,
            totalPickups = pickupStops.size,
            totalDeliveries = deliveryStops.size,
            totalCargoKg = totalCargo,
            totalDistanceKm = (cumulativeDist * 10).roundToInt() / 10.0,
            estimatedDurationMinutes = totalMins,
            fuelOrPowerCostInr = powerCost,
            co2SavedKg = co2Saved,
            stops = finalizedStops
        )
    }

    /**
     * Builds comprehensive delivery tracking timeline steps for an order.
     */
    fun buildTrackingSteps(
        status: DeliveryStatus,
        orderDate: String,
        farmerName: String,
        farmVillage: String,
        consumerAddress: String,
        estimatedDeliveryTime: String
    ): List<DeliveryTrackingStep> {
        val isBooked = status.stepIndex >= 0
        val isPickedUp = status.stepIndex >= 1
        val isInTransit = status.stepIndex >= 2
        val isDelivered = status.stepIndex >= 3

        return listOf(
            DeliveryTrackingStep(
                status = DeliveryStatus.BOOKED,
                title = "Order Confirmed & Escrow Secured",
                description = "Direct farm booking confirmed. Payment held securely in Kisan Vani Escrow.",
                timestamp = orderDate,
                isCompleted = isBooked,
                isCurrent = status == DeliveryStatus.BOOKED,
                location = "Kisan Vani Digital Portal"
            ),
            DeliveryTrackingStep(
                status = DeliveryStatus.PICKED_UP,
                title = "Harvest Plucked & Picked Up from Farm",
                description = "Freshly harvested produce loaded from $farmerName's farm at $farmVillage.",
                timestamp = if (isPickedUp) "Today, 08:15 AM" else "Est: Today, 08:00 AM",
                isCompleted = isPickedUp,
                isCurrent = status == DeliveryStatus.PICKED_UP,
                location = "$farmVillage Farm Gate"
            ),
            DeliveryTrackingStep(
                status = DeliveryStatus.IN_TRANSIT,
                title = "On Route in Temperature-Controlled EV Van",
                description = "Dispatched via Green Fleet Van (MH-15-EV-4092). Driver Sunil Shinde.",
                timestamp = if (isInTransit) "Today, 09:30 AM" else "Est: Today, 09:15 AM",
                isCompleted = isInTransit,
                isCurrent = status == DeliveryStatus.IN_TRANSIT,
                location = "En Route (Hyperlocal Corridor)"
            ),
            DeliveryTrackingStep(
                status = DeliveryStatus.DELIVERED,
                title = "Delivered to Doorstep & Escrow Released",
                description = "Delivered to $consumerAddress. Farmer receives instant direct payout.",
                timestamp = if (isDelivered) "Today, 10:45 AM" else estimatedDeliveryTime,
                isCompleted = isDelivered,
                isCurrent = status == DeliveryStatus.DELIVERED,
                location = consumerAddress
            )
        )
    }
}
