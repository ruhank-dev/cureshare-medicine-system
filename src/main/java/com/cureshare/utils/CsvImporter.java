package com.cureshare.utils;

import com.cureshare.models.Medicine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CsvImporter {

	public static void importFromResources() {

	    try {

	        InputStream is = CsvImporter.class.getResourceAsStream("/medicines.csv");

	        if (is == null) {
	            System.out.println("CSV file not found in resources!");
	            return;
	        }

	        BufferedReader br = new BufferedReader(new InputStreamReader(is));

	        String line;

	        while ((line = br.readLine()) != null) {

	            if (line.trim().isEmpty()) continue;

	            String[] parts = line.split(",");

	            if (parts.length < 5) continue;

	            String name = parts[0].trim();
	            String category = parts[1].trim();
	            String batch = parts[2].trim();
	            LocalDate expiry = LocalDate.parse(parts[3].trim());
	            int quantity = Integer.parseInt(parts[4].trim());

	            Medicine m = new Medicine(
	                    null,
	                    name,
	                    category,
	                    batch,
	                    expiry,
	                    quantity,
	                    "Bulk Upload"
	            );

	            m.setStatus(Medicine.Status.PENDING);

	            DataStore.getInstance().addMedicine(m);
	        }

	        br.close();

	        System.out.println("Bulk upload completed.");

	    } catch (Exception e) {
	        System.out.println("Error importing CSV: " + e.getMessage());
	    }
	}
}