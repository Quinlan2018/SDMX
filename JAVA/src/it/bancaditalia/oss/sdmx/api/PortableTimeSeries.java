/* Copyright 2010,2014 Bank Of Italy
*
* Licensed under the EUPL, Version 1.1 or - as soon they
* will be approved by the European Commission - subsequent
* versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the
* Licence.
* You may obtain a copy of the Licence at:
*
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in
* writing, software distributed under the Licence is
* distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied.
* See the Licence for the specific language governing
* permissions and limitations under the Licence.
*/
package it.bancaditalia.oss.sdmx.api;

import it.bancaditalia.oss.sdmx.exceptions.DataStructureException;
import it.bancaditalia.oss.sdmx.util.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is a Java container for a Time Series. It will be transformed by a converter in the various 
 * statistical packages into a native time series object.
 * 
 * @author Attilio Mattiocco
 *
 */

public class PortableTimeSeries {
	
	protected static Logger logger = Configuration.getSdmxLogger();

	private String frequency = null;
	private String dataflow = null;
	
	private List<String> attributes = null;
	//note that the dimensions have to be ordered as prescribed by the DSD
	private List<String> dimensions = null;
	private List<String> timeSlots = null;
	private List<Double> observations = null;
	//left here for backward compatibility
	private List<String> status = null;
	private Hashtable<String, ArrayList<String>> obsLevelAttributes = null;

	private String name;
	
	public PortableTimeSeries() {
		super();
		this.attributes = new ArrayList<String>();
		this.dimensions = new ArrayList<String>();
		this.timeSlots = new ArrayList<String>();
		this.observations = new ArrayList<Double>();
		this.status = new ArrayList<String>();
		this.obsLevelAttributes = new Hashtable<String, ArrayList<String>>();
	}

	public List<String> getAttributes() {
		return attributes;
	}
	public String getAttributeValue(String code){
		String value = null;
		for (Iterator<String> iterator = attributes.iterator(); iterator.hasNext();) {
			String attr = (String) iterator.next();
			String[] tokens = attr.split("\\s*=\\s*");
			String key = tokens[0];
			String tmp = tokens[1];
			if(key != null && !key.isEmpty() && tmp != null && key.equals(code)){
				value = tmp;
				break;
			}		
		}
		return value;
	}

