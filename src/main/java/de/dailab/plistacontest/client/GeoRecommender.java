
/*
 Copyright (c) 2013, TU Berlin
 Permission is hereby granted, free of charge, to any person obtaining 
 a copy of this software and associated documentation files (the "Software"),
 to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense,
 and/or sell copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included
 in all copies or substantial portions of the Software.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 DEALINGS IN THE SOFTWARE.
 */

package de.dailab.plistacontest.client;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Comparator;

import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.lucene.document.SetBasedFieldSelector;

/*
 * Class to represent the table of items available to be recommended
 */
public class GeoRecommender {

	private DirtyRingBuffer<String, Long> table = new DirtyRingBuffer<String, Long>(
			100);
	
	//private Map <Long, CircularFifoBuffer> clickHistory = new HashMap <Long, CircularFifoBuffer>();
	
	// declare variable for item locales
	private Set<Long> berlin = new HashSet<Long>();
	private Set<Long> cologne = new HashSet<Long>();
	// declare variables for user geography
	//private Set<String> usr_geo_itm_tage = new HashSet<String>(); // this
																	// variable
																	// contain
																	// user geo
																	// -item to
																	// ensure
																	// unique
																	// user's
																	// are
																	// counted
	//private Set<String> usr_geo_itm_ksta = new HashSet<String>();
	private Map<Long, Long> geo_stats_berlin = new HashMap<Long, Long>(); // this
																			// variable
																			// updates
																			// the
																			// counts
																			// of
																			// unique
																			// user's
																			// per
																			// state.
	private Map<Long, Long> geo_stats_nonberlin = new HashMap<Long, Long>();
	private Map<Long, Long> geo_stats_col = new HashMap<Long, Long>(); // this
																		// variables
																		// updates
																		// the
																		// counts
																		// of
																		// unique
																		// user's
																		// per
																		// state.
	private Map<Long, Long> geo_stats_noncol = new HashMap<Long, Long>();
	private Map<Long, Float> geo_stats_probab_berlin = new HashMap<Long, Float>();
	private Map<Long, Float> geo_stats_probab_nonberlin = new HashMap<Long, Float>();
	private Map<Long, Float> geo_stats_probab_col = new HashMap<Long, Float>();
	private Map<Long, Float> geo_stats_probab_noncol = new HashMap<Long, Float>();
	private long counter = 0;
	private int numberofGeoCandidates = 100;

	
	/**
	 * Handle the item update, itemID is ignored
	 * 
	 * @param _item
	 * @return
	 */
	public boolean handleItemUpdate(final RecommenderItem _item) {

		// check the item
		if (_item == null || _item.getItemID() == null
				|| _item.getItemID() == 0L || _item.getDomainID() == null) {
			return false;
		}
		

		Long domainId = _item.getDomainID();
		Long itmId = _item.getItemID();
		Long usr = _item.getUserID();
		Long usrGeo = _item.getUserGeo();
		String url=_item.getURL();
		
		
		
		// String itm_url=_item.getURL();

		if (url != null) {
			if (url.contains("http://www.tagesspiegel.de/berlin/")) {
				berlin.add(itmId);

			}

			if (url.contains("http://www.ksta.de/koeln/")) {
				cologne.add(itmId);

			}
			return true;

		}

		
		counter++;
		// add the item to the table
		table.addValueByKey(_item.getDomainID() + "", _item.getItemID());
		
		
		
		
		if (usr != null && usrGeo != null) {
			//String usr_geo_itm_key = usr.toString().concat(
					//usrGeo.toString().concat(itmId.toString()));
			if (domainId == 1677L) {
				//usr_geo_itm_tage.add(usr_geo_itm_key);
				//if (!usr_geo_itm_tage.contains(usr_geo_itm_key)) {
					if (berlin.contains(itmId)) {
						if (geo_stats_berlin.get(usrGeo) != null) {
							geo_stats_berlin.put(usrGeo,
									geo_stats_berlin.get(usrGeo) + 1);
						} else {
							geo_stats_berlin.put(usrGeo, 1L);
						}

					} else {
						if (geo_stats_nonberlin.get(usrGeo) != null) {
							geo_stats_nonberlin.put(usrGeo,
									geo_stats_nonberlin.get(usrGeo) + 1);
						} else {
							geo_stats_nonberlin.put(usrGeo, 1L);
						}

					}
				}

				else if (domainId == 418L) {
				//}

				//usr_geo_itm_ksta.add(usr_geo_itm_key);
				//if (!usr_geo_itm_ksta.contains(usr_geo_itm_key)) {
					if (cologne.contains(itmId)) {
						if (geo_stats_col.get(usrGeo) != null) {
							geo_stats_col.put(usrGeo,
									geo_stats_col.get(usrGeo) + 1);
						} else {
							geo_stats_col.put(usrGeo, 1L);
						}

					} else {
						if (geo_stats_noncol.get(usrGeo) != null) {
							geo_stats_noncol.put(usrGeo,
									geo_stats_noncol.get(usrGeo) + 1);
						} else {
							geo_stats_noncol.put(usrGeo, 1L);
						}

					}
				}

			}

		//}
		if (counter % 1000 == 0) {
			geo_stats_probab_berlin.putAll(computeCPD(geo_stats_berlin));
			geo_stats_probab_nonberlin.putAll(computeCPD(geo_stats_nonberlin));
			geo_stats_probab_col.putAll(computeCPD(geo_stats_col));
			geo_stats_probab_noncol.putAll(computeCPD(geo_stats_noncol));
			
			
		}
		

		return true;
	}
	
	
  //This function takes the counts of state-level frequency visits to a certain local as argument, computes and returns conditional probability distributions P(geoState|category).
	public Map<Long, Float> computeCPD(Map<Long, Long> tobesumed) {
		
		Map<Long, Float> cpd = new HashMap<Long, Float>();
		Long sum = 0L;
		for (Long f : tobesumed.values()) {
			sum += f;
		}
		
		for (Long k : tobesumed.keySet()) {
			cpd.put(k, (float) (tobesumed.get(k)/sum));
		}

		return cpd;
	}

