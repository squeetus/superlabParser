package parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Parser {

	public static void main(String[] args) throws IOException {
		
		Participant p = new Participant();
		
		parseFile(p);
		printData(p);
	}
	
	
	/*
	 * 		Parse the data file
	 */
	public static void parseFile(Participant p) throws IOException {
			
		BufferedReader br = new BufferedReader(new FileReader("Data/p8.txt"));
	    try {
	        parseMetadata(br, p);
	        parseParticipantData(br, p);
	        calculateStatistics(p);
	     	        
	    } finally {
	        br.close();
	    }

	}
	
	
	/*
	 *		Parse the first few lines of the file to pull out important information 
	 */
	public static void parseMetadata(BufferedReader br, Participant p) throws IOException {
		String line;
		String[] splitLine;
		
		//Read ID 
        line = br.readLine();
        p.setId(line);
        
        br.readLine();	//	skip line
        
        //Read Date and Time
        line = br.readLine();
        splitLine = line.split("\t");
        p.setCreated(splitLine[0], splitLine[1]);
        
        skipLine(br,4);	//!!! needs to be 4
        
		splitLine = br.readLine().split("\t");
		p.setGroup(splitLine[0]);
		skipLine(br, 41);
		
		
	}

	
	/*
	 *		Parse the main contents of the file
	 */
	public static void parseParticipantData(BufferedReader br, Participant p) throws IOException {
        String line = "";
		DataLine d;
        int numT, numC;
        numT = numC = 0;
        int correct = 0;
        
		// Process each line
		while ((line = br.readLine()) != null) {
			d = new DataLine(line);
			
			//	Add data line to the participant
            if(d.isTarget()) {
            	p.addTarget(d);
            	if(d.isCorrect())
            		correct++;
            	numT++;
            }
            else {
            	p.addCue(d);
            	numC++;
            }  	
        } 
		
		System.out.println("Parsed " + correct + " correct targets out of " + numT + " targets and " + numC + " cues\n");
	}

	
	/*
	 * 		Call functions to compute statistics about the participant's data
	 */
	public static void calculateStatistics(Participant p) {
		p.calcMeanCorrectRT();
	}
	
	/*
	 * 		Print out relevant Participant data
	 */
	public static void printData(Participant p) {
		p.printInfo();
		
	}
	
	
	/*
	 * 		Skip Lines in buffered reader
	 */
	public static void skipLine(BufferedReader br) throws IOException {
			br.readLine();
	}
	public static void skipLine(BufferedReader br, int toSkip) throws IOException {
		for(int i = 0; i < toSkip; i++)
			br.readLine();

	}
}