	public String[] getAttributesArray() {
		return attributes.toArray(new String[0]);
	}
	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	public void addAttribute(String attribute) {
		this.attributes.add(attribute);
	}
	public List<String> getDimensions() {
		return dimensions;
	}
	public String getDimensionValue(String code){
		String value = null;
		for (Iterator<String> iterator = dimensions.iterator(); iterator.hasNext();) {
			String dim = (String) iterator.next();
			String[] tokens = dim.split("\\s*=\\s*");
			String key = tokens[0];
			String tmp = tokens[1];
			if(key != null && !key.isEmpty() && tmp != null && key.equals(code)){
				value = tmp;
				break;
			}
		}
		return value;
	}
	public String[] getDimensionsArray() {
		return dimensions.toArray(new String[0]);
	}
	public void setDimensions(List<String> dimensions) {
		this.dimensions = dimensions;
	}
	public void addDimension(String dimension) {
		this.dimensions.add(dimension);
	}
	public void addObservation(String observation, String timeSlot, Hashtable<String, String> attributes){
		// we'll be very easy here. 
		if(observation == null || observation.isEmpty()){
			logger.info(getName() + ": missing observation for time slot: " + timeSlot + ", I'll set a NaN.");
			this.observations.add(new Double("NaN"));
		}
		else{
			try {
				this.observations.add(new Double(observation));
			} catch (NumberFormatException  e) {
				logger.info(getName() + ": found invalid observation for time slot: " + timeSlot + ", I'll set a NaN.");
				this.observations.add(new Double("NaN"));
			}
		}
		if(timeSlot == null || timeSlot.isEmpty()){
			logger.info(getName() + ": a time slot is missing. This is not a well formed time series. I'll set a blank character.");
			timeSlot = "";
		}
		this.timeSlots.add(timeSlot);
		
		if(attributes != null){
			for (Iterator<String> iterator = attributes.keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				//backward compatibility, to be removed in a couple of versions
				if(key.equals("OBS_STATUS")){
					this.status.add(attributes.get(key));
				}
				if(obsLevelAttributes.containsKey(key)){
					obsLevelAttributes.get(key).add(attributes.get(key));
				}
				else{
					//new attribute
					ArrayList<String> newattr = new ArrayList<String>();
					for (int i = 0; i < timeSlots.size() - 1; i++) {
						newattr.add("");
					}
					newattr.add(attributes.get(key));
					obsLevelAttributes.put(key, newattr);
				}
			}
		}
		//now add empty slots for all attributes that are not present
		if(obsLevelAttributes != null){
			for (Iterator<ArrayList<String>> iterator = obsLevelAttributes.values().iterator(); iterator.hasNext();) {
				ArrayList<String> tmp = (ArrayList<String>) iterator.next();
				if(tmp.size() < timeSlots.size()){
					tmp.add("");
				}
			}
		}
	}
	public void setObservations(List<Double> obs) throws DataStructureException{
		if(obs.size() == this.timeSlots.size()){
			this.observations = obs;
		}
		else{
			throw new DataStructureException("Error setting data in time series. Wrong observation number.");
		}
	}
	public List<Double> getObservations() {
		return observations;
	}
	public Double[] getObservationsArray() {
		return observations.toArray(new Double[0]);
	}
	public void setTimeSlots(List<String> dates) throws DataStructureException{
		if(dates.size() == this.observations.size()){
			this.timeSlots = dates;
		}
		else{
			throw new DataStructureException("Error setting dates in time series. Wrong dates number.");
		}
	}
	public List<String> getTimeSlots() {
		return timeSlots;
	}
	public String[] getTimeSlotsArray() {
		return timeSlots.toArray(new String[0]);
	}
	@Deprecated
	public List<String> getStatus() {
		return this.status;
	}
	@Deprecated
	public String[] getStatusArray() {
		return this.status.toArray(new String[0]);
	}
	
	public List<String> getObsLevelAttributesNames() {
		return Collections.list(obsLevelAttributes.keys());
	}

	public String[] getObsLevelAttributesNamesArray() {
		return getObsLevelAttributesNames().toArray(new String[0]);
	}

	public List<String> getObsLevelAttributes(String attributeName) {
		return obsLevelAttributes.get(attributeName);
	}
	
	public String[] getObsLevelAttributesArray(String attributeName) {
		return getObsLevelAttributes(attributeName).toArray(new String[0]);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		String name = this.name;
		if(name == null || name.isEmpty()){
			// we determine the name in the SDMX way
			if(dimensions.size() > 0){
				if(dataflow != null && !dataflow.isEmpty()){
					name = this.dataflow + ".";
				}
				else{
					name = "";
				}
				String delims = "[ =]";
				for (Iterator<String> iterator = dimensions.iterator(); iterator.hasNext();) {
					String dim = (String) iterator.next();
					String[] tokens = dim.split(delims);
					String value = tokens[1];
					name += value;
					if(iterator.hasNext()){
						name += ".";
					}
				}
			}
		}
		return name;
	}
	
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getDataflow() {
		return dataflow;
	}
	public void setDataflow(String dataflow) {
		this.dataflow = dataflow;
	}
	
	public void reverse(){
		Collections.reverse(this.observations);
		Collections.reverse(this.timeSlots);
		for (Iterator<String> iterator = obsLevelAttributes.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			ArrayList<String> attrs = obsLevelAttributes.get(key);
			Collections.reverse(attrs);
			obsLevelAttributes.put(key, attrs);
		}
	}
	
	public String toString(){
		String buffer = "";
		buffer += "\nName: " + getName();
		buffer += "\nFrequency: " + frequency;
		buffer += "\nAttributes: " + attributes;
		buffer += "\nDimensions: " + dimensions;
		buffer += "\nVALUES: ";
		buffer += observations;
		buffer += "\nTIMES:";
		buffer += timeSlots;
		buffer += "\nOBSERVATION ATTRIBUTES:";
		buffer += obsLevelAttributes;
		
		return buffer;
	}


}
