package parser;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Participant {
	private String id;					//	User ID
	private String group;				// 	Group ID
	private String created;				//	DateTime participant file was created
	private String parsed;				// 	DateTime the file was parsed
	private int trimThreshold = 2000;	// 	Threshold for trimming target responses larger than 2.5 Standard Deviations
	
	private DataLine[] targets;			// 	Responses for targets
	private DataLine[] cues;			//	Responses for cues
	private DataLine[] errors;			// 	Erroneous data lines
	private int tCount = 0;					// 	Count of valid Target responses
	private int cCount = 0;					// 	Count of valid Cue responses
	private int eCount = 0;					// 	Count of erroneous lines of data
	
	private double[] meanAwareRT;		// 	Average CORRECT trimmed Response Time
	private double[] meanUnawareRT;		// 	Average CORRECT trimmed Response Time
	private double percentCorrect; 		//	% of correct trials
	private int ssCount_u, ssCount_a, sdCount_u, sdCount_a, dsCount_u, dsCount_a, numNoCueCorrect, ddCount_u, ddCount_a, ncCount, numFalseAlert, fpCount, numIncorrect, meanFalsePositiveRT, meanNoCueRT, numCorrect, trimCount, falsePositiveCount, numNoCue, numAware, numNotAware;
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	
	/* Constructor */
	Participant() {
		parsed = LocalDateTime.now().format(formatter);
		
		targets = new DataLine[400];
		cues = new DataLine[400];
		errors = new DataLine[10];
		
		meanAwareRT = new double[4];
		meanUnawareRT = new double[4];
	}
	
	/* Get/Set */
	public String getCreated() {
		return created;
	}
	
	public String getParsed() {
		return parsed;
	}
	
	public String getGroup() {
		return group;
	}
	
	public String getId() {
		return id;
	}
	
	public void setCreated(String date, String time) {
		String dt = date + " " + time;
		created = dt;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setGroup(String grp) {
		this.group = grp;
	}
	
	public void addTarget(DataLine t) {
		targets[tCount] = t;
		tCount++;
	}
	
	public void addCue(DataLine c) {
		cues[cCount] = c;
		cCount++;
	}
	
	public void addError(DataLine e) {
		errors[eCount] = e;
		eCount++;
	}
	
	/* Calculation Methods */
	public void calcMeanCorrectRT() {		
		ssCount_u = sdCount_u = dsCount_u = ddCount_u = ssCount_a = sdCount_a = dsCount_u = ddCount_a = ncCount = fpCount = numFalseAlert = numIncorrect = numNoCueCorrect = meanFalsePositiveRT = meanNoCueRT = numCorrect = falsePositiveCount = numNoCue = numAware = numNotAware = trimCount = 0;
		int RT;
		Boolean aware, unaware;
		unaware = false;
		
		//int numWithinThreshold = 0;
		
		int numErroneous = 0;

		// Read each target response line
		for(int i = 0; i < tCount; i++) {
			aware = cues[i].isAware();
			if(cues[i].isFalseAlert()) {
				falsePositiveCount++;
				//cues[i].printLine();
			}
			if(cues[i].condition() == 4)
				numNoCue++;
							
			// If the response is correct, add it to the sum and increment count
			//  SC/SL: 0	SC/DL: 1	DC/DL: 2	DC/SL: 3
			//	SS			SD			DD			DS
			
			if(targets[i].isCorrect()) {//&& cues[i].isCorrect()
				//targets[i].printLine();
				RT = targets[i].getResponseTime();
				//System.out.println(RT);
				if(RT <= trimThreshold) {		
				
					numCorrect++;
					
					if(aware) {
						numAware++;
						//cues[i].printLine();
					}
					else if(!cues[i].isFalseAlert() && !cues[i].isErroneous() && !cues[i].isCorrect()) {
						unaware = true;
						numNotAware++;
						//cues[i].printLine();
					} else if(cues[i].isFalseAlert()) {
						//falsePositiveCount++;
						numFalseAlert++;
						//cues[i].printLine();
					}
					else if(cues[i].isErroneous()) {
						numErroneous++;
						//cues[i].printLine();
					} else {
						numNoCueCorrect++;
					}
					
				//RT = targets[i].getResponseTime();
				//System.out.println(RT);
				//if(RT <= trimThreshold) {		
					//numWithinThreshold++;
					
					switch(targets[i].condition()) {
					case 0:	// Same color, same location
						if(aware) {
							meanAwareRT[0] += RT;
							ssCount_a++;
						} else if(unaware) {
							meanUnawareRT[0] += RT;
							ssCount_u++;
						} 
						break;
					case 1: // Same color, different location
						if(aware) {
							meanAwareRT[1] += RT;
							sdCount_a++;
						} else if(unaware) {
							meanUnawareRT[1] += RT;
							sdCount_u++;
						}
						break;
					case 2: // Different color, different location
						if(aware) {
							meanAwareRT[2] += RT;
							ddCount_a++;
						} else if(unaware) {
							meanUnawareRT[2] += RT;
							ddCount_u++;
						}
						break;
					case 3: // Different color, same location
						if(aware) {
							meanAwareRT[3] += RT;
							dsCount_a++;
						} else if(unaware) {
							meanUnawareRT[3] += RT;
							dsCount_u++;
						}
						break;
					case 4:	// No cue
						if(!cues[i].isFalseAlert() && !cues[i].isErroneous()) {
							meanNoCueRT += RT;
							ncCount++;
						} else if(!cues[i].isErroneous()){
							meanFalsePositiveRT += RT;
							fpCount++;
						} else {
							System.out.print("Erroneous input (check for validity): ");
							//cues[i].printLine();
							cues[i].printLineFew();
						}
						break;
					default:
						System.out.println(RT);
						break;
					}
				} else {
					trimCount++;
				}
			} else {
				numIncorrect++;
			}
		}
		
		if(ssCount_a > 0)
			meanAwareRT[0] /= ssCount_a;
		if(sdCount_a > 0)
			meanAwareRT[1] /= sdCount_a;
		if(ddCount_a > 0)
			meanAwareRT[2] /= ddCount_a;
		if(dsCount_a > 0)
			meanAwareRT[3] /= dsCount_a;
		
		if(ssCount_u > 0)
			meanUnawareRT[0] /= ssCount_u;
		if(sdCount_u > 0)
			meanUnawareRT[1] /= sdCount_u;
		if(ddCount_u > 0)
			meanUnawareRT[2] /= ddCount_u;
		if(dsCount_u > 0)
			meanUnawareRT[3] /= dsCount_u;
		
		if(ncCount > 0)
			meanNoCueRT /= ncCount;
		
		if(fpCount > 0)
			meanFalsePositiveRT /= fpCount;
		
		// Validate number of aware and unaware trials used
		System.out.println();
		System.out.print("Aware count matches?\t" + (ssCount_a + sdCount_a + ddCount_a + dsCount_a) + "\t" + numAware);
		if((ssCount_a + sdCount_a + ddCount_a + dsCount_a) == numAware)
			System.out.print("\tyes!\n");
		else
			System.out.println("\tno\n");
		System.out.print("Unaware count matches?\t" + (ssCount_u + sdCount_u + ddCount_u + dsCount_u) + "\t" + numNotAware);
		if((ssCount_u + sdCount_u + ddCount_u + dsCount_u) == numNotAware)
			System.out.print("\tyes!\n");
		else
			System.out.print("\tno\n");
		
		//System.out.println(temp);
		
		//System.out.println(ncCount);
		//System.out.println(ssCount_u);// + sdCount_a + ddCount_a + dsCount_a);
		//System.out.println(sdCount_u);// + sdCount_u + ddCount_u + dsCount_u);
		//System.out.println(ddCount_u);
		//System.out.println(dsCount_u);
		
		//System.out.println(numNoCue);
		
		//System.out.println(numWithinThreshold);
		System.out.println("\nNo Cue correct responses:\t" + numNoCueCorrect);
		System.out.println("False Alerts:\t\t\t" + numFalseAlert);
		System.out.println("Erroneous keypresses:\t\t" + numErroneous);
		System.out.println("correct trims:\t\t\t" + trimCount);
		System.out.print("\nSum matches?:\t" + (numAware + numNotAware + numFalseAlert + numErroneous + numNoCueCorrect + trimCount) + "\t" + (numCorrect + trimCount));
		if((numAware + numNotAware + numFalseAlert + numErroneous + numNoCueCorrect + trimCount) == (numCorrect + trimCount))
			System.out.print("\tyes!\n");
		else
			System.out.print("\tno\n");
	}
	
	public void printInfo() {
		DecimalFormat f = new DecimalFormat( "####.##" );
		
		System.out.println("\n------------------------\nParticipant Information\n");
		System.out.println("ID:\t\t" + id);
		System.out.println("Group:\t\t" + group);
		System.out.println("Created:\t" + created);
		System.out.println("Parsed:\t\t" + parsed);
		System.out.println("\nCorrect Lines trimmed (threshold is " + trimThreshold + "ms):\t" + trimCount);
		
		//System.out.println("\n" + numCorrect + " correct responses");
		
		
		System.out.println("\nMean Correct Response times (ms):\n\t\t\tSS\t\t\tSD\t\t\tDD\t\t\tDS");
		System.out.println("Aware (" + numAware +"):\t\t" + Double.valueOf(f.format(meanAwareRT[0])) + " (" + ssCount_a +")\t\t" + Double.valueOf(f.format(meanAwareRT[1])) + " (" + sdCount_a +")\t\t" + Double.valueOf(f.format(meanAwareRT[2]))+ " (" + ddCount_a +")\t\t" + Double.valueOf(f.format(meanAwareRT[3])) + " (" + dsCount_a +")");
		System.out.println("Unaware (" + numNotAware +"):\t\t" + Double.valueOf(f.format(meanUnawareRT[0])) + " (" + ssCount_u +")\t\t" + Double.valueOf(f.format(meanUnawareRT[1])) + " (" + sdCount_u +")\t\t" + Double.valueOf(f.format(meanUnawareRT[2])) + " (" + ddCount_u +")\t\t" + Double.valueOf(f.format(meanUnawareRT[3])) + " (" + dsCount_u +")");
		System.out.println("No Cue:\t\t" + f.format(meanNoCueRT) + " (" + numNoCueCorrect +")");
		System.out.println("False Positive:\t" + f.format(meanFalsePositiveRT) + " (" + numFalseAlert +")");
		
		System.out.println("\nUnaware:\t\t\t" + f.format((numNotAware/Double.valueOf(numCorrect))*100) + "%\t(" + numNotAware + "/" + numCorrect + ")");
		System.out.println("False positives:\t\t" + f.format((falsePositiveCount/Double.valueOf(numNoCue))*100) + "%\t("+falsePositiveCount+ "/" + numNoCue + ")");
		System.out.println("Trimmed Target Accuracy:\t" + f.format((numCorrect/Double.valueOf(targets.length))*100) + "%\t("+numCorrect+ "/" + targets.length + ")");
		System.out.println("Overall Target Accuracy:\t" + f.format(((trimCount + numCorrect)/Double.valueOf(targets.length))*100) + "%\t("+(numCorrect + trimCount) + "/" + targets.length +")");
		System.out.println("Incorrect responses:\t\t" + f.format((numIncorrect/Double.valueOf(targets.length))*100) + "%\t("+numIncorrect+ "/" + targets.length + ")");
	}
	
}
