package com.vspace.util

import android.graphics.Point
import kotlin.math.*

/**
 * Utility class providing basic 2D geometry calculations used for touch
 * interaction and drag-and-drop coordinate computation.
 */
class MathUtil {
    companion object {
        /**
         * Computes the Euclidean distance between two 2D points.
         *
         * @param x1 x-coordinate of the first point.
         * @param y1 y-coordinate of the first point.
         * @param x2 x-coordinate of the second point.
         * @param y2 y-coordinate of the second point.
         * @return the integer distance between the two points.
         */
        fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Int {
            return sqrt((x1 - x2).toDouble().pow(2.0) + (y1 - y2).toDouble().pow(2.0))
                .toInt()
        }

        /**
         * Calculates a point on the line from [A] to [B] at the specified [cutLength] from [A].
         *
         * @param A the starting point.
         * @param B the ending point.
         * @param cutLength the distance from [A] along the line toward [B].
         * @return the interpolated [Point].
         */
        fun getPointByCutLength(A: Point, B: Point, cutLength: Int): Point {
            val radian = getRadian(A, B)
            return Point(
                A.x + (cutLength * cos(radian.toDouble())).toInt(),
                A.y + (cutLength * sin(radian.toDouble())).toInt()
            )
        }

        /**
         * Computes the radian angle between the line segment (A -> B) and the horizontal axis.
         * The result is negated when [B] is above [A] to account for the inverted Y-axis.
         *
         * @param A the starting point.
         * @param B the ending point.
         * @return the angle in radians.
         */
        private fun getRadian(A: Point, B: Point): Float {
            val lenA: Int = B.x - A.x
            val lenB: Int = B.y - A.y
            val lenC = sqrt((lenA * lenA + lenB * lenB).toDouble()).toFloat()
            var radian = acos((lenA / lenC).toDouble()).toFloat()

            radian *= if (B.y < A.y) -1 else 1
            return radian
        }
    }
}
