package lcf.weather;

import java.util.Calendar;
import java.util.Date;

abstract public class Moon {
	public enum MoonPhases {
		MOON_NEW, MOON_EVENING_CRESCENT, MOON_FIRST_QUARTER, MOON_WAXING_GIBBOUS, MOON_FULL, MOON_WANING_GIBBOUS, MOON_LAST_QUARTER, MOON_MORNING_CRESCENT
	}

	public static MoonPhases get() {
		return get(new Date());
	}

	public static MoonPhases get(Date when) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(when);
		int y = cal.get(Calendar.YEAR);
		int m = cal.get(Calendar.MONTH) + (12 - Calendar.DECEMBER); //+1 because january is 0
		int d = cal.get(Calendar.DAY_OF_MONTH);

		int yy = y - (12 - m) / 10;
		int mm = m + 9;
		if (mm >= 12) {
			mm = mm - 12;
		}
		int k1 = (int) (365.25 * (yy + 4712));
		int k2 = (int) (30.6001 * mm + 0.5);
		int k3 = (int) (((yy / 100) + 49) * 0.75) - 38;
		// 'j' for dates in Julian calendar:
		int j = k1 + k2 + d + 59;
		if (j > 2299160) {
			// For Gregorian calendar:
			j = j - k3; // 'j' is the Julian date at 12h UT (Universal Time)
		}
		double t = (j - 2451550.1) / 29.530588853;
		double phase = t - Math.floor(t);
		double age = phase * 29.53;

		if (age < 1.84566) {
			return MoonPhases.MOON_NEW;
		} else if (age < 5.53699) {
			return MoonPhases.MOON_EVENING_CRESCENT;
		} else if (age < 9.22831) {
			return MoonPhases.MOON_FIRST_QUARTER;
		} else if (age < 12.91963) {
			return MoonPhases.MOON_WAXING_GIBBOUS;
		} else if (age < 16.61096) {
			return MoonPhases.MOON_FULL;
		} else if (age < 20.30228) {
			return MoonPhases.MOON_WANING_GIBBOUS;
		} else if (age < 23.99361) {
			return MoonPhases.MOON_LAST_QUARTER;
		} else if (age < 27.68493) {
			return MoonPhases.MOON_MORNING_CRESCENT;
		} else {
			return MoonPhases.MOON_NEW;
		}
	}
}

// source 
//http://mysite.verizon.net/res148h4j/javascript/script_moon_phase.html#source
// compute moon position and phase
/*
	function moon_posit( Y, M, D )
	{
	    var YY = n0;
	    var MM = n0;
	    var K1 = n0; 
	    var K2 = n0; 
	    var K3 = n0;
	    var JD = n0;
	    var IP = f0;
	    var DP = f0;
	    var NP = f0;
	    var RP = f0;
	    
	    // calculate the Julian date at 12h UT
	    YY = Y - Math.floor( ( 12 - M ) / 10 );       
	    MM = M + 9; 
	    if( MM >= 12 ) MM = MM - 12;
	    
	    K1 = Math.floor( 365.25 * ( YY + 4712 ) );
	    K2 = Math.floor( 30.6 * MM + 0.5 );
	    K3 = Math.floor( Math.floor( ( YY / 100 ) + 49 ) * 0.75 ) - 38;
	    
	    JD = K1 + K2 + D + 59;                  // for dates in Julian calendar
	    if( JD > 2299160 ) JD = JD - K3;        // for Gregorian calendar
	        
	    // calculate moon's age in days
	    IP = normalize( ( JD - 2451550.1 ) / 29.530588853 );
	    AG = IP*29.53;
	    
	    if(      AG <  1.84566 ) Phase = "NEW";
	    else if( AG <  5.53699 ) Phase = "Evening crescent";
	    else if( AG <  9.22831 ) Phase = "First quarter";
	    else if( AG < 12.91963 ) Phase = "Waxing gibbous";
	    else if( AG < 16.61096 ) Phase = "FULL";
	    else if( AG < 20.30228 ) Phase = "Waning gibbous";
	    else if( AG < 23.99361 ) Phase = "Last quarter";
	    else if( AG < 27.68493 ) Phase = "Morning crescent";
	    else                     Phase = "NEW";

	    IP = IP*2*Math.PI;                      // 
	 }
	 
	 // normalize values to range 0...1    
	function normalize( v )
	{
	    v = v - Math.floor( v  ); 
	    if( v < 0 )
	        v = v + 1;
	        
	    return v;
	}
*/
