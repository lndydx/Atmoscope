package com.lnxteam.atmoscope.data

import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.*

data class PlanetInfo(
    val name: String,
    val emoji: String,
    val altitude: Double,
    val azimuth: Double,
    val isVisible: Boolean,
    val description: String
)

private data class OrbitalElements(
    val N: Double, val i: Double, val w: Double,
    val a: Double, val e: Double, val M: Double
)

object PlanetCalculator {

    private const val RAD2DEG = 180.0 / PI

    private fun sind(deg: Double) = sin(Math.toRadians(deg))
    private fun cosd(deg: Double) = cos(Math.toRadians(deg))
    private fun atan2d(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    private fun rev(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    private fun daysSinceEpoch(time: ZonedDateTime): Double {
        val utc = time.withZoneSameInstant(ZoneOffset.UTC)
        val y = utc.year; val mth = utc.monthValue; val day = utc.dayOfMonth
        val hour = utc.hour + utc.minute / 60.0 + utc.second / 3600.0
        val a = (14 - mth) / 12
        val yA = y + 4800 - a
        val mA = mth + 12 * a - 3
        val jdn = day + (153 * mA + 2) / 5 + 365 * yA + yA / 4 - yA / 100 + yA / 400 - 32045
        val jd = jdn + (hour - 12) / 24.0
        return jd - 2451543.5
    }

    private fun elementsFor(planet: String, d: Double): OrbitalElements = when (planet) {
        "Mercury" -> OrbitalElements(
            N = rev(48.3313 + 3.24587E-5 * d), i = 7.0047 + 5.00E-8 * d,
            w = rev(29.1241 + 1.01444E-5 * d), a = 0.387098,
            e = 0.205635 + 5.59E-10 * d, M = rev(168.6562 + 4.0923344368 * d)
        )
        "Venus" -> OrbitalElements(
            N = rev(76.6799 + 2.46590E-5 * d), i = 3.3946 + 2.75E-8 * d,
            w = rev(54.8910 + 1.38374E-5 * d), a = 0.723330,
            e = 0.006773 - 1.302E-9 * d, M = rev(48.0052 + 1.6021302244 * d)
        )
        "Mars" -> OrbitalElements(
            N = rev(49.5574 + 2.11081E-5 * d), i = 1.8497 - 1.78E-8 * d,
            w = rev(286.5016 + 2.92961E-5 * d), a = 1.523688,
            e = 0.093405 + 2.516E-9 * d, M = rev(18.6021 + 0.5240207766 * d)
        )
        "Jupiter" -> OrbitalElements(
            N = rev(100.4542 + 2.76854E-5 * d), i = 1.3030 - 1.557E-7 * d,
            w = rev(273.8777 + 1.64505E-5 * d), a = 5.20256,
            e = 0.048498 + 4.469E-9 * d, M = rev(19.8950 + 0.0830853001 * d)
        )
        "Saturn" -> OrbitalElements(
            N = rev(113.6634 + 2.38980E-5 * d), i = 2.4886 - 1.081E-7 * d,
            w = rev(339.3939 + 2.97661E-5 * d), a = 9.55475,
            e = 0.055546 - 9.499E-9 * d, M = rev(316.9670 + 0.0334442282 * d)
        )
        "Uranus" -> OrbitalElements(
            N = rev(74.0005 + 1.3978E-5 * d), i = 0.7733 + 1.9E-8 * d,
            w = rev(96.6612 + 3.0565E-5 * d), a = 19.18171 - 1.55E-8 * d,
            e = 0.047318 + 7.45E-9 * d, M = rev(142.5905 + 0.011725806 * d)
        )
        "Neptune" -> OrbitalElements(
            N = rev(131.7806 + 3.0173E-5 * d), i = 1.7700 - 2.55E-7 * d,
            w = rev(272.8461 - 6.027E-6 * d), a = 30.05826 + 3.313E-8 * d,
            e = 0.008606 + 2.15E-9 * d, M = rev(260.2471 + 0.005995147 * d)
        )
        else -> throw IllegalArgumentException("Unknown planet")
    }

    private fun solveKepler(M: Double, e: Double): Double {
        var E = M + RAD2DEG * e * sind(M) * (1 + e * cosd(M))
        repeat(4) {
            val dE = (E - RAD2DEG * e * sind(E) - M) / (1 - e * cosd(E))
            E -= dE
        }
        return E
    }

    private fun heliocentric(el: OrbitalElements): Triple<Double, Double, Double> {
        val E = solveKepler(el.M, el.e)
        val xv = el.a * (cosd(E) - el.e)
        val yv = el.a * (sqrt(1 - el.e * el.e) * sind(E))
        val v = atan2d(yv, xv)
        val r = sqrt(xv * xv + yv * yv)

        val xh = r * (cosd(el.N) * cosd(v + el.w) - sind(el.N) * sind(v + el.w) * cosd(el.i))
        val yh = r * (sind(el.N) * cosd(v + el.w) + cosd(el.N) * sind(v + el.w) * cosd(el.i))
        val zh = r * (sind(v + el.w) * sind(el.i))
        return Triple(xh, yh, zh)
    }

    private fun sunGeocentric(d: Double): Triple<Double, Double, Double> {
        val w = rev(282.9404 + 4.70935E-5 * d)
        val e = 0.016709 - 1.151E-9 * d
        val M = rev(356.0470 + 0.9856002585 * d)
        val E = solveKepler(M, e)
        val xv = cosd(E) - e
        val yv = sqrt(1 - e * e) * sind(E)
        val v = atan2d(yv, xv)
        val r = sqrt(xv * xv + yv * yv)
        val lonsun = rev(v + w)
        return Triple(r * cosd(lonsun), r * sind(lonsun), 0.0)
    }

    private fun toEquatorial(x: Double, y: Double, z: Double, ecl: Double): Pair<Double, Double> {
        val ye = y * cosd(ecl) - z * sind(ecl)
        val ze = y * sind(ecl) + z * cosd(ecl)
        val ra = rev(atan2d(ye, x))
        val dec = atan2d(ze, sqrt(x * x + ye * ye))
        return Pair(ra, dec)
    }

    private fun altAz(ra: Double, dec: Double, lst: Double, lat: Double): Pair<Double, Double> {
        val ha = rev(lst - ra)
        val x = cosd(ha) * cosd(dec)
        val y = sind(ha) * cosd(dec)
        val z = sind(dec)
        val xhor = x * sind(lat) - z * cosd(lat)
        val yhor = y
        val zhor = x * cosd(lat) + z * sind(lat)
        val az = rev(atan2d(yhor, xhor) + 180)
        val alt = atan2d(zhor, sqrt(xhor * xhor + yhor * yhor))
        return Pair(alt, az)
    }

    fun calculateAll(lat: Double, lon: Double, time: ZonedDateTime = ZonedDateTime.now()): List<PlanetInfo> {
        val d = daysSinceEpoch(time)
        val ecl = 23.4393 - 3.563E-7 * d

        val (xs, ys, zs) = sunGeocentric(d)
        val (raSun, decSun) = toEquatorial(xs, ys, zs, ecl)

        val utHour = time.withZoneSameInstant(ZoneOffset.UTC)
            .let { it.hour + it.minute / 60.0 + it.second / 3600.0 }
        val wSun = rev(282.9404 + 4.70935E-5 * d)
        val mSun = rev(356.0470 + 0.9856002585 * d)
        val gmst0 = rev(wSun + mSun + 180)
        val lst = rev(gmst0 + utHour * 15 + lon)

        val (altSun, _) = altAz(raSun, decSun, lst, lat)
        val isNight = altSun < 0

        val planets = listOf(
            Triple("Mercury", "☿️", "Merkurius"),
            Triple("Venus", "♀", "Venus"),
            Triple("Mars", "♂", "Mars"),
            Triple("Jupiter", "♃", "Jupiter"),
            Triple("Saturn", "♄", "Saturnus"),
            Triple("Uranus", "♅", "Uranus"),
            Triple("Neptune", "♆", "Neptunus")
        )

        return planets.map { (key, emoji, label) ->
            val el = elementsFor(key, d)
            val (xh, yh, zh) = heliocentric(el)
            val (ra, dec) = toEquatorial(xh + xs, yh + ys, zh + zs, ecl)
            val (alt, az) = altAz(ra, dec, lst, lat)
            val visible = alt > 0 && isNight
            val needsTelescope = key == "Uranus" || key == "Neptune"
            val desc = when {
                visible && needsTelescope -> "Posisinya di atas ufuk, tapi butuh teropong/teleskop untuk melihatnya"
                visible -> "Terlihat di langit, ketinggian ${alt.roundToInt()}° dari ufuk"
                alt > 0 && !isNight -> "Di atas ufuk, tapi langit masih terang"
                else -> "Di bawah ufuk, belum bisa diamati"
            }
            PlanetInfo(label, emoji, alt, az, visible, desc)
        }.sortedByDescending { it.altitude }
    }
}