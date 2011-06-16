/**
 * This file is part of osmnavigation by Marcus Wolschon <a href="mailto:Marcus@Wolscon.biz">Marcus@Wolscon.biz</a>.
 * You can purchase support for a sensible hourly rate or
 * a commercial license of this file (unless modified by others) by contacting him directly.
 *
 *  osmnavigation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  osmnavigation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with osmnavigation.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************
 * Editing this file:
 *  -For consistent code-quality this file should be checked with the
 *   checkstyle-ruleset enclosed in this project.
 *  -After the design of this file has settled it should get it's own
 *   JUnit-Test that shall be executed regularly. It is best to write
 *   the test-case BEFORE writing this class and to run it on every build
 *   as a regression-test.
 *
 *  Created: 08.11.2007
 */
package org.openstreetmap.travelingsalesman.routing.metrics;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.openstreetmap.osm.ConfigurationSection;
import org.openstreetmap.osm.Settings;
import org.openstreetmap.osm.Plugins.IPlugin;
import org.openstreetmap.osm.data.IDataSet;
import org.openstreetmap.osm.data.coordinates.LatLon;
import org.openstreetmap.travelingsalesman.routing.Route.RoutingStep;

import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
/*JMG*/
/**
 * This metric simply calculated the length of a way-
 * segment.<br/>
 * It is the most trivial metric you can have.
 * @author <a href="mailto:Marcus@Wolschon.biz">Marcus Wolschon</a>
 */
public class BlindRouteMetric implements IRoutingMetric {

    /**
     * The map we operate on.
     */
    private IDataSet myMap;

    /**
     * This plugin has no  settings, thus this method returns null
     * as described in {@link IPlugin#getSettings()}.
     * @return null
     */
    public ConfigurationSection getSettings() {
        return null;
    }

    /**
     * ${@inheritDoc}.
     */
    public double getCost(final RoutingStep aSegment) {
    	List<WayNode> nodes = aSegment.getNodes();
    	double cost = 0;
    	Way way = aSegment.getWay();
    	boolean sound = true;
    	boolean tactile = true;
    	Collection<Tag> tags = way.getTags();
    	for(Tag t:tags)
    	{
    		if(t.getKey().equals("highway") && t.getValue().equals("obstacle"))
			{
				if (cost < Double.MAX_VALUE) cost = Double.MAX_VALUE;
			}
    		if(Settings.getInstance().get("blind-sound", "0").equals("1"))
    		{
    			if(t.getKey().equals("traffic_light_sound"))
    			{
    				if( t.getValue().equals("no") && cost < Double.MAX_VALUE)
    					cost+= 100;
    				sound = false;
    			}
    		}
    		else if(Settings.getInstance().get("blind-sound", "0").equals("2"))
    		{
    			if(t.getKey().equals("traffic_light_sound"))
    			{
    				if( t.getValue().equals("no"))
    					if (cost < Double.MAX_VALUE) cost = Double.MAX_VALUE;
    				sound = false;
    			}
    		}
    		if(Settings.getInstance().get("blind-tactile", "0").equals("1"))
    		{
    			if(t.getKey().equals("tactile_paving"))
    			{
    				if( t.getValue().equals("no") && cost < Double.MAX_VALUE)
    					cost+= 100;
    				tactile = false;
    			}
    		}
    		else if(Settings.getInstance().get("blind-tactile", "0").equals("2"))
    		{
    			if(t.getKey().equals("tactile_paving"))
    			{
    				if( t.getValue().equals("no"))
    					if (cost < Double.MAX_VALUE) cost = Double.MAX_VALUE;
    				tactile = false;
    			}
    		}
    		if(t.getKey().equals("highway")&& t.getValue().equals("crossing") && cost < Double.MAX_VALUE)
    			cost += 5; //More cost for crossings, less crossing is better for blind people.
    	}
    	if(sound) //It means there is no sound tag and there is no information about it
    	{
    		if(Settings.getInstance().get("blind-sound", "0").equals("1") && cost < Double.MAX_VALUE)
    			cost += 100;
    		else if(Settings.getInstance().get("blind-sound", "0").equals("2"))
    			if (cost < Double.MAX_VALUE) cost = Double.MAX_VALUE;
    	}
    	if(tactile) //It means there is no tactile tag and there is no information about it
    	{
    		if(Settings.getInstance().get("blind-tactile", "0").equals("1") && cost < Double.MAX_VALUE)
    			cost += 100;
    		else if(Settings.getInstance().get("blind-tactile", "0").equals("2"))
    			if (cost < Double.MAX_VALUE) cost = Double.MAX_VALUE;
    	}
    	WayNode lastNode = null;
        return cost;
    }

// This method isn't requires by any interface
//    /**
//     * Calculate the distance between 2 adjectant nodes.<br/>
//     * We return Double.MAX_VALUE if one of the nodes is null.
//     * @param aStart the first node
//     * @param aEnd the second node
//     * @return the distance between them.
//     */
    private double getCost(final Node aStart, final Node aEnd) {
        if (aStart == null) {
            return Double.MAX_VALUE;
        }
        if (aEnd == null) {
            return Double.MAX_VALUE;
        }
        double dist =  LatLon.distanceInMeters(aStart.getLatitude(), aStart.getLongitude(),
                                           aEnd.getLatitude(), aEnd.getLongitude());
        return dist;
    }

    /**
     * This metric allways returns 0 for crossings.
     * @param crossing the crossing we tage
     * @param from the way+node we come from
     * @param to the way+node we go to
     * @return a cost. Guaranteed to be >=0.
     */
    public double getCost(final Node crossing, final RoutingStep from, final RoutingStep to) {
    	return 0;
    }

    /**
     * @return the map we operate on
     */
    public IDataSet getMap() {
        return myMap;
    }

    /**
     * @param aMap the map we operate on
     */
    public void setMap(final IDataSet aMap) {
        myMap = aMap;
    }
    
    private int getHeight(WayNode node) {
        // TODO add your handling code here:
	    URL u;
	      InputStream is = null;
	      String s = null;
	    //Open the URL for reading
	      String latitud = Double.toString(myMap.getNodeByID(node.getNodeId()).getLatitude());
	      String longitud = Double.toString(myMap.getNodeByID(node.getNodeId()).getLongitude());
	      int height = 0;

	    try {
	      u = new URL("http://api.geonames.org/astergdem?lat="+ latitud +"&lng="+ longitud +"&username=demo");
	      try {
	    	  is = u.openStream();         // throws an IOException
	    	  BufferedReader d = new BufferedReader(new InputStreamReader(is));
	          while ((s = d.readLine()) != null) {
	              height = Integer.parseInt(s);
	           }
	      } // end try
	      catch (Exception e) {
	        System.err.println(e);
	      }
	    } // end try
	    catch (MalformedURLException e) {
	      System.err.println(e);
	    }
    	return height;
    }

}
