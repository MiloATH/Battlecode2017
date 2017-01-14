package rush.Helpers;

public enum CreationStatus {
	  
	
		CREATEDNOTHING("Has created nothing"),
		PLANTEDTREE("PLANTEDTREE"),
		MADESOLDIER1("Made first Solder"),
		MADESOLDIER2("Made second Solder"),
		MADESOLDIER3("Made third Solder"),
		MADETANK("Made Tank"),
		MADESCOUT1("Made first scout"),
		MADESCOUT2("Made second scout"),
		MADELUMBERJACK("Made lumberjack"),
		MADEGARDENER("Made gardener");
		
		
		
	    private final String type; 
	    CreationStatus(String typeNew) {
	        this.type = typeNew;
	    }
	
	
}
