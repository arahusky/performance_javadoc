/*
 Copyright 2014 Jakub Naplava
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cuni.mff.d3s.tools.perfdoc.server;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author arahusky
 */
public class MethodMeasurer {

    private String method;

    private String generator;

    private int rangeValue;

    private ArrayList<Object> data = new ArrayList<Object>();

    public MethodMeasurer(String data) {
        JSONParser parser = new JSONParser();
        parser.parseData(data);
        
        for (int i = 0; i<this.data.size(); i++)
        {
            System.out.println(this.data.get(i));
        }
    }

    public MethodMeasurer(String method, String generator, int rangeValue, ArrayList<Object> data) {
        this.method = method;
        this.generator = generator;
        this.rangeValue = rangeValue;
        this.data = data;
    }

    public JSONObject measureTime()
    {
        
        return new JSONObject();
    }
    
    /**
     * private class that parses incoming JSON and the result saves in the
     * MethodMeasure instance
     */
    private class JSONParser {

        private void parseData(String parseData) {
            JSONObject obj = new JSONObject(parseData);

            method = obj.getString("testedMethod");
            generator = obj.getString("generator");
            rangeValue = obj.getInt("rangeValue");

            JSONArray dataArray = obj.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                data.add(dataArray.get(i));
            }
            
            normalize();
        }
        
        /**
         * incoming data may contain stuff like "0 to 0", which should be converted to "0"
         */
        private void normalize()
        {
            for (int i = 0; i<data.size(); i++)
            {
                if (i != rangeValue)
                {
                    String item = (String) data.get(i);
                    
                    if (item.contains(" to ")) {
                        String[] chunks = item.split(" to ");
                        if (chunks.length == 2 && (chunks[0].equals(chunks[1]))) {                        
                        data.set(i, chunks[0]);
                        }
                    }
                }
            }
        }
    }
}
