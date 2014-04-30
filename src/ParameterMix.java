 public enum ParameterMix {     
    ONE    ("11 12 5 1 3 3 3822 0.24 0.04 0.96"),  
    TWO    ("12 10 1 3 3 1 2644 0.11 0.09 0.92");

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
    	}
    	return "Invalid ENUM";
    }
};  
