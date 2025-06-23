package com.moon.leetcode.math;

/**
 * 温度转换
 */
public class N2469 {
    public double[] convertTemperature(double celsius) {
        double[] res = new double[2];
        double kelvin = celsius + 273.15;
        double fahrenheit = celsius * 1.80 + 32.00;
        res[0] = kelvin;
        res[1] = fahrenheit;
        return res;
    }
}
