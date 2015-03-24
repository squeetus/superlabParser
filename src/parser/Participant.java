package parser;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.math.RoundingMode;

public class Participant {
	private String id;					//	User ID
	private String group;				// 	Group ID
	private String created;				//	DateTime participant file was created
	private String parsed;				// 	DateTime the file was parsed
	private double trimThreshold = 2000.0;	// 	Threshold for trimming target responses larger than 2.5 Standard Deviations
	
	private DataLine[] targets;			// 	Responses for targets
	private DataLine[] cues;			//	Responses for cues
	private DataLine[] errors;			// 	Erroneous data lines
	private int tCount = 0;					// 	Count of valid Target responses
	private int cCount = 0;					// 	Count of valid Cue responses
	private int eCount = 0;					// 	Count of erroneous lines of data
	
	private double[] meanAwareRT;		// 	Average CORRECT trimmed Response Time
	private double[] meanUnawareRT;		// 	Average CORRECT trimmed Response Time
	private double percentCorrect; 		//	% of correct trials
	
	private Double[] proportionWrong;
	private int ssCount_u, ssCount_a, sdCount_u, sdCount_a, dsCount_u, dsCount_a, totalNumAware, totalNumNotAware, numNoCueCorrect, ddCount_u, ddCount_a, ncCount, numFalseAlert, fpCount, numIncorrect, meanFalsePositiveRT, meanNoCueRT, numCorrect, trimCount, falsePositiveCount, numNoCue, numAware, numNotAware;
	//private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	
	/* Constructor */
	Participant() {
		//parsed = LocalDateTime.now().format(formatter);
		
		targets = new DataLine[400];
		cues = new DataLine[400];
		errors = new DataLine[10];
		
		meanAwareRT = new double[4];
		meanUnawareRT = new double[4];
		proportionWrong = new Double[9];
		for(int i = 0; i < 9; i++)
			proportionWrong[i] = 0.0;
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
	
	// Return the standard deviation of response times
	public double getStandardDeviation(double mean) {
		double sd = 0;
		float numerator = 0;
		
		for(int i=0; i<targets.length; i++) {
			numerator += ((targets[i].getResponseTime() - mean) * (targets[i].getResponseTime() - mean));
		}
		sd = Math.sqrt(numerator/(targets.length-1));
		System.out.println("Standard dev: " + sd);
		return sd;
	}
	
	// Determine the threshold for trimming target responses
	public double getTrimThreshold() {
		DecimalFormat format = new DecimalFormat();
	    format.setRoundingMode(RoundingMode.valueOf(3));
	    
		double meanRT = 0;
		int sum = 0;
		for(int i=0; i<targets.length; i++) {
			sum += targets[i].getResponseTime();
		}
		meanRT = sum/targets.length;
		//System.out.println(meanRT + "\n" + sum + "\n" + targets.length);
		
		return meanRT + 2.5 * getStandardDeviation(meanRT);
	}
	
	/* Calculation Methods */
	public void calcMeanCorrectRT() {		
		trimThreshold = getTrimThreshold();
		System.out.println("trim threshold: " + trimThreshold);
		
		ssCount_u = sdCount_u = dsCount_u = ddCount_u = ssCount_a = sdCount_a = dsCount_u = ddCount_a = totalNumAware = totalNumNotAware = ncCount = fpCount = numFalseAlert = numIncorrect = numNoCueCorrect = meanFalsePositiveRT = meanNoCueRT = numCorrect = falsePositiveCount = numNoCue = numAware = numNotAware = trimCount = 0;
		int RT;
		Boolean aware, unaware, validWrong;
		Integer[] wrongCount = new Integer[9]; //SSA(E), SSU(E), SDA(E), SDU(E)...
		Integer[] trimCategoryCount = new Integer[9];
		Integer[] categoryCount = new Integer[9];
		int totalWrong = 0;
		int erroneous = 0;
		int incorrectErroneous = 0;
		int trimErroneous = 0;
		int temp = 0;
		
		for(int i = 0; i<9; i++) {
			wrongCount[i] = 0;
			categoryCount[i] = 0;
			trimCategoryCount[i] = 0;
		}
		
		unaware = false;
		validWrong = false;
		
		//int numWithinThreshold = 0;
		
		int numErroneous = 0;

		// Read each target response line
		for(int i = 0; i < tCount; i++) {
			aware = cues[i].isAware();
			unaware = false;
			validWrong = false;
			
			if(aware)
				totalNumAware++;
			
			if(cues[i].isFalseAlert()) {
				falsePositiveCount++;
				//cues[i].printLine();
			}
			if(cues[i].condition() == 4)
				numNoCue++;
			
			if(!cues[i].isErroneous() && !targets[i].isErroneous()) {
				validWrong = true;
				
			}
							
			// If the response is correct, add it to the sum and increment count
			//  SC/SL: 0	SC/DL: 1	DC/DL: 2	DC/SL: 3
			//	SS			SD			DD			DS
			if(!cues[i].isFalseAlert() && !cues[i].isErroneous() && !cues[i].isCorrect()) 
				totalNumNotAware++;
				
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
						//System.out.println(numNotAware);
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
							//cues[i].printLine();
						}  else 
							erroneous++;
						break;
					case 1: // Same color, different location
						if(aware) {
							meanAwareRT[1] += RT;
							sdCount_a++;
						} else if(unaware) {
							meanUnawareRT[1] += RT;
							sdCount_u++;
							//cues[i].printLine();
						} else 
							erroneous++;
						break;
					case 2: // Different color, different location
						if(aware) {
							meanAwareRT[2] += RT;
							ddCount_a++;
						} else if(unaware) {
							meanUnawareRT[2] += RT;
							ddCount_u++;
							//cues[i].printLine();
							//System.out.println(unaware);
							//System.out.println(cues[i].isErroneous());
						} else 
							erroneous++;
						break;
					case 3: // Different color, same location
						if(aware) {
							meanAwareRT[3] += RT;
							dsCount_a++;
						} else if(unaware) {
							meanUnawareRT[3] += RT;
							dsCount_u++;
						} else 
							erroneous++;
						break;
					case 4:	// No cue
						
						if(!cues[i].isFalseAlert() && !cues[i].isErroneous()) {
							meanNoCueRT += RT;
							ncCount++;
						} else if(!cues[i].isErroneous()){
							meanFalsePositiveRT += RT;
							fpCount++;
						} else {
							//System.out.print("Erroneous input (check for validity): ");
							//cues[i].printLine();
							//System.out.println(cues[i].isErroneous());
							//cues[i].printLineFew();
							erroneous++;
						}
						break;
					default:
						System.out.println(RT);
						break;
					}
				} else {
					trimCount++;
					
					//Classify the trimmed lines by the 8 target options
					if(aware) {
						
					}
					else if(!cues[i].isFalseAlert() && !cues[i].isErroneous() && !cues[i].isCorrect()) 
						unaware = true;
					else if(cues[i].isErroneous()) {
						//cues[i].printLine();
						//trimErroneous++;
					} //else
						//cues[i].printLine();
						
					
					//Classify the target by cue condition and update the proportion correctly
					switch(targets[i].condition()) {
					case 0:	// Same color, same location
						if(aware) {
							trimCategoryCount[0]++;
							categoryCount[0]++;
						} else if(unaware) {
							trimCategoryCount[1]++;
							categoryCount[1]++;
						} else
							trimErroneous++;
						break;
					case 1: // Same color, different location
						if(aware) {
							trimCategoryCount[2]++;
							categoryCount[2]++;
						} else if(unaware) {
							trimCategoryCount[3]++;
							categoryCount[3]++;
						} else
							trimErroneous++;
						break;
					case 2: // Different color, different location
						if(aware) {
							trimCategoryCount[4]++;
							categoryCount[4]++;
						} else if(unaware) {
							trimCategoryCount[5]++;
							categoryCount[5]++;
						}else
							trimErroneous++;
						break;
					case 3: // Different color, same location
						if(aware) {
							trimCategoryCount[6]++;
							categoryCount[6]++;
						} else if(unaware) {
							trimCategoryCount[7]++;
							categoryCount[7]++;
						} else
							trimErroneous++;
						break;
					case 4:	// No cue
						trimCategoryCount[8]++;
						categoryCount[8]++;
						break;
					default:
						break;
					}
				}
			} else {
				numIncorrect++;
				
				//Calculate the number of incorrect (not erroneous input) responses for the 8 target options
				if(validWrong) { // Only consider target responses where the input was valid
					//targets[i].printLine();
					//cues[i].printLine();
					//System.out.println();
					
					totalWrong++;
					
					if(aware) {
					
					}
					else if(!cues[i].isFalseAlert() && !cues[i].isErroneous() && !cues[i].isCorrect()) 
						unaware = true;
						
					
					//Classify the target by cue condition and update the proportion correctly
					switch(targets[i].condition()) {
					case 0:	// Same color, same location
						if(aware) {
							wrongCount[0]++;
							categoryCount[0]++;
						} else if(unaware) {
							wrongCount[1]++;
							categoryCount[1]++;
						} else {
							incorrectErroneous++;
							//cues[i].printLine();
						}
						break;
					case 1: // Same color, different location
						if(aware) {
							wrongCount[2]++;
							categoryCount[2]++;
						} else if(unaware) {
							wrongCount[3]++;
							categoryCount[3]++;
						} else {
							incorrectErroneous++;
							//cues[i].printLine();
						}
						break;
					case 2: // Different color, different location
						if(aware) {
							wrongCount[4]++;
							categoryCount[4]++;
						} else if(unaware) {
							wrongCount[5]++;
							categoryCount[5]++;
						} else{
							incorrectErroneous++;
							//cues[i].printLine();
						}
						break;
					case 3: // Different color, same location
						if(aware) {
							wrongCount[6]++;
							categoryCount[6]++;
						} else if(unaware) {
							wrongCount[7]++;
							categoryCount[7]++;
						} else{
							incorrectErroneous++;
							//cues[i].printLine();
						}
						break;
					case 4:	// No cue
						wrongCount[8]++;
						categoryCount[8]++;
						break;
					default:
						break;
					}
				} else
					incorrectErroneous++;
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
		
		categoryCount[0] += ssCount_a;
		categoryCount[1] += ssCount_u;
		categoryCount[2] += sdCount_a;
		categoryCount[3] += sdCount_u;
		categoryCount[4] += ddCount_a;
		categoryCount[5] += ddCount_u;
		categoryCount[6] += dsCount_a;
		categoryCount[7] += dsCount_u;
		categoryCount[8] += (fpCount + ncCount);
		
		for(int i = 0; i < 9; i++) {
			if(categoryCount[i] != 0)
				proportionWrong[i] =  Double.valueOf(wrongCount[i])/Double.valueOf(categoryCount[i]);
				//proportionWrong[i] =  Double.valueOf(wrongCount[i])/80.0;
		
			//wrongCount should never be > 0 if categoryCount is 0
		}
		
		
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
		
		//System.out.println("\nTotal number valid & incorrect: " + totalWrong);
		System.out.print("\nIncorrect (" + numIncorrect + ") by category:\t");
		
		int summ = 0;
		for(int i = 0; i<9; i++) {
			System.out.print(" " + wrongCount[i]);
			summ += wrongCount[i];
		}
		summ += incorrectErroneous;
		System.out.print(" (erroneous): " + incorrectErroneous);
		System.out.println(" = " + (summ));
		
		
		System.out.print("Trimmed by category:\t");
		
		summ = 0;
		for(int i = 0; i<9; i++) {
			System.out.print(" " + trimCategoryCount[i]);
			summ += trimCategoryCount[i];
		}
		System.out.print(" (erroneous cue): " + trimErroneous);
		summ += trimErroneous;
		System.out.println(" = " + (summ));

		
		summ = 0;
		System.out.println("\nCategory count: ");
		for(int i = 0; i<9; i++) {
			System.out.print(" " + categoryCount[i]);
			summ += categoryCount[i];
			//System.out.println(summ);
		}
		//System.out.println(erroneous+"\n"+incorrectErroneous+"\n"+trimErroneous);
		System.out.print(" (erroneous): " + (erroneous + incorrectErroneous + trimErroneous));
		summ += erroneous + incorrectErroneous + trimErroneous;
		System.out.println(" = " + (summ));
		
		System.out.println("\nProportion wrong: ");
		for(int i = 0; i<9; i++) {
			System.out.print(" " + proportionWrong[i]);
		}
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
		
		System.out.println("\nUnaware:\t\t\t" + f.format((totalNumNotAware/Double.valueOf(targets.length))*100) + "%\t(" + totalNumNotAware + "/" + targets.length + ")");
		System.out.println("False positives:\t\t" + f.format((falsePositiveCount/Double.valueOf(numNoCue))*100) + "%\t("+falsePositiveCount+ "/" + numNoCue + ")");
		System.out.println("Trimmed Target Accuracy:\t" + f.format((numCorrect/Double.valueOf(targets.length))*100) + "%\t("+numCorrect+ "/" + targets.length + ")");
		System.out.println("Overall Target Accuracy:\t" + f.format(((trimCount + numCorrect)/Double.valueOf(targets.length))*100) + "%\t("+(numCorrect + trimCount) + "/" + targets.length +")");
		System.out.println("Incorrect responses:\t\t" + f.format((numIncorrect/Double.valueOf(targets.length))*100) + "%\t("+numIncorrect+ "/" + targets.length + ")");
	}
	
	public void printInfoToFile() {
		Writer writer = null;
		DecimalFormat f = new DecimalFormat( "####.##" );
		
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("Output/Console/" + id + "Output.txt"), "utf-8"));

			 writer.write("\n------------------------\nParticipant Information\n");
			 writer.write("\nID:\t\t" + id);
			 writer.write("\nGroup:\t\t" + group);
			 writer.write("\nCreated:\t" + created);
			 writer.write("\nParsed:\t\t" + parsed);
			 writer.write("\n\nCorrect Lines trimmed (threshold is " + trimThreshold + "ms):\t" + trimCount);
			
			// writer.write("\n" + numCorrect + " correct responses");
			
			
			 writer.write("\n\nMean Correct Response times (ms):\n\t\t\tSS\t\t\tSD\t\t\tDD\t\t\tDS");
			 writer.write("\nAware (" + numAware +"):\t\t" + Double.valueOf(f.format(meanAwareRT[0])) + " (" + ssCount_a +")\t\t" + Double.valueOf(f.format(meanAwareRT[1])) + " (" + sdCount_a +")\t\t" + Double.valueOf(f.format(meanAwareRT[2]))+ " (" + ddCount_a +")\t\t" + Double.valueOf(f.format(meanAwareRT[3])) + " (" + dsCount_a +")");
			 writer.write("\nUnaware (" + numNotAware +"):\t\t" + Double.valueOf(f.format(meanUnawareRT[0])) + " (" + ssCount_u +")\t\t" + Double.valueOf(f.format(meanUnawareRT[1])) + " (" + sdCount_u +")\t\t" + Double.valueOf(f.format(meanUnawareRT[2])) + " (" + ddCount_u +")\t\t" + Double.valueOf(f.format(meanUnawareRT[3])) + " (" + dsCount_u +")");
			 writer.write("\nNo Cue:\t\t" + f.format(meanNoCueRT) + " (" + numNoCueCorrect +")");
			 writer.write("\nFalse Positive:\t" + f.format(meanFalsePositiveRT) + " (" + numFalseAlert +")");
			
			 writer.write("\n\nUnaware:\t\t\t" + f.format((totalNumNotAware/Double.valueOf(targets.length))*100) + "%\t(" + totalNumNotAware + "/" + targets.length + ")");
			 writer.write("\nFalse positives:\t\t" + f.format((falsePositiveCount/Double.valueOf(numNoCue))*100) + "%\t("+falsePositiveCount+ "/" + numNoCue + ")");
			 writer.write("\nTrimmed Target Accuracy:\t" + f.format((numCorrect/Double.valueOf(targets.length))*100) + "%\t("+numCorrect+ "/" + targets.length + ")");
			 writer.write("\nOverall Target Accuracy:\t" + f.format(((trimCount + numCorrect)/Double.valueOf(targets.length))*100) + "%\t("+(numCorrect + trimCount) + "/" + targets.length +")");
			 writer.write("\nIncorrect responses:\t\t" + f.format((numIncorrect/Double.valueOf(targets.length))*100) + "%\t("+numIncorrect+ "/" + targets.length + ")");	    
		    
		} catch (IOException ex) {
			System.out.println("Error creating and writing to file: " + ex);
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
		
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("Output/Data/" + id + "Data.txt"), "utf-8"));
		    String tab = "\t";
		    
		    //8 mean correct trimmed reaction times for target
		    writer.write(f.format(meanAwareRT[0]) + tab);
		    writer.write(f.format(meanUnawareRT[0]) + tab);
		    writer.write(f.format(meanAwareRT[1]) + tab);
		    writer.write(f.format(meanUnawareRT[1]) + tab);
		    writer.write(f.format(meanAwareRT[2]) + tab);
		    writer.write(f.format(meanUnawareRT[2]) + tab);
		    writer.write(f.format(meanAwareRT[3]) + tab);
		    writer.write(f.format(meanUnawareRT[3]) + tab);
		    
		    //8 proportions of errors
		    for(int i = 0; i<8; i++) {
		    	writer.write(f.format(proportionWrong[i]) + tab);
		    }
		    
		    // NC response
		    writer.write(f.format(meanNoCueRT) + tab);
		    
		    // FP proportion
		    writer.write(f.format(falsePositiveCount/Double.valueOf(numNoCue)) + tab);
		    
		    // Trimmed proportion
		    writer.write(f.format(trimCount/400.0) + tab);
		    
		    // Aware proportion
		    writer.write(f.format(totalNumAware/320.0) + tab);
		    
		    // Unaware proportion
		   //writer.write(f.format(totalNumNotAware/320.0)  + tab);
		  // System.out.println(totalNumAware);
		   //System.out.println(totalNumNotAware);
		} catch (IOException ex) {
			System.out.println("Error creating and writing to file: " + ex);
		} finally {
			try {writer.close();} catch (Exception ex) {}
		}
	}
	
}
