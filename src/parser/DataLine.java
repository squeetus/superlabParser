package parser;

public class DataLine {
	
	/*
	 * 		Parsed Data
	 */
	private Boolean target;			//	Is this line target (True) or cue (False)
	private int block;				//	0 = R/G		1 = Y/B
	private Boolean correct; 		// 	Is this line correct (True) or incorrect (False)
	private int responseTime;		// 	Response time (in ms)
	private int location;			// 	SL/DL: 	0 = same 	1 = different
	private int color;				// 	SC/DC:	0 = same	1 = different
	
	private int expCondition; 		//  SC/SL: 0	SC/DL: 1	DC/DL: 2	DC/SL: 3	NO CUE: 4
	private Boolean aware = false;	//	Cue awareness
	private Boolean falsePositive = false;	//  Responder saw a nonexistent cue
	private Boolean erroneous = false;	// 	Is this line of data valid?
	
	private String line;
	private String[] splitLine;  
	/*
	 * 		Constructor
	 */	
	DataLine(String line) {
		this.line = line;
		parseLine(line);
	}
	
	public void parseLine(String line) {   
        splitLine = line.split("\t");
		
		//	Get color block
		if(splitLine[2].indexOf("Red") >= 0)
			block = 0;
		else 
			block = 1;
		
		//	Get target or cue
			if(splitLine[4].indexOf("target") >= 0)
				target = true;
			else
				target = false;	
			
		// 	Get correct or not
		if(splitLine[9].equals("C"))
			correct = true;
		else {
			correct = false;
			
			//Check to see if the inputs were incorrect
			if( !(target && (splitLine[6].equals("f") || splitLine[6].equals("j"))) && !(!target && ((splitLine[6].equals("1") || splitLine[6].equals("2")) || (splitLine[6].equals("e") || splitLine[6].equals("i")))) ) {
			//if(!(splitLine[6].equals("f") || splitLine[6].equals("j") || splitLine[6].equals("1") || splitLine[6].equals("2"))) {	
					
				if(!target && (splitLine[6].equals("f") || splitLine[6].equals("j")))
					erroneous = true;
				else if(target && (splitLine[6].equals("1") || splitLine[6].equals("2") || splitLine[6].equals("e") || splitLine[6].equals("i")))
					erroneous = true;
				else {
					// Known cases
					if(splitLine[6].equals("k")) {
						if(splitLine[8].equals("j")) 
							correct = true;
					} else if(splitLine[6].equals("g")) {
						if(splitLine[8].equals("f"))
							correct = true;		
					} else if(splitLine[6].equals("r")) {
						if(splitLine[8].equals("1; e")) {
							aware = true;
							correct = true;
							//printLine();
						}
					} else if(splitLine[6].equals("d")) {
						if(splitLine[8].equals("f"))
							correct = true;	
					} else if(splitLine[6].equals("o")) {
						if(splitLine[8].equals("1; e"))
							correct = true;	
					} else {	//For now just print out the offending characters 
						//this.printLine();
						System.out.println("Erroneous cases: " + splitLine[6] + " " + splitLine[8]);
					}
				}
			}
		}
		
		//determine awareness
		if(!target) {
			if(splitLine[5].equals("1") || splitLine[5].equals("e"))
				if(correct) aware = true;
				else 
					falsePositive = true;
			//else if(splitLine[5].equals("2"))
				//aware = false;
				//!! What if no cue and 2?
		}
		
		//	Get response time
		responseTime = Integer.parseInt(splitLine[10]);	
		
		//	Initialize location and color
		location = -1;
		color = -1;
		
		//	Get location and color
		if(splitLine.length == 18) {
			if(splitLine[16].equals("SL"))
				location = 0;
			else if(splitLine[16].equals("DL"))
				location = 1;
			
			if(splitLine[17].equals("SC"))
				color = 0;
			else if(splitLine[17].equals("DC"))
				color = 1;
		} else {
			expCondition = 4;
		}
		
		//	Assign cue conditions
		if(location == color) {
			if(location == 0)
				expCondition = 0;
			if(location == 1)
				expCondition = 2;
		}
		else if(location == 1 && color == 0)
			expCondition = 1;
		else if(location == 0 && color == 1)
			expCondition = 3;
	}
	
	
	/*
	 * 		Get/Set
	 */
	public int getResponseTime() {
		return responseTime;
	}
	
	public int condition() {
		return expCondition;
	}
	
	public Boolean isCorrect() {
		return correct;
	}
	
	public Boolean isTarget() {
		return target;
	}
	
	public Boolean isAware() {
		return aware;
	}
	
	public Boolean isErroneous() {
		return erroneous;
	}
	
	public Boolean isFalseAlert() {
		return falsePositive;
	}
	
	/*
	 * 		Methods
	 */
	public void printLine() {
		//System.out.println("block: " + block + "\t" + "target: " + target + "\t" + "correct: " + correct + "\t" + "responseTime: " + responseTime + "\t" + "location: " + location + "\t" + "color: " + color);
		System.out.println(line);
		//System.out.println(splitLine[0] + "\t" + splitLine[1] + "\tPressed: " + splitLine[6] + "\tCorrect: " + splitLine[8]);
	}
	
	public void printLineFew() {
		System.out.println(splitLine[0] + "\t" + splitLine[1] + "\tPressed: " + splitLine[6] + "\tCorrect: " + splitLine[8]);
	}
	
}