	//This a generic function that takes a hashmap and sorts it by value, and returns the sorted keys as a list object. 
	public <K, V extends Comparable<? super V>> List<Long> SortedByValues(
			Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(
				map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		List<Long> top_k = new ArrayList<Long>();

		for (Map.Entry<K, V> entry : sortedEntries) {
			Long key = (Long) entry.getKey();
			top_k.add(key);

		}

		return top_k;
	}

	//This function generates the geographical recommendations. For a given state, all items from one locale have the same score. So practically, it just choices items either from Berlin and nobn-berlin, or col and noncol
	public List<Long> geoRec(Set<Long> candidateset, Long domain, Long userG) {
		Map<Long, Float> geomap = new HashMap<Long, Float>();

		for (Long each : candidateset) {
			if (domain == 1677L) {
				if (berlin.contains(each)) {
					if (geo_stats_probab_berlin.get(userG) != null) {
						geomap.put(each, geo_stats_probab_berlin.get(userG));
					}

				} else {
					if (geo_stats_probab_nonberlin.get(userG) != null) {
						geomap.put(each, geo_stats_probab_nonberlin.get(userG));
					}
				}

			} else if (domain == 418L) {

				if (cologne.contains(each)) {
					if (geo_stats_probab_col.get(userG) != null) {
						geomap.put(each, geo_stats_probab_col.get(userG));
					}

				} else {
					if (geo_stats_probab_noncol.get(userG) != null) {
						geomap.put(each, geo_stats_probab_noncol.get(userG));
					}
				}
			}

		}
		return SortedByValues(geomap);

	}

	/**
	 * Return something from the buffer.
	 * 
	 * @param _currentRequest
	 * @return
	 */
	public List<Long> getLastItems(final RecommenderItem _currentRequest) {

		Integer numberOfRequestedResults = _currentRequest
				.getNumberOfRequestedResults();
		Long itemID = _currentRequest.getItemID();
		Long domainID = _currentRequest.getDomainID();
		Long userG =_currentRequest.getUserGeo();

		// recompute probabs after every 100 impressions.
		
		// handle invalid values
		if (numberOfRequestedResults == null
				|| numberOfRequestedResults.intValue() < 0
				|| numberOfRequestedResults.intValue() > 10 || domainID == null) {
			return new ArrayList<Long>(0);
		}

		Set<Long> blackListedIDs = new HashSet<Long>();
		blackListedIDs.add(0L);
		blackListedIDs.add(itemID);

		
		//If domain is not Tage or Ksta, then return receny recommendation only
		if(!(domainID.equals(418L) || domainID.equals(1677L))){
			try{
				return new ArrayList<Long>(table.getValuesByKey(domainID + "",
					numberOfRequestedResults, blackListedIDs));
			}
			catch(NullPointerException e)
	        {
	            System.out.print("NullPointerException caught");
	            return new ArrayList<Long>(0);
	        }

		}
		//Set<Long> recency = table.getValuesByKey(domainID + "",
		//		numberOfRequestedResults.intValue(), blackListedIDs);

		// create array list and add  twice the number of requested recency
		// recommendations. Twice because we consider more than the requested items to generate geographical and recency based recommendation
		List<Long> returnResult = new ArrayList<Long>();
		returnResult.addAll(table.getValuesByKey(domainID + "",
				2 * numberOfRequestedResults.intValue(), blackListedIDs));
		System.out.println("\n\n\nThis is the result from Recency only \n\n\n\n" + returnResult);
		// Get the georecommenders scores for the 100 most recent(popular items
		// and rank them
		Set<Long> candidates = table.getValuesByKey(domainID + "",
				numberofGeoCandidates, blackListedIDs);
		// to be continued
		List<Long> geoRecsSorted = new ArrayList<Long>();
		geoRecsSorted = geoRec(candidates, domainID,userG);
		System.out.println("\n\n\nThis is the result from georec only \n\n\n\n" + geoRecsSorted);
		// Set<Long> returnResults = new HashSet<Long>(geoRecsSorted);
		// get the intersection
		returnResult.retainAll(geoRecsSorted);
		// see the difference between the number of required recommendations and
		// intersection
		int sizeDiff = numberOfRequestedResults.intValue()
				- returnResult.size();
		//If the size is bigger, just return and a subset(the number requested) 
		if (sizeDiff < 0) {
			List<Long> r = new ArrayList<Long>();
			r.addAll(returnResult);
			return r.subList(0, numberOfRequestedResults.intValue() + 1);
		} else if (sizeDiff > 0) {

			int part = sizeDiff / 2;
			int part2 = sizeDiff - (part + 1);
			blackListedIDs.addAll(returnResult);
			//If all geographical recommendations are already in the result, add only from recency
			if (geoRecsSorted.size() - returnResult.size() < part2) {
				Set<Long> additional = new HashSet<Long>();
				additional= table.getValuesByKey(domainID + "", sizeDiff, blackListedIDs);
				returnResult.addAll(additional);
				return returnResult;
			}
			

			returnResult.addAll(table.getValuesByKey(domainID + "", part + 1,
					blackListedIDs));

			if (part <= 1) {
				return returnResult;
			}
			int c = 0;
			for (int i = 0; i < geoRecsSorted.size(); i++) {
				if (!returnResult.contains(geoRecsSorted.get(i)) &&  !blackListedIDs.contains(geoRecsSorted.get(i)) ) {
					returnResult.add(geoRecsSorted.get(i));
					c++;

				}
				if (c == part2) {
					break;
				}
			}

		}
		System.out.println("\n\n\nFinal Recommendation\n\n\n\n" + returnResult);
		return returnResult;
	}
}
