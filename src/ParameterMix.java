 public enum ParameterMix {     
    ONE    	("11 12 5 1 3 3 3822 0.24 0.04 0.96"),  
    TWO    	("12 10 1 3 3 1 2644 0.11 0.09 0.92"),
    THREE	("12 10 4 3 6 2 1304 0.10 0.03 0.90"),
    FOUR	("14 10 5 5 6 2 315 0.08 0.05 0.90"),
    FIVE	("15 14 9 16 7 10 4007 0.02 0.10 0.84"),
    SIX		("15 15 9 10 9 9 7125 0.01 0.20 0.77"),
    SEVEN	("15 15 10 13 8 10 5328 0.04 0.18 0.80"),
    EIGHT	("16 14 15 12 9 5 8840 0.04 0.19 0.76");

    private final String params;     

    ParameterMix( String _params ){
        this.params = _params;
    }

    public String getParams() {
        return params;
    }       

    public static String getParamsFromID(int i) {
    	switch (i) {
			case 1: return ONE.getParams();
			case 2: return TWO.getParams();
			case 3: return THREE.getParams();
			case 4: return FOUR.getParams();
			case 5: return FIVE.getParams();
			case 6: return SIX.getParams();
			case 7: return SEVEN.getParams();
			case 8: return EIGHT.getParams();
    	}
    	return "Invalid ENUM";
    }
};  
