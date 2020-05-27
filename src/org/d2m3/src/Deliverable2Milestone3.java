package org.d2m3.src;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.utils.D2Utils;
import org.utils.D3M3Utils;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Deliverable2Milestone3 {

	private static final String TRAINING = "_training.arff";
	private static final String TESTING = "_testing.arff";

	public static void main(String[] args) throws Throwable{

		// Declare the list of the dataset names
		String[] projects = {"AVRO", "BOOKKEEPER"};

		// Declare the number of revision for each dataset
		Integer[] limits = {15, 7};

		// Open the FileWriter for the output file
		try (FileWriter csvWriter = new FileWriter("output/outputut_D2M3.csv")) {

			// Append the first line of the result file
			csvWriter.append("Dataset,# Training,% Training,% Defect Training,%Defect Testing,Classifier,Balancing,FeatureSelection,TP,FP,TN,FN,Precision,Recall,ROC Area,Kappa\n");	

			// For each project...
			for (int j = 0; j < projects.length; j++) {

				// Iterate over the single version for the WalkForward technique...
				for (int i = 1; i < limits[j]; i++) {

					// Create the ARFF file for the training, till the i-th version
					List<Integer> resultTraining = D2Utils.walkForwardTraining(projects[j], i);

					// Create the ARFF file for testing, with the i+1 version
					List<Integer> resultTesting = D2Utils.walkForwardTesting(projects[j], i+1);

					double percentTraining = resultTraining.get(0) / (double)(resultTraining.get(0) + resultTesting.get(0));
					double percentDefectTraining = resultTraining.get(1) / (double)resultTraining.get(0);
					double percentDefectTesting = resultTesting.get(1) / (double)resultTesting.get(0);
					double percentageMajorityClass = 1 - ( (resultTraining.get(1) + resultTesting.get(1)) / (double)(resultTraining.get(0) + resultTesting.get(0)));

					DataSource source2 = new DataSource(projects[j] + TRAINING);
					Instances testingNoFilter = source2.getDataSet();
					DataSource source = new DataSource(projects[j] + TESTING);
					Instances noFilterTraining = source.getDataSet();

					List<String> samplingResult = D3M3Utils.applySampling(noFilterTraining, testingNoFilter, percentageMajorityClass, "False");
					for (String result : samplingResult) {
						csvWriter.append(projects[j] + "," + i  + "," + percentTraining  + "," + percentDefectTraining  + "," + percentDefectTesting +"," + result);
					}

					List<String> featureSelectionResult = D3M3Utils.applyFeatureSelection(noFilterTraining, testingNoFilter, percentageMajorityClass);
					for (String result : featureSelectionResult) {
						csvWriter.append(projects[j] + "," + i  + "," + percentTraining  + "," + percentDefectTraining  + "," + percentDefectTesting +"," + result);
					}	
					

				}
				// Delete the temp file
				Files.deleteIfExists(Paths.get(projects[j] + TESTING));
				Files.deleteIfExists(Paths.get(projects[j] + TRAINING));
			}
			
			csvWriter.flush();
		}
	}
}