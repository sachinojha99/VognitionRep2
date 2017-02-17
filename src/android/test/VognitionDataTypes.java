package com.vognition.opensdk;

/**
 * Created by noahternullo on 11/21/14.
 */
public class VognitionDataTypes {
    /**
     * Vognition assumes that a thermostat will have at least 4 timesetpoints on each day of the week.
     * It further assumes that the 0th timesetpoint corresponds to a MORNING, and that the 3rd timesetpoint corresponds to NIGHT.
     * @author noahternullo
     *
     */
    public static enum FunctionalMode {
        NOT_IDENTIFIED(-1),
        Morning(420),
        Afternoon(720),
        Evening(1020),
        Night(1200);

        FunctionalMode(int time_in_minutes) {
            this.time_in_minutes = time_in_minutes;
        }

        private int time_in_minutes;

        /**
         * Returns the index into array of time_set_points this functionalmode represents
         * @return ordinal()-1;
         */
        public int index() {
            return (this.ordinal()-1);
        }

        public int getTimeInMintues() {
            return time_in_minutes;
        }

        /**
         *
         * @param response The WRM4 response from the service
         * @return the equivalent FunctionalMode
         * @throws IllegalArgumentException if the character isn't '0', '1', '2', '3', or '4'
         */
        public static FunctionalMode convertFromResponseString(char f_mode_character) {

            switch (f_mode_character) {
                case '0':
                    return NOT_IDENTIFIED;

                case '1':
                    return Morning;

                case '2':
                    return Afternoon;

                case '3':
                    return Evening;

                case '4':
                    return Night;

                default:
                    throw new IllegalArgumentException("Value ["+f_mode_character+"] can't be converted into a FunctionalMode.");
            }
        }

        public boolean isSpecificMode() {
            return (this != NOT_IDENTIFIED);
        }
    }

    public static enum WRM4_Weekday {
        mon,
        tue,
        wed,
        thu,
        fri,
        sat,
        sun,
        invoke_now,
        all_weekdays,
        weekend,
        full_week,
        NOT_IDENTIFIED;

        /**
         *
         * @param response The WRM4 response from the service.  This is assumed to be 0-a, in hexadecimal
         * @return the equivalent WRM4_Weekday Mode
         * @throws ArrayOutOfBounds if the hexadecimal value is beyond A
         * @throws NumberFormatException if the character passed in can't be converted to an integer by parsing it base 16.
         */
        public static WRM4_Weekday convertFromResponseString(char weekday_char) {
            return WRM4_Weekday.values()[Integer.parseInt(""+weekday_char,16)];
        }

        public boolean isSpecificDay() {
            return (this.ordinal() < invoke_now.ordinal());
        }

        /**
         * @return 0 for Monday....6 for Sunday
         */
        public int dayOfWeek() {
            return ordinal();
        }


    }

    public static enum OpMode {
        BOTH,
        HEAT,
        COOL;

        public boolean includesHeat() {
            return (this == HEAT || this == BOTH);
        }

        public boolean includesCool() {
            return (this == COOL || this == BOTH);
        }

        public static OpMode convertFromResponseString(char op_mode_char) {
            return OpMode.values()[Integer.parseInt(""+op_mode_char)];
        }
    }
}
