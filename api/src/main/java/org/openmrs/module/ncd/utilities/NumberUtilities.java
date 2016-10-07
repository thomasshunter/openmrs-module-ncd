package org.openmrs.module.ncd.utilities;

//Important: extends Object, so an instance of this class can be referenced from within Velocity.
public class NumberUtilities {

    public NumberUtilities() {
    }

    /**
     * Formats an integer to a fixed field width with leading spaces.
     * 
     * @param n The integer to be formatted.
     * @param width The output field width.
     * @return The formatted integer.
     * @deprecated Use format(int n, int width) instead.
     */
    public String formatN(int n, int width) {
        return format(n, width);
    }

    /**
     * Formats an integer to a fixed field width with leading spaces.
     * 
     * @param n The integer to be formatted.
     * @param width The output field width.
     * @return The formatted integer.
     */
    public String format(int n, int width) {
        return String.format("%1$" + width + "d", n);
    }

    /**
     * Formats a long to a fixed field width with leading spaces.
     * 
     * @param n The long to be formatted.
     * @param width The output field width.
     * @return The formatted long.
     */
    public String format(long n, int width) {
        return String.format("%1$" + width + "d", n);
    }

    /**
     * Formats a double to a fixed field width with leading spaces.
     * 
     * @param n The integer to be formatted.
     * @param width The output field width.
     * @return The formatted integer.
     */
    public String format(double n, int width, int frac) {
        return String.format("%1$" + width + "." + frac + "f", n);
    }
}
